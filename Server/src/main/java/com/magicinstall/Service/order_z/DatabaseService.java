package com.magicinstall.service.order_z;

import android.app.Service;
import android.content.Intent;
import android.database.Cursor;
import android.os.IBinder;
import android.util.Log;

import com.magicinstall.library.DataPacket;
import com.magicinstall.library.Define;
import com.magicinstall.library.Hash;
import com.magicinstall.library.LanAutoSearch;
import com.magicinstall.library.SocketManager;

import java.net.DatagramPacket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

public class DatabaseService extends Service {
    private static final String TAG = "DatabaseService";

    private static final String PREFERENCE_I_AM_SERVER_KEY = "PREFERENCE_I_AM_SERVER_KEY";

//    public static final int UDP_BROADCAST_PORT = 13130;
//    public static final String UDP_BROADCAST_GROUP = "224.0.0.1";


    private DatabaseInServer mDatabase;

    private LanAutoSearch mLanSearcher;
    private SocketManager mSocketManager;

    public DatabaseService() {
    }

    @Override
    public void onCreate() {
//        super.onCreate();
        Log.d(TAG, "onCreate");

        // TODO: 测试用, 专登打开网络服务
//        PreferenceManager.getDefaultSharedPreferences(getBaseContext())
//                .edit()
//                .putBoolean(PREFERENCE_I_AM_SERVER_KEY, true)
//                .commit();

        // 判断自己是不是Server
//        if (PreferenceManager.getDefaultSharedPreferences(getBaseContext())
//                .getBoolean(PREFERENCE_I_AM_SERVER_KEY, false)) {
//
//            // 自动切换至服务端
//            if (!startAutoReplyIP()) {
//                Log.e(TAG, "打开网络服务出错!");
//            }
//        }

        // 打开数据库
        mDatabase = new DatabaseInServer(this);
        mDatabase.Backup(null);

        // 监听客户端连接
        mSocketManager = new SocketManager() {
            /**
             * TCP 收到报文事件
             * <p>哩个事件唔喺主线程执行
             *
             * @param packet 已经解好包的packet,
             *               从packet 的item 相关方法读取内容.
             * @param extend packet 的扩展数据,
             *               从哩个数据解释命令等.
             * @return 返回一个包含回复内容的报文.
             */
            @Override
            public DataPacket onReceivedPacket(DataPacket packet, byte[] extend) {
                // 转发事件
                return onGotClientPacketAndReply(packet, extend);
            }
        };
        mSocketManager.startListen(Define.TCP_PORT);

        // 打开自动搜索应答
//        try {
//            mLanSearcher = new LanAutoSearch(getBaseContext(), Define.UDP_BROADCAST_GROUP);
//        } catch (UnknownHostException e) {
//            e.printStackTrace();
//        }
//        mLanSearcher.startServer(Define.UDP_BROADCAST_PORT, Define.UDP_UNICAST_PORT);
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind");
//        return new ServiceAIDL();

//        mServiceAIDL = new ServiceAIDL();
//        try {
//            mServiceAIDL.print("new AIDL");
//        } catch (RemoteException e) {
//            e.printStackTrace();
//        }
//        return mServiceAIDL;

        return null;
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
        Log.d(TAG, "onStartCommand");
        // TODO: 判断自己是否持有数据库


        // TODO: 测试用, 专登打开网络服务
//        PreferenceManager.getDefaultSharedPreferences(getBaseContext())
//                .edit()
//                .putBoolean(PREFERENCE_I_AM_SERVER_KEY, true)
//                .commit();

        // 判断自己是不是Server
//        if (PreferenceManager.getDefaultSharedPreferences(getBaseContext())
//                .getBoolean(PREFERENCE_I_AM_SERVER_KEY, false)) {
//
//            // 自动切换至服务端
//            if (!startAutoReplyIP()) {
//                Log.e(TAG, "打开网络服务出错!");
//            }
//        }

        return START_STICKY; // 被Kill 之后自动重启
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy");

        stopAtuoReplyIP(); // 放第一


        super.onDestroy();
    }


    /************************************************************************
     *                                AIDL                                  *
     ************************************************************************/

////    private ServiceAIDL mServiceAIDL;
//
//    /**
//     * 持有应用端的AIDL
//     */
//    private final RemoteCallbackList <ApplicationAidlInterface>mCallbacks = new RemoteCallbackList<ApplicationAidlInterface>();
//
//    /**
//     *  AIDL 接口
//     */
//    private class ServiceAIDL extends ServiceAidlInterface.Stub {
////        @Override
////        public void basicTypes(int anInt, long aLong, boolean aBoolean, float aFloat, double aDouble, String aString) throws RemoteException {
////
////        }
//
//        /**
//         * 将App 的AIDL 传俾Service, Service 可以通过佢回调App
//         *
//         * @param aidl
//         */
//        @Override
//        public void addAIDL(ApplicationAidlInterface aidl) throws RemoteException {
//            mCallbacks.register(aidl);
//        }
//
//        /**
//         * App 断开Service 的绑定时, 移除AIDL
//         *
//         * @param aidl
//         */
//        @Override
//        public void removeAIDL(ApplicationAidlInterface aidl) throws RemoteException {
//            mCallbacks.unregister(aidl);
//        }
//
//        @Override
//        public void print(String msg) throws RemoteException {
//            Log.d(TAG, msg);
//        }
//
//        /**
//         * 转发SQLiteDatabase类 的rawQuery 方法
//         *
//         * @param sql
//         * @param selectionArgs
//         * @return 网络报文格式的包
//         */
//        @Override
//        public byte[] rawQuery(String sql, String[] selectionArgs) throws RemoteException {
//            return new byte[0];
//        }
//    }

