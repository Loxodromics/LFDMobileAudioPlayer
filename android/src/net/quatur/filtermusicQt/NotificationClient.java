package net.quatur.filtermusicQt;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;

public class NotificationClient extends org.qtproject.qt5.android.bindings.QtActivity {
    private static NotificationManager m_notificationManager;
    private static Notification.Builder m_builder;
    private static NotificationClient m_instance;

    public NotificationClient() {
        m_instance = this;
    }
}
