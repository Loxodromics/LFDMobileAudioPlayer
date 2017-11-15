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
	  m_media(),
	  m_title("")
{
//	QString localImageUrl = qApp->applicationDirPath();
//	localImageUrl.append("/res/filtermusic-logo.jpg");
//	qDebug() << "localImageUrl" << localImageUrl;
//	this->m_media = new AudioMedia(this,
//								   false,
//								   false,
//								   true,
//								   false,
//								   true,
//								   false,
//								   "54321!",
//								   "Titel",
//								   "Artitst",
//								   "LFD Audio Player",
//								   localImageUrl,
//								   "http://ice1.somafm.com/groovesalad-128-mp3" );
}

void AudioPlayer::play()
{

}

void AudioPlayer::pause()
{

}

bool AudioPlayer::playing() const
{
	return (this->m_playingState == PlayingState::Playing);
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

AudioMedia* AudioPlayer::media() const
{
	return this->m_media;
}

QString AudioPlayer::title() const
{
	return this->m_title;
}

void AudioPlayer::setMedia(AudioMedia* media)
{
	if ( this->m_media != media )
	{
		this->pause();
		emit mediaChanged( this->media() );
		this->m_media = media;
	}
}

void AudioPlayer::setMediaTitle(const QString title)
{
	if ( this->m_media->title() != title )
	{
		this->m_media->setTitle( title );
		emit mediaChanged( this->media() );
	}
}

void AudioPlayer::setTitle(QString title)
{
	if (m_title == title)
		return;

	m_title = title;
	emit titleChanged(m_title);
}

AudioPlayer::PlayingState AudioPlayer::playingState() const
{
	return this->m_playingState;
}

}  /// namespace LFD
