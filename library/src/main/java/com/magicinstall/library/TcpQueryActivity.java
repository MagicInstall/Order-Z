package com.magicinstall.library;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.util.Collection;
import java.util.HashMap;

/**
 * 所有需要从服务端查询的Activity ,
 * 都从这个类派生,
 * 哩个类定义了一些TCP 以及报文相关的实现.
 */
public abstract class TcpQueryActivity extends AppCompatActivity {
    private static final String TAG = "TcpQueryActivity";

    /**
     * 静态方式取得报文的扩展字节所使用的标识
     * <p> 子类必须重写这个值
     */
    public static final byte ACTIVITY_ID = 00;

    /**
     * 本Activity 在报文中表示的ID
     * <p> 子类只需通过重写父类字段{ACTIVITY_ID}即可.
     * @return 返回一个与其它Activity 不同的ID设置
     */
//    public byte getID(){
//        return ACTIVITY_ID;
//    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.v(TAG, "更新Application 对" + getLocalClassName() + "的引用");
        ((InteractionActivity)getApplication()).onActiveChanged(this, true);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.v(TAG, "撤销Application 对" + getLocalClassName() + "的引用");
        ((InteractionActivity)getApplication()).onActiveChanged(this, false);
    }

    /************************************************************************
     *                              Activity                                *
     ************************************************************************/

    /**
     * 由Application 调用的刷新全部元素事件.
     * <p>该事件一般不在主线程运行
     * @param collection List 的每一个元素保存每一项的查询结果;
     *             HashMap 的Key 对应每一行的字段名
     */
    public abstract void onRefresh(Collection<?> collection);

    /**
     * 由Application 调用的元素添加事件
     * <p>该事件一般不在主线程运行
     * @param collection List 的每一个元素保存每一项的查询结果;
     *             HashMap 的Key 对应每一行的字段名
     */
    public abstract void onAdd(Collection<?> collection) ;

    /**
     * TODO: 取消哩个方法
     * 根据传入的命令, 返回要查询的字段名(或多组字段名).
     * <p>重写该方法时不要调用父类的实现.
     * @param commend 某些命令可能需要的字段不同.
     * @param key     对应返回的HashMap 实例的key.
     * @return 某些命令需要多重查询, 或从不同的表之中查询,
     *         每个查询都会分别放在HashMap 的对应元素中;
     *         每一个HashMap 的val存放一组字段名的数组,
     *         该数组在某些命令中需要注意顺序.
     */
    public static  HashMap<String, String[]> getColumnsName(byte commend, String[] key) throws Exception {
        // TODO: 如果重写唔可以唔Throw, 就改成Log 输出
        throw new Exception("必须重写哩个方法!");
    }

    /**
     * Application 与TcpQueryActivity 交互的接口
     */
    public interface InteractionActivity {
        /**
         * Activity 活动状态改变事件
         * <p>让Application 保存或撤销Activity 的引用,
         * 便于回调TcpQueryActivity 上的方法.
         * @param activity 引发事件的Activity
         * @param isActive true: 表示传入的Activity 应该保存引用;
         *                 反之撤销引用.
         */
        void onActiveChanged(Activity activity, boolean isActive);
    }

}
