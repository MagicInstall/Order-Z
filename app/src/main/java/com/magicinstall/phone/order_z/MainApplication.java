package com.magicinstall.phone.order_z;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.IBinder;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.util.Log;

/**
 * Created by wing on 16/4/25.
 */
public class MainApplication extends ActivityManagerApplication{
    private static final String TAG = "MainApplication";

    private static String PREFERENCE_LOGINED_KEY = "PREFERENCE_LOGINED_KEY";



    public String mUser;
    public String mPassword;

//    public MainApplication() {
//
//    }



    @Override
    public void onCreate() {
        super.onCreate();

//        Intent intent = new Intent(this, ServiceAidlInterface.class);
//        startService(intent);
        bindService(new Intent(this, DatabaseService.class), mServiceConnection, Context.BIND_AUTO_CREATE);

//        Log.i(TAG, new WifiUDP().getWifiIpAddress(this));

        // TODO: 本地验证
        // TODO: 连接主机
    }

    @Override
    public void onTerminate() {
//        stopService()
        super.onTerminate();
    }

    /**
     * 从SharedPreferences 读出是否已经登陆
     * TODO: 考虑会唔会同各种验证冲突
     * @return
     */
    public boolean isLogined() {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        return settings.getBoolean(PREFERENCE_LOGINED_KEY, false);
    }

    /**
     * 在SharedPreferences 里写入登陆状态
     * TODO: 考虑会唔会同各种验证冲突
     * @param logined
     */
    public void setLogined(boolean logined) {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        settings.edit()
                .putBoolean(PREFERENCE_LOGINED_KEY, logined)
                .commit();
    }

    public String getAppVerstionName(){
        String version = null;
        try {
            PackageManager manager = this.getPackageManager();
            PackageInfo info = manager.getPackageInfo(this.getPackageName(), 0);
            version = info.versionName;
        } catch (Exception e){
        }
        return version;
    }


    /************************************************************************
     *                           Service 与 AIDL                            *
     ************************************************************************/

    private DatabaseService mDatabaseService;
    private ServiceAidlInterface mServiceAIDL;

    /**
     * Service 的连接器
     * 主要用嚟得到服务类的AIDL 对象, 同埋 new 一个AIDL俾服务类,(两种AIDL 系唔同嘅)
     */
    private ServiceConnection mServiceConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.v(TAG, "onServiceConnected");

            // 得到Service 的AIDL
            mServiceAIDL = ServiceAidlInterface.Stub.asInterface(service);
            if (mServiceAIDL == null) {
                Log.e(TAG, "Unable to get AIDL!");
                return;
            }

            // 将自己的AIDL 送去Service
            try {
                mServiceAIDL.addAIDL(mApplicationAIDL);
                mServiceAIDL.print("new AIDL"); // 测试用
            } catch (RemoteException e) {
                e.printStackTrace();
            }

        }//(^((?!(dalvikvm|jdwp)).)*$)

        public void onServiceDisconnected(ComponentName name) {
            // 通知Service 移除AIDL
            try {
                mServiceAIDL.removeAIDL(mApplicationAIDL);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            mServiceAIDL = null;
            Log.v(TAG, "onServiceDisconnected");
        }
    };

    /**
     * AIDL 接口
     */
    private ApplicationAidlInterface mApplicationAIDL = new ApplicationAidlInterface.Stub() {

        @Override
        public void print(String msg) throws RemoteException {
            Log.d(TAG, msg);
        }
    };
}
