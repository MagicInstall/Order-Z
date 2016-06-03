package com.magicinstall.service.order_z;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.magicinstall.library.Define;

public class CompanyInfoActivity extends AppCompatActivity {
    private static final String TAG = "CompanyInfoActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_company_info);

        // 用传入的ID 判断是新建定系编辑
        Intent intent = getIntent();
        boolean isEdition = intent.getIntExtra(Define.DATABASE_COMPANY_ID, -1) > 0;

        TextView name_txt = (TextView)findViewById(R.id.EditCompanyName);
        if (isEdition) name_txt.setText(intent.getStringExtra(Define.DATABASE_COMPANY_NAME));
        name_txt.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) nameLostFocus();
            }
        });

        TextView sub_name_txt = (TextView)findViewById(R.id.EditCompanySubName);
        if (isEdition) sub_name_txt.setText(intent.getStringExtra(Define.DATABASE_COMPANY_SUBNAME));
        sub_name_txt.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) subNameLostFocus();
            }
        });

        View user_manage_btn = findViewById(R.id.UsersManage);
        user_manage_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gotoUserManage();
            }
        });

        Button delete_btn = (Button)findViewById(R.id.CompanyDelete);
        delete_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                delete();
            }
        });


    }

    private void nameLostFocus(){
        Log.v(TAG, "nameLostFocus");
    }

    private void subNameLostFocus(){
        Log.v(TAG, "subNameLostFocus");
    }

    private void gotoUserManage(){
        Log.v(TAG, "gotoUserManage)");
        // TODO: 跳转
    }

    /**
     * 删除企业
     */
    private void delete(){
        // TODO: 弹出Owner 的验证界面
    }
}
