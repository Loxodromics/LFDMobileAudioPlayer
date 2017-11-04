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

void AndroidAudioPlayer::setMedia(const AudioMedia* media)
{
	AudioPlayer::setMedia(media);

	qDebug() << "updateStation";
	const QString stationUrl = this->media()->url();
	QtAndroid::runOnAndroidThread([stationUrl] {
		QtAndroid::androidActivity().callMethod<void>("setestation","(Ljava/lang/String;)V",
													  QAndroidJniObject::fromString(stationUrl).object<jstring>());
	});
}

//void AndroidAudioPlayer::statusChanged(QMediaPlayer::MediaStatus status)
//{
//	/// TODO
//	qDebug() << "statusChanged" << status;
//}


} /// namespace LFD
