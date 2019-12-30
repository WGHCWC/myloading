package com.wghcwc.myload;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.wghcwc.myloading.LoadingUtils;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        LoadingUtils.show();
    }
}
