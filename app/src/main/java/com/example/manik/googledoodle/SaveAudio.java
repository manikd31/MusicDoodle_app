package com.example.manik.googledoodle;

import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.Toast;

import com.example.manik.googledoodle.data.AudioContract;
import com.example.manik.googledoodle.data.AudioDbHelper;


import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Manik on 31-07-2017.
 */

public class SaveAudio extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy");

    SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a");

    private static final int EXISTING_AUDIO_LOADER = 0;

    private Uri mCurrentAudioUri;

    Button saveFileButton, discardFileButton;

    EditText fileNameEditText;

    StringBuilder pianoSequence = new StringBuilder("");
    StringBuilder celloSequence = new StringBuilder("");
    StringBuilder xyloSequence = new StringBuilder("");

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.save_page);

        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                pianoSequence.append(String.valueOf(MainActivity.checkBoxSequence[i][j][0])).append(",");
                xyloSequence.append(String.valueOf(MainActivity.checkBoxSequence[i][j][1])).append(",");
                celloSequence.append(String.valueOf(MainActivity.checkBoxSequence[i][j][2])).append(",");
            }
            pianoSequence.deleteCharAt(pianoSequence.length() - 1);
            pianoSequence.append(";");
            xyloSequence.deleteCharAt(xyloSequence.length() - 1);
            xyloSequence.append(";");
            celloSequence.deleteCharAt(celloSequence.length() - 1);
            celloSequence.append(";");
        }

        fileNameEditText = (EditText) findViewById(R.id.save_file_by_name);

        saveFileButton = (Button) findViewById(R.id.save_file_button);
        discardFileButton = (Button) findViewById(R.id.discard_file_button);

        Intent intent = getIntent();
        mCurrentAudioUri = intent.getData();

        if (mCurrentAudioUri != null) {
            getSupportLoaderManager().initLoader(EXISTING_AUDIO_LOADER, null, this);
            saveFileButton.setText("DONE");
            discardFileButton.setText("CANCEL");
        } else {
            saveFileButton.setText("SAVE");
            discardFileButton.setText("DISCARD");
        }

        saveFileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String fileNameStr = fileNameEditText.getText().toString().trim();
                if (TextUtils.isEmpty(fileNameStr)) {
                    Toast.makeText(SaveAudio.this, "Please enter a valid name", Toast.LENGTH_SHORT).show();
                } else {
                        saveAudio();
                        Intent savedIntent = new Intent(SaveAudio.this, MainActivity.class);
                        startActivity(savedIntent);
                }
            }
        });

        discardFileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(SaveAudio.this);
                builder.setMessage("Are you sure you don't want to save this file?");
                builder.setNegativeButton("YES", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Intent backIntent = new Intent(SaveAudio.this, MainActivity.class);
                        startActivity(backIntent);
                    }
                });

                builder.setPositiveButton("NO", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (dialogInterface != null) {
                            dialogInterface.dismiss();
                        }
                    }
                });

                AlertDialog alertDialog = builder.create();
                alertDialog.show();
            }
        });
    }

    private void saveAudio() {
        String audioName = fileNameEditText.getText().toString().trim();
        String audioDate = dateFormat.format(new Date());
        String audioTime = timeFormat.format(new Date());
        String audioXylo = xyloSequence.toString();
        String audioPiano = pianoSequence.toString();
        String audioCello = celloSequence.toString();
        String audioTempo = MainActivity.tempoString;

        if (mCurrentAudioUri == null && TextUtils.isEmpty(audioName))
            return;

        ContentValues values = new ContentValues();
        values.put(AudioContract.AudioEntry.COLUMN_AUDIO_NAME, audioName);
        values.put(AudioContract.AudioEntry.COLUMN_AUDIO_DATE, audioDate);
        values.put(AudioContract.AudioEntry.COLUMN_AUDIO_TIME, audioTime);
        values.put(AudioContract.AudioEntry.COLUMN_AUDIO_PIANO, audioPiano);
        values.put(AudioContract.AudioEntry.COLUMN_AUDIO_XYLO, audioXylo);
        values.put(AudioContract.AudioEntry.COLUMN_AUDIO_CELLO, audioCello);
        values.put(AudioContract.AudioEntry.COLUMN_AUDIO_TEMPO, audioTempo);

        if (mCurrentAudioUri == null) {

            Uri newUri = getContentResolver().insert(AudioContract.AudioEntry.CONTENT_URI, values);

            if (newUri == null) {

                // Toast to denote that there was some error in saving the file
                Toast.makeText(this, "Error with saving Audio File", Toast.LENGTH_SHORT).show();

            } else {

                // Toast to denote that there was no error in saving the file
                Toast.makeText(this, "Audio File saved successfully", Toast.LENGTH_SHORT).show();

                /*
                // Toast to display saved file name (temporarily)
                Toast.makeText(this, "File Name : " + values.getAsString(AudioContract.AudioEntry.COLUMN_AUDIO_NAME),
                        Toast.LENGTH_SHORT).show();

                // Toast to display saved file date (temporarily)
                Toast.makeText(this, "File Date : " + values.getAsString(AudioContract.AudioEntry.COLUMN_AUDIO_DATE),
                        Toast.LENGTH_SHORT).show();

                // Toast to display saved file time (temporarily)
                Toast.makeText(this, "File Time : " + values.getAsString(AudioContract.AudioEntry.COLUMN_AUDIO_TIME),
                        Toast.LENGTH_SHORT).show();

                // Toast to display saved file tempo (temporarily)
                Toast.makeText(this, "File Tempo : " + values.getAsString(AudioContract.AudioEntry.COLUMN_AUDIO_TEMPO),
                        Toast.LENGTH_SHORT).show();

                // Toast to display saved file piano string (temporarily)
                Toast.makeText(this, "File Piano : " + values.getAsString(AudioContract.AudioEntry.COLUMN_AUDIO_PIANO),
                        Toast.LENGTH_SHORT).show();

                // Toast to display saved file xylo string (temporarily)
                Toast.makeText(this, "File Xylo : " + values.getAsString(AudioContract.AudioEntry.COLUMN_AUDIO_XYLO),
                        Toast.LENGTH_SHORT).show();

                // Toast to display saved file cello string (temporarily)
                Toast.makeText(this, "File Cello : " + values.getAsString(AudioContract.AudioEntry.COLUMN_AUDIO_CELLO),
                        Toast.LENGTH_SHORT).show();
                        */

            }
        }

        else {
            int rowsAffected = getContentResolver().update(mCurrentAudioUri, values, null, null);

            if (rowsAffected == 0) {
                Toast.makeText(this, "Error with updating Audio File", Toast.LENGTH_SHORT).show();
            }
            else {
                Toast.makeText(this, "Audio File updated successfully", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(SaveAudio.this);
        builder.setMessage("Are you sure you don't want to save this file?");
        builder.setNegativeButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Intent backIntent = new Intent(SaveAudio.this, MainActivity.class);
                startActivity(backIntent);
            }
        });

        builder.setPositiveButton("NO", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (dialogInterface != null) {
                    dialogInterface.dismiss();
                }
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String projection[] = {
                AudioContract.AudioEntry._ID,
                AudioContract.AudioEntry.COLUMN_AUDIO_NAME,
                AudioContract.AudioEntry.COLUMN_AUDIO_DATE,
                AudioContract.AudioEntry.COLUMN_AUDIO_TIME,
                AudioContract.AudioEntry.COLUMN_AUDIO_TEMPO,
                AudioContract.AudioEntry.COLUMN_AUDIO_PIANO,
                AudioContract.AudioEntry.COLUMN_AUDIO_XYLO,
                AudioContract.AudioEntry.COLUMN_AUDIO_CELLO
        };

        return new CursorLoader(this,
                mCurrentAudioUri,
                projection,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (cursor == null)
            return;

        if (cursor.moveToFirst()) {
            int nameColumnIndex = cursor.getColumnIndex(AudioContract.AudioEntry.COLUMN_AUDIO_NAME);
            // int dateColumnIndex = cursor.getColumnIndex(AudioContract.AudioEntry.COLUMN_AUDIO_DATE);
            // int timeColumnIndex = cursor.getColumnIndex(AudioContract.AudioEntry.COLUMN_AUDIO_TIME);

            String audioName = cursor.getString(nameColumnIndex);
            // String audioDate = cursor.getString(dateColumnIndex);
            // String audioTime = cursor.getString(timeColumnIndex);

            fileNameEditText.setText(audioName);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        fileNameEditText.setText("");
    }
}
