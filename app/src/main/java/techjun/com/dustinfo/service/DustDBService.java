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
import java.util.Date;

import techjun.com.dustinfo.db.DBHelperDust;
import techjun.com.dustinfo.model.Dust;
import techjun.com.dustinfo.utils.NotificationUtil;

import static java.lang.Thread.sleep;

public class DustDBService extends Service {

    public interface ICurrentDustCallback {
        void OnCurrentDust(ArrayList<Dust> curDustArrayList);
    }

    public final int POOLING_FREQUENCY = 1000 * 30;//30sec
    public final int JSON_RETRY_COUNT = 3;
    public final static int START_POOLING = 1001;
    public final static int STOP_POOLING = 1002;
    public final static int DO_POOLING = 1003;
    public final String TAG = "DustDBService";

    //public final String SERVER_ADDRESS = "https://lit-inlet-76867.herokuapp.com";
    //public final String SERVER_ADDRESS = "http://techjunsoft.hopto.org:5000";
    public final String SERVER_ADDRESS = "http://192.168.219.108:5000";

    private IBinder mBinder = new LocalBinder();
    private SendMassgeHandler mMainHandler;
    private ServiceThread mServiceThread;
    private NotificationUtil notificationUtil;
    private DBHelperDust mDBHelperDust;
    private ICurrentDustCallback mCallback;

    private String[] mSidoCity;

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
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand");
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

    public void requestDustData(String[] requestSidoCity) {
        Log.d(TAG, "requestDustData requestSidoCity : " + requestSidoCity[1]);
        mSidoCity = requestSidoCity;
        new DustDBService.JsonLoadingTask().execute(requestSidoCity);
    }

