package com.tracy.treadsure;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;

public class Store_account_setting extends AppCompatActivity {

    //Firebase
    private DatabaseReference mStoreDatabase;
    private FirebaseUser mCurrentUser;

    //Android layout
    private CircleImageView mDisplayImage;
    private TextView mName;
    private Button mStatusBtn,mImageBtn;

    //request Code
    private static final int GALLERY_PICK = 1;

    //Strorage firebase
    private StorageReference mImageStorage;

    private ProgressDialog mProgressDialog;

    String name,cellphone,email,locate,taxId,phone,storename;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_store_account_setting);
        MyApplication.getInstance().addActivity(this);

       Toolbar toolbar = (Toolbar) findViewById(R.id.Store_account_appBar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("帳號設定");
        //讓他有返回鍵
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mDisplayImage = (CircleImageView) findViewById(R.id.Store_settings_image);
        mName = (TextView) findViewById(R.id.Store_settings_name);
        mStatusBtn = (Button) findViewById(R.id.Store_settings_status_btn);
        mImageBtn = (Button) findViewById(R.id.Store_settings_image_btn);

        //Strorage firebase
        mImageStorage = FirebaseStorage.getInstance().getReference();
        //Firebase User
        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        String current_uid = mCurrentUser.getUid();

        mStoreDatabase = FirebaseDatabase.getInstance().getReference().child("Store").child(current_uid);
        //離線使用firebase以及使圖片文字load進出現沒有延遲 Chat.class
        mStoreDatabase.keepSynced(true); //只同步String及integer沒有圖片

        mStoreDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                name = dataSnapshot.child("name").getValue().toString();
                storename = dataSnapshot.child("storename").getValue().toString();
                cellphone = dataSnapshot.child("cellphone").getValue().toString();
                email = dataSnapshot.child("email").getValue().toString();
                locate = dataSnapshot.child("city").getValue().toString();
                taxId = dataSnapshot.child("taxId").getValue().toString();
                phone = dataSnapshot.child("storephone").getValue().toString();
                if(dataSnapshot.hasChild("image")) {
                    final String image = dataSnapshot.child("image").getValue().toString();
                    //String thumb_status = dataSnapshot.child("thumb_image").getValue().toString();
                    if(!image.equals("default")) {
                        //把image的URL載入進ImageView中
                        //Picasso.with(SettingsActivity.this).load(image).placeholder(R.drawable.user_person_before).into(mDisplayImage);
                        Picasso.with(Store_account_setting.this).load(image).networkPolicy(NetworkPolicy.OFFLINE)
                                .placeholder(R.mipmap.nav_account).into(mDisplayImage, new Callback() {
                            @Override
                            public void onSuccess() {


                            }

                            @Override
                            public void onError() {

                                Picasso.with(Store_account_setting.this).load(image).placeholder(R.mipmap.nav_account).into(mDisplayImage);
                            }
                        });
                    }
                }
                mName.setText(name);



            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mStatusBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                String name_value = mName.getText().toString();

                Intent status_intent = new Intent(Store_account_setting.this,Store_StatusActivity.class);

                status_intent.putExtra("name_value",name_value);
                status_intent.putExtra("storename_value",storename);
                status_intent.putExtra("taxId_value",taxId);
                status_intent.putExtra("cellphone_value",cellphone);
                status_intent.putExtra("email_value",email);
                status_intent.putExtra("locate_value",locate);
                status_intent.putExtra("phone_value",phone);
                startActivity(status_intent);

            }
        });

        mImageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {



                Intent galleryIntent = new Intent();
                galleryIntent.setType("image/*");
                //系統幫使用者找到裝置內合適的App來取得指定MIME類型的內容
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                //跳出選單讓使用者選擇要使用哪個
                startActivityForResult(Intent.createChooser(galleryIntent,"選擇圖片"),GALLERY_PICK);


                // start picker to get image for cropping and then use the image in cropping activity
               // CropImage.activity()
                  //      .setGuidelines(CropImageView.Guidelines.ON)
                  //      .start(SettingsActivity.this);


            }
        });


    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == GALLERY_PICK && resultCode == RESULT_OK) {

            Uri imageUri = data.getData();

            // 裁剪存在手機上的圖片
            CropImage.activity(imageUri)
                    .setAspectRatio(1,1) //1:1寬高
                    .start(this);

        }
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {

            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {

                mProgressDialog = new ProgressDialog(Store_account_setting.this);
                mProgressDialog.setTitle("上傳圖片中");
                mProgressDialog.setMessage("請等待上傳");
                mProgressDialog.setCanceledOnTouchOutside(false);
                mProgressDialog.show();

                //裁剪的image uri
                Uri resultUri = result.getUri();
                //以userid名字存入當圖片名下次就可以取代
                String current_user_id = mCurrentUser.getUid();
                final File thumb_filePath = new File(resultUri.getPath());
                //壓縮Big image
                Bitmap thumb_bitmap = new Compressor(this)
                        .setMaxWidth(200)
                        .setMaxHeight(200)
                        .setQuality(75)
                        .compressToBitmap(thumb_filePath);
                //上傳記憶體的資料
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                thumb_bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                final byte[] thumb_byte = baos.toByteArray();


                //取得圖片路徑
                StorageReference filepath = mImageStorage.child("profile_image").child(current_user_id+".jpg");
                //加到快取的資料夾
                final StorageReference thumb_filepath = mImageStorage.child("profile_image").child("thumbs").child(current_user_id+".jpg");



                //上傳圖片
                filepath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                        if(task.isSuccessful()) {
                            //取得圖片的URL
                            final String download_url = task.getResult().getDownloadUrl().toString();

                            UploadTask uploadTask = thumb_filepath.putBytes(thumb_byte);
                            uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> thumb_task) {

                                    String thumb_downloadUrl = thumb_task.getResult().getDownloadUrl().toString();

                                    if(thumb_task.isSuccessful()) {

                                        Map update_hashMap = new HashMap<>();
                                        update_hashMap.put("image",download_url);
                                        update_hashMap.put("thumb_image",thumb_downloadUrl);

                                        //database底下的image
                                        //原本有.child("image")
                                        mStoreDatabase.updateChildren(update_hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if(task.isSuccessful()) {
                                                    mProgressDialog.dismiss();
                                                    Toast.makeText(Store_account_setting.this,"上傳成功",Toast.LENGTH_LONG).show();

                                                }
                                            }
                                        });

                                    } else {

                                        Toast.makeText(Store_account_setting.this,"上傳快取失敗",Toast.LENGTH_LONG).show();
                                        mProgressDialog.dismiss();

                                    }

                                }
                            });




                        } else {

                            Toast.makeText(Store_account_setting.this,"上傳失敗",Toast.LENGTH_LONG).show();
                            mProgressDialog.dismiss();
                        }

                    }
                });

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {

                Exception error = result.getError();

            }
        }
    }

    //隨機產生字串符
    public static String random() {
        Random generator = new Random();
        StringBuilder randomStringBuilder = new StringBuilder();
        //隨機產生1~10
        int randomLength = generator.nextInt(10);
        char tempChar;
        for (int i = 0; i < randomLength; i++){
            tempChar = (char) (generator.nextInt(96) + 32);
            randomStringBuilder.append(tempChar);
        }
        return randomStringBuilder.toString();
    }
}
