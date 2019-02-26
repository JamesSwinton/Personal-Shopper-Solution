package com.ses.zebra.pssdemo_2019;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.util.Log;

import com.ses.zebra.pssdemo_2019.Debugging.Logger;

public class TTS {

    private static TextToSpeech textToSpeech;

    public static void init(final Context context) {
        if (textToSpeech == null) {
            textToSpeech = new TextToSpeech(context, i -> Logger.i("TTS", "TTS Initialised"));
        }
    }

    public static void speak(final String text) {
        textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null);
    }
}
