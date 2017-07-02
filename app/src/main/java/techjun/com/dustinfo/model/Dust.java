package techjun.com.dustinfo.model;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by leebongjun on 2017. 7. 1..
 */

public class Dust {
    private int index;
    private String mSido;
    private String mCity;
    private String mDateTime;
    private int year;
    private int month;
    private int day;
    private int hour;
    private int minute;
    private float mCO; //일산화탄
    private float mNO2; //이산화질소
    private float mO3; //오존
    private int mPM10; //미세먼지
    private int mPM25; //초미세먼지
    private float mSO2; //아황산가

    public Dust() {}

    public Dust(String mSido, String mCity, String mDateTime, float mCO, float mNO2, float mO3, int mPM10, int mPM25, float mSO2) {
        this.mSido = mSido;
        this.mCity = mCity;
        this.mCO = mCO;
        this.mNO2 = mNO2;
        this.mO3 = mO3;
        this.mPM10 = mPM10;
        this.mPM25 = mPM25;
        this.mSO2 = mSO2;
        this.mDateTime = mDateTime;

        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        Date dustDataTime = null;
        Calendar calendar = Calendar.getInstance();

        try {
            dustDataTime = df.parse(mDateTime);
        }  catch (ParseException e) {
            e.printStackTrace();
        }
        calendar.setTime(dustDataTime);
        this.year = calendar.get(Calendar.YEAR);
        this.month = calendar.get(Calendar.MONTH) + 1;
        this.day =  calendar.get(Calendar.DAY_OF_MONTH);
        this.hour =  calendar.get(Calendar.HOUR_OF_DAY);
        this.minute = calendar.get(Calendar.MINUTE);
    }

    public Dust(int index, String mSido, String mCity, String mDateTime, int year, int month, int day, int hour, int minute, float mCO, float mNO2, float mO3, int mPM10, int mPM25, float mSO2) {
        this.index = index;
        this.mSido = mSido;
        this.mCity = mCity;
        this.mDateTime = mDateTime;
        this.year = year;
        this.month = month;
        this.day = day;
        this.hour = hour;
        this.minute = minute;
        this.mCO = mCO;
        this.mNO2 = mNO2;
        this.mO3 = mO3;
        this.mPM10 = mPM10;
        this.mPM25 = mPM25;
        this.mSO2 = mSO2;
    }

    public int getIndex() {
        return index;
    }

    public String getmSido() {
        return mSido;
    }

    public String getmCity() {
        return mCity;
    }

    public String getmDateTime() {
        return mDateTime;
    }

    public int getYear() {
        return year;
    }

    public int getMonth() {
        return month;
    }

    public int getDay() {
        return day;
    }

    public int getHour() {
        return hour;
    }

    public int getMinute() {
        return minute;
    }

    public float getmCO() {
        return mCO;
    }

    public float getmNO2() {
        return mNO2;
    }

    public float getmO3() {
        return mO3;
    }

    public int getmPM10() {
        return mPM10;
    }

    public int getmPM25() {
        return mPM25;
    }

    public float getmSO2() {
        return mSO2;
    }
}
