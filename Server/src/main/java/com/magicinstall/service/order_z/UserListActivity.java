package com.magicinstall.service.order_z;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.magicinstall.library.Define;
import com.magicinstall.library.TcpQueryActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class UserListActivity extends TcpQueryActivity
        implements SwipeRefreshLayout.OnRefreshListener, Toolbar.OnMenuItemClickListener{
    private static final String TAG = "UserListActivity";

    /** 静态方式取得报文的扩展字节所使用的标识*/
    public static final byte ACTIVITY_ID = 02;

    private SwipeRefreshLayout mSwipeLayout;
    private ListView mListView;
    private SimpleAdapter mUserListAdapter;

    /** TODO: 改成唔再自己持有哩个列表 */
    private List<HashMap<String, Object>> mUserList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate");

        setContentView(R.layout.activity_user_list);

        Toolbar toolbar = (Toolbar) findViewById(R.id.Toolbar);
        //设置右上角的填充菜单
        toolbar.inflateMenu(R.menu.menu_user_list);
        toolbar.setOnMenuItemClickListener(this);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "NavigationOnClick");
                onBackPressed();
            }
        });

        mSwipeLayout = (SwipeRefreshLayout)findViewById(R.id.UserSwipe);
        mSwipeLayout.setOnRefreshListener(this);

        mUserListAdapter = new SimpleAdapter(
                this, mUserList, R.layout.item_user_list,
                new String[]{
                        Define.DATABASE_USER_NAME/*, "phone", "amount"*/},
                new int[]{R.id.name/*, R.id.phone, R.id.amount*/});

        mListView = (ListView) findViewById(R.id.userListView);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            /**
             * 职员列表点击事件
             */
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                if (view == null) return;
                Log.i(TAG, "职员选择 position:" + position + " id:" + id);
                onUserClick(mUserList.get(position));
            }
        });

    }

    /**
     * Toolbar 上的item 点击事件
     * @param item
     * @return
     */
    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()){
            case R.id.AddUser:
                Log.i(TAG, "新增职员");
                break;
            case R.id.ChangeManager:
                Log.i(TAG, "变更主管");
//                // 跳转
//                Intent intent = new Intent();
//                intent.setClass(getBaseContext(), CompanyInfoActivity.class);
//                startActivity(intent);
                break;
            default:
                Log.d(TAG, "未设置的Toolbar item(id:" + item.getItemId() +") 事件");
                break;
        }
        return false;
    }



    /**
     * SwipeRefreshLayout 的用户下拉刷新事件
     */
    @Override
    public void onRefresh() {
        Log.v(TAG, "onSwipeRefresh");
        getUserList();
//        getCompanyList();
//        new Handler().postDelayed(new Runnable() {
//            @Override public void run() {
//
//                mSwipeLayout.setRefreshing(false);
//            }
//        }, 2000);
    }

    /**
     * 点击某个职员事件
     * @param item
     */
    private void onUserClick(HashMap<String, Object> item) {
        Log.i(TAG, "onUserClick " + item.get(Define.DATABASE_USER_NAME) +
                " id:" + item.get(Define.DATABASE_USER_ID));
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.v(TAG, "onStart");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.v(TAG, "onRestart");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.v(TAG, "onResume");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.v(TAG, "onPause");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.v(TAG, "onStop");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.v(TAG, "onDestroy");
    }

    /************************************************************************
     *                          TcpQueryActivity                            *
     ************************************************************************/

    /**
     * 由Application 调用的刷新全部元素事件.
     * <p>该事件一般不在主线程运行
     *
     * @param list List 的每一个元素保存每一项的查询结果;
     *             HashMap 的Key 对应每一行的字段名
     */
    @Override
    public void onRefresh(ArrayList<HashMap<String, Object>> list) {
        // TODO: 改成直接用哩个list 参数设置Adapter
        mUserList.clear();
        mUserList.addAll(list);
        // 在主线程触发事件
        Handler handler = new Handler(getMainLooper());
        handler.post(new Runnable(){
            @Override
            public void run() {
                mListView.setAdapter(mUserListAdapter);
                Log.d(TAG, "setRefreshing(false)");
                mSwipeLayout.setRefreshing(false);
            }
        });
    }

    /**
     * 由Application 调用的元素添加事件
     * <p>该事件一般不在主线程运行
     *
     * @param list List 的每一个元素保存每一项的查询结果;
     *             HashMap 的Key 对应每一行的字段名
     */
    @Override
    public void onAdd(ArrayList<HashMap<String, Object>> list) {
        mUserList.addAll(list);

        // 在主线程触发事件
        Handler handler = new Handler(getMainLooper());
        handler.post(new Runnable(){
            @Override
            public void run() {
                mListView.setAdapter(mUserListAdapter);
                // TODO: 显示一下提示
            }
        });
    }

    /**
     * 根据传入的命令, 返回要查询的字段名(或多组字段名).
     * @param commend 某些命令可能需要的字段不同.
     * @param key     对应返回的HashMap 实例的key.
     * @return 某些命令需要多重查询, 或从不同的表之中查询,
     *         每个查询都会分别放在HashMap 的对应元素中;
     *         每一个HashMap 的val存放一组字段名的数组,
     *         该数组在某些命令中需要注意顺序.
     */
    public static HashMap<String, String[]> getColumnsName(byte commend, final String[] key) {
        return new HashMap<String, String[]>(){
            {put(key[0], new String[]{
                    Define.DATABASE_USER_ID,
                    Define.DATABASE_USER_IN_COMPANY,
                    Define.DATABASE_USER_NAME,
                    Define.DATABASE_USER_PASSWORD});
            }
        };
    }

    private void getUserList() {
        // 在主线程更改下拉刷新控件的显示状态
        Handler handler = new Handler(getMainLooper());
        handler.post(new Runnable(){
            @Override
            public void run() {
                Log.d(TAG, "setRefreshing(true)");
                mSwipeLayout.setRefreshing(true);
                // 为避免setRefreshing(false) 喺setRefreshing(true)之前插入到Looper,
                // 同样在主线程调用getUserList().
                Intent intent = getIntent();
                int company_id = intent.getIntExtra(Define.DATABASE_COMPANY_ID, -1);
                ((MainApplication)getApplication()).getUserList(ACTIVITY_ID, company_id);
            }
        });
    }
}
