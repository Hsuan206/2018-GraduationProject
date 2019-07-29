package com.tracy.treadsure;

import android.app.Application;
import android.content.Context;

/**
 * Created by Admin on 2018/4/6.
 */

public class GetTimeAgo extends Application{

    private static final int SECOND_MILLIS = 1000;
    private static final int MINUTE_MILLIS = 60 * SECOND_MILLIS;
    private static final int HOUR_MILLIS = 60 * MINUTE_MILLIS;
    private static final int DAY_MILLIS = 24 * HOUR_MILLIS;

    public static String getTimeAgo(long time, Context ctx) {
        //上一次上線時間  L為long
        if (time < 1000000000000L) {
            // 如果time 給秒，轉換為毫秒
            time *= 1000;
        }

        //返回當前時間(毫秒)
        long now = System.currentTimeMillis();
        if (time > now || time <= 0) {
            return null;
        }

        // TODO: localize
        //現在(毫秒)-上次上線時間(毫秒)
        final long diff = now - time;
        if (diff < MINUTE_MILLIS) {
            return "現在";
        } else if (diff < 2 * MINUTE_MILLIS) {
            return "1 分鐘以前";
        } else if (diff < 50 * MINUTE_MILLIS) {
            return diff / MINUTE_MILLIS + " 分鐘以前";
        } else if (diff < 90 * MINUTE_MILLIS) {
            return "1 小時以前";
        } else if (diff < 24 * HOUR_MILLIS) {
            return diff / HOUR_MILLIS + " 小時以前";
        } else if (diff < 48 * HOUR_MILLIS) {
            return "昨天";
        } else {
            return diff / DAY_MILLIS + " 天以前";
        }
    }


}
