//
//  androidaudioplayer.h
//  LFD Audio Player
//
//  Created by philipp on 04.11.2017.
//  Copyright (c) 2017 Philipp Engelhard. All rights reserved.
//
#ifndef LFD_ANDROIDAUDIOPLAYER_H
#define LFD_ANDROIDAUDIOPLAYER_H

#include "../lfdaudioplayer.h"
#include <QtAndroid>

#define AUDIOFOCUS_GAIN 1
#define AUDIOFOCUS_LOSS -1
#define AUDIOFOCUS_LOSS_TRANSIENT -2
#define AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -3
#define BACK_BUTTON_PRESSED 2

namespace LFD {

class AndroidAudioPlayer : public AudioPlayer
{
public:

	enum class FocusState : int
	{
		Paused = 0,
		Playing = 1,
		Stopped = 2
	};

	explicit AndroidAudioPlayer( QObject* parent = nullptr );

public slots:
	/// AudioPlayer override
	Q_INVOKABLE virtual void play() override;
	Q_INVOKABLE virtual void pause() override;

	virtual void setMedia(LFD::AudioMedia* media) override;
	void setFocus(int focus);
};

} /// namespace LFD

extern "C" {
JNIEXPORT jint JNICALL Java_net_quatur_QAndroidResultReceiver_jniExport_jniExport_intMethod(JNIEnv*, jobject, jint);
JNIEXPORT jint JNICALL Java_net_quatur_QAndroidResultReceiver_jniExport_jniExport_titleReporter(JNIEnv* env, jobject, jstring title);
}

#endif /// LFD_ANDROIDAUDIOPLAYER_H
