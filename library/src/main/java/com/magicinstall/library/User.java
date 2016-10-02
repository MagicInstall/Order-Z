package com.magicinstall.library;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.UUID;

/**
 * Created by wing on 16/6/24.
 */
public class User implements Cloneable {

    private static final String TAG = "User";

    public InetAddress ServerAddress = null;
    /** 持有登陆令牌 */
    public UUID Token = null;

    public int Id = 0;
    public String Name = null;
    public int InCompany = -1;
    /** TODO:注意密码安全问题 */
    public String Password = null;
//    public Date Update = null;
    public boolean Deleted = false;
//    public RSA Rsa = null;


    /**
     * 一般情况下, 只要Id 同ServerAddress 属性都已设置正确,
     * 哩个hash code 基本上系正确嘅.
     * Returns an integer hash code for this object. By contract, any two
     * objects for which {@link #equals} returns {@code true} must return
     * the same hash code value. This means that subclasses of {@code Object}
     * usually override both methods or neither method.
     * <p/>
     * <p>Note that hash values must not change over time unless information used in equals
     * comparisons also changes.
     * <p/>
     * <p>See <a href="{@docRoot}reference/java/lang/Object.html#writing_hashCode">Writing a correct
     * {@code hashCode} method</a>
     * if you intend implementing your own {@code hashCode} method.
     *
     * @return this object's hash code.
     * @see #equals
     */
    @Override
    public int hashCode() {
        int code = Id;
        if (ServerAddress != null) {
            code += ServerAddress.hashCode();
        }
        else if (Name != null) {
            code += Name.hashCode();
        }
        else  {
            code += InCompany;
        }
        Log.v(TAG, "hashCode:" + code);
        return code;
    }

    /**
     * 哩个方法用hashCode 嚟判断是否是同一个用户,
     * 判断的基准@see{User.hashCode()}.
     * @param o
     * @return
     */
    @Override
    public boolean equals(Object o) {
        return this.hashCode() == o.hashCode();
    }


    /**
     * 将本实例的属性全部赋予另一个实例
     * @param user
     */
    private void cloneTo(@NonNull User user){
        user.ServerAddress = this.ServerAddress;
        user.Token         = this.Token;
        user.Id            = this.Id;
        user.Name          = this.Name;
        user.Password      = this.Password;
        user.InCompany     = this.InCompany;
        user.Deleted       = this.Deleted;
    }

//    /**
//     * 用户登陆后的令牌对象
//     */
//    public static class Token {
//        byte mToken[] = null;
//
//        public Token() {
//
//        }
//
//        @Override
//        public boolean equals(Object o) {
//
//        }
//    }

    /************************************************************************
     *                              数据库字段                                *
     ************************************************************************/
    public static final String TABLE_NAME = "Messages";

    public static final String FIELD_ID = "ID";
    public static final String FIELD_NAME = "Name";
    public static final String FIELD_PASSWORD = "PasswordMD5";
    public static final String FIELD_IN_COMPANY = "InCompany";
    public static final String FIELD_DELETED = "Deleted";


    /************************************************************************
     *                        序列化与反序列化的静态方法                        *
     ************************************************************************/

    /**
     * 将多个User 对象的属性压入报文
     * <p>必需在调用报文实例的make 方法前, 最后才将User 对象压入报文, 否则packet2Users 方法会出错
     * @param users
     * @param packet
     * @return
     */
    public static boolean pushUser2Packet(@NonNull User users[], @NonNull DataPacket packet) {
        for (User user : users) {
            pushUser2Packet(user, packet);
        }
        return true;
    }


    /**
     * 将User 对象的属性压入报文
     * <p>必需在调用报文实例的make 方法前, 最后才将User 对象压入报文, 否则packet2Users 方法会出错
     * <p>对应packet2Users 的顺序
     * @param user
     * @param packet
     * @return
     */
    public static boolean pushUser2Packet(@NonNull User user, @NonNull DataPacket packet) {
        if (user.Token != null)
            packet.pushItem(user.Token.toString());
        else
            packet.pushItem((byte[]) null);

        packet.pushItem(user.Id);
        packet.pushItem(user.Name);
        packet.pushItem(user.Password);
        packet.pushItem(user.InCompany);
        packet.pushItem(user.Deleted ? 1 : 0);

        return true;
    }


