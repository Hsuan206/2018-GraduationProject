package com.tracy.treadsure;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class ProfileSetting extends AppCompatActivity implements DialogInterface.OnClickListener {

    ImageView imageView;
    GetImage getImage;
    FirebaseAuth auth;
    FirebaseUser fuser;
    Spinner role_spinner, city_spinner;
    DatabaseReference mRootRef= FirebaseDatabase.getInstance().getReference(),mUserDatabase,mStoreDatabase;
    TextView setUserName_input,setEmail_input,setIntroduction_input,setCellphone_input;


    public static class User {
        public String name;
        public String email;
        public String role;
        public String introduction;
       // public String image;
        //public String thumb_image;
        public String device_token;
        public String cellphone;
        public String city;
//String image,String thumb_image,
        public User(String name, String email,String introduction,String role,String device_token,String cellphone,String city) {
            this.name=name;
            this.email=email;
            this.introduction=introduction;
            this.role=role;
            //this.image=image;
            //this.thumb_image=thumb_image;
            this.device_token=device_token;
            this.cellphone=cellphone;
            this.city=city;

        }

    }

    public void setProfile(View view) {
        new AlertDialog.Builder(this)
                .setMessage("確定要儲存基本資料?")
                .setNegativeButton("取消",null)
                .setPositiveButton("確認",this)
                .show();
    }

    @Override
    public void onClick(DialogInterface dialogInterface, int i) {
        if(i==DialogInterface.BUTTON_POSITIVE) {
            String[] role = getResources().getStringArray(R.array.role);
            int index = role_spinner.getSelectedItemPosition();
            String[] city = getResources().getStringArray(R.array.city);
            int cityindex = city_spinner.getSelectedItemPosition();

            //取得裝置的辨識符記，稱為「Token」是實作雲端訊息的第一步
            String deviceToken = FirebaseInstanceId.getInstance().getToken();
            //write user's profile to the database
            Map<String, Object> user = new HashMap<>();
            user.put(fuser.getUid(), new User(
                    setUserName_input.getText().toString(),
                    setEmail_input.getText().toString(),
                    setIntroduction_input.getText().toString(),
                    role[index],

                    deviceToken,
                    setCellphone_input.getText().toString(),
                    city[cityindex]));

           // mRootRef.child("User").updateChildren(user);
            //創Object UserID

            if (role[index].equals("一般使用者")) {

                mRootRef.child("User").updateChildren(user);
                if((fuser.getPhotoUrl())!=null) {
                    Map photoMap = new HashMap();
                    photoMap.put("image",fuser.getPhotoUrl().toString());
                    photoMap.put("thumb_image",fuser.getPhotoUrl().toString());
                    mRootRef.child("User").child(fuser.getUid()).setValue(photoMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(ProfileSetting.this, "設定成功", Toast.LENGTH_LONG).show();
                        }
                    });
                }
               /* Intent profileIntent = new Intent(ProfileSetting.this, Navigation.class);
                //放入接收者的user_id  // with ProfileActivity
                profileIntent.putExtra("username",setUserName_input.getText().toString());
                profileIntent.putExtra("useremail",setUserName_input.getText().toString());
                profileIntent.putExtra("userphoto",fuser.getPhotoUrl().toString());
                startActivity(profileIntent);*/
                startActivity(new Intent(ProfileSetting.this, Navigation.class));
            }
            else {
                mRootRef.child("Store").updateChildren(user);
                if((fuser.getPhotoUrl())!=null) {
                    Map photoMap = new HashMap();
                    photoMap.put("image",fuser.getPhotoUrl().toString());
                    photoMap.put("thumb_image",fuser.getPhotoUrl().toString());
                    mRootRef.child("Store").child(fuser.getUid()).setValue(photoMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(ProfileSetting.this, "設定成功", Toast.LENGTH_LONG).show();
                        }
                    });
                }
                startActivity(new Intent(ProfileSetting.this, StoreProfile.class));

            }
        }

    }


    private class GetImage extends AsyncTask<String , Integer , Bitmap> {

        @Override
        protected void onPreExecute() {
            //執行前 設定可以在這邊設定
            super.onPreExecute();
        }

        @Override
        protected Bitmap doInBackground(String... params) {
            //執行中 在背景做事情

            String urlStr = params[0];
            try {
                URL url = new URL(urlStr);
                return BitmapFactory.decodeStream(url.openConnection().getInputStream());
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            //執行中 可以在這邊告知使用者進度
            super.onProgressUpdate(values);

        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            //執行後 完成背景任務
            super.onPostExecute(bitmap);
            imageView.setImageBitmap(bitmap);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_setting);

        MyApplication.getInstance().addActivity(this);

        imageView=(ImageView)findViewById(R.id.header_image);
        role_spinner=(Spinner)findViewById(R.id.role_spinner);
        city_spinner=(Spinner)findViewById(R.id.city_spinner);
        setUserName_input=(EditText) findViewById(R.id.setUserName_input);
        setEmail_input=(EditText) findViewById(R.id.setEmail_input);
        setEmail_input.setFocusable(false);
        setEmail_input.setFocusableInTouchMode(false);
        setEmail_input.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(ProfileSetting.this,"email不可修改", Toast.LENGTH_LONG).show();
            }
        });
        setIntroduction_input=(EditText) findViewById(R.id.setIntroduction_input);
        setCellphone_input=(EditText) findViewById(R.id.setCellphone_input);

        auth=FirebaseAuth.getInstance();
        fuser=auth.getCurrentUser();
        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("User");
        mStoreDatabase = FirebaseDatabase.getInstance().getReference().child("Store");


        try{
            if(fuser.getPhotoUrl()!=null) {
            new GetImage().execute(fuser.getPhotoUrl().toString());
            }


        }catch (Exception e){
            Toast.makeText(ProfileSetting.this, e.getMessage(), Toast.LENGTH_LONG).show();

        }
        setUserName_input.setText(fuser.getDisplayName());
        setEmail_input.setText(fuser.getEmail());


    }
    @Override public boolean onKeyDown(int keyCode, KeyEvent event)  {
        if(keyCode == KeyEvent.KEYCODE_BACK){
            new android.app.AlertDialog.Builder(this)
                    .setTitle("結束程式")
                    .setMessage("退出將不儲存您的資料，需重新登入")
                    .setPositiveButton("是",new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            finish();
                            auth.signOut();
                            startActivity(new Intent(ProfileSetting.this, MainActivity.class));


                        }
                    })
                    .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {

                        }
                    })
                    .show();
            return true;
        }

        return super.onKeyDown(keyCode, event);

    }




}
