package com.magicinstall.service.order_z;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.magicinstall.library.Database;

/**
 * Created by wing on 16/4/26.
 */
public class DatabaseInServer extends Database {
    private static final String TAG = "DatabaseInServer";

    private static final int DATABASE_VERSION = 1; //数据库版本
    private static final String DATABASE_NAME = "database"; //数据库名称

    public DatabaseInServer(Context context) {
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
        // 用户表
        db.execSQL(
                " CREATE TABLE Users (" +
                        "ID integer PRIMARY KEY AUTOINCREMENT," +
                        "Name char NOT NULL," +
                        "PasswordMD5 char," +     // MD5要大写表示
                        "InCompany integer NOT NULL," +
                        "Deleted integer(1) DEFAULT(0)" + // 正常用户是0; 已删除的是1
                        ")"
        );
        // 先插入一个admin 账号, 哩个账号就会占用 1号ID
        db.execSQL("INSERT INTO Users(Name, PasswordMD5, InCompany) " +
                "VALUES ('admin', '21232F297A57A5A743894A0E4A801FC3', 0)"); // 默认密码是admin

        // 测试
        db.execSQL("INSERT INTO Users(Name, InCompany) VALUES ('测试', 1)");

        // 企业表
        db.execSQL(
                "CREATE TABLE Company (" +
                        "ID integer PRIMARY KEY AUTOINCREMENT," +
                        "Name char NOT NULL," +
                        "SubName char," +
                        "InParent integer DEFAULT(-1)," + // 母公司, 默认-1 即是自己就是母公司
                        "DatabaseOwner integer DEFAULT(1)," + // 默认1 即是超级管理员(admin)
                        "PasswordMD5 char," +
                        "Deleted integer(1) DEFAULT(0)" + // 正常企业是0; 已删除的是1
                        ");"
        );
        // TODO: 测试用: 插入一个默认企业, 企业的数据库默认由admin 账号所有
        db.execSQL("INSERT INTO Company(Name) VALUES ('测试')");

        // 菜牌表
        db.execSQL(
                "CREATE TABLE Menus (" +
                        "ID integer PRIMARY KEY AUTOINCREMENT," +
                        "InCompany integer NOT NULL" + // 菜色所在的企业ID
                        ");");

        // 订单表
        db.execSQL(
                "CREATE TABLE Orders (" +
                    "ID integer PRIMARY KEY AUTOINCREMENT," +
                    "InCompany integer NOT NULL" + // 订单所在的企业ID
                    ");");

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
        switch (oldVersion) {
            default: break;
        }
    }


}
