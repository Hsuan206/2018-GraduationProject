package com.tracy.treadsure;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class Store_StatusActivity extends AppCompatActivity {
    private Toolbar mToolbar;
    private EditText mEmail,mStore_Name,mStoreName,mStoreCellphone,mStoreLocate,mStorePhone,mTaxId;
    private Button mSavebtn;

    private DatabaseReference mStatusDatabase;
    private FirebaseUser mCurrentUser;

    private ProgressDialog mProgress;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_store_status);
        MyApplication.getInstance().addActivity(this);

        //Firebase
        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        String current_uid = mCurrentUser.getUid();
        mStatusDatabase = FirebaseDatabase.getInstance().getReference().child("Store").child(current_uid);


        mToolbar = (Toolbar)findViewById(R.id.store_status_appBar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("個人資料");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //from Store_account_setting


        String name_value = getIntent().getStringExtra("name_value");
        String storename_value = getIntent().getStringExtra("storename_value");
        String taxId_value = getIntent().getStringExtra("taxId_value");
        String email_value = getIntent().getStringExtra("email_value");
        String cellphone_value = getIntent().getStringExtra("cellphone_value");
        String locate_value = getIntent().getStringExtra("locate_value");
        String phone_value = getIntent().getStringExtra("phone_value");



        mTaxId = (EditText) findViewById(R.id.store_taxId_input);
        mTaxId.setFocusable(false);
        mTaxId.setFocusableInTouchMode(false);
        mTaxId.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(Store_StatusActivity.this,"統一編號不可更改",Toast.LENGTH_SHORT).show();
            }
        });
        mEmail = (EditText) findViewById(R.id.store_email_input);
        mEmail.setFocusable(false);
        mEmail.setFocusableInTouchMode(false);
        mEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(Store_StatusActivity.this,"email不可更改",Toast.LENGTH_SHORT).show();
            }
        });
        mStore_Name = (EditText) findViewById(R.id.store_name_input);
        mStoreName = (EditText) findViewById(R.id.storename_input);
        mStoreName.setFocusable(false);
        mStoreName.setFocusableInTouchMode(false);
        mStoreName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(Store_StatusActivity.this,"店名不可更改",Toast.LENGTH_SHORT).show();
            }
        });
        mStoreCellphone = (EditText) findViewById(R.id.store_cellphone_input);
        mStoreCellphone.setFocusable(false);
        mStoreCellphone.setFocusableInTouchMode(false);
        mStoreCellphone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(Store_StatusActivity.this,"手機不可更改",Toast.LENGTH_SHORT).show();
            }
        });
        mStoreLocate = (EditText) findViewById(R.id.store_locate_input);
        mStorePhone = (EditText) findViewById(R.id.store_phone_input);

        mSavebtn = (Button)findViewById(R.id.store_status_save_btn);

        //將值放在layout中

        mTaxId.setText(taxId_value);
        mEmail.setText(email_value);
        mStore_Name.setText(name_value);
        mStoreName.setText(storename_value);
        mStoreCellphone.setText(cellphone_value);
        mStoreLocate.setText(locate_value);
        mStorePhone.setText(phone_value);



        mSavebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Progress
                mProgress = new ProgressDialog(Store_StatusActivity.this);
                mProgress.setTitle("保存中");
                mProgress.setMessage("等待保存中");
                mProgress.show();

                //更新資料庫
                mStatusDatabase.child("name").setValue(mStore_Name.getText().toString());
                mStatusDatabase.child("cellphone").setValue(mStoreCellphone.getText().toString());
                mStatusDatabase.child("email").setValue(mEmail.getText().toString());
                mStatusDatabase.child("locate").setValue(mStoreLocate.getText().toString());
                mStatusDatabase.child("phone").setValue(mStorePhone.getText().toString());


                mStatusDatabase.child("taxId").setValue(mTaxId.getText().toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()) {

                            mProgress.dismiss();
                            Toast.makeText(getApplicationContext(),"保存成功",Toast.LENGTH_LONG).show();

                        } else {
                            Toast.makeText(getApplicationContext(),"error",Toast.LENGTH_LONG).show();
                        }
                    }
                });

            }
        });
    }
}
