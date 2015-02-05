package com.neurodesign.android.application;

import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;

import com.neurodesign.android.application.database.DataBean;
import com.neurodesign.android.application.database.DataHandler;

public class DroidClientActivity extends Activity {
	
	private DataHandler database;
	private DataBean data;
	private TextView statusDisplay;
	private TextView temperatureDisplay;
	private TextView humidityDisplay;
	private TextView temperatureUnit;
	
	//private boolean threadAlive = false;
	private Timer refreshThread = null; 

	private LocalServiceConnection serviceConn = new LocalServiceConnection();

	
	class LocalServiceConnection implements ServiceConnection {
		IServiceBinder service;
		public void onServiceConnected(ComponentName className,	IBinder boundService) {
			service = (IServiceBinder)boundService;
			if (service != null) service.setUIOpen(true);
			Log.i(getClass().getName(), "neurodesign : " + "Service connected");
		}

		public void onServiceDisconnected(ComponentName className) {
			if (service != null) service.setUIOpen(false);
			Log.i(getClass().getName(), "neurodesign : " + "Service disconnected");
		}
	};

	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        //Try to start the background service
        if (!DroidServiceActivity.isRunning(this)) {
    		Log.i(getClass().getName(), "neurodesign : " + "Starting service");
        	IntentReceiver intent = new IntentReceiver();
        	intent.onReceive(this, null);
        } else {
    		Log.i(getClass().getName(), "neurodesign : " + "Service running");
        }

		if (serviceConn != null) bindService(new Intent(this, com.neurodesign.android.application.DroidServiceActivity.class), serviceConn, Context.BIND_AUTO_CREATE);
        
        try {
        	database = new DataHandler(this);
        } catch (Exception e) {
        	Log.e(getClass().getName(), "neurodesign : " + getClass().getName() + ".onCreate: Unable to open database" );
        	return;
        }
		
		statusDisplay = (TextView) findViewById(R.id.statusDisplay);
		temperatureDisplay = (TextView) findViewById(R.id.temperatureDisplay);
		humidityDisplay = (TextView) findViewById(R.id.humidityDisplay);
		temperatureUnit = (TextView) findViewById(R.id.temperatureUnit);
		statusDisplay.setText(getString(R.string.serviceStatusOff));
		
		refreshThread = new Timer();
		refreshThread.scheduleAtFixedRate(new TimerTask() {
			
			@Override
			public void run() {
				data = database.getData();
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						try {
							if (data.getUnit().equals("0")) {
								temperatureUnit.setText(getString(R.string.celcius));
								temperatureDisplay.setText(trunc(Double.toString(data.getTemperature())));
							} else {
								temperatureUnit.setText(getString(R.string.fahrenheit));
								temperatureDisplay.setText(trunc(Double.toString(convertToFahrenheit(data.getTemperature()))));
							}
							humidityDisplay.setText(trunc(Double.toString(data.getHumidity())));
							statusDisplay.setText(getString(R.string.serviceStatusOn));
						} catch (Exception e) {
							Log.e(getClass().getName(), "neurodesign : " + e.toString());
						}
					}
					
					private String trunc(String strDouble) {
						try {
							if (strDouble == null) return "0.00";
							int pos = strDouble.indexOf(".");
							if (pos == -1) return strDouble;
							int lastIndex = pos + 3;
							return strDouble.substring(0, lastIndex > strDouble.length() ? strDouble.length() : lastIndex);
						} catch (Exception e) {}
						return "0.00";
					}
					private double convertToFahrenheit (double celcius) {
						//return ((40D + celcius) * 1.8D) - 40D;
						return (1.8D * celcius)+32.0D;
					}
				});
			}
			
		}, 0, 5000);
	}

	@Override
	protected void onStop() {
		if (isFinishing()) {
			Log.i(getClass().getName(), "neurodesign : " + "UI is closing");
			if (refreshThread != null) refreshThread.cancel();
			if (serviceConn != null) {
				serviceConn.onServiceDisconnected(null);
				unbindService(serviceConn);
			}
		}
		super.onStop();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent idata) {
		super.onActivityResult(requestCode, resultCode, idata);

		try {

			SharedPreferences _sharedPreferences = PreferenceManager
					.getDefaultSharedPreferences(getBaseContext());
			String unit = _sharedPreferences.getString("unit", "0");
			String threshold = _sharedPreferences.getString("threshold", "25");
			
			data = database.getData();
			data.setUnit(unit);
			data.setMaxTemperature(Double.parseDouble(threshold));
			database.setData(data, DataHandler.PREFERENCES);
		} catch (Exception re) {
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
		case R.id.settings:
			showPrefs();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	public void showPrefs() {
		SharedPreferences _sharedPreferences = PreferenceManager
				.getDefaultSharedPreferences(getBaseContext());
		SharedPreferences.Editor editor = _sharedPreferences.edit();
		editor.putString("unit", data.getUnit());
		editor.putString("threshold", Double.toString(data.getMaxTemperature()));

		Intent prefsIntent = new Intent(this.getApplicationContext(),
				Preferences.class);
		startActivityForResult(prefsIntent, 0);
	}
}