package com.example.weatherappq2;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import java.util.ArrayList;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "weather.db";
    private static final String TABLE_NAME = "weatherData";
    private static final String[] ColumnNames = {"id", "date", "location", "isFuture", "minTemp", "maxTemp", "description", "icon"};

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, 1);
    }

    public void insertData(String date, String location, boolean isFuture, String minTemp, String maxTemp, String icon, String description) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("INSERT INTO " + TABLE_NAME + " (date, location, isFuture, minTemp, maxTemp, icon, description) VALUES ('" + date + "', '" + location + "', " + isFuture + ", '" + minTemp + "', '" + maxTemp + "', '" + icon + "', '" + description + "')");
    }

    @SuppressLint("Range")
    public ArrayList<String> getData(String city, String date, Boolean isFuture) {
        String query = "SELECT * FROM " + TABLE_NAME + " WHERE location = '" + city + "' AND date = '" + date + "' AND isFuture = " + isFuture;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(query, null);
        ArrayList<String> data = new ArrayList<>();
        if (cursor.moveToFirst()) {
            for (int i = 4; i < ColumnNames.length; i++) {
                data.add(cursor.getString(cursor.getColumnIndex(ColumnNames[i])));
            }
        }
        cursor.close();
        return data;
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_NAME + " (id INTEGER PRIMARY KEY AUTOINCREMENT, date TEXT, location TEXT, isFuture BOOLEAN, minTemp TEXT, maxTemp TEXT, icon TEXT, description TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);

        onCreate(db);
    }
}
