package com.neurodesign.android.application.database;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.util.Log;

public class DataProvider extends ContentProvider {

	public static final Uri AUTHORITY = Uri
			.parse("content://com.neurodesign.android.thermonitor");

	public static final int IDX_ID = 0;
	public static final int IDX_TEMPERATURE = 1;
	public static final int IDX_MAXTEMPERATURE = 2;
	public static final int IDX_HUMIDITY = 3;
	public static final int IDX_UNIT = 4;

	public static final String _ID = "_id";
	public static final String _TEMPERATURE = "T";
	public static final String _MAXTEMPERATURE = "MT";
	public static final String _HUMIDITY = "H";
	public static final String _UNIT = "U";

	private static final int DATABASE_VERSION = 1;
	private static final String DATABASE_NAME = "thermonitor.db";
	private static final String TABLE_NAME = "DataBean";
	private static final String CREATE_TABLE = "create table " + TABLE_NAME
			+ " (" + _ID
			+ " integer primary key autoincrement, "
			+ _TEMPERATURE
			+ " real, "
			+ _MAXTEMPERATURE
			+ " real, "
			+ _HUMIDITY
			+ " real, "
			+ _UNIT
			+ " text);";

	private SQLDBHelper mSQLHelper;
	
	/*
	 * 
	 * SQL Helper class
	 * 
	 * */
	private static class SQLDBHelper extends SQLiteOpenHelper {

		SQLDBHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
			Log.i(getClass().getName(), "neurodesign : " +
					"Creating helper database object.");
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			Log.i(getClass().getName(), "neurodesign : " + "Creating database.");
			db.execSQL(CREATE_TABLE);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			Log.i(getClass().getName(), "neurodesign : " +
					"Upgrading database, this will drop tables and recreate.");
			db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
			onCreate(db);
		}
	}
	
	
	@Override
	public int delete(Uri arg0, String arg1, String[] arg2) {
		try {
			SQLiteDatabase db = mSQLHelper.getWritableDatabase();
			db.delete(TABLE_NAME, null, null);
			return 1;
		} catch (Exception e) {}
		return 0;
	}

	@Override
	public String getType(Uri arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
        if (values == null) return null;

        SQLiteDatabase db = mSQLHelper.getWritableDatabase();
        db.delete(TABLE_NAME, null, null);
        long rowId = db.insert(TABLE_NAME, null, values);
        if (rowId > 0) {
            Uri noteUri = ContentUris.withAppendedId(DataProvider.AUTHORITY, rowId);
            getContext().getContentResolver().notifyChange(noteUri, null);
            return noteUri;
        }

        throw new SQLException("Failed to insert row into " + uri);
	}

	@Override
	public boolean onCreate() {
        mSQLHelper = new SQLDBHelper(getContext());
		return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
            String sortOrder) {

		try {
	        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
	        qb.setTables(TABLE_NAME);
	
	        SQLiteDatabase db = mSQLHelper.getReadableDatabase();
	        Cursor c = qb.query(db, projection, selection, selectionArgs, null, null, null);
	        return c;
		} catch (Exception e) {}
		return null;
	}

	@Override
	public int update(Uri uri, ContentValues values, String where, String[] whereArgs) {
		try {
	        insert(uri, values);
			return 1;
		} catch (Exception e) {}
		return 0;
	}
}
