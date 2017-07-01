package techjun.com.dustinfo.model;

/**
 * Created by leebongjun on 2017. 6. 4..
 */

public class DustSet {
    public final static int DUST_INFO_LENGTH = 24;
    private String[] mCurLocation = new String[3];
    private String[] mCurDataTime = new String[DUST_INFO_LENGTH];
    private int[] mPM10 = new int[DUST_INFO_LENGTH]; //미세먼지
    private int[] mPM25 = new int[DUST_INFO_LENGTH]; //초미세먼지
    private float[] mCO = new float[DUST_INFO_LENGTH]; //일산화탄
    private float[] mNO2 = new float[DUST_INFO_LENGTH]; //이산화질소
    private float[] mO3 = new float[DUST_INFO_LENGTH]; //오존
    private float[] mSO2 = new float[DUST_INFO_LENGTH]; //아황산가

    public DustSet() {}

    public DustSet(String[] mCurLocation) {
        this.mCurLocation = mCurLocation;
    }

    public void setmPM10(int[] mPM10) {
        this.mPM10 = mPM10;
    }

    public void setmPM25(int[] mPM25) {
        this.mPM25 = mPM25;
    }

    public void setmCurLocation(String[] mCurLocation) {
        this.mCurLocation = mCurLocation;
    }

    public void setmCurDataTime(String[] mCurDataTime) {
        this.mCurDataTime = mCurDataTime;
    }

    public String[] getmCurDataTime() {
        return mCurDataTime;
    }

    public int[] getmPM10() {
        return mPM10;
    }

    public int[] getmPM25() {
        return mPM25;
    }

    public String[] getmCurLocation() {
        return mCurLocation;
    }


    public float[] getmCO() {
        return mCO;
    }

    public void setmCO(float[] mCO) {
        this.mCO = mCO;
    }

    public float[] getmNO2() {
        return mNO2;
    }

    public void setmNO2(float[] mNO2) {
        this.mNO2 = mNO2;
    }

    public float[] getmO3() {
        return mO3;
    }

    public void setmO3(float[] mO3) {
        this.mO3 = mO3;
    }

    public float[] getmSO2() {
        return mSO2;
    }

    public void setmSO2(float[] mSO2) {
        this.mSO2 = mSO2;
    }
}
