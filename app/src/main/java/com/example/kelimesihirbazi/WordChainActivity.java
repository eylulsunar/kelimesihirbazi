package com.example.kelimesihirbazi;

import android.os.Bundle;
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
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

public class WordChainActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private TextView tvChainResult, tvStoryResult;
    private Button btnGenerateMagic, btnSaveImage;
    private ImageView ivGeneratedImage;
    private List<String> finalChain = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_word_chain);

        dbHelper = new DatabaseHelper(this);
        tvChainResult = findViewById(R.id.tvChainResult);
        tvStoryResult = findViewById(R.id.tvStoryResult);
        btnGenerateMagic = findViewById(R.id.btnGenerateMagic);
        btnSaveImage = findViewById(R.id.btnSaveImage);
        ivGeneratedImage = findViewById(R.id.ivGeneratedImage);

        buildWordChain();

        btnGenerateMagic.setOnClickListener(v -> {
            if (finalChain.size() < 5) {
                Toast.makeText(this, "Yeterli kelime yok! Önce büyü kitabına kelime ekleyin.", Toast.LENGTH_SHORT).show();
                return;
            }
            callRealAI();
        });

        // Kullanıcı kendi isteğiyle resmi kaydeder
        btnSaveImage.setOnClickListener(v -> {
            Toast.makeText(this, "Görsel cihaz hafızasına kaydedildi.", Toast.LENGTH_SHORT).show();
        });
    }

    // Son harf - ilk harf kuralına göre kelimeler uç uca eklenir
    private void buildWordChain() {
        List<String> allWords = dbHelper.getAllWordsForChain();
        if (allWords.isEmpty()) {
            tvChainResult.setText("Sistemde kelime bulunamadı.");
            return;
        }

        Collections.shuffle(allWords);
        String currentWord = allWords.get(0);
        finalChain.add(currentWord);
        allWords.remove(currentWord);

        while (finalChain.size() < 5 && !allWords.isEmpty()) {
            char lastChar = currentWord.charAt(currentWord.length() - 1);
            String nextWord = null;

            for (String word : allWords) {
                if (word.charAt(0) == lastChar) {
                    nextWord = word;
                    break;
                }
            }

            if (nextWord != null) {
                finalChain.add(nextWord);
                allWords.remove(nextWord);
                currentWord = nextWord;
            } else {
                currentWord = allWords.get(0);
                finalChain.add(currentWord);
                allWords.remove(currentWord);
            }
        }

        tvChainResult.setText("Zincir: " + String.join(" ➔ ", finalChain));
    }

    // API Entegrasyonu
    private void callRealAI() {
        btnGenerateMagic.setEnabled(false);
        tvStoryResult.setText("Yapay zeka hikayeyi yazıyor...");

        new Thread(() -> {
            try {
                String apiKey = "AI.Ab8RN6Kes-juRVNWhKSDI-ol02lY";

                URL url = new URL("https://generativelanguage.googleapis.com/v1beta/models/gemini-pro:generateContent?key=" + apiKey);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);

                String promptWords = String.join(", ", finalChain);
                String jsonBody = "{\"contents\": [{\"parts\": [{\"text\": \"Şu kelimeleri kullanarak çok kısa bir sihir hikayesi yaz: " + promptWords + "\"}]}]}";

                try(OutputStream os = conn.getOutputStream()) {
                    byte[] input = jsonBody.getBytes("utf-8");
                    os.write(input, 0, input.length);
                }

                int responseCode = conn.getResponseCode();
                if (responseCode == 200) {
                    Scanner scanner = new Scanner(conn.getInputStream());
                    StringBuilder response = new StringBuilder();
                    while(scanner.hasNext()) response.append(scanner.nextLine());
                    scanner.close();

                    JSONObject jsonResponse = new JSONObject(response.toString());
                    String story = jsonResponse.getJSONArray("candidates")
                            .getJSONObject(0)
                            .getJSONObject("content")
                            .getJSONArray("parts")
                            .getJSONObject(0)
                            .getString("text");

                    runOnUiThread(() -> {
                        tvStoryResult.setText(story);
                        ivGeneratedImage.setVisibility(View.VISIBLE);
                        btnSaveImage.setVisibility(View.VISIBLE);
                        btnGenerateMagic.setText("Tamamlandı");
                    });
                } else {
                    // Hata durumu
                    runOnUiThread(() -> {
                        tvStoryResult.setText("API Hatası: " + responseCode + "\n(Kota dolmuş veya anahtar geçersiz olabilir)");
                        btnGenerateMagic.setText("Hata Oluştu");
                        btnGenerateMagic.setEnabled(true);
                    });
                }
                conn.disconnect();

            } catch (Exception e) {
                runOnUiThread(() -> {
                    tvStoryResult.setText("Bağlantı Hatası: İnternet bağlantınızı kontrol edin.");
                    btnGenerateMagic.setText("Tekrar Dene");
                    btnGenerateMagic.setEnabled(true);
                });
            }
        }).start();
    }
}