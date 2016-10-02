package com.magicinstall.phone.order_z;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.ContentResolver;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.magicinstall.library.Company;
import com.magicinstall.library.Define;
import com.magicinstall.library.Hash;
import com.magicinstall.library.TcpQueryActivity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static android.Manifest.permission.READ_CONTACTS;



/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends TcpQueryActivity implements LoaderCallbacks<Cursor> {
    private static final String TAG = "LoginActivity";


    // UI references.
    private View mConnectTips;
    private Spinner mCompanySpinner;
    private AutoCompleteTextView mUserNameView;
    private EditText mPasswordView;
    private Button mSignInButton;
    private View mProgressView;
    private View mLoginFormView;
    private TextView mForgotView;


    /** 下拉适配器
     * <p>哩个适配器同时兼作企业列表的储存器(利用其内部的ArrayList<Company> 属性)
     */
    private Company.CompanyAdapter mCompanyAdapter = new Company.CompanyAdapter(
            this,
            R.layout.spinner_login_company ){

        /**
         * 重写哩个方法绑定控件
         * <p>如果下拉控件指定了不同的Resource,
         * 亦可以只使用哩个方法完成两种样式的绑定,
         * 这需要在本方法内做足够的Resource id 判断工作.
         *
         * @param company     当前的企业对象.
         * @param position    正在适配的这个控件在父控件中的位置.
         * @param convertView 用哩个对象的findViewById() 方法取得需要绑定的控件.
         * @return 返回一个已经设置好的控件.
         */
        @Override
        public View bindView(Company company, int position, View convertView) {
            ImageView icon_view = (ImageView) convertView.findViewById(R.id.Icon);
            if (icon_view != null && company.Icon != null)
                icon_view.setImageBitmap(company.Icon);

            TextView name_view = (TextView) convertView.findViewById(R.id.Name);
            if (name_view != null && company.Name != null)
                name_view.setText(company.Name);

            TextView sub_name_view = (TextView) convertView.findViewById(R.id.SubName);
            if (sub_name_view != null && company.SubName != null)
                sub_name_view.setText(company.SubName);

            TextView addr_view = (TextView) convertView.findViewById(R.id.Address);
            if (addr_view != null)
                addr_view.setText(String.valueOf(company.ServerAddress));

            TextView id_view = (TextView) convertView.findViewById(R.id.Id);
            if (id_view != null)
                id_view.setText(String.valueOf(company.Id));

            return convertView;
        }
    };

    /** 取得企业显示适配器内的企业列表实例
     * <p>这里得到的只是引用的副本, 对哩个副本new 是冇用嘅 */
//    public ArrayList<Company> getCompanyList(){ return mCompanyAdapter.Companies;}

    /** 取得企业显示适配器 */
    public Company.CompanyAdapter getCompanyAdapter(){return mCompanyAdapter;}

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Set up the login form.

        // 从数据库读出所有企业名
//        String com_names[] = ((MainApplication)getApplication()).getAllCompanies();

//        mCompanyAdapter = new SimpleAdapter(this, android.R.layout.simple_spinner_item, com_names);
//        mCompanyAdapter = new SimpleAdapter(
//                this,
//                ((MainApplication)getApplication()).getAllCompanies(),
//
//                );
//        // TODO: 暂时用默认的样式
//        mCompanyAdapter.setDropDownViewResource(R.layout.spinner_login_company);

        // 连接提示条
        mConnectTips = findViewById(R.id.connect_tips);

        // 企业转盘
        mCompanySpinner = (Spinner) findViewById(R.id.Login_company_spinner);
        mCompanySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){
            /**
             * <p>Callback method to be invoked when an item in this view has been
             * selected. This callback is invoked only when the newly selected
             * position is different from the previously selected position or if
             * there was no selected item.</p>
             * <p/>
             * Impelmenters can call getItemAtPosition(position) if they need to access the
             * data associated with the selected item.
             *
             * @param parent   The AdapterView where the selection happened
             * @param view     The view within the AdapterView that was clicked
             * @param position The position of the view in the adapter
             * @param id       The row id of the item that is selected
             */
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (view == null) return;
                Log.i(TAG, "企业选择 position:" + position + " id:" + id +
                        " " + mCompanyAdapter.getItem(position).Name);
            }

            /**
             * Callback method to be invoked when the selection disappears from this
             * view. The selection can disappear for instance when touch is activated
             * or when the adapter becomes empty.
             *
             * @param parent The AdapterView that now contains no selected item.
             */
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                Log.i(TAG, "企业选择: 乜都冇选");
            }
        });
        mCompanyAdapter.setCompanies(((MainApplication)getApplication()).Companies);
        mCompanySpinner.setAdapter(mCompanyAdapter);


        mUserNameView = (AutoCompleteTextView) findViewById(R.id.login_user_textview);
        populateAutoComplete();

        mPasswordView = (EditText) findViewById(R.id.login_password_textview);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_ACTION_DONE) {
                    login();
                    return true;
                }
                return false;
            }
        });

        mSignInButton = (Button) findViewById(R.id.login_sign_in_button);
        mSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                login();
            }
        });

        mForgotView = (TextView)findViewById(R.id.forgot_password_button);
        mForgotView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                if (id == R.id.login || id == EditorInfo.IME_NULL) {
//                    login();
//                    return true;
//                }
//                return false;
                gotoForgotPasswordActivity();
            }
        });

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);

        // 打开UDP
        ((MainApplication)getApplication()).startSearchServer();
