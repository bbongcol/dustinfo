package techjun.com.dustinfo.utils;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.NotificationCompat;
import android.widget.RemoteViews;

import techjun.com.dustinfo.R;
import techjun.com.dustinfo.SplashActivity;

/**
 * Created by leebongjun on 2017. 7. 1..
 */

public class NotificationUtil {
    public final int REQUEST_CODE_DUSTINFO = 0;
    private static NotificationUtil sInstance = null;
    private Context mContext;
    private NotificationManager mNotificationManager = null;
    private android.support.v4.app.NotificationCompat.Builder mDustInfoNotifyBuilder = null;

    private NotificationUtil(Context context) {
        mContext = context;
        mNotificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        createDustInfoNotification();
    }

    public static NotificationUtil getInstance(Context context) {
        if (sInstance == null) {
            //Always pass in the Application Context
            sInstance = new NotificationUtil(context.getApplicationContext());
        }
        return sInstance;
    }

    private void createDustInfoNotification() {
        mDustInfoNotifyBuilder = new NotificationCompat.Builder(mContext)
                //.setContentTitle("미세먼지 : ")
                .setOngoing(true) // Cant cancel your notification (except notificationManager.cancel(); )
                //.setContent(customDustInfoRemoteView())  //Custom View
                //.setNumber(100)
                //.setDefaults(Notification.DEFAULT_ALL)  //알림 진동이 온다
                .setSmallIcon(R.drawable.ic_cloud_queue);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mDustInfoNotifyBuilder.setCategory(Notification.CATEGORY_STATUS)
                    //.setPriority(Notification.PRIORITY_HIGH)
                    .setVisibility(Notification.VISIBILITY_PUBLIC);
        }
        setContentIntent(REQUEST_CODE_DUSTINFO);
    }

    private RemoteViews customDustInfoRemoteView() {
        //커스텀 화면 만들기
        RemoteViews remoteViews = new RemoteViews(mContext.getPackageName(), R.layout.custom_notification);
        remoteViews.setImageViewResource(R.id.img, R.mipmap.ic_launcher);
        remoteViews.setTextViewText(R.id.title, "Title");
        remoteViews.setTextViewText(R.id.message, "message");
        return remoteViews;
    }

    private void setContentIntent (int requestCode) {
        //알람 누를때 앱 띄우기
        Intent resultIntent = new Intent(mContext, SplashActivity.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(mContext);
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(requestCode, PendingIntent.FLAG_UPDATE_CURRENT);
        mDustInfoNotifyBuilder.setContentIntent(resultPendingIntent);
    }

    public void setContentTitle(String title) {
        mDustInfoNotifyBuilder.setContentTitle(title).setWhen(System.currentTimeMillis());
    }

    public void notify (int requestCode) {
        if(requestCode == REQUEST_CODE_DUSTINFO) {
            mNotificationManager.notify(REQUEST_CODE_DUSTINFO, mDustInfoNotifyBuilder.build());
        }
    }
}
