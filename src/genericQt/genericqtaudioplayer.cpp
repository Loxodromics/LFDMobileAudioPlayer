#include "genericqtaudioplayer.h"
#include <QDebug>

namespace LFD {

GenericQtAudioPlayer::GenericQtAudioPlayer(QObject* parent)
    : AudioPlayer(parent)
{
    connect( this, SIGNAL( playPressed() ),
             this, SLOT( play() ) );

    connect( this, SIGNAL( pausePressed() ),
             this, SLOT( pause() ) );

    connect( &this->m_player, SIGNAL(metaDataChanged()),
             this, SLOT(metaDataChanged()));

    connect( &this->m_player, SIGNAL(mediaStatusChanged(QMediaPlayer::MediaStatus)),
             this, SLOT(statusChanged(QMediaPlayer::MediaStatus)));

    connect( &this->m_player, SIGNAL(bufferStatusChanged(int)),
             this, SLOT(bufferingProgress(int)));

    connect( &this->m_player, SIGNAL(error(QMediaPlayer::Error)),
             this, SLOT(displayErrorMessage()));

//    connect( &this->m_player, SIGNAL(stateChanged(QMediaPlayer::State)),
//             this, SLOT(stateChanged(QMediaPlayer::State)));
}

void GenericQtAudioPlayer::play()
{
    qDebug() << "GenericQtAudioPlayer::play()";
    if ( this->media() != nullptr )
    {
        this->m_player.setMedia(QUrl(this->media()->url()));
        this->setPlayingState(PlayingState::Connecting);
        this->m_player.play();
    }
}

void GenericQtAudioPlayer::pause()
{
    this->setPlayingState(PlayingState::NotConnected);
    this->m_player.pause();
}

void GenericQtAudioPlayer::metaDataChanged()
{
    if (this->m_player.isMetaDataAvailable()) {
//        setTrackInfo(QString("%1 - %2")
//                .arg(player->metaData(QMediaMetaData::AlbumArtist).toString())
//                .arg(player->metaData(QMediaMetaData::Title).toString()));

//        if (coverLabel) {
//            QUrl url = player->metaData(QMediaMetaData::CoverArtUrlLarge).value<QUrl>();

//            coverLabel->setPixmap(!url.isEmpty()
//                    ? QPixmap(url.toString())
//                    : QPixmap());
//        }
    }
}

void GenericQtAudioPlayer::statusChanged(QMediaPlayer::MediaStatus status)
{
    // handle status message
    switch (status) {
    case QMediaPlayer::UnknownMediaStatus:
    case QMediaPlayer::NoMedia:
    case QMediaPlayer::LoadedMedia:
    case QMediaPlayer::BufferingMedia:
    case QMediaPlayer::BufferedMedia:
        this->setPlayingState(PlayingState::Playing);
        break;
    case QMediaPlayer::LoadingMedia:
        this->setPlayingState(PlayingState::Connecting);
        break;
    case QMediaPlayer::StalledMedia:
        this->setPlayingState(PlayingState::Connecting);
        break;
    case QMediaPlayer::EndOfMedia:
        this->setPlayingState( PlayingState::NotConnected );
        break;
    case QMediaPlayer::InvalidMedia:
        displayErrorMessage();
        break;
    }
}

void GenericQtAudioPlayer::stateChanged(QMediaPlayer::State state)
{
    if (state == QMediaPlayer::StoppedState) {
        this->setPlayingState( PlayingState::NotConnected );
    }
}

void GenericQtAudioPlayer::bufferingProgress(int progress)
{
    qDebug() << tr("Buffering %4%").arg(progress);
}

void GenericQtAudioPlayer::displayErrorMessage()
{
    qDebug() << "error:" << this->m_player.errorString();
    this->setPlayingState( PlayingState::NotConnected );
}

} // namespace LFD
