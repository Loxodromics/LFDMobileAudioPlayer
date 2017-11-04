//
//  androidaudioplayer.h
//  %PROJECT_NAME%
//
//  Created by philipp2 on 04.11.2017.
//  Copyright (c) 2017 Philipp Engelhard. All rights reserved.
//
#ifndef LFD_ANDROIDAUDIOPLAYER_H
#define LFD_ANDROIDAUDIOPLAYER_H

#include "src/lfdaudioplayer.h"
#include "src/android/radiostation.h"

namespace LFD {

class AndroidAudioPlayer : public AudioPlayer
{
public:
	explicit AndroidAudioPlayer( QObject* parent = nullptr );

	/// AudioPlayer override
	Q_INVOKABLE virtual void play() override;
	Q_INVOKABLE virtual void pause() override;

public slots:
	virtual void setMediaPath( QString mediaPath ) override;

protected:
	ahmed::RadioStatiosContainer* radioStatiosContainer;
};

} /// namespace LFD

#endif /// LFD_ANDROIDAUDIOPLAYER_H
