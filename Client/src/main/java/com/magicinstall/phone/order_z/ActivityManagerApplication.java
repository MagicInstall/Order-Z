package com.magicinstall.phone.order_z;

import android.app.Application;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.util.LinkedList;
import java.util.List;

/**
 * Activity 管理类
 *
 * 整个程序内的所有Activity 必须是ActivityManagerApplication.ManagedActivity 的子类.
 * Created by wing on 16/4/25.
 */
public class ActivityManagerApplication extends Application{
    private static final String TAG = ActivityManagerApplication.class.toString();

    private List<ManagedActivity> activitysList = null;
//    private static MyApplication instance;

    public ActivityManagerApplication() {
        activitysList = new LinkedList<ManagedActivity>();
    }

//    /**
//     * 单例模式中获取唯一的MyApplication实例
//     *
//     * @return
//     */
//    public static MyApplication getInstance() {
//        if (null == instance) {
//            instance = new MyApplication();
//        }
//        return instance;
//
//    }

    /**
     * 添加Activity到容器中
     *
     */
    public void addActivity(ManagedActivity activity) {
//        if (activitysList != null && activitysList.size() > 0) {
            if(!activitysList.contains(activity)){
                activitysList.add(activity);
                Log.v(TAG, "add:" + activity.getClass().toString());
            }
//        }else{
//            activitysList.add(activity);
//        }

    }

    /**
     * 移除activity
     *
     */
    public void removeActivity(ManagedActivity activity) {
        if(activitysList.contains(activity)){
            activitysList.remove(activity);
            Log.v(TAG, "remove:" + activity.getClass().toString());
        }
    }

    /**
     * 遍历所有Activity并finish
     */
    public void exit() {
//        if (activitysList != null && activitysList.size() > 0) {
            for (ManagedActivity activity : activitysList) {
                activity.finish();
                Log.v(TAG, "remove:" + activity.getClass().toString());
            }
//        }
        System.exit(0);
    }

    /**
     * 受管理的Activity 类
     */
    public static class ManagedActivity extends AppCompatActivity {
        @Override
        protected void onCreate(@Nullable Bundle savedInstanceState) {
            // 插入到列表
            ActivityManagerApplication app = ((ActivityManagerApplication)getApplicationContext());
            app.addActivity(this);
            super.onCreate(savedInstanceState);
        }

        @Override
        protected void onDestroy() {
            // 移出列表
            ActivityManagerApplication app = ((ActivityManagerApplication)getApplicationContext());
            app.removeActivity(this);
            super.onDestroy();
        }

        /**
         * 完全退出App.
         */
        protected void exit() {
            ActivityManagerApplication app = ((ActivityManagerApplication)getApplicationContext());
            app.exit();
        }
    }
}
