package net.quatur.filtermusicQt;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.ResultReceiver;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaBrowserServiceCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaButtonReceiver;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v7.app.NotificationCompat;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.KeyEvent;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import de.umass.lastfm.Album;
import de.umass.lastfm.ImageSize;
import de.umass.lastfm.Track;

import net.quatur.QAndroidResultReceiver.jniExport.jniExport;

public class BackgroundAudioService extends MediaBrowserServiceCompat implements MediaPlayer.OnCompletionListener, AudioManager.OnAudioFocusChangeListener {

	public static final String COMMAND_EXAMPLE = "command_example";
	private String mTitle = "Loading ...", mArtist = "", mAlbumurl = "", mURL = "", mNotification = "filtermusic", mStreamTitle = "";

	private Bitmap mAlbumBitMap = null;
	private MediaPlayer mMediaPlayer;
	private MediaSessionCompat mMediaSessionCompat;
	private static final String TAG = "RADIOSERVICE";
	public static String APIKEY = "4de0532fe30150ee7a553e160fbbe0e0";
	IcyStreamMeta icy;
	private static jniExport m_jniExport;

	private BroadcastReceiver Buttonreceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			try {
			int state = intent.getIntExtra("state", -1);
			int cmd = intent.getIntExtra("cmd", -1);
			String url = intent.getStringExtra("url");
			String txt = intent.getStringExtra("txt");
			Log.d("mMediaControllerCompat", "++++++++++++Kintent.getIntExtra " + state + cmd + url + txt);

			if (cmd == 1) {
				playFunc();
			} else if (cmd == 0) {
			    Log.d("mMediaControllerCompat", "cmd pause/stop");
				stopFunc();
//			    pauseFunc();
            }

		    if (cmd == -1 && state == -1) {
				if (url != null) {
					Log.d("mMediaControllerCompat", "+if (url != null) {a ");
					prepare(url);
				} else {
				    setnotification(txt);
					Log.d("mMediaControllerCompat", "+setnotification(txt); " + txt);

				}
			}
		    switch (state) {
				case KeyEvent.KEYCODE_HEADSETHOOK:
				    break;
				case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
				    Log.d("mMediaControllerCompat", "++++++++++++KEYCODE_MEDIA_PLAY_PAUSE");
					if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
//						pauseFunc();
                        Log.d("mMediaControllerCompat", "KEYCODE_MEDIA_PLAY_PAUSE");
						stopFunc();
					} else {
					    playFunc();
					}
				    break;
				case KeyEvent.KEYCODE_MEDIA_PLAY:
				    Log.d("mMediaControllerCompat", "++++++++++++KEYCODE_MEDIA_PLAY");

					break;
				case KeyEvent.KEYCODE_MEDIA_PAUSE:
				    Log.d("mMediaControllerCompat", "++++++++++++KEYCODE_MEDIA_PAUSE");
					break;
				case KeyEvent.KEYCODE_MEDIA_NEXT:

				    break;
				case KeyEvent.KEYCODE_MEDIA_PREVIOUS:
				    break;
				default:
				    break;
			}

		}
	    catch(Exception e) {
			Log.d("BroadcastReceiver", "broadcast exception " + e.toString());
//			Log.d("BroadcastReceiver", "broadcast exception " + e.printStackTrace());
            e.printStackTrace();
			}
		}

	};

    private BroadcastReceiver mNoisyReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			Log.d("BroadcastReceiver", " public void onReceive(Context context, Intent intent+");

			if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
				/// what is this?
				Log.d("BroadcastReceiver", "  mMediaPlayer != null && mMediaPlayer.isPlaying() ");
				pauseFunc();
			}
		}
	};

    public void pauseFunc() {
		Log.d("mMediaControllerCompat", "pauseFunc()+++++++++++++++++++++++++++++++++");
		if (mMediaPlayer.isPlaying()) {
			mMediaPlayer.pause();

			stopMeta();
			setMediaPlaybackState(PlaybackStateCompat.STATE_PAUSED);
			showPausedNotification();
		}
	}

    public void stopFunc() {
		Log.d("mMediaControllerCompat", "stopFunc()+++++++++++++++++++++++++++++++++");
		if (mMediaPlayer.isPlaying()) {
			mMediaPlayer.stop();
//			mMediaPlayer.release();
        }
	    stopMeta();
		setMediaPlaybackState(PlaybackStateCompat.STATE_NONE);
		showPausedNotification();
	}

    public void playFunc() {
		Log.d("mMediaControllerCompat", "playFunc()+++++++++++++++++++++++++++++++++");
		getMeta(mURL);
//		if (!successfullyRetrievedAudioFocus()) {
//			return;
//		}

        if (playing_Prepared)
		{
			Log.d("mMediaControllerCompat", "playing_Prepared");
			mMediaPlayer.start();
			mMediaSessionCompat.setActive(true);
			setMediaPlaybackState(PlaybackStateCompat.STATE_PLAYING);
			showPlayingNotification();
		}
	    else {
			Log.d("mMediaControllerCompat", "!playing_Prepared");
			prepare(mURL);
			playing_Requested = true;
		}
	}

    public void prepare(String url) {
		Log.d("mMediaControllerCompat", "prepare(" + url + ")+++++++++++++++++++++++++++++++++");
		stopFunc();
		mURL = url;
		playing_Prepared = false;
		playing_Requested = false;
		try {
//			mMediaPlayer.stop();
            mMediaPlayer.reset();
			mMediaPlayer.setDataSource(url);
			initMediaSessionMetadata();
		} catch (IOException e) {
		    Log.d("mMediaControllerCompat", "prepare()++++++++++ something went wrong");
			return;
		}

	    mMediaPlayer.prepareAsync();
	}

    public void setnotification(String txt) {
		mNotification = txt;
		if (mMediaPlayer.isPlaying())
		    showPlayingNotification();
		else
		    showPausedNotification();
	}

    public void onPrepared(MediaPlayer player) {
		Log.d("mMediaControllerCompat", "onPrepared(mMediaControllerCompat)+++++++++++++++++++++++++++++++++");
		playing_Prepared = true;

		if (playing_Requested) {
			Log.d("mMediaControllerCompat", "onPrepared(mMediaControllerCompat)++++playing_Requested");
			playFunc();
		}
//		mMediaPlayer.start();
    }

    private MediaSessionCompat.Callback mMediaSessionCallback = new MediaSessionCompat.Callback() {

		@Override
		public void onPlay() {
			super.onPlay();
			playFunc();
		}

	    @Override
		public void onPause() {
			super.onPause();
			pauseFunc();
		}

	    @Override
		public void onPlayFromMediaId(String mediaId, Bundle extras) {
			super.onPlayFromMediaId(mediaId, extras);
			Log.d("mPlayer", "onPlayFromMediaId()+++++++++++++++++++++++++++++++++");
			prepare(mediaId);
		}

	    @Override
		public void onCommand(String command, Bundle extras, ResultReceiver cb) {
			super.onCommand(command, extras, cb);
			if (COMMAND_EXAMPLE.equalsIgnoreCase(command)) {
				//Custom command here
			}
		}

	    @Override
		public void onSeekTo(long pos) {
			super.onSeekTo(pos);
		}

	};

    @Override
	public void onCreate() {
		super.onCreate();
		registerReceiver(Buttonreceiver, new IntentFilter("android.intent.action.MAIN"));
		initMediaPlayer();
		initMediaSession();
		initNoisyReceiver();

		((AudioManager) getSystemService(AUDIO_SERVICE)).registerMediaButtonEventReceiver(new ComponentName(
		        this,
				MusicIntentReceiver.class));

		IntentFilter mediaFilter = new IntentFilter(Intent.ACTION_MEDIA_BUTTON);
		mediaFilter.setPriority(IntentFilter.SYSTEM_HIGH_PRIORITY);
		//registerReceiver(mMediaButtonReceiver, mediaFilter);
	}

    private void initNoisyReceiver() {
		IntentFilter filter = new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
		registerReceiver(mNoisyReceiver, filter);
	}

    @Override
	public void onDestroy() {
		super.onDestroy();
		AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		audioManager.abandonAudioFocus(this);
		unregisterReceiver(mNoisyReceiver);
		unregisterReceiver(Buttonreceiver);

		mMediaSessionCompat.release();
		NotificationManagerCompat.from(this).cancel(1);
	}

    public boolean playing_Requested = false;
	public boolean playing_Prepared = false;

	private void initMediaPlayer() {
		mMediaPlayer = new MediaPlayer();
		mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
			@Override
			public void onPrepared(MediaPlayer mp) {
				playing_Prepared = true;
				Log.d("mPlayer", "onPrepared()+++++++++++++++++++++++++++++++++");

				if (playing_Requested) {
					Log.d("mPlayer", "onPrepared()++++playing_Requested");
					playFunc();
				}
			}
		});
	    mMediaPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
		mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
		mMediaPlayer.setVolume(1.0f, 1.0f);
	}

    private void showPlayingNotification() {
		if (mAlbumBitMap == null)
		    mAlbumBitMap = BitmapFactory.decodeResource(getResources(), R.drawable.icon);

		NotificationCompat.Builder builder = MediaStyleHelper.from(BackgroundAudioService.this, mMediaSessionCompat, mArtist, mTitle, mAlbumBitMap, mNotification);
		if (builder == null) {
			return;
		}
	    builder.addAction(new NotificationCompat.Action(android.R.drawable.ic_media_pause, "Stop", MediaButtonReceiver.buildMediaButtonPendingIntent(this, PlaybackStateCompat.ACTION_PAUSE)));
		builder.setStyle(new NotificationCompat.MediaStyle().setShowActionsInCompactView(0).setMediaSession(mMediaSessionCompat.getSessionToken()));
		builder.setSmallIcon(R.mipmap.ic_launcher);
		builder.setOngoing(true);
		NotificationManagerCompat.from(BackgroundAudioService.this).notify(1, builder.build());
	}

    private void showPausedNotification() {
//		NotificationCompat.Builder builder = MediaStyleHelper.from(BackgroundAudioService.this, mMediaSessionCompat, mArtist, mTitle, mAlbumBitMap, mNotification);
//		if (builder == null) {
//			return;
//		}
//	    builder.addAction(new NotificationCompat.Action(android.R.drawable.ic_media_play, "Play", MediaButtonReceiver.buildMediaButtonPendingIntent(this, PlaybackStateCompat.ACTION_PLAY)));
//		builder.setStyle(new NotificationCompat.MediaStyle().setShowActionsInCompactView(0).setMediaSession(mMediaSessionCompat.getSessionToken()));
//		builder.setSmallIcon(R.mipmap.ic_launcher);
//		NotificationManagerCompat.from(this).notify(1, builder.build());
        NotificationManagerCompat.from(this).cancelAll();
	}

    private void initMediaSession() {
		ComponentName mediaButtonReceiver = new ComponentName(getApplicationContext(), MediaButtonReceiver.class);
		mMediaSessionCompat = new MediaSessionCompat(getApplicationContext(), "Tag", mediaButtonReceiver, null);
		mMediaSessionCompat.setCallback(mMediaSessionCallback);
		mMediaSessionCompat.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS | MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);
		Intent mediaButtonIntent = new Intent(Intent.ACTION_MEDIA_BUTTON);
		mediaButtonIntent.setClass(this, MediaButtonReceiver.class);
		PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, mediaButtonIntent, 0);
		mMediaSessionCompat.setMediaButtonReceiver(pendingIntent);
		setSessionToken(mMediaSessionCompat.getSessionToken());
	}

    private long getAvailableActions() {
		long actions =
		        PlaybackStateCompat.ACTION_PLAY_PAUSE |
				        PlaybackStateCompat.ACTION_PLAY_FROM_MEDIA_ID |
						PlaybackStateCompat.ACTION_PLAY_FROM_SEARCH |
						PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS |
						PlaybackStateCompat.ACTION_SKIP_TO_NEXT;
		return actions;
	}

    private void setMediaPlaybackState(int state) {
		PlaybackStateCompat.Builder playbackstateBuilder = new PlaybackStateCompat.Builder();
		if (state == PlaybackStateCompat.STATE_PLAYING) {
			playbackstateBuilder.setActions(getAvailableActions() | PlaybackStateCompat.ACTION_PAUSE);
		} else {
		    playbackstateBuilder.setActions(getAvailableActions() | PlaybackStateCompat.ACTION_PLAY);
		}
	    playbackstateBuilder.setState(state, PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN, 0);
		mMediaSessionCompat.setPlaybackState(playbackstateBuilder.build());
	}

    private void initMediaSessionMetadata() {
		MediaMetadataCompat.Builder metadataBuilder = new MediaMetadataCompat.Builder();
		metadataBuilder.putBitmap(MediaMetadataCompat.METADATA_KEY_DISPLAY_ICON, BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher));
		metadataBuilder.putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher));
		metadataBuilder.putString(MediaMetadataCompat.METADATA_KEY_TITLE, mTitle);
		metadataBuilder.putString(MediaMetadataCompat.METADATA_KEY_ARTIST, mArtist);
		metadataBuilder.putLong(MediaMetadataCompat.METADATA_KEY_TRACK_NUMBER, 1);
		metadataBuilder.putLong(MediaMetadataCompat.METADATA_KEY_NUM_TRACKS, 1);
