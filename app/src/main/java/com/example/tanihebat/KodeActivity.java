package com.example.tanihebat;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class KodeActivity extends AppCompatActivity {
    EditText edtMasukID;
    Button btnSubmit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_kode);

        edtMasukID = findViewById(R.id.edtMasukID);
        btnSubmit = findViewById(R.id.btnSubmit);

        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (edtMasukID.getText().toString().equals("TaniA01")) {
                    Toast.makeText(KodeActivity.this,"Berhasil Masuk Aplikasi",Toast.LENGTH_SHORT).show();
                    Intent i = new Intent(KodeActivity.this, MainActivity.class);
                    startActivity(i);
                } else {
                    Toast.makeText(KodeActivity.this,"ID Tidak Sesuai, Gagal Masuk Aplikasi",Toast.LENGTH_SHORT).show();
                }
            }
        });

    }
}