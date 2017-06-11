package techjun.com.dustinfo.model;

/**
 * Created by leebongjun on 2017. 6. 4..
 */

public class Dust {
    private int[] mPM10 = new int[24];
    private int[] mPM25 = new int[24];
    private String[] mCurDataTime = new String[24];
    private String[] mCurLocation = new String[3];

    public Dust () {}

    public Dust (String[] mCurLocation) {
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
}
