package com.example.kelimesihirbazi;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class HomeActivity extends AppCompatActivity {


    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        dbHelper = new DatabaseHelper(this);

        // Arayüzdeki butonları bağlar
        Button btnSinav = findViewById(R.id.btnSinav);
        Button btnWordle = findViewById(R.id.btnWordle);
        Button btnEkle = findViewById(R.id.btnEkle);
        Button btnAyarlar = findViewById(R.id.btnAyarlar);
        Button btnLlm = findViewById(R.id.btnLlm);
        Button btnAnaliz = findViewById(R.id.btnAnaliz);


        btnSinav.setOnClickListener(v -> startActivity(new Intent(HomeActivity.this, DashboardActivity.class)));
        btnWordle.setOnClickListener(v -> startActivity(new Intent(HomeActivity.this, WordleActivity.class)));
        btnEkle.setOnClickListener(v -> startActivity(new Intent(HomeActivity.this, AddWordActivity.class)));
        btnAyarlar.setOnClickListener(v -> startActivity(new Intent(HomeActivity.this, SettingsActivity.class)));
        btnLlm.setOnClickListener(v -> startActivity(new Intent(HomeActivity.this, WordChainActivity.class)));

        // Analiz Raporunu tetikleyen buton
        btnAnaliz.setOnClickListener(v -> showAnalysisReport());
    }

    private void showAnalysisReport() {
        // Kullanıcı ID'sini hafızadan alır
        int userId = getSharedPreferences("KullaniciVerileri", MODE_PRIVATE).getInt("aktifKullaniciID", 1);


        String reportText = dbHelper.generateAnalysisReport(userId);

        new AlertDialog.Builder(this)
                .setTitle("Büyücülük Analiz Raporu")
                .setMessage(reportText)
                .setPositiveButton("Parşömene Aktar (Paylaş)", (dialog, which) -> {
                    Intent sendIntent = new Intent();
                    sendIntent.setAction(Intent.ACTION_SEND);
                    sendIntent.putExtra(Intent.EXTRA_TEXT, "Kelime Sihirbazı Başarı Raporum:\n\n" + reportText);
                    sendIntent.setType("text/plain");
                    startActivity(Intent.createChooser(sendIntent, "Raporu Paylaş"));
                })
                .setNegativeButton("Kapat", null)
                .show();
    }
}