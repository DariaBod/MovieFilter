package com.example.moviefilter;

import android.content.Intent;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
    public void shoot(View view){
        Intent intent = new Intent(MainActivity.this, Editor.class);
        intent.putExtra("photo", 1);
        startActivity(intent);
    }
    public void gallery(View view){
        Intent intent = new Intent(MainActivity.this, Editor.class);
        intent.putExtra("photo", 2);
        startActivity(intent);
    }

}
