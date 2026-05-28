package com.example.kelimesihirbazi;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class SettingsActivity extends AppCompatActivity {

    private EditText etKelimeLimiti;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        etKelimeLimiti = findViewById(R.id.etKelimeLimiti);
        Button btnAyarlariKaydet = findViewById(R.id.btnAyarlariKaydet);

        // Kullanıcının belirlediği kelime limitini cihaz hafızasında tutmak için SharedPreferences kullanılır.

        sharedPreferences = getSharedPreferences("AyarlarDosyasi", MODE_PRIVATE);

        // Kullanıcı daha önce bir ayar yapmadıysa, proje gereksinim dokümanında istenen varsayılan 10 sayısını getirilir.
        int mevcutLimit = sharedPreferences.getInt("gunlukLimit", 10);
        etKelimeLimiti.setText(String.valueOf(mevcutLimit));

        btnAyarlariKaydet.setOnClickListener(v -> {
            String girilenDeger = etKelimeLimiti.getText().toString().trim();

            if (girilenDeger.isEmpty()) {
                Toast.makeText(this, "Lütfen geçerli bir sayı girin.", Toast.LENGTH_SHORT).show();
                return;
            }

            int yeniLimit = Integer.parseInt(girilenDeger);

            // Kullanıcının girdiği yeni değeri Editor aracılığıyla SharedPreferences dosyasına kaydedilir.
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putInt("gunlukLimit", yeniLimit);
            editor.apply();

            Toast.makeText(this, "Ayarlar başarıyla mühürlendi!", Toast.LENGTH_SHORT).show();
            finish(); // İşlem bitince activity'i kapatıp Dashboard'a geri dönülür.
        });
    }
}