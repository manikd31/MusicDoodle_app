package com.example.manik.googledoodle.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.MediaStore;

import com.example.manik.googledoodle.data.AudioContract.AudioEntry;

/**
 * Created by Manik on 01-08-2017.
 */

public class AudioDbHelper extends SQLiteOpenHelper {

    private final static String DATABASE_NAME = "audiostore.db";

    private final static int DATABASE_VERSION = 1;

    public AudioDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase database) {

        String SQL_CREATE_AUDIO_TABLE = "CREATE TABLE " + AudioEntry.TABLE_NAME + "("
                + AudioEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + AudioEntry.COLUMN_AUDIO_NAME + " TEXT NOT NULL, "
                + AudioEntry.COLUMN_AUDIO_DATE + " TEXT, "
                + AudioEntry.COLUMN_AUDIO_TIME + " TEXT, "
                + AudioEntry.COLUMN_AUDIO_TEMPO + " TEXT, "
                + AudioEntry.COLUMN_AUDIO_PIANO + " TEXT, "
                + AudioEntry.COLUMN_AUDIO_XYLO + " TEXT, "
                + AudioEntry.COLUMN_AUDIO_CELLO + " TEXT);";

        database.execSQL(SQL_CREATE_AUDIO_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
    }
}
