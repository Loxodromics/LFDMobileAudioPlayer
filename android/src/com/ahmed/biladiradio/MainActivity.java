package com.ahmed.biladiradio;

import android.content.ComponentName;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.RemoteException;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.media.session.MediaController;
import android.app.Activity;
import com.ahmed.QAndroidResultReceiver.jniExport.jniExport;

public class MainActivity extends org.qtproject.qt5.android.bindings.QtActivity{
    private static final int STATE_PAUSED = 0;
    private static final int STATE_PLAYING = 1;
    private static jniExport m_jniExport;

    private int mCurrentState;
    private Button mPlayPauseToggleButton;
    public void onDestroy(){
        super.onDestroy();
        //getMediaController().getTransportControls().pause();
        mMediaBrowserCompat.disconnect();
    }
    public void playstation(){
        Intent i = new Intent("android.intent.action.MAIN").putExtra("cmd",1);
        sendBroadcast(i);
    }
    public void pausestation(){
        Intent i = new Intent("android.intent.action.MAIN").putExtra("cmd",0);
        sendBroadcast(i);
    }
    public void setestation(String url){
        Intent i = new Intent("android.intent.action.MAIN").putExtra("url",url);
        sendBroadcast(i);
    }
    public void setnotificationtext(String txt){
        Intent i = new Intent("android.intent.action.MAIN").putExtra("txt",txt);
        Log.d("mMediaControllerCompat", "setnotificationtext "+ txt);
        sendBroadcast(i);
    }
    public void togglestate(){
        if( mCurrentState == STATE_PAUSED ) {
            playstation();
            mCurrentState = STATE_PLAYING;
        } else {
            pausestation();
            mCurrentState = STATE_PAUSED;
        }
    }
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mMediaBrowserCompat = new MediaBrowserCompat(this, new ComponentName(this, BackgroundAudioService.class),
                mMediaBrowserCompatConnectionCallback, getIntent().getExtras());
        mMediaBrowserCompat.connect();
    }
    private MediaBrowserCompat mMediaBrowserCompat;
    private MediaControllerCompat mMediaControllerCompat;

    private MediaBrowserCompat.ConnectionCallback mMediaBrowserCompatConnectionCallback = new MediaBrowserCompat.ConnectionCallback() {

        @Override
        public void onConnected() {
            super.onConnected();
            try {

                mMediaControllerCompat = new MediaControllerCompat(MainActivity.this, mMediaBrowserCompat.getSessionToken());
                mMediaControllerCompat.registerCallback(mMediaControllerCompatCallback);
                Log.d("mMediaControllerCompat", "mMediaControllerCompat +++++++++++++++++++++++++++++++++");
                MediaControllerCompat.setMediaController(MainActivity.this,mMediaControllerCompat);// "http://ice1.somafm.com/groovesalad-128-mp3"
                //Intent i = new Intent("android.intent.action.MAIN").putExtra("url","http://ice1.somafm.com/groovesalad-128-mp3");
                //sendBroadcast(i);
                //getMediaController().getTransportControls().playFromMediaId("http://ice1.somafm.com/groovesalad-128-mp3", null);
            } catch( RemoteException e ) {

            }
        }
    };

    private MediaControllerCompat.Callback mMediaControllerCompatCallback = new MediaControllerCompat.Callback() {
        @Override
        public void onPlaybackStateChanged(PlaybackStateCompat state) {
            super.onPlaybackStateChanged(state);
            if( state == null ) {
                return;
            }
            switch( state.getState() ) {
                case PlaybackStateCompat.STATE_PLAYING: {
                    mCurrentState = STATE_PLAYING;
		    m_jniExport.intMethod(0);
                    Log.d("mMediaControllerCompat", "mCurrentState = STATE_PLAYING;");
                    break;
                }
                case PlaybackStateCompat.STATE_PAUSED: {
                    mCurrentState = STATE_PAUSED;
		    m_jniExport.intMethod(1);
                    Log.d("mMediaControllerCompat", "mCurrentState = STATE_PAUSED;");
                    break;
                }
            }
        }
    };
}
