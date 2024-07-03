package com.example.tanihebat;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class LoadingScreenActivity extends AppCompatActivity {

    private Handler handler = new Handler();

    private Runnable runnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading_screen);

        runnable = new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(LoadingScreenActivity.this, KodeActivity.class);
                startActivity(intent);
                finish();
            }
        };
        handler.postDelayed(runnable,2000);
    }
    protected void onDestroy(){
        super.onDestroy();
        handler.removeCallbacks(runnable);
    }
}