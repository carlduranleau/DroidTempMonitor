package com.neurodesign.android.application;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class IntentReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.i(getClass().getName(), "neurodesign : " + "onReceive: START");
		Intent serviceIntent = new Intent();
		serviceIntent.setAction("com.neurodesign.android.application.DroidServiceActivity");
		context.startService(serviceIntent);
		Log.i(getClass().getName(), "neurodesign : " + "onReceive: END");
	}
}
