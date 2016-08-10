package com.swdmnd.avometer;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.content.ContentValues;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Arief on 5/16/2016.
 * An SQLite database helper
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    // Logcat tag
    //private static final String TAG = "com.swdmnd.avometer.DatabaseHelper";

    // Database Version
    private static final int DATABASE_VERSION = 5;

    // Database Name
    private static final String DATABASE_NAME = "sofc";

    // Table Names
    private static final String TABLE_NAME = "sofc_table";

    // Column names
    private static final String COL_ID = "id";
    private static final String COL_DATE = "tanggal";
    private static final String COL_TIME = "waktu";
    private static final String COL_VOLTAGE = "tegangan";
    private static final String COL_CURRENT = "arus";
    private static final String COL_TEMPERATURE = "suhu";
    private static final String COL_RESISTANCE = "resistansi";

    // Table Create Statements
    private static final String CREATE_TABLE_SOFC =
            "CREATE TABLE " + TABLE_NAME +
                    "(" +
                    COL_ID + " INTEGER PRIMARY KEY, " +
                    COL_DATE + " TEXT NOT NULL, " +
                    COL_TIME + " TEXT NOT NULL, " +
                    COL_VOLTAGE + " REAL NOT NULL, " +
                    COL_CURRENT + " REAL NOT NULL, " +
                    COL_TEMPERATURE + " REAL NOT NULL, "+
                    COL_RESISTANCE + " REAL NOT NULL" +
                    ")";


    public DatabaseHelper(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(CREATE_TABLE_SOFC);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(sqLiteDatabase);
    }

    public void refreshDatabase(){
        SQLiteDatabase db = this.getReadableDatabase();
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
        db.close();
    }

    public List<String> listDates(){
        List<String> dateList = new ArrayList<>();
        String selectQuery = "SELECT DISTINCT " + COL_DATE + " FROM " + TABLE_NAME + " ORDER BY DATE(" + COL_DATE + ") ASC";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (c.moveToFirst()) {
            do {
                dateList.add(c.getString(c.getColumnIndex(COL_DATE)));
            } while (c.moveToNext());
        } else {
            c.close();
            return null;
        }

        c.close();
        db.close();
        return dateList;
    }

    public List<DataRecord> getDailyRecord(String date){
        List<DataRecord> dailyRecords = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_NAME + " WHERE " + COL_DATE + " = '" + date + "'" + " ORDER BY TIME(" + COL_TIME  + ") ASC";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (c.moveToFirst()) {
            do {
                DataRecord dataRecord = new DataRecord(
                        c.getInt(c.getColumnIndex(COL_ID)),
                        c.getString(c.getColumnIndex(COL_DATE)),
                        c.getString(c.getColumnIndex(COL_TIME)),
                        c.getDouble(c.getColumnIndex(COL_VOLTAGE)),
                        c.getDouble(c.getColumnIndex(COL_CURRENT)),
                        c.getDouble(c.getColumnIndex(COL_TEMPERATURE)),
                        c.getDouble(c.getColumnIndex(COL_RESISTANCE))
                );
                dailyRecords.add(dataRecord);
            } while (c.moveToNext());
        } else {
            c.close();
            db.close();
            return null;
        }

        c.close();
        db.close();
        return dailyRecords;
    }

    public void recordData(DataRecord dataRecord){
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COL_ID, dataRecord.getId());
        values.put(COL_DATE, dataRecord.getDate());
        values.put(COL_TIME, dataRecord.getTime());
        values.put(COL_VOLTAGE, dataRecord.getVoltage());
        values.put(COL_CURRENT, dataRecord.getCurrent());
        values.put(COL_TEMPERATURE, dataRecord.getTemperature());
        values.put(COL_RESISTANCE, dataRecord.getResistance());

        // insert data
        db.insert(TABLE_NAME, null, values);
        db.close();
    }

    public int getLastId(){
        int lastId = -1;

        String selectQuery = "SELECT * FROM " + TABLE_NAME + " ORDER BY " + COL_ID + " DESC LIMIT 1";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(selectQuery, null);

        if (c.moveToFirst()) {
            lastId = c.getInt(c.getColumnIndex(COL_ID));
        } else {
            c.close();
            return -1;
        }

        c.close();
        db.close();
        return lastId;
    }
}
