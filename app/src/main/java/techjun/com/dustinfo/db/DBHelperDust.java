package techjun.com.dustinfo.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import techjun.com.dustinfo.model.Dust;

/**
 * Created by leebongjun on 2017. 6. 13..
 */

public class DBHelperDust extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "DustDB.db";
    private static final int DATABASE_VERSION = 1;
    Context context;

    // TODO db를 전역 변수로 사용할지 지역변수로 사용할지 결정

   // DBHelper 생성자로 관리할 DB 이름과 버전 정보를 받음
    public DBHelperDust(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        // TODO Auto-generated constructor stub
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE dust_data (" +
                "_id Integer PRIMARY KEY AUTOINCREMENT, " +
                "sido TEXT, " +
                "city TEXT, " +
                "datetime TEXT, " +
                "year Integer, " +
                "month Integer, " +
                "day Integer, " +
                "hour Integer, " +
                "minute Integer, " +
                "covalue REAL, " +
                "no2value REAL, " +
                "o3value REAL, " +
                "pm10value Integer, " +
                "pm25value Integer, " +
                "so2value REAL);");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public void insertDustList(ArrayList<Dust> dustArryList) {
        SQLiteDatabase db = getWritableDatabase();

        for(int i = 0; i < dustArryList.size(); i++) {
            ContentValues values = new ContentValues();
            values.put("sido", dustArryList.get(i).getmSido());
            values.put("city", dustArryList.get(i).getmCity());
            values.put("datetime", dustArryList.get(i).getmDateTime());
            values.put("year", dustArryList.get(i).getYear());
            values.put("month", dustArryList.get(i).getMonth());
            values.put("day", dustArryList.get(i).getDay());
            values.put("hour", dustArryList.get(i).getHour());
            values.put("minute", dustArryList.get(i).getMinute());
            values.put("covalue", dustArryList.get(i).getmCO());
            values.put("no2value", dustArryList.get(i).getmNO2());
            values.put("o3value", dustArryList.get(i).getmO3());
            values.put("pm10value", dustArryList.get(i).getmPM10());
            values.put("pm25value", dustArryList.get(i).getmPM25());
            values.put("so2value", dustArryList.get(i).getmSO2());

            db.insert("dust_data", null, values);
        }
        db.close();
    }

    //Dust 정보 업데이트
    public void updateDust(String city, int hour, Dust dust) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("year", dust.getYear());
        values.put("month", dust.getMonth());
        values.put("datetime", dust.getmDateTime());
        values.put("day", dust.getDay());
        //values.put("hour", dust.getHour());  //do not need to update
        values.put("minute", dust.getMinute());
        values.put("covalue", dust.getmCO());
        values.put("no2value", dust.getmNO2());
        values.put("o3value", dust.getmO3());
        values.put("pm10value", dust.getmPM10());
        values.put("pm25value", dust.getmPM25());
        values.put("so2value", dust.getmSO2());

        db.update("dust_data", values, "city=? AND hour=?", new String[]{city, String.valueOf(hour)});
        db.close();
    }

    public void delete(String city) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete("dust_data", "city=?", new String[]{String.valueOf(city)});
        db.close();
    }

    public ArrayList<Dust> getDustList(String city) {
        ArrayList<Dust> dustArrayList = new ArrayList<Dust>();
        SQLiteDatabase db = getWritableDatabase();

        //Cursor cursor = db.rawQuery("SELECT _id, city, sido, dong, year, month, day, hour, minute, covalue, no2value, o3value, pm10value, pm25value, so2value FROM dust_data", null);
        Cursor cursor = db.query("dust_data", null, null, null, null, null, null);

        while (cursor.moveToNext()) {
            if(city.equalsIgnoreCase(cursor.getString(cursor.getColumnIndex("city")))) {
                Dust dust = new Dust(cursor.getInt(cursor.getColumnIndex("_id")), //id
                        cursor.getString(cursor.getColumnIndex("sido")), //sido
                        cursor.getString(cursor.getColumnIndex("city")), //city
                        cursor.getString(cursor.getColumnIndex("datetime")), //datetime
                        cursor.getInt(cursor.getColumnIndex("year")), //year
                        cursor.getInt(cursor.getColumnIndex("month")), //month
                        cursor.getInt(cursor.getColumnIndex("day")), //day
                        cursor.getInt(cursor.getColumnIndex("hour")), //hour
                        cursor.getInt(cursor.getColumnIndex("minute")), //minute
                        cursor.getFloat(cursor.getColumnIndex("covalue")), //covalue
                        cursor.getFloat(cursor.getColumnIndex("no2value")), //no2value
                        cursor.getFloat(cursor.getColumnIndex("o3value")), //o3vaule
                        cursor.getInt(cursor.getColumnIndex("pm10value")), //pm10value
                        cursor.getInt(cursor.getColumnIndex("pm25value")), //pm25value
                        cursor.getFloat(cursor.getColumnIndex("so2value")) //so2value
                        );
                dustArrayList.add(dust);
            }
        }
        return dustArrayList;
    }
}

