//
//  main.cpp
//  iOS Media Player
//
//  Created by philipp on 27.09.2017.
//  Copyright (c) 2017 Philipp Engelhard. All rights reserved.
//
#include <QGuiApplication>
#include <QQmlApplicationEngine>
#include <QQmlContext>
#include <QDebug>

#ifdef Q_OS_IOS
#include "src/ios/iosaudioplayer.h"
#endif

#ifdef Q_OS_ANDROID
#include "src/android/androidaudioplayer.h"
LFD::AndroidAudioPlayer* audioPlayer;
#endif

#ifdef Q_OS_ANDROID

JNIEXPORT jint JNICALL Java_net_quatur_QAndroidResultReceiver_jniExport_jniExport_intMethod(JNIEnv*, jobject, jint focusChange)
{
	/// TODO
//	static int lastfocus = 0;
//	rootObject->setProperty("command", focusChange);
	qDebug() << "yyy_Java_net_quatur_QAndroidResultReceiver_jniExport_jniExport_intMethod " << focusChange;
	audioPlayer->setFocus(focusChange);

	return 1;
}

JNIEXPORT jint JNICALL Java_net_quatur_QAndroidResultReceiver_jniExport_jniExport_StringReceiver(JNIEnv *env, jobject var2, jstring string)
{
	Q_UNUSED(env);
	Q_UNUSED(var2);
	Q_UNUSED(string);

	return 1;
}
#endif ///Q_OS_ANDROID

int main(int argc, char *argv[])
{
	QCoreApplication::setAttribute(Qt::AA_EnableHighDpiScaling);
	QGuiApplication app(argc, argv);
	QQmlApplicationEngine engine;

#ifdef Q_OS_IOS
	LFD::IosAudioPlayer audioPlayer;
	engine.rootContext()->setContextProperty("AudioPlayer", &audioPlayer);
#endif

#ifdef Q_OS_ANDROID
	audioPlayer = new LFD::AndroidAudioPlayer();
	engine.rootContext()->setContextProperty("AudioPlayer", audioPlayer);
#endif ///Q_OS_ANDROID

	engine.load(QUrl(QLatin1String("qrc:/main.qml")));
	if (engine.rootObjects().isEmpty())
		return -1;

	return app.exec();
}
