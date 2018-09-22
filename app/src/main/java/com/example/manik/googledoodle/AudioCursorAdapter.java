package com.example.manik.googledoodle;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.example.manik.googledoodle.data.AudioContract;

import org.w3c.dom.Text;

/**
 * Created by Manik on 01-08-2017.
 */

public class AudioCursorAdapter extends CursorAdapter {

    public AudioCursorAdapter(Context context, Cursor c) {
        super(context, c);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
        return LayoutInflater.from(context).inflate(R.layout.list_item, viewGroup, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        TextView nameTextView = view.findViewById(R.id.display_audio_name);
        TextView dateTextView = view.findViewById(R.id.display_audio_date);
        TextView timeTextView = view.findViewById(R.id.display_audio_time);

        int nameColumnIndex = cursor.getColumnIndex(AudioContract.AudioEntry.COLUMN_AUDIO_NAME);
        int dateColumnIndex = cursor.getColumnIndex(AudioContract.AudioEntry.COLUMN_AUDIO_DATE);
        int timeColumnIndex = cursor.getColumnIndex(AudioContract.AudioEntry.COLUMN_AUDIO_TIME);

        String audioName = cursor.getString(nameColumnIndex);
        String audioDate = cursor.getString(dateColumnIndex);
        String audioTime = cursor.getString(timeColumnIndex);

        nameTextView.setText(audioName);
        dateTextView.setText(audioDate);
        timeTextView.setText(audioTime);
    }
}
