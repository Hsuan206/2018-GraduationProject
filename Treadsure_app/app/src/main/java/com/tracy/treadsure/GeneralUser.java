package com.tracy.treadsure;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

public class GeneralUser extends AppCompatActivity {
    TextView generaluser_txv;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_general_user);
        generaluser_txv=(TextView)findViewById(R.id.generaluser_txv);
    }
}
