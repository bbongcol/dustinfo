package techjun.com.dustinfo.service;


import android.os.Handler;
import android.util.Log;

import java.util.Date;

/**
 * Created by leebongjun on 2017. 7. 7..
 */

public class ServiceThread extends Thread{
    Handler handler;
    boolean isRun = true;
    boolean TimeAdjustment = false;
    private final String TAG = "ServiceThread";

    public ServiceThread(Handler handler){
        this.handler = handler;
        Log.d(TAG, "ServiceThread");
    }

    public void stopForever(){
        synchronized (this) {
            this.isRun = false;
        }
    }

    public void run(){
        while(isRun){
            try{
                if(TimeAdjustment) {
                    long now = System.currentTimeMillis();
                    Date curDateTime = new Date(now);
                    //Log.d(TAG, "curDateTime.getMinutes() : "+curDateTime.getMinutes());
                    if(curDateTime.getMinutes() >= 30) {
                        Log.d(TAG, "Sleep : "+(90 - curDateTime.getMinutes()) + " min");
                        Thread.sleep((90 - curDateTime.getMinutes()) * 60 * 1000);
                    } else {
                        Log.d(TAG, "Sleep : "+ (30 - curDateTime.getMinutes()) + " min");
                        Thread.sleep((30 - curDateTime.getMinutes()) * 60 * 1000 );
                    }
                } else {
                    Thread.sleep(1 * 60 * 1000); //1분에 한번식 깨어난다
                }
            } catch (Exception e) {}
            handler.sendEmptyMessage(DustDBService.START_POOLING);
        }
    }
}
