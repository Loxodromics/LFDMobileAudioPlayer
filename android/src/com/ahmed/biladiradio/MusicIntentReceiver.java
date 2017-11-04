package com.ahmed.biladiradio;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.KeyEvent;

/**
 * Created by Ahmed on 10/27/2017.
 */

public class MusicIntentReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("mMediaControllerCompat", "public void onReceive(Context context, Intent intent) {;");

        if (intent.getAction().equals(
                android.media.AudioManager.ACTION_AUDIO_BECOMING_NOISY)) {

            // send an intent to our MusicService to telling it to pause the
            // audio

        } else if (intent.getAction().equals(Intent.ACTION_MEDIA_BUTTON)) {

            KeyEvent keyEvent = (KeyEvent) intent.getExtras().get(Intent.EXTRA_KEY_EVENT);
            if (keyEvent.getAction() != KeyEvent.ACTION_DOWN)
                return;
            Intent i = new Intent("android.intent.action.MAIN").putExtra("state",keyEvent.getKeyCode());
            context.sendBroadcast(i);
        }
    }
}