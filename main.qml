import QtQuick 2.7
import QtQuick.Controls 2.0
import QtQuick.Layouts 1.3

ApplicationWindow {
    visible: true
    width: 480
    height: 640
    title: qsTr("LFD Mobile Music Player")

    property bool isPlaying: false

    Rectangle {
        id: background

        anchors.fill: parent

        gradient: Gradient {
            GradientStop {
                position: 0.00;
                color: "#00d1ff";
            }
            GradientStop {
                position: 1.00;
                color: "#002a33";
            }
        }
    }

    Button {
        id: playPauseButton

        anchors.centerIn: parent

        text: (isPlaying ? "Pause" : "Play")

        onClicked: {
            AudioPlayer.setMediaPath("http://ice1.somafm.com/groovesalad-128-mp3")
            if (isPlaying)
                AudioPlayer.pause()
            else
                AudioPlayer.play()
            isPlaying = !isPlaying
        }
    }

}
