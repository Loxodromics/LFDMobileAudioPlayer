//
//  lfdaudiomedia.h
//  LFD Audio Player
//
//  Created by philipp on 06.10.2017.
//  Copyright (c) 2017 Philipp Engelhard. All rights reserved.
//
#ifndef LFD_AUDIOMEDIA_H
#define LFD_AUDIOMEDIA_H

#include <QObject>

namespace LFD {

class AudioMedia : public QObject
{
	Q_OBJECT
public:
	explicit AudioMedia( QObject* parent = nullptr) ;
	explicit AudioMedia( QObject* parent,
						 bool hasNext,
						 bool hasPrevious,
						 bool isLikable,
						 bool isLiked,
						 bool isStream,
						 bool canSeek,
						 QString id,
						 QString title,
						 QString artist,
						 QString album,
						 QString localImageUrl,
						 QString url );
	AudioMedia( const AudioMedia& audioMedia );

	bool hasNext() const;
	void setHasNext(bool hasNext);

	bool hasPrevious() const;
	void setHasPrevious(bool hasPrevious);

	bool isLikable() const;
	void setIsLikable(bool isLikable);

	bool isLiked() const;
	void setIsLiked(bool isLiked);

	bool isStream() const;
	void setIsStream(bool isStream);

	bool canSeek() const;
	void setCanSeek(bool canSeek);

	QString id() const;
	void setId(const QString& id);

	QString title() const;
	void setTitle(const QString& title);

	QString artist() const;
	void setArtist(const QString& artist);

	QString album() const;
	void setAlbum(const QString& album);

	QString localImageUrl() const;
	void setLocalImageUrl(const QString& localImageUrl);

	QString url() const;
	void setUrl(const QString& url);

signals:

public slots:

protected:
	bool m_hasNext;
	bool m_hasPrevious;
	bool m_isLikable;
	bool m_isLiked;
	bool m_isStream;
	bool m_canSeek;
	QString m_id;
	QString m_title;
	QString m_artist;
	QString m_album;
	QString m_localImageUrl;
	QString m_url;
};

} /// namespace LFD

#endif /// LFD_AUDIOMEDIA_H
