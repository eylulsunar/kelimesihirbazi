package com.example.kelimesihirbazi;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class DashboardActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        dbHelper = new DatabaseHelper(this);

        Button btnKelimeEkle = findViewById(R.id.btnKelimeEkle);
        Button btnSinav = findViewById(R.id.btnSinav);
        Button btnWordle = findViewById(R.id.btnWordle);
        Button btnWordChain = findViewById(R.id.btnWordChain);
        Button btnAnaliz = findViewById(R.id.btnAnaliz);
        Button btnAyarlar = findViewById(R.id.btnAyarlar);

        // Diğer ekranlara geçiş yönlendirmeleri
        btnKelimeEkle.setOnClickListener(v -> startActivity(new Intent(DashboardActivity.this, AddWordActivity.class)));
        btnSinav.setOnClickListener(v -> startActivity(new Intent(DashboardActivity.this, ExamActivity.class)));
        btnWordle.setOnClickListener(v -> startActivity(new Intent(DashboardActivity.this, WordleActivity.class)));
        btnWordChain.setOnClickListener(v -> startActivity(new Intent(DashboardActivity.this, WordChainActivity.class)));

        btnAnaliz.setOnClickListener(v -> showAnalysisDialog());
        btnAyarlar.setOnClickListener(v -> showSettingsDialog());
    }

    private void showAnalysisDialog() {
        String reportText = dbHelper.generateAnalysisReport();

        new AlertDialog.Builder(this)
                .setTitle("Analiz Raporu")
                .setMessage(reportText)
                .setPositiveButton("Çıktı Al / Paylaş", (dialog, which) -> {
                    Intent sendIntent = new Intent();
                    sendIntent.setAction(Intent.ACTION_SEND);
                    sendIntent.putExtra(Intent.EXTRA_TEXT, "Kelime Sihirbazı Analiz Raporu\n\n" + reportText);
                    sendIntent.setType("text/plain");
                    startActivity(Intent.createChooser(sendIntent, "Raporu Paylaş"));
                })
                .setNegativeButton("Kapat", null)
                .show();
    }

    private void showSettingsDialog() {
        SharedPreferences preferences = getSharedPreferences("KelimeSihirbaziAyarlar", MODE_PRIVATE);
        int currentLimit = preferences.getInt("yeniKelimeSayisi", 10);

        EditText input = new EditText(this);
        input.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        input.setText(String.valueOf(currentLimit));
        input.setPadding(50, 40, 50, 40);

        new AlertDialog.Builder(this)
                .setTitle("Sınav Ayarları")
                .setMessage("Günlük yeni kelime sınırını belirleyin:")
                .setView(input)
                .setPositiveButton("Kaydet", (dialog, which) -> {
                    String value = input.getText().toString().trim();
                    if (!value.isEmpty()) {
                        preferences.edit().putInt("yeniKelimeSayisi", Integer.parseInt(value)).apply();
                        Toast.makeText(DashboardActivity.this, "Sınır güncellendi.", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("İptal", null)
                .show();
    }
}