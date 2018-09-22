package com.example.manik.googledoodle;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.manik.googledoodle.data.AudioContract;
import com.example.manik.googledoodle.data.AudioDbHelper;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Manik on 31-07-2017.
 */

public class PreviousRecords extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int AUDIO_LOADER = 0;

    AudioCursorAdapter mCursorAdapter;

    private AudioDbHelper mDbHelper;

    ImageView deleteAll, emptyImage;

    ListView listOfRecords;

    TextView recordingsView;

    private Uri mCurrentAudioUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.previous_records);

        emptyImage = (ImageView) findViewById(R.id.empty_view_image);

        mDbHelper = new AudioDbHelper(this);

        recordingsView = (TextView) findViewById(R.id.recordings_text_view);
        Typeface typeface = Typeface.createFromAsset(getAssets(), "fonts/archistico.ttf");
        recordingsView.setTypeface(typeface);

        emptyImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent startNew = new Intent(PreviousRecords.this, MainActivity.class);
                startActivity(startNew);
            }
        });

        listOfRecords = (ListView) findViewById(R.id.list_of_records);

        deleteAll = (ImageView) findViewById(R.id.delete_all_records);

        View emptyView = findViewById(R.id.empty_view);

        listOfRecords.setEmptyView(emptyView);

        mCursorAdapter = new AudioCursorAdapter(this, null);
        listOfRecords.setAdapter(mCursorAdapter);

        listOfRecords.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {

                Intent playSavedIntent = new Intent(PreviousRecords.this, MainActivity.class);

                Uri currentAudioUri = ContentUris.withAppendedId(AudioContract.AudioEntry.CONTENT_URI, id);

                SQLiteDatabase db = mDbHelper.getReadableDatabase();
                Cursor c = db.rawQuery("SELECT * FROM " + AudioContract.AudioEntry.TABLE_NAME
                        + " WHERE " + AudioContract.AudioEntry._ID + " = " + id,
                        null);

                if (c == null) {
                    Toast.makeText(PreviousRecords.this, "Cursor is NULL", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(PreviousRecords.this, "You may continue to play",
                            Toast.LENGTH_SHORT).show();

                    c.close();

                    playSavedIntent.setData(currentAudioUri);
                    startActivity(playSavedIntent);
                }
            }
        });

        listOfRecords.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int pos, final long id) {

                TextView nameView = (TextView) view.findViewById(R.id.display_audio_name);
                currentFileName = nameView.getText().toString();

                // Toast.makeText(PreviousRecords.this,
                //         "Long Click at position : " + listOfRecords.getItemAtPosition(pos).toString(),
                //         Toast.LENGTH_SHORT).show();

                AlertDialog.Builder builder = new AlertDialog.Builder(PreviousRecords.this);
                builder.setMessage("What action do you want to perform?");
                builder.setNegativeButton("RENAME", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        mCurrentAudioUri = ContentUris.withAppendedId(AudioContract.AudioEntry.CONTENT_URI, id);

                        // TODO: Rename the file here

                        saveAudioDialogBox();

                        /*
                        Intent renameIntent = new Intent(PreviousRecords.this, SaveAudio.class);

                        renameIntent.setData(mCurrentAudioUri);

                        startActivity(renameIntent);
                        */
                    }
                });

                builder.setPositiveButton("DELETE", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if(dialogInterface != null) {
                            dialogInterface.dismiss();

                            AlertDialog.Builder deleteBuilder = new AlertDialog.Builder(PreviousRecords.this);
                            deleteBuilder.setMessage("Do you want to permanently delete this Audio File?");
                            deleteBuilder.setNegativeButton("YES", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    mCurrentAudioUri = ContentUris.withAppendedId(AudioContract.AudioEntry.CONTENT_URI, id);
                                    if (mCurrentAudioUri != null) {
                                        int rowsDeleted = getContentResolver().delete(mCurrentAudioUri, null, null);
                                        if (rowsDeleted == 0) {
                                            Toast.makeText(PreviousRecords.this, "Error with deleting this Audio File",
                                                    Toast.LENGTH_SHORT).show();
                                        }
                                        else {
                                            Toast.makeText(PreviousRecords.this, "Audio File deleted", Toast.LENGTH_SHORT).show();
                                            String countQuery = "SELECT * FROM " + AudioContract.AudioEntry.TABLE_NAME + ";";
                                            SQLiteDatabase db = mDbHelper.getReadableDatabase();
                                            Cursor c = db.rawQuery(countQuery, null);
                                            int count = c.getCount();
                                            c.close();

                                            if (count == 0) {
                                                deleteAll.setClickable(false);
                                                deleteAll.setVisibility(View.INVISIBLE);
                                            } else {
                                                deleteAll.setClickable(true);
                                                deleteAll.setVisibility(View.VISIBLE);
                                            }
                                        }
                                    }
                                }
                            });

                            deleteBuilder.setPositiveButton("NO", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface2, int i) {
                                    if (dialogInterface2 != null) {
                                        dialogInterface2.dismiss();
                                    }
                                }
                            });

                            AlertDialog alert = deleteBuilder.create();
                            alert.show();
                        }
                    }
                });

                AlertDialog alert = builder.create();
                alert.show();

                return true;
            }
        });

        getSupportLoaderManager().initLoader(AUDIO_LOADER, null, this);

        String countQuery = "SELECT * FROM " + AudioContract.AudioEntry.TABLE_NAME + ";";
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        Cursor c = db.rawQuery(countQuery, null);
        int count = c.getCount();
        c.close();

        if (count == 0) {
            deleteAll.setClickable(false);
            deleteAll.setVisibility(View.INVISIBLE);
        } else {
            deleteAll.setClickable(true);
            deleteAll.setVisibility(View.VISIBLE);
        }

        deleteAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(PreviousRecords.this);
                builder.setMessage("Are you sure you want to delete all Audio Files?");
                builder.setNegativeButton("YES", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // TODO: Delete the entire database here
                        int rowsDeleted = getContentResolver()
                                .delete(AudioContract.AudioEntry.CONTENT_URI, null, null);

                        if (rowsDeleted == 0) {
                            Toast.makeText(PreviousRecords.this, "Error with deleting Audio Table",
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(PreviousRecords.this, "Audio Table deleted",
                                    Toast.LENGTH_SHORT).show();
                        }

                        deleteAll.setClickable(false);
                        deleteAll.setVisibility(View.INVISIBLE);
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

    private void saveAudioDialogBox() {

        // Initialise the alert dialog builder
        final AlertDialog.Builder builder = new AlertDialog.Builder(PreviousRecords.this);
        builder.setMessage("Rename file");

        // Initialise the edit-text field
        final EditText option = new EditText(PreviousRecords.this);
        option.setImeOptions(EditorInfo.IME_ACTION_DONE);
        option.setInputType(InputType.TYPE_TEXT_VARIATION_PERSON_NAME);
        // option.setTypeface(typeface);
        option.setMaxLines(1);

        if (mCurrentAudioUri != null) {
            option.setText(currentFileName);
        }

        // Add the edit text view to the builder
        builder.setView(option);

        /*
        if ("Yes".equals(option.getText().toString()) ||
                "YES".equals(option.getText().toString()) ||
                "yes".equals(option.getText().toString())) {
            finishAffinity();
        }
        */

        builder.setNegativeButton("DONE", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if ("".equals(option.getText().toString())) {
                    Toast.makeText(PreviousRecords.this, "Cannot save blank audio file", Toast.LENGTH_SHORT).show();
                    saveAudioDialogBox();
                } else {
                    // TODO: Add code to save file to database, etc etc.
                    audioName = option.getText().toString();

                    saveAudioToDb();
                }
            }
        });

        builder.setPositiveButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                AlertDialog.Builder builderCancel = new AlertDialog.Builder(PreviousRecords.this);
                builderCancel.setMessage("Changes unsaved. Continue?");

                builderCancel.setNegativeButton("YES", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (dialogInterface != null) {
                            dialogInterface.dismiss();
                        }
                    }
                });

                builderCancel.setPositiveButton("NO, SAVE CHANGES", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        saveAudioDialogBox();
                    }
                });

                AlertDialog dialogCancel = builderCancel.create();
                dialogCancel.show();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();

    }

    SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy");

    SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a");

    String audioName;

    private void saveAudioToDb() {

        audioName = audioName.trim();
        String audioDate = dateFormat.format(new Date());
        String audioTime = timeFormat.format(new Date());
        // String audioXylo = tempXyloSequence.toString();
        // String audioPiano = tempPianoSequence.toString();
        // String audioCello = tempCelloSequence.toString();
        // String audioTempo = tempoString;

        if (mCurrentAudioUri == null && TextUtils.isEmpty(audioName))
            return;

        ContentValues values = new ContentValues();
        values.put(AudioContract.AudioEntry.COLUMN_AUDIO_NAME, audioName);
        values.put(AudioContract.AudioEntry.COLUMN_AUDIO_DATE, audioDate);
        values.put(AudioContract.AudioEntry.COLUMN_AUDIO_TIME, audioTime);
        // values.put(AudioContract.AudioEntry.COLUMN_AUDIO_PIANO, audioPiano);
        // values.put(AudioContract.AudioEntry.COLUMN_AUDIO_XYLO, audioXylo);
        // values.put(AudioContract.AudioEntry.COLUMN_AUDIO_CELLO, audioCello);
        // values.put(AudioContract.AudioEntry.COLUMN_AUDIO_TEMPO, audioTempo);


        // This is the code that will never be run
        // ...UNREACHABLE CODE...
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

    String currentFileName;

    @Override
    public void onBackPressed() {
        Intent goBack = new Intent(PreviousRecords.this, MainActivity.class);
        startActivity(goBack);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String projection[] = {
                AudioContract.AudioEntry._ID,
                AudioContract.AudioEntry.COLUMN_AUDIO_NAME,
                AudioContract.AudioEntry.COLUMN_AUDIO_DATE,
                AudioContract.AudioEntry.COLUMN_AUDIO_TIME,
                // AudioContract.AudioEntry.COLUMN_AUDIO_PIANO,
                // AudioContract.AudioEntry.COLUMN_AUDIO_XYLO,
                // AudioContract.AudioEntry.COLUMN_AUDIO_CELLO
        };

        return new CursorLoader(this, AudioContract.AudioEntry.CONTENT_URI, projection,
                null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        mCursorAdapter.swapCursor(cursor);
        // currentFileName = cursor.getString(cursor.getColumnIndex(AudioContract.AudioEntry.COLUMN_AUDIO_NAME));
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mCursorAdapter.swapCursor(null);
    }
}
