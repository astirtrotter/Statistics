package com.personaldatatracker.engine;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DataBaseWrapper extends SQLiteOpenHelper {

	private static final String DATABASE_NAME = "PersonalDataTracker.db";
	private static final int DATABASE_VERSION = 1;

	public static final String TBL_DATA_CATEGORY = "DataCategory";
	public static final String FIELD_CATEGORY_ID = "_id";					// integer
	public static final String FIELD_CATEGORY_NAME = "_name";				// text
	public static final String FIELD_CATEGORY_UNIT = "_unit";				// text


	public static final String TBL_DATA_ITEM = "###ItemTable###";
	public static final String FIELD_ITEM_DATE = "_date";					// integer (value: date_long = value * milisecond * sec * min * hour, i.e. value * 86,400,000)
	public static final String FIELD_ITEM_VALUE = "_value";				// float
	
	

	private static final String DATABASE_CREATE_CATEGORY_TBL = "create table if not exists " + TBL_DATA_CATEGORY
													+ "("
													+ FIELD_CATEGORY_ID + " integer primary key autoincrement, "
													+ FIELD_CATEGORY_NAME + " text, "
													+ FIELD_CATEGORY_UNIT + " text"
													+ ");";

	private static final String DATABASE_CREATE_ITEM_TBL = "create table if not exists " + TBL_DATA_ITEM
													+ "("
													+ FIELD_ITEM_DATE + " integer primary key, "
													+ FIELD_ITEM_VALUE + " float"
													+ ");";
	

	public DataBaseWrapper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(DATABASE_CREATE_CATEGORY_TBL);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("DROP TABLE IF EXISTS " + TBL_DATA_CATEGORY);
		onCreate(db);
	}

	public void addCategory(SQLiteDatabase db, String categoryName) {
		db.execSQL(DATABASE_CREATE_ITEM_TBL.replace(TBL_DATA_ITEM, "[" + categoryName + "]"));
	}

	public void removeCategory(SQLiteDatabase db, String categoryName) {
		db.execSQL("DROP TABLE IF EXISTS " + "[" + categoryName + "]");
	}

	public void renameCategory(SQLiteDatabase db, String oldCategoryName, String newCategoryName) {
		db.execSQL("ALTER TABLE " + "[" + oldCategoryName + "]" + " RENAME TO " + "[" + newCategoryName + "]");
	}
}
