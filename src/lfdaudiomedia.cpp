//
//  lfdaudiomedia.cpp
//  LFD Audio Player
//
//  Created by philipp on 06.10.2017.
//  Copyright (c) 2017 Philipp Engelhard. All rights reserved.
//
#include "lfdaudiomedia.h"

namespace LFD {

AudioMedia::AudioMedia(QObject* parent)
	: QObject(parent)
{

}

AudioMedia::AudioMedia(QObject* parent, bool hasNext, bool hasPrevious, bool isLikable, bool isLiked, bool isStream, bool canSeek, QString id, QString title, QString artist, QString album, QString localImageUrl, QString url)
	: QObject( parent ),
	  m_hasNext( hasNext ),
	  m_hasPrevious( hasPrevious ),
	  m_isLikable( isLikable ),
	  m_isLiked( isLiked ),
	  m_isStream( isStream ),
	  m_canSeek( canSeek ),
	  m_id( id ),
	  m_title( title ),
	  m_artist( artist ),
	  m_album( album ),
	  m_localImageUrl( localImageUrl ),
	  m_url( url )
{

}

AudioMedia::AudioMedia(const AudioMedia& audioMedia)
{
	m_hasNext = audioMedia.hasNext();
	m_hasPrevious = audioMedia.hasPrevious();
	m_isLikable = audioMedia.isLikable();
	m_isLiked = audioMedia.isLiked();
	m_isStream = audioMedia.isStream();
	m_canSeek = audioMedia.canSeek();
	m_id = audioMedia.id();
	m_title = audioMedia.title();
	m_artist = audioMedia.artist();
	m_album = audioMedia.album();
	m_localImageUrl = audioMedia.localImageUrl();
	m_url = audioMedia.url();
}

bool AudioMedia::hasNext() const
{
	return m_hasNext;
}

void AudioMedia::setHasNext(bool hasNext)
{
	m_hasNext = hasNext;
}

bool AudioMedia::hasPrevious() const
{
	return m_hasPrevious;
}

void AudioMedia::setHasPrevious(bool hasPrevious)
{
	m_hasPrevious = hasPrevious;
}

bool AudioMedia::isLikable() const
{
	return m_isLikable;
}

void AudioMedia::setIsLikable(bool isLikable)
{
	m_isLikable = isLikable;
}

bool AudioMedia::isLiked() const
{
	return m_isLiked;
}

void AudioMedia::setIsLiked(bool isLiked)
{
	m_isLiked = isLiked;
}

bool AudioMedia::isStream() const
{
	return m_isStream;
}

void AudioMedia::setIsStream(bool isStream)
{
	m_isStream = isStream;
}

bool AudioMedia::canSeek() const
{
	return m_canSeek;
}

void AudioMedia::setCanSeek(bool canSeek)
{
	m_canSeek = canSeek;
}

QString AudioMedia::id() const
{
	return m_id;
}

void AudioMedia::setId(const QString& id)
{
	m_id = id;
}

QString AudioMedia::title() const
{
	return m_title;
}

void AudioMedia::setTitle(const QString& title)
{
	m_title = title;
}

QString AudioMedia::artist() const
{
	return m_artist;
}

void AudioMedia::setArtist(const QString& artist)
{
	m_artist = artist;
}

QString AudioMedia::album() const
{
	return m_album;
}

void AudioMedia::setAlbum(const QString& album)
{
	m_album = album;
}

QString AudioMedia::localImageUrl() const
{
	return m_localImageUrl;
}

void AudioMedia::setLocalImageUrl(const QString& imageUrl)
{
	m_localImageUrl = imageUrl;
}

QString AudioMedia::url() const
{
	return m_url;
}

void AudioMedia::setUrl(const QString& url)
{
	m_url = url;
}

} /// namespace LFD
