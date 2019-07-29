package com.tracy.treadsure;

import android.app.Activity;
import android.app.Application;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by angoo on 2018/2/19.
 */

public class MyApplication extends Application {

    private List<Activity> activityList = new LinkedList<Activity>();
    private static MyApplication instance;

    private MyApplication()
    {
    }
    //單例模式中獲取唯一的MyApplication實例
    public static MyApplication getInstance()
    {
        if(null == instance)
        {
            instance = new MyApplication();
        }
        return instance;
    }
    //添加Activity到容器中
    public void addActivity(Activity activity)
    {
        activityList.add(activity);
    }
    //遍歷所有Activity並finish
    public void exit()
    {
        for(Activity activity:activityList)
        {
            activity.finish();
        }
        System.exit(0);
    }
}
