package com.example.kelimesihirbazi;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class HomeActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        findViewById(R.id.btnSinav).setOnClickListener(v -> startActivity(new Intent(this, DashboardActivity.class)));
        findViewById(R.id.btnWordle).setOnClickListener(v -> startActivity(new Intent(this, WordleActivity.class)));
        findViewById(R.id.btnEkle).setOnClickListener(v -> startActivity(new Intent(this, AddWordActivity.class)));
    }
}