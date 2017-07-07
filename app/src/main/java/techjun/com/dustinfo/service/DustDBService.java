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
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import techjun.com.dustinfo.db.DBHelperDust;
import techjun.com.dustinfo.model.Dust;
import techjun.com.dustinfo.model.DustSet;
import techjun.com.dustinfo.utils.LocationUtil;
import techjun.com.dustinfo.utils.NotificationUtil;

public class DustDBService extends Service {

    public interface ICurrentDustCallback {
        void OnCurrentDust(ArrayList<Dust> curDustArrayList);
    }

    public final int POOLING_FREQUENCY = 1000 * 30;//30sec
    public final static int START_POOLING = 1001;
    public final static int STOP_POOLING = 1002;
    public final static int DO_POOLING = 1003;
    public final String TAG = "DustDBService";

    private IBinder mBinder = new LocalBinder();
    private SendMassgeHandler mMainHandler;
    private ServiceThread mServiceThread;
    private NotificationUtil notificationUtil;
    private DBHelperDust mDBHelperDust;
    private ICurrentDustCallback mCallback;

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
        Log.d(TAG, "onCreate");
        notificationUtil = NotificationUtil.getInstance(this);
        mDBHelperDust = new DBHelperDust(this);
        mMainHandler = new SendMassgeHandler();
        mServiceThread = new ServiceThread(mMainHandler);
        mServiceThread.start();

        ArrayList<Dust> dustArrayList = mDBHelperDust.getDustList(LocationUtil.getInstance(getApplicationContext()).getCurrentSidoCity()[1]);

