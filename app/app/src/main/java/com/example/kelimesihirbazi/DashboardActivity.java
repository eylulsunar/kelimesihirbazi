package com.example.kelimesihirbazi;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import com.airbnb.lottie.LottieAnimationView;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class DashboardActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private Cursor cursor;
    private TextView tvKalan, tvIng, tvTr, tvOrnek;
    private CardView cardSayfa;
    private Button btnCevap, btnDogru, btnYanlis;
    private View layoutKarar;
    private int currentUserId = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        // Kullanıcı giriş yaparken hafızaya alınan ID'yi çekiyoruz
        SharedPreferences prefs = getSharedPreferences("KullaniciVerileri", MODE_PRIVATE);
        currentUserId = prefs.getInt("aktifKullaniciID", 1);

        dbHelper = new DatabaseHelper(this);
        initViews();
        loadWords();
    }

    private void initViews() {
        tvKalan = findViewById(R.id.tvKalanKelime);
        tvIng = findViewById(R.id.tvKelimeIngilizce);
        tvTr = findViewById(R.id.tvKelimeTurkce);
        tvOrnek = findViewById(R.id.tvOrnekCumle);
        cardSayfa = findViewById(R.id.cardSayfa);
        btnCevap = findViewById(R.id.btnCevabiGor);
        btnDogru = findViewById(R.id.btnDogru);
        btnYanlis = findViewById(R.id.btnYanlis);
        layoutKarar = findViewById(R.id.layoutKararButonlari);

        btnCevap.setOnClickListener(v -> showAnswer());
        btnDogru.setOnClickListener(v -> processResult(true));
        btnYanlis.setOnClickListener(v -> processResult(false));
    }

    private void loadWords() {
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        cursor = dbHelper.getDueWords(today, currentUserId);
        updateUI();
    }

    private void updateUI() {
        if (cursor != null && cursor.moveToFirst()) {
            int wordId = cursor.getInt(cursor.getColumnIndexOrThrow("WordID"));

            tvKalan.setText(cursor.getCount() + " Büyü Bekliyor");
            tvIng.setText(cursor.getString(cursor.getColumnIndexOrThrow("EngWordName")));
            tvTr.setText(cursor.getString(cursor.getColumnIndexOrThrow("TurWordName")));

            // Örnek cümleyi ilişkili tablodan çekiyoruz
            Cursor sampleCursor = dbHelper.getReadableDatabase().rawQuery(
                    "SELECT Samples FROM WordSamples WHERE WordID=?",
                    new String[]{String.valueOf(wordId)});

            if(sampleCursor.moveToFirst()) {
                tvOrnek.setText(sampleCursor.getString(0));
            }
            sampleCursor.close();

            tvTr.setVisibility(View.INVISIBLE);
            tvOrnek.setVisibility(View.INVISIBLE);
            btnCevap.setVisibility(View.VISIBLE);
            layoutKarar.setVisibility(View.GONE);
            cardSayfa.setAlpha(1f);
        } else {
            tvKalan.setText("Bugünlük tüm büyüler tamamlandı!");
            cardSayfa.setVisibility(View.GONE);
            btnCevap.setVisibility(View.GONE);
            layoutKarar.setVisibility(View.GONE);
        }
    }

    private void showAnswer() {
        tvTr.setVisibility(View.VISIBLE);
        tvOrnek.setVisibility(View.VISIBLE);
        btnCevap.setVisibility(View.GONE);
        layoutKarar.setVisibility(View.VISIBLE);
    }

    private void processResult(boolean isCorrect) {
        int wordId = cursor.getInt(cursor.getColumnIndexOrThrow("WordID"));
        int currentLevel = cursor.getInt(cursor.getColumnIndexOrThrow("Level"));

        int newLevel = isCorrect ? currentLevel + 1 : 0;
        int days = getIntervalDays(newLevel);

        updateDatabase(wordId, newLevel, days);
        loadWords();
    }

    private int getIntervalDays(int count) {
        switch (count) {
            case 1: return 1;
            case 2: return 7;
            case 3: return 30;
            case 4: return 90;
            case 5: return 180;
            case 6: return 360;
            default: return 365;
        }
    }
    private void showAnalysisReport() {
        // DatabaseHelper içerisindeki generateAnalysisReport metodu ile veriyi çekiyoruz.

        String reportText = dbHelper.generateAnalysisReport(currentUserId);

        new android.app.AlertDialog.Builder(this)
                .setTitle("Büyücülük Analiz Raporu")
                .setMessage(reportText)
                .setPositiveButton("Parşömene Aktar (Paylaş)", (dialog, which) -> {
                    android.content.Intent sendIntent = new android.content.Intent();
                    sendIntent.setAction(android.content.Intent.ACTION_SEND);
                    sendIntent.putExtra(android.content.Intent.EXTRA_TEXT, "Kelime Sihirbazı Başarı Raporum:\n\n" + reportText);
                    sendIntent.setType("text/plain");
                    startActivity(android.content.Intent.createChooser(sendIntent, "Raporu Paylaş"));
                })
                .setNegativeButton("Kapat", null)
                .show();
    }
    private void updateDatabase(int wordId, int level, int days) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, days);
        String nextDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal.getTime());
        dbHelper.updateWordProgress(currentUserId, wordId, level, nextDate);
    }
}