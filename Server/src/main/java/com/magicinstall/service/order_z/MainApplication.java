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
        // 帮助取得PROCESS_NAME 的值
        Log.v(TAG, "进程名:" + getCursorProcessName(this));

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
                getCompanyList((byte) 0);
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
            case UserListActivity.ACTIVITY_ID:
                if (mUserListActivity != null) {
                    switch (command) {
                        case Define.EXTEND_COMMAND_GET_USER_LIST:
                            usersRefresh(packet, extend);
                            break;
                        case Define.EXTEND_COMMAND_NEW_User:
                            usersAdd(packet, extend);
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
    /**
     * 哩度更新各个InteractionActivity 子类的引用
     */
    @Override
    public void onActiveChanged(Activity activity, boolean isActive) {
        // MainActivity
        if (activity.getClass() ==  MainActivity.class) {
            if (isActive) mMainActivity = (MainActivity)activity;
            else          mMainActivity = null;
        }
        // UserListActivity
        if (activity.getClass() ==  UserListActivity.class) {
            if (isActive) mUserListActivity = (UserListActivity)activity;
            else          mUserListActivity = null;
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

        if (mMainActivity != null) mMainActivity.onAdd(list);
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
        // App 连接服务端嘅时候已经取得过企业列表, 直接传递到请求的Activity就是
        if (activityID != 0/*Application 自己*/){
            switch (activityID) {
                case MainActivity.ACTIVITY_ID:
                    mMainActivity.onRefresh(mCompanyList);
                    break;
            }
            return;
        }

        // 一般只会是由Application 调用先会去到哩度
        Log.v(TAG, "请求查询企业列表, Activity ID:" + activityID);
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
                                MainActivity.ACTIVITY_ID
                        });

                mClientSocket.send(packet);
            }
        }.start();
    }

    /************************************************************************
     *                           UserListActivity                           *
     ************************************************************************/
    UserListActivity mUserListActivity;
    /** 持有职员列表
     *  再套一层HashMap 是区分该列表是在哪个企业 */
    HashMap<Integer/*企业ID*/, ArrayList<HashMap<String, Object>>> mUserList = new HashMap<>();

    /**
     * 解释职员刷新报文
     */
    protected void usersRefresh(DataPacket packet, byte extend[]) {
        ArrayList<HashMap<String, Object>> list = userPacket2List(packet, extend);
        if (list == null) return;

        // 没有一个职员返回的话, 无法取得企业ID
        if (list.size() > 0) {
            // 只取第一个职员的所属企业ID
            Integer company_id = (Integer) list.get(0).get(Define.DATABASE_USER_IN_COMPANY);

            // 判断是否已经有该企业的职员列表
            if (mUserList.containsKey(company_id)) {
                // 移除已有的职员列表
                mUserList.remove(company_id);
            }
            mUserList.put(company_id, list);
        }
        if (mUserListActivity != null) mUserListActivity.onRefresh(list);
    }

    /**
     * 解释职员添加报文
     */
    protected void usersAdd(DataPacket packet, byte extend[]) {
        ArrayList<HashMap<String, Object>> list = userPacket2List(packet, extend);
        if (list == null) return;

        // 没有一个职员返回的话, 无法取得企业ID
        if (list.size() > 0) {
            // 只取第一个职员的所属企业ID
            Integer company_id = (Integer) list.get(0).get(Define.DATABASE_USER_IN_COMPANY);

            ArrayList<HashMap<String, Object>> userlist_in_company =
                    mUserList.get(company_id);

            userlist_in_company.addAll(list);
        }

        if (mUserListActivity != null) mUserListActivity.onAdd(list);
    }

    /**
     * 转换职员查询返回的报文
     */
    protected ArrayList<HashMap<String, Object>> userPacket2List(DataPacket packet, byte extend[]) {
        ArrayList<HashMap<String, Object>> list = new ArrayList<>();
        HashMap<String, Object> column;
        int company_id;
        if (packet.moveToFirstItem()) {
            company_id = Hash.byte32ToInt(packet.getCurrentItem().data); // 测试用
            while (packet.moveNextItem() != null) {
                column = new HashMap<>();
                column.put(
                        Define.DATABASE_USER_ID,
                        Hash.byte32ToInt(packet.getCurrentItem().data));

                packet.moveNextItem();
                company_id = Hash.byte32ToInt(packet.getCurrentItem().data); // 测试用
                column.put(
                        Define.DATABASE_USER_IN_COMPANY,
                        Hash.byte32ToInt(packet.getCurrentItem().data));

                packet.moveNextItem();
                column.put(
                        Define.DATABASE_USER_NAME,
                        new String(packet.getCurrentItem().data));

                packet.moveNextItem();
                if (packet.getCurrentItem().data != null) {
                    column.put(
                            Define.DATABASE_USER_PASSWORD,
                            new String(packet.getCurrentItem().data));
                } else {
                    column.put(Define.DATABASE_USER_PASSWORD, null);
                }

                list.add(column);
            }
        }
        return list;
    }


    /**
     * 取得职员列表
     * <p>由于要顾及后面解释报文的复杂程度,
     * 哩个方法一次只查询一个企业的职员列表.
     * @param activityID 传入调用哩个方法的Activity的{ACTIVITY_ID}的值,
     *                   当查询返回的时候按照该ID,
     *                   调用相应的回调方法.
     */
    public void getUserList(final byte activityID, final int companyID) {
        if (companyID < 1) return;

        // 判断是否已经取得过该企业的职员列表
        if (mUserList.containsKey(companyID)) {
            if (activityID != 0/* 排除Application 自己*/) {
                // 直接返回职员列表
                switch (activityID) {
                    case UserListActivity.ACTIVITY_ID:
                        mUserListActivity.onRefresh(mUserList.get(companyID));
                        break;
                }
                return;
            }
        }

        // 哩度开始从服务端查询该企业的职员列表
        Log.v(TAG, "请求查询职员列表, " +
                "Activity ID:" + activityID +
                "Company ID:" + companyID);

        // 在新线程运行
        new Thread() {
            @Override
            public void run() {
                Thread.currentThread().setPriority(4); // 稍低的优先级

                // 询问需要的字段
                final HashMap<String, String[]> columns =
                        UserListActivity.getColumnsName(
                            (byte) Define.EXTEND_COMMAND_GET_USER_LIST,
                            new String[]{"UserList"});

                // 转换成报文
                DataPacket packet = new DataPacket();
                // 第一个item 是企业ID
                packet.pushItem(companyID);
                // 接着是查询字段
                for (String column : columns.get("UserList")){
                    packet.pushItem(column);
                }

                packet.make(new byte[] {
                        Define.EXTEND_COMMAND_GET_USER_LIST,
                        UserListActivity.ACTIVITY_ID
                });

                mClientSocket.send(packet);
            }
        }.start();
    }
}
