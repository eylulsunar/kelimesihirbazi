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
            String username = etKullaniciAdi.getText().toString().trim();
            String password = etSifre.getText().toString().trim();

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Lütfen tüm alanları doldur.", Toast.LENGTH_SHORT).show();
                return;
            }

            if (dbHelper.addUser(username, password)) {
                Toast.makeText(this, "Büyücü kaydı başarılı!", Toast.LENGTH_SHORT).show();
                finish(); // Giriş ekranına dön
            } else {
                Toast.makeText(this, "Bu isimde bir büyücü zaten kayıtlı.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}