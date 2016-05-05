package com.magicinstall.phone.order_z;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 数据库基类
 * <p>
 *
 * Created by wing on 16/4/25.
 */
public abstract class Database extends SQLiteOpenHelper{
    private static final String TAG = Database.class.toString();

    private Context mContext;
    private SQLiteDatabase mDatabase;

    /**
     * 默认本地db文件夹
     */
    private static String mDatabaseLocalDir;

    /**
     * 备份文件夹
     * <p>
     * <lu>
     *     <li>取得路径建议用#getDatabaseBackupDir() 方法, 因为哩个变量只保存不带反斜杠的路径;</li>
     *     <li>所有实例(包括派生类的实例)共用一个备份路径;</li>
     * </lu>
     */
    private static String mDatabaseBackupDir;

    /**
     * 设置备份路径
     * @param dir 路径不可包含文件名!
     */
    public void setDatabaseBackupDir (String dir) {
        mDatabaseBackupDir = dir;
    }

    /**
     * 取得当前备份路径
     * @return
     */
    public String getDatabaseBackupDir() {
        return mDatabaseBackupDir;
    }

    /**
     * 构造
     * @param context 建议传入Application
     * @param fileName 只需要文件名, 不包括路径
     * @param factory TODO: 未搞明白哩个参数
     * @param version 从1开始递增的整数
     */
    public Database (Context context, String fileName, SQLiteDatabase.CursorFactory factory, int version)  {
        super(context, fileName, factory, version);

        mContext  = context;

        // 取得本地数据库路径
        mDatabaseLocalDir = context.getDatabasePath(fileName).getParent() + "/";
        Log.v(TAG, "Default database dir: " + mDatabaseLocalDir);

        // 取得扩展储存路径
        mDatabaseBackupDir =  context.getExternalFilesDir(null).getAbsolutePath() + "/";
        Log.v(TAG, "Backup database dir: " + mDatabaseBackupDir);

        // 取得SD卡路径
        Log.v(TAG, "SD dir: " + Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + context.getPackageName() + "/");

        mDatabase = getWritableDatabase();
        if (mDatabase == null) {
            Log.e(TAG, "Can not load the database file!");
            throw new RuntimeException("Can not load the database file!");
        }
    }

    /**
     * 第一次自动创建数据库文件嘅时候调用
     * <p>
     * 只有通过#getWritableDatabase()或#getReadableDatabase()方法载入数据库先可以引发哩个事件!
     * <p>
     * 建议: 只喺哩个方法内部构建最初版本的数据库,
     * 子类应该在哩个方法最后调用#onUpgrade(db, 1, DATABASE_VERSION),
     * 然后每个版本升级都在#onUpgrade() 方法内实现;
     * 因为#SQLiteOpenHelper 唔识分辨初创数据库的版本!
     * @param db
     */
    public abstract void onCreate(SQLiteDatabase db);

    /**
     * 数据库版本更新
     * <p>
     * 只有通过#getWritableDatabase()或#getReadableDatabase()方法载入数据库先可以引发哩个事件!
     * <p>
     * 目前唔允许降级
     * @param db
     * @param oldVersion
     * @param newVersion
     */
    public abstract void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion);

    //    /**
