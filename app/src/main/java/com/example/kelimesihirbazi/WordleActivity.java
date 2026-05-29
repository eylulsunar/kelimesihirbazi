package com.example.kelimesihirbazi;

import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class WordleActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private String targetWord = "";
    private int currentRow = 0;
    private EditText etWordInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wordle);

        dbHelper = new DatabaseHelper(this);
        targetWord = dbHelper.getRandomWordleWord();

        // Çökme Koruması: Veritabanında henüz 5 harfli kelime yoksa otomatik bir tane atanır
        if (targetWord == null || targetWord.isEmpty()) {
            Toast.makeText(this, "Uyarı: Veritabanında 5 harfli kelime yok!", Toast.LENGTH_LONG).show();
            targetWord = "MAGIC";
        }

        etWordInput = findViewById(R.id.etWordInput);

        // Kullanıcı harf girdikçe sihirli kutulara anında yansıtılır (TextWatcher)
        etWordInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Yazılan harfler anlık olarak o anki satıra dağıtılır
                for (int i = 0; i < 5; i++) {
                    TextView cell = getCell(currentRow, i);
                    if (i < s.length()) {
                        cell.setText(String.valueOf(s.charAt(i)).toUpperCase());
                    } else {
                        cell.setText(""); // Harf silinirse kutuyu da boşalt
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Klavyedeki Enter tuşuna basıldığında Doğru/Yanlış kontrolü yapılır
        etWordInput.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE ||
                    (event != null && event.getKeyCode() == android.view.KeyEvent.KEYCODE_ENTER)) {

                String guess = etWordInput.getText().toString().toUpperCase();
                if (guess.length() == 5) {
                    checkWord(guess);
                } else {
                    Toast.makeText(this, "Büyü yarım kaldı! Tam 5 harf girmelisin.", Toast.LENGTH_SHORT).show();
                }
                return true;
            }
            return false;
        });
    }
    private void checkWord(String guess) {
        String target = targetWord.toUpperCase();

        for (int i = 0; i < 5; i++) {
            TextView cell = getCell(currentRow, i);
            char guessedChar = guess.charAt(i);

            if (guessedChar == target.charAt(i)) {
                // Doğru harf, doğru yer -> YEŞİL
                cell.setBackgroundColor(Color.parseColor("#538D4E"));
            } else if (target.contains(String.valueOf(guessedChar))) {
                // Doğru harf, yanlış yer -> SARI
                cell.setBackgroundColor(Color.parseColor("#B59F3B"));
            } else {
                // Harf kelimede yok -> KOYU GRİ
                cell.setBackgroundColor(Color.parseColor("#3A3A3C"));
            }
            cell.setTextColor(Color.WHITE);
        }

        if (guess.equals(target)) {
            Toast.makeText(this, "Tebrikler! Büyülü kelimeyi buldun.", Toast.LENGTH_LONG).show();
            etWordInput.setEnabled(false); // Oyun kazanıldı, kutuyu kilitle
        } else {
            currentRow++;
            etWordInput.setText(""); // Yeni satır için giriş kutusunu temizle

            if (currentRow > 5) {
                Toast.makeText(this, "Oyun Bitti! Doğru kelime: " + target, Toast.LENGTH_LONG).show();
                etWordInput.setEnabled(false); // Haklar bitti, kutuyu kilitle
            }
        }
    }

    // Ekrandaki kutucukların ID'sini dinamik olarak bulan metot
    private TextView getCell(int row, int col) {
        int id = getResources().getIdentifier("cell_" + row + "_" + col, "id", getPackageName());
        return findViewById(id);
    }
}