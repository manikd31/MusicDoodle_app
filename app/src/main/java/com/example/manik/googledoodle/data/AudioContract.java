package com.example.manik.googledoodle.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by Manik on 01-08-2017.
 */

public final class AudioContract {
    public static final String CONTENT_AUTHORITY = "com.example.manik.googledoodle";

    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static final String PATH_GOOGLE_DOODLE = "googledoodle";

    private AudioContract() {}

    public static final class AudioEntry implements BaseColumns {

        public static final String CONTENT_LIST_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
                + "/" + PATH_GOOGLE_DOODLE;

        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
                + "/" + PATH_GOOGLE_DOODLE;

        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_GOOGLE_DOODLE);

        public static final String TABLE_NAME = "googledoodle";

        public static final String _ID = BaseColumns._ID;
        public static final String COLUMN_AUDIO_NAME = "name";
        public static final String COLUMN_AUDIO_DATE = "date";
        public static final String COLUMN_AUDIO_TIME = "time";
        public static final String COLUMN_AUDIO_CELLO = "cello";
        public static final String COLUMN_AUDIO_PIANO = "piano";
        public static final String COLUMN_AUDIO_XYLO = "xylo";
        public static final String COLUMN_AUDIO_TEMPO = "tempo";
    }
}
