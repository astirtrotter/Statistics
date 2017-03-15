package com.personaldatatracker.engine;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DataTrackerDB {
    private static final long DIV_VAL = 1000 * 60 * 60 * 24;

	// Database fields
	private DataBaseWrapper dbHelper;
	private static final String[] CATEGORY_TBL_FIELDS = { DataBaseWrapper.FIELD_CATEGORY_ID,
										DataBaseWrapper.FIELD_CATEGORY_NAME,
                                        DataBaseWrapper.FIELD_CATEGORY_UNIT};
    private static final String[] ITEM_TBL_FIELDS = { DataBaseWrapper.FIELD_ITEM_DATE,
                                        DataBaseWrapper.FIELD_ITEM_VALUE};

	private static SQLiteDatabase database = null;
	
	public DataTrackerDB(Context context) {
		dbHelper = new DataBaseWrapper(context);
	}

	private void openDatabase(boolean isWritable) throws SQLException {
        if (database != null && database.isOpen())
            database.close();

        if (isWritable)
            database = dbHelper.getWritableDatabase();
        else
            database = dbHelper.getReadableDatabase();
	}

    private void closeDatabase()
    {
        database.close();
    }

    public void clearDataBase() {
        openDatabase(true);

        database.delete(DataBaseWrapper.TBL_DATA_CATEGORY, null, null);

        closeDatabase();
    }

    public List<Category> loadData() {
        List<Category> ret = new ArrayList<>();

        openDatabase(false);

        Cursor cursor = database.query(
		    /* FROM */ DataBaseWrapper.TBL_DATA_CATEGORY,
		    /* SELECT */ CATEGORY_TBL_FIELDS,
		    /* WHERE */ null,
		    /* WHERE args */ null,
		    /* GROUP BY */ null,
		    /* HAVING */ null,
		    /* ORDER BY */ null
        );

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {

            Category category = new Category();

            String categoryName = cursor.getString(1);
            String categoryUnit = cursor.getString(2);
            List<Date> axisX = new ArrayList<>();
            List<Float> axisY = new ArrayList<>();

            category.setCategoryName(categoryName);
            category.setCategoryUnit(categoryUnit);
            category.setAxisX(axisX);
            category.setAxisY(axisY);

            Cursor cursorItem = database.query(
                /* FROM */ "[" + categoryName + "]",
                /* SELECT */ ITEM_TBL_FIELDS,
                /* WHERE */ null,
                /* WHERE args */ null,
                /* GROUP BY */ null,
                /* HAVING */ null,
                /* ORDER BY */ DataBaseWrapper.FIELD_ITEM_DATE + " ASC"
            );
            cursorItem.moveToFirst();
            while (!cursorItem.isAfterLast()) {
                long dateVal = cursorItem.getInt(0);
                dateVal *= DIV_VAL;
                Date date = new Date(dateVal);
                Float val = cursorItem.getFloat(1);

                axisX.add(date);
                axisY.add(val);

                cursorItem.moveToNext();
            }
            cursorItem.close();

            ret.add(category);

            cursor.moveToNext();
        }

        closeDatabase();
        cursor.close();

        return ret;
    }

    public void removeCategory(String categoryName) {
        openDatabase(true);

        database.delete(DataBaseWrapper.TBL_DATA_CATEGORY, DataBaseWrapper.FIELD_CATEGORY_NAME + " IS ?", new String[]{categoryName});
        dbHelper.removeCategory(database, categoryName);

        closeDatabase();
    }

    public void addCategory(String categoryName, String categoryUnit) {

        openDatabase(true);

        ContentValues values = new ContentValues();
        values.put(DataBaseWrapper.FIELD_CATEGORY_NAME, categoryName);
        values.put(DataBaseWrapper.FIELD_CATEGORY_UNIT, categoryUnit);
        database.insert(DataBaseWrapper.TBL_DATA_CATEGORY, null, values);

        dbHelper.addCategory(database, categoryName);

        closeDatabase();
    }

    public void updateCategory(String oldCategoryName, String newCategoryName, String newCategoryUnit) {
        openDatabase(true);

        ContentValues values = new ContentValues();
        values.put(DataBaseWrapper.FIELD_CATEGORY_NAME, newCategoryName);
        values.put(DataBaseWrapper.FIELD_CATEGORY_UNIT, newCategoryUnit);
        database.update(DataBaseWrapper.TBL_DATA_CATEGORY, values, DataBaseWrapper.FIELD_CATEGORY_NAME + " IS ?", new String[]{oldCategoryName});

        if (!oldCategoryName.equals(newCategoryName))
            dbHelper.renameCategory(database, oldCategoryName, newCategoryName);

        closeDatabase();
    }

    public void updateCategoryItem(String categoryName, Date oldDate, Date newDate, Float newValue) {
        openDatabase(true);

        ContentValues values = new ContentValues();
        values.put(DataBaseWrapper.FIELD_ITEM_DATE, newDate.getTime() / DIV_VAL);
        values.put(DataBaseWrapper.FIELD_ITEM_VALUE, newValue);
        database.update("[" + categoryName + "]", values, DataBaseWrapper.FIELD_ITEM_DATE + " = ?", new String[]{String.valueOf(oldDate.getTime() / DIV_VAL)});

        closeDatabase();
    }

    public void removeCategoryItem(String categoryName, Date date) {
        openDatabase(true);

        database.delete("[" + categoryName + "]", DataBaseWrapper.FIELD_ITEM_DATE + " = ?", new String[]{String.valueOf(date.getTime() / DIV_VAL)});

        closeDatabase();
    }

    public void addContentItem(String categoryName, Date date, Float value) {
        openDatabase(true);

        ContentValues values = new ContentValues();
        values.put(DataBaseWrapper.FIELD_ITEM_DATE, date.getTime() / DIV_VAL);
        values.put(DataBaseWrapper.FIELD_ITEM_VALUE, value);
        database.insert("[" + categoryName + "]", null, values);

        closeDatabase();
    }
}
