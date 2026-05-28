package com.example.kelimesihirbazi;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class WordleActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private GridLayout wordleGrid;
    private EditText etKelimeTahmini;
    private TextView[][] cells = new TextView[6][5];

    private String targetWord = "";
    private int currentRow = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wordle);

        dbHelper = new DatabaseHelper(this);
        wordleGrid = findViewById(R.id.wordleGrid);
        etKelimeTahmini = findViewById(R.id.etKelimeTahmini);
        Button btnTahminEt = findViewById(R.id.btnTahminEt);

        initGrid();
        fetchRandomLearnedWord();

        btnTahminEt.setOnClickListener(v -> {
            String guess = etKelimeTahmini.getText().toString().toUpperCase().trim();

            if (guess.length() != 5) {
                Toast.makeText(this, "Sihirli sözcük tam 5 harfli olmalıdır!", Toast.LENGTH_SHORT).show();
                return;
            }

            if (currentRow < 6) {
                checkWordleGuess(guess);
                etKelimeTahmini.setText("");
            } else {
                Toast.makeText(this, "Deneme hakkınız bitti. Doğru kelime: " + targetWord, Toast.LENGTH_LONG).show();
            }
        });
    }


    private void initGrid() {
        for (int i = 0; i < 6; i++) {
            for (int j = 0; j < 5; j++) {
                TextView tv = new TextView(this);
                tv.setWidth(120);
                tv.setHeight(120);
                tv.setTextSize(24f);
                tv.setTextColor(Color.parseColor("#2C1E16")); // İçindeki harf rengi (Koyu Mürekkep)
                tv.setGravity(Gravity.CENTER);
                tv.setBackgroundColor(Color.parseColor("#A89F91")); // Varsayılan boş hücre rengi (Soluk gri/kahve)

                GridLayout.LayoutParams params = new GridLayout.LayoutParams();
                params.setMargins(8, 8, 8, 8);
                tv.setLayoutParams(params);

                cells[i][j] = tv;
                wordleGrid.addView(tv);
            }
        }
    }

    /*
     * UserWordProgress tablosu ile Words tablosu birleştirilerek (INNER JOIN) uzunluğu 5 olan
     * ve listeye eklenmiş rastgele bir kelime çekilir.
     */
    private void fetchRandomLearnedWord() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String query = "SELECT w.EngWordName FROM Words w " +
                "INNER JOIN UserWordProgress p ON w.WordID = p.WordID " +
                "WHERE LENGTH(w.EngWordName) = 5 " +
                "ORDER BY RANDOM() LIMIT 1";

        Cursor cursor = db.rawQuery(query, null);
        if (cursor.moveToFirst()) {
            targetWord = cursor.getString(0).toUpperCase();
        } else {
            // Veritabanında 5 harfli öğrenilen kelime yoksa hata almamak için varsayılan atama (Fallback)
            targetWord = "MAGIC";
        }
        cursor.close();
    }

    /*
     * Opsiyonel anlık geri bildirim mekanizması: Harf tekrarlarını
     * yanlış renklendirmemek için algoritma iki tur (double-pass) çalışır.
     */
    private void checkWordleGuess(String guess) {
        boolean[] targetMatched = new boolean[5];
        boolean[] guessMatched = new boolean[5];

        // 1. Tur: Tam eşleşen (Harf doğru, Yer doğru) harfleri tespit edip Yeşil yapıyoruz.
        for (int i = 0; i < 5; i++) {
            cells[currentRow][i].setText(String.valueOf(guess.charAt(i)));
            if (guess.charAt(i) == targetWord.charAt(i)) {
                cells[currentRow][i].setBackgroundColor(Color.parseColor("#50C878"));
                targetMatched[i] = true;
                guessMatched[i] = true;
            }
        }

        // 2. Tur: Harf doğru ama yeri yanlış olanları Sarı, tamamen yanlış olanları Kırmızı yapıyoruz.
        for (int i = 0; i < 5; i++) {
            if (!guessMatched[i]) {
                boolean isPartialMatch = false;
                for (int j = 0; j < 5; j++) {
                    if (!targetMatched[j] && guess.charAt(i) == targetWord.charAt(j)) {
                        cells[currentRow][i].setBackgroundColor(Color.parseColor("#C0B283"));
                        targetMatched[j] = true;
                        guessMatched[i] = true;
                        isPartialMatch = true;
                        break;
                    }
                }

                // Eğer döngüde eşleşme bulunamadıysa bu harf kelimede hiç yoktur, Kırmızı yap.
                if (!isPartialMatch) {
                    cells[currentRow][i].setBackgroundColor(Color.parseColor("#B22222"));
                }
            }
        }

        // Kelimenin tamamı doğru bilindiyse oyunu bitir ve tebrik mesajı göster.
        if (guess.equals(targetWord)) {
            Toast.makeText(this, "Tebrikler! Büyüyü çözdün.", Toast.LENGTH_LONG).show();
            etKelimeTahmini.setEnabled(false); // Yeni tahmin yapılmasını engelle
        }

        currentRow++;
    }
}