//     * 获得数据库路径，如果不存在，则创建对象对象
//     * @param dbFileName
//     * @return 返回数据库文件对象
//     */
//    public File getDatabasePath(String dbFileName) {
//        // TODO: 判断是否存在sd卡, 有内置外置\中途拆除 等情况判断
//        boolean sdExist = android.os.Environment.MEDIA_MOUNTED.equals(android.os.Environment.getExternalStorageState());
//        if(!sdExist){//如果不存在,
//            Log.e("SD卡管理：", "SD卡不存在，请加载SD卡");
//            return null;
//        }
//        else{//如果存在
//            //获取sd卡路径
//            String dbDir=android.os.Environment.getExternalStorageDirectory().toString();
//            dbDir += "/scexam";//数据库所在目录
//            String dbPath = dbDir+"/"+dbFileName;//数据库路径
//            //判断目录是否存在，不存在则创建该目录
//            File dirFile = new File(dbDir);
//            if(!dirFile.exists())
//                dirFile.mkdirs();
//
//            //数据库文件是否创建成功
//            boolean isFileCreateSuccess = false;
//            //判断文件是否存在，不存在则创建该文件
//            File dbFile = new File(dbPath);
//            if(!dbFile.exists()){
//                try {
//                    isFileCreateSuccess = dbFile.createNewFile();//创建文件
//                } catch (IOException e) {
//                    // TODO Auto-generated catch block
//                    e.printStackTrace();
//                }
//            }
//            else
//                isFileCreateSuccess = true;
//
//            //返回数据库文件对象
//            if(isFileCreateSuccess)
//                return dbFile;
//            else
//                return null;
//        }
//    }
//
//    /**
//     * TODO: 重载这个方法，是用来打开SD卡上的数据库的，android 2.3及以下会调用这个方法。
//     *
//     * @param name
//     * @param mode
//     * @param factory
//     */
//    public SQLiteDatabase openOrCreateDatabase(String name, int mode,
//                                               SQLiteDatabase.CursorFactory factory) {
//        SQLiteDatabase result = SQLiteDatabase.openOrCreateDatabase(getDatabasePath(name), null);
//        return result;
//    }

    /**
     * 载入数据库文件
     * @param context
     * @return
     */
    private SQLiteDatabase loadDatabase(Context context) {
        Log.i(TAG, "Database init...");

        String db_dir = context.getDatabasePath("db").getAbsolutePath();
        Log.i(TAG, db_dir);


//        try { // getCanonicalPath 需要指定异常
//            DATABASE_LOCAL_DIR = mContext.getDatabasePath(DATABASE_NAME).getAbsolutePath(); // 取得数据库的相对路径
//            Log.v(TAG, "DB local dir: " + DATABASE_LOCAL_DIR);
//        }  catch (){
//            Log.e(TAG, e.toString());
//        }

        // TODO: 用文件对象新建(测试建表SQL语句, 以后直接用SQLiteOpenHelper的方法打开)
//        String db_dir = android.os.Environment.getExternalStorageDirectory().getAbsolutePath()
//                + "/Order-Z";
//        String db_path = db_dir + "/" + DATABASE_NAME;
//        Log.d(TAG, "DB SD Path: " + db_path);
//
//        // 检查目录
//        File dir = new File(db_dir);
//        if (!dir.exists()) dir.mkdir();

//        String db_dir = context.getFilesDir().getAbsolutePath()+ "/databases/";
//        Log.i(TAG, "Data: " + db_dir);
//        File dir = new File(db_dir);
//        if (!dir.exists()) dir.mkdir();



        return getReadableDatabase();
//        return SQLiteDatabase.openOrCreateDatabase(db_path, null);

    }

//    /**
//     * 备份整个databases 文件夹
//     * @param dir
//     * @param childDir
//     * @return
//     */
//    public long Backup(@Nullable String dir, String childDir) {
//        String path = dir;
//        if (path == null) path = getDatabaseBackupDir(true);
//        path += childDir;
//
//        return Backup(path);
//    }

    /**
     * 备份整个databases 文件夹
     * <p>
     * 文件夹名自动改为时间
     * @param dir 目的路径, 不能带有最后的反斜杠! <p>
     *            <lu>
     *            <li>传入null 表示使用#getDatabaseBackupDir() 的路径</li>
     *            <li>最终备份的路径是@dir路径下的, 以时间命名的文件夹</li>
     *            </lu>
     * @return 返回databases 文件夹中, 已备份的文件数
     */
    public long Backup(@Nullable String dir) {
        if (dir == null) dir = getDatabaseBackupDir();
        // 加上下级目录, 以时间命名文件夹
        dir += new SimpleDateFormat("yyyy-MM-dd HH;mm;ss").format(new Date()) + "/";

        long success_count = 0;
        try {
            success_count = FileEx.copyDirectory(new File(mDatabaseLocalDir), new File(dir), true);
        } catch (IOException e){
            // TODO: ....
            Log.e(TAG, e.toString());
        }

        return success_count;
    }

    public static final int BACKUP_SUCCESS        =  0;
    public static final int BACKUP_NO_EXTERNAL    = -1;
    public static final int BACKUP_FILE_NOT_EXIST = -2;
    public static final int BACKUP_DIR_ILLEGAL    = -3;
}
