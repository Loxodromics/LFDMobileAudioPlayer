package net.quatur.filtermusicQt;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.ndk.CrashlyticsNdk;

import net.quatur.QAndroidResultReceiver.jniExport.jniExport;

import io.fabric.sdk.android.Fabric;

public class MainActivity extends org.qtproject.qt5.android.bindings.QtActivity {
    private static final int STATE_PAUSED = 0;
    private static final int STATE_PLAYING = 1;
    private static final int STATE_STOPPED = 2;
    private static jniExport m_jniExport;

    private MediaBrowserAdapter mMediaBrowserAdapter;
    private boolean mIsPlaying;
    private int mCurrentState;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics(), new CrashlyticsNdk());

        m_jniExport = new jniExport();

        mMediaBrowserAdapter = new MediaBrowserAdapter(this);
        mMediaBrowserAdapter.addListener(new MediaBrowserListener());
    }

    @Override
    public void onStart() {
        super.onStart();
        mMediaBrowserAdapter.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
        mMediaBrowserAdapter.onStop();
    }

    public void playstation() {
        try {
            mMediaBrowserAdapter.getTransportControls().play();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void pausestation() {
//        mMediaBrowserAdapter.getTransportControls().pause();
    }

    public void setestation(String url) {
//        mMediaBrowserAdapter.getTransportControls().playFromUri(Uri.parse(url), null);
    }

    public void setnotificationtext(String txt) {
        //TODO
    }

    private class MediaBrowserListener extends MediaBrowserAdapter.MediaBrowserChangeListener {

        @Override
        public void onConnected(@Nullable MediaControllerCompat mediaController) {
            super.onConnected(mediaController);
        }

        @Override
        public void onPlaybackStateChanged(PlaybackStateCompat playbackState) {
            mIsPlaying = playbackState != null &&
                    playbackState.getState() == PlaybackStateCompat.STATE_PLAYING;

            if (playbackState == null) {
                return;
            }
            Log.d("MediaBrowserListener", "playbackState: " + playbackState);
            switch (playbackState.getState()) {
                case PlaybackStateCompat.STATE_PLAYING: {
                    mCurrentState = STATE_PLAYING;
                    Log.d("mMediaControllerCompat", "mCurrentState = STATE_PLAYING;");
                    break;
                }
                case PlaybackStateCompat.STATE_PAUSED: {
                    mCurrentState = STATE_PAUSED;
                    m_jniExport.sendSetTitle("");
                    Log.d("MediaBrowserListener", "mCurrentState = STATE_PAUSED;");
                    break;
                }
                case PlaybackStateCompat.STATE_NONE: {
                    mCurrentState = STATE_STOPPED;
                    m_jniExport.sendSetTitle("");
                    Log.d("MediaBrowserListener", "mCurrentState = STATE_STOPPED;");
                    break;
                }
                default: {
                    Log.d("MediaBrowserListener", "ccccc unknown state");
                    break;
                }
            }
            m_jniExport.sendSetFocus(mCurrentState);
        }

        @Override
        public void onMetadataChanged(MediaMetadataCompat mediaMetadata) {
            if (mediaMetadata == null) {
                return;
            }
        /*
		    mTitleTextView.setText(
			        mediaMetadata.getString(MediaMetadataCompat.METADATA_KEY_TITLE));
			mArtistTextView.setText(
			        mediaMetadata.getString(MediaMetadataCompat.METADATA_KEY_ARTIST));
			mAlbumArt.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher));*/
        }
    }

}
/*
    private int mCurrentState;

	private BackgroundBroadcastReceiver titleBroadcastReceiver = new BackgroundBroadcastReceiver() {

//        @Override
//        public void onReceive(Context context, Intent intent) {
//            super.onReceive(context, intent);
//            Log.d("mMediaControllerCompat", "onReceive");
//            try {
//                Bundle extra = intent.getExtras();
//                String title = extra.getString("title");
//                Log.d("mMediaControllerCompat", "title: " + title);
//                if (title != "")
//                    m_jniExport.sendSetTitle("jo" + title);
//            }
//            catch(Exception e) {
//                Log.d("BroadcastReceiver", "broadcast exception " + e.toString());
////			Log.d("BroadcastReceiver", "broadcast exception " + e.printStackTrace());
//                e.printStackTrace();
//            }
//        }
//
//        @Override
//        public void gotTitle(String title) {
//            Log.d("mMediaControllerCompat", "gotTitle2: " + title);
//            if (title != "")
//                m_jniExport.sendSetTitle("jo" + title);
//        };
    };

    public void onDestroy() {
		super.onDestroy();
		//getMediaController().getTransportControls().pause();
		mMediaBrowserCompat.disconnect();
		}

	public void playstation() {
		Intent i = new Intent("android.intent.action.MAIN").putExtra("cmd", 1);
		sendBroadcast(i);
		}

	public void pausestation() {
		Intent i = new Intent("android.intent.action.MAIN").putExtra("cmd", 0);
		sendBroadcast(i);
		}

	public void setestation(String url) {
		Intent i = new Intent("android.intent.action.MAIN").putExtra("url", url);
		sendBroadcast(i);
		}

	public void setnotificationtext(String txt) {
		Intent i = new Intent("android.intent.action.MAIN").putExtra("txt", txt);
		Log.d("mMediaControllerCompat", "setnotificationtext " + txt);
		sendBroadcast(i);
		}

//	public void togglestate() {
//		if ( (mCurrentState == STATE_PAUSED) ||
//		     (mCurrentState == STATE_PAUSED) ) {
//			playstation();
//			mCurrentState = STATE_PLAYING;
//		} else {
//			pausestation();
//			mCurrentState = STATE_PAUSED;
//		}
//	}

    public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mMediaBrowserCompat = new MediaBrowserCompat(this, new ComponentName(this, BackgroundAudioService.class),
		        mMediaBrowserCompatConnectionCallback, getIntent().getExtras());
				mMediaBrowserCompat.connect();
		Fabric.with(this, new Crashlytics(), new CrashlyticsNdk());
		titleBroadcastReceiver.m_jniExport = m_jniExport;
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
				MediaControllerCompat.setMediaController(MainActivity.this, mMediaControllerCompat);// "http://ice1.somafm.com/groovesalad-128-mp3"
				//Intent i = new Intent("android.intent.action.MAIN").putExtra("url","http://ice1.somafm.com/groovesalad-128-mp3");
				//sendBroadcast(i);
				//getMediaController().getTransportControls().playFromMediaId("http://ice1.somafm.com/groovesalad-128-mp3", null);
				} catch (RemoteException e) {

			}
		}
	};

    private MediaControllerCompat.Callback mMediaControllerCompatCallback = new MediaControllerCompat.Callback() {
		@Override
		public void onPlaybackStateChanged(PlaybackStateCompat state) {
			super.onPlaybackStateChanged(state);
			if (state == null) {
				return;
			}
		    Log.d("mMediaControllerCompat", "state: " + state);
			switch (state.getState()) {
				case PlaybackStateCompat.STATE_PLAYING: {
					mCurrentState = STATE_PLAYING;
					Log.d("mMediaControllerCompat", "mCurrentState = STATE_PLAYING;");
					break;
				}
			    case PlaybackStateCompat.STATE_PAUSED: {
					mCurrentState = STATE_PAUSED;
					m_jniExport.sendSetTitle("");
					Log.d("mMediaControllerCompat", "mCurrentState = STATE_PAUSED;");
					break;
				}
			    case PlaybackStateCompat.STATE_NONE: {
					mCurrentState = STATE_STOPPED;
					m_jniExport.sendSetTitle("");
					Log.d("mMediaControllerCompat", "mCurrentState = STATE_STOPPED;");
					break;
				}
			    default: {
					Log.d("mMediaControllerCompat", "ccccc unknown state");
					break;
				}
			}
		    m_jniExport.sendSetFocus(mCurrentState);
		}
	};
*/

