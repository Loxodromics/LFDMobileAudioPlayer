//
//  lfdaudioplayer.h
//  LFD Audio Player
//
//  Created by philipp on 06.10.2017.
//  Copyright (c) 2017 Philipp Engelhard. All rights reserved.
//
#ifndef LFD_AUDIOPLAYER_H
#define LFD_AUDIOPLAYER_H

#include <QObject>
#include <src/lfdaudiomedia.h>

namespace LFD {

class AudioPlayer : public QObject
{
	Q_OBJECT
public:

	enum class PlayingState : int
	{
		NotConnected,
		FailedPlaying,
		Connecting,
		Reconnecting,
		Paused,
		Playing
	};
	Q_ENUMS(PlayingState)

	explicit AudioPlayer(QObject* parent = nullptr);

	Q_PROPERTY(bool playing READ playing NOTIFY playingChanged)
	Q_PROPERTY(PlayingState playingState READ playingState NOTIFY playingStateChanged)
	Q_PROPERTY(QString mediaPath READ mediaPath WRITE setMediaPath NOTIFY mediaPathChanged)

	Q_INVOKABLE virtual void play() = 0;
	Q_INVOKABLE virtual void pause() = 0;
	Q_INVOKABLE virtual bool playing() const;

	virtual QString mediaPath() const;
	AudioPlayer::PlayingState playingState() const;

	const AudioMedia* media() const;
	virtual void setMedia(const AudioMedia* media);

public slots:
	virtual void setMediaPath( QString mediaPath );

signals:
	void playingChanged( bool playing );
	void mediaPathChanged( QString mediaPath );
	void nextTrack( QString mediaPath );
	void previousTrack( QString mediaPath );
	void like( QString mediaPath );
	void playingStateChanged( PlayingState playingState );
	void mediaChanged( const AudioMedia* media );

protected:
	void setPlayingState( const PlayingState& playingState );

	PlayingState m_playingState;
	QString m_mediaPath;
	const AudioMedia* m_media;
};

} /// namespace LFD

#endif // LFD_AUDIOPLAYER_H
