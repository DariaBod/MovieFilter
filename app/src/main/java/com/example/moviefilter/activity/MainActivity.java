package com.example.moviefilter.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.example.moviefilter.R;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        Log.d("MyTag", String.valueOf(metrics));
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void shoot(View view) {
        Intent intent = new Intent(MainActivity.this, Editor.class);
        intent.putExtra("photo", 1);
        startActivity(intent);
        finish();
    }

    public void gallery(View view) {
        Intent intent = new Intent(MainActivity.this, Editor.class);
        intent.putExtra("photo", 2);
        startActivity(intent);
        finish();
    }
}