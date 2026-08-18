package com.smartnurse.app;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "smart_nurse.db";
    private static final int DATABASE_VERSION = 1;
    private static final String TABLE_NAME = "medications";

    private static final String COL_ID = "id";
    private static final String COL_NAME = "name";
    private static final String COL_INSTRUCTIONS = "instructions";
    private static final String COL_PHOTO_PATH = "photo_path";
    private static final String COL_TIME = "time";
    private static final String COL_PHONE = "phone";
    private static final String COL_AUDIO_PATH = "audio_path";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTable = "CREATE TABLE " + TABLE_NAME + " (" +
                COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_NAME + " TEXT, " +
                COL_INSTRUCTIONS + " TEXT, " +
                COL_PHOTO_PATH + " TEXT, " +
                COL_TIME + " TEXT, " +
                COL_PHONE + " TEXT, " +
                COL_AUDIO_PATH + " TEXT)";
        db.execSQL(createTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    // افزودن دارو
    public long insertMedication(Medication med) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_NAME, med.getName());
        values.put(COL_INSTRUCTIONS, med.getInstructions());
        values.put(COL_PHOTO_PATH, med.getPhotoPath());
        values.put(COL_TIME, med.getTime());
        values.put(COL_PHONE, med.getPhone());
        values.put(COL_AUDIO_PATH, med.getAudioPath());
        long id = db.insert(TABLE_NAME, null, values);
        db.close();
        return id;
    }

    // به‌روزرسانی دارو
    public void updateMedication(Medication med) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_NAME, med.getName());
        values.put(COL_INSTRUCTIONS, med.getInstructions());
        values.put(COL_PHOTO_PATH, med.getPhotoPath());
        values.put(COL_TIME, med.getTime());
        values.put(COL_PHONE, med.getPhone());
        values.put(COL_AUDIO_PATH, med.getAudioPath());
        db.update(TABLE_NAME, values, COL_ID + " = ?",
                new String[]{String.valueOf(med.getId())});
        db.close();
    }

    // حذف دارو
    public void deleteMedication(long id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_NAME, COL_ID + " = ?", new String[]{String.valueOf(id)});
        db.close();
    }

    // دریافت همه داروها
    public List<Medication> getAllMedications() {
        List<Medication> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_NAME + " ORDER BY " + COL_TIME, null);

        if (cursor.moveToFirst()) {
            do {
                Medication med = new Medication(
                        cursor.getLong(cursor.getColumnIndex(COL_ID)),
                        cursor.getString(cursor.getColumnIndex(COL_NAME)),
                        cursor.getString(cursor.getColumnIndex(COL_INSTRUCTIONS)),
                        cursor.getString(cursor.getColumnIndex(COL_PHOTO_PATH)),
                        cursor.getString(cursor.getColumnIndex(COL_TIME)),
                        cursor.getString(cursor.getColumnIndex(COL_PHONE)),
                        cursor.getString(cursor.getColumnIndex(COL_AUDIO_PATH))
                );
                list.add(med);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return list;
    }

    // دریافت یک دارو با شناسه
    public Medication getMedication(long id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_NAME + " WHERE " + COL_ID + " = ?",
                new String[]{String.valueOf(id)});
        if (cursor != null && cursor.moveToFirst()) {
            Medication med = new Medication(
                    cursor.getLong(cursor.getColumnIndex(COL_ID)),
                    cursor.getString(cursor.getColumnIndex(COL_NAME)),
                    cursor.getString(cursor.getColumnIndex(COL_INSTRUCTIONS)),
                    cursor.getString(cursor.getColumnIndex(COL_PHOTO_PATH)),
                    cursor.getString(cursor.getColumnIndex(COL_TIME)),
                    cursor.getString(cursor.getColumnIndex(COL_PHONE)),
                    cursor.getString(cursor.getColumnIndex(COL_AUDIO_PATH))
            );
            cursor.close();
            db.close();
            return med;
        }
        if (cursor != null) cursor.close();
        db.close();
        return null;
    }
}