    /**
     * 转换报文到User 对象
     * <p>对应pushUser2Packet 的顺序
     * @param packet 在传入参数之前, 必需先将报文的Item 指针指向正确的位置!
     *               该方法从当前指针位置开始转换;
     *               转换后, 指针指向当前User 的最后一个Item,
     *               若有后续Item 需要处理, 必需手动moveNextItem.
     * @return
     */
    public static User packet2User(@NonNull DataPacket packet) {
        User user = new User();
        user.ServerAddress = packet.remoteAddress;

        if (packet.getCurrentItem() == null){
            Log.e(TAG, "报文不正确"); return null;
        }
        if (packet.getCurrentItem().data != null)
            user.Token = UUID.fromString(new String(packet.getCurrentItem().data));

        if (packet.moveNextItem() == null){
            Log.e(TAG, "报文不正确"); return null;
        }
        user.Id = Hash.byte32ToInt(packet.getCurrentItem().data);

        if (packet.moveNextItem() == null){
            Log.e(TAG, "报文不正确"); return null;
        }
        if (packet.getCurrentItem().data != null)
            user.Name = new String(packet.getCurrentItem().data);

        if (packet.moveNextItem() == null){
            Log.e(TAG, "报文不正确"); return null;
        }
        if (packet.getCurrentItem().data != null)
            user.Password = new String(packet.getCurrentItem().data);

        if (packet.moveNextItem() == null){
            Log.e(TAG, "报文不正确"); return null;
        }
        user.InCompany = Hash.byte32ToInt(packet.getCurrentItem().data);

        if (packet.moveNextItem() == null){
            Log.e(TAG, "报文不正确"); return null;
        }
        user.Deleted = Hash.byte32ToInt(packet.getCurrentItem().data) != 0;


        return user;
    }


    /**
     * 转换报文到User 对象的数组
     * @param packet 在传入参数之前, 必需先将报文的Item 指针指向正确的位置!
     *               该方法从当前指针位置开始转换.
     * @return
     */
    public static ArrayList<User> packet2Users(@NonNull DataPacket packet) {
        HashSet<User> hash_set = new HashSet<>();
        User user;

        if (packet.getCurrentItem() != null) {
            do {
                user = User.packet2User(packet);
                if (user == null) break;
                if (!hash_set.add(user)) {
                    Log.w(TAG, "packet2User() -> 同一个报文中居然有相同HashCode 的实例? 当前实例已丢弃");
                }
            }while (packet.moveNextItem() != null);
        }


//        if (packet.moveToFirstItem()) {
//            do {
//                user = new User();
//                user.ServerAddress = packet.remoteAddress;
//
//                if (packet.moveNextItem() == null) {
//                    Log.e(TAG, "报文不正确");
//                    return null;
//                }
//                if (packet.getCurrentItem().data != null)
//                    user.Token = UUID.fromString(new String(packet.getCurrentItem().data));
//
//                if (packet.moveNextItem() == null) {
//                    Log.e(TAG, "报文不正确");
//                    return null;
//                }
//                user.Id = Hash.byte32ToInt(packet.getCurrentItem().data);
//
//                if (packet.moveNextItem() == null) {
//                    Log.e(TAG, "报文不正确");
//                    return null;
//                }
//                user.Name = String.valueOf(packet.getCurrentItem().data);
//
//                if (packet.moveNextItem() == null) {
//                    Log.e(TAG, "报文不正确");
//                    return null;
//                }
//                user.Password = String.valueOf(packet.getCurrentItem().data);
//
//                if (packet.moveNextItem() == null) {
//                    Log.e(TAG, "报文不正确");
//                    return null;
//                }
//                user.InCompany = Hash.byte32ToInt(packet.getCurrentItem().data);
//
//                if (packet.moveNextItem() == null) {
//                    Log.e(TAG, "报文不正确");
//                    return null;
//                }
//                user.Deleted = Hash.byte32ToInt(packet.getCurrentItem().data) == 0 ? false : true;
//
//                if (!hash_set.add(user)) {
//                    Log.w(TAG, "packet2User() -> 同一个报文中居然有相同HashCode 的实例? 当前实例已丢弃");
//                }
//            }
//            while (packet.moveNextItem() != null);
//        }
        return new ArrayList<>(hash_set);
    }



//    /**
//     * TODO: 可能要参照登陆验证的方法修正一下
//     * 转换用户查询返回的报文
//     * <p>该方法与queryAndPushItem 方法对应
//     */
//    public static ArrayList<User> packet2User(DataPacket packet, byte extend[]) {
//        HashSet<User> hash_set = new HashSet<>();
//        User user;
//        if (packet.moveToFirstItem()) {
//            do {
//                user = new User();
//                user.ServerAddress = packet.remoteAddress;
//
//                if (packet.getCurrentItem().data != null)
//                    user.Token = UUID.fromString(String.valueOf(packet.getCurrentItem().data));
//
//                packet.moveNextItem();
//                user.Id = Hash.byte32ToInt(packet.getCurrentItem().data);
//
//                packet.moveNextItem();
//                user.Name = new String(packet.getCurrentItem().data);
//
//                packet.moveNextItem();
//                if (packet.getCurrentItem().data != null) {
//                    user.Password = new String(packet.getCurrentItem().data);
//                }
//
//                packet.moveNextItem();
//                user.InCompany = Hash.byte32ToInt(packet.getCurrentItem().data);
//
//
//                packet.moveNextItem();
//                user.Deleted = Hash.byte32ToInt(packet.getCurrentItem().data) > 0;
//
//                if (!hash_set.add(user)) {
//                    Log.w(TAG, "packet2User() -> 同一个报文中居然有相同HashCode 的实例? 当前实例已丢弃");
//                }
//            } while (packet.moveNextItem() != null);
//        }
//
//        return new ArrayList<>(hash_set);
//    }

