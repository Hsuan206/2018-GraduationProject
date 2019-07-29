package com.tracy.treadsure;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

import com.google.firebase.messaging.RemoteMessage;

/**
 * Created by Admin on 2018/4/1.
 */

public class FirebaseMessagingService extends com.google.firebase.messaging.FirebaseMessagingService {




    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);


        /*  取至index.js中
            const payload = {
                notification: {
                    title : '好友邀請',
                    body: `${userName}發給你一則好友邀請` ,
                    icon: "default",
                    click_action:"com.tracy.a20180324_TARGET_NOTIFICATION"
                },
                data:{
                      from_user_id:from_user_id
                }
            };
        */

        String from_user_id = remoteMessage.getData().get("from_user_id");
        //通知取得內容
        String notification_title = remoteMessage.getNotification().getTitle();
        String notification_message = remoteMessage.getNotification().getBody();
        String click_action = remoteMessage.getNotification().getClickAction();


        //通知設定內容
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.user_person_before)
                        .setContentTitle(notification_title)
                        .setContentText(notification_message)
                        .setAutoCancel(true);


        Intent resultIntent = new Intent(click_action);
        resultIntent.putExtra("user_id",from_user_id);

        //在 Notification 內，動作本身是由 PendingIntent 完成定義，其中包含的 Intent 會啟動應用程式中的 Activity
        PendingIntent resultPendingIntent =
                PendingIntent.getActivity(
                        this,
                        0,
                        resultIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        //使用者按一下通知中的通知文字時，呼叫 setContentIntent()來啟動 Activity
        mBuilder.setContentIntent(resultPendingIntent);

        //設定notification ID
        int mNotificationId = (int) System.currentTimeMillis();
        //取得NotificationManager Service
        NotificationManager mNotifyMgr = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        //建立通知
        mNotifyMgr.notify(mNotificationId,mBuilder.build());

    }
}
