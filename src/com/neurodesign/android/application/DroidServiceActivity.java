package com.neurodesign.android.application;

import org.apache.http.HttpHost;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.impl.client.DefaultHttpClient;
import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.PropertyInfo;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapPrimitive;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.AndroidHttpTransport;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.neurodesign.android.application.database.DataBean;
import com.neurodesign.android.application.database.DataHandler;

public class DroidServiceActivity extends Service {
	private static final long SHORT_INTERVAL = 10000;
	private static final long LONG_INTERVAL = 300000;
	
	//public static final String ServiceEndpoint = "http://dominodoc.neuro-design.com:8080/axis2/services/ThermonitorServices";
	public static final String ServiceEndpoint = "http://quickr.neuro-design.com:8888/axis2/services/ThermonitorServices";
	
	private DataHandler database;

	private double mTemperature = 0;
	private double mHumidity = 0;
	private double mThreshold = 25;
	
	private MediaPlayer mPlayer = null;
	
	private boolean isUIOpen = false;
	private boolean isAlertOn = false;
	
	private IBinder binder = new ServiceBinder();
	
	public class ServiceBinder extends Binder implements IServiceBinder {
		public void setUIOpen (boolean flag) {
			Log.i(getClass().getName(), "neurodesign : " + "setUIOpen:" + (flag ? "true": "false"));
			isUIOpen = flag;
		}
	}
	
	@Override
	public IBinder onBind(Intent arg0) {
		//May set isUIOpen flag here and return null.
		//But to be able to share other function in 
		//the future, we'll stick to this method.
		return binder;
	}

	@Override
	public boolean onUnbind(Intent intent) {
		//May set isUIOpen flag here and return null.
		//But to be able to share other function in 
		//the future, we'll stick to this method.
		return super.onUnbind(intent);
	}

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		Log.i(getClass().getName(), "neurodesign : " + "onCreate: START");
		
		try {
			database = new DataHandler(this);
			_startService();
		} catch (Exception e) {
			Log.e(getClass().getName(), "neurodesign : " +getClass().getName() + ": " + e.toString());
			Log.e(getClass().getName(), "neurodesign : " +getClass().getName() + ": Unable to start service");
		}
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		Log.i(getClass().getName(), "neurodesign : " + "onDestroy: START");
		_shutdownService();
	}

	@Override
	public void onStart(Intent intent, int startId) {
		// TODO Auto-generated method stub
		super.onStart(intent, startId);
	}
	
	private void _startService () {
		Log.i(getClass().getName(), "neurodesign : " + "_startService: START");

		new Thread() {
			@Override
			public void run() {
				long interval = LONG_INTERVAL;
				boolean isThreadActive = true;
				while (isThreadActive) {
					readData();
					checkThreshold();
					try {
						for (long t = 0; t <= interval; t += 1000) {
							interval = (isUIOpen || isAlertOn) ? SHORT_INTERVAL : LONG_INTERVAL;
							sleep(1000);
						}
					} catch (Exception e) {
						Log.e(getClass().getName(), "neurodesign : " + e.toString());
						Log.e(getClass().getName(), "neurodesign : " + "Background Thread stopped.");
						isThreadActive = false;
					}
				}
			}
		}.start();
	}
	
	private void _shutdownService () {
		Log.i(getClass().getName(), "neurodesign : " + "_shutdownService: START");
	}
	
	public boolean readData() {
		
		Log.i(getClass().getName(), "neurodesign : " + "readData: START");

		AndroidHttpTransport transport = null;
		try {
			
			DefaultHttpClient client = new DefaultHttpClient();
            HttpHost proxy = new HttpHost("proxy.quebec.organisation.int.que",8080,"http");
            client.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, proxy);
			

			DataBean oldData = database.getData();

			Log.i(getClass().getName(), "neurodesign : " + "Background Thread.Reading data...");

			SoapObject rpc = new SoapObject("http://ws.apache.org/axis2", "getTemperature");
			PropertyInfo param = new PropertyInfo();
			param.setName("args0");
			param.setValue(new Integer(0));
			param.setType(Integer.class);
			rpc.addProperty(param);
			SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
			envelope.setOutputSoapObject(rpc);

			Log.i(getClass().getName(), "neurodesign : " + "readData: 1");
			
			transport = new AndroidHttpTransport(ServiceEndpoint);
			transport.debug = true;

			Log.i(getClass().getName(), "neurodesign : " + "readData: 2");
			
			transport.call("http://ws.apache.org/axis2/getTemperature", envelope);
			
			Log.i(getClass().getName(), "neurodesign : " + "readData: 3");
			
			SoapPrimitive response = (SoapPrimitive)envelope.getResponse();

			Log.i(getClass().getName(), "neurodesign : " + "readData: 4");

			
			Double resultT = new Double(0);
			try {
				resultT = new Double(response.toString());
			} catch (Exception e) {}

			Log.i(getClass().getName(), "neurodesign : " + "readData: 5");
			
			mTemperature = resultT.doubleValue();
			oldData.setTemperature(mTemperature);

			Log.i(getClass().getName(), "neurodesign : " + "Temperature: " + Double.toString(mTemperature) + " C");
			
			rpc = new SoapObject("http://ws.apache.org/axis2",
					"getHumidity");
			envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
			envelope.setOutputSoapObject(rpc);
			transport = new AndroidHttpTransport(ServiceEndpoint);
			transport.debug = true;
			
			transport.call("http://ws.apache.org/axis2/getHumidity", envelope);

			response = (SoapPrimitive)envelope.getResponse();
			
			Double resultH = new Double(0);
			try {
				resultH = new Double(response.toString());
			} catch (Exception e) {}
			
			mHumidity = resultH.doubleValue();
			oldData.setHumidity(mHumidity);
			
			Log.i(getClass().getName(), "neurodesign : " +
					"Humidity: " + Double.toString(mHumidity) + " %");
			
			database.setData(oldData, DataHandler.DATA);

			Log.i(getClass().getName(), "neurodesign : " + "readData: END");

			return true;
		} catch (Exception e) {
			Log.e(getClass().getName(), "neurodesign : " + "readData: " + e.toString());
			e.printStackTrace();
		}
		return false;
	}
	
	
	public void checkThreshold() {
		Log.i(getClass().getName(), "neurodesign : " +
				"Threshold: " + Double.toString(mThreshold));
		
		if (mTemperature >= mThreshold) {
			try {
				isAlertOn = true;
				Log.i(getClass().getName(), "neurodesign : " + "ALERT!");
				if (mPlayer == null) {
					mPlayer = MediaPlayer.create(this, R.raw.alarm);
					mPlayer.setLooping(true);
					mPlayer.setVolume(1, 1);
					mPlayer.start();
				}
				
			} catch (Exception e) {
				mPlayer = null;
				Log.e(getClass().getName(), e.toString());
				Log.i(getClass().getName(), "neurodesign : " +
						"checkTreshold:" + e.toString());
			}
		} else {
			isAlertOn = false;
			if (mPlayer != null) {
				mPlayer.stop();
				mPlayer.reset();
				mPlayer = null;
			}
		}
	}

	public static boolean isRunning(Context context) {
		ActivityManager manager = (ActivityManager) context.getSystemService(ACTIVITY_SERVICE);
		Log.i(DroidServiceActivity.class.getName(), "neurodesign : " + "Searching for " + DroidServiceActivity.class.getName());
		for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
			if (DroidServiceActivity.class.getName().equals(service.service.getClassName())) {
				Log.i(DroidServiceActivity.class.getName(), "neurodesign : " + "Found!");
				return true;
			}
		}
		return false;
	} 
}