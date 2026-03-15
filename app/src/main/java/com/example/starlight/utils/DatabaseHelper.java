package com.example.starlight.utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DB_NAME    = "starlight_users.db";
    private static final int    DB_VERSION = 1;

    private static final String TABLE      = "users";
    private static final String COL_ID     = "id";
    private static final String COL_NAME   = "name";
    private static final String COL_EMAIL  = "email";
    private static final String COL_PASS   = "password";

    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE + " (" +
                COL_ID    + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_NAME  + " TEXT NOT NULL, " +
                COL_EMAIL + " TEXT UNIQUE NOT NULL, " +
                COL_PASS  + " TEXT NOT NULL)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldV, int newV) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE);
        onCreate(db);
    }

    // Register new user — returns true if success
    public boolean signup(String name, String email, String password) {
        try {
            SQLiteDatabase db = getWritableDatabase();
            ContentValues cv = new ContentValues();
            cv.put(COL_NAME,  name.trim());
            cv.put(COL_EMAIL, email.trim().toLowerCase());
            cv.put(COL_PASS,  password);
            long result = db.insertOrThrow(TABLE, null, cv);
            db.close();
            return result != -1;
        } catch (Exception e) {
            return false; // email already exists
        }
    }

    // Login — returns user name if credentials match, null otherwise
    public String login(String email, String password) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query(TABLE, new String[]{COL_NAME},
                COL_EMAIL + "=? AND " + COL_PASS + "=?",
                new String[]{email.trim().toLowerCase(), password},
                null, null, null);
        String name = null;
        if (c.moveToFirst()) {
            name = c.getString(0);
        }
        c.close();
        db.close();
        return name;
    }

    // Check if email already registered
    public boolean emailExists(String email) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query(TABLE, new String[]{COL_ID},
                COL_EMAIL + "=?",
                new String[]{email.trim().toLowerCase()},
                null, null, null);
        boolean exists = c.getCount() > 0;
        c.close();
        db.close();
        return exists;
    }

    // Get user details by email
    public String[] getUserDetails(String email) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query(TABLE,
                new String[]{COL_NAME, COL_EMAIL},
                COL_EMAIL + "=?",
                new String[]{email.trim().toLowerCase()},
                null, null, null);
        String[] details = null;
        if (c.moveToFirst()) {
            details = new String[]{c.getString(0), c.getString(1)};
        }
        c.close();
        db.close();
        return details;
    }
}