package com.example.kelimesihirbazi;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "KelimeSihirbazi.db";
    private static final int DATABASE_VERSION = 3;

    // Tablo İsimleri
    public static final String TABLE_USERS = "Users";
    public static final String TABLE_WORDS = "Words";
    public static final String TABLE_SAMPLES = "WordSamples";
    public static final String TABLE_PROGRESS = "UserWordProgress";

    /*
     *SQL tablo şemaları.
     * Foreign Key (Dış Anahtar) bağlantıları veri bütünlüğünü korumak için UserID ve WordID üzerinden kurulmuştur.
     */
    private static final String CREATE_TABLE_USERS = "CREATE TABLE " + TABLE_USERS + " ("
            + "UserID INTEGER PRIMARY KEY AUTOINCREMENT, " // [cite: 8]
            + "UserName TEXT UNIQUE, " // [cite: 8]
            + "Password TEXT)"; // [cite: 8]

    private static final String CREATE_TABLE_WORDS = "CREATE TABLE " + TABLE_WORDS + " ("
            + "WordID INTEGER PRIMARY KEY AUTOINCREMENT, " // [cite: 12]
            + "EngWordName TEXT, " // [cite: 12]
            + "TurWordName TEXT, " // [cite: 12]
            + "Picture TEXT)"; // Örn: C://words/yeri.jpeg formatındaki dosya yolu [cite: 12]

    private static final String CREATE_TABLE_SAMPLES = "CREATE TABLE " + TABLE_SAMPLES + " ("
            + "WordSamplesID INTEGER PRIMARY KEY AUTOINCREMENT, " // [cite: 13]
            + "WordID INTEGER, " // [cite: 13]
            + "Samples TEXT, " // [cite: 13]
            + "FOREIGN KEY(WordID) REFERENCES " + TABLE_WORDS + "(WordID) ON DELETE CASCADE)";

    private static final String CREATE_TABLE_PROGRESS = "CREATE TABLE " + TABLE_PROGRESS + " ("
            + "ProgressID INTEGER PRIMARY KEY AUTOINCREMENT, "
            + "UserID INTEGER, "
            + "WordID INTEGER, "
            + "Level INTEGER DEFAULT 0, "
            + "NextReviewDate TEXT, "
            + "FOREIGN KEY(UserID) REFERENCES " + TABLE_USERS + "(UserID) ON DELETE CASCADE, "
            + "FOREIGN KEY(WordID) REFERENCES " + TABLE_WORDS + "(WordID) ON DELETE CASCADE)";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_USERS);
        db.execSQL(CREATE_TABLE_WORDS);
        db.execSQL(CREATE_TABLE_SAMPLES);
        db.execSQL(CREATE_TABLE_PROGRESS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PROGRESS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SAMPLES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_WORDS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        onCreate(db);
    }

    public boolean addUser(String username, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("UserName", username);
        values.put("Password", password);

        long result = db.insert(TABLE_USERS, null, values);
        return result != -1;
    }

    public boolean checkUser(String username, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_USERS + " WHERE UserName=? AND Password=?", new String[]{username, password});
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }

    public boolean addWord(String engWord, String turWord, String picturePath, String sampleText) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();
        try {
            ContentValues wordValues = new ContentValues();
            wordValues.put("EngWordName", engWord);
            wordValues.put("TurWordName", turWord);
            wordValues.put("Picture", picturePath);

            long wordId = db.insert(TABLE_WORDS, null, wordValues);

            if (wordId != -1 && sampleText != null && !sampleText.trim().isEmpty()) {
                ContentValues sampleValues = new ContentValues();
                sampleValues.put("WordID", wordId);
                sampleValues.put("Samples", sampleText);
                db.insert(TABLE_SAMPLES, null, sampleValues);
            }

            db.setTransactionSuccessful();
            return wordId != -1;
        } finally {
            db.endTransaction();
        }
    }

    public Cursor getDueWords(String todayDate, int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT w.*, p.Level FROM " + TABLE_WORDS + " w " +
                "INNER JOIN " + TABLE_PROGRESS + " p ON w.WordID = p.WordID " +
                "WHERE p.NextReviewDate <= ? AND p.UserID = ?";
        return db.rawQuery(query, new String[]{todayDate, String.valueOf(userId)});
    }

    public void updateWordProgress(int userId, int wordId, int level, String nextDate) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("Level", level);
        values.put("NextReviewDate", nextDate);

        db.update(TABLE_PROGRESS, values, "UserID=? AND WordID=?", new String[]{String.valueOf(userId), String.valueOf(wordId)});
        db.close();
    }

    public String generateAnalysisReport(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery("SELECT Level, COUNT(*) FROM " + TABLE_PROGRESS + " WHERE UserID=? GROUP BY Level", new String[]{String.valueOf(userId)});

        int totalWords = 0;
        int fullyLearned = 0;
        StringBuilder builder = new StringBuilder();

        while (cursor.moveToNext()) {
            int level = cursor.getInt(0);
            int count = cursor.getInt(1);

            totalWords += count;
            if (level >= 6) {
                fullyLearned += count;
            }

            builder.append("Seviye ").append(level).append(" -> ").append(count).append(" Kelime\n");
        }
        cursor.close();

        if (totalWords == 0) {
            return "Henüz yeterli veri yok.";
        }

        int successRate = (fullyLearned * 100) / totalWords;

        return "Genel Başarı Yüzdesi: %" + successRate + "\n\n" +
                "Seviye Dağılımı:\n" + builder.toString();
    }
}