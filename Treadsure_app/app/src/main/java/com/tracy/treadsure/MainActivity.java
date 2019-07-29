package com.tracy.treadsure;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.ErrorCodes;
import com.firebase.ui.auth.IdpResponse;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity{
    private static final int RC_SIGN_IN = 123;
    Button signout_btn;
    FirebaseAuth auth;
    FirebaseUser fuser ;
    DatabaseReference mRootRef= FirebaseDatabase.getInstance().getReference();

    public static class User {

        public String name;
        public String email;
        public User(){

        }
        public String getEmail() {
            return email;
        }

        public String photoUrl ;

        public User(String name, String email,String photoUrl) {
            this.name=name;
            this.email=email;
            this.photoUrl=photoUrl;
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        auth = FirebaseAuth.getInstance();
        fuser=auth.getCurrentUser();
        MyApplication.getInstance().addActivity(this);

        /*try {

            PackageInfo info = getPackageManager().getPackageInfo("com.tracy.treadsure", PackageManager.GET_SIGNATURES);
            for(Signature signature:info.signatures)
            {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.d("KeyHash", Base64.encodeToString(md.digest(),Base64.DEFAULT));
            }

        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }*/





        Button btn = (Button) findViewById(R.id.signout_btn);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                auth.signOut();
                startToSignIn();
            }
        });
        //Checking If user has already signed in
        if (fuser!= null) {
            // already signed in
            Toast.makeText(MainActivity.this,"目前是登入狀態",Toast.LENGTH_LONG).show();


        } else {
            // not signed in
            Toast.makeText(MainActivity.this," 目前還尚未登入",Toast.LENGTH_LONG).show();
            try {
                startToSignIn();
            }catch (Exception e){
                Toast.makeText(MainActivity.this,e.getMessage(),Toast.LENGTH_LONG).show();
            }

        }



    }

    @Override
    public void onStart() {
        super.onStart();

        fuser=auth.getCurrentUser();
        //Toast.makeText(MainActivity.this,"已呼叫onStart()",Toast.LENGTH_LONG).show();
        if (fuser!= null) {   //Checking If user has already signed in
            // already signed in
            Toast.makeText(MainActivity.this,"目前是登入狀態",Toast.LENGTH_LONG).show();
            Log.d("登入者",fuser.getUid().toString());



            /*SharedPreferences sharedPreferences = getSharedPreferences("data" , MODE_PRIVATE);

            int index = sharedPreferences.getInt("index" , 0);
            Log.d("0是user,1是store", toString().valueOf(index));
            if(index==0) {
                //finish();
                //startActivity(new Intent(this, Navigation.class));
            }
            else {
                startActivity(new Intent(this, Store_navigation.class));
            }*/

            mRootRef.child("User").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    if (dataSnapshot.hasChild(fuser.getUid())) {

                        String deviceToken = FirebaseInstanceId.getInstance().getToken();
                        mRootRef.child("User").child(fuser.getUid()).child("device_token").setValue(deviceToken);
                        startActivity(new Intent(MainActivity.this,Navigation.class));
                    }
                    else {
                        mRootRef.child("Store").addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {

                                if (dataSnapshot.hasChild(fuser.getUid())) {
                                    startActivity(new Intent(MainActivity.this,Store_navigation.class));
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

        }

    }

    private void startToSignIn() {
        Toast.makeText(MainActivity.this,"開啟登入Activity",Toast.LENGTH_LONG).show();
        startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setAvailableProviders(Arrays.asList(
                                new AuthUI.IdpConfig.EmailBuilder().build(),
                                new AuthUI.IdpConfig.GoogleBuilder().build(),
                                new AuthUI.IdpConfig.FacebookBuilder().build()
                        ))
                        .setLogo(R.drawable.logo)
                        .setIsSmartLockEnabled(false)
                        .setTheme(R.style.FrontPage)
                        .build(),
                RC_SIGN_IN);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // RC_SIGN_IN is the request code you passed into startActivityForResult(...) when starting the sign in flow.
        if (requestCode == RC_SIGN_IN) {

            IdpResponse response = IdpResponse.fromResultIntent(data);


            // Successfully signed in
            if (resultCode == RESULT_OK) {
                Toast.makeText(MainActivity.this,"登入成功",Toast.LENGTH_SHORT).show();
                mRootRef.child("User").addValueEventListener(new ValueEventListener() {

                    @Override
                    public void onDataChange(final DataSnapshot userdataSnapshot) {
                        mRootRef.child("Store").addValueEventListener(new ValueEventListener() {

                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if ((!userdataSnapshot.hasChild(fuser.getUid())) && (!dataSnapshot.hasChild(fuser.getUid()))) {
                                    startActivity(new Intent(MainActivity.this,ProfileSetting.class));
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

/*
                //Checking user's role
                mRootRef.child("User").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {


                            String role = dataSnapshot.child(fuser.getUid()).child("role").getValue().toString();

                            if(role!=null){

                                if(role.equals("一般使用者")) {

                                    startActivity(new Intent(MainActivity.this, GeneralUser.class));
                                }

                            }
                            else {
                                Toast.makeText(MainActivity.this,"user",Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(MainActivity.this,ProfileSetting.class));

                            }

                        }
                        else {

                            mRootRef.child("Store").addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
//dataSnapshot.child(fuser.getUid()).exists()
                                    if (dataSnapshot.hasChild(fuser.getUid())) {

                                        String role = dataSnapshot.child(fuser.getUid()).child("role").getValue().toString();

                                        if(role!=null){

                                            if(role.equals("店家")) {

                                                startActivity(new Intent(MainActivity.this, BusinessUser.class));
                                            }

                                        }

                                    }else {

                                        Toast.makeText(MainActivity.this,"store",Toast.LENGTH_SHORT).show();
                                        startActivity(new Intent(MainActivity.this,ProfileSetting.class));

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

*/
            } else {
                // Sign in failed
                if (response == null) {
                    // User pressed back button
                    Toast.makeText(MainActivity.this,"使用者按下返回鍵",Toast.LENGTH_SHORT).show();
                    return;
                }

                if (response.getError().getErrorCode() == ErrorCodes.NO_NETWORK) {
                    Toast.makeText(MainActivity.this,"目前沒有網路連結",Toast.LENGTH_SHORT).show();
                    return;
                }

                Toast.makeText(MainActivity.this,"未知的錯誤",Toast.LENGTH_SHORT).show();

            }
        }
    }

}
