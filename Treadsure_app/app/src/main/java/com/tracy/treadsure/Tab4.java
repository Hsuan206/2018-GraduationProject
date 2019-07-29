package com.tracy.treadsure;

/**
 * Created by angoo on 2018/1/31.
 */

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;


public class Tab4 extends Fragment {

    private RecyclerView mConvList;

    private DatabaseReference mConvDatabase,mMessageDatabase,mUsersDatabase,mRootRef;
    private FirebaseAuth mAuth;
    private String mCurrent_user_id;
    private static String userName;
    private View mMainView;
    private static TextView userStatusView;
    public Tab4() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mMainView = inflater.inflate(R.layout.tab4, container, false);

        mConvList = (RecyclerView) mMainView.findViewById(R.id.conv_list);
        mAuth = FirebaseAuth.getInstance();

        mCurrent_user_id = mAuth.getCurrentUser().getUid();
        mRootRef = FirebaseDatabase.getInstance().getReference();
        mConvDatabase = FirebaseDatabase.getInstance().getReference().child("Chat").child(mCurrent_user_id);
        // offline load，會更快
        mConvDatabase.keepSynced(true);
        mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("User");
        mUsersDatabase.keepSynced(true);
        mMessageDatabase = FirebaseDatabase.getInstance().getReference().child("messages").child(mCurrent_user_id);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);

        mConvList.setHasFixedSize(true);
        mConvList.setLayoutManager(linearLayoutManager);

        // Inflate the layout for this fragment
        return mMainView;
    }

    @Override
    public void onStart() {
        super.onStart();

        // 因為需要知道最新的訊息是誰送的
        Query conversationQuery = mConvDatabase.orderByChild("timestamp");

        //使用FirebaseUI提供的FirebaseRecyclerAdapter類別快速生出adapter供RecyclerView顯示，不須實作太多方法即可同步Firebase資料。
        FirebaseRecyclerAdapter<Conv,ConvViewHolder> firebaseConvAdapter = new FirebaseRecyclerAdapter<Conv,ConvViewHolder>(
                Conv.class,
                R.layout.users_single_layout,
                ConvViewHolder.class,
                conversationQuery
        ) {
            @Override
            protected void populateViewHolder(final ConvViewHolder convViewHolder, final Conv conv, int position) {

                final String list_user_id = getRef(position).getKey();

                mUsersDatabase.child(list_user_id).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {


                        userName = dataSnapshot.child("name").getValue().toString();
                        if(dataSnapshot.hasChild("thumb_image")) {
                            String userThumb = dataSnapshot.child("thumb_image").getValue().toString();
                            convViewHolder.setUserImage(userThumb,getContext());
                        }
                        // 因為不是所有的User都有Online 所以用 hasChild
                        if(dataSnapshot.hasChild("online")) {

                            String userOnline = dataSnapshot.child("online").getValue().toString();
                            convViewHolder.setUserOnline(userOnline);

                        }
                        convViewHolder.setName(userName);

                        convViewHolder.mView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {

                                Intent chatIntent = new Intent(getContext(),ChatActivity.class);
                                //放入接收者的user_id  // with ChatActivity
                                chatIntent.putExtra("user_id",list_user_id);
                                chatIntent.putExtra("user_name",userName);
                                startActivity(chatIntent);

                            }
                        });

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

                // 取得最新的一則訊息
                Query lastMessageQuery = mMessageDatabase.child(list_user_id).limitToLast(1);

                lastMessageQuery.addChildEventListener(new com.google.firebase.database.ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {


                        String data = dataSnapshot.child("message").getValue().toString();
                        String from = dataSnapshot.child("from").getValue().toString();
                        // 是否被看過
                        convViewHolder.setMessage(data,conv.isSeen(),from,list_user_id);


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

        };
        //將Adapter設定給RecyclerView
        mConvList.setAdapter(firebaseConvAdapter);
        // 設置分割線
        //mConvList.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL));

        ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new
                CallBack(0, ItemTouchHelper.RIGHT,firebaseConvAdapter);

        ItemTouchHelper touchHelper = new ItemTouchHelper(simpleItemTouchCallback);
        // 加入到recycleView
        touchHelper.attachToRecyclerView(mConvList);

    }
    public static class ConvViewHolder extends RecyclerView.ViewHolder {
        View mView;

        public ConvViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
        }
        public void setMessage(String message,boolean isSeen,String from,String user_id) {

            // 顯示在View
            userStatusView = (TextView) mView.findViewById(R.id.user_single_status);
            if(message.indexOf("https://firebasestorage.googleapis.com/v0/b/fir-1-2b746.appspot.com/o/message_images")>=0) {
                if(!from.equals(user_id))
                    userStatusView.setText("傳送一張圖片");
                else
                    userStatusView.setText(userName+"傳送一張圖片");
            }
            else {
                userStatusView.setText(message);
            }

            // 是否已讀
            if(!isSeen) {
                userStatusView.setTypeface(userStatusView.getTypeface(), Typeface.BOLD);
            }
            else {
                userStatusView.setTypeface(userStatusView.getTypeface(), Typeface.NORMAL);
            }

        }
        public void setName(String name) {

            TextView userNameView = (TextView) mView.findViewById(R.id.user_single_name);
            userNameView.setText(name);
        }
        public void setUserImage(String thumb_images,Context ctx) {

            CircleImageView userImageView = (CircleImageView) mView.findViewById(R.id.user_single_image);
            //使用Picasso將圖片顯示在ImageView
            Picasso.with(ctx).load(thumb_images).placeholder(R.drawable.user_person_before).into(userImageView);

        }
        public void setUserOnline(String online_status) {

            ImageView userOnlineView = (ImageView) mView.findViewById(R.id.user_single_online_icon);

            if(online_status.equals("true")) {
                userOnlineView.setVisibility(View.VISIBLE);
            }
            else {
                userOnlineView.setVisibility(View.INVISIBLE);
            }

        }


    }
    public class CallBack extends ItemTouchHelper.SimpleCallback {

        // 使用FirebaseRecyclerAdapter
        private FirebaseRecyclerAdapter adapter;


        /**
         * pass我的FirebaseRecyclerAdapter
         */
        public CallBack(int dragDirs, int swipeDirs, FirebaseRecyclerAdapter adapter) {
            super(dragDirs, swipeDirs);
            this.adapter = adapter;
        }

        @Override
        public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
            return false;
        }

        @Override
        public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
            // 取得位置
            int position = viewHolder.getAdapterPosition();
            final String list_user_id = adapter.getRef(position).getKey();
            // 刪除好友
            //mRootRef.child("Chat").child(list_user_id).child(mCurrent_user_id).removeValue();
            mRootRef.child("Chat").child(mCurrent_user_id).child(list_user_id).removeValue();
            //mRootRef.child("messages").child(list_user_id).child(mCurrent_user_id).removeValue();
            mRootRef.child("messages").child(mCurrent_user_id).child(list_user_id).removeValue();
            Toast.makeText(getActivity(), "刪除聊天成功", Toast.LENGTH_SHORT).show();

        }
        @Override
        public void onChildDraw (Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive){
            Bitmap icon;
            Paint p = new Paint();
            if(actionState == ItemTouchHelper.ACTION_STATE_SWIPE){

                View itemView = viewHolder.itemView;
                float height = (float) itemView.getBottom() - (float) itemView.getTop();
                float width = height / 3;
                itemView.setAlpha(1 - Math.abs(dX) / 1080);
                if(dX > 0){
                    p.setColor(Color.parseColor("#D32F2F"));
                    // 重新定義一個矩形
                    RectF background = new RectF((float) itemView.getLeft(), (float) itemView.getTop(), dX,(float) itemView.getBottom());

                    // 畫布上畫個上了顏色的矩形
                    c.drawRect(background,p);
                    //取得icon
                    icon = BitmapFactory.decodeResource(getResources(), R.mipmap.trashcan);

                    RectF icon_dest = new RectF((float) itemView.getLeft() + width ,(float) itemView.getTop() + width,(float) itemView.getLeft()+ 2*width,(float)itemView.getBottom() - width);
                    c.drawBitmap(icon,null,icon_dest,p);
                } /*else {
                    p.setColor(Color.parseColor("#D32F2F"));
                    RectF background = new RectF((float) itemView.getRight() + dX, (float) itemView.getTop(),(float) itemView.getRight(), (float) itemView.getBottom());
                    c.drawRect(background,p);
                    icon = BitmapFactory.decodeResource(getResources(), R.drawable.p);
                    RectF icon_dest = new RectF((float) itemView.getRight() - 2*width ,(float) itemView.getTop() + width,(float) itemView.getRight() - width,(float)itemView.getBottom() - width);
                    c.drawBitmap(icon,null,icon_dest,p);
                }*/
            }
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);

        }


    }

}
