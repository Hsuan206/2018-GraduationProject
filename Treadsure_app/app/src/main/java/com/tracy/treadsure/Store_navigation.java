package com.tracy.treadsure;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class Store_navigation extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;
    private FirebaseAuth mAuth;
    private DatabaseReference mStoreDatabase;
    private ImageView mProfileImage;
    String image;
    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);



        mAuth = FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser() != null) {
            mStoreDatabase = FirebaseDatabase.getInstance().getReference().child("Store").child(mAuth.getCurrentUser().getUid());
            mStoreDatabase.keepSynced(true);
        }

        if(!isConnected(Store_navigation.this)) buildDialog(Store_navigation.this).show();
        else {
            setContentView(R.layout.activity_store_navigation);
            MyApplication.getInstance().addActivity(this);
            final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);
            getSupportActionBar().setTitle("周邊物件");


            mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

            //設定viewpager及tablayout連動
            mViewPager = (ViewPager) findViewById(R.id.container);
            mViewPager.setAdapter(mSectionsPagerAdapter);

            TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);

            //TabLayoutOnPageChangeListener: tab position is kept in sync
            mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
            //設定title要用addOnPageChangeListener
            mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                @Override
                //螢幕滾動時
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

                }
                //page滑動時
                @Override
                public void onPageSelected(int position) {
                    switch(position){
                        case 0:
                            toolbar.setTitle("周邊物件");
                            break;
                        case 1:
                            toolbar.setTitle("查看優惠券");
                            break;
                        case 2:
                            toolbar.setTitle("查看廣告");
                            break;
                    }
                }
                @Override
                public void onPageScrollStateChanged(int state) {

                }
            });

            tabLayout.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(mViewPager));


            //DrawerLayout實現側滑菜單:DrawerLayout 連接活動的 ActionBar
            DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);

            //將 DrawerLayout 及action bar綁在一起
            //後面的字串參數 openDrawerContentDescRes與 closeDrawerContentDescRes 是 accessibility services 可轉換成語音??
            ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                    this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);

            //將指定的Listener添加到將drawer事件的Listener列表
            drawer.addDrawerListener(toggle);
            //將drawer狀態與 DrawerLayout同步
            toggle.syncState();

            //側邊的drawer
            NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
            final View header = navigationView.inflateHeaderView(R.layout.nav_header_store_navigation);
            mStoreDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    String name = dataSnapshot.child("name").getValue().toString();
                    String email = dataSnapshot.child("email").getValue().toString();
                    if(dataSnapshot.hasChild("image")) {
                        image = dataSnapshot.child("image").getValue().toString();
                        mProfileImage = header.findViewById(R.id.header_image);
                        Picasso.with(Store_navigation.this).load(image).placeholder(R.drawable.user_person_before).into(mProfileImage);
                    }
                    Log.d("Name",name);



                    TextView headerName = header.findViewById(R.id.header_name);
                    headerName.setText(name);
                    Log.d("Name",name);
                    TextView headerEmail = header.findViewById(R.id.header_email);
                    headerEmail.setText(email);




                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
            navigationView.setNavigationItemSelectedListener(this);
        }

    }
    //點擊返回按鈕,如果DrawerLayout是打開就關閉
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.store_navigation, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.store_ar) {

            // 傳入進入AR識別字串 使用者
            SharedPreferences sharedPreferences = getSharedPreferences("UorS" , MODE_PRIVATE);
            sharedPreferences.edit().putInt("index", 1).apply();
            startActivity(new Intent(this, ARwebView.class));
        }
        //noinspection SimplifiableIfStatement
        if (id == R.id.store_logout) {
            FirebaseAuth.getInstance().signOut();
            finish();
            startActivity(new Intent(this, MainActivity.class));
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        if (id == R.id.nav_account) {
            startActivity(new Intent(this, Store_account_setting.class));
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
    //刪除PlaceholderFragment
    /**
     * A {@link FragmentPagerAdapter}回傳一個fragment 相合的tab.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    Store_Tab1 tab1 = new Store_Tab1();
                    return tab1;
                case 1:
                    Store_Tab2 tab2 = new Store_Tab2();
                    return tab2;
                case 2:
                    Store_Tab3 tab3 = new Store_Tab3();
                    return tab3;

                default:
                    return null;

            }
        }

        @Override
        public int getCount() {
            // Show 2 total pages.
            return 3;
        }

    }
    @Override public boolean onKeyDown(int keyCode, KeyEvent event)  {
        if(keyCode == KeyEvent.KEYCODE_BACK){
            new AlertDialog.Builder(this)
                    .setTitle("結束程式")
                    .setMessage("是否退出此應用程式")
                    .setPositiveButton("是",new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            finish();
                            MyApplication.getInstance().exit();
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
    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser == null) {
            sendToStart();
        }


    }

    private void sendToStart() {
        finish();
        startActivity(new Intent(Store_navigation.this,MainActivity.class));

    }

    // 確認是否有網路
    public boolean isConnected(Context context) {

        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netinfo = cm.getActiveNetworkInfo();

        if (netinfo != null && netinfo.isConnectedOrConnecting()) {
            android.net.NetworkInfo wifi = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            android.net.NetworkInfo mobile = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

            if((mobile != null && mobile.isConnectedOrConnecting()) || (wifi != null && wifi.isConnectedOrConnecting())) return true;
            else return false;
        } else
            return false;
    }

    //無網路彈跳視窗
    public AlertDialog.Builder buildDialog(Context c) {

        AlertDialog.Builder builder = new AlertDialog.Builder(c);
        builder.setTitle("沒有網路連線");
        builder.setMessage("請開啟網路數據或Wifi，點擊確認離開");

        builder.setPositiveButton("確認", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {

                finish();
            }
        });

        return builder;
    }
}
