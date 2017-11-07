
CONFIG += c++11

# The following define makes your compiler emit warnings if you use
# any feature of Qt which as been marked deprecated (the exact warnings
# depend on your compiler). Please consult the documentation of the
# deprecated API in order to know how to port your code away from it.
DEFINES += QT_DEPRECATED_WARNINGS

#INCLUDEPATH += \
#    $$PWD/src

HEADERS += \
    $$PWD/src/lfdaudioplayer.h \
    $$PWD/src/lfdaudiomedia.h

SOURCES += \
    $$PWD/src/lfdaudioplayer.cpp \
    $$PWD/src/lfdaudiomedia.cpp

ios {
    QMAKE_CXXFLAGS += -fobjc-arc

    HEADERS += \
        $$PWD/src/ios/LFDAudioPlayerConstants.h \
        $$PWD/src/ios/LFDAudioPlayer.h \
        $$PWD/src/ios/iosaudioplayer.h

    SOURCES += $$PWD/src/ios/LFDAudioPlayerConstants.mm \
        $$PWD/src/ios/LFDAudioPlayer.mm \
        $$PWD/src/ios/iosaudioplayer.mm

        LIBS += -framework AVFoundation -framework Foundation -framework MediaPlayer

} # ios

android {
    QT += androidextras

    HEADERS += \
        $$PWD/src/android/androidaudioplayer.h

    SOURCES += \
        $$PWD/src/android/androidaudioplayer.cpp

    DISTFILES += \
        $$PWD/android/src/com/ahmed/QAndroidResultReceiver/jniExport/jniExport.java \
        $$PWD/android/src/com/ahmed/biladiradio/* \
        $$PWD/android/AndroidManifest.xml

    ANDROID_PACKAGE_SOURCE_DIR = $$PWD/android

} # android
