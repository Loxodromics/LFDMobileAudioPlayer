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
#include "lfdaudiomedia.h"

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
	Q_PROPERTY(QString title READ title NOTIFY titleChanged)

	Q_INVOKABLE virtual void play();
	Q_INVOKABLE virtual void pause();
	Q_INVOKABLE virtual bool playing() const;

	AudioPlayer::PlayingState playingState() const;

	AudioMedia* media() const;
	QString title() const;

public slots:
	virtual void setMedia(LFD::AudioMedia* media);
	void setMediaTitle(const QString title);
	void setTitle(QString title);

signals:
	void playingChanged( bool playing );
	void mediaPathChanged( QString mediaPath );
	void nextTrack( QString mediaPath );
	void previousTrack( QString mediaPath );
	void like( QString mediaPath );
	void playingStateChanged( PlayingState playingState );
	void mediaChanged( AudioMedia* media );
	void titleChanged( QString title );
	void playPressed();
	void pausePressed();

protected:
	void setPlayingState( const PlayingState& playingState );

	PlayingState m_playingState;
	AudioMedia* m_media;
	QString m_title;
};

} /// namespace LFD

#endif // LFD_AUDIOPLAYER_H