    /************************************************************************
     *                         数据库查询相关的静态方法                         *
     ************************************************************************/

    /**
     * 客户端生成登陆用的报文
     * @param company
     * @param user
     * @param packet
     * @return
     */
//    public static boolean pushLoginInformation(@NonNull Company company, @NonNull User user, @NonNull DataPacket packet){
//        // 必需同loginValid() 方法的顺序一致
//        packet.pushItem(company.Id);
//        packet.pushItem(user.Name);
//        packet.pushItem(user.Password);
//        return true;
//    }

    /**
     * 得到一个标准的登陆报文
     * <p>哩个方法对应validAndPushItem 方法的顺序
     * @param company
     * @param userName
     * @param password
     * @return
     */
    public static DataPacket getLoginPacket(
            @NonNull Company company,
            @NonNull String userName,
            @NonNull String password) {

        DataPacket packet = new DataPacket();
        User user = new User();
        user.Name = userName;
        user.Password = password;
        user.InCompany = company.Id;

        User.pushUser2Packet(user, packet);
        return packet;
    }

    /**
     * 服务端验证用户登陆方法
     * <p>哩个方法对应getLoginPacket 方法的顺序
     * @param db
     * @param requestPacket 传入收到的客户端登陆报文.
     * @param user 该参数用作返回, 如果验证通过, 这个实例的各个属性将被重新设置为成功登陆的用户
     * @param replyPacket 该参数用作返回, 返回回复给客户端的报文.
     * @return 返回Define.LOGIN_FLAG_XXX 表示验证结果
     */
    public static int validAndPushItem(
            @NonNull Database db,
            @NonNull DataPacket requestPacket,
                     User user,
            @NonNull DataPacket replyPacket
            /*, byte successData[]*/) {

        // 必需同getLoginPacket() 方法的顺序一致
        User request_user = User.packet2User(requestPacket);

        Cursor cursor = db.query(
                TABLE_NAME,
                null,
                FIELD_IN_COMPANY + "=? AND " + FIELD_NAME + "=?",
                new String[]{
                        String.valueOf(request_user.InCompany),
                        request_user.Name
                },
                null, null, null, null
        );

        if (!cursor.moveToFirst()){
            Log.v(TAG, "用户(" + request_user.Name + ")不存在");
            replyPacket.pushItem(Define.LOGIN_FLAG_NOT_USER);
            return Define.LOGIN_FLAG_NOT_USER;
        }

        if (cursor.getInt(cursor.getColumnIndex(FIELD_DELETED)) != 0){
            Log.i(TAG, "用户(" + request_user.Name + ")已被标记为已删除");
            replyPacket.pushItem(Define.LOGIN_FLAG_ERROR_USER_DEL);
            return Define.LOGIN_FLAG_ERROR_USER_DEL;
        }

        String db_password = cursor.getString(cursor.getColumnIndex(FIELD_PASSWORD));
//        if (user.Password == null) user.Password = "";

        if (db_password.equals(request_user.Password)) {
            Log.i(TAG, "用户(" + request_user.Name + ")验证已通过");
            replyPacket.pushItem(Define.LOGIN_FLAG_SUCCESS);

            // 只有验证通过后, 才压入用户对象的数据

            request_user.Token = UUID.randomUUID();
            request_user.Id = cursor.getInt(cursor.getColumnIndex(FIELD_ID));
            request_user.Name = cursor.getString(cursor.getColumnIndex(FIELD_NAME));
            request_user.Password = null;
            request_user.InCompany = cursor.getInt(cursor.getColumnIndex(FIELD_IN_COMPANY));
            request_user.Deleted = cursor.getInt(cursor.getColumnIndex(FIELD_DELETED)) != 0;

            // 复制属性到返回对象
            if (user != null) {
                request_user.cloneTo(user);
            }
            pushUser2Packet(request_user, replyPacket);

            return Define.LOGIN_FLAG_SUCCESS;
        } else {
            Log.i(TAG, "用户(" + request_user.Name + ")的密码错误");
            replyPacket.pushItem(Define.LOGIN_FLAG_ERROR_PASSWORD);
            return Define.LOGIN_FLAG_ERROR_PASSWORD;

        }

//        return -1;
    }


