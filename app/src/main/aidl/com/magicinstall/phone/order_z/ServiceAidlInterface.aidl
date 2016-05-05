// ServiceAidlInterface.aidl
package com.magicinstall.phone.order_z;

import com.magicinstall.phone.order_z.ApplicationAidlInterface;

/**
* AIDL 接口使用笔记
* <p>
* 配合远程服务使用:
* <lu>
*     <li>用AIDL 模板建本接口,
*         每次修改接口都要Build 一次先会更新自动生成嘅java文件(Stub)</li>
*     <li>用Service 模板建后台服务类, AndroidManifest.xml 入边<application>标签下添加:
*         <p>
*         <service
*             android:name=".[Service类名]"
*             android:enabled="true"
*             android:process="[工程.的.包名].[中意嘅服务名]">
*             <intent-filter>
*                 <action android:name="[工程.的.包名].[中意嘅意图名]"/>
*             </intent-filter>
*         </service>
*         <p>;
*     </li>
*     <li>服务类中实现接口的方法, 在{@link #Service.onBind}方法中new 一个AIDL 实例并返回佢,
*         服务类无需持有哩个实例的引用;
*         在{@link #Service.onStartCommand}方法中直接返回{@link #Service.START_STICKY}
*         指明哩个服务被Kill 之后自动重启</li>
*     <li>建一个Application 子类(以下简称app 类),
*         AndroidManifest.xml 入边<application>标签的name属性写入[.类名],
*         app 类里边new 一个{@Link #Application.ServiceConnection}实例,
*         {@Link #Application.ServiceConnection} 的
*         {@Link #Application.ServiceConnection.onServiceConnected}方法里边,
*         用xxx = [AIDL 接口名].Stub.asInterface(service) 取得并持有AIDL 实例</li>
*     <li>依家app 类可以过AIDL 实例的方法调用服务类的方法;</li>
*     <li>要由服务类调用app 类, 就又要建一个AIDL, 参照上面嘅方法,
*         </li>
* </lu>
* <p>注意:
* <lu>
*     <li>由于安卓的Binder 限制, 唔可以传递超过1M 的数据(唔知哩1M 包括D乜嘢...)
* </lu>
*/
interface ServiceAidlInterface {
//    /**
//     * Demonstrates some basic types that you can use as parameters
//     * and return values in AIDL.
//     */
//    void basicTypes(int anInt, long aLong, boolean aBoolean, float aFloat,
//            double aDouble, String aString);

    /**
     * 将App 的AIDL 传俾Service, Service 可以通过佢回调App
     * @param aidl
     */
    void addAIDL(ApplicationAidlInterface aidl);

    /**
     * App 断开Service 的绑定时, 移除AIDL
     * @param aidl
     */
    void removeAIDL(ApplicationAidlInterface aidl);

    /**
     * 测试用
     */
    void print(String msg);
}
