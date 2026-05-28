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

        EditText etEngWord = findViewById(R.id.etEngWord);
        EditText etTurWord = findViewById(R.id.etTurWord);
        EditText etSampleSentence = findViewById(R.id.etSampleSentence);
        EditText etPicturePath = findViewById(R.id.etPicturePath);
        Button btnKelimeKaydet = findViewById(R.id.btnKelimeKaydet);

        btnKelimeKaydet.setOnClickListener(v -> {
            String engWord = etEngWord.getText().toString().trim();
            String turWord = etTurWord.getText().toString().trim();
            String sampleSentence = etSampleSentence.getText().toString().trim();
            String picturePath = etPicturePath.getText().toString().trim();

            if (engWord.isEmpty() || turWord.isEmpty()) {
                Toast.makeText(this, "İngilizce ve Türkçe sözcük alanları zorunludur.", Toast.LENGTH_SHORT).show();
                return;
            }

            // Transaction bloğu içeren veritabanı metodunu çağırarak Words ve WordSamples tablolarına eşzamanlı kayıt atıyoruz.
            boolean basarili = dbHelper.addWord(engWord, turWord, picturePath, sampleSentence);

            if (basarili) {
                Toast.makeText(this, "Büyü başarıyla eklendi!", Toast.LENGTH_SHORT).show();
                etEngWord.setText("");
                etTurWord.setText("");
                etSampleSentence.setText("");
                etPicturePath.setText("");
                etEngWord.requestFocus();
            } else {
                Toast.makeText(this, "Büyü eklenirken bir hata oluştu.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}