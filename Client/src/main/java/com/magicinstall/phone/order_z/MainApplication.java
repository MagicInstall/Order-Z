package com.magicinstall.phone.order_z;

import android.app.Activity;
import android.app.Application;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.util.Log;

import com.magicinstall.library.ClientSocket;
import com.magicinstall.library.Company;
import com.magicinstall.library.DataPacket;
import com.magicinstall.library.Define;
import com.magicinstall.library.Hash;
import com.magicinstall.library.LanAutoSearch;
import com.magicinstall.library.TcpQueryActivity;
import com.magicinstall.library.User;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashSet;

/**
 * Created by wing on 16/4/25.
 */
public class MainApplication extends Application
        implements TcpQueryActivity.InteractionActivity{
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
        Log.v(TAG, "onCreate");

//        Intent intent = new Intent(this, ServiceAidlInterface.class);
//        startService(intent);
//        bindService(new Intent(this, DatabaseService.class), mServiceConnection, Context.BIND_AUTO_CREATE);

//        Log.i(TAG, new LanAutoSearch().getWifiIpAddress(this));



//        // 私匙加的密
//        String private_encode = "43397afc1aff805b868ee37875581d6fe18d7902fddfc56e2d157094aa9e531ed22bee17d19980cebf8c59e079f6da56a4802d02eb5d405577ba0d9c7f5cb2980a03bcf5fb2666fddae5ed148b130985d6a7b62cf1efcd37ee6279e4c75fd92fb83b4511b3b7b66d6a71076aee3f944a7a47b6d8f76a103a45752bff927a31c8421f2d14894445f37f9762be4d6b675cd62aab19392bb7e3773fab64e7f2b666139dfd5d399233a7a1b0dd287d9d77f0b558c78f257e362dbaf9926449e5c7ba970214facb4b4382267868a39d0258d20a3fd1df7bdbf20899051708912a2d91d4e0fe7bae5447e74f07e816192e3a1dc41345959afd280b6d75bb6074ea4564984d67f460cec8e339bf2deac6e59553550ba34e97c6b24abb8feccd4a42c2a20d10e66056c7b86849522601529240019825fbcd8dd370560d8927c6a675c51f3ce2c843a8b0c2782f10b8a73d0199914ee60728074f905e3a26f59676c6a936920613e53d5fd2adcfa3896f2645fdc5781b5d7b8a521d5694a3daa032f5b3c9114fef95023951fcddf774d91d291eddae0276e6bbb21d4785e6d076b5ae42122c69d9b8fb2244d303089c0e89a5a0adbef08a43e0049a7340c78b26c88166587dfb374ab5759a463134f892e27de2d5097a2fb34c8d670cc1c0b574cc8d1799149e8c370a556d89738f7c858cd1d0e8deca49853190c069684d50f5cee4764cd37d9f424487a1f5d95cafb23c4781fdec06adba9a47517e098c91b2e0e48b591baad13a23ddea82a4acddf3f1864ecfcf012defaf887b12fcd3e4059863dfa1204f84f8082373461dd126494d29033a50d1b8e923d59af5132f840107d585a2bdea627cf81ca01da7edd42f0fa0fd1533503481632895d7808f46bd62dacfdeb154eb9b58cc8514d45bcf8953b338fe99aba50fbc5c2adfd385ea454a3377039c508674eb7bb265ff4835e9eb9d221a0d3762d9b433b49ab3e17d384b657972d23143b998ef0fd1d8ed59d94afe43cc02d5b0cb4e79c19a52c30ee4306a5db3255392a87196aad59c6732de67ef5d4c4ae18d78be2b79573fce8ae2c512f2b84ecbe0bc74339a734ce4499646372b8d1fb2a478d38ede3deae67ac529fff68fafe64215f07355f84836e48c47a96b03f38515e8f94cda3eee292e77b6c5ccf59bb08148ddcee8bacef5cd1c03de716ecddf03b5f1b4a890b51f76f3d3ae4041d1456e80fccf9cf85b4f945318bf09e064f7b72d44d78bd8ae6363a2633418e1";
//
//        byte[] data = Hash.hexString2Bytes(private_encode);
//
//        RSA rsa = new RSA();
//        try {
//            rsa.loadPublicKey(getBaseContext(), R.raw.android_public_key);
//        } catch (Exception e) {
//            Log.d(TAG, "加载公钥时产生的异常"); return;
//        }
//
//        byte dec_data[] = rsa.decryptData(data);
//        Log.d("解出文件MD5", Hash.getMD5(dec_data));


//        ByteArrayOutputStream out = new ByteArrayOutputStream(1024);
//        try {
//            InputStream stream =
//                    getBaseContext().getResources().openRawResource(R.raw.test_file);
//            byte[] b = new byte[1024];
//            int n;
//            while ((n = stream.read(b)) != -1) {
//                out.write(b, 0, n);
//            }
//            stream.close();
//            out.close();
//        } catch (IOException e) {
//            Log.e(TAG, "文件读取异常"); return;
//        }
//        Log.v("原文件字节:", Hash.bytes2Hex(out.toByteArray()));
//        Log.d("原文件MD5", Hash.getMD5(out.toByteArray()));


//        RSA rsa = new RSA();
//        try {
//            rsa.loadPublicKey(getBaseContext(), R.raw.android_public_key);
//        } catch (Exception e) {
//            Log.d(TAG, "加载公钥时产生的异常"); return;
//        }
//
//        byte enc_data[] = rsa.encrypt(out.toByteArray());
////        Log.d("公匙加密", Hash.bytes2Hex(dec_data));
//
//        RSA rsa2 = new RSA();
//        try {
//            rsa2.loadPrivateKey(getBaseContext(), R.raw.android_private_key);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        byte dec_data[] = rsa2.decryptData(enc_data);
//        Log.d("解文件MD5", Hash.getMD5(dec_data));

//        // 分包拆包
//        System.out.print("WTF\n");
//        ByteArrayOutputStream outputStream = new ByteArrayOutputStream(1000);
//        DataPacket sendPacket = new DataPacket();
//        sendPacket.setLimit(128+64);
//        for (int i = 0; i < 10; i++) {
//            try {
//                outputStream.write(new byte[]{0,1,2,3,4,5,6,7,8,9});
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//            sendPacket.pushItem(outputStream.toByteArray());
//        }
//        // 载入公匙
//        RSA rsa1 = new RSA();
//        try {
//            rsa1.loadPublicKey(getBaseContext(), R.raw.android_public_key);
//        } catch (Exception e) {
//            Log.e(TAG, "公匙唔啱");
//        }
//        sendPacket.make(new byte[]{0,1,2,3,4,5,6,7,8,9}, rsa1);
//
//        // 打印
//        sendPacket.moveToFirstPacket();
//        do {
//            System.out.print("VerifyCRC " + (sendPacket.getCurrentPacket().verifyCRC() ? "OK  " : "fail"));
//            System.out.print(" P:" + Hash.bytes2Hex(sendPacket.getCurrentPacket().data) + "\n");
//        }while (sendPacket.moveNextPacket() != null);
//
//        // 拆包
//        DataPacket getPacket = new DataPacket();
//        sendPacket.moveToFirstPacket();
//        do {
//            getPacket.pushPacket(sendPacket.getCurrentPacket().data);
//        } while (sendPacket.moveNextPacket() != null);
//
//        RSA rsa2 = new RSA();
//        try {
//            rsa2.loadPrivateKey(getBaseContext(), R.raw.android_private_key);
//        } catch (Exception e) {
//            Log.e(TAG, "私匙唔啱");
//        }
//
//        byte ex[] = new byte[10];
//        int id = getPacket.resolve(ex, rsa2);
//        System.out.print("ID:" + id +" Ex:" + Hash.bytes2Hex(ex) + "\n");
//        do {
//            System.out.print("I:" + Hash.bytes2Hex(getPacket.getCurrentItem().data) + "\n");
//        } while (getPacket.moveNextItem() != null);



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
     *                         InteractionActivity                          *
     ************************************************************************/

    /**
     * Activity 活动状态改变事件
     * <p>让Application 保存或撤销Activity 的引用,
     * 便于回调TcpQueryActivity 上的方法.
     *
     * @param activity 引发事件的Activity
     * @param isActive true: 表示传入的Activity 应该保存引用;
     */
    @Override
    public void onActiveChanged(Activity activity, boolean isActive) {
        // LoginActivity
        if (activity.getClass() ==  LoginActivity.class) {
            if (isActive) mLoginActivity = (LoginActivity) activity;
            else          mLoginActivity = null;
        }

    }




    /************************************************************************
     *                           Service 与 AIDL                            *
     ************************************************************************/

//    private DatabaseService mDatabaseService;
//    private ServiceAidlInterface mServiceAIDL;
//
//    /**
//     * Service 的连接器
//     * 主要用嚟得到服务类的AIDL 对象, 同埋 new 一个AIDL俾服务类,(两种AIDL 系唔同嘅)
//     */
//    private ServiceConnection mServiceConnection = new ServiceConnection() {
//        public void onServiceConnected(ComponentName name, IBinder service) {
//            Log.v(TAG, "onServiceConnected");
//
//            // 得到Service 的AIDL
//            mServiceAIDL = ServiceAidlInterface.Stub.asInterface(service);
//            if (mServiceAIDL == null) {
//                Log.e(TAG, "Unable to get AIDL!");
//                return;
//            }
//
//            // 将自己的AIDL 送去Service
//            try {
//                mServiceAIDL.addAIDL(mApplicationAIDL);
//                mServiceAIDL.print("new AIDL"); // 测试用
//            } catch (RemoteException e) {
//                e.printStackTrace();
//            }
//
//        }//(^((?!(dalvikvm|jdwp)).)*$)
//
//        public void onServiceDisconnected(ComponentName name) {
//            // 通知Service 移除AIDL
//            try {
//                mServiceAIDL.removeAIDL(mApplicationAIDL);
//            } catch (RemoteException e) {
//                e.printStackTrace();
//            }
//            mServiceAIDL = null;
//            Log.v(TAG, "onServiceDisconnected");
//        }
//    };
//
//    /**
//     * AIDL 接口
//     */
//    private ApplicationAidlInterface mApplicationAIDL = new ApplicationAidlInterface.Stub() {
//
//        @Override
//        public void print(String msg) throws RemoteException {
//            Log.d(TAG, msg);
//        }
//    };



    /************************************************************************
     *                              UDP Socket                              *
     *   哩度使用自己嘅ClientSocket 集合, 与平时使用的ClientSocket 实例区分处理   *
     ************************************************************************/
    private LanAutoSearch mServerSearcher;

    /** 自动搜主机使用自己的Socket 集合 */
    private HashSet<ClientSocket> mTcpSockets = new HashSet<>();

    /** 轮询主机用 */
//    private HashMap<String/*IP*/, InetAddress> mServerIp = new HashMap<>();
    /** 企业实例反查IP用 */
//    private HashMap<Company, InetAddress> CompanyIp = new HashMap<>();

    /** 哩个内部类保存搜索到的服务端 */
//    public class Company {
//        public InetAddress Address = null;
//        int Id = 0;
//        public String Name = null;
//        public String SubName = null;
//        public RSA Rsa = null;
//    }
//
//    private HashMap<String/*IP*/, Company>CompanyMap = new HashMap<>();

    /**
     * 解释企业刷新/添加报文
     */
    protected void companyRefresh(DataPacket packet, byte extend[]) {
        Log.d(TAG, "正在处理企业列表报文");


        ArrayList<Company> company_list = Company.packet2Company(packet, extend);
//        ArrayList<Company> adapter_list = mLoginActivity.getCompanyList();
        if (company_list == null) return;

        Here : for (Company new_company : company_list) {
            // 与已有的实例进行碰撞
            for (Company old_company : Companies) {
                // 当有相同的企业:
                if (old_company.hashCode() == new_company.hashCode()) {
                    // 只更新数组
                    Companies.set(
                            Companies.indexOf(old_company),
                            new_company
                    );
                    continue Here; // 跳过下面的更新过程
                }
            }

            Companies.add(new_company);


        }


        // 在主线程更新UI
        Handler handler = new Handler(getMainLooper());
        handler.post(new Runnable(){
            @Override
            public void run() {
                if (mLoginActivity == null){
                    Log.w(TAG, "LoginActivity 已注销");
                    return;
                }

                Log.d(TAG, "更新企业列表");
                mLoginActivity.onRefresh(Companies);
            }
        });

    }

    /** 开始搜服务端
     * 停止代码直接写在onPause 事件中 */
    public void startSearchServer() {
        if (mServerSearcher != null) return;

        try {
            mServerSearcher = new LanAutoSearch(this, Define.UDP_BROADCAST_GROUP){
                /**
                 * 客户端在准备广播通知在线服务器的事件
                 * <p>哩个事件不在主线程执行!
                 * <p>对将要发送的数据加密应该在哩个事件中进行.
                 *
                 * @return 返回一段数据用于发给服务端的数据,
                 * 该数据返回null 将会导致广播中止,
                 * 正常情况应该用{@Link stop()} 方法中止广播.
                 */
                @Override
                public byte[] willCallServerEvent() {
                    // TODO: 发一段防伪信息
                    return super.willCallServerEvent();
                }

                /**
                 * 客户端收到一个服务端的应答的事件
                 * <p>哩个事件已经被放入主线程运行
                 * <p>对服务端发来的数据应该在哩个事件中进行.
                 *
                 * @param packet
                 */
                @Override
                public void onFoundServerEvent(DatagramPacket packet) {

                    InetAddress address = packet.getAddress();

                    for (ClientSocket client : mTcpSockets){
                        if (client.getSocket().getInetAddress().equals(packet.getAddress()))
                            return;
                    }

                    Log.i(TAG, "搜索到服务端" + address.toString());
//                    if (mServerIp.containsKey(address.toString())) return;

                    // TODO: 显示一个Toast

//                    mServerIp.put(address.toString(), address);

                    // 让Application 代为连接服务端, 等连接成功后会触发事件更新
                    String ip = address.getHostAddress();
//                    ((MainApplication)getApplicationContext()).connectionServer(ip);

                    ClientSocket socket = new ClientSocket(ip, Define.TCP_PORT, null) {
                        @Override
                        public void onClose(ClientSocket client) {
                            if (mTcpSockets.contains(client)) {
                                String cli_ip = client.getSocket().getInetAddress().getHostAddress();
                                ArrayList<Company> will_remove = new ArrayList<>();

                                for (Company company: Companies){
                                    String com_ip = company.ServerAddress.getHostAddress();

                                    if (company.ServerAddress.getHostAddress().equals(client.getSocket().getInetAddress().getHostAddress()))
                                        // 唔可以remove 正在遍历的集合元素, 只能用另一个集合保存要移除的引用
                                        will_remove.add(company);
                                }

                                // 移除相关的企业
                                for (Company company: will_remove) {
                                    Companies.remove(company);
                                }

                                mTcpSockets.remove(client);

                                // 在主线程更新UI
                                Handler handler = new Handler(getMainLooper());
                                handler.post(new Runnable(){
                                    @Override
                                    public void run() {
                                        if (mLoginActivity == null){
                                            Log.w(TAG, "LoginActivity 已注销");
                                            return;
                                        }

                                        Log.d(TAG, "更新企业列表");
                                        mLoginActivity.onRefresh(Companies);
                                    }
                                });
                            }
                        }

                        @Override
                        public void onConnected(final ClientSocket client) {
                            mTcpSockets.add(client);

                            // 取得一次企业列表
                            getCompanyList(LoginActivity.ACTIVITY_ID, client);

//                            // 在新线程运行
//                            new Thread() {
//                                @Override
//                                public void run() {
//                                    Thread.currentThread().setPriority(4); // 稍低的优先级
//                                    Thread.currentThread().setName("取得一次企业列表");
//                                    // 转换成报文
//                                    DataPacket packet = new DataPacket();
//                                    packet.make(new byte[] {
//                                            Define.EXTEND_COMMAND_GET_COMPANY_LIST,
//                                            LoginActivity.ACTIVITY_ID
//                                    });
//
//                                    client.send(packet);
//                                }
//                            }.start();
                        }

                        @Override
                        public DataPacket onReceivedCompletePacket(DataPacket packet, byte[] extend) {
                            Log.v(TAG, "收到服务端报文");
                            int command = Hash.byte32ToInt(extend, Define.EXTEND_COMMAND_OFFSET, 1/*字节*/);
                            switch (command) {
                                case Define.EXTEND_COMMAND_GET_COMPANY_LIST:
                                case Define.EXTEND_COMMAND_NEW_COMPANY:
                                    // 两个命令的处理过程系一样嘅
                                    companyRefresh(packet, extend);
                                    break;

                                case Define.EXTEND_COMMAND_USER_LOGIN:
                                    onLoginReply(packet, extend);
                                    break;

                                case Define.EXTEND_COMMAND_USER_REQUEST_JOIN:
                                    onNewUserReply(packet, extend);
                                    break;
                            }
                            return null;
                        }
                    };

//                    mServerSearcher.stop(); TODO: 改成登陆后
                }
            };
        } catch (UnknownHostException e) {
            Log.e(TAG, "searchServer -> " + Define.UDP_BROADCAST_GROUP + " 不是UDP 广播组IP");
            return;
        }

        boolean s = mServerSearcher.startClient(
                Define.UDP_BROADCAST_PORT,
                Define.UDP_UNICAST_PORT,
                Define.UDP_SEARCH_INTERVAL);

        if (!s) Log.e(TAG, "无法启动服务端自动搜索");
    }


    /** 停止搜索服务端 */
    public void stopSearchServer() {
        if (mServerSearcher != null) mServerSearcher.stop();
        mServerSearcher = null;


        // 关闭全部TCP Socket
        for(ClientSocket client: mTcpSockets) {
            if (client.isConnected()) {
                try { client.getSocket().close();
                } catch (IOException e) {
                    Log.w(TAG,
                            client.getSocket().getInetAddress().toString() +
                                    " 被多次重复关闭");
                }
            }
            mTcpSockets.remove(client);
        }
    }


    /************************************************************************
     *                              TCP Socket                              *
     ************************************************************************/
    /** 平时主要使用的Socket */
    private ClientSocket mClientSocket = null;

    /**
     * 取得TCP Socket 是否已经连接到服务端
     * @return true:已连接
     */
    public boolean isConnected(){return mClientSocket.isConnected();}

    /**
     * 向服务端发起TCP 连接
     * <p>除登陆界面外, 此方法只由本实例调用
     */
    private void connectionServer(String ip){
        mClientSocket = new ClientSocket(ip, Define.TCP_PORT, null) {
            @Override
            public void onConnected(ClientSocket client) {
//                // 先取得一次本地服务端的企业列表
//                getCompanyList(LoginActivity.ACTIVITY_ID, mClientSocket);
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

        // TODO: 特殊Activity判断

        // 根据命令派发报文
        int command = Hash.byte32ToInt(extend, Define.EXTEND_COMMAND_OFFSET, 1/*字节*/);
        switch (command) {
            case Define.EXTEND_COMMAND_GET_COMPANY_LIST:
//                companyRefresh(packet, extend);
                break;
            case Define.EXTEND_COMMAND_NEW_COMPANY:
//                companyAdd(packet, extend);
                break;

            default:
                Log.w(TAG, "未知命令:" + command);
                break;
        }

//        switch (Hash.byte32ToInt(extend, Define.EXTEND_ACTIVITY_OFFSET, 1/*字节*/)) {
//            case LoginActivity.ACTIVITY_ID:
//                if (mLoginActivity != null) {
//
//                }
//                break;
//            case MainActivity.ACTIVITY_ID:
//                if (mMainActivity != null) {
//                    switch (command) {
//                        case Define.EXTEND_COMMAND_GET_COMPANY_LIST:
//                            companyRefresh(packet, extend);
//                            break;
//                        case Define.EXTEND_COMMAND_NEW_COMPANY:
//                            companyAdd(packet, extend);
//                            break;
//                    }
//                }
//                break;
//            case UserListActivity.ACTIVITY_ID:
//                if (mUserListActivity != null) {
//                    switch (command) {
//                        case Define.EXTEND_COMMAND_GET_USER_LIST:
//                            usersRefresh(packet, extend);
//                            break;
//                        case Define.EXTEND_COMMAND_NEW_USER:
//                            usersAdd(packet, extend);
//                            break;
//                    }
//                }
//                break;
//            default:
//                Log.w(TAG, "唔知之前系边个Activity 请求查询");
//                break;
//        }
    }

    /************************************************************************
     *                            LoginActivity                             *
     ************************************************************************/

    /** 持有已登陆的用户对象
     * <p>在成功登陆后,
     * 哩个对象一直存在喺整个App 的生命周期 */
    public User LoggedInUser = null;

    /** 持有Activity */
    private LoginActivity mLoginActivity = null;
    /** 持有企业列表 */
    public ArrayList<Company> Companies = new ArrayList<>();


//    /**
//     * 解释企业刷新报文
//     */
//    protected void companyRefresh(DataPacket packet, byte extend[]) {
//        ArrayList<Company> company_list = Company.packet2Company(packet, extend);
//        if (company_list == null) return;
//
//        for (Company company : company_list) {
//            // 哩个判断是以hashCode 为依据的, 移除亦是以hashCode 查找的
//            if (Companies.contains(company)) Companies.remove(company);
//
//            Companies.add(company);
//        }
//
//        if (mLoginActivity != null) mLoginActivity.onRefresh(Companies);
//
//    }
//
//    /**
//     * 解释企业添加报文
//     */
//    protected void companyAdd(DataPacket packet, byte extend[]) {
//        ArrayList<Company> company_list = Company.packet2Company(packet, extend);
//        if (company_list == null) return;
//
//        for (Company company : company_list) {
//            // 哩个判断是以hashCode 为依据的, 移除亦是以hashCode 查找的
//            if (Companies.contains(company)) Companies.remove(company);
//
//            Companies.add(company);
//        }
//
//        if (mLoginActivity != null) mLoginActivity.onAdd(company_list);
//
//    }

    /**
     * 取得企业列表
     * @param activityID 传入调用哩个方法的Activity的{getID()}的值,
     *                   当查询返回的时候按照该ID,
     *                   调用相应的回调方法.
     * @param client 由于Login 使用的是不同的ClientSocket 实例,
     *               要喺哩个参数指定具体使用边个实例发送查询报文.
     */
    public void getCompanyList(final byte activityID, final ClientSocket client) {
        // App 连接服务端嘅时候已经取得过企业列表, 直接传递到请求的Activity就是
//        if (activityID != 0/*Application 自己*/){
//            switch (activityID) {
//                case LoginActivity.ACTIVITY_ID:
//                    LoginActivity.onRefresh(mCompanyList);
//                    break;
//            }
//            return;
//        }

        // 一般只会是由Application 调用先会去到哩度
        Log.v(TAG, "请求查询企业列表, Activity ID:" + activityID);
        // 询问需要的字段
//        final HashMap<String, String[]> columns =
//                MainActivity.getColumnsName(
//                        (byte) Define.EXTEND_COMMAND_GET_COMPANY_LIST,
//                        new String[]{"CompanyList"});

        // 在新线程运行
        new Thread() {
            @Override
            public void run() {
                Thread.currentThread().setPriority(4); // 稍低的优先级
                Thread.currentThread().setName("取得一次企业列表");

                // 转换成报文
                DataPacket packet = new DataPacket();
//                for (String column : columns.get("CompanyList")){
//                    packet.pushItem(column);
//                }
                packet.make(new byte[] {
                        Define.EXTEND_COMMAND_GET_COMPANY_LIST,
                        activityID
                });

                client.send(packet);
            }
        }.start();
    }

    /**
     * 使用平时的Socket 连接服务器
     * @param company
     * @param userName
     * @param password
     * @return
     */
    public boolean login2Server(byte activityID, @NonNull Company company, @NonNull String userName, @NonNull String password) {
        // 遍历找出对应的 Socket
        for (ClientSocket client : mTcpSockets) {
            // 用地址实例作为依据
            if (client.getSocket().getInetAddress().equals(company.ServerAddress)) {
                // 得到标准报文
                DataPacket packet = User.getLoginPacket(company, userName, password);

                packet.make(new byte[] {
                        Define.EXTEND_COMMAND_USER_LOGIN,
                        activityID
                });

                return client.send(packet); // 发送并退出方法
            }
        }

        Log.e(TAG, "揾唔返企业实例对应该的IP 地址??");
        return false;
    }

    /**
     * 处理登陆请求的返回报文
     * @param packet
     * @param extend
     */
    private void onLoginReply(DataPacket packet, byte[] extend) {
        User user = new User();
        final int flag = User.packet2ValidResult(packet, extend, user);

        if (flag == Define.LOGIN_FLAG_SUCCESS) {
            LoggedInUser = user;
        }

        if (mLoginActivity != null) {
            // 在主线程触发事件
            Handler handler = new Handler(getMainLooper());
            handler.post(new Runnable(){
                @Override
                public void run() {
                    mLoginActivity.onLoginReply(flag); // 哩个方法内涉及UI 更新
                }
            });
        }


    }

    /**
     * LoginActivity 验证用户登陆
     * @param user
     * @param passwordMD5
     * @return
     */
    public boolean verifyUser(String company, String user, String passwordMD5) {
        return false;
    }

    /**
     * SettingsActivity 用户登出
     * <p>登出选项在设置里边
     */
    public void exitUser() {

    }

    /**
     * LoginActivity 新增用户
     * 哩个方法对应该Server 的MainApplication 的同名方法.
     */
    public void addUser(byte activityID, @NonNull Company company, @NonNull String userName){
//        DataPacket packet = new DataPacket();
//
//        packet.pushItem(company.Id);
//        packet.pushItem(userName);
//
//        packet.make(new byte[] {
//                Define.EXTEND_COMMAND_USER_REQUEST_JOIN,
//                activityID
//        });
//
//        mClientSocket.send(packet);

        // 遍历找出对应的 Socket
        for (ClientSocket client : mTcpSockets) {
            // 用地址实例作为依据
            if (client.getSocket().getInetAddress().equals(company.ServerAddress)) {
                // 生成报文
                DataPacket packet = new DataPacket();
                User user = new User();
                user.Name = userName;
                user.InCompany = company.Id;
                User.pushUser2Packet(user, packet);

                packet.make(new byte[] {
                        Define.EXTEND_COMMAND_USER_REQUEST_JOIN,
                        activityID
                });

                client.send(packet); // 发送并退出方法
                //
                return;
            }
        }

        Log.e(TAG, "揾唔返企业实例对应该的IP 地址??");
    }

    /**
     * 处理服务端返回的新用户申请情况报文
     * @param packet
     * @param extend
     */
    private void onNewUserReply(DataPacket packet, byte[] extend) {
        if (!packet.moveToFirstItem()) {
            Log.e(TAG, "报文解释错误");
            return;
        }

        final int flag = Hash.byte32ToInt(packet.getCurrentItem().data);

        if (mLoginActivity != null) {
            // 在主线程触发事件
            Handler handler = new Handler(getMainLooper());
            handler.post(new Runnable(){
                @Override
                public void run() {
                    mLoginActivity.onNewUserReply(flag); // 哩个方法内涉及UI 更新
                }
            });
        }
    }
}