    boolean checkNeedToUpdate(ArrayList<Dust> dustArrayList) {
        boolean needToUpdate = false;
        Log.d(TAG, "checkNeedToUpdate dustArrayList.size() : " + dustArrayList.size());
        if (dustArrayList.size() == 0) {
            Log.d(TAG, "Update Case - dustArrayList.size() == 0");
            needToUpdate = true;
        } else if (dustArrayList.size() < 24) {
            Log.d(TAG, "Update Case - ustArrayList.size() < 24");
            needToUpdate = true;
        } else if (dustArrayList.get(0).getmPM10() == 0 && dustArrayList.get(0).getmPM25() == 0) {
            Log.d(TAG, "Update Case - data is 0");
            needToUpdate = true;
        } else {
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            try {
                Date dustDataTime = new Date(dustArrayList.get(0).getYear() - 1900,
                        dustArrayList.get(0).getMonth() - 1,
                        dustArrayList.get(0).getDay(),
                        dustArrayList.get(0).getHour(),
                        dustArrayList.get(0).getMinute());
                Date curDateTime = df.parse(df.format(new Date()));

                long dustdate = dustDataTime.getTime();
                long current = curDateTime.getTime();
                long diff = current - dustdate;

                if (diff > 90 * 60 * 1000) {
                    //지난번에 얻어온 값의 시간에서 1시간 30분 이상 경과 했을 경우에만 값을 다시 얻어온다.
                    Log.d(TAG, "Update Case - new data available. Need To Update");
                    needToUpdate = true;
                } else {
                    Log.d(TAG, "Update Case - Data is up to date. (" + dustArrayList.get(0).getmDateTime() + ")" + " Dela : " + diff / 60000 + " min");
                    needToUpdate = false;
                    mMainHandler.removeMessages(DO_POOLING);
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        return needToUpdate;
    }

    boolean checkNeedToUpdateDB(ArrayList<Dust> curDBDustArrayList, ArrayList<Dust> newDustInfo) {
        boolean needToUpdate = false;

        Log.d(TAG, "checkNeedToUpdateDB curDBDustArrayList.size() : " + curDBDustArrayList.size());

        if (curDBDustArrayList.size() == 0) {
            Log.d(TAG, "checkNeedToUpdateDB Update Case - curDBDustArrayList.size() == 0");
            needToUpdate = true;
        }  else if (curDBDustArrayList.size() < 24) {
            Log.d(TAG, "Update Case - ustArrayList.size() < 24");
            needToUpdate = true;
        } else if (curDBDustArrayList.get(0).getmPM10() == 0 && curDBDustArrayList.get(0).getmPM25() == 0) {
            Log.d(TAG, "checkNeedToUpdateDB Update Case - data is 0");
            needToUpdate = true;
        } else {
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm");

            Date dustDataTime = new Date(curDBDustArrayList.get(0).getYear() - 1900,
                    curDBDustArrayList.get(0).getMonth() - 1,
                    curDBDustArrayList.get(0).getDay(),
                    curDBDustArrayList.get(0).getHour(),
                    curDBDustArrayList.get(0).getMinute());

            Date newDustInfoDateTime = new Date(newDustInfo.get(0).getYear() - 1900,
                    newDustInfo.get(0).getMonth() - 1,
                    newDustInfo.get(0).getDay(),
                    newDustInfo.get(0).getHour(),
                    newDustInfo.get(0).getMinute());

            long dustdate = dustDataTime.getTime();
            long current = newDustInfoDateTime.getTime();
            long diff = current - dustdate;

            if (diff >= 60 * 60 * 1000) {
                //지난번에 얻어온 값의 시간에서 1시간 이상 경과 했을 경우에만 값을 다시 얻어온다.
                Log.d(TAG, "checkNeedToUpdateDB Update Case - new data available. Need To Update");
                needToUpdate = true;
            } else {
                Log.d(TAG, "checkNeedToUpdateDB Update Case - Data is up to date. newDustInfo : (" + newDustInfo.get(0).getmDateTime() + ")" + " Dela : " + diff / 60000 + " min");
                needToUpdate = false;
                mMainHandler.removeMessages(DO_POOLING);
            }
        }
        return needToUpdate;
    }

    class SendMassgeHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            //Log.d(TAG, "handleMessage");
            super.handleMessage(msg);
            switch (msg.what) {
                case START_POOLING:
                    Log.d(TAG, "handleMessage START_POOLING");
                    sendEmptyMessage(DO_POOLING);
                    break;
                case STOP_POOLING:
                    Log.d(TAG, "handleMessage STOP_POOLING");
                    removeMessages(DO_POOLING);
                    break;
                case DO_POOLING:
                    Log.d(TAG, "handleMessage DO_POOLING");
                    new DustDBService.JsonLoadingTask().execute(mSidoCity);
                    break;
            }
        }
    }

    private class JsonLoadingTask extends AsyncTask<String[], Void, String[]> {
        @Override
        protected String[] doInBackground(String[]... strs) {
            //TODO 위치가 추가되면 여기서 for 문으로 추가하기
            return getDustInfoJson(strs[0]);
        }

        @Override
        protected void onPostExecute(String[] sidoCity) {
            ArrayList<Dust> dustArrayList = mDBHelperDust.getDustList(sidoCity[1]);
            Log.d(TAG, "Before Notification updated and callback dustArrayList.size():" + dustArrayList.size());
            if (dustArrayList.size() == 24) {
                notificationUtil.setContentTitle("미세먼지: " + dustArrayList.get(0).getmPM10() + "  초미세먼지: " + dustArrayList.get(0).getmPM25());
            } else {
                notificationUtil.setContentTitle("미세먼지: -  초미세먼지: -");
                dustArrayList = null;
            }

            notificationUtil.notify(0);
            Log.d(TAG, "Notification updated");

            if (mCallback != null) {
                Log.d(TAG, "Call OnCurrentDust");
                mCallback.OnCurrentDust(dustArrayList);
            }
        }
    }

    private String[] getDustInfoJson(String[] sidocity) {
        if (checkNeedToUpdate(mDBHelperDust.getDustList(sidocity[1]))) {
            try {
                Log.d(TAG, "getDustInfoJson");
                String jsonPage = null;
                JSONArray json = null;
                boolean validJSONArray = false;

                for(int i= 0; i < JSON_RETRY_COUNT; i++) {
                    jsonPage = getStringFromUrl(getDustUrl(sidocity));
                    if (jsonPage != null && jsonPage.length() != 0) {
                        json = new JSONArray(jsonPage);
                        if (json.length() == 24 || json.length() == 25) {
                            validJSONArray = true;
                            break;
                        }
                    }
                    Thread.sleep(500);
                }

                if (validJSONArray) {
                    ArrayList<Dust> newDustArrayList = new ArrayList<Dust>();

                    int jsonlength = json.length();

                    if (jsonlength == 25) {
                        jsonlength = jsonlength - 1;
                    }

                    for (int i = 0; i < jsonlength; i++) {
                        Dust dust = new Dust(
                                json.getJSONObject(i).getString("sidoName"),
                                json.getJSONObject(i).getString("cityName"),
                                json.getJSONObject(i).getString("dataTime"),
                                (float) json.getJSONObject(i).getDouble("coValue"),
                                (float) json.getJSONObject(i).getDouble("no2Value"),
                                (float) json.getJSONObject(i).getDouble("o3Value"),
                                json.getJSONObject(i).getInt("pm10Value"),
                                json.getJSONObject(i).getInt("pm25Value"),
                                (float) json.getJSONObject(i).getDouble("so2Value")
                        );
                        newDustArrayList.add(dust);
                    }

                    Log.d(TAG, "Get dustArrayList OK!. Try update db : " + newDustArrayList.get(0).getmDateTime() + " newDustArrayList.size() : " + newDustArrayList.size());
                    //TODO db 전체가 아니라 일부만 업데이트 되도록 수정 필요. 일단은 앞에꺼를 지우고 다시 넣는 방식으로 적용
                    if (checkNeedToUpdateDB(mDBHelperDust.getDustList(sidocity[1]), newDustArrayList)) {
                        Log.d(TAG, "DB updated");
                        mDBHelperDust.delete(sidocity[1]);
                        mDBHelperDust.insertDustList(newDustArrayList);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                // TODO: handle exception
            }
        }
        return sidocity;
    }

    private String getDustUrl(String[] sidocity) {
        StringBuffer sb = new StringBuffer();
        sb.append(SERVER_ADDRESS);

        if(!sidocity[0].equalsIgnoreCase("")) {
            sb.append("/getDust/sido/");
            sb.append(sidocity[0]);
            sb.append("/");
            sb.append(sidocity[1]);
        }
        return String.valueOf(sb);
    }

    private String getStringFromUrl(String pUrl) {
        Log.d(TAG, "getStringFromUrl pUrl:" + pUrl);
        BufferedReader bufreader = null;
        HttpURLConnection urlConnection = null;

        StringBuffer page = new StringBuffer();

        try {
            URL url = new URL(pUrl);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setConnectTimeout(15000);
            urlConnection.setReadTimeout(15000);
            InputStream contentStream = urlConnection.getInputStream();
            bufreader = new BufferedReader(new InputStreamReader(contentStream, "UTF-8"));
            String line = null;

            while ((line = bufreader.readLine()) != null) {
                page.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if(bufreader!=null) bufreader.close();
                if(urlConnection!=null) urlConnection.disconnect();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return page.toString();
    }
}