    /**
     * 客户端解释验证结果报文
     * @param packet
     * @param extend
     * @param user 如果验证通过, 哩个引用会设置成完整的内容
     * @return
     */
    public static int packet2ValidResult(
            @NonNull DataPacket packet,
            @NonNull byte extend[],
            @NonNull User user) {

        if (!packet.moveToFirstItem()){
            Log.e(TAG, "报文不正确");
            return -1;
        }

        // 哩个顺序同validAndPushItem() 方法对应
        int flag = Hash.byte32ToInt(packet.getCurrentItem().data);

        if (flag == Define.LOGIN_FLAG_SUCCESS) {
            User user_tmp = User.packet2User(packet);

            if (user_tmp == null) {
                Log.e(TAG, "收到的验证结果在解释时出错!");
                return -1;
            }

            user_tmp.cloneTo(user);
            user.ServerAddress = packet.remoteAddress;

//            user.Token     = user_tmp.Token;
//            user.Id        = user_tmp.Id;
//            user.Name      = user_tmp.Name;
//            user.Password  = user_tmp.Password;
//            user.InCompany = user_tmp.InCompany;
//            user.Deleted   = user_tmp.Deleted;
//
//            packet.moveNextItem();
//            user.Token = UUID.fromString(new String(packet.getCurrentItem().data));
//            packet.moveNextItem();
//            user.Id = Hash.byte32ToInt(packet.getCurrentItem().data);
//            packet.moveNextItem();
//            user.Name = String.valueOf(packet.getCurrentItem().data);
//            packet.moveNextItem();
//            user.Password = String.valueOf(packet.getCurrentItem().data);
//            packet.moveNextItem();
//            user.InCompany = Hash.byte32ToInt(packet.getCurrentItem().data);
//            packet.moveNextItem();
//            user.Deleted = Hash.byte32ToInt(packet.getCurrentItem().data) == 0 ? false : true;
        }

        return flag;
    }


    /**
     * 服务端查询职员列表并生成报文的方法
     * <p>该方法与packet2User 方法对应
     */
    public static int queryAndPushItem(Database db,
                                       String where,
                                       String[] whereArgs,
                                       String groupBy,
                                       String having,
                                       String orderBy,
                                       String limit,
                                       DataPacket packet) {
        return db.queryAndPushItem(
                TABLE_NAME,
                new String[]{  // <- 必需同packet2User 方法的顺序一致
                        FIELD_ID,
                        FIELD_NAME,
                        /*FIELD_PASSWORD, 从不向任何人返回密码*/
                        FIELD_IN_COMPANY,
                        FIELD_DELETED
                },
                where, whereArgs, groupBy, having, orderBy, limit,
                packet
        );
    }


