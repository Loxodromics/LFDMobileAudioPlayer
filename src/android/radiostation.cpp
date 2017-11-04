#include "radiostation.h"

namespace ahmed {

RadioStatiosContainer::RadioStatiosContainer(){

}

void RadioStatiosContainer::StopStation(){
	//rootObject->setProperty("command", 0);
	QtAndroid::runOnAndroidThread([] {
		QtAndroid::androidActivity().callMethod<void>("pausestation");
	});
}

void RadioStatiosContainer::setStation(const QString id){
	QtAndroid::runOnAndroidThread([id] {
		QtAndroid::androidActivity().callMethod<void>("setestation","(Ljava/lang/String;)V",
												   QAndroidJniObject::fromString(id).object<jstring>());
	});
}

RadioStatiosContainer::~RadioStatiosContainer(){
}

void RadioStatiosContainer::togglePlayer(){
	QtAndroid::runOnAndroidThread([] {
			QtAndroid::androidActivity().callMethod<void>("togglestate");
	});

}

int RadioStatiosContainer::isplaying(){
	return state;
}

void RadioStatiosContainer::changeNotificationTitle(QString title){
	QString mTitle = title;
	QtAndroid::runOnAndroidThread([mTitle] {
		QtAndroid::androidActivity().callMethod<void>("setnotificationtext","(Ljava/lang/String;)V",
												   QAndroidJniObject::fromString(mTitle).object<jstring>());
	});
}

} /// namespace ahmed
