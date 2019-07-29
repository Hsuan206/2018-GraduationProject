package com.tracy.treadsure;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by angoo on 2018/5/6.
 */

public class AddFriend extends AppCompatActivity {

    public class otherUser {
        private String ID;
        public void setID (String id){
            ID = id;
        }
        public String getID (){
            return ID;
        }

    }
    //使用者 // 好友邀請狀態 // 每個人的好友資料
    public DatabaseReference mUserDatabase,mFriendReqDatabase,mFriendDatabase,mNotificationDatabase,mRootRef,mObjectDatabase,mCollectionDatabase;

    public String user_id;
    FirebaseUser mCurrent_user;
    public String mCurrent_state = "不是朋友";

    AddFriend.otherUser id = new AddFriend.otherUser();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        // 創根目錄
        mRootRef = FirebaseDatabase.getInstance().getReference();

        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("User");
        // 邀請狀態
        mFriendReqDatabase = FirebaseDatabase.getInstance().getReference().child("Friend_req");
        // 好友資料
        mFriendDatabase = FirebaseDatabase.getInstance().getReference().child("Friends");
        // 通知
        mNotificationDatabase = FirebaseDatabase.getInstance().getReference().child("Notification");
        mObjectDatabase = FirebaseDatabase.getInstance().getReference().child("Object");
        mCurrent_user = FirebaseAuth.getInstance().getCurrentUser();


    };
    public void addFriend() {

        Toast.makeText(AddFriend.this,id.getID(),Toast.LENGTH_SHORT).show();

        user_id = id.getID();
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

                    } else if (req_type.equals("送出")) {

                        mCurrent_state = "已送出邀請";

                    }

                } else { // 如果不是那他們就是好友

                    mFriendDatabase.child(mCurrent_user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {

                            if(dataSnapshot.hasChild(user_id)) {

                                mCurrent_state = "朋友";
                            }


                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                        }
                    });

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        mCurrent_state = "不是朋友";
        // ------------ 還不是朋友 ------------
        if(mCurrent_state.equals("不是朋友")) {

            //接收者會收到通知 所以是user_id
            DatabaseReference newNotificationref = mRootRef.child("Notification").child(user_id).push();
            // 取得user_id
            String newNotificationId = newNotificationref.getKey();


            HashMap<String, String> notificationData = new HashMap<>();
            notificationData.put("from", mCurrent_user.getUid());
            notificationData.put("type", "請求");


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
                        Toast.makeText(AddFriend.this,"發送邀請錯誤",Toast.LENGTH_SHORT).show();

                    } else {

                        mCurrent_state = "已送出邀請";
                        //送出的button改變text
                        // mProfileSendReqBtn.setText("取消朋友邀請");
                    }
                    //因為我們在上面設false
                    //mProfileSendReqBtn.setEnabled(true);
                }

            });
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
                                //mProfileSendReqBtn.setEnabled(true);
                                mCurrent_state = "不是朋友";
                                //送出的button改變text
                                //mProfileSendReqBtn.setText("發送好友邀請");

                                //mDeclineBtn.setVisibility(View.INVISIBLE);
                                //mDeclineBtn.setEnabled(false);
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
                            mCurrent_state = "朋友";
                        }
                        else {

                            String error = databaseError.getMessage();
                            Toast.makeText(AddFriend.this,error,Toast.LENGTH_SHORT).show();


                        }
                    }
                });
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

                            }
                            else {

                                String error = databaseError.getMessage();
                                Toast.makeText(AddFriend.this,error,Toast.LENGTH_SHORT).show();


                            }
                        }
                    });

                }

            }

        }
        /*
        mObjectDatabase.child(id.getID()).child("user").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                user_id = dataSnapshot.getValue().toString();
                mUserDatabase = FirebaseDatabase.getInstance().getReference().child("User").child(user_id);

                mUserDatabase.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {




                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });*/

    }


}
