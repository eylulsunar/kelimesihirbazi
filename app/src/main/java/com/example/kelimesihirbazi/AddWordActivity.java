package com.example.kelimesihirbazi;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class AddWordActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_word);

        dbHelper = new DatabaseHelper(this);


        EditText etEng = findViewById(R.id.etEngWord);
        EditText etTur = findViewById(R.id.etTurWord);
        EditText etCumle = findViewById(R.id.etSampleSentence);
        Button btnKaydet = findViewById(R.id.btnKelimeKaydet);

        // Kullanıcı ID'sini oturumdan alıyoruz
        int currentUserId = getSharedPreferences("Session", MODE_PRIVATE).getInt("aktifKullaniciID", 1);

        btnKaydet.setOnClickListener(v -> {
            String eng = etEng.getText().toString().trim();
            String tur = etTur.getText().toString().trim();
            String cumle = etCumle.getText().toString().trim();

            if (eng.isEmpty() || tur.isEmpty()) {
                Toast.makeText(this, "İngilizce ve Türkçe alanlar boş bırakılamaz!", Toast.LENGTH_SHORT).show();
            } else {
                // 5 parametreli metot çağrısı (DatabaseHelper ile eşleşti)
                boolean basarili = dbHelper.addWord(eng, tur, "", cumle, currentUserId);

                if (basarili) {
                    Toast.makeText(this, "Büyü kütüphaneye eklendi!", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(this, "Hata oluştu, büyü eklenemedi.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}