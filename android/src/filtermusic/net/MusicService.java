/*
 * Copyright 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package filtermusic.net;

import android.app.Notification;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaBrowserServiceCompat;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;

import filtermusic.net.notifications.MediaNotificationManager;
import filtermusic.net.players.MediaPlayerAdapter;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MusicService extends MediaBrowserServiceCompat {

    private static final String TAG = MusicService.class.getSimpleName();
    private static final String EMPTY_MEDIA_ROOT_ID = "empty_root_id";

    private MediaSessionCompat mSession;
    private PlayerAdapter mPlayback;
    private MediaNotificationManager mMediaNotificationManager;
    private MediaSessionCallback mCallback;
    private boolean mServiceInStartedState;
    private String mCurrentStreamTitle;
    private static Timer timer = new Timer();
    private IcyStreamMeta mIcyStreamMeta;
    private PlaybackStateCompat mLastPlaybackState;

    @Override
    public void onCreate() {
        super.onCreate();

        // Create a new MediaSession.
        mSession = new MediaSessionCompat(this, "MusicService");
        mCallback = new MediaSessionCallback();
        mSession.setCallback(mCallback);
        mSession.setFlags(
                MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS |
                        MediaSessionCompat.FLAG_HANDLES_QUEUE_COMMANDS |
                        MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);
        setSessionToken(mSession.getSessionToken());

        mMediaNotificationManager = new MediaNotificationManager(this);

        mPlayback = new MediaPlayerAdapter(this, new MediaPlayerListener());
        Log.d(TAG, "onCreate: MusicService creating MediaSession, and MediaNotificationManager");
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        stopSelf();
    }

    @Override
    public void onDestroy() {
        mMediaNotificationManager.onDestroy();
        mPlayback.stop();
        mSession.release();
        Log.d(TAG, "onDestroy: MediaPlayerAdapter stopped, and MediaSession released");
    }

    // TODO for library browsing, car play, etc
    @Override
    public BrowserRoot onGetRoot(@NonNull String clientPackageName,
                                 int clientUid,
                                 Bundle rootHints) {
        return new BrowserRoot(EMPTY_MEDIA_ROOT_ID, null);
    }

    @Override
    public void onLoadChildren(
            @NonNull final String parentMediaId,
            @NonNull final Result<List<MediaBrowserCompat.MediaItem>> result) {
        result.sendResult(getMediaItems());
        return;
    }

    public List<MediaBrowserCompat.MediaItem> getMediaItems() {
        List<MediaBrowserCompat.MediaItem> result = new ArrayList<>();
        result.add(
                new MediaBrowserCompat.MediaItem(
                        getMetadata("").getDescription(), MediaBrowserCompat.MediaItem.FLAG_PLAYABLE));
        return result;
    }

    private MediaMetadataCompat getMetadata(String mediaId) {
//            MediaMetadataCompat metadataWithoutBitmap = music.get(mediaId);
        Bitmap albumArt = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);

        MediaMetadataCompat.Builder builder = new MediaMetadataCompat.Builder();
        builder.putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, "id");
        builder.putString(MediaMetadataCompat.METADATA_KEY_ALBUM, "");
        builder.putString(MediaMetadataCompat.METADATA_KEY_ARTIST, "");
        builder.putString(MediaMetadataCompat.METADATA_KEY_GENRE, "");
        builder.putString(MediaMetadataCompat.METADATA_KEY_TITLE, "");
        builder.putString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI, "");
//            builder.putLong(
//                    MediaMetadataCompat.METADATA_KEY_DURATION,
//                    metadataWithoutBitmap.getLong(MediaMetadataCompat.METADATA_KEY_DURATION));
        builder.putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, albumArt);
        return builder.build();
    }

    private MediaMetadataCompat getMetadata(String mediaId, String streamUrl, String album, String artist, String genre, String title) {
        Bitmap albumArt = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);

        MediaMetadataCompat.Builder builder = new MediaMetadataCompat.Builder();
        builder.putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, mediaId);
        builder.putString(MediaMetadataCompat.METADATA_KEY_ALBUM, album);
        builder.putString(MediaMetadataCompat.METADATA_KEY_ARTIST, artist);
        builder.putString(MediaMetadataCompat.METADATA_KEY_GENRE, genre);
        builder.putString(MediaMetadataCompat.METADATA_KEY_TITLE, title);
        builder.putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, albumArt);
        builder.putString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI, streamUrl);
        return builder.build();
    }

    public void getMeta(final String link) {
        timer = new Timer();
        timer.scheduleAtFixedRate(new QueryMetaDataTask(link), 0, 5000);
    }

    private class QueryMetaDataTask extends TimerTask
    {
        public String mStationUrl;
        private String mArtist = "";
        private String mTitle = "";

        public QueryMetaDataTask(String url) {
            mStationUrl = url;
//            Log.d("QueryMetaDataTask","constructor ------------" + mStationUrl);
        }

        public void run() {
            try {
//                Log.d("QueryMetaDataTask","public void run() {");
                mIcyStreamMeta = new IcyStreamMeta(new URL(mStationUrl));
//                Log.d("TAG", "current: " + mCurrentStreamTitle + " new: " + mIcyStreamMeta.getStreamTitle());

                if (!mIcyStreamMeta.getStreamTitle().equals(mCurrentStreamTitle)) {
                    mCurrentStreamTitle = mIcyStreamMeta.getStreamTitle();

                    if (!mCurrentStreamTitle.isEmpty()) {

                        mArtist = mIcyStreamMeta.getArtist();
                        mTitle = mIcyStreamMeta.getTitle();

                        //sendMsg("title", mCurrentStreamTitle);

//                    Log.d("QueryMetaDataTask", mArtist + "  ************ " + mTitle);

                        MediaMetadataCompat newMedia = getMetadata(mCallback.mPreparedMedia.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID),
                                mCallback.mPreparedMedia.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI),
                                mCallback.mPreparedMedia.getString(MediaMetadataCompat.METADATA_KEY_ALBUM),
                                mArtist, //mCallback.mPreparedMedia.getString(MediaMetadataCompat.METADATA_KEY_ARTIST),
                                mCallback.mPreparedMedia.getString(MediaMetadataCompat.METADATA_KEY_GENRE),
                                mTitle); //mCallback.mPreparedMedia.getString(MediaMetadataCompat.METADATA_KEY_TITLE));
                        mCallback.mPreparedMedia = newMedia;
                        mSession.setMetadata(newMedia);

                        Notification notification =
                                mMediaNotificationManager.getNotification(
                                        newMedia, mLastPlaybackState, getSessionToken());
                        mMediaNotificationManager.getNotificationManager()
                                .notify(MediaNotificationManager.NOTIFICATION_ID, notification);
//                    Log.d(TAG, "onMetadataChanged");
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (StringIndexOutOfBoundsException e) {
                e.printStackTrace();
            }
        }
    }

    public void stopMeta(){
        timer.cancel();
    }

    // MediaSession Callback: Transport Controls -> MediaPlayerAdapter
    public class MediaSessionCallback extends MediaSessionCompat.Callback {
        private final List<MediaSessionCompat.QueueItem> mPlaylist = new ArrayList<>();
        private int mQueueIndex = -1;
        private MediaMetadataCompat mPreparedMedia;

        @Override
        public void onAddQueueItem(MediaDescriptionCompat description) {
            mPlaylist.add(new MediaSessionCompat.QueueItem(description, description.hashCode()));
            mQueueIndex = (mQueueIndex == -1) ? 0 : mQueueIndex;
        }

        @Override
        public void onRemoveQueueItem(MediaDescriptionCompat description) {
            mPlaylist.remove(new MediaSessionCompat.QueueItem(description, description.hashCode()));
            mQueueIndex = (mPlaylist.isEmpty()) ? -1 : mQueueIndex;
        }

        @Override
        public void onPrepare() {
            if (isReadyToPlay()) {
                // Nothing to play.
                return;
            }

//            final String mediaId = mPlaylist.get(mQueueIndex).getDescription().getMediaId();
//            mPreparedMedia = getMetadata("mediaId");

            mSession.setMetadata(mPreparedMedia);

            if (!mSession.isActive()) {
                mSession.setActive(true);
            }
        }

        @Override
        public void onPlay() {
            if (!isReadyToPlay()) {
                // Nothing to play.
                return;
            }

            if (mPreparedMedia == null) {
                onPrepare(); //FIXME: that won't change anything, we have no URL
            }

            mPlayback.playFromMedia(mPreparedMedia);
            getMeta(mPreparedMedia.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI));
            Log.d(TAG, "onPlayFromMediaId: MediaSession active");
        }

        @Override
        public void onPause() {
            mPlayback.pause();
            stopMeta();
        }

        @Override
        public void onStop() {
            mPlayback.stop();
            mSession.setActive(false);
            stopMeta();
        }

        @Override
        public void onSkipToNext() {
//            mQueueIndex = (++mQueueIndex % mPlaylist.size());
            mPreparedMedia = null;
            onPlay();
        }

        @Override
        public void onSkipToPrevious() {
//            mQueueIndex = mQueueIndex > 0 ? mQueueIndex - 1 : mPlaylist.size() - 1;
            mPreparedMedia = null;
            onPlay();
        }

        @Override
        public void onPlayFromUri(Uri uri, Bundle extras) {
            super.onPlayFromUri(uri, extras);
        }

        @Override
        public void onPrepareFromUri(Uri uri, Bundle extras) {
            super.onPrepareFromUri(uri, extras);

            mPreparedMedia = getMetadata("mediaId",
                    uri.toString(),
                    "album",
                    extras.getString("name"),
                    "genre",
                    "filtermusic");
            mSession.setMetadata(mPreparedMedia);

            Log.d(TAG, "onPrepareFromUri");

            if (!mSession.isActive()) {
                mSession.setActive(true);
            }
        }

        @Override
        public void onSeekTo(long pos) {
            mPlayback.seekTo(pos);
        }

        private boolean isReadyToPlay() {
            return (mPreparedMedia != null && !mPreparedMedia.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI).isEmpty());
        }
    }

    // MediaPlayerAdapter Callback: MediaPlayerAdapter state -> MusicService.
    public class MediaPlayerListener extends PlaybackInfoListener {

        private final ServiceManager mServiceManager;

        MediaPlayerListener() {
            mServiceManager = new ServiceManager();
        }

        @Override
        public void onPlaybackStateChange(PlaybackStateCompat state) {
            // Report the state to the MediaSession.
            mSession.setPlaybackState(state);
            mLastPlaybackState = state;

            // Manage the started state of this service.
            switch (state.getState()) {
                case PlaybackStateCompat.STATE_PLAYING:
                    mServiceManager.moveServiceToStartedState(state);
                    break;
                case PlaybackStateCompat.STATE_PAUSED:
                    mServiceManager.updateNotificationForPause(state);
                    break;
                case PlaybackStateCompat.STATE_STOPPED:
                    mServiceManager.moveServiceOutOfStartedState(state);
                    break;
            }
        }

        @Override
        public void onMetadataChanged(MediaMetadataCompat metadata) {
            Notification notification =
                    mMediaNotificationManager.getNotification(
                            mPlayback.getCurrentMedia(), mLastPlaybackState, getSessionToken());
            mMediaNotificationManager.getNotificationManager()
                    .notify(MediaNotificationManager.NOTIFICATION_ID, notification);
            Log.d(TAG, "onMetadataChanged");
        }

        class ServiceManager {

            private void moveServiceToStartedState(PlaybackStateCompat state) {
                Notification notification =
                        mMediaNotificationManager.getNotification(
                                mPlayback.getCurrentMedia(), state, getSessionToken());

                if (!mServiceInStartedState) {
                    ContextCompat.startForegroundService(
                            MusicService.this,
                            new Intent(MusicService.this, MusicService.class));
                    mServiceInStartedState = true;
                }

                startForeground(MediaNotificationManager.NOTIFICATION_ID, notification);
            }

            private void updateNotificationForPause(PlaybackStateCompat state) {
                stopForeground(false);
                Notification notification =
                        mMediaNotificationManager.getNotification(
                                mPlayback.getCurrentMedia(), state, getSessionToken());
                mMediaNotificationManager.getNotificationManager()
                        .notify(MediaNotificationManager.NOTIFICATION_ID, notification);
            }

            private void moveServiceOutOfStartedState(PlaybackStateCompat state) {
                stopForeground(true);
                stopSelf();
                mServiceInStartedState = false;
            }
        }

    }

}
