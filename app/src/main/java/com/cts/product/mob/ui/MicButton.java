package com.cts.product.mob.ui;

import android.content.Context;
import android.view.View;

import com.cts.product.mob.service.MicrophoneState;

import java.util.List;

import ai.api.AIListener;
import ai.api.AIServiceException;
import ai.api.PartialResultsListener;
import ai.api.RequestExtras;
import ai.api.android.AIConfiguration;
import ai.api.android.AIService;
import ai.api.model.AIError;
import ai.api.model.AIRequest;
import ai.api.model.AIResponse;
import ai.api.services.GoogleRecognitionServiceImpl;
import ai.api.ui.SoundLevelButton;


public class MicButton extends SoundLevelButton implements AIListener {
    private static final String TAG = MicButton.class.getName();

    public interface MicButtonListener {
        void onResult(final AIResponse result);
        void onError(final AIError error);
        void onCancelled();
    }

    protected static final int[] STATE_WAITING = {ai.api.R.attr.state_waiting};
    protected static final int[] STATE_SPEAKING = {ai.api.R.attr.state_speaking};
    protected static final int[] STATE_INITIALIZING_TTS = {ai.api.R.attr.state_initializing_tts};

    private float animationStage = 0;
    private boolean animationSecondPhase = false;
    // private final MicButtonListener.WaitingAnimation animation = new MicButtonListener.WaitingAnimation();

    private AIService aiService;
    private MicButton.MicButtonListener resultsListener;
    private PartialResultsListener partialResultsListener;

    private volatile MicrophoneState currentState = MicrophoneState.Normal;



    public MicButton(final Context context) {
        super(context);
    }

    public void initialize(final AIConfiguration config) {
        aiService = AIService.getService(getContext(), config);
        aiService.setListener(this);

        if (aiService instanceof GoogleRecognitionServiceImpl) {
            ((GoogleRecognitionServiceImpl) aiService).setPartialResultsListener(new PartialResultsListener() {
                @Override
                public void onPartialResults(final List<String> partialResults) {
                    if (partialResultsListener != null) {
                        partialResultsListener.onPartialResults(partialResults);
                    }
                }
            });
        }
    }
    public void setResultsListener(MicButtonListener resultsListener) {
        this.resultsListener = resultsListener;
    }
    public void setPartialResultsListener(PartialResultsListener partialResultsListener) {
        this.partialResultsListener = partialResultsListener;
    }



    public void startListening(final RequestExtras requestExtras) {
        if (aiService != null) {
            if (currentState == MicrophoneState.Normal) {
                aiService.startListening(requestExtras);
            }
        } else {
            throw new IllegalStateException("Call initialize method before usage");
        }
    }
    public AIResponse textRequest(final AIRequest request) throws AIServiceException {
        if (aiService != null) {
            return aiService.textRequest(request);
        } else {
            throw new IllegalStateException("Call initialize method before usage");
        }
    }


    @Override
    protected void onClick(View v) {
        super.onClick(v);
        if (aiService != null) {
            switch (currentState) {
                case Normal:
                    aiService.startListening();
                    break;
                case Busy:
                    aiService.cancel();
                    break;
                default:
                    aiService.stopListening();
                    break;
            }
        }
    }

    @Override
    public void onResult(AIResponse result) {
        post(new Runnable() {
            @Override
            public void run() {
                changeState(MicrophoneState.Normal);
            }
        });
        if (resultsListener != null) {
            resultsListener.onResult(result);
        }
    }

    @Override
    public void onError(AIError error) {
        post(new Runnable() {
            @Override
            public void run() {
                changeState(MicrophoneState.Normal);
            }
        });
        if (resultsListener != null) {
            resultsListener.onError(error);
        }
    }

    @Override
    public void onAudioLevel(float level) {
        setSoundLevel(level);
    }

    @Override
    public void onListeningStarted() {
        post(new Runnable() {
            @Override
            public void run() {
                changeState(MicrophoneState.Listening);
            }
        });
    }

    @Override
    public void onListeningCanceled() {
        post(new Runnable() {
            @Override
            public void run() {
                changeState(MicrophoneState.Normal);
            }
        });
        if (resultsListener != null) {
            resultsListener.onCancelled();
        }
    }

    @Override
    public void onListeningFinished() {
        post(new Runnable() {
            @Override
            public void run() {
                changeState(MicrophoneState.Busy);
            }
        });
    }



    public void resume() {
        if (aiService != null) {
            aiService.resume();
        }
    }
    public void pause() {
        cancelListening();
        if (aiService != null) {
            aiService.pause();
        }
    }
    private void cancelListening() {
        if (aiService != null) {
            if (currentState != MicrophoneState.Normal) {
                aiService.cancel();
                changeState(MicrophoneState.Normal);
            }
        }
    }


    protected void changeState(final MicrophoneState toState) {
        switch (toState) {
            case Normal:
                stopProcessingAnimation();
                setDrawSoundLevel(false);
                break;
            case Busy:
                startProcessingAnimation();
                setDrawSoundLevel(false);
                break;
            case Listening:
                stopProcessingAnimation();
                setDrawSoundLevel(true);
                break;
            case Speaking:
                stopProcessingAnimation();
                setDrawSoundLevel(false);
                break;
            case InitializingTts:
                stopProcessingAnimation();
                setDrawSoundLevel(false);
                break;
        }

        currentState = toState;
        refreshDrawableState();
    }

    private void startProcessingAnimation() {
    }
    private void stopProcessingAnimation() {
    }

    protected MicrophoneState getCurrentState() {
        return currentState;
    }


}


