package net.quatur.filtermusicQt;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import net.quatur.QAndroidResultReceiver.jniExport.jniExport;

public class BackgroundBroadcastReceiver extends BroadcastReceiver {
	private static final String TAG = BackgroundBroadcastReceiver.class.getSimpleName();

	private static final String MESSAGE_ID = "message_id";
    public jniExport m_jniExport;  /// find a nicer solution to this...

	@Override
	public void onReceive(Context context, Intent intent) {
		Bundle extra = intent.getExtras();
        String title = extra.getString("title");
        gotTitle(title);
	}

    public void gotTitle(String title) {
        Log.d(TAG, "gotTitle: " + title);
        if (title != "")
            m_jniExport.sendSetTitle(title);
    };
}

