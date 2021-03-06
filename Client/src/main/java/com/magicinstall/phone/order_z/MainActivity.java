package com.magicinstall.phone.order_z;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.magicinstall.library.Define;
import com.magicinstall.library.TcpQueryActivity;

import java.util.Collection;

public class MainActivity extends TcpQueryActivity /*ActivityManagerApplication.ManagedActivity*/ {
    private static final String TAG = "MainActivity";

    private ImageView mSettingButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "onCreate");
//        if (!LanAutoSearch.checkUser("", "")) {
//            gotoLoginActivity();
//        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        mSettingButton = (ImageView) findViewById(R.id.setting_btn);
        mSettingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gotoSettingActivity();
            }
        });
    }

//    /**
//     * 返回键退到桌面
//     * @param keyCode
//     * @param event
//     * @return
//     */
//    @Override
//    public boolean onKeyDown(int keyCode, KeyEvent event)
//    {
//        if (keyCode == KeyEvent.KEYCODE_BACK) {
//
//            Intent home = new Intent(Intent.ACTION_MAIN);
//            home.addCategory(Intent.CATEGORY_HOME);
//            startActivity(home);
//            return false;
//        }
//
//        // 其它键向下传递
//        return super.onKeyDown(keyCode, event);
//    }

    @Override
    protected void onStart() {
        Log.i(TAG, "onStart");
//        if (LanAutoSearch.checkUser("", "")) {
//            gotoLoginActivity();
//        }
        super.onStart();
    }

    @Override
    protected void onResume() {
        Log.i(TAG, "onResume");
//        if (!LanAutoSearch.checkUser("", "")) {
//            gotoLoginActivity();
//        }

        // TODO: 哩度判断登陆咗未
//        if (!((MainApplication)getApplicationContext()).isLogined()) {
//            gotoLoginActivity();
//        }

        super.onResume();
    }

    @Override
    protected void onDestroy() {
        Log.i(TAG, "onDestroy");
        super.onDestroy();
    }



    /**
     * 跳转到登陆页面
     */
    private void gotoLoginActivity() {
        Intent intent = new Intent();
//        intent.putExtra("User", mEmailView.getText().toString());
        // 指定intent要启动的类
        intent.setClass(this, LoginActivity.class);
        this.startActivity(intent);
        Log.i(TAG, "gotoLoginActivity");
//        this.finish();
    }

    /**
     * 跳转到设置页面
     */
    private void gotoSettingActivity() {
        Intent intent = new Intent();
//        intent.putExtra("User", mEmailView.getText().toString());
        // 指定intent要启动的类
        intent.setClass(this, SettingsActivity.class);
        this.startActivity(intent);
        Log.i(TAG, "gotoSettingsActivity");
//        this.finish();
    }



    /************************************************************************
     *                           TcpQueryActivity                           *
     ************************************************************************/
    public static final byte ACTIVITY_ID = Define.MAIN_ACTIVITY_ID;


    /**
     * 由Application 调用的刷新全部元素事件.
     * <p>该事件一般不在主线程运行
     *
     * @param collection List 的每一个元素保存每一项的查询结果;
     *                   HashMap 的Key 对应每一行的字段名
     */
    @Override
    public void onRefresh(Collection<?> collection) {

    }

    /**
     * 由Application 调用的元素添加事件
     * <p>该事件一般不在主线程运行
     *
     * @param collection List 的每一个元素保存每一项的查询结果;
     *                   HashMap 的Key 对应每一行的字段名
     */
    @Override
    public void onAdd(Collection<?> collection) {

    }
}
