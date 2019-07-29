package com.tracy.treadsure;

/**
 * Created by angoo on 2018/1/31.
 */

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.ChildEventListener;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

public class Tab2 extends Fragment {
    private RecyclerView mCollectionList;
    private DatabaseReference mCouponCollectionDatabase, mCouponDatabase,mStoreDatabase,mRootRef;
    private FirebaseAuth mAuth;
    private String mCurrent_user_id,title,content,user;
    private View mMainView;
    TextView txv_title,txv_content,txv_creator;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mMainView = inflater.inflate(R.layout.tab2, container, false);



        mCollectionList = (RecyclerView) mMainView.findViewById(R.id.collection_list);
        mAuth = FirebaseAuth.getInstance();

        mRootRef = FirebaseDatabase.getInstance().getReference();
        mCurrent_user_id = mAuth.getCurrentUser().getUid();
        mCouponCollectionDatabase = FirebaseDatabase.getInstance().getReference().child("CouponCollection").child(mCurrent_user_id);
        mCouponCollectionDatabase.keepSynced(true);

        mCouponDatabase = FirebaseDatabase.getInstance().getReference().child("Coupon");
        mCouponDatabase.keepSynced(true);

        mStoreDatabase = FirebaseDatabase.getInstance().getReference().child("Store");
        mStoreDatabase.keepSynced(true);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);

        mCollectionList.setHasFixedSize(true);
        mCollectionList.setLayoutManager(linearLayoutManager);

        return mMainView;
    }
    @Override
    public void onStart() {
        super.onStart();

        //使用FirebaseUI提供的FirebaseRecyclerAdapter類別快速生出adapter供RecyclerView顯示，不須實作太多方法即可同步Firebase資料。
        final FirebaseRecyclerAdapter<Collection,ObjViewHolder> firebaseObjAdapter = new FirebaseRecyclerAdapter<Collection,ObjViewHolder>(
                Collection.class,
                R.layout.user_object_list,
                ObjViewHolder.class,
                mCouponCollectionDatabase
        ) {
            @Override
            protected void populateViewHolder(final ObjViewHolder objViewHolder, final Collection collection, int position) {



                //objViewHolder.setDate(collection.getDate());

                final String list_object_id = getRef(position).getKey();
                mCouponDatabase.child(list_object_id).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(final DataSnapshot dataSnapshot) {

                        if(dataSnapshot.hasChild("title")) {
                            title = dataSnapshot.child("title").getValue().toString();
                        }
                        if(dataSnapshot.hasChild("content")) {
                            content = dataSnapshot.child("content").getValue().toString();
                        }
                        if(dataSnapshot.hasChild("user")) {
                            user = dataSnapshot.child("user").getValue().toString();
                        }
                        //final String user = dataSnapshot.child("user").getValue().toString();


                        objViewHolder.setDate(String.valueOf(getDate(collection.getDate())));
                        objViewHolder.setTitle(title);
                        objViewHolder.mView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {


                                CharSequence option[] = new CharSequence[] {"查看物件資訊","刪除優惠券"};
                                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                                builder.setTitle("選擇一個項目");
                                builder.setItems(option, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {

                                        if(i == 0) {
                                            final View content_layout = LayoutInflater.from(getActivity()).inflate(R.layout.activity_coupon_show, null);

                                            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

                                            builder.setView(content_layout);

                                            txv_title = content_layout.findViewById(R.id.title);
                                            txv_content = content_layout.findViewById(R.id.content);
                                            txv_creator = content_layout.findViewById(R.id.creator);
                                            final Button btn = content_layout.findViewById(R.id.pay);
                                            btn.setOnClickListener(new android.view.View.OnClickListener(){

                                                @Override
                                                public void onClick(View v) {

                                                    btn.setEnabled(false);
                                                    Toast.makeText(getActivity(), "兌換成功", Toast.LENGTH_SHORT).show();

                                                    mRootRef.child("CouponCollection").child(mCurrent_user_id).child(list_object_id).child("pay").setValue(true);
                                                    mRootRef.child("CouponCollection").child(list_object_id).child(mCurrent_user_id).child("pay").setValue(true);

                                                }
                                            });

                                            mCouponCollectionDatabase.child(list_object_id).addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(DataSnapshot dataSnapshot) {
                                                    if(dataSnapshot.child("pay").getValue().toString()=="true") {

                                                        btn.setEnabled(false);
                                                    }
                                                }

                                                @Override
                                                public void onCancelled(DatabaseError databaseError) {

                                                }
                                            });


                                            txv_title.setText(title);
                                            txv_content.setText(content);

                                            mStoreDatabase.child(user).addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(DataSnapshot dataSnapshot) {

                                                    String userName = dataSnapshot.child("storename").getValue().toString();
                                                    txv_creator.setText(userName);

                                                }

                                                @Override
                                                public void onCancelled(DatabaseError databaseError) {

                                                }
                                            });


                                            builder.show();
                                        }
                                        if(i == 1) {

                                            mRootRef.child("CouponCollection").child(list_object_id).child(mCurrent_user_id).removeValue();
                                            mRootRef.child("CouponCollection").child(mCurrent_user_id).child(list_object_id).removeValue();
                                            Toast.makeText(getActivity(), "刪除優惠券成功", Toast.LENGTH_SHORT).show();

                                        }
                                    }
                                });

                                builder.show();

                            }
                        });

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

            }

        };
        //將Adapter設定給RecyclerView
        mCollectionList.setAdapter(firebaseObjAdapter);
        //mCollectionList.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL));
        //滑動以刪除物件
        ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new
                CallBack(0, ItemTouchHelper.RIGHT,firebaseObjAdapter);

        ItemTouchHelper touchHelper = new ItemTouchHelper(simpleItemTouchCallback);
        // 加入到recycleView
        touchHelper.attachToRecyclerView(mCollectionList);
    }

    public static class ObjViewHolder extends RecyclerView.ViewHolder {
        View mView;
        public ObjViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
        }
        public void setTitle(String title) {

            TextView objectTitleView = (TextView) mView.findViewById(R.id.user_object_title);
            objectTitleView.setText(title);
        }
        public void setDate(String date) {

            //設成是何時收藏
            TextView objectStatusView = (TextView) mView.findViewById(R.id.user_object_status);
            objectStatusView.setText("自 "+ date + " 收藏");
        }
        public void setType(int typeid) {

            ImageView typeView = (ImageView) mView.findViewById(R.id.object_type);
            typeView.setImageResource(typeid);
        }
        public void setVisibility(boolean isVisible){
            RecyclerView.LayoutParams param = (RecyclerView.LayoutParams)itemView.getLayoutParams();
            if (isVisible){
                param.height = LinearLayout.LayoutParams.WRAP_CONTENT;
                param.width = LinearLayout.LayoutParams.MATCH_PARENT;
            }else{
                param.height = 0;
                param.width = 0;
            }
            itemView.setLayoutParams(param);
        }
        private String getDate(long time) {
            Date date = new Date(time*1000L); // *1000 is to convert seconds to milliseconds
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd kk:mm"); // the format of your date
            sdf.setTimeZone(TimeZone.getTimeZone("GMT-8"));

            return sdf.format(date);
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
            // 刪除物件
            mRootRef.child("CouponCollection").child(list_user_id).child(mCurrent_user_id).removeValue();
            mRootRef.child("CouponCollection").child(mCurrent_user_id).child(list_user_id).removeValue();
            Toast.makeText(getActivity(), "刪除收藏優惠券成功", Toast.LENGTH_SHORT).show();

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
    private String getDate(long time) {

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日 aakk:mm:ss");
        Date Date = (new Date(time));

        return sdf.format(Date);
    }

}
