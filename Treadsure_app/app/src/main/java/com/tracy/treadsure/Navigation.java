package com.tracy.treadsure;

import android.*;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class Navigation extends AppCompatActivity
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

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;
    private FirebaseAuth mAuth;
    private DatabaseReference mUserDatabase;
    private ImageView mProfileImage;
    String image;
    private Context mContext = this;
    private static final int BODY_SENSORS = 2,RECORD_AUDIO = 3,REQUEST_LOCATION = 4;

    private static final int GALLERY_PICK = 1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser() != null) {
            mUserDatabase = FirebaseDatabase.getInstance().getReference().child("User").child(mAuth.getCurrentUser().getUid());
            //mUserDatabase.keepSynced(true);
        }
        if(!isConnected(Navigation.this))
            buildDialog(Navigation.this).show();
        else {
            Toast.makeText(Navigation.this,"歡迎進入Treadsure世界", Toast.LENGTH_SHORT).show();
            setContentView(R.layout.activity_navigation);
            MyApplication.getInstance().addActivity(this);

            final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);
            getSupportActionBar().setTitle("周邊物件");

            mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

            //設定viewpager及tablayout連動
            mViewPager = (ViewPager) findViewById(R.id.container);
            mViewPager.setAdapter(mSectionsPagerAdapter);

            TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);



            mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
            //設定title
            mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

                }
                @Override
                public void onPageSelected(int position) {
                    switch(position){
                        case 0:
                            toolbar.setTitle("周邊物件");
                            break;
                        case 1:
                            toolbar.setTitle("收藏優惠券");
                            break;
                       /*case 2:
                            toolbar.setTitle("收藏廣告");
                            break;*/
                        case 2:
                            toolbar.setTitle("查看物件");
                            break;
                        case 3:
                            toolbar.setTitle("聊天室");
                            break;
                        case 4:
                            toolbar.setTitle("好友");
                            break;
                    }

                }
                @Override            public void onPageScrollStateChanged(int state) {

                }
            });

            tabLayout.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(mViewPager));



            DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
            ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                    this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
            drawer.addDrawerListener(toggle);
            toggle.syncState();

            final NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
            final View header = navigationView.inflateHeaderView(R.layout.nav_header_navigation);
            mUserDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
                String name,email;
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if(dataSnapshot.hasChild("name")) {
                        name = dataSnapshot.child("name").getValue().toString();
                    }
                    if(dataSnapshot.hasChild("email")) {
                        email = dataSnapshot.child("email").getValue().toString();
                    }

                    if(dataSnapshot.hasChild("image")) {
                        image = dataSnapshot.child("image").getValue().toString();
                        mProfileImage = header.findViewById(R.id.header_image);
                        Picasso.with(Navigation.this).load(image).placeholder(R.drawable.user_person_before).into(mProfileImage);
                    }

                    //final View content_layout = LayoutInflater.from(Navigation.this).inflate(R.layout.nav_header_navigation, null);

                    TextView headerName = header.findViewById(R.id.header_name);
                    headerName.setText(name);

                    TextView headerEmail = header.findViewById(R.id.header_email);
                    headerEmail.setText(email);




                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
            navigationView.setNavigationItemSelectedListener(this);
        }
        //setContentView(R.layout.activity_navigation);

        //-----------------------權限----------------------------
        //相機權限
        if(ContextCompat.checkSelfPermission(mContext, android.Manifest.permission.CAMERA)!= PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.CAMERA},
                    1);
        }
        // BODY_SENSORS
        if(ContextCompat.checkSelfPermission(mContext, android.Manifest.permission.BODY_SENSORS)!= PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.BODY_SENSORS},
                    BODY_SENSORS);
        }
        // RECORD_AUDIO
        if(ContextCompat.checkSelfPermission(mContext, android.Manifest.permission.RECORD_AUDIO)!= PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.RECORD_AUDIO},
                    RECORD_AUDIO);
        }


    }

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
        getMenuInflater().inflate(R.menu.navigation, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.ar) {

            // 傳入進入AR識別字串 使用者
            SharedPreferences sharedPreferences = getSharedPreferences("UorS" , MODE_PRIVATE);
            sharedPreferences.edit().putInt("index", 0).apply();
            startActivity(new Intent(this, ARwebView.class));
        }
        if (id == R.id.inform) {

            startActivity(new Intent(this, Notification.class));
        }
        //noinspection SimplifiableIfStatement
        if (id == R.id.logout) {
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
            startActivity(new Intent(this, Account_setting.class));
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
//刪除PlaceholderFragment
    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    Tab1 tab1 = new Tab1();
                    return tab1;
                case 1:
                    Tab2 tab2 = new Tab2();
                    return tab2;
                /*case 2:
                    Tab2_1 tab2_1 = new Tab2_1();
                    return tab2_1;*/
                case 2:
                    Tab3 tab3 = new Tab3();
                    return tab3;
                case 3:
                    Tab4 tab4 = new Tab4();
                    return tab4;
                case 4:
                    Tab5 tab5 = new Tab5();
                    return tab5;
                default:
                    return null;

            }
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 5;
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
        else {
            //在MainActivity的時候有連上代表是在線
            mUserDatabase.child("online").setValue("true");

        }

    }

    @Override
    protected void onStop() {
        super.onStop();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null ) {
            //mUserRef.child("lastSeen").setValue(ServerValue.TIMESTAMP);
            mUserDatabase.child("online").setValue(ServerValue.TIMESTAMP);
        }

    }
    private void sendToStart() {
        finish();
        startActivity(new Intent(Navigation.this,MainActivity.class));

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

    //-------------------------------權限------------------------------------

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case BODY_SENSORS: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "你同意此權限", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "你拒绝此權限", Toast.LENGTH_SHORT).show();
                }
                return;
            }
            case RECORD_AUDIO: {

                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "你同意此權限", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "你拒绝此權限", Toast.LENGTH_SHORT).show();
                }
                return;
            }
            case REQUEST_LOCATION: {

                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "你同意此權限", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "你拒绝此權限", Toast.LENGTH_SHORT).show();
                }
                return;
            }
            default:


        }
    }

}
