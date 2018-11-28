package com.cts.product.mob.util;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.util.Log;

import java.util.Locale;

public class TTS implements TextToSpeech.OnInitListener{
    private TextToSpeech tts;

    public TTS(final Context context) {
        this.tts = new TextToSpeech(context, this);
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            int result = tts.setLanguage(Locale.US);
            if (result == TextToSpeech.LANG_MISSING_DATA
                    || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTS", "This Language is not supported");
            } else {
                //speakOut();
                Log.e("TTS", "TTS Initialized");
            }
        } else {
            Log.e("TTS", "Initilization Failed!");
        }
    }

    public void speakOut(String text) {
        //tts.speak(text, TextToSpeech.QUEUE_FLUSH, null,null);
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
}
