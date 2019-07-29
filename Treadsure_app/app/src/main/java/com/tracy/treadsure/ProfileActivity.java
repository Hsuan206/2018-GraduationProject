package com.tracy.treadsure;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity {

    private TextView mProfileName,mProfileStatus,mProfileFriendsCount;
    private ImageView mProfileImage;
    private Button mProfileSendReqBtn,mDeclineBtn;

    //使用者 // 好友邀請狀態 // 每個人的好友資料
    private DatabaseReference mUserDatabase,mFriendReqDatabase,mFriendDatabase,mNotificationDatabase,mRootRef;

    private FirebaseUser mCurrent_user;

    private ProgressDialog mProgressDialog;

    private String mCurrent_state;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        MyApplication.getInstance().addActivity(this);

        // 取得接收的使用者B的id // with UsersActivity
        final String user_id = getIntent().getStringExtra("user_id");
        // 創根目錄
        mRootRef = FirebaseDatabase.getInstance().getReference();

        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("User").child(user_id);
        // 邀請狀態
        mFriendReqDatabase = FirebaseDatabase.getInstance().getReference().child("Friend_req");
        // 好友資料
        mFriendDatabase = FirebaseDatabase.getInstance().getReference().child("Friends");
        // 通知
        mNotificationDatabase = FirebaseDatabase.getInstance().getReference().child("Notification");


        mCurrent_user = FirebaseAuth.getInstance().getCurrentUser();

        mProfileImage = (ImageView)findViewById(R.id.profile_image);
        mProfileName = (TextView) findViewById(R.id.profile_displayName);
        mProfileStatus = (TextView) findViewById(R.id.profile_status);
        mProfileSendReqBtn = (Button)findViewById(R.id.profile_send_req_btn);
        mDeclineBtn = (Button) findViewById(R.id.profile_decline_btn);

        mCurrent_state = "不是朋友";

        mDeclineBtn.setVisibility(View.INVISIBLE);
        mDeclineBtn.setEnabled(false);


        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setTitle("載入使用者資料");
        mProgressDialog.setMessage("請等待載入");
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.show();




        mUserDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String display_name = dataSnapshot.child("name").getValue().toString();
                String status = dataSnapshot.child("introduction").getValue().toString();
                mProfileName.setText(display_name);
                mProfileStatus.setText(status);
                if((mCurrent_user.getPhotoUrl())!=null||dataSnapshot.hasChild("image")) {
                    String image = dataSnapshot.child("image").getValue().toString();



                    Picasso.with(ProfileActivity.this).load(image).placeholder(R.drawable.user_person_before).into(mProfileImage);
                }
                if(mCurrent_user.getUid().equals(user_id)){

                    mDeclineBtn.setEnabled(false);
                    mDeclineBtn.setVisibility(View.INVISIBLE);

                    mProfileSendReqBtn.setEnabled(false);
                    mProfileSendReqBtn.setVisibility(View.INVISIBLE);

                }

                // ------------- 發送與接收好友邀請 --------------
                //取得此帳號的id
                mFriendReqDatabase.child(mCurrent_user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        //如果此帳號有接收者B的id
                        if(dataSnapshot.hasChild(user_id)) {
                            //取得B的請求類型
                            String req_type = dataSnapshot.child(user_id).child("請求類型").getValue().toString();

                            if (req_type.equals("接收")) {

                                mCurrent_state = "已接受邀請";
                                //送出的button改變text
                                mProfileSendReqBtn.setText("接受朋友邀請");

                                //接收者會看到拒絕好友邀請
                                mDeclineBtn.setVisibility(View.VISIBLE);
                                mDeclineBtn.setEnabled(true);

                            } else if (req_type.equals("送出")) {

                                mCurrent_state = "已送出邀請";
                                mProfileSendReqBtn.setText("取消朋友邀請");

                                //送出者不會看到拒絕好友邀請
                                mDeclineBtn.setVisibility(View.INVISIBLE);
                                mDeclineBtn.setEnabled(false);

                            }

                            mProgressDialog.dismiss();
                        } else { // 如果不是那他們就是好友

                            mFriendDatabase.child(mCurrent_user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {

                                    if(dataSnapshot.hasChild(user_id)) {

                                        mCurrent_state = "朋友";
                                        //送出的button改變text
                                        mProfileSendReqBtn.setText("解除好友");

                                        //成為好友後看到對方也會隱藏拒絕好友邀情
                                        mDeclineBtn.setVisibility(View.INVISIBLE);
                                        mDeclineBtn.setEnabled(false);

                                    }

                                    mProgressDialog.dismiss();

                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                    mProgressDialog.dismiss();
                                }
                            });

                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mProfileSendReqBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //點下button他就不能再點了
                mProfileSendReqBtn.setEnabled(false);

                // ------------ 還不是朋友 ------------
                if(mCurrent_state.equals("不是朋友")) {

                    //接收者會收到通知 所以是user_id
                    DatabaseReference newNotificationref = mRootRef.child("Notification").child(user_id).push();
                    // 取得user_id
                    String newNotificationId = newNotificationref.getKey();


                    HashMap<String, String> notificationData = new HashMap<>();
                    notificationData.put("from", mCurrent_user.getUid());
                    notificationData.put("type", "好友邀請");


                    Map requestsMap = new HashMap();
                    //請求好友A的 - 點選的使用者 - A的請求類型是送出
                    requestsMap.put("Friend_req/" + mCurrent_user.getUid() + "/" + user_id + "/請求類型","送出");
                    //接收者B - 送出好友邀請的A - B的請求類型是接收 // 跟上面剛好相反
                    requestsMap.put("Friend_req/" + user_id + "/" + mCurrent_user.getUid() + "/請求類型","接收");
                    requestsMap.put("Notification/" + user_id + "/" + newNotificationId,notificationData);

                    mRootRef.updateChildren(requestsMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                            if(databaseError != null) {
                                Toast.makeText(ProfileActivity.this,"發送邀請錯誤",Toast.LENGTH_SHORT).show();

                            } else {

                                mCurrent_state = "已送出邀請";
                                //送出的button改變text
                                mProfileSendReqBtn.setText("取消朋友邀請");
                            }
                            //因為我們在上面設false
                            mProfileSendReqBtn.setEnabled(true);

                        }
                    });

                }
                /* ---------- 還不是朋友方法一 太慢 -------------
                //請求好友A的 - 點選的使用者 - A的請求類型是送出
                    mFriendReqDatabase.child(mCurrent_user.getUid()).child(user_id).child("請求類型")
                            .setValue("送出").addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            if(task.isSuccessful()) {
                                //接收者B - 送出好友邀請的A - B的請求類型是接收 // 跟上面剛好相反
                                mFriendReqDatabase.child(user_id).child(mCurrent_user.getUid()).child("請求類型")
                                        .setValue("接收").addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {

                                        HashMap<String,String> notificationData = new HashMap<>();
                                        notificationData.put("from",mCurrent_user.getUid());
                                        notificationData.put("type","請求");

                                        //接收者會收到通知 所以是user_id
                                        mNotificationDatabase.child(user_id).push().setValue(notificationData).addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {

                                                mCurrent_state = "已送出邀請";
                                                //送出的button改變text
                                                mProfileSendReqBtn.setText("取消好友邀請");

                                                mDeclineBtn.setVisibility(View.INVISIBLE);
                                                mDeclineBtn.setEnabled(false);

                                                Toast.makeText(ProfileActivity.this,"送出成功",Toast.LENGTH_SHORT).show();

                                            }
                                        });

                                        }
                                });


                            } else {

                                Toast.makeText(ProfileActivity.this,"送出失敗",Toast.LENGTH_SHORT).show();
                            }
                            mProfileSendReqBtn.setEnabled(true);

                        }
                    });*/


                // -------------- 取消當朋友 --------------(與上面相反)
                if(mCurrent_state.equals("已送出邀請")) {
                    //請求者A - 刪除接收者B取消當好友
                    mFriendReqDatabase.child(mCurrent_user.getUid()).child(user_id).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {

                            //接收者B - 也要刪除使用者A好友
                            mFriendReqDatabase.child(user_id).child(mCurrent_user.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {

                                    //因此開放按鈕
                                    mProfileSendReqBtn.setEnabled(true);
                                    mCurrent_state = "不是朋友";
                                    //送出的button改變text
                                    mProfileSendReqBtn.setText("發送好友邀請");

                                    mDeclineBtn.setVisibility(View.INVISIBLE);
                                    mDeclineBtn.setEnabled(false);
                                }
                            });

                        }
                    });


                }

                // --------------- 確認邀請狀態 ----------------

                if(mCurrent_state.equals("已接受邀請")) {

                    final String currentDate = DateFormat.getDateTimeInstance().format(new Date());

                    Map friendsMap = new HashMap();
                    friendsMap.put("Friends/" + mCurrent_user.getUid() + "/" +user_id +"/date",currentDate);
                    friendsMap.put("Friends/" +user_id + "/" + mCurrent_user.getUid() +"/date",currentDate);

                    friendsMap.put("Friend_req/" + mCurrent_user.getUid() + "/" +user_id,null);
                    friendsMap.put("Friend_req/" + user_id + "/" + mCurrent_user.getUid(),null);

                    mRootRef.updateChildren(friendsMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                            if(databaseError == null ) {

                                //因此開放按鈕
                                mProfileSendReqBtn.setEnabled(true);
                                mCurrent_state = "朋友";
                                //送出的button改變text
                                mProfileSendReqBtn.setText("解除好友");

                                mDeclineBtn.setVisibility(View.INVISIBLE);
                                mDeclineBtn.setEnabled(false);

                            }
                            else {

                                String error = databaseError.getMessage();
                                Toast.makeText(ProfileActivity.this,error,Toast.LENGTH_SHORT).show();


                            }
                        }
                    });

                }
                /* ---------- 確認邀請狀態方法一 太慢 -------------
                    //要紀錄是何時成為好友
                    final String currentDate = DateFormat.getDateTimeInstance().format(new Date());
                    mFriendDatabase.child(mCurrent_user.getUid()).child(user_id).setValue(currentDate).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {

                            //此時的刪除是指成為好友所以刪除邀請的資料庫
                            mFriendDatabase.child(user_id).child(mCurrent_user.getUid()).setValue(currentDate).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {

                                    //請求者A - 刪除接收者B取消當好友
                                    mFriendReqDatabase.child(mCurrent_user.getUid()).child(user_id).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {

                                            //接收者B - 也要刪除使用者A好友
                                            mFriendReqDatabase.child(user_id).child(mCurrent_user.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {

                                                    //因此開放按鈕
                                                    mProfileSendReqBtn.setEnabled(true);
                                                    mCurrent_state = "朋友";
                                                    //送出的button改變text
                                                    mProfileSendReqBtn.setText("解除好友");

                                                    mDeclineBtn.setVisibility(View.INVISIBLE);
                                                    mDeclineBtn.setEnabled(false);

                                                }
                                            });

                                        }
                                    });


                                }
                            });

                        }
                    });*/

                // --------------- 解除好友 ----------------- (與上面相反)

                if(mCurrent_state.equals("朋友")) {

                    Map unfriendMap = new HashMap();
                    unfriendMap.put("Friends/" + mCurrent_user.getUid() + "/" + user_id,null);
                    unfriendMap.put("Friends/" + user_id + "/" + mCurrent_user.getUid(),null);

                    mRootRef.updateChildren(unfriendMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                            if(databaseError == null ) {

                                mCurrent_state = "不是朋友";
                                //送出的button改變text
                                mProfileSendReqBtn.setText("發送好友邀請");

                                mDeclineBtn.setVisibility(View.INVISIBLE);
                                mDeclineBtn.setEnabled(false);

                            }
                            else {

                                String error = databaseError.getMessage();
                                Toast.makeText(ProfileActivity.this,error,Toast.LENGTH_SHORT).show();


                            }

                            //因此開放按鈕
                            mProfileSendReqBtn.setEnabled(true);
                        }
                    });

                }
                /*  ---------- 解除好友方法一 太慢 -------------
                    //此時的刪除是好友所以刪除好友的資料庫
                    //請求者A - 刪除接收者B解除當好友
                    mFriendDatabase.child(mCurrent_user.getUid()).child(user_id).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            //接收者B - 刪除請求者A解除當好友
                            mFriendDatabase.child(user_id).child(mCurrent_user.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {

                                    //因此開放按鈕
                                    mProfileSendReqBtn.setEnabled(true);
                                    mCurrent_state = "不是朋友";
                                    //送出的button改變text
                                    mProfileSendReqBtn.setText("發送好友邀請");

                                    mDeclineBtn.setVisibility(View.INVISIBLE);
                                    mDeclineBtn.setEnabled(false);

                                }
                            });


                        }
                    });*/



            }
        });
        mDeclineBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //送出的button改變text
                mProfileSendReqBtn.setText("發送好友邀請");
                mFriendReqDatabase.child(mCurrent_user.getUid()).child(user_id).removeValue();
                mFriendReqDatabase.child(user_id).child(mCurrent_user.getUid()).removeValue();

            }
        });


    }
}
