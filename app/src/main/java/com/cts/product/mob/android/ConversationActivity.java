package com.cts.product.mob.android;

import android.Manifest;
import android.app.FragmentManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
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
import com.cts.product.mob.adapter.ChatMessage;
import com.cts.product.mob.service.MicrophoneState;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.PriorityQueue;
import java.util.Queue;

import ai.api.AIListener;
import ai.api.AIServiceException;
import ai.api.PartialResultsListener;
import ai.api.android.AIConfiguration;
import ai.api.android.AIService;
import ai.api.model.AIError;
import ai.api.model.AIEvent;
import ai.api.model.AIRequest;
import ai.api.model.AIResponse;
import ai.api.model.Result;
import ai.api.model.Status;
import ai.api.services.GoogleRecognitionServiceImpl;

import static com.cts.product.mob.adapter.ChatMessage.ChatDirection.Received;
import static com.cts.product.mob.adapter.ChatMessage.ChatDirection.Sent;

public class ConversationActivity extends AppCompatActivity implements AIListener, PartialResultsListener, TextToSpeech.OnInitListener {
    private static final String TAG = ConversationActivity.class.getName();
    private final int PERMISSIONS_RECORD_AUDIO = 0x1;

    // Controls
    private FloatingActionButton micButton;
    private TextView speechTextView;
    private FragmentVoiceChat chatRosterFragment;

    // Services
    private AIService aiService;
    private MicrophoneState micState;
    private TextToSpeech tts;
    boolean isTtsReady = false;


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
        findViewById(R.id.button_restart).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Toast.makeText(getApplicationContext(), "Please wait for next release!", Toast.LENGTH_SHORT).show();
                restartConversation();
                clearRoster();
                startConversation();

            }
        });


        // Global controls
        //tts = new TTS(this);
        tts = new TextToSpeech(this, this);
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
        greetings();
        startConversation();

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

    private void greetings() {
        String greet = "Hi";
        Calendar gc = GregorianCalendar.getInstance();
        int hr = gc.get(Calendar.HOUR_OF_DAY);
        if (hr >= 0 && hr < 12) {
            greet = "Good Morning!";
        } else if (hr >= 12 && hr <=19) {
            greet = "Good Afternoon!";
        } else if (hr > 19 && hr <=23) {
            greet = "Good Evening!";
        }
        speakOut(greet);
    }

    private void restartConversation() {
        final AIEvent aiEvent = new AIEvent("EVNT_RESET");
        final AIRequest aiRequest = new AIRequest();
        aiRequest.setEvent(aiEvent);

        new AsyncTask<AIRequest, Void, AIResponse>(){
            @Override
            protected AIResponse doInBackground(AIRequest... requests) {
                try {
                    return aiService.textRequest(requests[0]);
                } catch (AIServiceException e) {
                    Log.e(TAG, e.getMessage(), e);
                }
                return null;
            }

            @Override
            protected void onPostExecute(AIResponse response) {
                if (response != null) {
                    //onResult(response);
                    Toast.makeText(getApplicationContext(), "All context reset.", Toast.LENGTH_SHORT).show();
                }
            }
        }.execute(aiRequest);
    }

    private void startConversation() {
        //final AIContext aiContext = new AIContext("app-input-context");
        //final Map<String, String> maps = new HashMap<>();
        final AIEvent aiEvent = new AIEvent("Welcome");
        final AIRequest aiRequest = new AIRequest();
        aiRequest.setEvent(aiEvent);

        new AsyncTask<AIRequest, Void, AIResponse>(){
            @Override
            protected AIResponse doInBackground(AIRequest... requests) {
                try {
                    return aiService.textRequest(requests[0]);
                } catch (AIServiceException e) {
                    Log.e(TAG, e.getMessage(), e);
                }
                return null;
            }

            @Override
            protected void onPostExecute(AIResponse response) {
                if (response != null) {
                    onResult(response);
                }
            }
        }.execute(aiRequest);


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

    private void updateUserSpeechToRoster(String resolvedSpeech) {
        if (!TextUtils.isEmpty(resolvedSpeech)) {
            chatRosterFragment.updateLastMessage(resolvedSpeech);
            speechTextView.setText("");
        }
    }

    private void addToRosterAndSpeak(String speechText, ChatMessage.ChatDirection direction) {
        addToRosterAndSpeak(null, speechText, direction);
    }

    private void addToRosterAndSpeak(String displayText, String speechText, ChatMessage.ChatDirection direction) {
        if (direction == ChatMessage.ChatDirection.Received) {
            speakOut(speechText);
        }
        final String tmp = TextUtils.isEmpty(displayText)? speechText:displayText;
        if (!TextUtils.isEmpty(tmp))
            chatRosterFragment.addMessage(new ChatMessage(tmp, direction));
    }

    private void clearRoster() {
        chatRosterFragment.clearMessages();;
    }

    /*
     * ---------------- Control Listener Events ------------------
     */

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

                Log.d(TAG, "onResult");

                final Status status = response.getStatus();
                final Result result = response.getResult();
                final String speech = result.getFulfillment().getSpeech();
                final String displayText = result.getFulfillment().getDisplayText();

                Log.i(TAG, "Status code: " + status.getCode());
                Log.i(TAG, "Status type: " + status.getErrorType());
                Log.i(TAG, "Action : " + result.getAction());
                Log.i(TAG, "Resolved Query: " + result.getResolvedQuery());
                Log.i(TAG, "Speech: " + speech);
                Log.i(TAG, "Display Text: " + displayText);

                updateUserSpeechToRoster(result.getResolvedQuery());
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
                speechTextView.setText("");
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
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
    }

    @Override
    protected void onStop() {
        if (aiService != null) {
            aiService.stopListening();
        }
        if (tts != null && tts.isSpeaking()) {
            tts.stop();
        }
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        if (aiService != null) {
            aiService.stopListening();
        }
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        if (aiService != null) {
            aiService.pause();
        }
        if (tts != null && tts.isSpeaking()) {
            tts.stop();
            tts.shutdown();
        }
        super.onPause();
    }

    @Override
    protected void onResume() {
        if (aiService != null) {
            aiService.resume();
        }
        super.onResume();
    }


    private Queue<String> queue = new PriorityQueue<>();
    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            int result = tts.setLanguage(Locale.US);
            if (result == TextToSpeech.LANG_MISSING_DATA
                    || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTS", "This Language is not supported");
            } else {
                isTtsReady = true;
                flushQueue();
                Log.e("TTS", "TTS Initialized");
            }
        } else {
            Log.e("TTS", "Initialization Failed!");
        }
    }

    private void flushQueue() {
        while (!queue.isEmpty()) {
            speakOut(queue.poll());
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {}
        }
    }

    private void speakOut(final String textToSpeak) {
        if (!isTtsReady) {
            queue.add(textToSpeak);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            tts.speak(textToSpeak, TextToSpeech.QUEUE_FLUSH, null, null);
        }else{
            tts.speak(textToSpeak, TextToSpeech.QUEUE_FLUSH, null);
        }
    }
}
