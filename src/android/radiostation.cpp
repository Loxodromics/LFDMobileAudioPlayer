#include "radiostation.h"

namespace ahmed {

void RadioStatiosContainer::statusChanged(QMediaPlayer::MediaStatus status)
{
	qDebug()<<"statusChanged";
	emit newStatusChanged(status);
}

RadioStatiosContainer::RadioStatiosContainer()
{

}

RadioStatiosContainer::~RadioStatiosContainer()
{

}

void RadioStatiosContainer::playStation()
{
	QtAndroid::runOnAndroidThread([] {
		QtAndroid::androidActivity().callMethod<void>("playstation");
	});
}

void RadioStatiosContainer::pauseStation()
{
	QtAndroid::runOnAndroidThread([] {
		QtAndroid::androidActivity().callMethod<void>("pausestation");
	});
}

void RadioStatiosContainer::setStation(const QString id)
{
	QtAndroid::runOnAndroidThread([id] {
		QtAndroid::androidActivity().callMethod<void>("setestation","(Ljava/lang/String;)V",
												   QAndroidJniObject::fromString(id).object<jstring>());
	});
}

void RadioStatiosContainer::togglePlayer()
{
	QtAndroid::runOnAndroidThread([] {
			QtAndroid::androidActivity().callMethod<void>("togglestate");
	});

}

int RadioStatiosContainer::isplaying()
{
	return state;
}

void RadioStatiosContainer::changeNotificationTitle(QString title)
{
	QString mTitle = title;
	QtAndroid::runOnAndroidThread([mTitle] {
		QtAndroid::androidActivity().callMethod<void>("setnotificationtext","(Ljava/lang/String;)V",
												   QAndroidJniObject::fromString(mTitle).object<jstring>());
	});
}

} /// namespace ahmed
