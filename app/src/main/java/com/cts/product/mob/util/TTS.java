package com.cts.product.mob.util;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.util.Log;

import java.util.Locale;
import java.util.PriorityQueue;
import java.util.Queue;

public class TTS extends UtteranceProgressListener implements TextToSpeech.OnInitListener {
    private TextToSpeech tts;
    boolean ready = false;
    private Queue<String> queue = new PriorityQueue<>();

    public TTS(final Context context) {
        this.tts = new TextToSpeech(context, this);
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            ready = true;
            int result = tts.setLanguage(Locale.US);
            if (result == TextToSpeech.LANG_MISSING_DATA
                    || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTS", "This Language is not supported");
            } else {
                //speakOut();

                Log.e("TTS", "TTS Initialized");
            }
        } else {
            ready = false;
            Log.e("TTS", "Initialization Failed!");
        }
    }

    public void speakOut(String text) {
        //tts.speak(text, TextToSpeech.QUEUE_FLUSH, null,null);
        if (!ready) {
            queue.add(text);
            return;
        }
        //queue.clear();
        tts.speak(text, TextToSpeech.QUEUE_FLUSH,null);
    }

    public void close() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
    }

    @Override
    protected void finalize() throws Throwable {
        close();
    }

    @Override
    public void onStart(String utteranceId) {

    }

    @Override
    public void onDone(String utteranceId) {
        if (!queue.isEmpty()) {
            String s;
            while ((s = queue.poll()) != null) {
                speakOut(s);
            }
        }
    }

    @Override
    public void onError(String utteranceId) {

    }
}
