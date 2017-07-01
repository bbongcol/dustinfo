package techjun.com.dustinfo.model;

/**
 * Created by leebongjun on 2017. 7. 1..
 */

public class Dust {
    private int index;
    private String mCity;
    private String mSido;
    private String mDong;
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

    public Dust(int index, String mCity, String mSido, String mDong, int year, int month, int day, int hour, int minute, float mCO, float mNO2, float mO3, int mPM10, int mPM25, float mSO2) {
        this.index = index;
        this.mCity = mCity;
        this.mSido = mSido;
        this.mDong = mDong;
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

    public void setIndex(int index) {
        this.index = index;
    }

    public String getmCity() {
        return mCity;
    }

    public void setmCity(String mCity) {
        this.mCity = mCity;
    }

    public String getmSido() {
        return mSido;
    }

    public void setmSido(String mSido) {
        this.mSido = mSido;
    }

    public String getmDong() {
        return mDong;
    }

    public void setmDong(String mDong) {
        this.mDong = mDong;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public int getDay() {
        return day;
    }

    public void setDay(int day) {
        this.day = day;
    }

    public int getHour() {
        return hour;
    }

    public void setHour(int hour) {
        this.hour = hour;
    }

    public int getMinute() {
        return minute;
    }

    public void setMinute(int minute) {
        this.minute = minute;
    }

    public int getmPM10() {
        return mPM10;
    }

    public void setmPM10(int mPM10) {
        this.mPM10 = mPM10;
    }

    public int getmPM25() {
        return mPM25;
    }

    public void setmPM25(int mPM25) {
        this.mPM25 = mPM25;
    }

    public float getmCO() {
        return mCO;
    }

    public void setmCO(float mCO) {
        this.mCO = mCO;
    }

    public float getmNO2() {
        return mNO2;
    }

    public void setmNO2(float mNO2) {
        this.mNO2 = mNO2;
    }

    public float getmO3() {
        return mO3;
    }

    public void setmO3(float mO3) {
        this.mO3 = mO3;
    }

    public float getmSO2() {
        return mSO2;
    }

    public void setmSO2(float mSO2) {
        this.mSO2 = mSO2;
    }
}
