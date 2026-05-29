package com.example.kelimesihirbazi;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        EditText etKullaniciAdi = findViewById(R.id.etKullaniciAdi);
        EditText etSifre = findViewById(R.id.etSifre);
        Button btnGirisYap = findViewById(R.id.btnGirisYap);
        TextView tvKayitOl = findViewById(R.id.tvKayitOl);
        TextView tvSifremiUnuttum = findViewById(R.id.tvSifremiUnuttum);

        // Giriş yapma işlemi ve veritabanı kontrolü sağlanır
        btnGirisYap.setOnClickListener(v -> {
            String kullanici = etKullaniciAdi.getText().toString().trim();
            String sifre = etSifre.getText().toString().trim();

            // Alanların boş olup olmadığı kontrol edilir
            if (!kullanici.isEmpty() && !sifre.isEmpty()) {
                DatabaseHelper db = new DatabaseHelper(MainActivity.this);

                // Girilen bilgilerin doğruluğu Users tablosundan sorgulanır
                boolean girisBasarili = db.checkUser(kullanici, sifre);

                if (girisBasarili) {
                    Intent intent = new Intent(MainActivity.this, DashboardActivity.class);
                    startActivity(intent);
                    finish(); // Başarılı girişte geri tuşu ile bu ekrana dönülmesi engellenir
                } else {
                    Toast.makeText(MainActivity.this, "Hatalı büyücü adı veya parola!", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(MainActivity.this, "Lütfen tüm bilgileri girin.", Toast.LENGTH_SHORT).show();
            }
        });

        // Yeni kayıt ekranına yönlendirme yapılır
        tvKayitOl.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, RegisterActivity.class);
            startActivity(intent);
        });

        // Şifre yenileme penceresi oluşturulur
        tvSifremiUnuttum.setOnClickListener(v -> {
            android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
            builder.setTitle("Şifre Yenileme");

            final android.widget.LinearLayout layout = new android.widget.LinearLayout(this);
            layout.setOrientation(android.widget.LinearLayout.VERTICAL);
            layout.setPadding(50, 20, 50, 20);

            final EditText etKullanici = new EditText(this);
            etKullanici.setHint("Kullanıcı Adı");
            layout.addView(etKullanici);

            final EditText etYeniSifre = new EditText(this);
            etYeniSifre.setHint("Yeni Şifre");
            etYeniSifre.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
            layout.addView(etYeniSifre);

            builder.setView(layout);

            // Veritabanı üzerinden şifre güncelleme işlemi yapılır
            builder.setPositiveButton("Güncelle", (dialog, which) -> {
                DatabaseHelper db = new DatabaseHelper(MainActivity.this);
                boolean guncellendi = db.updatePassword(etKullanici.getText().toString().trim(), etYeniSifre.getText().toString().trim());

                if (guncellendi) {
                    Toast.makeText(MainActivity.this, "Parola başarıyla yenilendi.", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this, "Sistemde böyle bir büyücü bulunamadı.", Toast.LENGTH_SHORT).show();
                }
            });

            builder.setNegativeButton("İptal", null);
            builder.show();
        });
    }
}