package com.cts.product.mob.android;

import android.Manifest;
import android.app.FragmentManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.cts.product.mob.R;
import static com.cts.product.mob.adapter.ChatMessage.ChatDirection.Sent;
import static com.cts.product.mob.adapter.ChatMessage.ChatDirection.Received;
import com.cts.product.mob.adapter.ChatMessage;
import com.cts.product.mob.service.MicrophoneState;
import com.cts.product.mob.util.TTS;

import java.util.List;

import ai.api.AIListener;
import ai.api.PartialResultsListener;
import ai.api.android.AIConfiguration;
import ai.api.android.AIService;
import ai.api.model.AIError;
import ai.api.model.AIResponse;
import ai.api.model.Result;
import ai.api.model.Status;
import ai.api.services.GoogleRecognitionServiceImpl;

public class ConversationActivity extends AppCompatActivity implements AIListener, PartialResultsListener {
    private static final String TAG = ConversationActivity.class.getName();
    private final int PERMISSIONS_RECORD_AUDIO = 0x1;

    // Controls
    private FloatingActionButton micButton;
    private TextView speechTextView;
    private FragmentVoiceChat chatRosterFragment;

    // Services
    private AIService aiService;
    private MicrophoneState micState;
    private TTS tts;


    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversation);

        // Window controls - static
        findViewById(R.id.button_close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent, savedInstanceState);
                finish();
            }
        });
        findViewById(R.id.button_info).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "Please wait for further release!", Toast.LENGTH_SHORT).show();
            }
        });


        // Global controls
        tts = new TTS(this);
        this.micButton = findViewById(R.id.button_mic);
        this.speechTextView = findViewById(R.id.speechTextView);
        this.chatRosterFragment = FragmentVoiceChat.newInstance("","");

        final FragmentManager fragmentManager = getFragmentManager();
        // Clear existing history
        fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        fragmentManager.beginTransaction()
                .replace(R.id.voice_chat_frame, chatRosterFragment, FragmentVoiceChat.TAG)
                .addToBackStack(null)
                .commit();


        initAIService();

    }


    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        requestRecordAudioPermission();
    }

    private void initAIService() {
        final AIConfiguration config = new AIConfiguration(
                "c2829da0d1124a9c9592e7a9150e4c17",
                AIConfiguration.SupportedLanguages.English,
                AIConfiguration.RecognitionEngine.System);

        if (aiService != null) {
            aiService.pause();
        }

        aiService = AIService.getService(this, config);
        aiService.setListener(this);

        if (aiService instanceof GoogleRecognitionServiceImpl) {
            GoogleRecognitionServiceImpl googleRecognitionService = (GoogleRecognitionServiceImpl) aiService;
            googleRecognitionService.setPartialResultsListener(this);
        }

        micButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startListening();
            }
        });

    }


    /*
     * ------------------- Private methods ---------------------
     */

    public void startListening() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (aiService != null) {
                    aiService.startListening();
                }
                changeState(MicrophoneState.Listening);
            }
        });
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

        micState = toState;
        refreshDrawableState();
    }

    private void startProcessingAnimation() {
    }
    private void stopProcessingAnimation() {
    }
    private void setDrawSoundLevel(boolean flag) {
    }
    private void refreshDrawableState() {
    }

    protected void requestRecordAudioPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.RECORD_AUDIO)) {
                Toast.makeText(this, "Please grant permissions to record your voice", Toast.LENGTH_LONG).show();
                //Give user option to still opt-in the permissions
                ActivityCompat.requestPermissions(this,
                        new String[] {Manifest.permission.RECORD_AUDIO},
                        PERMISSIONS_RECORD_AUDIO);
            } else {
                // Show user dialog to grant permission to record audio
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.RECORD_AUDIO},
                        PERMISSIONS_RECORD_AUDIO);
            }
        }
        /*else if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
            //Go ahead with recording audio now
            recordAudio();
        }*/
    }

    private void updateUserSpeechToRoster() {
        if (TextUtils.isEmpty(speechTextView.getText())) {
            chatRosterFragment.updateLastMessage(speechTextView.getText().toString());
            speechTextView.setText("");
        }
    }

    private void addToRosterAndSpeak(String speechText, ChatMessage.ChatDirection direction) {
        addToRosterAndSpeak(null, speechText, direction);
    }

    private void addToRosterAndSpeak(String displayText, String speechText, ChatMessage.ChatDirection direction) {
        if (tts != null) {
            tts.speakOut(speechText);
        }
        final String tmp = TextUtils.isEmpty(displayText)? speechText:displayText;
        chatRosterFragment.addMessage(new ChatMessage(tmp, direction));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        //super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case PERMISSIONS_RECORD_AUDIO: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay!
                    //recordAudio();
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(this, "Permissions Denied to record audio", Toast.LENGTH_LONG).show();
                }
                return;
            }
        }
    }


    /*
     * ---------------- AI Listener Events ------------------
     */

    @Override
    public void onResult(final AIResponse response) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                changeState(MicrophoneState.Normal);
                updateUserSpeechToRoster();

                Log.d(TAG, "onResult");

                final Status status = response.getStatus();
                final Result result = response.getResult();
                final String speech = result.getFulfillment().getSpeech();
                final String displayText = result.getFulfillment().getDisplayText();

                Log.i(TAG, "Status code: " + status.getCode());
                Log.i(TAG, "Status type: " + status.getErrorType());
                Log.i(TAG, "Action : " + result.getAction());
                Log.i(TAG, "Speech: " + speech);
                Log.i(TAG, "Display Text: " + displayText);

                addToRosterAndSpeak(displayText, speech, Received);

            }
        });
    }

    @Override
    public void onError(final AIError error) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                changeState(MicrophoneState.Normal);
                chatRosterFragment.addMessage(new ChatMessage(error.getMessage(), Received));
            }
        });
    }

    @Override
    public void onAudioLevel(float level) {

    }

    @Override
    public void onListeningStarted() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                changeState(MicrophoneState.Listening);
            }
        });
    }

    @Override
    public void onListeningCanceled() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                changeState (MicrophoneState.Normal);
            }
        });
    }

    @Override
    public void onListeningFinished() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                changeState (MicrophoneState.Busy);
                if (!TextUtils.isEmpty(speechTextView.getText())) {
                    addToRosterAndSpeak(speechTextView.getText().toString(), Sent);
                }
            }
        });
    }

    @Override
    public void onPartialResults(List<String> partialResults) {
        final String result = partialResults.get(0);
        if (!TextUtils.isEmpty(result)) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (speechTextView != null)
                        speechTextView.setText(result);
                }
            });
        }
    }

    /*
     *  ---------------------- View Listeners  ------------------------
     */

    @Override
    protected void onStop() {
        super.onStop();
        if (aiService != null) {
            aiService.stopListening();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (aiService != null) {
            aiService.stopListening();
        }
        if (tts != null) {
            tts.close();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (aiService != null)
            aiService.pause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (aiService != null)
            aiService.resume();
    }

}
