package com.tracy.treadsure;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class Notification extends AppCompatActivity {

    private FirebaseUser mCurrent_user;
    private RecyclerView mNotificationList;
    private DatabaseReference mNotificationDatabase;
    private static DatabaseReference mUsersDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);
        MyApplication.getInstance().addActivity(this);



        final Toolbar toolbar = (Toolbar) findViewById(R.id.Notification_appBar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("通知");
        //讓他有返回鍵
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mCurrent_user = FirebaseAuth.getInstance().getCurrentUser();

        mNotificationList = (RecyclerView) findViewById(R.id.Notificatioin_list);
        mNotificationDatabase = FirebaseDatabase.getInstance().getReference().child("Notification");
        mNotificationDatabase.keepSynced(true);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);

        mNotificationList.setHasFixedSize(true);
        mNotificationList.setLayoutManager(linearLayoutManager);

    }

    @Override
    public void onStart() {
        super.onStart();


        final Query NotificationQuery = mNotificationDatabase.child(mCurrent_user.getUid()).orderByChild("from");
        //使用FirebaseUI提供的FirebaseRecyclerAdapter類別快速生出adapter供RecyclerView顯示，不須實作太多方法即可同步Firebase資料。
        final FirebaseRecyclerAdapter<Inform,NotificationViewHolder> firebaseObjAdapter = new FirebaseRecyclerAdapter<Inform,NotificationViewHolder>(
                Inform.class,
                R.layout.user_object_list,
                NotificationViewHolder.class,
                NotificationQuery
        ) {
            @Override
            protected void populateViewHolder(final NotificationViewHolder notificationViewHolder, final Inform inform, int position) {

                final String list_user_id = getRef(position).getKey();

                mNotificationDatabase.child(list_user_id).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        notificationViewHolder.setValues(inform);

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
                //View 點下要取得別人的profile
                notificationViewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        Intent profileIntent = new Intent(Notification.this,ProfileActivity.class);
                        //放入接收者的user_id  // with ProfileActivity
                        profileIntent.putExtra("user_id",inform.getFrom());
                        startActivity(profileIntent);

                    }
                });


            }

        };
        //將Adapter設定給RecyclerView
        mNotificationList.setAdapter(firebaseObjAdapter);
        //mNotificationList.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
    }

    public static class NotificationViewHolder extends RecyclerView.ViewHolder {
        View mView;
        public NotificationViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
        }
        public void setValues(final Inform inform) {

            TextView objectTitleView = (TextView) mView.findViewById(R.id.user_object_title);
            objectTitleView.setText(inform.getType());
            final TextView objectStatusView = (TextView) mView.findViewById(R.id.user_object_status);
            mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("User");
            mUsersDatabase.keepSynced(true);
            mUsersDatabase.child(inform.getFrom()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    String name = dataSnapshot.child("name").getValue().toString();
                    objectStatusView.setText(name + "向你發送好友邀請");
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

        }

    }
}
