package com.tracy.treadsure;


import android.*;
import android.Manifest;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Base64;
import android.util.Log;
import android.view.KeyEvent;
import android.webkit.GeolocationPermissions;
import android.webkit.JavascriptInterface;
import android.webkit.JsPromptResult;
import android.webkit.JsResult;
import android.webkit.PermissionRequest;
import android.webkit.SslErrorHandler;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
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
import com.theartofdev.edmodo.cropper.CropImage;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import id.zelory.compressor.Compressor;

public class ARwebView extends AddFriend implements SensorEventListener {



    private Context mContext = this;
    //-------------------------------------------
    //創建物件
    createObject object = new createObject();
    //創建優惠券
    createCoupon coupon = new createCoupon();
    //創建廣告
    createAd ad_object = new createAd();
    //--------------------------------------------
    WebView mWebView;
    Button button;
    private boolean is_exit = false;

    private String locationProvider;
    LocationManager locationManager;
    String report_state = "未檢舉";
    private ValueCallback<Uri> uploadMessage;
    private ValueCallback<Uri[]> uploadMessageAboveL;
    //感測器管理員
    SensorManager mSensorManager;
    //加速感測器物件

    private Sensor mSensorAccelerometer;
    private Sensor mSensorAmbientTemp;
    private Sensor mSensorGravity;
    private Sensor mSensorGyroscope;
    private Sensor mSensorLight;
    private Sensor mSensorLinearAcceleration;
    private Sensor mSensorMagneticField;
    private Sensor mSensorPressure;
    private Sensor mSensorProximity;
    private Sensor mSensorRelativeHumidity;
    private Sensor mSensorRotationVector;

    TextView txv;
    ImageView igv;
    RelativeLayout layout;
    double mx=0,my=0,latitude,longitude;
    private static final int BODY_SENSORS = 2,RECORD_AUDIO = 3,REQUEST_LOCATION = 4;

