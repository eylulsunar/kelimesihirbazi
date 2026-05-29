package com.example.kelimesihirbazi;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "KelimeSihirbazi.db";
    private static final int DATABASE_VERSION = 5;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createUsersTable = "CREATE TABLE Users (" +
                "UserID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "UserName TEXT, " +
                "Password TEXT)";
        db.execSQL(createUsersTable);

        String createWordsTable = "CREATE TABLE Words (" +
                "WordID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "EngWordName TEXT, " +
                "TurWordName TEXT, " +
                "Picture TEXT, " +
                "Level INTEGER DEFAULT 0, " +
                "NextDate TEXT, " +
                "Topic TEXT DEFAULT 'Genel Kelimeler')";
        db.execSQL(createWordsTable);

        String createSamplesTable = "CREATE TABLE WordSamples (" +
                "WordSamplesID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "WordID INTEGER, " +
                "Samples TEXT, " +
                "FOREIGN KEY(WordID) REFERENCES Words(WordID))";
        db.execSQL(createSamplesTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS Users");
        db.execSQL("DROP TABLE IF EXISTS Words");
        db.execSQL("DROP TABLE IF EXISTS WordSamples");
        onCreate(db);
    }

    public boolean checkUser(String username, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM Users WHERE UserName = ? AND Password = ?", new String[]{username, password});
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }

    public boolean updatePassword(String username, String newPassword) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("Password", newPassword);
        int rows = db.update("Users", values, "UserName = ?", new String[]{username});
        return rows > 0;
    }

    public boolean addUser(String username, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM Users WHERE UserName = ?", new String[]{username});
        if (cursor.getCount() > 0) {
            cursor.close();
            return false;
        }
        cursor.close();
        ContentValues values = new ContentValues();
        values.put("UserName", username);
        values.put("Password", password);
        return db.insert("Users", null, values) != -1;
    }

    public boolean addWord(String engWord, String turWord, String picturePath, String sampleSentence, int userId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues wordValues = new ContentValues();
        wordValues.put("EngWordName", engWord);
        wordValues.put("TurWordName", turWord);
        wordValues.put("Picture", picturePath);
        long wordId = db.insert("Words", null, wordValues);

        if (wordId != -1 && sampleSentence != null && !sampleSentence.isEmpty()) {
            ContentValues sampleValues = new ContentValues();
            sampleValues.put("WordID", (int) wordId);
            sampleValues.put("Samples", sampleSentence);
            db.insert("WordSamples", null, sampleValues);
        }
        return wordId != -1;
    }

    // Konu bazlı yüzdesel başarı raporu üretilir
    public String generateAnalysisReport() {
        SQLiteDatabase db = this.getReadableDatabase();
        // Kelimeler konularına göre gruplanır; o konudaki kelime sayısı ve toplam seviye puanı çekilir
        Cursor cursor = db.rawQuery("SELECT Topic, COUNT(*), SUM(Level) FROM Words GROUP BY Topic", null);

        StringBuilder builder = new StringBuilder();
        builder.append("BÜYÜ KİTABI ANALİZ RAPORU\n");

        boolean hasData = false;
        int grandTotalWords = 0;
        int grandTotalLevel = 0;

        while (cursor.moveToNext()) {
            hasData = true;
            String topic = cursor.getString(0);
            int count = cursor.getInt(1);
            int sumLevel = cursor.getInt(2); // O konudaki tüm kelimelerin seviyeleri toplamı

            grandTotalWords += count;
            grandTotalLevel += sumLevel;

            int successRate = (int) (((float) sumLevel / (count * 6)) * 100);

            builder.append("Konu: ").append(topic != null ? topic : "Genel Kelimeler").append("\n");
            builder.append("Kelime Sayısı: ").append(count).append("\n");
            builder.append("Konu Başarısı: %").append(successRate).append("\n");
            builder.append("-------------------------------------\n");
        }
        cursor.close();

        if (!hasData) return "Büyü kitabında henüz analiz edilecek kelime yok.";

        // Tüm konuların genel başarı ortalaması
        int overallSuccess = (int) (((float) grandTotalLevel / (grandTotalWords * 6)) * 100);
        builder.append("\nGENEL BÜYÜCÜLÜK BAŞARISI: %").append(overallSuccess);

        return builder.toString();
    }
    public String getRandomWordleWord() {
        SQLiteDatabase db = this.getReadableDatabase();
        String targetWord = "";
        Cursor cursor = db.rawQuery("SELECT EngWordName FROM Words WHERE length(EngWordName) = 5 ORDER BY RANDOM() LIMIT 1", null);
        if (cursor.moveToFirst()) targetWord = cursor.getString(0);
        cursor.close();
        return targetWord;
    }

    public android.database.Cursor getExamWords(int yeniKelimeLimiti) {
        android.database.sqlite.SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT * FROM (SELECT WordID, EngWordName, TurWordName FROM Words WHERE Level > 0 AND Level < 6 AND NextDate <= date('now', 'localtime')) " +
                "UNION ALL " +
                "SELECT * FROM (SELECT WordID, EngWordName, TurWordName FROM Words WHERE Level = 0 OR NextDate IS NULL ORDER BY RANDOM() LIMIT ?)";

        return db.rawQuery(query, new String[]{String.valueOf(yeniKelimeLimiti)});
    }

    public void updateWordLevel(int wordId, boolean isCorrect) {
        SQLiteDatabase db = this.getWritableDatabase();
        if (isCorrect) {
            String updateQuery = "UPDATE Words SET Level = Level + 1, " +
                    "NextDate = CASE Level " +
                    "WHEN 0 THEN date('now', 'localtime', '+1 day') " +
                    "WHEN 1 THEN date('now', 'localtime', '+7 days') " +
                    "WHEN 2 THEN date('now', 'localtime', '+1 month') " +
                    "WHEN 3 THEN date('now', 'localtime', '+3 months') " +
                    "WHEN 4 THEN date('now', 'localtime', '+6 months') " +
                    "WHEN 5 THEN date('now', 'localtime', '+1 year') " +
                    "ELSE date('now', 'localtime', '+1 year') END " +
                    "WHERE WordID = ? AND Level < 6";
            db.execSQL(updateQuery, new Object[]{wordId});
        } else {
            db.execSQL("UPDATE Words SET Level = 0, NextDate = date('now', 'localtime', '+1 day') WHERE WordID = ?", new Object[]{wordId});
        }
    }

    public java.util.ArrayList<String> getRandomWrongAnswers(int excludeWordId) {
        java.util.ArrayList<String> wrongAnswers = new java.util.ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT TurWordName FROM Words WHERE WordID != ? ORDER BY RANDOM() LIMIT 3", new String[]{String.valueOf(excludeWordId)});
        while (cursor.moveToNext()) wrongAnswers.add(cursor.getString(0));
        cursor.close();
        return wrongAnswers;
    }

    public java.util.List<String> getAllWordsForChain() {
        java.util.List<String> words = new java.util.ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT EngWordName FROM Words ORDER BY Level DESC", null);
        while (cursor.moveToNext()) words.add(cursor.getString(0).toUpperCase());
        cursor.close();
        return words;
    }
}