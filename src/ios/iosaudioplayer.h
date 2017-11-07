//
//  iosaudioplayer.h
//  iOS Audio Player
//
//  Created by philipp on 27.09.2017.
//  Copyright (c) 2017 Philipp Engelhard. All rights reserved.
//
#ifndef LFD_IOSAUDIOPLAYER_H
#define LFD_IOSAUDIOPLAYER_H

#include <QObject>
#include "../lfdaudioplayer.h"

namespace LFD {

class IosAudioPlayer : public AudioPlayer
{
	Q_OBJECT
public:
	explicit IosAudioPlayer( QObject* parent = nullptr );

//	Q_PROPERTY(bool playing READ playing NOTIFY playingChanged)
//	Q_PROPERTY(QString mediaPath READ mediaPath WRITE setMediaPath NOTIFY mediaPathChanged)

	/// AudioPlayer override
	Q_INVOKABLE virtual void play() override;
	Q_INVOKABLE virtual void pause() override;

	void startedPlaying();
	void stoppedPlaying();
	void failedPlaying( QString errorMessage );

	void nextTrackPressed();
	void previousTrackPressed();
	void togglePlayPause();
	void likePressed();
	void dislikePressed();
	void seekForwardPressed();
	void seekBackwardPressed();

signals:
	void playingChanged( bool playing );
	void mediaPathChanged( QString mediaPath );
	void nextTrack( QString mediaPath );
	void previousTrack( QString mediaPath );
	void like( QString mediaPath );
	void dislike( QString mediaPath );
	void seekForward( QString mediaPath );
	void seekBackward( QString mediaPath );

public slots:
	virtual void setMedia(LFD::AudioMedia* media) override;

protected:
	void* m_audioPlayerDelegate;

};
} /// namespace LFD

#endif /// LFD_IOSAUDIOPLAYER_H