//        startSearchServer();
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.i(TAG, "onStart");

        // 手动触发一次刷新
        onRefresh(((MainApplication)getApplication()).Companies);
    }

    @Override
    protected void onPause() {
        Log.i(TAG, "onPause");
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        Log.i(TAG, "onDestroy");
        super.onDestroy();
    }

    /**
     * 返回键退出程序
     * @param keyCode
     * @param event
     * @return
     */
//    @Override
//    public boolean onKeyDown(int keyCode, KeyEvent event)
//    {
//        if (keyCode == KeyEvent.KEYCODE_BACK) {
//
////            Intent home = new Intent(Intent.ACTION_MAIN);
////            home.addCategory(Intent.CATEGORY_HOME);
////            startActivity(home);
//
//            exit();
//            return false;
//        }
//
//        // 其它键向下传递
//        return super.onKeyDown(keyCode, event);
//    }


    /**
     * 显示进度条, 隐藏输入控件
     */
//    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(boolean show) {
        mCompanySpinner.setEnabled(!show);
        mUserNameView.setEnabled(!show);
        mPasswordView.setEnabled(!show);
        mSignInButton.setEnabled(!show);
        mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);


//        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
//        // for very easy animations. If available, use these APIs to fade-in
//        // the progress spinner.
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
//            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);
//
//            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
//            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
//                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
//                @Override
//                public void onAnimationEnd(Animator animation) {
//                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
//                }
//            });
//
//            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
//            mProgressView.animate().setDuration(shortAnimTime).alpha(
//                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
//                @Override
//                public void onAnimationEnd(Animator animation) {
//                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
//                }
//            });
//        } else {
//            // The ViewPropertyAnimator APIs are not available, so simply show
//            // and hide the relevant UI components.
//            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
//            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
//        }
    }


    /**
     * 跳转到取回密码页面
     */
    private void gotoForgotPasswordActivity() {
        Log.i(TAG, mUserNameView.getText().toString());

        /* 新建一个Intent对象 */
        Intent intent = new Intent();

        intent.putExtra("User", mUserNameView.getText().toString());
        /* 指定intent要启动的类 */
        intent.setClass(this, ForgotPasswordActivity.class);
        /* 启动一个新的Activity */
        this.startActivity(intent);
        /* 关闭当前的Activity */
//        this.finish();
    }

    /**
     * 跳转到主页面
     */
    private void gotoMainActivity() {
        // 关UDP
        ((MainApplication)getApplication()).stopSearchServer();
//        if (mServerSearcher != null) mServerSearcher.stop();
//        mServerSearcher = null;

        // 关TCP
//        closeAllSocket();


        Intent intent = new Intent();
        intent.setFlags(/*Intent.FLAG_ACTIVITY_NEW_TASK |*/ Intent.FLAG_ACTIVITY_CLEAR_TASK);
//        intent.putExtra("User", mUserNameView.getText().toString());
//        intent.putExtra("Password", mPasswordView.getText().toString());

        // 指定intent要启动的类
        intent.setClass(this, MainActivity.class);
        startActivity(intent);
        finish();
    }




    /************************************************************************
     *                               登陆处理                                *
     ************************************************************************/

    /**
     * 登陆按钮点击事件处理
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void login() {
//        if (mLoginTask != null) {
//            return;
//        }


        // 清空提示
        mUserNameView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        Company company = mCompanyAdapter.getItem(mCompanySpinner.getSelectedItemPosition());
        String name = mUserNameView.getText().toString();
        String password = mPasswordView.getText().toString();


        // 检查用户名
        if (name.length() < 2) {
            mUserNameView.setError(getString(R.string.err_login_user_name_too_short));
            mUserNameView.requestFocus();
            return;
        }

        // 检查密码
        if (password.length() < 4) {
            mPasswordView.setError(getString(R.string.err_login_password_too_short));
            mPasswordView.requestFocus();
            return;
        }




//        boolean cancel = false;
//        View focusView = null;
//
//        // Check for a valid password, if the user entered one.
//        if (!TextUtils.isEmpty(password) && !checkPassword(password)) {
//            mPasswordView.setError(getString(R.string.error_invalid_password));
//            focusView = mPasswordView;
//            cancel = true;
//        }
//
//        // Check for a valid email address.
//        if (TextUtils.isEmpty(name)) {
//            mUserNameView.setError(getString(R.string.error_field_required));
//            focusView = mUserNameView;
//            cancel = true;
//        } else if (!isUserValid(name)) {
//            mUserNameView.setError(getString(R.string.error_invalid_email));
//            focusView = mUserNameView;
//            cancel = true;
//        }
//
//        if (cancel) {
//            // There was an error; don't attempt login and focus the first
//            // form field with an error.
//            focusView.requestFocus();
//        }
//        else {


            // TODO: 哩度开始登陆
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);


        ((MainApplication)getApplication()).login2Server(
                ACTIVITY_ID,
                company,
                name,
                Hash.getMD5(password) // 在传送至服务端之前, 将密码转成MD5
        );
//            mLoginTask = new UserLoginTask(name, password);
//            mLoginTask.execute((Void) null);
//        }
    }

    /**
     * TODO: 验证用户名输入
     * @param user
     * @return
     */
    private boolean isUserValid(String user) {
        return true;
    }

    /**
     * TODO: 检查密码输入
     * @param password
     * @return
     */
    private boolean checkPassword(String password) {
        return password.length() > 4;
    }




    /************************************************************************
     *                           TcpQueryActivity                           *
     ************************************************************************/
    public static final byte ACTIVITY_ID = Define.LOGIN_ACTIVITY_ID;

    /**
     * 由Application 调用的元素添加事件
     * <p>该事件一般不在主线程运行
     *
     * @param collection List 的每一个元素保存每一项的查询结果;
     *                   HashMap 的Key 对应每一行的字段名
     */
    @Override
    public void onAdd(Collection<?> collection) {
//        mCompanyAdapter.Messages.addAll((Collection<? extends Company>) collection);
    }

    /**
     * 由Application 调用的刷新全部元素事件.
     * <p>该事件一般不在主线程运行
     *
     * @param collection List 的每一个元素保存每一项的查询结果;
     *                   HashMap 的Key 对应每一行的字段名
     */
    @Override
    public void onRefresh(Collection<?> collection) {
        mCompanyAdapter.setCompanies((ArrayList<Company>) collection);
        mCompanySpinner.setAdapter(mCompanyAdapter);
//            mCompanySpinner.setAdapter(null);

        if (collection.size() > 0) {
            mConnectTips.setVisibility(View.GONE);
        } else {
            mConnectTips.setVisibility(View.VISIBLE);
        }
        Log.d(TAG, "连接提示条 -- " + collection.size());
    }

    /**
     * 由Application 调用的服务端登陆回复事件
     * @param flag
     */
    public void onLoginReply(int flag) {
        switch (flag) {
            case Define.LOGIN_FLAG_SUCCESS:
                Toast.makeText(getApplicationContext(), getString(R.string.text_login_successful), Toast.LENGTH_SHORT)
                        .show();
                // 跳转
                gotoMainActivity();
                break;
            case Define.LOGIN_FLAG_NOT_USER:
                // 弹窗
                final Company company = mCompanyAdapter.getItem(mCompanySpinner.getSelectedItemPosition());

                new AlertDialog.Builder(this)
                        .setTitle(getString(R.string.login_join_question_title))
                        .setMessage(getString(R.string.login_sure_you_join) + " " + company.Name +
                                (company.SubName != null ? "("+company.SubName+")" : "") +
                                " ?\n\n" + getString(R.string.text_user_join_tips))
                        .setPositiveButton(getString(R.string.login_sure_join), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // 发送添加新用户
                                ((MainApplication)getApplication()).addUser(ACTIVITY_ID, company, mUserNameView.getText().toString());
                                // 恢复界面
                                Toast.makeText(getApplicationContext(), getString(R.string.text_user_join_request_send), Toast.LENGTH_SHORT)
                                        .show();
                                showProgress(false);
                            }
                        })
                        .setNegativeButton(getString(R.string.login_dont_join), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // 恢复界面
                                showProgress(false);
                            }
                        })
                        .show();
                break;
            case Define.LOGIN_FLAG_ERROR_PASSWORD:
                showProgress(false);
                mPasswordView.setError(getString(R.string.err_incorrect_password));
                Toast.makeText(getApplicationContext(), mPasswordView.getError(), Toast.LENGTH_SHORT)
                        .show();
                break;
            case Define.LOGIN_FLAG_ERROR_USER_DEL:
                showProgress(false);
                mUserNameView.setError(getString(R.string.err_login_user_deleted));
                break;
        }
    }

    /**
     * 由Application 调用的服务端回复新用户申请情况事件
     * @param flag 申请结果以Define.USER_JOIN_FLAG_XXX 表示
     */
    public void onNewUserReply(int flag) {
        // 弹窗
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.login_new_user_reply_title));

        switch (flag) {
            case Define.USER_JOIN_FLAG_NO_ERROR:
                builder.setMessage(getString(R.string.login_new_user_msg_ok));
                break;
            default:
                builder.setMessage(
                        getString(R.string.login_new_user_msg_error) + "(" + flag + ")"
                );
                break;
        }

        builder.show();
    }















    /************************************************************************
     *                                IDE 生成                               *
     ************************************************************************/

    /**
     * Id to identity READ_CONTACTS permission request.
     */
    private static final int REQUEST_READ_CONTACTS = 0;

    /**
     * A dummy authentication store containing known user names and passwords.
     * TODO: remove after connecting to a real authentication system.
     */
    private static final String[] DUMMY_CREDENTIALS = new String[]{
            "foo@example.com:hello", "bar@example.com:world"
    };
    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private UserLoginTask mLoginTask = null;


    private void populateAutoComplete() {
        if (!mayRequestContacts()) {
            return;
        }

        if (VERSION.SDK_INT >= 14) {
            // Use ContactsContract.Profile (API 14+)
            getLoaderManager().initLoader(0, null, this);
        } else if (VERSION.SDK_INT >= 8) {
            // Use AccountManager (API 8+)
            new SetupEmailAutoCompleteTask().execute(null, null);
        }
    }

    private boolean mayRequestContacts() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }
        if (checkSelfPermission(READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        if (shouldShowRequestPermissionRationale(READ_CONTACTS)) {
            Snackbar.make(mUserNameView, R.string.permission_rationale, Snackbar.LENGTH_INDEFINITE)
                    .setAction(android.R.string.ok, new View.OnClickListener() {
                        @Override
                        @TargetApi(Build.VERSION_CODES.M)
                        public void onClick(View v) {
                            requestPermissions(new String[]{READ_CONTACTS}, REQUEST_READ_CONTACTS);
                        }
                    });
        } else {
            requestPermissions(new String[]{READ_CONTACTS}, REQUEST_READ_CONTACTS);
        }
        return false;
    }

    /**
     * Callback received when a permissions request has been completed.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == REQUEST_READ_CONTACTS) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                populateAutoComplete();
            }
        }
    }




    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new CursorLoader(this,
                // Retrieve data rows for the device user's 'profile' contact.
                Uri.withAppendedPath(ContactsContract.Profile.CONTENT_URI,
                        ContactsContract.Contacts.Data.CONTENT_DIRECTORY), ProfileQuery.PROJECTION,

                // Select only email addresses.
                ContactsContract.Contacts.Data.MIMETYPE +
                        " = ?", new String[]{ContactsContract.CommonDataKinds.Email
                .CONTENT_ITEM_TYPE},

                // Show primary email addresses first. Note that there won't be
                // a primary email address if the user hasn't specified one.
                ContactsContract.Contacts.Data.IS_PRIMARY + " DESC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        List<String> users = new ArrayList<>();
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            users.add(cursor.getString(ProfileQuery.ADDRESS));
            cursor.moveToNext();
        }

        addEmailsToAutoComplete(users);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {

    }

    private void addEmailsToAutoComplete(List<String> emailAddressCollection) {
        //Create adapter to tell the AutoCompleteTextView what to show in its dropdown list.
        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(LoginActivity.this,
                        android.R.layout.simple_dropdown_item_1line, emailAddressCollection);

        mUserNameView.setAdapter(adapter);
    }

    private interface ProfileQuery {
        String[] PROJECTION = {
                ContactsContract.CommonDataKinds.Email.ADDRESS,
                ContactsContract.CommonDataKinds.Email.IS_PRIMARY,
        };

        int ADDRESS = 0;
        int IS_PRIMARY = 1;
    }


    /**
     * Use an AsyncTask to fetch the user's email addresses on a background thread, and update
     * the email text field with results on the main UI thread.
     */
    class SetupEmailAutoCompleteTask extends AsyncTask<Void, Void, List<String>> {

        @Override
        protected List<String> doInBackground(Void... voids) {
            ArrayList<String> emailAddressCollection = new ArrayList<>();

            // Get all emails from the user's contacts and copy them to a list.
            ContentResolver cr = getContentResolver();
            Cursor emailCur = cr.query(ContactsContract.CommonDataKinds.Email.CONTENT_URI, null,
                    null, null, null);
            while (emailCur.moveToNext()) {
                String email = emailCur.getString(emailCur.getColumnIndex(ContactsContract
                        .CommonDataKinds.Email.DATA));
                emailAddressCollection.add(email);
            }
            emailCur.close();

            return emailAddressCollection;
        }

        @Override
        protected void onPostExecute(List<String> emailAddressCollection) {
            addEmailsToAutoComplete(emailAddressCollection);
        }
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {

        private final String mUser;
        private final String mPassword;

        UserLoginTask(String user, String password) {
            mUser = user;
            mPassword = password;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            // TODO: attempt authentication against a network service.

            try {

                Thread.sleep(1000); // 扮连接网络
            } catch (InterruptedException e) {
                return false;
            }

            for (String credential : DUMMY_CREDENTIALS) {
                String[] pieces = credential.split(":");
                if (pieces[0].equals(mUser)) {
                    // Account exists, return true if the password matches.
                    return pieces[1].equals(mPassword);
                }
            }

            // TODO: register the new account here.

            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mLoginTask = null;
            showProgress(false);

            // 登陆成功后的处理
            if (success) {
                ((MainApplication)getApplicationContext()).setLogined(true);
                Log.i(TAG, "Success");
                gotoMainActivity();
            } else {
                mPasswordView.setError(getString(R.string.err_incorrect_password));
                mPasswordView.requestFocus();
            }
        }

        @Override
        protected void onCancelled() {
            mLoginTask = null;
            showProgress(false);
        }
    }

}

