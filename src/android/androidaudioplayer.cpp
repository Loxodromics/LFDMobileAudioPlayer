//
//  androidaudioplayer.cpp
//  %PROJECT_NAME%
//
//  Created by philipp2 on 04.11.2017.
//  Copyright (c) 2017 Philipp Engelhard. All rights reserved.
//
#include "androidaudioplayer.h"

namespace LFD {

AndroidAudioPlayer::AndroidAudioPlayer(QObject* parent)
	: AudioPlayer(parent),
	  radioStatiosContainer(new ahmed::RadioStatiosContainer())
{

}

void AndroidAudioPlayer::play()
{
	//if ( !this->radioStatiosContainer->isplaying() )
	{
		this->radioStatiosContainer->togglePlayer();
	}
}

void AndroidAudioPlayer::pause()
{
//	if ( this->radioStatiosContainer->isplaying() )
	{
		this->radioStatiosContainer->togglePlayer();
	}
}

void AndroidAudioPlayer::setMediaPath(QString mediaPath)
{
	AudioPlayer::setMediaPath(mediaPath);
	this->radioStatiosContainer->setStation(mediaPath);
}

} // namespace LFD