    /************************************************************************
     *                               网络服务                                *
     ************************************************************************/

    /**
     * 打开UDP 自动派发本机内网IP
     * @return
     */
    private boolean startAutoReplyIP() {
        if (mLanSearcher != null) return true;

        try {
            mLanSearcher = new LanAutoSearch(getBaseContext(), Define.UDP_BROADCAST_GROUP) {

                /**
                 * 服务端准备回复的事件
                 * <p>哩个事件唔喺主线程执行
                 * <p>为防止被攻击, 应该验证一下客户端发嚟的内容.
                 * <p>对客户端发来的数据解密应该放在哩个事件中进行.
                 * @param clientPacket 从客户端收到的报文,
                 *                     可以从中取得客户端发过来的探测包内容,
                 *                     然后根据内容作出回复.
                 * @param lanLocalIp 服务器的内网IP
                 * @return 返回一段数据用于回复客户端的数据,
                 *         哩个返回数据必须将本机IP 包含其中,
                 *         客户端将根据哩个IP,
                 *         以TCP 重新发起连接.
                 */
                @Override
                public byte[] willReplyClientEvent(DatagramPacket clientPacket, String lanLocalIp) {
                    return super.willReplyClientEvent(clientPacket, lanLocalIp);
                }

            };
        } catch (UnknownHostException e) {
            Log.e(TAG, "哩个IP 唔可以用于广播组");
            return false;
        }

        return mLanSearcher.startServer(Define.UDP_BROADCAST_PORT, Define.UDP_UNICAST_PORT);
    }

    /**
     * 关闭UDP 内网Ip 派发功能
     */
    private void stopAtuoReplyIP(){
        if (mLanSearcher == null) return;

        mLanSearcher.stop();
        mLanSearcher = null;
    }

    /**
     * 收到客户端的TCP 报文并回复
     * @see {SocketManager.onReceivedPacket()}
     */
    private DataPacket onGotClientPacketAndReply(DataPacket packet, byte[] extend) {

//        DataPacket reply_packet = null;
        // 解释命令
        switch (Hash.byte32ToInt(extend, Define.EXTEND_COMMAND_OFFSET, 1)) {
            case Define.EXTEND_COMMAND_GET_COMPANY_LIST:
                Log.v(TAG, "命令:取得企业列表");
                return replyCompanyList(packet, extend);
            case Define.EXTEND_COMMAND_GET_USER_LIST:
                Log.v(TAG, "命令:取得职员列表");
                return replyUserList(packet, extend);

            default:
                Log.w(TAG, "未知命令" + Hash.bytes2Hex(extend));
                return null;
        }

//        return reply_packet;
    }

    /**
     * 根据传入的{columns} 参数的顺序, 将查询的结果逐一压入分包对象.
     * @param table 表名
     * @param columns 列名数组
     * @param where 条件子句
     * @param whereArgs 条件语句的参数数组
     * @param groupBy 分组
     * @param having 分组条件
     * @param orderBy 排序类
     * @param limit 分页查询的限制
     * @param packet 传入分包的指针
     * @return
     */
    private int queryAndPushItem(String table,
                                        String[] columns,
                                        String where,
                                        String[] whereArgs,
                                        String groupBy,
                                        String having,
                                        String orderBy,
                                        String limit,
                                        DataPacket packet) {

        Cursor cursor = mDatabase.query(table, columns, where, whereArgs, groupBy, having, orderBy, limit);

        if (cursor.moveToFirst()) {
            for (String column : columns) {
                // 判断字段类型
                switch (cursor.getType(cursor.getColumnIndex(column))) {
                    case Cursor.FIELD_TYPE_BLOB:
                        packet.pushItem(
                                cursor.getBlob(
                                        cursor.getColumnIndex(column)));
                        break;

                    case Cursor.FIELD_TYPE_FLOAT:
                        // TODO: DataPacket 类的pushItem 方法加入对浮点数的支持
                        packet.pushItem((byte[]) null); // 暂时用空项代替
                        break;

                    case Cursor.FIELD_TYPE_INTEGER:
                        packet.pushItem(
                                cursor.getInt(
                                        cursor.getColumnIndex(column)));
                        break;

                    case Cursor.FIELD_TYPE_NULL:
                        packet.pushItem((byte[]) null);
                        break;

                    case Cursor.FIELD_TYPE_STRING:
                        packet.pushItem(
                                cursor.getString(
                                        cursor.getColumnIndex(column)));
                        break;
                }
            }

        }
        else Log.i(TAG, "该查询没有一个列返回");

        return cursor.getCount();
    }

