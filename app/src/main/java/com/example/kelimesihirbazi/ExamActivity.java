package com.example.kelimesihirbazi;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ExamActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private Cursor wordCursor;
    private TextView tvEngWord;
    private Button[] optionButtons = new Button[4];
    private int currentWordId = -1;
    private String correctTurWord = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exam);

        dbHelper = new DatabaseHelper(this);
        tvEngWord = findViewById(R.id.tvEngWord);

        optionButtons[0] = findViewById(R.id.btnOption1);
        optionButtons[1] = findViewById(R.id.btnOption2);
        optionButtons[2] = findViewById(R.id.btnOption3);
        optionButtons[3] = findViewById(R.id.btnOption4);

        SharedPreferences prefs = getSharedPreferences("KelimeSihirbaziAyarlar", MODE_PRIVATE);
        int limit = prefs.getInt("yeniKelimeSayisi", 10);

        wordCursor = dbHelper.getExamWords(limit);
        loadNextQuestion();

        for (Button btn : optionButtons) {
            btn.setOnClickListener(v -> checkAnswer(((Button) v).getText().toString()));
        }
    }

    // Doğru cevap ve 3 yanlış çeldirici çekilip butonlara rastgele dağıtılır
    private void loadNextQuestion() {
        if (wordCursor != null && wordCursor.moveToNext()) {
            currentWordId = wordCursor.getInt(0);
            tvEngWord.setText(wordCursor.getString(1));
            correctTurWord = wordCursor.getString(2);

            ArrayList<String> options = dbHelper.getRandomWrongAnswers(currentWordId);
            options.add(correctTurWord);

            // Şıkların yeri her soruda karıştırılır
            Collections.shuffle(options);

            for (int i = 0; i < optionButtons.length; i++) {
                if (i < options.size()) {
                    optionButtons[i].setText(options.get(i));
                    optionButtons[i].setEnabled(true);
                }
            }
        } else {
            Toast.makeText(this, "Bugünlük sınav bitti, tebrikler!", Toast.LENGTH_LONG).show();
            if (wordCursor != null) wordCursor.close();
            finish();
        }
    }

    // Seçilen şıkkın doğruluğuna göre veritabanında kelimenin Level değeri güncellenir
    private void checkAnswer(String selectedAnswer) {
        if (selectedAnswer.equals(correctTurWord)) {
            Toast.makeText(this, "Doğru!", Toast.LENGTH_SHORT).show();
            dbHelper.updateWordLevel(currentWordId, true);
        } else {
            Toast.makeText(this, "Yanlış! Doğrusu: " + correctTurWord, Toast.LENGTH_SHORT).show();
            dbHelper.updateWordLevel(currentWordId, false);
        }

        loadNextQuestion();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (wordCursor != null && !wordCursor.isClosed()) {
            wordCursor.close();
        }
    }
}