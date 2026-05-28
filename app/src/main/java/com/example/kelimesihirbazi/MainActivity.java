package com.example.kelimesihirbazi;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        EditText etKullaniciAdi = findViewById(R.id.etKullaniciAdi);
        EditText etSifre = findViewById(R.id.etSifre);
        Button btnGirisYap = findViewById(R.id.btnGirisYap);
        TextView tvKayitOl = findViewById(R.id.tvKayitOl);
        TextView tvSifremiUnuttum = findViewById(R.id.tvSifremiUnuttum);

        dbHelper = new DatabaseHelper(this);

        btnGirisYap.setOnClickListener(v -> {
            String kullaniciAdi = etKullaniciAdi.getText().toString().trim();
            String sifre = etSifre.getText().toString().trim();

            if (kullaniciAdi.isEmpty() || sifre.isEmpty()) {
                Toast.makeText(MainActivity.this, "Sihirli sözcükler boş bırakılamaz!", Toast.LENGTH_SHORT).show();
            } else {
                boolean girisBasarili = dbHelper.checkUser(kullaniciAdi, sifre);

                if (girisBasarili) {
                    /*
                     * Giriş başarılı olduğunda kullanıcının ID'si veritabanından çekilir
                     * ve cihaz hafızasına (SharedPreferences) kaydedilir.
                     */
                    SQLiteDatabase db = dbHelper.getReadableDatabase();
                    Cursor cursor = db.rawQuery("SELECT UserID FROM " + DatabaseHelper.TABLE_USERS + " WHERE UserName=?", new String[]{kullaniciAdi});
                    if (cursor.moveToFirst()) {
                        int userId = cursor.getInt(0);
                        SharedPreferences prefs = getSharedPreferences("KullaniciVerileri", MODE_PRIVATE);
                        prefs.edit().putInt("aktifKullaniciID", userId).apply();
                    }
                    cursor.close();

                    Intent intent = new Intent(MainActivity.this, HomeActivity.class);
                    startActivity(intent);
                    finish(); // Geri tuşuna basıldığında tekrar giriş ekranına dönmesini engeller
                } else {
                    Toast.makeText(MainActivity.this, "Yanlış büyücü adı veya parola!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        tvKayitOl.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, RegisterActivity.class);
            startActivity(intent);
        });

        tvSifremiUnuttum.setOnClickListener(v -> showForgotPasswordDialog());
    }


     //Şifremi unuttum modülü.
    private void showForgotPasswordDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Parolayı Yenile");

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 40, 50, 10);

        final EditText etKullanici = new EditText(this);
        etKullanici.setHint("Kayıtlı Büyücü Adı");
        layout.addView(etKullanici);

        final EditText etYeniSifre = new EditText(this);
        etYeniSifre.setHint("Yeni Parola");
        layout.addView(etYeniSifre);

        builder.setView(layout);

        builder.setPositiveButton("Yenile", (dialog, which) -> {
            String kullanici = etKullanici.getText().toString().trim();
            String yeniSifre = etYeniSifre.getText().toString().trim();

            if (!kullanici.isEmpty() && !yeniSifre.isEmpty()) {
                SQLiteDatabase db = dbHelper.getWritableDatabase();
                android.content.ContentValues values = new android.content.ContentValues();
                values.put("Password", yeniSifre);

                // Update işlemi: Kullanıcı adı eşleşiyorsa şifreyi değiştir
                int rows = db.update(DatabaseHelper.TABLE_USERS, values, "UserName=?", new String[]{kullanici});
                if (rows > 0) {
                    Toast.makeText(this, "Parola başarıyla mühürlendi!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Böyle bir büyücü bulunamadı.", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Alanlar boş bırakılamaz.", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("İptal", (dialog, which) -> dialog.cancel());
        builder.show();
    }
}