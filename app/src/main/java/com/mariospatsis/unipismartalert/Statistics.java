package com.mariospatsis.unipismartalert;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class Statistics extends AppCompatActivity {
    FirebaseService mFirebaseService = new FirebaseService();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);
    }
}
