package com.tracy.treadsure;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

public class BusinessUser extends AppCompatActivity {
    TextView businessuser_txv;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_business_user);
        businessuser_txv=(TextView)findViewById(R.id.businessuser_txv);
    }
}
