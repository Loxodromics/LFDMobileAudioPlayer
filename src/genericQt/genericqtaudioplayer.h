#ifndef GENERICQTAUDIOPLAYER_H
#define GENERICQTAUDIOPLAYER_H

#include <QObject>
#include <QMediaPlayer>
#include "../lfdaudioplayer.h"

namespace LFD {

class GenericQtAudioPlayer : public LFD::AudioPlayer
{

	Q_OBJECT
public:
	explicit GenericQtAudioPlayer( QObject* parent = nullptr );

public slots:
	/// AudioPlayer override
	Q_INVOKABLE virtual void play() override;
	Q_INVOKABLE virtual void pause() override;
	Q_INVOKABLE virtual void setVolume(int volume) override;

private slots:
	void metaDataChanged();
	void statusChanged(QMediaPlayer::MediaStatus status);
	void stateChanged(QMediaPlayer::State state);
	void bufferingProgress(int progress);
	void displayErrorMessage();

protected:
	QMediaPlayer m_player;

};

} // namespace LFD

#endif // GENERICQTAUDIOPLAYER_H
