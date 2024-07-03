package com.example.tanihebat;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.squareup.picasso.Picasso;


public class ResultActivity extends AppCompatActivity {

    private ImageView ivImage;
    private TextView tvResultMasalah, tvResultSolusi, tvJudul;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        ivImage = findViewById(R.id.ivImage);
        tvResultMasalah = findViewById(R.id.tvResultMasalah);
        tvJudul = findViewById(R.id.tvJudul);
        tvResultSolusi = findViewById(R.id.tvResultSolusi);

        Intent intent = getIntent();
        String imageUrl = intent.getStringExtra("imageUri");
        String problem = intent.getStringExtra("problem");
        String solution = intent.getStringExtra("solution");
        String category = intent.getStringExtra("category");

        Picasso.get().load(imageUrl).into(ivImage);
        tvResultMasalah.setText(problem);
        tvResultSolusi.setText(solution);
        tvJudul.setText(category);
    }
}