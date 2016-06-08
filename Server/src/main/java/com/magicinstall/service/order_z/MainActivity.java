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

import com.magicinstall.library.DataPacket;
import com.magicinstall.library.Define;
import com.magicinstall.library.Hash;
import com.magicinstall.library.TcpQueryActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends TcpQueryActivity
        implements SwipeRefreshLayout.OnRefreshListener{
    private static final String TAG = "MainActivity";

    /** 静态方式取得报文的扩展字节所使用的标识*/
    public static final byte ACTIVITY_ID = 01;

    private DatabaseInServer mDatabase = null;

    private SwipeRefreshLayout mSwipeLayout;
    private ListView mListView;
    private SimpleAdapter mCompanyListAdapter;
    //    private RefreshRecyclerAdapter mCompanyListAdapter;

    /** TODO: 改成唔再自己持有哩个列表 */
    private List<HashMap<String, Object>> mCompanyList = new ArrayList<>();


//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.menu_main, menu);
//        return true;
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        switch (item.getItemId()) {
//            case R.id.AddCompanyButton:
//                // 跳转
//                Intent intent = new Intent();
//                intent.setClass(this, CompanyInfoActivity.class);
//                startActivity(intent);
//                return true;
//        }
//        return super.onOptionsItemSelected(item);
//    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate");

        setContentView(R.layout.activity_main);

        // 弱智安卓要手动设置ActionBar图标...
//        ActionBar actionBar = getSupportActionBar();
//        actionBar.setLogo(R.mipmap.ic_launcher);
//        actionBar.setDisplayUseLogoEnabled(true);
//        actionBar.setDisplayShowHomeEnabled(true);

        Toolbar toolbar = (Toolbar) findViewById(R.id.Toolbar);
//        setSupportActionBar(toolbar);
        //设置右上角的填充菜单
        toolbar.inflateMenu(R.menu.menu_main);
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            /**
             * This method will be invoked when a menu item is clicked if the item itself did
             * not already handle the event.
             *
             * @param item {@link MenuItem} that was clicked
             * @return <code>true</code> if the event was handled, <code>false</code> otherwise.
             */
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()){
                    case R.id.AddCompanyButton:
                        Log.i(TAG, "添加企业");
                        // 跳转
                        Intent intent = new Intent();
                        intent.setClass(getBaseContext(), CompanyInfoActivity.class);
                        startActivity(intent);
                        break;
                }
                return false;
            }
        });

        mSwipeLayout = (SwipeRefreshLayout)findViewById(R.id.MainSwipe);
//        swipe.setColorSchemeResources(R.color.color1, R.color.color2,
//                R.color.color3, R.color.color4);
        mSwipeLayout.setOnRefreshListener(this);
//
//        swipe.post(new Runnable(){
//            @Override
//            public void run() {
//                swipe.setRefreshing(true);
//            }
//        });
//        onRefresh();

//        RecyclerView list_view = (RecyclerView) findViewById(R.id.CompanyList);
//        list_view.addOnScrollListener(new RecyclerView.OnScrollListener() {
//
//            @Override
//            public void onScrollStateChanged(RecyclerView recyclerView,
//                                             int newState) {
//                super.onScrollStateChanged(recyclerView, newState);
////                if (newState == RecyclerView.SCROLL_STATE_IDLE
////                        && lastVisibleItem + 1 == adapter.getItemCount()) {
////                    mSwipeRefreshWidget.setRefreshing(true);
////                    // 此处在现实项目中，请换成网络请求数据代码，sendRequest .....
////                    handler.sendEmptyMessageDelayed(0, 3000);
////                }
//            }
//
//            @Override
//            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
//                super.onScrolled(recyclerView, dx, dy);
////                lastVisibleItem = mLayoutManager.findLastVisibleItemPosition();
//            }
//
//        });


//        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//            }
//        });


//        ImageButton add_button = (ImageButton)findViewById(R.id.AddCompanyButton);
//        add_button.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Log.i(TAG, "添加企业");
//                getCompanyList();
//            }
//        });


        mCompanyListAdapter = new SimpleAdapter(
                this, mCompanyList, R.layout.item_company_list,
                new String[]{
                        Define.DATABASE_COMPANY_NAME/*, "phone", "amount"*/},
                new int[]{R.id.name/*, R.id.phone, R.id.amount*/});

        mListView = (ListView) findViewById(R.id.CompanyListView);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            /**
             * 企业列表点击事件
             */
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                if (view == null) return;
                Log.i(TAG, "企业选择 position:" + position + " id:" + id);
                onCompanyClick(mCompanyList.get(position));
            }
        });

