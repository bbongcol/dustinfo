package techjun.com.dustinfo.service;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import techjun.com.dustinfo.R;
import techjun.com.dustinfo.model.Dust;
import techjun.com.dustinfo.utils.LocationUtil;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by leebongjun on 2017. 6. 4..
 */

public class DustService {
    public interface OnCurrentDustCB {
        void OnCurrentDust(int[] pm10, int[] pm25, String[] time);
    }

    private static DustService sInstance = null;
    Dust myDust;
    private OnCurrentDustCB myCallback;
    private Context mContext;
    final static String TAG = "DustService";

    final static String dust_data_preference = "dust_json_array";

    public DustService(Context context) {
        mContext = context;
        myDust = new Dust();
        restoreDustInfo();
    }

    public static DustService getInstance(Context context) {
        if (sInstance == null) {
            //Always pass in the Application Context
            sInstance = new DustService(context.getApplicationContext());
        }
        return sInstance;
    }

    //Call back method 호출을 위한 함수
    public void setOnCurrentDustCB (OnCurrentDustCB callback) {
        myCallback = callback;
    }

    public void updateDustInfo() {
        requestPMInfo();
    }

    public void requestPMInfo () {
        new JsonLoadingTask().execute();
    }

    private class JsonLoadingTask extends AsyncTask<String, Void, Integer> {
        @Override
        protected Integer doInBackground(String... strs) {
            if(checkNeedToUpdate()) {
                Log.d(TAG,"Update Now");
                return getJsonText();
            } else {
                Log.d(TAG,"No Update");
                return 0;
            }
        }

        @Override
        protected void onPostExecute(Integer result) {
            //Log.d("onPostExecute", myDust +" " + myDust.getmPM10()[0]);
            if (myCallback != null) {
                myCallback.OnCurrentDust(myDust.getmPM10(), myDust.getmPM25(), myDust.getmCurDataTime());
            }
        }
    }

