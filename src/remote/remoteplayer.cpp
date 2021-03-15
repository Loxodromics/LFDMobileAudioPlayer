/**
 *  remoteplayer.cpp
 *  Filtermusic
 *
 *  Created by philipp on 18.02.2021.
 *  Copyright (c) 2021 Philipp Engelhard. All rights reserved.
*/
#include "remoteplayer.h"

namespace filtermusic {

RemotePlayer::RemotePlayer(QObject* parent)
	: AudioPlayer(parent)
	, m_webSocket()
	, m_serverUrl(QStringLiteral("ws://192.168.1.100:54321"))
	, m_stationUrl("")
	, m_reconnectTimer(this)
{
	connect( this, SIGNAL( playPressed() ),
			 this, SLOT( play() ) );

	connect( this, SIGNAL( pausePressed() ),
			 this, SLOT( pause() ) );

	connect(&m_webSocket, &QWebSocket::connected,
			this, &RemotePlayer::onConnected);

	connect(&m_webSocket, &QWebSocket::disconnected,
			this, &RemotePlayer::closed);
}

void RemotePlayer::onConnected()
{
	qDebug() << "WebSocket connected";
	connect(&m_webSocket, &QWebSocket::textMessageReceived,
			this, &RemotePlayer::onTextMessageReceived);
	m_webSocket.sendTextMessage(QStringLiteral("Hello, Server!"));
}

void RemotePlayer::onTextMessageReceived(QString message)
{
	qDebug() << "Message received:" << message;
	if (message.startsWith("playing")) {
		this->setPlayingState(PlayingState::Playing);
	}
	else if ( (message.startsWith("stopped")) || (message.startsWith("paused")) ) {
		this->setPlayingState(PlayingState::NotConnected);
//		AudioPlayer::pause();
//		emit pausePressed();
	}
	else if (message.startsWith("volume")) {
		QString volume = message.mid(7);
		this->setVolume(volume.toInt());
	}
	else if (message.startsWith("lfdaudiomedia:")) {
		QString jsonBase64 = message.mid(14);
		QByteArray byteArray = QByteArray::fromBase64(jsonBase64.toUtf8());
		this->m_localMedia.fromJsonString(byteArray);
		emit this->mediaChanged(&this->m_localMedia);
	}
}

void RemotePlayer::checkConnection()
{
	qDebug() << "RemotePlayer::checkConnection()" << this->m_webSocket.state();
	if (this->m_webSocket.state() != QAbstractSocket::ConnectedState) {
		this->connectToServer();
	}
}

void RemotePlayer::play()
{
	qDebug() << "RemotePlayer::play()";
//	this->m_webSocket.sendTextMessage("play:http://stream-uk1.radioparadise.com/mp3-192");
	this->m_webSocket.sendTextMessage("play:" + this->m_media->url());
	qDebug() << "lfdaudiomedia:" << this->m_media->toJsonString();
	qDebug() << "lfdaudiomedia:" << this->m_media->toJsonString().toUtf8().toBase64();
	this->m_webSocket.sendTextMessage("lfdaudiomedia:" + this->m_media->toJsonString().toUtf8().toBase64());
}

void RemotePlayer::pause()
{
	qDebug() << "RemotePlayer::pause()";
	this->m_webSocket.sendTextMessage("stop");
}

void RemotePlayer::setVolume(int volume)
{
	if (this->m_volume != volume) {
		qDebug() << "RemotePlayer::setVolume()" << QString::number(volume);
		this->m_webSocket.sendTextMessage("volume:" + QString::number(volume));
	}
	AudioPlayer::setVolume(volume);
}

void RemotePlayer::connectToServer()
{
	m_webSocket.open(QUrl(this->m_serverUrl));
}

void RemotePlayer::setServerUrl(const QString serverUrl)
{
	this->m_serverUrl = serverUrl;
}

void RemotePlayer::setStationUrl(const QString stationUrl)
{
	this->m_stationUrl = stationUrl;
}

void RemotePlayer::startup()
{
	this->m_serverUrl = QStringLiteral("ws://192.168.1.100:54321");
	this->connectToServer();

	this->m_reconnectTimer.connect(&this->m_reconnectTimer, &QTimer::timeout,
								   this, &RemotePlayer::checkConnection);
	this->m_reconnectTimer.start(30000);
}

void RemotePlayer::closed()
{
	qDebug() << "RemotePlayer::closed()";
//	this->connectToServer();
}

} // namespace filtermusic