    /************************************************************************
     *                          新建用户相关的静态方法                         *
     ************************************************************************/

    /**
     * 服务端查询用该用户是否存在
     * @param packet 该参数用于返回回复客户端的报文
     * @return 返回Define.USER_JOIN_FLAG_XXX 标志,
     *          如果该用户在同一个企业存在多个匹配, 则返回的是Define.USER_JOIN_FLAG_IS_PLURAL 逻辑与 匹配的数量.
     */
    public static int queryIsExistsAndPushItem(
            @NonNull Database db,
            @NonNull User user,
            @NonNull DataPacket packet) {
        
        Cursor cursor = db.query(
                TABLE_NAME,
                null,
                FIELD_IN_COMPANY + "=? AND " + FIELD_NAME + "=?",
                new String[]{
                        String.valueOf(user.InCompany),
                        user.Name
                },
                null, null, null, null
        );

        int matches = cursor.getCount();

        if (matches < 0) {
            Log.e(TAG, "什么情况?");

            // 随便返回一个值
            packet.pushItem(matches);
            return matches;
        }

        if (matches == 0) {
            packet.pushItem(Define.USER_JOIN_FLAG_NOT_USER);
            return Define.USER_JOIN_FLAG_NOT_USER;
        }

        if (matches > 1) {
            packet.pushItem(Define.USER_JOIN_FLAG_IS_PLURAL);
            packet.pushItem(matches);

            // 返回在同一个企业中匹配的数量
            return Define.USER_JOIN_FLAG_IS_PLURAL | (matches & 0x7FFF/*只取后15位*/);
        }

        if (matches == 1) {
            cursor.moveToFirst(); // 理论上经过上边一堆if 之后, 哩度已经确定有一条结果
            if (cursor.getInt(cursor.getColumnIndex(FIELD_DELETED)) == 0 ? false : true) {
                packet.pushItem(Define.USER_JOIN_FLAG_IS_DELETED);
                return Define.USER_JOIN_FLAG_IS_DELETED;
            }
        }

        // 理论上经过上边一堆if 之后, 哩度已经确定该用户名在该企业中有且只有一个存在
        packet.pushItem(Define.USER_JOIN_FLAG_IS_EXISTS);
        return Define.USER_JOIN_FLAG_IS_EXISTS;
    }

    /**
     * 客户端解释新建用户请求的结果
     * @param packet
     * @return
     */
    public static int queryIsExistsPacket2Flag(@NonNull DataPacket packet) {
        if (!packet.moveToFirstItem()){
            Log.e(TAG, "报文不正确");
            return -1;
        }

        // 哩个顺序同queryIsExistsAndPushItem() 方法对应
        int flag = Hash.byte32ToInt(packet.getCurrentItem().data);

        // Define.USER_JOIN_FLAG_IS_PLURAL 标志需要特殊处理
        if (flag == Define.USER_JOIN_FLAG_IS_PLURAL) {
            if (packet.moveNextItem() == null){
                Log.e(TAG, "报文不正确");
                return -1;
            }
            return flag | (Hash.byte32ToInt(packet.getCurrentItem().data) & 0x7FFF);
        }

        // 正常情况喺哩度返回标志
        return flag;
    }



    /************************************************************************
     *                         数据库创建相关的静态方法                         *
     ************************************************************************/

    /**
     * 创建企业表
     * <p>在Database 的子类的onCreate 事件中使用
     * @param db
     */
    public static void createTable(SQLiteDatabase db) {
        // 职员表
        db.execSQL(
                " CREATE TABLE Messages (" +
                        "ID integer PRIMARY KEY AUTOINCREMENT," +
                        "Name char NOT NULL," +
                        "PasswordMD5 char DEFAULT('')," +     // MD5要大写表示
                        "InCompany integer NOT NULL," +
                        "Deleted integer(1) DEFAULT(0)" + // 正常用户是0; 已删除的是1
                        ");"
        );

        // 先插入一个admin 账号, 哩个账号就会占用 1号ID, InCompany 为0即不属于任何企业
        db.execSQL("INSERT INTO Messages(Name, PasswordMD5, InCompany) " +
                "VALUES ('admin', '21232F297A57A5A743894A0E4A801FC3', 0)"); // 默认密码是admin

        // TODO: 测试用
        db.execSQL("INSERT INTO Messages(Name, PasswordMD5, InCompany) " +
                "VALUES ('测试员', '81DC9BDB52D04DC20036DBD8313ED055', 1)"); // 密码是1234
    }

