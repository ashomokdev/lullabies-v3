package com.ashomok.lullabies.ui.splash_activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.ashomok.lullabies.ui.main_activity.MusicPlayerActivity;

/**
 * Created by iuliia on 3/2/18.
 */
public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = new Intent(this, MusicPlayerActivity.class);
        startActivity(intent);
        finish();
    }
}