    boolean checkNeedToUpdate() {
        boolean needToUpdate = false;
        if(myDust.getmPM10()[0] == 0 && myDust.getmPM25()[0] == 0) {
            needToUpdate = true;
        } else if(myDust.getmCurLocation()[1]!=null && myDust.getmCurLocation()[1].equalsIgnoreCase(LocationUtil.getInstance(mContext).getAddressList()[1])) {
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm");

            try {
                Date dustDataTime = df.parse(myDust.getmCurDataTime()[0]);
                Date curDateTime = df.parse(df.format(new Date()));

                long diff = curDateTime.getTime() - dustDataTime.getTime();

                if (diff > 90 * 60 * 1000) {
                    //지난번에 얻어온 값의 시간에서 1시간 30분 이상 경과 했을 경우에만 값을 다시 얻어온다.
                    needToUpdate = true;
                } else {
                    needToUpdate = false;
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        } else {
            //다른 지역인 경우
            needToUpdate = true;
        }
        return needToUpdate;
    }

    /**
     * 원격으로부터 JSON형태의 문서를 받아서
     * JSON 객체를 생성한 다음에 객체에서 필요한 데이터 추출
     */
    public int getJsonText() {
        try {
            //서버 통신 확인
            //String jsonTmp = getStringFromUrl("https://lit-inlet-76867.herokuapp.com");

            //주어진 URL 문서의 내용을 문자열로 얻는다.
            String jsonPage = getStringFromUrl(getDustUrl());

            //JSON객체를 JSONArray로 변경
            JSONArray json = new JSONArray(jsonPage);

            if(json.getJSONObject(0).has("msg") &&
                    json.getJSONObject(0).getString("msg").equalsIgnoreCase("RETRY REQUEST")) {
                jsonPage = getStringFromUrl(getDustUrl());
                json = new JSONArray(jsonPage);
            }

            //Log.d(TAG,""+json.length());
            for(int i=0; i < json.length() - 1; i++) {
                myDust.getmPM10()[i] = json.getJSONObject(i).getInt("pm10Value");
                myDust.getmPM25()[i] = json.getJSONObject(i).getInt("pm25Value");
                myDust.getmCurDataTime()[i] = json.getJSONObject(i).getString("dataTime");
                //Log.d(TAG,myDust.getmCurDataTime()[i] + " " + myDust.getmPM10()[i] + " " + myDust.getmPM25()[i]);
            }

            savePreferences(dust_data_preference, json.toString());

        } catch (Exception e) {
            e.printStackTrace();
            // TODO: handle exception
        }
        return 0;
    }//getJsonText()-----------

    //현재 위치 String을 이용해 https 주소를 생성한다
    public String getDustUrl() {
        StringBuffer sb = new StringBuffer();
        sb.append("https://lit-inlet-76867.herokuapp.com/getDust/sido/");
        myDust.setmCurLocation(LocationUtil.getInstance(mContext).getAddressList());

        switch(myDust.getmCurLocation()[0]) {
            case "충청북도":
                sb.append("충북");
                break;
            case "충청남도":
                sb.append("충");
                break;
            case "전라북도":
                sb.append("전북");
                break;
            case "전라남도":
                sb.append("전남");
                break;
            case "경상북도":
                sb.append("경북");
                break;
            case "경상남도":
                sb.append("경남");
                break;
            default:
                sb.append(myDust.getmCurLocation()[0].substring(0,2));
                break;
        }
        sb.append("/");
        sb.append(myDust.getmCurLocation()[1]);
        return String.valueOf(sb);
    }

    // getStringFromUrl : 주어진 URL의 문서의 내용을 문자열로 반환
    public String getStringFromUrl(String pUrl){

        BufferedReader bufreader=null;
        HttpURLConnection urlConnection = null;

        StringBuffer page=new StringBuffer(); //읽어온 데이터를 저장할 StringBuffer객체 생성

        try {

            URL url= new URL(pUrl);
            urlConnection = (HttpURLConnection) url.openConnection();
            InputStream contentStream = urlConnection.getInputStream();

            bufreader = new BufferedReader(new InputStreamReader(contentStream,"UTF-8"));
            String line = null;

            //버퍼의 웹문서 소스를 줄단위로 읽어(line), Page에 저장함
            while((line = bufreader.readLine())!=null){
                Log.d("line:",line);
                page.append(line);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }finally{
            //자원해제
            try {
                bufreader.close();
                urlConnection.disconnect();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return page.toString();
    }

    public void restoreDustInfo () {
        JSONArray json = getPreferences(dust_data_preference);

        try {
            if(json != null ) {
                for (int i = 0; i < json.length() - 1; i++) {
                    myDust.getmPM10()[i] = json.getJSONObject(i).getInt("pm10Value");
                    myDust.getmPM25()[i] = json.getJSONObject(i).getInt("pm25Value");
                    myDust.getmCurDataTime()[i] = json.getJSONObject(i).getString("dataTime");
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    // 값 저장하기
    private void savePreferences(String key, String value){
        Log.d(TAG,"savePreferences");
        SharedPreferences pref = mContext.getSharedPreferences(mContext.getString(R.string.dustinfo_preferences), MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString(key, value);
        editor.apply();
    }

    // 값 불러오기
    private JSONArray getPreferences(String key){
        Log.d(TAG,"getPreferences");
        SharedPreferences pref = mContext.getSharedPreferences(mContext.getString(R.string.dustinfo_preferences), MODE_PRIVATE);
        String jsonStr = pref.getString(key, null);
        JSONArray jsonArray = null;

        if(jsonStr != null) {
            try {
                jsonArray = new JSONArray(jsonStr);
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }

        return jsonArray;
    }

    // 값(ALL Data) 삭제하기
    private void removeAllPreferences(){
        SharedPreferences pref = mContext.getSharedPreferences(mContext.getString(R.string.dustinfo_preferences), MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.clear();
        editor.commit();
    }
}