    private static final int GALLERY_PICK = 1;
    private ProgressDialog mProgressDialog;
    private StorageReference mImageStorage;
    private DatabaseReference mAdObjectDatabase,mCouponDatabase;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.arwebview);
        MyApplication.getInstance().addActivity(this);

        /*SharedPreferences sharedPreferences = getSharedPreferences("ObjId",MODE_PRIVATE);
        obID = sharedPreferences.getString("index" ,"0");*/

        SharedPreferences sharedPreferences = getSharedPreferences("UorS",MODE_PRIVATE);

        int UorS = sharedPreferences.getInt("index" ,2);

        mImageStorage = FirebaseStorage.getInstance().getReference();
        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("User");
        mAdObjectDatabase = FirebaseDatabase.getInstance().getReference().child("AdObject");
        mCouponDatabase = FirebaseDatabase.getInstance().getReference().child("Coupon");

        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        // 取得加速感測器
        mSensorAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mSensorAmbientTemp = mSensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE);
        mSensorGravity = mSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
        mSensorGyroscope = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        mSensorLight = mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        mSensorLinearAcceleration = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        mSensorMagneticField = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        mSensorPressure = mSensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE);
        mSensorProximity = mSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        mSensorRelativeHumidity = mSensorManager.getDefaultSensor(Sensor.TYPE_RELATIVE_HUMIDITY);
        mSensorRotationVector = mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);



        txv = (TextView)findViewById(R.id.txv);

        //螢幕不隨手機旋轉
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);

        layout = (RelativeLayout) findViewById(R.id.layout);

        mWebView =(WebView) findViewById(R.id.webview);

        mWebView.clearCache(true);
        WebSettings webSettings = mWebView.getSettings();
        webSettings.setSupportZoom(true);
        webSettings.setBuiltInZoomControls(true);

        // 設置和JS交互權限
        webSettings.setJavaScriptEnabled(true);
        // 允許JS談窗
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
        // 允許geolocation
        webSettings.setGeolocationEnabled(true);
        //mWebView.getSettings().setGeolocationDatabasePath(getApplicationContext().getFilesDir().getPath());
        mWebView.addJavascriptInterface(new JsCallBack_createCoupon(), "ExtObj_createCoupon");
        mWebView.addJavascriptInterface(new JsCallBack_createAd(), "ExtObj_createAd");
        mWebView.addJavascriptInterface(new JsCallBack_addFriend(), "ExtObj_addFriend");
        mWebView.addJavascriptInterface(new JsCallBack_userId(), "ExtObj_userId");
        mWebView.addJavascriptInterface(new JsCallBack_modelId(), "ExtObj_modelId");
        mWebView.addJavascriptInterface(new JsCallBack_createObject(), "ExtObj_createObject");
        mWebView.addJavascriptInterface(new JsCallBack_adCollection(), "ExtObj_adCollection");
        mWebView.addJavascriptInterface(new JsCallBack_couponCollection(), "ExtObj_couponCollection");
        mWebView.addJavascriptInterface(new JsCallBack_report(), "ExtObj_report");
        mWebView.setWebViewClient(new WebViewClientImpl());

        // 由於設置了談窗檢驗調用结果，所以需要支持JS對話框
        // webview只是載體，内容需要使用webviewChromClient實現
        // 通过設置WebChromeClient處理JS的對話框
        mWebView.setWebChromeClient(new WebChromeClient() {

            // 攔截輸入框
            // message:代表 promt()的内容(不是url)
            // result:代表輸入框的返回值
            @Override
            public boolean onJsPrompt(WebView view, String url, String message, String defaultValue, JsPromptResult result) {

                // 判斷是否是需要的URL
                // 一般根據scheme 和 authority判斷 (前兩個參數)
                // uri = [scheme:][//authority][path][?query][#fragment]
                // 設傳進來的 url = "js://webview?arg1=111&arg2=222"

                Uri uri = Uri.parse(message);
                // 如果url的協議 = js
                // 就解析往下解析參數
                if ( uri.getScheme().equals("js")) {

                    if (uri.getAuthority().equals("chooseAd")) {

                    }
                    return true;
                }

                return super.onJsPrompt(view, url, message, defaultValue, result);
            }


            //設置的Alert()函數
            @Override
            public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
                return super.onJsAlert(view, url, message, result);

            }

            // JS確認框
            @Override
            public boolean onJsConfirm(WebView view, String url, String message, JsResult result) {
                return super.onJsConfirm(view, url, message, result);
            }
            @Override
            public void onGeolocationPermissionsHidePrompt() {
                super.onGeolocationPermissionsHidePrompt();
                Log.d("1", "onGeolocationPermissionsHidePrompt");
            }

            @Override
            public void onGeolocationPermissionsShowPrompt(final String origin,
                                                           final GeolocationPermissions.Callback callback) {
                AlertDialog.Builder builder = new AlertDialog.Builder(ARwebView.this);
                builder.setMessage("允許存取地理資訊?");
                builder.setPositiveButton("允許", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        callback.invoke(origin, true, true);

                    }
                });
                builder.setNegativeButton("拒絕", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        callback.invoke(origin, false, false);


                    }
                });
                builder.show();
                super.onGeolocationPermissionsShowPrompt(origin, callback);
                Log.d("1", "onGeolocationPermissionsShowPrompt");
            }
            @Override
            public void onPermissionRequest(final PermissionRequest request) {
                Log.d("權限", "onPermissionRequest");
                ARwebView.this.runOnUiThread(new Runnable() {
                    @TargetApi(Build.VERSION_CODES.M)
                    @Override
                    public void run() {
                        Log.d("權限", request.getOrigin().toString());
                        request.grant(request.getResources());
                        /*if(request.getOrigin().toString().equals("file:///")) {
                            Log.d("權限", "GRANTED");
                            request.grant(request.getResources());
                        }
                        if(request.getOrigin().toString().equals("https://simondagg.github.io/webar/demo_gpsless/h1.html")) {
                            Log.d("權限", "GRANTED");
                            request.grant(request.getResources());
                        }else {
                            Log.d("權限", "DENIED");
                            request.deny();
                        }*/
                    }
                });

            }
            @Override
            public boolean onShowFileChooser(WebView view, ValueCallback<Uri[]> filePathCallback, WebChromeClient.FileChooserParams fileChooserParams) {

                uploadMessageAboveL = filePathCallback;
                Toast.makeText(ARwebView.this,"選擇圖片",Toast.LENGTH_SHORT).show();
                Intent galleryIntent = new Intent();
                galleryIntent.setType("image/*");
                //系統幫使用者找到裝置內合適的App來取得指定MIME類型的內容
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                //跳出選單讓使用者選擇要使用哪個
                startActivityForResult(Intent.createChooser(galleryIntent,"選擇圖片"),GALLERY_PICK);



                return true;
            }



        });



        if(UorS==0) {
            // 先載入JS，格式格式:file:///android_asset/文件名.html
            Toast.makeText(this, "使用者模式", Toast.LENGTH_SHORT).show();
            mWebView.loadUrl("https://simondagg.github.io/webar/pk/seletObj.html");
            //mWebView.loadUrl("file:///android_asset/seletObj.html");
        }
        else if(UorS==1){
            Toast.makeText(this, "店家模式", Toast.LENGTH_SHORT).show();
            mWebView.loadUrl("https://simondagg.github.io/webar/pk/creatad.html");
            //mWebView.loadUrl("file:///android_asset/creatad1.html");
        }
        else
            Toast.makeText(this, UorS, Toast.LENGTH_SHORT).show();


        //mWebView.loadUrl("https://simondagg.github.io/webar/screen_touch/4video7.html");



        //相機權限
        if(ContextCompat.checkSelfPermission(mContext, Manifest.permission.CAMERA)!= PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA},
                    1);
        }
        // BODY_SENSORS
        if(ContextCompat.checkSelfPermission(mContext, Manifest.permission.BODY_SENSORS)!= PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.BODY_SENSORS},
                    BODY_SENSORS);
        }
        // RECORD_AUDIO
        if(ContextCompat.checkSelfPermission(mContext, Manifest.permission.RECORD_AUDIO)!= PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.RECORD_AUDIO},
                    RECORD_AUDIO);
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_LOCATION);
        }

    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == GALLERY_PICK && resultCode == RESULT_OK) {

            Uri[] results = null;
            if (resultCode == RESULT_OK) {

                if (data != null) {
                    String dataString = data.getDataString();
                    // 複選檔案的結果大多是包在Intent的ClipData中
                    ClipData clipData = data.getClipData();
                    if (clipData != null) {

                        results = new Uri[clipData.getItemCount()];

                        for (int i = 0; i < clipData.getItemCount(); i++) {
                            ClipData.Item item = clipData.getItemAt(i);
                            results[i] = item.getUri();
                        }

                    }
                    if (dataString != null)
                        results = new Uri[]{Uri.parse(dataString)};
                }


            }
            uploadMessageAboveL.onReceiveValue(results);
            uploadMessageAboveL = null;
        }
        else {

            if (uploadMessageAboveL != null) {
                uploadMessageAboveL.onReceiveValue(null);
                uploadMessageAboveL = null;
            }
        }

        /*if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {

        }*/
    }
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
        // ----------------------- 創物件 ---------------------------
        public void createObject() {


            final String currentDate = DateFormat.getDateTimeInstance().format(new Date());

            final HashMap<String,String> objectMap = new HashMap<>();
            objectMap.put("title",object.getTitle());
            objectMap.put("content",object.getContent());
            objectMap.put("type",object.getType());
            objectMap.put("user",mCurrent_user.getUid());
            objectMap.put("date",currentDate);
            objectMap.put("3D_ObjId",object.getModelid());

            objectMap.put("Latitude",object.getLatitude());
            objectMap.put("Longitude",object.getLongitude());
            objectMap.put("District",object.getDistrict());

            //objectMap.put("Latitude", String.valueOf(latitude));
            //objectMap.put("Longitude",String.valueOf(longitude));

            // 物件ID (亂數) / data
            mObjectDatabase.child(object.getObjectid()).setValue(objectMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful()) {
                        Toast.makeText(ARwebView.this,"成功新建",Toast.LENGTH_SHORT).show();

                    }
                }
            });

            /*Query objectkey = mObjectDatabase.orderByKey().limitToLast(1);
            objectkey.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    String key="";
                    for (DataSnapshot Snapshot : dataSnapshot.getChildren()) {
                        key = Snapshot.getKey().toString();
                        Log.d("KEY",Snapshot.getKey());
                    }
                    key = String.valueOf((Integer.parseInt(key))+1);
                    mObjectDatabase.child(key).setValue(objectMap).addOnCompleteListener(new OnCompleteListener() {
                        @Override
                        public void onComplete(@NonNull Task task) {

                            if(task.isSuccessful()) {
                                SharedPreferences sharedPreferences = getSharedPreferences("ObjId" , MODE_PRIVATE);
                                sharedPreferences.edit().putString("index", object.getObjectid()).apply();
                                mWebView.reload();
                                Toast.makeText(ARwebView.this,"成功新建",Toast.LENGTH_SHORT).show();
                            }

                        }
                    });
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });*/




        }
        // ----------------------- 加入廣告收藏 ---------------------------
        public void addAdCollection() {

            final String currentDate = DateFormat.getDateTimeInstance().format(new Date());

            mRootRef.child("AdCollection").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if(dataSnapshot.hasChild(mCurrent_user.getUid())) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(ARwebView.this);
                        builder.setTitle("取消收藏廣告");
                        builder.setPositiveButton("確定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                mRootRef.child("AdCollection").child(id.getID()).child(mCurrent_user.getUid()).removeValue();
                                mRootRef.child("AdCollection").child(mCurrent_user.getUid()).child(id.getID()).removeValue();
                                Toast.makeText(ARwebView.this, "取消收藏廣告成功", Toast.LENGTH_SHORT).show();

                            }
                        });
                        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {


                            }
                        });
                        builder.show();
                    }
                    else {
                        AlertDialog.Builder builder = new AlertDialog.Builder(ARwebView.this);
                        builder.setTitle("收藏廣告");
                        builder.setPositiveButton("確定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {


                                final Map collectionObj = new HashMap();
                                collectionObj.put("date",currentDate);


                                mRootRef.child("AdCollection").child(id.getID()).child(mCurrent_user.getUid()).updateChildren(collectionObj);
                                mRootRef.child("AdCollection").child(mCurrent_user.getUid()).child(id.getID()).updateChildren(collectionObj);
                                Toast.makeText(ARwebView.this, "收藏廣告成功", Toast.LENGTH_SHORT).show();

                            }
                        });
                        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {


                            }
                        });
                        builder.show();
                    }
                }


                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

        }
        // ----------------------- 加入優惠券收藏 ---------------------------
        public void addCouponCollection() {

            final String currentDate = DateFormat.getDateTimeInstance().format(new Date());

            mRootRef.child("CouponCollection").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if(dataSnapshot.hasChild(mCurrent_user.getUid())) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(ARwebView.this);
                        builder.setTitle("取消收藏優惠券");
                        builder.setPositiveButton("確定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                mRootRef.child("CouponCollection").child(id.getID()).child(mCurrent_user.getUid()).removeValue();
                                mRootRef.child("CouponCollection").child(mCurrent_user.getUid()).child(id.getID()).removeValue();
                                Toast.makeText(ARwebView.this, "取消收藏優惠券成功", Toast.LENGTH_SHORT).show();

                            }
                        });
                        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {


                            }
                        });
                        builder.show();
                    }
                    else {
                        AlertDialog.Builder builder = new AlertDialog.Builder(ARwebView.this);
                        builder.setTitle("收藏優惠券");
                        builder.setPositiveButton("確定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {


                                final Map collectionObj = new HashMap();
                                collectionObj.put("date",currentDate);
                                collectionObj.put("pay",false);

                                mRootRef.child("CouponCollection").child(id.getID()).child(mCurrent_user.getUid()).updateChildren(collectionObj);
                                mRootRef.child("CouponCollection").child(mCurrent_user.getUid()).child(id.getID()).updateChildren(collectionObj);
                                Toast.makeText(ARwebView.this, "收藏優惠券成功", Toast.LENGTH_SHORT).show();

                            }
                        });
                        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {


                            }
                        });
                        builder.show();
                    }
                }


                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

        }
        // ----------------------- 檢舉物件 ---------------------------
        public void addReport() {

            final String currentDate = DateFormat.getDateTimeInstance().format(new Date());
            final Map reportObj = new HashMap();
            reportObj.put("date",currentDate);
            mRootRef.child("Report").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if(dataSnapshot.hasChild(id.getID())){

                        AlertDialog.Builder builder = new AlertDialog.Builder(ARwebView.this);
                        builder.setTitle("取消檢舉");
                        builder.setPositiveButton("確定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                mRootRef.child("Report").child(id.getID()).removeValue();
                                Toast.makeText(ARwebView.this, "取消成功", Toast.LENGTH_SHORT).show();
                                report_state = "未檢舉";
                            }
                        });
                        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                            }
                        });
                        builder.show();
                    }
                    else {
                        CharSequence option[] = new CharSequence[]{"廣告不實", "含暴力、騷擾之言論"};
                        AlertDialog.Builder builder = new AlertDialog.Builder(ARwebView.this);
                        builder.setTitle("選擇一個項目");
                        builder.setItems(option, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                                if (i == 0) {
                                    reportObj.put("檢舉理由", "廣告不實");
                                    mRootRef.child("Report").child(id.getID()).child(mCurrent_user.getUid()).updateChildren(reportObj);
                                    Toast.makeText(ARwebView.this, "檢舉成功", Toast.LENGTH_SHORT).show();
                                    report_state = "已檢舉";
                                }
                                if (i == 1) {
                                    reportObj.put("檢舉理由", "含暴力、騷擾之言論");
                                    mRootRef.child("Report").child(id.getID()).child(mCurrent_user.getUid()).updateChildren(reportObj);
                                    Toast.makeText(ARwebView.this, "檢舉成功", Toast.LENGTH_SHORT).show();
                                    report_state = "已檢舉";

                                }
                            }
                        });
                        builder.show();
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

            //mRootRef.child("Report").child(mCurrent_user.getUid()).child(id.getID()).updateChildren(reportObj);


        }
        // ---------------------- 創優惠券 --------------------------

        public void createCoupon() {

            final String currentDate = DateFormat.getDateTimeInstance().format(new Date());
            final HashMap<String,String> couponMap = new HashMap<>();
            couponMap.put("title",coupon.getTitle());
            couponMap.put("content",coupon.getContent());
            couponMap.put("user",mCurrent_user.getUid());
            couponMap.put("date",currentDate);

            couponMap.put("district",coupon.getDistrict());

            // 廣告物件ID (亂數) / data
            mCouponDatabase.child(coupon.getCouponid()).setValue(couponMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful()) {
                        Toast.makeText(ARwebView.this,"成功新建",Toast.LENGTH_SHORT).show();

                    }
                }
            });

        }
        // ----------------------- 創廣告 ---------------------------
        public void createAdvertisement() {



            final String currentDate = DateFormat.getDateTimeInstance().format(new Date());
            final HashMap<String,String> ad_objectMap = new HashMap<>();
            ad_objectMap.put("title",ad_object.getTitle());
            ad_objectMap.put("content",ad_object.getContent());
            ad_objectMap.put("image",ad_object.getDownloadUrl());
            ad_objectMap.put("user",mCurrent_user.getUid());
            ad_objectMap.put("date",currentDate);

            ad_objectMap.put("Latitude",ad_object.getLatitude());
            ad_objectMap.put("Longitude",ad_object.getLongitude());

            //ad_objectMap.put("Latitude", String.valueOf(latitude));
            //ad_objectMap.put("Longitude",String.valueOf(longitude));



            // 廣告物件ID (亂數) / data
            mAdObjectDatabase.child(ad_object.getAd_objectid()).setValue(ad_objectMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful()) {
                        Toast.makeText(ARwebView.this,"成功新建",Toast.LENGTH_SHORT).show();

                    }
                }
            });

        }

        private final class WebViewClientImpl extends WebViewClient
        {
            @Override
            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                handler.proceed(); // Ignore SSL certificate errors
            }
            @Override
            public void onLoadResource(WebView view, String url) {
                // TODO Auto-generated method stub

                super.onLoadResource(view, url);
            }
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }

            @Override
            public void onPageFinished(WebView view, String url)
            {



                //傳入使用者ID至webview上
                view.loadUrl("javascript:window.ExtObj_userId.responseResult(currentUser(\"" + mCurrent_user.getUid() + "\"))");
                view.loadUrl("javascript:window.ExtObj_modelId.responseResult(setModel())");
                //view.loadUrl("javascript:window.ExtObj_objectId.responseResult(currentObject(\"" + obID+ "\"))");

                //webview資料回傳到Android 方法一
                //view.loadUrl("javascript:window.ExtObj_addFriend.responseResult(objectId())");


            }
        }
        //-----------------------接收來自js的值------------------------

        private class JsCallBack_userId
        {
            @JavascriptInterface
            public void responseResult(final String result)
            {

            }
        }
        private class JsCallBack_modelId
        {
            @JavascriptInterface
            public void responseResult(final String result)
            {

            }
        }
        private class JsCallBack_createCoupon
        {
            @JavascriptInterface
            public void responseResult(final String[] result)
            {

                String title,content,couponId,district;

                title = result[0];
                content = result[1];
                couponId = result[2];
                district = result[3];


                coupon.setTitle(title);
                coupon.setContent(content);
                coupon.setCouponid(couponId);

                coupon.setDistrict(district);


                //Toast.makeText(ARwebView.this,"建立成功",Toast.LENGTH_SHORT).show();

                createCoupon();

            }
        }

        private class JsCallBack_createAd
        {
            @JavascriptInterface
            public void responseResult(final String[] result)
            {

                String title,content,downloadUrl,Ad_objectId,latitude,longitude;

                title = result[0];
                content = result[1];
                downloadUrl = result[2];
                Ad_objectId = result[3];

                latitude = result[4];
                longitude = result[5];


                ad_object.setTitle(title);
                ad_object.setContent(content);
                ad_object.setDownloadUrl(downloadUrl);
                ad_object.setAd_objectid(Ad_objectId);

                ad_object.setLatitude(latitude);
                ad_object.setLongitude(longitude);


                //Toast.makeText(ARwebView.this,"建立成功",Toast.LENGTH_SHORT).show();

                createAdvertisement();

            }
        }
        private class JsCallBack_createObject
        {

            @JavascriptInterface
            public void responseResult(final String[] result)
            {

                String title,content,type,objectId,modelId,latitude,longitude,district;

                title = result[0];
                content = result[1];
                type = result[2];
                objectId = result[3];
                modelId = result[4];

                latitude = result[5];
                longitude = result[6];
                district = result[7];


                object.setTitle(title);
                object.setContent(content);
                object.setType(type);
                object.setObjectid(objectId);
                object.setModelid(modelId);

                object.setLatitude(latitude);
                object.setLongitude(longitude);
                object.setDistrict(district);


                //Toast.makeText(ARwebView.this,object.getLatitude(),Toast.LENGTH_SHORT).show();
                Toast.makeText(ARwebView.this,"建立成功",Toast.LENGTH_SHORT).show();
                //Toast.makeText(ARwebView.this,"建立成功",Toast.LENGTH_SHORT).show();
                createObject();

            }

        }
        private class JsCallBack_addFriend
        {

            @JavascriptInterface
            public void responseResult(final String result)
            {

                //
                id.setID(result);
                Log.d("使用者ID",id.getID());
                if(mCurrent_state.equals("已送出邀請")) {
                    mFriendReqDatabase.child(mCurrent_user.getUid()).child(user_id).removeValue();
                    mFriendReqDatabase.child(user_id).child(mCurrent_user.getUid()).removeValue();
                }
                else if(mCurrent_state.equals("已接受邀請")) {
                    final String currentDate = DateFormat.getDateTimeInstance().format(new Date());
                    Map friendsMap = new HashMap();
                    friendsMap.put("Friends/" + mCurrent_user.getUid() + "/" +user_id +"/date",currentDate);
                    friendsMap.put("Friends/" +user_id + "/" + mCurrent_user.getUid() +"/date",currentDate);

                    friendsMap.put("Friend_req/" + mCurrent_user.getUid() + "/" +user_id,null);
                    friendsMap.put("Friend_req/" + user_id + "/" + mCurrent_user.getUid(),null);

                    mRootRef.updateChildren(friendsMap);
                }
                else if (mCurrent_state.equals("朋友")) {
                    Map unfriendMap = new HashMap();
                    unfriendMap.put("Friends/" + mCurrent_user.getUid() + "/" + user_id,null);
                    unfriendMap.put("Friends/" + user_id + "/" + mCurrent_user.getUid(),null);
                    mRootRef.updateChildren(unfriendMap);
                }
                else {
                    addFriend();
                    Toast.makeText(ARwebView.this,"加好友成功",Toast.LENGTH_SHORT).show();
                }
                //Toast.makeText(ARwebView.this,id.getID(),Toast.LENGTH_SHORT).show();
            }

        }


        private class JsCallBack_adCollection
        {

            @JavascriptInterface
            public void responseResult(final String r)
            {

                //
                id.setID(r);
                Log.d("廣告ID",id.getID());
                addAdCollection();
                //Toast.makeText(ARwebView.this,id.getID(),Toast.LENGTH_SHORT).show();
            }

        }
        private class JsCallBack_couponCollection
        {

            @JavascriptInterface
            public void responseResult(final String r)
            {

                //
                id.setID(r);
                Log.d("優惠券ID",id.getID());
                addCouponCollection();
                //Toast.makeText(ARwebView.this,"建立成功",Toast.LENGTH_SHORT).show();
            }

        }

        private class JsCallBack_report
        {

            @JavascriptInterface
            public void responseResult(final String r)
            {

                id.setID(r);
                Log.d("物件ID",id.getID());
                addReport();
                //Toast.makeText(ARwebView.this,id.getID(),Toast.LENGTH_SHORT).show();
            }

        }
    //值改變就會呼叫 // sensorEvent讀取感測器的值
    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {

       /*if(mx == 0) {

            // 因為X.Y加速度單一方向變化值為 -10 ~ 10所以可以將移動範圍分成20等分
            mx = (layout.getWidth()-igv.getWidth()) /20.0;
            my = (layout.getHeight()-igv.getHeight()) /20.0;

        }

        // 取得圖的LayoutParams物件:用於設定邊界
        RelativeLayout.LayoutParams parms = (RelativeLayout.LayoutParams) igv.getLayoutParams();
        // 若右邊抬起(X加速度變大) 圖會往左移
        parms.leftMargin = (int) ((10-sensorEvent.values[0]) * mx);
        // 若上面抬起(Y加速度變大) 圖會往下移
        parms.topMargin = (int) ((10+sensorEvent.values[1]) * my);
        igv.setLayoutParams(parms);*/

        // %1.2f相當於將float值格式化為包含二位小數的字串
        //txv.setText(String.format("X: %1.2f\n\nY: %1.2f\n\nZ: %1.2f",sensorEvent.values[0],sensorEvent.values[1],sensorEvent.values[2]));
        txv.setText(" ");
    }

    //感測器精確度改變時呼叫 // i 讀取改變後的精確度
    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {


    }

    // activity畫面出來時
    @Override
    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(this, mSensorAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, mSensorAmbientTemp, SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, mSensorGravity, SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, mSensorGyroscope, SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, mSensorLight, SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, mSensorLinearAcceleration, SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, mSensorMagneticField, SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, mSensorPressure, SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, mSensorProximity, SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, mSensorRelativeHumidity, SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, mSensorRotationVector, SensorManager.SENSOR_DELAY_NORMAL);
    }


    // 切換到其他activity時候
    @Override
    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);
    }

   @Override public boolean onKeyDown(int keyCode, KeyEvent event)  {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            new AlertDialog.Builder(this)
                    .setTitle("結束AR")
                    .setMessage("是否退出AR")
                    .setPositiveButton("是",new DialogInterface.OnClickListener() {
                        public void onClick(final DialogInterface dialog, int id) {


                            mUserDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if(dataSnapshot.hasChild(mCurrent_user.getUid())) {
                                        dialog.dismiss();
                                        finish();
                                        startActivity(new Intent(ARwebView.this,Navigation.class));
                                    }
                                    else {
                                        mRootRef.child("Store").addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                if(dataSnapshot.hasChild(mCurrent_user.getUid())) {
                                                    finish();
                                                    startActivity(new Intent(ARwebView.this,Store_navigation.class));
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

    // ----------------------- 取得經緯度 ---------------------------

/*
    private void getLocation(Context context) {
        //1.获取位置管理器
        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        //2.获取位置提供器，GPS或是NetWork
        List<String> providers = locationManager.getProviders(true);

        if (providers.contains(LocationManager.GPS_PROVIDER)) {
            //如果是GPS定位
            locationProvider = LocationManager.GPS_PROVIDER;
        } else if (providers.contains(LocationManager.NETWORK_PROVIDER)) {
            //如果是网络定位
            locationProvider = LocationManager.NETWORK_PROVIDER;
        } else {

            return;
        }

        //3.获取上次的位置，一般第一次运行，此值为null
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }

        Location location = locationManager.getLastKnownLocation(locationProvider);
        if (location!=null){
            latitude = location.getLatitude();
            longitude = location.getLongitude();
        }
        // 监视地理位置变化，第二个和第三个参数分别为更新的最短时间minTime和最短距离minDistace
        locationManager.requestLocationUpdates(locationProvider, 0, 0,mListener);
    }
    LocationListener mListener = new LocationListener() {
        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
        }
        @Override
        public void onProviderEnabled(String provider) {
        }
        @Override
        public void onProviderDisabled(String provider) {
        }
        // 如果位置发生变化，重新显示
        @Override
        public void onLocationChanged(Location location) {
            latitude = location.getLatitude();
            longitude = location.getLongitude();
        }
    };

    // 移除定位監聽
    public void removeLocationUpdatesListener() {
        // 檢查權限
        if (
                ActivityCompat.checkSelfPermission( this, android.Manifest.permission.ACCESS_FINE_LOCATION ) != PackageManager.PERMISSION_GRANTED &&
                        ActivityCompat.checkSelfPermission( this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return  ;
        }
        if (locationManager != null) {

            locationManager.removeUpdates(mListener);
        }
    }
*/

    @Override
    public void onStart() {
        super.onStart();
        //getLocation(this);



    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        //removeLocationUpdatesListener();

    }
    @Override
    public void onConfigurationChanged(Configuration newConfig){
        super.onConfigurationChanged(newConfig);
    }


}
