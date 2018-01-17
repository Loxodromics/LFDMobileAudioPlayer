//
//  androidaudioplayer.cpp
//  LFD Audio Player
//
//  Created by philipp on 04.11.2017.
//  Copyright (c) 2017 Philipp Engelhard. All rights reserved.
//
#include "androidaudioplayer.h"
#include <QDebug>

namespace LFD
{

AndroidAudioPlayer::AndroidAudioPlayer(QObject* parent)
	: AudioPlayer(parent)
{
	connect( this, SIGNAL( playPressed() ),
			 this, SLOT( play() ) );

	connect( this, SIGNAL( pausePressed() ),
			 this, SLOT( pause() ) );
}

void AndroidAudioPlayer::play()
{
	qDebug() << "AndroidAudioPlayer::play()";
	if ( this->media() != nullptr )
	{
		this->setPlayingState(PlayingState::Connecting);
		QtAndroid::runOnAndroidThread([] {
			QtAndroid::androidActivity().callMethod<void>("playstation");
		});
	}
}

void AndroidAudioPlayer::pause()
{
	qDebug() << "AndroidAudioPlayer::pause()";
//	this->setPlayingState(PlayingState::NotConnected);
	QtAndroid::runOnAndroidThread([] {
		QtAndroid::androidActivity().callMethod<void>("pausestation");
	});
}

void AndroidAudioPlayer::setMedia(AudioMedia* media)
{
	AudioPlayer::setMedia(media);

	qDebug() << "updateStation";
	const QString stationUrl = this->media()->url();
	const QString stationName = this->media()->artist();
	QtAndroid::runOnAndroidThread([stationUrl, stationName] {
		QtAndroid::androidActivity().callMethod<void>("setStation","(Ljava/lang/String;Ljava/lang/String;)V",
													  QAndroidJniObject::fromString(stationUrl).object<jstring>(),
													  QAndroidJniObject::fromString(stationName).object<jstring>());
	});
}

void AndroidAudioPlayer::setFocus(int focus)
{
	qDebug() << "AndroidAudioPlayer::setFocus" << focus;
	switch (static_cast<FocusState>(focus)) {
	case FocusState::Paused:
		this->setPlayingState( PlayingState::Paused );
		break;
	case FocusState::Playing:
		this->setPlayingState( PlayingState::Playing );
		break;
	case FocusState::Stopped:
		this->setPlayingState( PlayingState::NotConnected );
		break;
	default:
		qDebug() << "unknow FocusState:" << focus;
		break;
	}

}

} /// namespace LFD