    /**
     * 更新企业表
     * <p>在Database 的子类的onUpgrade 事件中使用
     * @param db
     * @param oldVersion
     * @param newVersion
     */
    public static void upgradeTable(SQLiteDatabase db, int oldVersion, int newVersion) {

    }



    /************************************************************************
     *                               控件绑定                                *
     ************************************************************************/

    /**
     * 职员显示适配器
     */
    public static class UserAdapter extends BaseAdapter /*implements SpinnerAdapter */{
        private Context mContext;
        protected int mResource;
        protected int mDropDownResource;

        public ArrayList<User> Users = new ArrayList<>();
        public void setUsers(ArrayList<User> users) { Users = users;}

        public UserAdapter(
                Context context,
                /*@NonNull ArrayList<Company> companies,*/
                @LayoutRes int resource) {

            mContext          = context;
//            Messages         = companies;
            mResource         = resource;
            mDropDownResource = resource; // 下拉默认使用相同样式
        }

        /**
         * How many items are in the data set represented by this CompanyAdapter.
         *
         * @return Count of items.
         */
        @Override
        public int getCount() {
            return Users.size();
        }

        /**
         * Get the data item associated with the specified position in the data set.
         *
         * @param position Position of the item whose data we want within the adapter's
         *                 data set.
         * @return The data at the specified position.
         */
        @Override
        public User getItem(int position) {
            return Users.get(position);
        }

        /**
         * Get the row id associated with the specified position in the list.
         *
         * @param position The position of the item within the adapter's data set whose row id we want.
         * @return The id of the item at the specified position.
         */
        @Override
        public long getItemId(int position) {
            return position;
        }

        /**
         * Get a View that displays the data at the specified position in the data set. You can either
         * create a View manually or inflate it from an XML layout file. When the View is inflated, the
         * parent View (GridView, ListView...) will apply default layout parameters unless you use
         * {@link LayoutInflater#inflate(int, ViewGroup, boolean)}
         * to specify a root view and to prevent attachment to the root.
         *
         * @param position    The position of the item within the adapter's data set of the item whose view
         *                    we want.
         * @param convertView The old view to reuse, if possible. Note: You should check that this view
         *                    is non-null and of an appropriate type before using. If it is not possible to convert
         *                    this view to display the correct data, this method can create a new view.
         *                    Heterogeneous lists can specify their number of view types, so that this View is
         *                    always of the right type (see {@link #getViewTypeCount()} and
         *                    {@link #getItemViewType(int)}).
         * @param parent      The parent that this view will eventually be attached to
         * @return A View corresponding to the data at the specified position.
         */
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(mContext).inflate(
                        mResource,
                        parent, false);
            }

            return bindView(
                    getItem(position),
                    position,
                    convertView
            );
        }

        /**
         * 使用与常态不同的下拉样式
         * @param resource
         */
        public void setDropDownViewResource(int resource) {
            mDropDownResource = resource;
        }

        @Override
        public View getDropDownView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(mContext).inflate(
                        mDropDownResource,
                        parent, false);
            }

            return bindDropDownView(
                    getItem(position),
                    position,
                    convertView
            );
        }

        /**
         * 重写哩个方法绑定控件
         * <p>如果下拉控件指定了不同的Resource,
         * 亦可以只使用哩个方法完成两种样式的绑定,
         * 这需要在本方法内做足够的Resource id 判断工作.
         * @param user 当前的企业对象.
         * @param position 正在适配的这个控件在父控件中的位置.
         * @param convertView 用哩个对象的findViewById() 方法取得需要绑定的控件.
         * @return 返回一个已经设置好的控件.
         */
        public View bindView(User user, int position, View convertView){return convertView;}

        /**
         * 如果使用指定的下拉样式, 可以重写哩个方法分别绑定
         * @param user
         * @param position
         * @param convertView
         * @return
         */
        public View bindDropDownView(User user, int position, View convertView){
            return bindView(user, position, convertView);
        }
    }
}
