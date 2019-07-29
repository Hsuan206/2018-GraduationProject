package com.tracy.treadsure;

import android.app.ProgressDialog;
import android.graphics.drawable.Drawable;
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


public class StatusActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private EditText mStatus,mEmail,mUserName,mUserCellphone,mUserLocate;
    private Button mSavebtn;

    private DatabaseReference mStatusDatabase;
    private FirebaseUser mCurrentUser;

    private ProgressDialog mProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_status);
        MyApplication.getInstance().addActivity(this);


        //Firebase
        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        String current_uid = mCurrentUser.getUid();
        mStatusDatabase = FirebaseDatabase.getInstance().getReference().child("User").child(current_uid);


        mToolbar = (Toolbar)findViewById(R.id.status_appBar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("個人資料");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //from Account_setting

        String status_value = getIntent().getStringExtra("status_value");
        String name_value = getIntent().getStringExtra("name_value");
        String email_value = getIntent().getStringExtra("email_value");
        String cellphone_value = getIntent().getStringExtra("cellphone_value");
        String locate_value = getIntent().getStringExtra("locate_value");




        mStatus = (EditText) findViewById(R.id.status_input);

        mEmail = (EditText) findViewById(R.id.email_input);
        mEmail.setFocusable(false);
        mEmail.setFocusableInTouchMode(false);
        mEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(StatusActivity.this,"email不可更改",Toast.LENGTH_SHORT).show();
            }
        });
        mUserName = (EditText) findViewById(R.id.name_input);

        mUserCellphone = (EditText) findViewById(R.id.cellphone_input);
        mUserCellphone.setFocusable(false);
        mUserCellphone.setFocusableInTouchMode(false);
        mUserCellphone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(StatusActivity.this,"手機不可更改",Toast.LENGTH_SHORT).show();
            }
        });
        mUserLocate = (EditText) findViewById(R.id.locate_input);

        mSavebtn = (Button)findViewById(R.id.status_save_btn);

        //將值放在layout中
        mStatus.setText(status_value);
        mEmail.setText(email_value);
        mUserName.setText(name_value);
        mUserCellphone.setText(cellphone_value);
        mUserLocate.setText(locate_value);



        mSavebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Progress
                mProgress = new ProgressDialog(StatusActivity.this);
                mProgress.setTitle("保存中");
                mProgress.setMessage("等待保存中");
                mProgress.show();

                //更新資料庫
                mStatusDatabase.child("name").setValue(mUserName.getText().toString());
                mStatusDatabase.child("cellphone").setValue(mUserCellphone.getText().toString());
                mStatusDatabase.child("email").setValue(mEmail.getText().toString());
                mStatusDatabase.child("locate").setValue(mUserLocate.getText().toString());

                String status = mStatus.getText().toString();
                mStatusDatabase.child("introduction").setValue(status).addOnCompleteListener(new OnCompleteListener<Void>() {
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
