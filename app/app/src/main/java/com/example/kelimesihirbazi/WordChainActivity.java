package com.example.kelimesihirbazi;


import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;


import org.json.JSONObject;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class WordChainActivity extends AppCompatActivity {

    private static String AIzaSyAWx8nuWnLr4ZDrdAxtOPD7ltDMEDQ92PU;

    public static final String GEMINI_API_KEY =AIzaSyAWx8nuWnLr4ZDrdAxtOPD7ltDMEDQ92PU;
    private DatabaseHelper dbHelper;
    private TextView tvSecilenKelimeler, tvUretilenHikaye;
    private ImageView ivUretilenGorsel;
    private Button btnHikayeUret, btnGorseliKaydet;
    private String selectedWordsStr = "";
    private Bitmap generatedBitmap = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_word_chain);

        dbHelper = new DatabaseHelper(this);
        tvSecilenKelimeler = findViewById(R.id.tvSecilenKelimeler);
        tvUretilenHikaye = findViewById(R.id.tvUretilenHikaye);
        ivUretilenGorsel = findViewById(R.id.ivUretilenGorsel);
        btnHikayeUret = findViewById(R.id.btnHikayeUret);
        btnGorseliKaydet = findViewById(R.id.btnGorseliKaydet);

        fetchFiveRandomWords();

        btnHikayeUret.setOnClickListener(v -> {
            if (!selectedWordsStr.isEmpty()) {
                tvUretilenHikaye.setText("Sihir yapılıyor...");
                generateStoryAndImage(selectedWordsStr);
            }
        });

        btnGorseliKaydet.setOnClickListener(v -> saveImageToGallery());
    }

    private void fetchFiveRandomWords() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT EngWordName FROM Words ORDER BY RANDOM() LIMIT 5", null);
        List<String> words = new ArrayList<>();
        while (cursor.moveToNext()) { words.add(cursor.getString(0)); }
        cursor.close();

        if (words.size() == 5) {
            selectedWordsStr = String.join(", ", words);
            tvSecilenKelimeler.setText("Seçilenler: " + selectedWordsStr);
        }
    }

    private void generateStoryAndImage(String words) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            String prompt = "Bu 5 kelimeyle Türkçe kısa fantastik bir hikaye yaz: " + words;
            String story = callGeminiAPI(prompt);
            Bitmap image = callPollinationsAPI(words);
            runOnUiThread(() -> {
                if (story != null) tvUretilenHikaye.setText(story);
                if (image != null) {
                    ivUretilenGorsel.setImageBitmap(image);
                    ivUretilenGorsel.setVisibility(View.VISIBLE);
                    btnGorseliKaydet.setVisibility(View.VISIBLE);
                    generatedBitmap = image;
                }
            });
        });
    }

    private String callGeminiAPI(String prompt) {
        try {
            URL url = new URL("https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=" + GEMINI_API_KEY);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);
            String body = "{\"contents\":[{\"parts\":[{\"text\":\"" + prompt + "\"}]}]}";
            try (OutputStream os = conn.getOutputStream()) { os.write(body.getBytes()); }
            java.util.Scanner s = new java.util.Scanner(conn.getInputStream()).useDelimiter("\\A");
            JSONObject json = new JSONObject(s.next());
            return json.getJSONArray("candidates").getJSONObject(0).getJSONObject("content").getJSONArray("parts").getJSONObject(0).getString("text");
        } catch (Exception e) { return null; }
    }

    private Bitmap callPollinationsAPI(String words) {
        try {
            String url = "https://image.pollinations.ai/prompt/fantasy_" + words.replace(", ", "_") + "?width=512&height=512";
            return BitmapFactory.decodeStream(new URL(url).openConnection().getInputStream());
        } catch (Exception e) { return null; }
    }

    private void saveImageToGallery() {
        if (generatedBitmap == null) return;
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.DISPLAY_NAME, "buyu_" + System.currentTimeMillis() + ".jpg");
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
        android.net.Uri uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        try (OutputStream out = getContentResolver().openOutputStream(uri)) {
            generatedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            Toast.makeText(this, "Galeriye kaydedildi!", Toast.LENGTH_SHORT).show();
        } catch (Exception e) { e.printStackTrace(); }
    }
}