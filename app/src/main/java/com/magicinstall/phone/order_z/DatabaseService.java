package com.magicinstall.phone.order_z;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.util.Log;

public class DatabaseService extends Service {
    private static final String TAG = "DatabaseService";

    private static final int UDP_BROADCAST_PORT = 13130;

    private MainDatabase mDatabase;
    private LanAutoSearch mLanSearcher;

    public DatabaseService() {
    }

    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate");
//        super.onCreate();
        mDatabase = new MainDatabase(this);
        mDatabase.Backup(null);

//        mLanSearcher = new LanAutoSearch();
//        mLanSearcher.startServer(this, UDP_BROADCAST_PORT);
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind");
        return new ServiceAIDL();

//        mServiceAIDL = new ServiceAIDL();
//        try {
//            mServiceAIDL.print("new AIDL");
//        } catch (RemoteException e) {
//            e.printStackTrace();
//        }
//        return mServiceAIDL;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.d(TAG, "onUnbind");
        return false;
    }

    @Override
    public void onRebind(Intent intent) {
        Log.d(TAG, "onRebind");
//        super.onRebind(intent);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY; // 被Kill 之后自动重启
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy");
        super.onDestroy();
    }


    /************************************************************************
     *                                AIDL                                  *
     ************************************************************************/

//    private ServiceAIDL mServiceAIDL;

    /**
     * 持有应用端的AIDL
     */
    private final RemoteCallbackList <ApplicationAidlInterface>mCallbacks = new RemoteCallbackList<ApplicationAidlInterface>();

    /**
     *  AIDL 接口
     */
    private class ServiceAIDL extends ServiceAidlInterface.Stub {
//        @Override
//        public void basicTypes(int anInt, long aLong, boolean aBoolean, float aFloat, double aDouble, String aString) throws RemoteException {
//
//        }

        /**
         * 将App 的AIDL 传俾Service, Service 可以通过佢回调App
         *
         * @param aidl
         */
        @Override
        public void addAIDL(ApplicationAidlInterface aidl) throws RemoteException {
            mCallbacks.register(aidl);
        }

        /**
         * App 断开Service 的绑定时, 移除AIDL
         *
         * @param aidl
         */
        @Override
        public void removeAIDL(ApplicationAidlInterface aidl) throws RemoteException {
            mCallbacks.unregister(aidl);
        }

        @Override
        public void print(String msg) throws RemoteException {
            Log.d(TAG, msg);
        }
    }
}