//        // 更新MainApplication 的本Activity 的引用
//        ((MainApplication)getApplication()).setMainActivity(this);


    }

    /**
     * SwipeRefreshLayout 的用户下拉刷新事件
     */
    @Override
    public void onRefresh() {
        Log.v(TAG, "onSwipeRefresh");
        getCompanyList();
//        new Handler().postDelayed(new Runnable() {
//            @Override public void run() {
//
//                mSwipeLayout.setRefreshing(false);
//            }
//        }, 2000);
    }

    /**
     * 点击某个企业事件
     * @param item
     */
    protected void onCompanyClick(HashMap<String, Object> item) {
        Log.i(TAG, "onCompanyClick " + item.get(Define.DATABASE_COMPANY_NAME) +
                " id:" + item.get(Define.DATABASE_COMPANY_ID));

        // 跳转
        Intent intent = new Intent();
        intent.putExtra(
                Define.DATABASE_COMPANY_ID,
                (int) item.get(Define.DATABASE_COMPANY_ID));
        intent.putExtra(
                Define.DATABASE_COMPANY_NAME,
                (String) item.get(Define.DATABASE_COMPANY_NAME));
        intent.putExtra(
                Define.DATABASE_COMPANY_PASSWORD,
                (String) item.get(Define.DATABASE_COMPANY_PASSWORD));
        // 指定intent要启动的类
        intent.setClass(this, CompanyInfoActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.v(TAG, "onStart");

//        mSwipeLayout.post(new Runnable(){
//            @Override
//            public void run() {
//                mSwipeLayout.setRefreshing(true);
//            }
//        });

//        if (mCompanyList.size() < 1)
        // 异步取得企业列表
        getCompanyList();
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
//        // 撤销MainApplication 的本Activity 的引用
//        ((MainApplication)getApplication()).setMainActivity(null);
    }


    /************************************************************************
     *                          TcpQueryActivity                            *
     ************************************************************************/

    /**
     * 由Application 调用的刷新全部元素事件.
     * <p>该事件一般不在主线程运行
     * @param list List 的每一个元素保存每一项的查询结果;
     *             HashMap 的Key 对应每一行的字段名
     */
    @Override
    public void onRefresh(ArrayList<HashMap<String, Object>> list) {
        // TODO: 改成直接用哩个list 参数设置Adapter
        mCompanyList.clear();
        mCompanyList.addAll(list);
        // 在主线程触发事件
        Handler handler = new Handler(getMainLooper());
        handler.post(new Runnable(){
            @Override
            public void run() {
                mListView.setAdapter(mCompanyListAdapter);
                Log.d(TAG, "setRefreshing(false)");
                mSwipeLayout.setRefreshing(false);
            }
        });
    }

    /**
     * 由Application 调用的元素添加事件
     * <p>该事件一般不在主线程运行
     * @param list List 的每一个元素保存每一项的查询结果;
     *             HashMap 的Key 对应每一行的字段名
     */
    @Override
    public void onAdd(ArrayList<HashMap<String, Object>> list) {
        mCompanyList.addAll(list);

        // 在主线程触发事件
        Handler handler = new Handler(getMainLooper());
        handler.post(new Runnable(){
            @Override
            public void run() {
                mListView.setAdapter(mCompanyListAdapter);
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
                    Define.DATABASE_COMPANY_ID,
                    Define.DATABASE_COMPANY_NAME,
                    Define.DATABASE_COMPANY_PASSWORD});
            }
        };
    }

    /************************************************************************
     *                           MainApplication                            *
     ************************************************************************/

    /**
     * 由MainApplication 调用的刷新事件.
     */
//    public void onRefresh(DataPacket packet, byte[] extend) {
//        mCompanyList.clear();
//        onAdd(packet, extend);
//    }
//
//    /**
//     * 由MainApplication 调用的企业新建事件
//     */
//    public void onAdd(DataPacket packet, byte[] extend) {
//        // 从报文添加企业项
//        mCompanyList.addAll(companyListPacket2List(packet, extend));
//
//        // 在主线程触发事件
//        Handler handler = new Handler(getMainLooper());
//        handler.post(new Runnable(){
//            @Override
//            public void run() {
//                mListView.setAdapter(mCompanyListAdapter);
//                mSwipeLayout.setRefreshing(false);
//            }
//        });
//    }

    /**
     * 将报文转换成列表对象
     * @param packet
     * @param extend
     * @return
     */
    private List<HashMap<String, Object>> companyListPacket2List(DataPacket packet, byte[] extend) {
        List<HashMap<String, Object>> list = new ArrayList<>();
        HashMap<String, Object> column;
        if (packet.moveToFirstItem()) {
            do {
                column = new HashMap<>();
                column.put(
                        Define.DATABASE_COMPANY_ID,
                        Hash.byte32ToInt(packet.getCurrentItem().data));

                packet.moveNextItem();
                column.put(
                        Define.DATABASE_COMPANY_NAME,
                        new String(packet.getCurrentItem().data));

                packet.moveNextItem();
                if (packet.getCurrentItem().data != null) {
                    column.put(
                            Define.DATABASE_COMPANY_PASSWORD,
                            new String(packet.getCurrentItem().data));
                } else {
                    column.put(Define.DATABASE_COMPANY_PASSWORD, null);
                }

                list.add(column);
            } while (packet.moveNextItem() != null);
        }
        return list;
    }

    /**
     * 调用App 类的方法向服务端查询企业列表,
     *
     */
    private void getCompanyList() {
//        DataPacket packet = new DataPacket();
//        packet.pushItem(Define.DATABASE_COMPANY_ID);
//        packet.pushItem(Define.DATABASE_COMPANY_NAME);
//        packet.pushItem(Define.DATABASE_COMPANY_PASSWORD);

        // 在主线程更改下拉刷新控件的显示状态
        Handler handler = new Handler(getMainLooper());
        handler.post(new Runnable(){
            @Override
            public void run() {
                Log.d(TAG, "setRefreshing(true)");
                mSwipeLayout.setRefreshing(true);
                // 为避免setRefreshing(false) 喺setRefreshing(true)之前插入到Looper,
                // 同样在主线程调用getCompanyList().
                ((MainApplication)getApplication()).getCompanyList(ACTIVITY_ID);
            }
        });


    }


}
