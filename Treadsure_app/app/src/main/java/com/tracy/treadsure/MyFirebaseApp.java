package com.tracy.treadsure;

import com.google.firebase.database.FirebaseDatabase;

/**
 * Created by angoo on 2018/7/31.
 */

public class MyFirebaseApp extends android.app.Application {

        @Override
        public void onCreate() {
            super.onCreate();
    /* Enable disk persistence  */
            FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        }
}