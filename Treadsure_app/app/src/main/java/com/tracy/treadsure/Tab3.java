package com.tracy.treadsure;

/**
 * Created by angoo on 2018/1/31.
 */

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.RectF;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.support.v4.app.DialogFragment;

import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class Tab3 extends Fragment {


    private RecyclerView mObjList;
    View content_layout;
    private DatabaseReference mObjectDatabase,mUserDatabase,mCollectionDatabase;
    private FirebaseAuth mAuth;
    String mCurrent_user_id,list_object_id;
    private View mMainView;
    TextView txv_title,txv_content,txv_creator,edit;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mMainView = inflater.inflate(R.layout.tab3, container, false);

        mObjList = (RecyclerView) mMainView.findViewById(R.id.obj_list);
        mAuth = FirebaseAuth.getInstance();

        mCurrent_user_id = mAuth.getCurrentUser().getUid();

        mCollectionDatabase = FirebaseDatabase.getInstance().getReference().child("Collection");
        mCollectionDatabase.keepSynced(true);

        mObjectDatabase = FirebaseDatabase.getInstance().getReference().child("Object");
        mObjectDatabase.keepSynced(true);

        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("User");
        mUserDatabase.keepSynced(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);

        mObjList.setHasFixedSize(true);
        mObjList.setLayoutManager(linearLayoutManager);



        return mMainView;
    }
    public void firebase() {

        //只列出自己丟的物件
        final Query objectQuery = mObjectDatabase.orderByChild("user").equalTo(mCurrent_user_id);
        //final Query objectQueryDate = mObjectDatabase.orderByChild("date");

        //使用FirebaseUI提供的FirebaseRecyclerAdapter類別快速生出adapter供RecyclerView顯示，不須實作太多方法即可同步Firebase資料。
        final FirebaseRecyclerAdapter<Obj_test,ObjViewHolder> firebaseObjAdapter = new FirebaseRecyclerAdapter<Obj_test,ObjViewHolder>(
                Obj_test.class,
                R.layout.user_object_list,
                ObjViewHolder.class,
                objectQuery
        ) {
            @Override
            protected void populateViewHolder(final ObjViewHolder objViewHolder, final Obj_test obj_test, int position) {

                list_object_id = getRef(position).getKey();
                //objViewHolder.setDate(obj.getDate());

                mObjectDatabase.child(list_object_id).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        Log.d("資料",dataSnapshot.toString());
                        //final String title = dataSnapshot.child("title").getValue().toString();
                        //final String content = dataSnapshot.child("content").getValue().toString();
                        //final String user = dataSnapshot.child("user").getValue().toString();
                        //final String type = dataSnapshot.child("type").getValue().toString();
                        //final long date = dataSnapshot.child("date").getValue();

                        final String title = obj_test.getTitle();
                        final String content = obj_test.getContent();
                        final String user = obj_test.getUser();
                        final String type = obj_test.getType();
                        final long date = obj_test.getDate();
                        objViewHolder.setDate(String.valueOf(getDate(date)));

                        if(type.equals("交友")){
                            objViewHolder.setType(R.mipmap.friend);
                        }
                        else if(type.equals("揪團")){
                            objViewHolder.setType(R.mipmap.group);
                        }
                        else {
                            objViewHolder.setType(R.mipmap.other);
                        }

                        objViewHolder.setTitle(title);
                        objViewHolder.mView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {

                                Toast.makeText(getActivity(),type,Toast.LENGTH_SHORT).show();
                                CharSequence option[] = new CharSequence[] {"查看物件資訊","刪除物件"};
                                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                                builder.setTitle("選擇一個項目");
                                builder.setItems(option, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {

                                        if(i == 0) {
                                            content_layout = LayoutInflater.from(getActivity()).inflate(R.layout.activity_object_show, null);

                                            final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

                                            builder.setView(content_layout);

                                            txv_title = content_layout.findViewById(R.id.title);
                                            txv_content = content_layout.findViewById(R.id.content);
                                            txv_creator = content_layout.findViewById(R.id.creator);


                                            txv_title.setText(title);
                                            txv_content.setText(content);


                                            mUserDatabase.child(user).addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(DataSnapshot dataSnapshot) {

                                                    String userName = dataSnapshot.child("name").getValue().toString();
                                                    txv_creator.setText(userName);

                                                }

                                                @Override
                                                public void onCancelled(DatabaseError databaseError) {

                                                }
                                            });

                                            builder.show();

                                            edit = content_layout.findViewById(R.id.collectionCount);
                                            edit.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View view) {

                                                    DialogFragment dialog = DialogFragmentHelper.newInstance(txv_title.getText().toString(),txv_content.getText().toString(),list_object_id);//將EditText值傳送給DialogFragment
                                                    dialog.show(getActivity().getSupportFragmentManager(),"Dialog");//Dialog出現
                                                    dialog.setStyle(DialogFragment.STYLE_NO_TITLE, R.style.DialogStyle);

                                                    /*Intent edit_intent = new Intent(getActivity(),EditObject.class);
                                                    edit_intent.putExtra("title",title);
                                                    edit_intent.putExtra("content",content);

                                                    startActivity(edit_intent);*/

                                                    txv_title.setText(title);
                                                    txv_content.setText(content);

                                                }
                                            });
                                        }
                                        if(i == 1) {

                                            mObjectDatabase.child(list_object_id).removeValue();
                                            Toast.makeText(getActivity(), "刪除物件成功", Toast.LENGTH_SHORT).show();

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
        mObjList.setAdapter(firebaseObjAdapter);
        // 設置分割線
        //mObjList.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL));

        //滑動以刪除物件
        ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new
                CallBack(0, ItemTouchHelper.RIGHT,firebaseObjAdapter);

        ItemTouchHelper touchHelper = new ItemTouchHelper(simpleItemTouchCallback);
        // 加入到recycleView
        touchHelper.attachToRecyclerView(mObjList);

    }
    @Override
    public void onStart() {
        super.onStart();

        final Query objectQueryDate = mObjectDatabase.orderByChild("date");
        objectQueryDate.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                firebase();

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


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

            //設成是何時建立物件
            TextView objectStatusView = (TextView) mView.findViewById(R.id.user_object_status);
            objectStatusView.setText("自 "+ date + " 建立");
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
        public void onSwiped(final RecyclerView.ViewHolder viewHolder, int direction) {
            // 取得位置
            int position = viewHolder.getAdapterPosition();
            final String list_user_id = adapter.getRef(position).getKey();

            // 刪除物件
            mObjectDatabase.child(list_user_id).setValue(null);
            Toast.makeText(getActivity(), "刪除物件成功", Toast.LENGTH_SHORT).show();
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
            else if(actionState==ItemTouchHelper.ACTION_STATE_IDLE){
                firebase();
            }
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);

        }


    }
    private String getDate(long time) {

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日 aakk:mm:ss");
        Date Date = (new Date(time));

        return sdf.format(Date);
    }
    @SuppressLint("ValidFragment")
    public static class DialogFragmentHelper extends DialogFragment {
        public static DialogFragmentHelper newInstance(String title, String content,String list_object_id){
            DialogFragmentHelper f = new DialogFragmentHelper();
            Bundle args = new Bundle();
            args.putString("title",title);
            args.putString("content",content);
            args.putString("list_object_id",list_object_id);
            f.setArguments(args);//透過setArguments傳值
            return f;
        }
        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);//取消Dialog title列


            View v = inflater.inflate(R.layout.activity_edit_object, container, false);//選擇自定義的layout
            String title = getArguments().getString("title");
            String content = getArguments().getString("content");
            final String list_object_id = getArguments().getString("list_object_id");

            final EditText edt_title = v.findViewById(R.id.title);
            edt_title.setText(title);

            final EditText edt_content = v.findViewById(R.id.content);
            edt_content.setText(content);

            Button saveEdit = v.findViewById(R.id.save);
            saveEdit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    DatabaseReference mObjectDatabase;
                    mObjectDatabase = FirebaseDatabase.getInstance().getReference().child("Object");
                    //更新資料庫
                    mObjectDatabase.child(list_object_id).child("title").setValue(edt_title.getText().toString());
                    mObjectDatabase.child(list_object_id).child("content").setValue(edt_content.getText().toString());

                    dismiss();

                }

            });




            return v;
        }



    }
}
