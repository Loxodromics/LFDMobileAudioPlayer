package net.quatur.QAndroidResultReceiver.jniExport;

/**
 * Created by ahmed on 07/11/2016.
 */


public class jniExport {
	public static native int intMethod(int n);
    public static native void sendSetFocus(int n);
//	public native int StringReceiver(String n);
//	public void intMethod(String n){};
    public static native int titleReporter(String title);
    public static native void sendSetTitle(String title);
}