    /**
     * 回复企业列表查询
     * @param packet
     * @param extend
     * @return
     */
    private DataPacket replyCompanyList(DataPacket packet, byte[] extend) {
        List<String> columns = new ArrayList<>();
        packet.moveToFirstItem();
        while (packet.getCurrentItem() != null) {
            columns.add(new String(packet.getCurrentItem().data));
            packet.moveNextItem();
        }

        String ss[] = columns.toArray(new String[columns.size()]); // 测试

//        Cursor cursor = mDatabase.query(
//                "Company",
//                columns.toArray(new String[columns.size()]),
//                null, null, null, null,
//                Define.DATABASE_COMPANY_ID/*Order by*/,
//                null);
//
        DataPacket reply_packet = new DataPacket();
//        if (cursor.moveToFirst()) {
//            for (String column : columns) {
//                // 判断字段类型
//                switch (cursor.getType(cursor.getColumnIndex(column))) {
//                    case Cursor.FIELD_TYPE_BLOB:
//                        reply_packet.pushItem(
//                                cursor.getBlob(
//                                        cursor.getColumnIndex(column)));
//                        break;
//
//                    case Cursor.FIELD_TYPE_FLOAT:
//                        // TODO: DataPacket 类的pushItem 方法加入对浮点数的支持
//                        break;
//
//                    case Cursor.FIELD_TYPE_INTEGER:
//                        reply_packet.pushItem(
//                                cursor.getInt(
//                                        cursor.getColumnIndex(column)));
//                        break;
//
//                    case Cursor.FIELD_TYPE_NULL:
//                        reply_packet.pushItem((byte[]) null);
//                        break;
//
//                    case Cursor.FIELD_TYPE_STRING:
//                        reply_packet.pushItem(
//                                cursor.getString(
//                                        cursor.getColumnIndex(column)));
//                        break;
//                }
//            }
////            do {
////                reply_packet.pushItem(
////                        cursor.getInt(
////                                cursor.getColumnIndex(Define.DATABASE_COMPANY_ID)));
////                reply_packet.pushItem(
////                        cursor.getString(
////                                cursor.getColumnIndex(Define.DATABASE_COMPANY_NAME)));
////                reply_packet.pushItem(
////                        cursor.getString(
////                                cursor.getColumnIndex(Define.DATABASE_COMPANY_PASSWORD)));
////
////            } while (cursor.moveToNext());
//
//        }
//        else Log.i(TAG, "企业列表查询没有一个企业返回");

        // 更新扩展数据
        int count = queryAndPushItem(
                "Company",
                columns.toArray(new String[columns.size()]),
                Define.DATABASE_COMPANY_DELETED + "=0",
                null, null, null,
                Define.DATABASE_COMPANY_ID/*Order by*/,
                null,
                reply_packet);

        byte reply_extend[] = new byte[extend.length + 4/*企业数*/];
        System.arraycopy(extend, 0, reply_extend, 0, extend.length); // 复制原扩展数据到开头
//        byte company_count[] = Hash.int2byte32(cursor.getCount());
        System.arraycopy(
                Hash.int2byte32(count), 0,
                reply_extend, extend.length,
                4); // 复制企业数

        reply_packet.make(reply_extend, null/*TODO: RSA*/);
        return reply_packet;
    }


    /**
     * 回复企业列表查询
     * @param packet
     * @param extend
     * @return
     */
    private DataPacket replyUserList(DataPacket packet, byte[] extend) {
        List<String> columns = new ArrayList<>();
        packet.moveToFirstItem();
        // 第一个item是企业ID
        int company_id = Hash.byte32ToInt(packet.getCurrentItem().data);

        // 后面是要查询的字段
        while (packet.moveNextItem() != null) {
            columns.add(new String(packet.getCurrentItem().data));
//            packet.moveNextItem();
        }


        DataPacket reply_packet = new DataPacket();
        // 第一个item 是企业ID
        reply_packet.pushItem(company_id);

        int count = queryAndPushItem(
                "Users",
                columns.toArray(new String[columns.size()]),
                (Define.DATABASE_USER_IN_COMPANY + "=" + company_id + " AND " +
                        Define.DATABASE_USER_ID + ">1 AND "/*排除admin 账号*/ +
                        Define.DATABASE_COMPANY_DELETED + "=0")/*Where*/,
                null, null, null,
                null/*Order by Define.DATABASE_COMPANY_ID*/,
                null,
                reply_packet);

        byte reply_extend[] = new byte[extend.length + 4/*职员数*/];
        System.arraycopy(extend, 0, reply_extend, 0, extend.length); // 复制原扩展数据到开头
//        byte company_count[] = Hash.int2byte32(cursor.getCount());
        System.arraycopy(
                Hash.int2byte32(count), 0,
                reply_extend, extend.length,
                4); // 填入职员数

        reply_packet.make(reply_extend, null/*TODO: RSA*/);
        return reply_packet;
    }

}