//		metadataBuilder.putBitmap(MediaMetadataCompat.METADATA_KEY_ART, mAlbumBitMap);
//		metadataBuilder.putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, mAlbumBitMap);
        mMediaSessionCompat.setMetadata(metadataBuilder.build());
	}

    private boolean successfullyRetrievedAudioFocus() {
		AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

		int result = audioManager.requestAudioFocus(this,
		        AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);

		return result == AudioManager.AUDIOFOCUS_GAIN;
	}


    //Not important for general audio service, required for class
	@Nullable
	@Override
	public BrowserRoot onGetRoot(@NonNull String clientPackageName, int clientUid, @Nullable Bundle rootHints) {
		if (TextUtils.equals(clientPackageName, getPackageName())) {
			return new BrowserRoot("filtermusic", null); //TODO set any name
		}
	    return null;
	}

    @Override
	public void onLoadChildren(@NonNull String parentId, @NonNull Result<List<MediaBrowserCompat.MediaItem>> result) {
		result.sendResult(null);
	}

    @Override
	public void onAudioFocusChange(int focusChange) {
		switch (focusChange) {
			case AudioManager.AUDIOFOCUS_LOSS: {
				Log.d("mPlayer", "AudioManager.AUDIOFOCUS_LOSS");
				if (mMediaPlayer.isPlaying()) {
					pauseFunc();
				}
			    break;
			}
		    case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT: {
				Log.d("mPlayer", "AudioManager.AUDIOFOCUS_LOSS_TRANSIENT");
				pauseFunc();
				break;
			}
		    case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK: {
				if (mMediaPlayer != null) {
					mMediaPlayer.setVolume(0.3f, 0.3f);
				}
			    break;
			}
		    case AudioManager.AUDIOFOCUS_GAIN: {
				if (mMediaPlayer != null) {
					if (!mMediaPlayer.isPlaying()) {
						playFunc();
					}
				    mMediaPlayer.setVolume(1.0f, 1.0f);
				}
			    break;
			}
		}
	}

    @Override
	public void onCompletion(MediaPlayer mediaPlayer) {
		if (mMediaPlayer != null) {
			mMediaPlayer.release();
		}
	}

    @Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		MediaButtonReceiver.handleIntent(mMediaSessionCompat, intent);
		return super.onStartCommand(intent, flags, startId);
	}

    private String fetchForArtwork(String artist, String songName) {
		Track track = Track.getInfo(artist, songName, APIKEY);
		if (track == null) return null;
		String image_url = track.getImageURL(ImageSize.LARGE);
		if (image_url == null) {
			Album album = Album.getInfo(artist, track.getAlbum(), APIKEY);
			if (album != null)
			    return album.getImageURL(ImageSize.LARGE);
		}
	    return image_url;
	}

    private Bitmap bitMapDownloader(String url) {
		Bitmap mIcon_val = null;
		if (url == null)
		    return mIcon_val;
		Boolean test = Patterns.WEB_URL.matcher(url.toLowerCase()).matches();
		if (!test)
		    return mIcon_val;
		try {
			URL new_url = new URL(url);
			mIcon_val = BitmapFactory.decodeStream(new_url.openConnection().getInputStream());
		} catch (IOException e) {
		    e.printStackTrace();
		}
	    if (mIcon_val == null)
		    mIcon_val = BitmapFactory.decodeResource(getResources(), R.drawable.icon);
		return mIcon_val;
	}

    private String current_song;
	private static Timer timer = new Timer();

	public void getMeta(final String link) {
		timer = new Timer();
		timer.scheduleAtFixedRate(new mainTask(link), 0, 5000);
	}

    private class mainTask extends TimerTask {
		public String StationUrl;

		public mainTask(String url) {
			StationUrl = url;
			Log.d("TAG", "contructor ------------" + StationUrl);
		}

	    public void run() {
			try {
				if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {

					Log.d("TAG", "        public void run(" + StationUrl + ") {");
					icy = new IcyStreamMeta(new URL(StationUrl));
					mArtist = icy.getArtist();
					mTitle = icy.getTitle();
					mStreamTitle = icy.getStreamTitle();

//					m_jniExport.titleReporter(mStreamTitle);
//					m_jniExport.sendSetTitle(mStreamTitle);

					Log.d("TAG", mArtist + "  ************ " + mTitle);
					if (current_song != mTitle) {
						current_song = mTitle;
	//					mAlbumurl = fetchForArtwork(mArtist, mTitle);
	//					mAlbumBitMap = bitMapDownloader(mAlbumurl);
	//					if (mArtist == "") mArtist = "Unknown";
	//					if (mTitle == "") mTitle = "Unknown";
	//					Log.d("TAG", mArtist + "  ************ " + mTitle + "********************** " + mAlbumurl);
	                    initMediaSessionMetadata();
						showPlayingNotification();
					}
				}
			} catch (IOException e) {
			    e.printStackTrace();
			} catch (StringIndexOutOfBoundsException e) {
			    e.printStackTrace();
			}
			catch(Exception e) {
				e.printStackTrace();
			}
		}
	}

    public void stopMeta() {
		Log.d("TAG", "stopMeta");
		icy = null;
		if (timer != null)
		    timer.cancel();
		timer = null;
	}
}
