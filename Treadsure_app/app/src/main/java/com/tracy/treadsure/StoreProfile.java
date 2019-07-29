package com.tracy.treadsure;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;



public class StoreProfile extends AppCompatActivity {

    EditText mTaxId,mPhone,mAddress,mStoreName;
    Button mSavebtn;
    FirebaseUser mCurrentUser;
    private DatabaseReference mStoreDatabase;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_store_profile);

        MyApplication.getInstance().addActivity(this);

        //Firebase User
        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        String current_uid = mCurrentUser.getUid();

        mStoreDatabase = FirebaseDatabase.getInstance().getReference().child("Store").child(current_uid);
        mStoreDatabase.keepSynced(true);

        mTaxId = (EditText) findViewById(R.id.taxId_input);
        mPhone = (EditText) findViewById(R.id.Phone_input);
        mStoreName = (EditText) findViewById(R.id.storename_input);
        mAddress = (EditText) findViewById(R.id.Address_input);
        mSavebtn = (Button) findViewById(R.id.save_btn);


        mSavebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Map<String, Object> user = new HashMap<>();
                user.put("taxId",mTaxId.getText().toString());
                user.put("storephone",mPhone.getText().toString());
                user.put("storename",mStoreName.getText().toString());
                user.put("address",mAddress.getText().toString());

                mStoreDatabase.updateChildren(user).addOnCompleteListener(new OnCompleteListener(){

                    @Override
                    public void onComplete(@NonNull Task task) {
                        Toast.makeText(StoreProfile.this,"註冊成功",Toast.LENGTH_SHORT).show();
                        finish();
                        startActivity(new Intent(StoreProfile.this,Store_navigation.class));
                    }

                });


            }
        });


    }
}

