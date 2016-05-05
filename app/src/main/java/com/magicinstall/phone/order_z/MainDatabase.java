package com.magicinstall.phone.order_z;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

/**
 * Created by wing on 16/4/26.
 */
public class MainDatabase extends Database {
    private static final String TAG = MainDatabase.class.toString();

    private static final int DATABASE_VERSION = 1; //数据库版本
    private static final String DATABASE_NAME = "database"; //数据库名称

    public MainDatabase(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);

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
     * @param db The database.
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(
                " CREATE TABLE Users (" +
                        "ID integer PRIMARY KEY AUTOINCREMENT NOT NULL," +
                        "Name char," +
                        "PasswordMD5 char," +
                        "Deleted integer(1) NOT NULL DEFAULT(0)" + // 正常用户是0; 已删除的是1
                        ")"
        );
        Log.i(TAG, "Database file(" + db.getPath() + ") has created!");

        onUpgrade(db, 1, DATABASE_VERSION);
    }

    /**
     * 数据库版本更新
     * <p>
     * 只有通过#getWritableDatabase()或#getReadableDatabase()方法载入数据库先可以引发哩个事件!
     * <p>
     * 安卓唔允许数据库版本降级!
     *
     * @param db
     * @param oldVersion
     * @param newVersion
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.i(TAG, "onUpgrade:" + oldVersion + " -> " + newVersion);
    }


}
