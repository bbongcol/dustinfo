package techjun.com.dustinfo.service;

import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

import org.json.JSONArray;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import techjun.com.dustinfo.model.DustSet;
import techjun.com.dustinfo.utils.LocationUtil;
import techjun.com.dustinfo.utils.NotificationUtil;

public class DustDBService extends Service {

    private final IBinder mBinder = new LocalBinder();
    private final SendMassgeHandler mMainHandler = new SendMassgeHandler();
    private NotificationUtil notificationUtil;

    DustSet myDustSet;

    private final int POOLING_FREQUENCY = 1000 * 60 * 1;//30min
    private final int START_POOLING = 1001;
    private final int STOP_POOLING = 1002;
    private final int DO_POOLING = 1003;

    private final String TAG = "DustDBService";

    public DustDBService() {

    }

    public class LocalBinder extends Binder {
        public DustDBService getService() {
            return DustDBService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        //Log.d(TAG, "onCreate");
        myDustSet = new DustSet();
        notificationUtil = NotificationUtil.getInstance(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //Log.d(TAG, "onStartCommand");
        mMainHandler.removeMessages(DO_POOLING);
        mMainHandler.sendEmptyMessage(START_POOLING);
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //Log.d(TAG, "onDestroy");
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    /** method for clients */
    public ArrayList<DustSet> getDustData(String[] address) {
        ArrayList<DustSet> mDustSet = new ArrayList<DustSet>();
        return mDustSet;
    }

    // Handler 클래스
    class SendMassgeHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            //Log.d(TAG, "handleMessage");
            super.handleMessage(msg);
            switch(msg.what){
                case START_POOLING:
                    Log.d(TAG, "handleMessage START_POOLING");
                    sendEmptyMessage(DO_POOLING);
                    break;
                case STOP_POOLING:
                    //Log.d(TAG, "handleMessage STOP_POOLING");
                    removeMessages(DO_POOLING);
                    break;
                case DO_POOLING:
                    //DB업데이트
                    Log.d(TAG, "handleMessage DO_POOLING");
                    new DustDBService.JsonLoadingTask().execute();
                    sendEmptyMessageDelayed(DO_POOLING, POOLING_FREQUENCY);
                    break;
            }
        }
    };

    private class JsonLoadingTask extends AsyncTask<String, Void, Integer> {
        @Override
        protected Integer doInBackground(String... strs) {
            //if(checkNeedToUpdate()) {
            //    Log.d(TAG,"Update Now");
                return getJsonText();
            //} else {
            //    Log.d(TAG,"No Update");
            //    return 0;
            //}
        }

        @Override
        protected void onPostExecute(Integer result) {
            //Log.d("onPostExecute", myDustSet +" " + myDustSet.getmPM10()[0]);
            //if (myCallback != null) {
            //    myCallback.OnCurrentDust(myDustSet);
            //}

            long now = System.currentTimeMillis();
            // 현재시간을 date 변수에 저장한다.
            Date date = new Date(now);
            // 시간을 나타냇 포맷을 정한다 ( yyyy/MM/dd 같은 형태로 변형 가능 )
            SimpleDateFormat sdfNow = new SimpleDateFormat("HH:mm");
            // nowDate 변수에 값을 저장한다.
            String formatDate = sdfNow.format(date);

            notificationUtil.setContentTitle(/*"업데이트: "+formatDate+*/"미세먼지: "+ myDustSet.getmPM10()[0]+"  초미세먼지: "+ myDustSet.getmPM25()[0]);
            notificationUtil.notify(0);
        }
    }

    /**
     * 원격으로부터 JSON형태의 문서를 받아서
     * JSON 객체를 생성한 다음에 객체에서 필요한 데이터 추출
     */
    private int getJsonText() {
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
                myDustSet.getmPM10()[i] = json.getJSONObject(i).getInt("pm10Value");
                myDustSet.getmPM25()[i] = json.getJSONObject(i).getInt("pm25Value");
                myDustSet.getmCurDataTime()[i] = json.getJSONObject(i).getString("dataTime");
                //Log.d(TAG,myDustSet.getmCurDataTime()[i] + " " + myDustSet.getmPM10()[i] + " " + myDustSet.getmPM25()[i]);
            }

            //JSONObject sObject = new JSONObject();
            //sObject.put("address0", myDustSet.getmCurLocation()[0]);
            //sObject.put("address1", myDustSet.getmCurLocation()[1]);
            //sObject.put("address2", myDustSet.getmCurLocation()[2]);
            //json.put(sObject);
            //savePreferences(dust_data_preference, json.toString());

        } catch (Exception e) {
            e.printStackTrace();
            // TODO: handle exception
        }
        return 0;
    }//getJsonText()-----------

    //현재 위치 String을 이용해 https 주소를 생성한다
    private String getDustUrl() {
        StringBuffer sb = new StringBuffer();
        sb.append("https://lit-inlet-76867.herokuapp.com/getDust/sido/");
        myDustSet.setmCurLocation(LocationUtil.getInstance(getApplicationContext()).getAddressList());
        //myDustSet.setmCurLocation(new String[]{"서울","서초구",""});
        switch(myDustSet.getmCurLocation()[0]) {
            case "충청북도":
                sb.append("충북");
                break;
            case "충청남도":
                sb.append("충남");
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
                sb.append(myDustSet.getmCurLocation()[0].substring(0,2));
                break;
        }
        sb.append("/");
        sb.append(myDustSet.getmCurLocation()[1]);
        return String.valueOf(sb);
    }

    // getStringFromUrl : 주어진 URL의 문서의 내용을 문자열로 반환
    private String getStringFromUrl(String pUrl){

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
                //Log.d("line:",line);
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



    /*
    //콜백 인터페이스 선언
    public interface ICallback {
        public void recvData(); //액티비티에서 선언한 콜백 함수.
    }

    private ICallback mCallback;

    public void registerCallback(ICallback cb) {
        mCallback = cb;
    }

    //액티비티에서 서비스 함수를 호출하기 위한 함수 생성
    public void myServiceFunc(){
        //서비스에서 처리할 내용
    }

    //서비스에서 액티비티 함수 호출은..
    mCallback.recvData();
    */
}
