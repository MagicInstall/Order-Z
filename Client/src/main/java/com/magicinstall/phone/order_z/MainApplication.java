package com.magicinstall.phone.order_z;

import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
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



        // TODO: 检查自己是否持有数据库

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
     *                            LoginActivity                             *
     ************************************************************************/

    /**
     * LoginActivity 取得数据库里全部企业名
     * @return
     */
    public String[] getAllCompanies() {
//        if (mServiceAIDL == null) {
//            Log.e(TAG, "未连接AIDL");
//            return new String[]{"未连接AIDL"};
//        }
        return new String[]{"哈哈", "呵呵"};
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
}
