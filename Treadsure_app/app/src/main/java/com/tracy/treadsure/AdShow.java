package com.tracy.treadsure;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class AdShow extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ad_show);
        MyApplication.getInstance().addActivity(this);

    }
}
