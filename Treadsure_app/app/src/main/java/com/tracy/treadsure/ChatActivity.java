package com.tracy.treadsure;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {

    private String mChatUser;
    private Toolbar mChatToolbar;

    private DatabaseReference mRootRef;

    private TextView mTitleView,mLastSeenView;
    private CircleImageView mProfileImage;
    private FirebaseAuth mAuth;
    private String mCurrentUserId;

    private ImageButton mChatAddBtn;
    private ImageButton mChatSendBtn;
    private EditText mChatMessageView;

    private RecyclerView mMessagesList;
    private SwipeRefreshLayout mRefreshLayout;

    private final List<Messages> messagesList = new ArrayList<>();
    private LinearLayoutManager mLinearLayout;
    // from MessageAdapter.java 負責把 Dataset 裡面的資料，轉成 view 給 RecyclerView 顯示
    private MessageAdapter mAdapter;


    private static final int TOTAL_ITEMS_TO_LOAD = 10;
    private int mCurrentPage = 1;

    // 新解決方案:解決順序顛倒狀況
    private int itemPos = 0;
    // 最新的message
    private String mLastKey = "";
    // 解決重複的message
    private String mPrevKey = "";

    private static final int GALLERY_PICK = 1;

    // Storage Firebase
    private StorageReference mImageStorage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        MyApplication.getInstance().addActivity(this);
        mChatToolbar = (Toolbar) findViewById(R.id.chat_appBar);
        setSupportActionBar(mChatToolbar);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("");
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);

        mRootRef = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        mCurrentUserId = mAuth.getCurrentUser().getUid();
        //Strorage firebase
        mImageStorage = FirebaseStorage.getInstance().getReference();

        // from FriendsFragment.java
        mChatUser = getIntent().getStringExtra("user_id");
        String userName = getIntent().getStringExtra("user_name");

        //把 xml 表述的 layout 轉化為 View
        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View action_bar_view = inflater.inflate(R.layout.chat_custom_bar,null);
        actionBar.setCustomView(action_bar_view);

        // --------- 客製化ActionBar ------------

        mTitleView = (TextView) findViewById(R.id.custom_bar_title);
        mLastSeenView = (TextView) findViewById(R.id.custom_bar_seen);
        mProfileImage = (CircleImageView) findViewById(R.id.custom_bar_image);

        mChatAddBtn = (ImageButton) findViewById(R.id.chat_add_btn);
        mChatSendBtn = (ImageButton) findViewById(R.id.chat_send_btn);
        mChatMessageView = (EditText) findViewById(R.id.chat_message_view);

        mMessagesList = (RecyclerView) findViewById(R.id.messages_list);
        mRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.message_swipe_layout);

        // ---------- Adapter 將View轉成RycycleView ----------
        // 已經將Messages 資料放進list裡面
        mAdapter = new MessageAdapter(messagesList);
        mLinearLayout = new LinearLayoutManager(this);
        mMessagesList.setHasFixedSize(true);
        mMessagesList.setLayoutManager(mLinearLayout);
        //將Adapter設定給RecyclerView
        mMessagesList.setAdapter(mAdapter);

        mRootRef.child("Chat").child(mCurrentUserId).child(mChatUser).child("seen").setValue(true);

        loadMessages();


        mTitleView.setText(userName);

        // --------- 取得對方的上線情況 ----------
        mRootRef.child("User").child(mChatUser).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                String online = dataSnapshot.child("online").getValue().toString();
                if(dataSnapshot.hasChild("image")) {
                    String image = dataSnapshot.child("image").getValue().toString();
                    Picasso.with(ChatActivity.this).load(image).placeholder(R.drawable.user_person_before).into(mProfileImage);
                }
                if(online.equals("true")) {

                    mLastSeenView.setText("Online");
                } else  {
                    // 如果離線，使用者的online會變成timestamp // from MainActivity.java

                    // with GetTimeAgo.java
                    GetTimeAgo getTimeAgo = new GetTimeAgo();
                    // 將上一次上線的timestamp轉換成long
                    long lastTime = Long.parseLong(online);


                    // 轉換成幾天前幾分前上線
                    String lastSeenTime = getTimeAgo.getTimeAgo(lastTime,getApplicationContext());
                    mLastSeenView.setText(lastSeenTime);
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        // ----------- 新增node ---------------
        mRootRef.child("Chat").child(mCurrentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // 如果此使用者沒有跟接收者聊天
                if(!dataSnapshot.hasChild(mChatUser)) {

                    Map chatAddMap = new HashMap();
                    chatAddMap.put("seen",false);
                    chatAddMap.put("timestamp", ServerValue.TIMESTAMP);

                    Map chatUserMap = new HashMap();
                    chatUserMap.put("Chat/" + mCurrentUserId + "/" + mChatUser,chatAddMap);
                    chatUserMap.put("Chat/" +mChatUser + "/" + mCurrentUserId,chatAddMap);
                    mRootRef.updateChildren(chatUserMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                            if(databaseError != null){

                                Log.d("CHAT_LOG", databaseError.getMessage().toString());

                            }
                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        // ----------- 送出訊息 ---------------
        mChatSendBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                sendMessage();
            }
        });

        // 每Refresh一次Page就增加一次page
        mRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                // 但一直增加的時候，更新時會在第二次時Load 20則
                mCurrentPage++;
                // 從list移除但若再loadMessages()會造成View重複出現message
                //messagesList.clear();

                // 每次load新的page時，就把message position 歸零
                itemPos = 0;

                loadMoreMessages();

            }
        });
        // -------------- 新增/上傳相片 ------------------

        mChatAddBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent galleryIntent = new Intent();
                galleryIntent.setType("image/*");
                //系統幫使用者找到裝置內合適的App來取得指定MIME類型的內容
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                //跳出選單讓使用者選擇要使用哪個
                startActivityForResult(Intent.createChooser(galleryIntent,"選擇圖片"),GALLERY_PICK);

            }
        });


    }

    // 新增/上傳相片接收結果
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == GALLERY_PICK && resultCode == RESULT_OK){

            Uri imageUri = data.getData();

            final String current_user_ref = "messages/" + mCurrentUserId + "/" + mChatUser;
            final String chat_user_ref = "messages/" + mChatUser + "/" + mCurrentUserId;

            // push 新增資料使用 (會參考到一個新的路徑，可以getkey or set Data)
            DatabaseReference user_message_push = mRootRef.child("messages")
                    .child(mCurrentUserId).child(mChatUser).push();

            // 取得push 進去的unique ID (基於timestamp隨機生成，代表每一個資料點)
            final String push_id = user_message_push.getKey();

            //取得圖片路徑
            StorageReference filepath = mImageStorage.child("message_images").child( push_id + ".jpg");
            //上傳圖片
            filepath.putFile(imageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                    if(task.isSuccessful()){

                        // 取得圖片的URL
                        String download_url = task.getResult().getDownloadUrl().toString();


                        Map messageMap = new HashMap();
                        messageMap.put("message", download_url);
                        messageMap.put("seen", false);
                        // 傳送圖片
                        messageMap.put("type", "image");
                        messageMap.put("time", ServerValue.TIMESTAMP);
                        // 來自發送人
                        messageMap.put("from", mCurrentUserId);


                        // 可看成 messages - mCurrentUserId - mChatUser - push_id - message
                        Map messageUserMap = new HashMap();
                        messageUserMap.put(current_user_ref + "/" + push_id, messageMap);
                        messageUserMap.put(chat_user_ref + "/" + push_id, messageMap);

                        mChatMessageView.setText("");

                        mRootRef.updateChildren(messageUserMap, new DatabaseReference.CompletionListener() {
                            @Override
                            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                                if(databaseError != null){

                                    Log.d("CHAT_LOG", databaseError.getMessage().toString());

                                }

                            }
                        });


                    }

                }
            });

        }

    }

    // 每次Refresh時執行，更新多次
    private void loadMoreMessages() {

        DatabaseReference messageRef = mRootRef.child("messages").child(mCurrentUserId).child(mChatUser);

        // endAt(A) A是最後一個，從A個往前的All，limitToLast指往前10個 (包含mLastKey)，limitToFirst 則前面的10個
        Query messageQuery = messageRef.orderByKey().endAt(mLastKey).limitToLast(10);
        messageQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                //取得messages下的資料
                Messages message = dataSnapshot.getValue(Messages.class);
                // 取得此message的key
                String messageKey = dataSnapshot.getKey();

                // 解決重複message，因為LimitToLast會包含mLastKey自己
                // 如果mPrekey 和 messageKey相同代表此message是重複的，這裡是不重複
                if(!mPrevKey.equals(messageKey)) {

                    // 把message 放置第0個位置，每次Refresh會都放在最上面，因此順序會顛倒
                    // 解決方法: itemPos 每次Refresh一則訊息就+1，將從第一則往下排，一直到第10
                    messagesList.add(itemPos++,message);

                } else {

                    mPrevKey = mLastKey;

                }

                // 代表是第一個被load的資料
                if(itemPos == 1) {

                    mLastKey = messageKey;
                }

                Log.d("所有KEY","Last Key" + mLastKey + " | Prev Key" + mPrevKey + " | Message Key" + messageKey);

                // 通知資料被變動，更新 ListView 顯示內容
                mAdapter.notifyDataSetChanged();

                mRefreshLayout.setRefreshing(false);

                // scroll 到指定的位置，並置頂顯示 (第幾個,距離多少px)
                mLinearLayout.scrollToPositionWithOffset(10,0);

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    // 準備產生Activity 呼叫 loadMessages() 更新資料，只更新一次
    private void loadMessages() {

        DatabaseReference messageRef = mRootRef.child("messages").child(mCurrentUserId).child(mChatUser);

        // 限制只顯示最後的十條，若要載入之前10條就要swipeView
        // Query: 排序或是過濾資料
        // 每Run一次就會更新一次
        Query messageQuery = messageRef.limitToLast(mCurrentPage * TOTAL_ITEMS_TO_LOAD);

        //要可以進行remove 所以用addChildEventListener
        messageQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                //取得messages下的資料
                Messages message = dataSnapshot.getValue(Messages.class);

                itemPos++;

                // 代表是第一個被load的資料
                if(itemPos == 1) {

                    String messageKey = dataSnapshot.getKey();
                    mLastKey = messageKey;
                    mPrevKey = messageKey;

                }

                messagesList.add(message);
                // 通知資料被變動，更新 ListView 顯示內容
                mAdapter.notifyDataSetChanged();

                // 使View scroll至最下面剛打字的地方
                mMessagesList.scrollToPosition(messagesList.size()-1);

                mRefreshLayout.setRefreshing(false);

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private void sendMessage() {

        String message = mChatMessageView.getText().toString();
        // 如果打入文字不是空的
        if(!TextUtils.isEmpty(message)) {

            String current_user_ref = "messages/" + mCurrentUserId + "/" + mChatUser;
            String chat_user_ref = "messages/" + mChatUser + "/" + mCurrentUserId;


            // push 新增資料使用 (會參考到一個新的路徑，可以getkey or set Data)
            DatabaseReference user_message_push = mRootRef.child("messages")
                    .child(mCurrentUserId).child(mChatUser).push();

            // 取得push 進去的unique ID (基於timestamp隨機生成，代表每一個資料點)
            String push_id = user_message_push.getKey();

            Map messageMap = new HashMap();
            messageMap.put("message", message);
            messageMap.put("seen",false);
            //可以是text或圖片
            messageMap.put("type","text");
            messageMap.put("time", ServerValue.TIMESTAMP);
            // 可知是誰送出訊息
            messageMap.put("from",mCurrentUserId);

            // 已送出訊息就清空messageView
            mChatMessageView.setText("");

            // 可看成 messages - mCurrentUserId - mChatUser - push_id - message
            Map messageUserMap = new HashMap();
            messageUserMap.put(current_user_ref + "/" + push_id, messageMap);
            messageUserMap.put(chat_user_ref + "/" +push_id,messageMap);

            mRootRef.child("Chat").child(mCurrentUserId).child(mChatUser).child("seen").setValue(true);
            mRootRef.child("Chat").child(mCurrentUserId).child(mChatUser).child("timestamp").setValue(ServerValue.TIMESTAMP);

            mRootRef.child("Chat").child(mChatUser).child(mCurrentUserId).child("seen").setValue(false);
            mRootRef.child("Chat").child(mChatUser).child(mCurrentUserId).child("timestamp").setValue(ServerValue.TIMESTAMP);


            //同步更新messages
            mRootRef.updateChildren(messageUserMap, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                    if(databaseError != null){

                        Log.d("CHAT_LOG", databaseError.getMessage().toString());

                    }

                }
            });

        }

    }
}
