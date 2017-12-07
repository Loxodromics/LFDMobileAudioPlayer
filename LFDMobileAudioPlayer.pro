QT += quick
CONFIG += c++11

# The following define makes your compiler emit warnings if you use
# any feature of Qt which as been marked deprecated (the exact warnings
# depend on your compiler). Please consult the documentation of the
# deprecated API in order to know how to port your code away from it.
DEFINES += QT_DEPRECATED_WARNINGS

HEADERS += \
    src/lfdaudioplayer.h \
    src/lfdaudiomedia.h

SOURCES += src/main.cpp \
    src/lfdaudioplayer.cpp \
    src/lfdaudiomedia.cpp

RESOURCES += qml.qrc

QMAKE_INFO_PLIST = Info.plist

# Additional import path used to resolve QML modules in Qt Creator's code model
QML_IMPORT_PATH =

# Additional import path used to resolve QML modules just for Qt Quick Designer
QML_DESIGNER_IMPORT_PATH =

# Default rules for deployment.
qnx: target.path = /tmp/$${TARGET}/bin
else: unix:!android: target.path = /opt/$${TARGET}/bin
!isEmpty(target.path): INSTALLS += target

ios {
    QMAKE_CXXFLAGS += -fobjc-arc

    HEADERS += \
        src/ios/LFDAudioPlayerConstants.h \
        src/ios/LFDAudioPlayer.h \
        src/ios/iosaudioplayer.h

    SOURCES += src/ios/LFDAudioPlayerConstants.mm \
        src/ios/LFDAudioPlayer.mm \
        src/ios/iosaudioplayer.mm

        LIBS += -framework AVFoundation -framework Foundation -framework MediaPlayer

        resourceFiles.files += resources/filtermusic-logo.jpg
        resourceFiles.path = res
        QMAKE_BUNDLE_DATA += resourceFiles
} # ios

android {
    QT += androidextras

    HEADERS += \
        src/android/androidaudioplayer.h

    SOURCES += \
        src/android/androidaudioplayer.cpp

    DISTFILES += \
        android/src/net/quatur/QAndroidResultReceiver/jniExport/jniExport.java \
        android/src/net/quatur/filtermusicQt/* \
        android/AndroidManifest.xml

    ANDROID_PACKAGE_SOURCE_DIR = $$PWD/android

} # android
