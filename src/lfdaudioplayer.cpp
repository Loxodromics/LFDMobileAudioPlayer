//
//  lfdaudioplayer.cpp
//  LFD Audio Player
//
//  Created by philipp on 06.10.2017.
//  Copyright (c) 2017 Philipp Engelhard. All rights reserved.
//
#include "lfdaudioplayer.h"
#include <QGuiApplication>
#include <QDebug>

namespace LFD {

AudioPlayer::AudioPlayer(QObject* parent)
	: QObject(parent),
	  m_playingState(PlayingState::NotConnected),
	  m_mediaPath("")
{
	QString localImageUrl = qApp->applicationDirPath();
	localImageUrl.append("/res/filtermusic-logo.jpg");
	qDebug() << "localImageUrl" << localImageUrl;
	this->m_media = new AudioMedia(this,
								   false,
								   false,
								   true,
								   false,
								   true,
								   false,
								   "54321!",
								   "Titel",
								   "Artitst",
								   "LFD Audio Player",
								   localImageUrl,
								   "http://ice1.somafm.com/groovesalad-128-mp3" );
}

bool AudioPlayer::playing() const
{
	return (this->m_playingState == PlayingState::Playing);
}

QString AudioPlayer::mediaPath() const
{
	return this->m_mediaPath;
}

void AudioPlayer::setMediaPath(QString mediaPath)
{
	if ( this->m_mediaPath != mediaPath )
	{
		this->m_mediaPath = mediaPath;
		emit mediaPathChanged( this->m_mediaPath );
	}
}

void AudioPlayer::setPlayingState(const PlayingState& playingState)
{
	if ( this->m_playingState != playingState )
	{
		this->m_playingState = playingState;
		emit playingStateChanged( this->m_playingState );
		emit playingChanged( this->playing() );
	}
}

const AudioMedia* AudioPlayer::media() const
{
	return this->m_media;
}

void AudioPlayer::setMedia(const AudioMedia* media)
{
	if ( this->m_media != media )
	{
		this->pause();
		emit mediaChanged( this->media() );
		this->m_media = media;
	}
}

AudioPlayer::PlayingState AudioPlayer::playingState() const
{
	return this->m_playingState;
}

}  /// namespace LFD
