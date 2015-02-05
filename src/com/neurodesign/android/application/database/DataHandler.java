package com.neurodesign.android.application.database;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

public class DataHandler {
	public static final Uri AUTHORITY = Uri
			.parse("content://com.neurodesign.android.thermonitor");

	public static final int PREFERENCES = 1;
	public static final int DATA = 2;
	public static final int ALL = 3;
	
	private Context mContext = null;
	
	public DataHandler (Context context) throws Exception {
		if (context == null) throw new Exception ("DataHandler.constructor: Database context cannot be null");
		mContext = context;
	}
	
	public DataBean getData () {
		try {
			ContentResolver cr = mContext.getContentResolver();
			Cursor cursor = cr.query(DataHandler.AUTHORITY, null, null, null, null);
			
			if (cursor.moveToFirst()) {
			
				DataBean bean = new DataBean();
				
				bean.setTemperature(cursor.getDouble(cursor.getColumnIndex("T")));
				bean.setMaxTemperature(cursor.getDouble(cursor.getColumnIndex("MT")));
				bean.setHumidity(cursor.getDouble(cursor.getColumnIndex("H")));
				bean.setUnit(cursor.getString(cursor.getColumnIndex("U")));
				
				return bean;
			}
			cursor.close();
		} catch (Exception e) {
			Log.e(getClass().getName(), "neurodesign : " + "DataHandler.getData(): " + e.toString());
		}
		return new DataBean();
	}
	
	public boolean setData(DataBean bean, int flags) {
		
		try {
			if (bean == null) return false;
			
			ContentValues content = new ContentValues();
			DataBean oldData = getData();
			
			if ((flags & DATA) != 0)
				content.put("T", bean.getTemperature());
			else
				content.put("T", oldData.getTemperature());
			
			if ((flags & PREFERENCES) != 0)
				content.put("MT", bean.getMaxTemperature());
			else
				content.put("MT", oldData.getMaxTemperature());

			if ((flags & DATA) != 0)
				content.put("H", bean.getHumidity());
			else
				content.put("H", oldData.getHumidity());
			
			if ((flags & PREFERENCES) != 0)
				content.put("U", bean.getUnit());
			else
				content.put("U", oldData.getUnit());

			ContentResolver cr = mContext.getContentResolver();
			cr.update(DataHandler.AUTHORITY, content, null, null);
			
			return true;
		}  catch (Exception e) {}
		return false;
	}
}
