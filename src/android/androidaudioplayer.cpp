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
	/// for testing
	this->setMedia(this->media());
}

void AndroidAudioPlayer::play()
{
	QtAndroid::runOnAndroidThread([] {
		QtAndroid::androidActivity().callMethod<void>("playstation");
	});
}

void AndroidAudioPlayer::pause()
{
	QtAndroid::runOnAndroidThread([] {
		QtAndroid::androidActivity().callMethod<void>("pausestation");
	});
}

void AndroidAudioPlayer::setMedia(AudioMedia* media)
{
	AudioPlayer::setMedia(media);

	qDebug() << "updateStation";
	const QString stationUrl = this->media()->url();
	QtAndroid::runOnAndroidThread([stationUrl] {
		QtAndroid::androidActivity().callMethod<void>("setestation","(Ljava/lang/String;)V",
													  QAndroidJniObject::fromString(stationUrl).object<jstring>());
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
	default:
		qDebug() << "unknow FocusState:" << focus;
		break;
	}

}

} /// namespace LFD
