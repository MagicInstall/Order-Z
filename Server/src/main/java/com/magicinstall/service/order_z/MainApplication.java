package com.magicinstall.service.order_z;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.magicinstall.library.ClientSocket;
import com.magicinstall.library.DataPacket;
import com.magicinstall.library.Define;
import com.magicinstall.library.Hash;
import com.magicinstall.library.TcpQueryActivity;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by wing on 16/5/25.
 */
public class MainApplication extends Application
        implements TcpQueryActivity.InteractionActivity{

    private static final String TAG = "MainApplication";

    /** 喺debuger 中确认需要Application 的进程名, 弱智安卓... */
    private static final String PROCESS_NAME = "com.magicinstall.phone.order_z";

    @Override
    public void onCreate() {
        super.onCreate();
        // 打印进程名
        Log.v(TAG, getCursorProcessName(this));

        // 判断需要Application 的进程
        if (!isForegroundProcess()) return;
        Log.v(TAG, "onCreate");


        startService(new Intent(this, DatabaseService.class));
        connectionLocalServer();
    }

    /** 弱智的安卓只能用弱智的方法去解决... */
    private boolean isForegroundProcess() {return getCursorProcessName(this).equals(PROCESS_NAME);}
    /**
     * WTF! 相当弱智的安卓...
     * <p>哩个方法用嚟取得进程名,
     * Application 类的所有事件都要先判断一下系边个进程先好继续做嘢...
     * @param context
     * @return
     */
    private String getCursorProcessName(Context context) {
        int pid = android.os.Process.myPid();
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningAppProcessInfo appProcess : activityManager.getRunningAppProcesses()) {
            if (appProcess.pid == pid) {
                return appProcess.processName;
            }
        }
        return "";
    }


    /************************************************************************
     *                              TCP Socket                              *
     ************************************************************************/
    private ClientSocket mClientSocket = null;

    /**
     * 取得TCP Socket 是否已经连接到服务端
     * @return true:已连接
     */
    public boolean isConnected(){return mClientSocket.isConnected();}

    /**
     * 向服务端发起TCP 连接
     */
    protected void connectionLocalServer(){
        mClientSocket = new ClientSocket("127.0.0.1", Define.TCP_PORT, null) {
            @Override
            public void onConnected() {
                // 先取得一次本地服务端的企业列表
                getCompanyList(MainActivity.ACTIVITY_ID);
            }

            @Override
            public DataPacket onReceivedCompletePacket(DataPacket packet, byte[] extend) {
                // 转发事件
                onGetPacket(packet, extend);
                return null;
            }
        };
    }

    /**
     * 收到报文事件
     * <p>喺哩个方法进行各种包的派发
     */
    protected void onGetPacket(DataPacket packet, byte[] extend) {
        Log.v(TAG, "收到服务端报文"/* + data.length + "字节"*/);

        int command = Hash.byte32ToInt(extend, Define.EXTEND_COMMAND_OFFSET, 1/*字节*/);

        // TODO: 特殊命令判断

        // 根据Activity 派发报文
        switch (Hash.byte32ToInt(extend, Define.EXTEND_ACTIVITY_OFFSET, 1/*字节*/)) {
            case MainActivity.ACTIVITY_ID:
                if (mMainActivity != null) {
                    switch (command) {
                        case Define.EXTEND_COMMAND_GET_COMPANY_LIST:
                            companyRefresh(packet, extend);
                            break;
                        case Define.EXTEND_COMMAND_NEW_COMPANY:
                            companyAdd(packet, extend);
                            break;
                    }
                }
                break;
            default:
                Log.w(TAG, "唔知之前系边个Activity 请求查询");
                break;
        }
    }



    /************************************************************************
     *                         InteractionActivity                          *
     ************************************************************************/
    @Override
    public void onActiveChanged(Activity activity, boolean isActive) {
        if (activity.getClass() ==  MainActivity.class) {
            if (isActive) mMainActivity = (MainActivity)activity;
            else          mMainActivity = null;
        }
    }

    /************************************************************************
     *                            MainActivity                              *
     ************************************************************************/
    MainActivity mMainActivity;
    /** 持有企业列表 */
    ArrayList<HashMap<String, Object>> mCompanyList = new ArrayList<>();

    /**
     * 解释企业刷新报文
     */
    protected void companyRefresh(DataPacket packet, byte extend[]) {
        ArrayList<HashMap<String, Object>> list = companyPacket2List(packet, extend);
        if (list == null) return;
        mCompanyList = list;
        if (mMainActivity != null) mMainActivity.onRefresh(list);
    }

    /**
     * 解释企业添加报文
     */
    protected void companyAdd(DataPacket packet, byte extend[]) {
        ArrayList<HashMap<String, Object>> list = companyPacket2List(packet, extend);
        if (list == null) return;

        if (mCompanyList == null) mCompanyList = list;
        else mCompanyList.addAll(list);

        if (mMainActivity != null) mMainActivity.onRefresh(list);
    }

    /**
     * 转换企业查询返回的报文
     */
    protected ArrayList<HashMap<String, Object>> companyPacket2List(DataPacket packet, byte extend[]) {
        ArrayList<HashMap<String, Object>> list = new ArrayList<>();
        HashMap<String, Object> column;
        if (packet.moveToFirstItem()) {
            do {
                column = new HashMap<>();
                column.put(
                        Define.DATABASE_COMPANY_ID,
                        Hash.byte32ToInt(packet.getCurrentItem().data));

                packet.moveNextItem();
                column.put(
                        Define.DATABASE_COMPANY_NAME,
                        new String(packet.getCurrentItem().data));

                packet.moveNextItem();
                if (packet.getCurrentItem().data != null) {
                    column.put(
                            Define.DATABASE_COMPANY_PASSWORD,
                            new String(packet.getCurrentItem().data));
                } else {
                    column.put(Define.DATABASE_COMPANY_PASSWORD, null);
                }

                list.add(column);
            } while (packet.moveNextItem() != null);
        }
        return list;
    }


    /**
     * 取得企业列表
     * @param activityID 传入调用哩个方法的Activity的{getID()}的值,
     *                   当查询返回的时候按照该ID,
     *                   调用相应的回调方法.
     */
    public void getCompanyList(final byte activityID) {
        Log.v(TAG, "请求查询企业列表");
        // 询问需要的字段
        final HashMap<String, String[]> columns =
                MainActivity.getColumnsName(
                        (byte) Define.EXTEND_COMMAND_GET_COMPANY_LIST,
                        new String[]{"CompanyList"});

        // 在新线程运行
        new Thread() {
            @Override
            public void run() {
            Thread.currentThread().setPriority(4); // 稍低的优先级
                // 转换成报文
                DataPacket packet = new DataPacket();
                for (String column : columns.get("CompanyList")){
                    packet.pushItem(column);
                }
                packet.make(new byte[] {
                                Define.EXTEND_COMMAND_GET_COMPANY_LIST,
                                activityID
                        });

                mClientSocket.send(packet);
            }
        }.start();
    }

}
