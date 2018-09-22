package com.example.manik.googledoodle.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import com.example.manik.googledoodle.data.AudioContract.AudioEntry;

/**
 * Created by Manik on 01-08-2017.
 */

public class AudioProvider extends ContentProvider{

    private static final String LOG_TAG = AudioProvider.class.getSimpleName();

    private AudioDbHelper mDbHelper;

    private static final int ALL_AUDIO = 100;

    private static final int AUDIO_ID = 101;

    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {

        sUriMatcher.addURI(AudioContract.CONTENT_AUTHORITY, AudioContract.PATH_GOOGLE_DOODLE, ALL_AUDIO);

        sUriMatcher.addURI(AudioContract.CONTENT_AUTHORITY, AudioContract.PATH_GOOGLE_DOODLE + "/#", AUDIO_ID);

    }

    @Override
    public boolean onCreate() {
        mDbHelper = new AudioDbHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        SQLiteDatabase database = mDbHelper.getReadableDatabase();

        Cursor cursor;

        int match = sUriMatcher.match(uri);
        switch (match) {
            case ALL_AUDIO:

                cursor = database.query(AudioEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;

            case AUDIO_ID:

                selection = AudioEntry._ID + "=?";
                selectionArgs = new String[] {String.valueOf(ContentUris.parseId(uri))};

                cursor = database.query(AudioEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;

            default:
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);
        }

        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        return cursor;
    }

    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case ALL_AUDIO:
                return AudioEntry.CONTENT_LIST_TYPE;
            case AUDIO_ID:
                return AudioEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri + " with match " + match);
        }
    }

    @Override
    public Uri insert( Uri uri, ContentValues contentValues) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case ALL_AUDIO:
                return insertAudioFile(uri, contentValues);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }

    private Uri insertAudioFile(Uri uri, ContentValues values) {
        String name = values.getAsString(AudioEntry.COLUMN_AUDIO_NAME);
        if (name == null) {
            throw new IllegalArgumentException("Audio file requires a name");
        }

        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        long id = database.insert(AudioEntry.TABLE_NAME, null, values);

        if (id == -1) {
            Log.e(LOG_TAG, "Failed to insert row for " + uri);
            return null;
        }

        getContext().getContentResolver().notifyChange(uri, null);

        return ContentUris.withAppendedId(uri, id);
    }

    @Override
    public int delete( Uri uri, String selection, String[] selectionArgs) {
        int rowsDeleted;

        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        final int match = sUriMatcher.match(uri);

        switch (match) {
            case ALL_AUDIO:
                rowsDeleted = database.delete(AudioEntry.TABLE_NAME, selection, selectionArgs);

                if (rowsDeleted != 0) {
                    getContext().getContentResolver().notifyChange(uri, null);
                    return rowsDeleted;
                }

            case AUDIO_ID:
                selection = AudioEntry._ID + "=?";
                selectionArgs =new String[] {String.valueOf(ContentUris.parseId(uri))};

                rowsDeleted = database.delete(AudioEntry.TABLE_NAME, selection, selectionArgs);

                if (rowsDeleted != 0) {
                    getContext().getContentResolver().notifyChange(uri, null);
                    return rowsDeleted;
                }

            default:
                throw new IllegalArgumentException("Delete is not supported for " + uri);
        }
    }

    @Override
    public int update( Uri uri, ContentValues contentValues, String selection, String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case AUDIO_ID:
                selection = AudioEntry._ID + "=?";
                selectionArgs = new String[] {String.valueOf(ContentUris.parseId(uri))};
                return updateAudio(uri, contentValues, selection, selectionArgs);

            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }
    }

    private int updateAudio(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        if (values.containsKey(AudioEntry.COLUMN_AUDIO_NAME)) {
            String oldName = values.getAsString(AudioEntry.COLUMN_AUDIO_NAME);
            if (oldName == null) {
                throw new IllegalArgumentException("Audio FIle requires a name");
            }
        }
        if (values.size() == 0) {
            return 0;
        }
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        int rowsUpdated = db.update(AudioEntry.TABLE_NAME, values, selection, selectionArgs);

        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsUpdated;
    }
}
