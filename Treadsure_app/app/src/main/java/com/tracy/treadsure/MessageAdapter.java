package com.tracy.treadsure;

import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by Admin on 2018/4/6.
 */

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder>{

    // 將Messages 資料放進list裡面
    private List<Messages> mMessageList;
    private FirebaseAuth mAuth= FirebaseAuth.getInstance();
    String current_user_id = mAuth.getCurrentUser().getUid();

    private DatabaseReference mUserDatabase;

    public MessageAdapter(List<Messages> mMessageList) {

        this.mMessageList = mMessageList;

    }

    //  onCreateViewHolder: 現有的 ViewHolder 不夠用，要求 Adapter 產生一個新的
    @Override
    public MessageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.message_single_layout ,parent, false);

        return new MessageViewHolder(v);

    }


    public class MessageViewHolder extends RecyclerView.ViewHolder {

        public TextView messageText,displayName,time;
        public CircleImageView profileImage;
        public ImageView messageImage;

        //保存起來的 view 會放在 view 裡面
        public MessageViewHolder(View view) {
            super(view);

            messageText = (TextView) view.findViewById(R.id.message_text_layout);
            profileImage = (CircleImageView) view.findViewById(R.id.message_profile_layout);
            displayName = (TextView) view.findViewById(R.id.name_text_layout);
            messageImage = (ImageView) view.findViewById(R.id.message_image_layout);
            time = (TextView) view.findViewById(R.id.time_text_layout);


        }
    }

    //onBindViewHolder:重用之前產生的 ViewHolder，把特定位置的資料連結上去準備顯示
    @Override
    public void onBindViewHolder(final MessageViewHolder viewHolder, int i) {

        // mAuth = FirebaseAuth.getInstance();

        //String current_user_id = mAuth.getCurrentUser().getUid();

        // 取得第幾個
        Messages c = mMessageList.get(i);

        // 取得是誰送出訊息
        String from_user = c.getFrom();
        // 取得傳輸的是文字/圖片
        String message_type = c.getType();
        long message_time = c.getTime();


        Log.d("錯誤",current_user_id + from_user);
        //layout changes // 如果此使用者送出訊息
        if(from_user.equals(current_user_id)) {

            viewHolder.messageText.setBackgroundResource(R.drawable.message_layout);
            viewHolder.messageText.setTextColor(Color.BLACK);
            viewHolder.profileImage.setVisibility(View.GONE);
            viewHolder.displayName.setVisibility(View.INVISIBLE);

            //設定訊息時間
            Calendar cal = Calendar.getInstance(Locale.ENGLISH);
            cal.setTimeInMillis(message_time); //(timestamp)
            String date = DateFormat.format("hh:mm", cal).toString();
            viewHolder.time.setText(date);

            RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            //此處相當於Layout的Android:layout_gravity属性
            lp.addRule(RelativeLayout.ALIGN_PARENT_END);
            viewHolder.messageText.setLayoutParams(lp);
            RelativeLayout.LayoutParams lp2 = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            //此處相當於Layout中的Android:layout_gravity属性
            lp2.addRule(RelativeLayout.START_OF,R.id.message_text_layout);
            lp2.addRule(RelativeLayout.ALIGN_BOTTOM,R.id.message_text_layout);
            lp2.setMargins(0,90,20,20);
            viewHolder.time.setLayoutParams(lp2);


        }
        // 如果是其他使用者送出
        else {

            // 要用drawable的要是Resource
            viewHolder.messageText.setBackgroundResource(R.drawable.message_text_background);
            viewHolder.messageText.setTextColor(Color.WHITE);
            viewHolder.profileImage.setVisibility(View.VISIBLE);
            viewHolder.displayName.setVisibility(View.VISIBLE);


            //設定訊息時間
            Calendar cal = Calendar.getInstance(Locale.ENGLISH);
            cal.setTimeInMillis(message_time); //(timestamp)
            String date = DateFormat.format("hh:mm", cal).toString();
            viewHolder.time.setText(date);


            RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            //此處相當於Layout中的Android:layout_gravity属性
            lp.addRule(RelativeLayout.ALIGN_START,R.id.name_text_layout);
            lp.addRule(RelativeLayout.BELOW,R.id.name_text_layout);
            viewHolder.messageText.setLayoutParams(lp);

            RelativeLayout.LayoutParams lp2 = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            //此處相當於Layout中的Android:layout_gravity属性
            lp2.addRule(RelativeLayout.END_OF,R.id.message_text_layout);
            lp2.addRule(RelativeLayout.ALIGN_BOTTOM,R.id.message_profile_layout);
            lp2.setMargins(20,90,0,0);
            viewHolder.time.setLayoutParams(lp2);

        }
        viewHolder.messageText.setText(c.getMessage());



        //-------- 設置頭貼與名字 ---------
        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("User").child(from_user);

        mUserDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                String name = dataSnapshot.child("name").getValue().toString();
                if(dataSnapshot.hasChild("thumb_image")) {
                    String image = dataSnapshot.child("thumb_image").getValue().toString();
                    Picasso.with(viewHolder.profileImage.getContext()).load(image)
                            .placeholder(R.drawable.user_person_before).into(viewHolder.profileImage);

                }
                viewHolder.displayName.setText(name);


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        if(message_type.equals("text")) {

            viewHolder.messageText.setText(c.getMessage());
            // 將View隱藏
            viewHolder.messageImage.setVisibility(View.INVISIBLE);


        } else {

            viewHolder.messageText.setVisibility(View.INVISIBLE);
            Picasso.with(viewHolder.profileImage.getContext()).load(c.getMessage())
                    .placeholder(R.drawable.user_person_before).into(viewHolder.messageImage);

        }

    }

    @Override
    public int getItemCount() {
        return mMessageList.size();
    }
}