        if(dustArrayList.size() == 24) {
            notificationUtil.setContentTitle(/*"업데이트: "+formatDate+*/"미세먼지: "+ dustArrayList.get(0).getmPM10()+"  초미세먼지: "+ dustArrayList.get(0).getmPM25());
            notificationUtil.notify(0);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand");
        //mMainHandler.removeMessages(DO_POOLING);
        //mMainHandler.sendEmptyMessage(START_POOLING);
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mServiceThread.stopForever();
        mServiceThread = null;
        Log.d(TAG, "onDestroy");
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public void registerCallback(ICurrentDustCallback cb) {
        mCallback = cb;
    }

    /** method for clients */
    public ArrayList<Dust> requestDustData(String[] mSidoCity) {
        ArrayList<Dust> dustArrayList = mDBHelperDust.getDustList(mSidoCity[1]);
        if(checkNeedToUpdate(dustArrayList)) {
            //update 필요
            new DustDBService.JsonLoadingTask().execute(mSidoCity);
        }
        //DB의 데이터를 일단 바로 넘긴다
        return dustArrayList;
    }

    boolean checkNeedToUpdate(ArrayList<Dust> dustArrayList) {
        boolean needToUpdate = false;

        if(dustArrayList.size() != 24) {
            Log.d(TAG,"Update Case - dustArrayList.size() != 24");
            needToUpdate = true;
        } else if(dustArrayList.get(0).getmPM10() == 0 && dustArrayList.get(0).getmPM25() == 0){
            Log.d(TAG,"Update Case - data is 0");
            needToUpdate = true;
        } else  {
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            try {
                Date dustDataTime = new Date(dustArrayList.get(0).getYear() - 1900,
                        dustArrayList.get(0).getMonth() - 1 ,
                        dustArrayList.get(0).getDay(),
                        dustArrayList.get(0).getHour(),
                        dustArrayList.get(0).getMinute());
                Date curDateTime = df.parse(df.format(new Date()));

                long dustdate = dustDataTime.getTime();
                long current = curDateTime.getTime();
                long diff = current - dustdate;

                if (diff > 90 * 60 * 1000) {
                    //지난번에 얻어온 값의 시간에서 1시간 30분 이상 경과 했을 경우에만 값을 다시 얻어온다.
                    Log.d(TAG,"Update Case - new data available. Need To Update");
                    needToUpdate = true;
                } else {
                    Log.d(TAG,"Update Case - Data is up to date. ("+dustArrayList.get(0).getmDateTime()+")"+" Dela : "+diff/60000+" min");
                    needToUpdate = false;
                    mMainHandler.removeMessages(DO_POOLING);
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        return needToUpdate;
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
                    Log.d(TAG, "handleMessage STOP_POOLING");
                    removeMessages(DO_POOLING);
                    break;
                case DO_POOLING:
                    //DB업데이트
                    Log.d(TAG, "handleMessage DO_POOLING");
                    sendEmptyMessageDelayed(DO_POOLING, POOLING_FREQUENCY);
                    requestDustData(LocationUtil.getInstance(getApplicationContext()).getCurrentSidoCity());
                    //new DustDBService.JsonLoadingTask().execute();
                    break;
            }
        }
    };

    private class JsonLoadingTask extends AsyncTask<String[], Void, Integer> {
        @Override
        protected Integer doInBackground(String[]... strs) {
            //TODO 위치가 추가되면 여기서 for 문으로 추가하기
            getDustInfoJson(strs[0]);
            return 0;
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

            ArrayList<Dust> dustArrayList = mDBHelperDust.getDustList(LocationUtil.getInstance(getApplicationContext()).getCurrentSidoCity()[1]);

            if(dustArrayList.size() == 24) {
                notificationUtil.setContentTitle(/*"업데이트: "+formatDate+*/"미세먼지: "+ dustArrayList.get(0).getmPM10()+"  초미세먼지: "+ dustArrayList.get(0).getmPM25());
                notificationUtil.notify(0);

                //서비스에서 액티비티 함수 호출은..
                if(mCallback !=null) {
                    mCallback.OnCurrentDust(dustArrayList);
                }
            }
            Log.d(TAG, "Notification updated");
        }
    }

    /**
     * 원격으로부터 JSON형태의 문서를 받아서
     * JSON 객체를 생성한 다음에 객체에서 필요한 데이터 추출
     */
    private int getDustInfoJson(String[] sidocity) {
        try {
            //서버 통신 확인
            //String jsonTmp = getStringFromUrl("https://lit-inlet-76867.herokuapp.com");

            //주어진 URL 문서의 내용을 문자열로 얻는다.
            String jsonPage = getStringFromUrl(getDustUrl(sidocity));

            //JSON객체를 JSONArray로 변경
            JSONArray json = new JSONArray(jsonPage);

            if(json.getJSONObject(0).has("msg") &&
                    json.getJSONObject(0).getString("msg").equalsIgnoreCase("RETRY REQUEST")) {
                jsonPage = getStringFromUrl(getDustUrl(sidocity));
                json = new JSONArray(jsonPage);
            }

            ArrayList<Dust> dustArrayList = new ArrayList<Dust>();


            for(int i=0; i < json.length() - 1; i++) {
                Dust dust = new Dust(
                        json.getJSONObject(i).getString("sidoName"),
                        json.getJSONObject(i).getString("cityName"),
                        json.getJSONObject(i).getString("dataTime"),
                        (float)json.getJSONObject(i).getDouble("coValue"),
                        (float)json.getJSONObject(i).getDouble("no2Value"),
                        (float)json.getJSONObject(i).getDouble("o3Value"),
                        json.getJSONObject(i).getInt("pm10Value"),
                        json.getJSONObject(i).getInt("pm25Value"),
                        (float)json.getJSONObject(i).getDouble("so2Value")
                );
                dustArrayList.add(dust);
            }

            //TODO db 전체가 아니라 일부만 업데이트 되도록 수정 필요. 일단은 앞에꺼를 지우고 다시 넣는 방식으로 적용
            if(checkNeedToUpdate(mDBHelperDust.getDustList(LocationUtil.getInstance(getApplicationContext()).getCurrentSidoCity()[1]))) {
                Log.d(TAG, "DB updated");
                mDBHelperDust.delete(LocationUtil.getInstance(getApplicationContext()).getCurrentSidoCity()[1]);
                mDBHelperDust.insertDustList(dustArrayList);
            }
        } catch (Exception e) {
            e.printStackTrace();
            // TODO: handle exception
        }
        return 0;
    }//getJsonText()-----------

    //현재 위치 String을 이용해 https 주소를 생성한다
    private String getDustUrl(String[] sidocity) {
        StringBuffer sb = new StringBuffer();
        sb.append("https://lit-inlet-76867.herokuapp.com/getDust/sido/");
        sb.append(sidocity[0]);
        sb.append("/");
        sb.append(sidocity[1]);
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
}
