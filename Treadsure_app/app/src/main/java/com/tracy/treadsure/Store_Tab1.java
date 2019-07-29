package com.tracy.treadsure;

/**
 * Created by angoo on 2018/1/31.
 */

import android.*;
import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class Store_Tab1 extends Fragment implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,LocationListener {
    MapView mapView;
    View rootView;
    private static final int REQUEST_LOCATION = 2;
    private GoogleMap mMap;
    //持續更新位置存取更多資訊時要和goolge建立連線
    GoogleApiClient mGoogleApiClient;
    LocationRequest locationRequest;
    private DatabaseReference mAdObjectDatabase,mCouponDatabase;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.tab1, container, false);
        mAdObjectDatabase = FirebaseDatabase.getInstance().getReference().child("AdObject");
        mCouponDatabase = FirebaseDatabase.getInstance().getReference().child("Coupon");
        return rootView;
    }

    @Override
    public void onViewCreated(View view,Bundle savedInstanceState) {
        super.onViewCreated(view,savedInstanceState);

        mapView = (MapView) rootView.findViewById(R.id.mapView);
        if(mapView != null) {
            mapView.onCreate(null);
            mapView.onResume();
            mapView.getMapAsync(this);
        }
        //還未真正連線到google
        if(mGoogleApiClient==null){
            mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                    .addConnectionCallbacks(this) //加入連線後回報對象
                    .addOnConnectionFailedListener(this)//連線失敗回報對象
                    .addApi(LocationServices.API).build();

        }
        //自行設計的方法
        createLocationRequest();

    }
    private void createLocationRequest(){
        locationRequest = new LocationRequest();
        //回報速率 更新間隔 (可能其他app在用map)
        locationRequest.setInterval(5000);
        //最快的回報速率 最短間隔 (最少間隔幾秒才會收到下一位置的更新)
        locationRequest.setFastestInterval(2000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

    }
    @Override
    public void onMapReady(GoogleMap googleMap) {
       /* MapsInitializer.initialize(getContext());
        map = googleMap;
        googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        googleMap.addMarker(new MarkerOptions().position(new LatLng(25.037542,121.564433)).title("SA").snippet("DD"));
        CameraPosition Liberty = CameraPosition.builder().target(new LatLng(25.037117,121.563089)).zoom(18).bearing(0).tilt(45).build();
        googleMap.moveCamera(CameraUpdateFactory.newCameraPosition(Liberty));*/
        mMap = googleMap;
        if (ActivityCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // ActivityCompat.requestPermissions(Context,危險權限字串陣列,按下對話框後的辨認碼常數)
            ActivityCompat.requestPermissions(getActivity(), new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);

        } else {
            mMap.setMyLocationEnabled(true);
            setupMyLocation();
        }
        mMap.getUiSettings().setZoomControlsEnabled(true);
        /*
        LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        CameraUpdate地圖元件的視點
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));*/

/*
        //LatLng傳入並建立經緯度座標
        LatLng taipei101 = new LatLng(23.895760,121.535024);
        //先移動視角
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(taipei101,16));
        //在增加標記為101 MarkerOptions()產生具有選項資訊的標記選項物件
        Marker marker = mMap.addMarker(new MarkerOptions()
                .position(taipei101)
                .title("翠絲")
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.p))
                .snippet("18歲"));

        mMap.moveCamera(CameraUpdateFactory.newLatLng(taipei101));
        marker.showInfoWindow();

        LatLng taipeicityhall = new LatLng(23.894842,121.535049);
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(taipeicityhall,16));
        Marker marker1 = mMap.addMarker(new MarkerOptions()
                .position(taipeicityhall)
                .title("桃樂絲")
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.bear))
                .snippet("19歲"));
        marker1.showInfoWindow(); //呼叫此方法顯示視窗 不需要點圖示也能顯示資訊

        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                new AlertDialog.Builder(getActivity())
                        .setTitle(marker.getTitle())
                        .setMessage("是否進入AR介面")
                        .setPositiveButton("是",new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                getActivity().finish();
                                startActivity(new Intent(getActivity(),ARwebView.class));
                            }
                        })
                        .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                            }
                        })
                        .show();
            }
        });*/
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_LOCATION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    setupMyLocation();
                    mMap.setMyLocationEnabled(true);
                    /*if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {


                    } else {
                        //
                    }*/
                }
                else {

                }
                break;
        }
    }

    @SuppressLint("MissingPermission")
    private void setupMyLocation() {
        mMap.setMyLocationEnabled(true);
        //按下 我的位置 按鈕時的事件listener
        mMap.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {
            @Override
            //按下按鈕時執行
            public boolean onMyLocationButtonClick() {
                //透過位置服務，取得目前裝置所在
                //getSystemServiced取得系統服務 LOCATION_SERVICE取得LocationManager
                LocationManager locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);

                Criteria criteria = new Criteria();
                //設定為存取精確
                criteria.setAccuracy(Criteria.ACCURACY_FINE);
                // 向系統查詢最合適的服務提供者名稱 ( 通常也是 "gps")
                String provider = locationManager.getBestProvider(criteria,true);
                //getLastKnownLocation取得目前裝置位置
                Location location = locationManager.getLastKnownLocation(provider);


                if(location!=null) {
                    //印除錯訊息
                    Log.i("LOCATION",location.getLatitude()+"/"+location.getLongitude());
                    //Zoom放大地圖到第15級CameraUpdateFactory.newLatLngZoom (取得位置,15)
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(),location.getLongitude()),16));
                }

                return false;
            }

        });
    }
    //連線時實作
    @Override
    public void onStart() {
        mGoogleApiClient.connect();
        super.onStart();
    }
    //中斷連線實作
    @Override
    public void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    //連線成功時呼叫
    @SuppressLint("MissingPermission")
    @Override
    public void onConnected(@Nullable Bundle bundle) { // 增加特定目標時註腳掉避免 目前位置會影響到101
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // ActivityCompat.requestPermissions(Context,危險權限字串陣列,按下對話框後的辨認碼常數)
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);

        } else {
            @SuppressLint("MissingPermission")
            Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            if(location!=null) {
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(),location.getLongitude()),18));
                //註冊位置更新listener介面
            }
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient,locationRequest,this);


        }

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        if(location!=null) {
            Log.d("LOCATION",location.getLatitude()+","+location.getLongitude());
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(),location.getLongitude()),18));
        }
        mAdObjectDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                for (DataSnapshot Snapshot : dataSnapshot.getChildren()) {
                    String title = Snapshot.child("title").getValue().toString();
                    String content = Snapshot.child("content").getValue().toString();
                    Double latitude = Double.parseDouble(Snapshot.child("Latitude").getValue().toString());
                    Double longitude = Double.parseDouble(Snapshot.child("Longitude").getValue().toString());
                    //LatLng傳入並建立經緯度座標
                    LatLng taipei101 = new LatLng(latitude,longitude);
                    //先移動視角
                    //mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(taipei101,16));
                    //在增加標記為101 MarkerOptions()產生具有選項資訊的標記選項物件
                    Marker marker = mMap.addMarker(new MarkerOptions()
                            .position(taipei101)
                            .title(title)
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.circle_red))
                            .snippet(content));

                    //mMap.moveCamera(CameraUpdateFactory.newLatLng(taipei101));
                    marker.showInfoWindow();//呼叫此方法顯示視窗 不需要點圖示也能顯示資訊
                }



            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        mCouponDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                for (DataSnapshot Snapshot : dataSnapshot.getChildren()) {
                    String title = Snapshot.child("title").getValue().toString();
                    String content = Snapshot.child("content").getValue().toString();
                    Double latitude = Double.parseDouble(Snapshot.child("Latitude").getValue().toString());
                    Double longitude = Double.parseDouble(Snapshot.child("Longitude").getValue().toString());
                    //LatLng傳入並建立經緯度座標
                    LatLng taipei101 = new LatLng(latitude,longitude);
                    //先移動視角
                    //mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(taipei101,16));
                    //在增加標記為101 MarkerOptions()產生具有選項資訊的標記選項物件
                    Marker marker = mMap.addMarker(new MarkerOptions()
                            .position(taipei101)
                            .title(title)
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.circle_red))
                            .snippet(content));

                    //mMap.moveCamera(CameraUpdateFactory.newLatLng(taipei101));
                    marker.showInfoWindow();//呼叫此方法顯示視窗 不需要點圖示也能顯示資訊
                }



            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        //LatLng傳入並建立經緯度座標
        LatLng taipei101 = new LatLng(120,23);
        //先移動視角
        //mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(taipei101,16));
        //在增加標記為101 MarkerOptions()產生具有選項資訊的標記選項物件
        Marker marker = mMap.addMarker(new MarkerOptions()
                .position(taipei101)
                .title("哈囉")
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.circle))
                .snippet("妳好"));

        //mMap.moveCamera(CameraUpdateFactory.newLatLng(taipei101));
        marker.showInfoWindow();//呼叫此方法顯示視窗 不需要點圖示也能顯示資訊
        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                new AlertDialog.Builder(getActivity())
                        .setTitle(marker.getTitle())
                        .setMessage("是否進入AR介面")
                        .setPositiveButton("是",new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                getActivity().finish();
                                startActivity(new Intent(getActivity(),ARwebView.class));
                            }
                        })
                        .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                            }
                        })
                        .show();
            }
        });

    }
}
