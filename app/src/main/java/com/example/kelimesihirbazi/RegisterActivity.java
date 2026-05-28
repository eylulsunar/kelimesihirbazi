package com.example.kelimesihirbazi;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class RegisterActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        dbHelper = new DatabaseHelper(this);

        EditText etKullaniciAdi = findViewById(R.id.etKullaniciAdiKayit);
        EditText etSifre = findViewById(R.id.etSifreKayit);
        Button btnKaydiTamamla = findViewById(R.id.btnKaydiTamamla);

        btnKaydiTamamla.setOnClickListener(v -> {
            String kullaniciAdi = etKullaniciAdi.getText().toString().trim();
            String sifre = etSifre.getText().toString().trim();

            if (kullaniciAdi.isEmpty() || sifre.isEmpty()) {
                Toast.makeText(this, "Lütfen gizli parolayı tamamla.", Toast.LENGTH_SHORT).show();
                return;
            }

            boolean basarili = dbHelper.addUser(kullaniciAdi, sifre);
            if (basarili) {
                Toast.makeText(this, "Aramıza hoş geldin!", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "Bu isimde bir büyücü zaten var.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}