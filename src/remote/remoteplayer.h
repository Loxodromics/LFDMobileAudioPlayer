/**
 *  remoteplayer.h
 *  Filtermusic
 *
 *  Created by philipp on 18.02.2021.
 *  Copyright (c) 2021 Philipp Engelhard. All rights reserved.
*/
#pragma once


#include <QObject>
#include <QWebSocket>
#include "../lfdaudioplayer.h"

namespace filtermusic {

class RemotePlayer : public LFD::AudioPlayer
{

	Q_OBJECT
public:
	explicit RemotePlayer( QObject* parent = nullptr );

public slots:
	/// AudioPlayer override
	Q_INVOKABLE virtual void play() override;
	Q_INVOKABLE virtual void pause() override;

	/// Socket
	Q_INVOKABLE void connectToServer(const QString serverUrl);
	Q_INVOKABLE void setStationUrl(const QString stationUrl);

signals:
	void closed();

private slots:
//	void metaDataChanged();
//	void statusChanged(QMediaPlayer::MediaStatus status);
//	void stateChanged(QMediaPlayer::State state);
//	void bufferingProgress(int progress);
//	void displayErrorMessage();

protected:
	QWebSocket m_webSocket;
	QString m_serverUrl;
	QString m_stationUrl;

protected slots:
	void onConnected();
	void onTextMessageReceived(QString message);
};

} // namespace filtermusic
