#ifndef RADIOSTATION_H
#define RADIOSTATION_H
#include <QObject>
#include <QList>
#include <QMediaPlayer>
#include <QtCore>

#ifdef Q_OS_ANDROID
#include <QtAndroid>
#endif
#include "qdebug.h"


#define AUDIOFOCUS_GAIN 1
#define AUDIOFOCUS_LOSS -1
#define AUDIOFOCUS_LOSS_TRANSIENT -2
#define AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -3
#define BACK_BUTTON_PRESSED 2

QStringList QVariantList_to_QStringList(const QVariantList s);
QVariantList QStringList_to_QVariantList(const QStringList s);

class RadioStatiosContainer : public QObject
{
    Q_OBJECT
    QString currentStation;
    int state;

signals:
    void closeApp();
    void newStatusChanged(QMediaPlayer::MediaStatus status);
public slots:
    void statusChanged(QMediaPlayer::MediaStatus status)
    {
        qDebug()<<"statusChanged";
        emit newStatusChanged(status);
    }
public:
    RadioStatiosContainer();
    ~RadioStatiosContainer();
    Q_INVOKABLE void setStation(const QString id);
    Q_INVOKABLE void togglePlayer();
    Q_INVOKABLE int isplaying();
    Q_INVOKABLE void StopStation();
    Q_INVOKABLE void changeNotificationTitle(QString title);
    QObject *rootObject;
};

#ifdef Q_OS_ANDROID
#include <QtAndroid>


extern "C" {
JNIEXPORT jint JNICALL Java_com_ahmed_QAndroidResultReceiver_jniExport_jniExport_intMethod
(JNIEnv *, jobject, jint);

JNIEXPORT jint JNICALL Java_com_ahmed_QAndroidResultReceiver_jniExport_jniExport_StringReceiver
(JNIEnv *var1, jobject var2, jstring string);
}
#endif
#endif // RADIOSTATION_H
