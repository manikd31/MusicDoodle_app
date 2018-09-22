package com.example.manik.googledoodle;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.media.AudioAttributes;
import android.media.Image;
import android.media.SoundPool;
import android.net.Uri;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.manik.googledoodle.data.AudioContract;
import com.example.manik.googledoodle.data.AudioDbHelper;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

import static com.example.manik.googledoodle.R.string.msec_100;
import static com.example.manik.googledoodle.R.string.save;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private boolean checkPauseState[][][] = new boolean[8][8][3];

    private int instrumentPauseState = 0;

    private long delayPauseState = 0;

    private boolean isPaused = false;

    public static String tempoString = "";

    LinearLayout myLayout;

    // ImageView pianoImg, xyloImg, celloImg;

    ImageView changeInstrumentView, viewList, saveView;

    TextView currentFileNameView;

    SoundPool mySounds;

    // Then look for pattern of first character.
    int pianoId[] = new int[8];
    int celloId[] = new int[8];
    int xyloId[] = new int[8];
    int beatId = 0;

    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            saveView.setClickable(true);
            saveView.setEnabled(true);
            saveView.setVisibility(View.VISIBLE);

            return false;
        }
    };

    private Spinner mTempoSpinner;

    // This is the check box array which will be used to play/store audio files
    // Argument 1 is for different notes
    // Argument 2 is for different time instants
    // Argument 3 is the type of instrument
    private boolean actualCheckBox[][][] = new boolean[8][8][3];

    public static int checkBoxSequence[][][] = new int[8][8][3];

    // This is tge check box array which will be used to display the grid
    // Argument 1 is for different notes
    // Argument 2 is for different time instants
    private CheckBox displayCheckBox[][] = new CheckBox[8][8];

    // Integer constants for instruments
    private static final int PIANO = 0;
    private static final int XYLOPHONE = 1;
    private static final int CELLO = 2;

    // The array to store the background colors
    int colors[] = new int[11];

    // The integer variable to store what type of instrument it is
    // Piano == 0
    // Xylophone == 1
    // Cello == 2
    int typeInstrument;

    // This is the reference check box array
    private CheckBox cb[] = new CheckBox[8];

    // The GridLayout
    GridLayout myGrid;

    boolean mHasAudioChanged = false;

    TextView status;

    // Handler object to handle runnables
    private android.os.Handler handler = new android.os.Handler();

    // The delay variable used for time-gaps
    long delayInMilliseconds;

    // The time variable used to display the time elapsed
    int elapsedTime = 0;

    // The instrument type used to denote the type of instrument selected
    int instrumentNumber = 0;

    private Uri mCurrentAudioUri;

    ImageView stopPlayBtn;

    private static final int EXISTING_AUDIO_LOADER = 0;

    StringBuilder tempPianoSequence = new StringBuilder("");
    StringBuilder tempXyloSequence = new StringBuilder("");
    StringBuilder tempCelloSequence = new StringBuilder("");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Media player controls
        initMediaPlayer();

        Intent intent = getIntent();

        mCurrentAudioUri = intent.getData();

        saveView = (ImageView) findViewById(R.id.save_layout);

        myGrid = (GridLayout) findViewById(R.id.my_grid_layout);

        changeInstrumentView = (ImageView) findViewById(R.id.change_instrument);

        stopPlayBtn = (ImageView) findViewById(R.id.play_or_stop);
        stopPlayBtn.setClickable(false);
        stopPlayBtn.setEnabled(false);
        stopPlayBtn.setVisibility(View.INVISIBLE);

        currentFileNameView = (TextView) findViewById(R.id.current_file_name_view);

        if (mCurrentAudioUri != null) {
            getSupportLoaderManager().initLoader(EXISTING_AUDIO_LOADER, null, this);
            currentFileNameView.setTextColor(Color.WHITE);
            Toast.makeText(this, "Audio File is Ready\nClick on the PLAY button at the bottom to play the audio", Toast.LENGTH_LONG).show();
            stopPlayBtn.setEnabled(true);
            stopPlayBtn.setClickable(true);
            stopPlayBtn.setVisibility(View.VISIBLE);
            myGrid.setClickable(false);
            // changeInstrumentView.setClickable(false);
        } else {
            initCheckBoxes();
            for (int i = 0; i < 8; i++) {
                for (int j = 0; j < 8; j++) {
                    displayCheckBox[i][j].setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if (!mHasAudioChanged) {
                                mHasAudioChanged = true;
                                saveView.setEnabled(true);
                                saveView.setClickable(true);
                                saveView.setVisibility(View.VISIBLE);
                            }
                        }
                    });
                }
            }
            currentFileNameView.setText(getString(R.string.no_name));
            currentFileNameView.setTextColor(Color.BLACK);
            saveView.setEnabled(false);
            saveView.setClickable(false);
            saveView.setVisibility(View.INVISIBLE);
            myGrid.setClickable(true);
        }

        viewList = (ImageView) findViewById(R.id.records);

        saveView.setClickable(false);
        saveView.setEnabled(false);
        saveView.setVisibility(View.INVISIBLE);

        myLayout = (LinearLayout) findViewById(R.id.my_linear_layout);

        changeInstrumentView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (instrumentNumber == 0) {
                    instrumentNumber++;
                    typeInstrument = XYLOPHONE;
                    displayGrid(typeInstrument);
                    changeInstrumentView.setImageResource(R.drawable.ic_xylophone);
                } else if (instrumentNumber == 1) {
                    instrumentNumber++;
                    typeInstrument = CELLO;
                    displayGrid(typeInstrument);
                    changeInstrumentView.setImageResource(R.drawable.ic_cello);
                } else if (instrumentNumber == 2) {
                    instrumentNumber = 0;
                    typeInstrument = PIANO;
                    displayGrid(typeInstrument);
                    changeInstrumentView.setImageResource(R.drawable.ic_piano);
                }
            }
        });

        // TODO: Add save file feature here
        saveView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // changeInstrumentView.setClickable(true);

                mHasAudioChanged = false;

                stopPlayBtn.setEnabled(false);
                stopPlayBtn.setClickable(false);
                stopPlayBtn.setVisibility(View.INVISIBLE);

                Toast.makeText(MainActivity.this, "Save Dialog Box", Toast.LENGTH_SHORT).show();

                for (int k = 0; k < 3; k++) {
                    for (int i = 0; i < 8; i++) {
                        for (int j = 0; j < 8; j++){
                            if (actualCheckBox[i][j][k]) {
                                checkBoxSequence[i][j][k] = 1;
                            } else {
                                checkBoxSequence[i][j][k] = 0;
                            }
                            // displayCheckBox[i][j].setChecked(false);
                        }
                    }
                }

                for (int i = 0; i < 8; i++) {
                    for (int j = 0; j < 8; j++) {
                        tempPianoSequence.append(String.valueOf(checkBoxSequence[i][j][0])).append(",");
                        tempXyloSequence.append(String.valueOf(checkBoxSequence[i][j][1])).append(",");
                        tempCelloSequence.append(String.valueOf(checkBoxSequence[i][j][2])).append(",");
                    }
                    tempPianoSequence.deleteCharAt(tempPianoSequence.length() - 1);
                    tempPianoSequence.append(";");
                    tempXyloSequence.deleteCharAt(tempXyloSequence.length() - 1);
                    tempXyloSequence.append(";");
                    tempCelloSequence.deleteCharAt(tempCelloSequence.length() - 1);
                    tempCelloSequence.append(";");
                }

                releaseMediaPlayer();

                // initMediaPlayer();

                /*
                Intent saveIntent = new Intent(MainActivity.this, SaveAudio.class);
                if (mCurrentAudioUri != null) {
                    saveIntent.setData(mCurrentAudioUri);
                }
                startActivity(saveIntent);
                */

                // TODO: Call the save audio dialog box from here, which is defined at the end of this code
                saveAudioDialogBox();
            }
        });

        stopPlayBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                myGrid.setClickable(true);

                mHasAudioChanged = false;

                for (int i = 0; i < 8; i++) {
                    for (int j = 0; j < 8; j++) {
                        displayCheckBox[i][j].setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                if (!mHasAudioChanged) {
                                    mHasAudioChanged = true;
                                    saveView.setEnabled(true);
                                    saveView.setClickable(true);
                                    saveView.setVisibility(View.VISIBLE);
                                }
                            }
                        });
                    }
                }

                stopPlayBtn.setClickable(false);
                stopPlayBtn.setEnabled(false);
                stopPlayBtn.setVisibility(View.INVISIBLE);

                // TODO: Play the selected audio file here.
                // currentFileNameView.setText(currentFileName);

                if (tempo.equals(getString(R.string.msec_500))) {
                    mTempoSpinner.setSelection(2);
                } else if (tempo.equals(getString(R.string.msec_750))) {
                    mTempoSpinner.setSelection(1);
                } else if (tempo.equals(getString(R.string.msec_250))) {
                    mTempoSpinner.setSelection(3);
                } else if (tempo.equals(getString(msec_100))) {
                    mTempoSpinner.setSelection(4);
                } else if (tempo.equals(getString(R.string.msec_1000))) {
                    mTempoSpinner.setSelection(0);
                }

                String[] rowsP = pianoSequence.split(";");
                String[] rowsX = xyloSequence.split(";");
                String[] rowsC = celloSequence.split(";");

                for (int i = 0; i < 8; i++) {
                    String[] colsP = rowsP[i].split(",");
                    String[] colsX = rowsX[i].split(",");
                    String[] colsC = rowsC[i].split(",");
                    for (int j = 0; j < 8; j++) {

                        if (colsP[j].equals("1")) {
                            actualCheckBox[i][j][0] = true;
                        } else {
                            actualCheckBox[i][j][0] = false;
                        }


                        if (colsX[j].equals("1")) {
                            actualCheckBox[i][j][1] = true;
                        } else {
                            actualCheckBox[i][j][1] = false;
                        }


                        if (colsC[j].equals("1")) {
                            actualCheckBox[i][j][2] = true;
                        } else {
                            actualCheckBox[i][j][2] = false;
                        }
                    }
                }
                displayGrid(typeInstrument);
            }
        });

        viewList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*
                for (int i = 0; i < 8; i++) {
                    for (int j = 0; j < 8; j++) {
                        displayCheckBox[i][j].setChecked(false);
                    }
                }
                */

                if (saveView.isEnabled()) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setMessage("Unsaved changes will be lost. Wish to continue?");
                    builder.setNegativeButton("YES", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            releaseMediaPlayer();

                            saveView.setClickable(false);
                            saveView.setEnabled(false);
                            saveView.setVisibility(View.INVISIBLE);

                            stopPlayBtn.setEnabled(false);
                            stopPlayBtn.setClickable(false);
                            stopPlayBtn.setVisibility(View.INVISIBLE);

                            mHasAudioChanged = false;

                            Intent viewListIntent = new Intent(MainActivity.this, PreviousRecords.class);
                            startActivity(viewListIntent);
                        }
                    });
                    builder.setPositiveButton("NO, Save Changes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                            if (dialogInterface != null) {
                                dialogInterface.dismiss();
                            }
                        }
                    });

                    AlertDialog dialog = builder.create();
                    dialog.show();

                } else {
                    releaseMediaPlayer();

                    saveView.setClickable(false);
                    saveView.setEnabled(false);
                    saveView.setVisibility(View.INVISIBLE);

                    stopPlayBtn.setEnabled(false);
                    stopPlayBtn.setClickable(false);
                    stopPlayBtn.setVisibility(View.INVISIBLE);

                    mHasAudioChanged = false;

                    Intent viewListIntent = new Intent(MainActivity.this, PreviousRecords.class);
                    startActivity(viewListIntent);
                }

            }
        });

        mTempoSpinner = (Spinner) findViewById(R.id.tempo_spinner);
        mTempoSpinner.setOnTouchListener(mTouchListener);
        setupSpinner();

        // Reference Check-boxes
        cb[0] = (CheckBox) findViewById(R.id.refer0);
        cb[1] = (CheckBox) findViewById(R.id.refer1);
        cb[2] = (CheckBox) findViewById(R.id.refer2);
        cb[3] = (CheckBox) findViewById(R.id.refer3);
        cb[4] = (CheckBox) findViewById(R.id.refer4);
        cb[5] = (CheckBox) findViewById(R.id.refer5);
        cb[6] = (CheckBox) findViewById(R.id.refer6);
        cb[7] = (CheckBox) findViewById(R.id.refer7);

        // Check boxes
        initCheckBoxes();

        // Initialise the color-array
        colors[0] = R.color.bgGreen;
        colors[1] = R.color.bgOrange;
        colors[2] = R.color.bgBlueGray;
        colors[3] = R.color.bgCyan;
        colors[4] = R.color.bgPurple;
        colors[5] = R.color.bgIndigo;
        colors[6] = R.color.bgRed;
        colors[7] = R.color.bgBrown;
        colors[8] = R.color.bgDeepPurple;
        colors[9] = R.color.bgPink;
        colors[10] = R.color.bgYellow;

        /*
        if (mCurrentAudioUri != null) {

            String[] columns = new String[]{
                    AudioContract.AudioEntry.COLUMN_AUDIO_NAME,
                    AudioContract.AudioEntry.COLUMN_AUDIO_DATE,
                    AudioContract.AudioEntry.COLUMN_AUDIO_TIME,
                    AudioContract.AudioEntry.COLUMN_AUDIO_TEMPO,
                    AudioContract.AudioEntry.COLUMN_AUDIO_PIANO,
                    AudioContract.AudioEntry.COLUMN_AUDIO_XYLO,
                    AudioContract.AudioEntry.COLUMN_AUDIO_CELLO
            };

            AudioDbHelper mDbHelper = new AudioDbHelper(this);
            SQLiteDatabase db = mDbHelper.getReadableDatabase();
            Cursor cursor = db.query(AudioContract.AudioEntry.TABLE_NAME,
                    columns,
                    null,
                    null,
                    null,
                    null,
                    null);

            if (cursor == null) {
                Toast.makeText(this, "CURSOR IS NULL", Toast.LENGTH_SHORT).show();
            } else {
                // Toast.makeText(this, "Cursor is Not Null\nYou may continue", Toast.LENGTH_SHORT).show();
            }
            cursor.close();
        }
        */

        /*
        // Set all check boxes to NOT CLICKABLE by the user.
        for (int i = 0; i < 7; i++) {
            for (int j = 0; j < 7; j++) {
                actualCheckBox[i][j].setClickable(false);
            }
        }
        */

        /*
        nameEditText = (EditText) findViewById(R.id.name_edit_text);

        nameEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == EditorInfo.IME_ACTION_DONE) {
                    resetAll();
                    elapsedTime = 0;
                    nameStr = nameEditText.getText().toString().trim();
                    nameArray = nameStr.toCharArray();
                    if (!TextUtils.isEmpty(nameStr)) {
                        index = 0;
                        checkPattern();
                    }
                }
                return false;
            }
        });

        nameStr = nameEditText.getText().toString().trim();
        nameArray = nameStr.toCharArray();
        */

        status = (TextView) findViewById(R.id.status_view);

        /*
        findViewById(R.id.play_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                check();
            }
        });
        */

        findViewById(R.id.reset_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                reset();
            }
        });

        findViewById(R.id.reset_all_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setupSpinner();
                resetAll();
            }
        });

        startTimer();
    }

    // This method will be used to reset the progress of the current instrument
    private void reset() {

        saveView.setClickable(true);
        saveView.setEnabled(true);
        saveView.setVisibility(View.VISIBLE);

        int a, b;
        for (a = 0; a < 8; a++) {
            // cb[a].setChecked(false);
            for (b = 0; b < 8; b++) {
                displayCheckBox[a][b].setChecked(false);
                actualCheckBox[a][b][typeInstrument] = false;
            }
        }
        displayGrid(typeInstrument);
        // status.setText("0 minute(s) 0 second(s)");
    }

    // This method will be used to reset the progress of all instruments
    private void resetAll() {

        saveView.setClickable(true);
        saveView.setEnabled(true);
        saveView.setVisibility(View.VISIBLE);

        elapsedTime = 0;
        int a, b;
        for (a = 0; a < 8; a++) {
            cb[a].setChecked(false);
            for (b = 0; b < 8; b++) {
                displayCheckBox[a][b].setChecked(false);
                actualCheckBox[a][b][0] = false;
                actualCheckBox[a][b][1] = false;
                actualCheckBox[a][b][2] = false;
            }
        }
        typeInstrument = CELLO;
        displayGrid(typeInstrument);
    }

    Runnable runnable = new Runnable() {
        @Override
        public void run() {

            // This is the index of checkbox that needs to be checked at an instant of time
            int checkBoxNumber = elapsedTime % 8;

            mySounds.play(beatId, 1, 1, 1, 0, 1);

            elapsedTime++;
            status.setText(String.valueOf(checkBoxNumber + 1));
            startTimer();

            cb[checkBoxNumber].setChecked(true);

            /*
            int result = mAudioManager.requestAudioFocus(mOnAudioFocusChangeListener,
                    //Use the Music Stream
                    AudioManager.STREAM_MUSIC,
                    //Request Permanent Focus
                    AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
                    */

            for (int i = 0; i < 8; i++) {
                for (int j = 0; j < 8; j++) {
                    if (displayCheckBox[i][j].isChecked()) {
                        actualCheckBox[i][j][typeInstrument] = true;
                    } else {
                        actualCheckBox[i][j][typeInstrument] = false;
                    }
                }
            }

            // Checks for which audio files to play.
            for (int x = 0; x < 8; x++) {
                if (actualCheckBox[x][checkBoxNumber][0]) {
                    mySounds.play(pianoId[x], 0.75f, 0.75f, 1, 0, 1);
                }
                if (actualCheckBox[x][checkBoxNumber][1]) {
                    mySounds.play(xyloId[x], 1, 1, 1, 0, 1);
                }
                if (actualCheckBox[x][checkBoxNumber][2]) {
                    mySounds.play(celloId[x], 1.5f, 1.5f, 1, 0, 1);
                }
                /*
                // check for piano sounds
                if (actualCheckBox[x][checkBoxNumber][0]) {
                    mySounds.play(pianoId[x], 0.25f, 0.25f, 1, 0, 1);
                }
                // check for xylophone sounds
                if (actualCheckBox[x][checkBoxNumber][1]) {
                    mySounds.play(xyloId[x], 1, 1, 1, 0, 1);
                }
                // check for cello sounds
                if (actualCheckBox[x][checkBoxNumber][2]) {
                    mySounds.play(celloId[x], 1, 1, 1, 0, 1);
                }
                */
            }

            Random random = new Random();

            myLayout.setBackgroundColor(getResources().getColor(colors[random.nextInt(11)]));

            // Only used for the reference check boxes
            if (checkBoxNumber == 0) {
                cb[7].setChecked(false);
            } else {
                cb[checkBoxNumber - 1].setChecked(false);
            }
        }
    };

    public void startTimer() {
        handler.postDelayed(runnable, delayInMilliseconds);
    }

    @Override
    protected void onStop() {
        super.onStop();
        handler.removeCallbacks(runnable);
    }

    // This method creates the media-players to play different sounds
    private void initMediaPlayer() {

        AudioAttributes attr = new AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .build();

        mySounds = new SoundPool.Builder()
                .setAudioAttributes(attr)
                .setMaxStreams(10)
                .build();

        xyloId[0] = mySounds.load(this, R.raw.xylo_c, 1);
        xyloId[1] = mySounds.load(this, R.raw.xylo_d, 1);
        xyloId[2] = mySounds.load(this, R.raw.xylo_e, 1);
        xyloId[3] = mySounds.load(this, R.raw.xylo_f, 1);
        xyloId[4] = mySounds.load(this, R.raw.xylo_g, 1);
        xyloId[5] = mySounds.load(this, R.raw.xylo_a, 1);
        xyloId[6] = mySounds.load(this, R.raw.xylo_b, 1);
        xyloId[7] = mySounds.load(this, R.raw.xylo_c_next, 1);

        pianoId[0] = mySounds.load(this, R.raw.piano_c, 1);
        pianoId[1] = mySounds.load(this, R.raw.piano_d, 1);
        pianoId[2] = mySounds.load(this, R.raw.piano_e, 1);
        pianoId[3] = mySounds.load(this, R.raw.piano_f, 1);
        pianoId[4] = mySounds.load(this, R.raw.piano_g, 1);
        pianoId[5] = mySounds.load(this, R.raw.piano_a, 1);
        pianoId[6] = mySounds.load(this, R.raw.piano_b, 1);
        pianoId[7] = mySounds.load(this, R.raw.piano_c_next, 1);

        celloId[0] = mySounds.load(this, R.raw.cello_c, 1);
        celloId[1] = mySounds.load(this, R.raw.cello_d, 1);
        celloId[2] = mySounds.load(this, R.raw.cello_e, 1);
        celloId[3] = mySounds.load(this, R.raw.cello_f, 1);
        celloId[4] = mySounds.load(this, R.raw.cello_g, 1);
        celloId[5] = mySounds.load(this, R.raw.cello_a, 1);
        celloId[6] = mySounds.load(this, R.raw.cello_b, 1);
        celloId[7] = mySounds.load(this, R.raw.cello_c_next, 1);

        beatId = mySounds.load(this, R.raw.deep_bass_beat, 1);
    }

    // This method unloads all soundpool audio
    private void releaseMediaPlayer() {
        for (int i = 0; i < 8; i++) {
            mySounds.unload(pianoId[i]);
            mySounds.unload(xyloId[i]);
            mySounds.unload(celloId[i]);
        }
        mySounds.unload(beatId);

        mySounds.release();
    }

    // This method will be called to display the grid of check boxes, and will store sounds for
    // each type of instruments
    private void displayGrid(int type) {
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                if (actualCheckBox[i][j][type])
                    displayCheckBox[i][j].setChecked(true);
                else
                    displayCheckBox[i][j].setChecked(false);
            }
        }
    }

    // This method initialises the checkboxes to be handled
    private void initCheckBoxes() {
        displayCheckBox[0][0] = (CheckBox) findViewById(R.id.letter_box_1);
        displayCheckBox[0][1] = (CheckBox) findViewById(R.id.letter_box_2);
        displayCheckBox[0][2] = (CheckBox) findViewById(R.id.letter_box_3);
        displayCheckBox[0][3] = (CheckBox) findViewById(R.id.letter_box_4);
        displayCheckBox[0][4] = (CheckBox) findViewById(R.id.letter_box_5);
        displayCheckBox[0][5] = (CheckBox) findViewById(R.id.letter_box_6);
        displayCheckBox[0][6] = (CheckBox) findViewById(R.id.letter_box_7);
        displayCheckBox[0][7] = (CheckBox) findViewById(R.id.letter_box_8);
        displayCheckBox[1][0] = (CheckBox) findViewById(R.id.letter_box_9);
        displayCheckBox[1][1] = (CheckBox) findViewById(R.id.letter_box_10);
        displayCheckBox[1][2] = (CheckBox) findViewById(R.id.letter_box_11);
        displayCheckBox[1][3] = (CheckBox) findViewById(R.id.letter_box_12);
        displayCheckBox[1][4] = (CheckBox) findViewById(R.id.letter_box_13);
        displayCheckBox[1][5] = (CheckBox) findViewById(R.id.letter_box_14);
        displayCheckBox[1][6] = (CheckBox) findViewById(R.id.letter_box_15);
        displayCheckBox[1][7] = (CheckBox) findViewById(R.id.letter_box_16);
        displayCheckBox[2][0] = (CheckBox) findViewById(R.id.letter_box_17);
        displayCheckBox[2][1] = (CheckBox) findViewById(R.id.letter_box_18);
        displayCheckBox[2][2] = (CheckBox) findViewById(R.id.letter_box_19);
        displayCheckBox[2][3] = (CheckBox) findViewById(R.id.letter_box_20);
        displayCheckBox[2][4] = (CheckBox) findViewById(R.id.letter_box_21);
        displayCheckBox[2][5] = (CheckBox) findViewById(R.id.letter_box_22);
        displayCheckBox[2][6] = (CheckBox) findViewById(R.id.letter_box_23);
        displayCheckBox[2][7] = (CheckBox) findViewById(R.id.letter_box_24);
        displayCheckBox[3][0] = (CheckBox) findViewById(R.id.letter_box_25);
        displayCheckBox[3][1] = (CheckBox) findViewById(R.id.letter_box_26);
        displayCheckBox[3][2] = (CheckBox) findViewById(R.id.letter_box_27);
        displayCheckBox[3][3] = (CheckBox) findViewById(R.id.letter_box_28);
        displayCheckBox[3][4] = (CheckBox) findViewById(R.id.letter_box_29);
        displayCheckBox[3][5] = (CheckBox) findViewById(R.id.letter_box_30);
        displayCheckBox[3][6] = (CheckBox) findViewById(R.id.letter_box_31);
        displayCheckBox[3][7] = (CheckBox) findViewById(R.id.letter_box_32);
        displayCheckBox[4][0] = (CheckBox) findViewById(R.id.letter_box_33);
        displayCheckBox[4][1] = (CheckBox) findViewById(R.id.letter_box_34);
        displayCheckBox[4][2] = (CheckBox) findViewById(R.id.letter_box_35);
        displayCheckBox[4][3] = (CheckBox) findViewById(R.id.letter_box_36);
        displayCheckBox[4][4] = (CheckBox) findViewById(R.id.letter_box_37);
        displayCheckBox[4][5] = (CheckBox) findViewById(R.id.letter_box_38);
        displayCheckBox[4][6] = (CheckBox) findViewById(R.id.letter_box_39);
        displayCheckBox[4][7] = (CheckBox) findViewById(R.id.letter_box_40);
        displayCheckBox[5][0] = (CheckBox) findViewById(R.id.letter_box_41);
        displayCheckBox[5][1] = (CheckBox) findViewById(R.id.letter_box_42);
        displayCheckBox[5][2] = (CheckBox) findViewById(R.id.letter_box_43);
        displayCheckBox[5][3] = (CheckBox) findViewById(R.id.letter_box_44);
        displayCheckBox[5][4] = (CheckBox) findViewById(R.id.letter_box_45);
        displayCheckBox[5][5] = (CheckBox) findViewById(R.id.letter_box_46);
        displayCheckBox[5][6] = (CheckBox) findViewById(R.id.letter_box_47);
        displayCheckBox[5][7] = (CheckBox) findViewById(R.id.letter_box_48);
        displayCheckBox[6][0] = (CheckBox) findViewById(R.id.letter_box_49);
        displayCheckBox[6][1] = (CheckBox) findViewById(R.id.letter_box_50);
        displayCheckBox[6][2] = (CheckBox) findViewById(R.id.letter_box_51);
        displayCheckBox[6][3] = (CheckBox) findViewById(R.id.letter_box_52);
        displayCheckBox[6][4] = (CheckBox) findViewById(R.id.letter_box_53);
        displayCheckBox[6][5] = (CheckBox) findViewById(R.id.letter_box_54);
        displayCheckBox[6][6] = (CheckBox) findViewById(R.id.letter_box_55);
        displayCheckBox[6][7] = (CheckBox) findViewById(R.id.letter_box_56);
        displayCheckBox[7][0] = (CheckBox) findViewById(R.id.letter_box_57);
        displayCheckBox[7][1] = (CheckBox) findViewById(R.id.letter_box_58);
        displayCheckBox[7][2] = (CheckBox) findViewById(R.id.letter_box_59);
        displayCheckBox[7][3] = (CheckBox) findViewById(R.id.letter_box_60);
        displayCheckBox[7][4] = (CheckBox) findViewById(R.id.letter_box_61);
        displayCheckBox[7][5] = (CheckBox) findViewById(R.id.letter_box_62);
        displayCheckBox[7][6] = (CheckBox) findViewById(R.id.letter_box_63);
        displayCheckBox[7][7] = (CheckBox) findViewById(R.id.letter_box_64);
    }

    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setMessage("Are you sure you want to go back without saving?");
        builder.setNegativeButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                mHasAudioChanged = false;
                Intent backIntent = new Intent(MainActivity.this, HomePage.class);
                releaseMediaPlayer();
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
    protected void onPause() {
        super.onPause();

        isPaused = true;

        for (int k = 0; k < 3; k++) {
            for (int i = 0; i < 8; i++) {
                for (int j = 0; j < 8; j++) {
                    checkPauseState[i][j][k] = actualCheckBox[i][j][k];
                }
            }
        }

        instrumentPauseState = instrumentNumber;

        delayPauseState = delayInMilliseconds;
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (isPaused) {

            isPaused = false;

            startTimer();

            for (int k = 0; k < 3; k++) {
                for (int i = 0; i < 8; i++) {
                    for (int j = 0; j < 8; j++) {
                        actualCheckBox[i][j][k] = checkPauseState[i][j][k];
                    }
                }
            }

            instrumentNumber = instrumentPauseState;

            for (int i = 0; i < 8; i++) {
                for (int j = 0; j < 8; j++) {
                    if (actualCheckBox[i][j][instrumentPauseState]) {
                        displayCheckBox[i][j].setChecked(true);
                    } else {
                        displayCheckBox[i][j].setChecked(false);
                    }
                }
            }

            delayInMilliseconds = delayPauseState;

            if (delayInMilliseconds == 100) {
                mTempoSpinner.setSelection(4);
            } else if (delayInMilliseconds == 250) {
                mTempoSpinner.setSelection(3);
            } else if (delayInMilliseconds == 500) {
                mTempoSpinner.setSelection(2);
            } else if (delayInMilliseconds == 750) {
                mTempoSpinner.setSelection(1);
            } else if (delayInMilliseconds == 1000) {
                mTempoSpinner.setSelection(0);
            }

        }
    }


    private void setupSpinner() {
        ArrayAdapter spinnerAdapter = ArrayAdapter.createFromResource(this,
                R.array.array_tempo_options, android.R.layout.simple_spinner_item);

        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        mTempoSpinner.setAdapter(spinnerAdapter);

        mTempoSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selection = (String) parent.getItemAtPosition(position);
                if (!TextUtils.isEmpty(selection)) {
                    if (selection.equals(getString(R.string.msec_500))) {
                        delayInMilliseconds = 500;
                        tempoString = getString(R.string.msec_500);
                    } else if (selection.equals(getString(R.string.msec_750))) {
                        delayInMilliseconds = 750;
                        tempoString = getString(R.string.msec_750);
                    } else if (selection.equals(getString(R.string.msec_250))) {
                        delayInMilliseconds = 250;
                        tempoString = getString(R.string.msec_250);
                    } else if (selection.equals(getString(msec_100))) {
                        delayInMilliseconds = 100;
                        tempoString = getString(msec_100);
                    } else if (selection.equals(getString(R.string.msec_1000))){
                        delayInMilliseconds = 1000;
                        tempoString = getString(R.string.msec_1000);
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                delayInMilliseconds = 500;
                tempoString = getString(R.string.msec_500);
            }
        });
    }

    @Override
    public android.support.v4.content.Loader<Cursor> onCreateLoader(int id, Bundle args) {

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

    String currentFileName, tempo;
    String pianoSequence, xyloSequence, celloSequence;

    @Override
    public void onLoadFinished(android.support.v4.content.Loader<Cursor> loader, Cursor cursor) {
        if (cursor == null)
            return;

        if (cursor.moveToFirst()) {
            int currentFileNameIndex = cursor.getColumnIndex(AudioContract.AudioEntry.COLUMN_AUDIO_NAME);
            currentFileName = cursor.getString(currentFileNameIndex);

            currentFileNameView.setText(currentFileName);

            tempo = cursor.getString(cursor.getColumnIndex(AudioContract.AudioEntry.COLUMN_AUDIO_TEMPO));

            /*
            if (tempo.equals(getString(R.string.msec_500))) {
                mTempoSpinner.setSelection(2);
            } else if (tempo.equals(getString(R.string.msec_750))) {
                mTempoSpinner.setSelection(1);
            } else if (tempo.equals(getString(R.string.msec_250))) {
                mTempoSpinner.setSelection(3);
            } else if (tempo.equals(getString(msec_100))) {
                mTempoSpinner.setSelection(4);
            } else if (tempo.equals(getString(R.string.msec_1000))) {
                mTempoSpinner.setSelection(0);
            }
            */

            pianoSequence = cursor.getString(cursor.getColumnIndex(AudioContract.AudioEntry.COLUMN_AUDIO_PIANO));
            xyloSequence = cursor.getString(cursor.getColumnIndex(AudioContract.AudioEntry.COLUMN_AUDIO_XYLO));
            celloSequence = cursor.getString(cursor.getColumnIndex(AudioContract.AudioEntry.COLUMN_AUDIO_CELLO));

            /*
            Toast.makeText(this, "File Piano : " + pianoSequence, Toast.LENGTH_SHORT).show();
            Toast.makeText(this, "File Xylo : " + xyloSequence, Toast.LENGTH_SHORT).show();
            Toast.makeText(this, "File Cello : " + celloSequence, Toast.LENGTH_SHORT).show();
            */

        }
    }

    @Override
    public void onLoaderReset(android.support.v4.content.Loader<Cursor> loader) {
        for (int k = 0; k < 3; k++) {
            for (int i = 0; i < 8; i++) {
                for (int j = 0; j < 8; j++) {
                    actualCheckBox[i][j][k] = false;
                }
            }
        }
    }


    /*
    int index = 0;

    private void checkPattern() {

        reset();

        switch (nameArray[index]) {
            case 'A':
            case 'a':
                patternA();
                break;

            case 'B':
            case 'b':
                patternB();
                break;

            case 'C':
            case 'c':
                patternC();
                break;

            case 'D':
            case 'd':
                patternD();
                break;

            case 'E':
            case 'e':
                patternE();
                break;

            case 'F':
            case 'f':
                patternF();
                break;

            case 'G':
            case 'g':
                patternG();
                break;

            case 'H':
            case 'h':
                patternH();
                break;

            case 'I':
            case 'i':
                patternI();
                break;

            case 'J':
            case 'j':
                patternJ();
                break;

            case 'K':
            case 'k':
                patternK();
                break;

            case 'L':
            case 'l':
                patternL();
                break;

            case 'M':
            case 'm':
                patternM();
                break;

            case 'N':
            case 'n':
                patternN();
                break;

            case 'O':
            case 'o':
                patternO();
                break;

            case 'P':
            case 'p':
                patternP();
                break;

            case 'Q':
            case 'q':
                patternQ();
                break;

            case 'R':
            case 'r':
                patternR();
                break;

            case 'S':
            case 's':
                patternS();
                break;

            case 'T':
            case 't':
                patternT();
                break;

            case 'U':
            case 'u':
                patternU();
                break;

            case 'V':
            case 'v':
                patternV();
                break;

            case 'W':
            case 'w':
                patternW();
                break;

            case 'X':
            case 'x':
                patternX();
                break;

            case 'Y':
            case 'y':
                patternY();
                break;

            case 'Z':
            case 'z':
                patternZ();
                break;

            default:
                Toast.makeText(this, "Character is not an Alphabet", Toast.LENGTH_SHORT).show();
        }
    }
    */

    /*
    // Method is called when first letter of name is A.
    private void patternA() {
        for (int i = 2; i < 7; i++) {
            displayCheckBox[i][1].setChecked(true);
            actualCheckBox[i][1][typeInstrument] = true;

            displayCheckBox[i][5].setChecked(true);
            actualCheckBox[i][5][typeInstrument] = true;
        }
        for (int j = 1; j < 6; j++) {
            displayCheckBox[3][j].setChecked(true);
            actualCheckBox[3][j][typeInstrument] = true;
        }
        displayCheckBox[0][3].setChecked(true);
        actualCheckBox[0][3][typeInstrument] = true;

        displayCheckBox[1][2].setChecked(true);
        actualCheckBox[1][2][typeInstrument] = true;

        displayCheckBox[1][4].setChecked(true);
        actualCheckBox[1][4][typeInstrument] = true;
    }

    // Method is called when first letter of name is B.
    private void patternB() {
        for (int i = 0; i < 7; i++) {
            displayCheckBox[i][1].setChecked(true);
            actualCheckBox[i][1][typeInstrument] = true;
        }
        for (int j = 1; j < 5; j++) {
            displayCheckBox[0][j].setChecked(true);
            actualCheckBox[0][j][typeInstrument] = true;
            displayCheckBox[3][j].setChecked(true);
            actualCheckBox[3][j][typeInstrument] = true;
            displayCheckBox[6][j].setChecked(true);
            actualCheckBox[6][j][typeInstrument] = true;
        }
        displayCheckBox[1][5].setChecked(true);
        actualCheckBox[1][5][typeInstrument] = true;
        displayCheckBox[2][5].setChecked(true);
        actualCheckBox[2][5][typeInstrument] = true;
        displayCheckBox[4][5].setChecked(true);
        actualCheckBox[4][5][typeInstrument] = true;
        displayCheckBox[5][5].setChecked(true);
        actualCheckBox[5][5][typeInstrument] = true;
    }

    // Method is called when first letter of name is C.
    private void patternC() {
        for (int i = 2; i <= 4; i++) {
            displayCheckBox[0][i].setChecked(true);
            actualCheckBox[0][i][typeInstrument] = true;
            displayCheckBox[6][i].setChecked(true);
            actualCheckBox[6][i][typeInstrument] = true;
        }
        for (int j = 1; j <= 5; j++) {
            displayCheckBox[j][1].setChecked(true);
            actualCheckBox[j][1][typeInstrument] = true;
        }
        displayCheckBox[1][5].setChecked(true);
        actualCheckBox[1][5][typeInstrument] = true;
        displayCheckBox[5][5].setChecked(true);
        actualCheckBox[5][5][typeInstrument] = true;
    }

    // Method is called when first letter of name is D.
    private void patternD() {
        for (int i = 0; i < 7; i++) {
            displayCheckBox[i][1].setChecked(true);
            actualCheckBox[i][1][typeInstrument] = true;
        }
        for (int j = 1; j < 4; j++) {
            displayCheckBox[0][j].setChecked(true);
            actualCheckBox[0][j][typeInstrument] = true;
            displayCheckBox[6][j].setChecked(true);
            actualCheckBox[6][j][typeInstrument] = true;
        }
        for (int i = 2; i < 5; i++) {
            displayCheckBox[i][5].setChecked(true);
            actualCheckBox[i][5][typeInstrument] = true;
        }
        displayCheckBox[1][4].setChecked(true);
        actualCheckBox[1][4][typeInstrument] = true;
        displayCheckBox[5][4].setChecked(true);
        actualCheckBox[5][4][typeInstrument] = true;
    }

    // Method is called when first letter of name is E.
    private void patternE() {
        for (int i = 0; i < 7; i++) {
            displayCheckBox[i][1].setChecked(true);
            actualCheckBox[i][1][typeInstrument] = true;
        }
        for (int j = 1; j < 6; j++) {
            displayCheckBox[0][j].setChecked(true);
            actualCheckBox[0][j][typeInstrument] = true;
            displayCheckBox[6][j].setChecked(true);
            actualCheckBox[6][j][typeInstrument] = true;
        }
        for (int j = 1; j < 5; j++) {
            displayCheckBox[3][j].setChecked(true);
            actualCheckBox[3][j][typeInstrument] = true;
        }
    }

    // Method is called when first letter of name is F.
    private void patternF() {
        for (int i = 0; i < 7; i++) {
            displayCheckBox[i][1].setChecked(true);
            actualCheckBox[i][1][typeInstrument] = true;
        }
        for (int j = 1; j < 6; j++) {
            displayCheckBox[0][j].setChecked(true);
            actualCheckBox[0][j][typeInstrument] = true;
        }
        for (int j = 1; j < 5; j++) {
            displayCheckBox[3][j].setChecked(true);
            actualCheckBox[3][j][typeInstrument] = true;
        }
    }

    // Method is called when first letter of name is G.
    private void patternG() {
        for (int i = 2; i <= 4; i++) {
            displayCheckBox[0][i].setChecked(true);
            actualCheckBox[0][i][typeInstrument] = true;
            displayCheckBox[6][i].setChecked(true);
            actualCheckBox[6][i][typeInstrument] = true;
        }
        for (int j = 1; j <= 5; j++) {
            displayCheckBox[j][1].setChecked(true);
            actualCheckBox[j][1][typeInstrument] = true;
        }
        for (int j = 3; j < 6; j++) {
            displayCheckBox[3][j].setChecked(true);
            actualCheckBox[3][j][typeInstrument] = true;
        }
        displayCheckBox[1][5].setChecked(true);
        actualCheckBox[1][5][typeInstrument] = true;
        displayCheckBox[5][5].setChecked(true);
        actualCheckBox[5][5][typeInstrument] = true;
        displayCheckBox[4][5].setChecked(true);
        actualCheckBox[4][5][typeInstrument] = true;
    }

    // Method is called when first letter of name is H.
    private void patternH() {
        for (int i = 0; i < 7; i++) {
            displayCheckBox[i][1].setChecked(true);
            actualCheckBox[i][1][typeInstrument] = true;
            displayCheckBox[i][5].setChecked(true);
            actualCheckBox[i][5][typeInstrument] = true;
        }
        for (int j = 1; j < 6; j++) {
            displayCheckBox[3][j].setChecked(true);
            actualCheckBox[3][j][typeInstrument] = true;
        }
    }

    // Method is called when first letter of name is I.
    private void patternI() {
        for (int i = 0; i < 7; i++) {
            displayCheckBox[i][3].setChecked(true);
            actualCheckBox[i][3][typeInstrument] = true;
        }
        for (int j = 1; j < 6; j++) {
            displayCheckBox[0][j].setChecked(true);
            actualCheckBox[0][j][typeInstrument] = true;
            displayCheckBox[6][j].setChecked(true);
            actualCheckBox[6][j][typeInstrument] = true;
        }
    }

    // Method is called when first letter of name is J.
    private void patternJ() {
        for (int j = 1; j < 6; j++) {
            displayCheckBox[0][j].setChecked(true);
            actualCheckBox[0][j][typeInstrument] = true;
        }
        for (int i = 0; i < 6; i++) {
            displayCheckBox[i][4].setChecked(true);
            actualCheckBox[i][4][typeInstrument] = true;
        }
        displayCheckBox[4][1].setChecked(true);
        actualCheckBox[4][1][typeInstrument] = true;
        displayCheckBox[5][1].setChecked(true);
        actualCheckBox[5][1][typeInstrument] = true;
        displayCheckBox[6][2].setChecked(true);
        actualCheckBox[6][2][typeInstrument] = true;
        displayCheckBox[6][3].setChecked(true);
        actualCheckBox[6][3][typeInstrument] = true;
    }

    // Method is called when first letter of name is K.
    private void patternK() {
        for (int i = 0; i < 7; i++) {
            displayCheckBox[i][1].setChecked(true);
            actualCheckBox[i][1][typeInstrument] = true;
        }
        for (int i = 0, j = 5; i < 4 && j > 1; i++, j--) {
            displayCheckBox[i][j].setChecked(true);
            actualCheckBox[i][j][typeInstrument] = true;
            displayCheckBox[6-i][j].setChecked(true);
            actualCheckBox[6-i][j][typeInstrument] = true;
        }
    }

    // Method is called when first letter of name is L.
    private void patternL() {
        for (int i = 0; i < 7; i++) {
            displayCheckBox[i][1].setChecked(true);
            actualCheckBox[i][1][typeInstrument] = true;
        }
        for (int j = 1; j < 6; j++) {
            displayCheckBox[6][j].setChecked(true);
            actualCheckBox[6][j][typeInstrument] = true;
        }
    }

    // Method is called when first letter of name is M.
    private void patternM() {
        for (int i = 0; i < 7; i++) {
            displayCheckBox[i][0].setChecked(true);
            actualCheckBox[i][0][typeInstrument] = true;
            displayCheckBox[i][6].setChecked(true);
            actualCheckBox[i][6][typeInstrument] = true;
        }
        for (int j = 0; j <= 3; j++) {
            displayCheckBox[j][j].setChecked(true);
            actualCheckBox[j][j][typeInstrument] = true;
            displayCheckBox[j][6-j].setChecked(true);
            actualCheckBox[j][6-j][typeInstrument] = true;
        }
    }

    // Method is called when first letter of name is N.
    private void patternN() {
        for (int i = 0; i < 7; i++) {
            displayCheckBox[i][1].setChecked(true);
            actualCheckBox[i][1][typeInstrument] = true;
            displayCheckBox[i][5].setChecked(true);
            actualCheckBox[i][5][typeInstrument] = true;
        }
        displayCheckBox[1][2].setChecked(true);
        actualCheckBox[1][2][typeInstrument] = true;
        displayCheckBox[2][2].setChecked(true);
        actualCheckBox[2][2][typeInstrument] = true;
        displayCheckBox[3][3].setChecked(true);
        actualCheckBox[3][3][typeInstrument] = true;
        displayCheckBox[4][4].setChecked(true);
        actualCheckBox[4][4][typeInstrument] = true;
        displayCheckBox[5][4].setChecked(true);
        actualCheckBox[5][4][typeInstrument] = true;
    }

    // Method is called when first letter of name is O.
    private void patternO() {
        for (int j = 2; j < 5; j++) {
            displayCheckBox[0][j].setChecked(true);
            actualCheckBox[0][j][typeInstrument] = true;
            displayCheckBox[6][j].setChecked(true);
            actualCheckBox[6][j][typeInstrument] = true;
        }
        for (int i = 1; i < 6; i++) {
            displayCheckBox[i][1].setChecked(true);
            actualCheckBox[i][1][typeInstrument] = true;
            displayCheckBox[i][5].setChecked(true);
            actualCheckBox[i][5][typeInstrument] = true;
        }
    }

    // Method is called when first letter of name is P.
    private void patternP() {
        for (int i = 0; i < 7; i++) {
            displayCheckBox[i][1].setChecked(true);
            actualCheckBox[i][1][typeInstrument] = true;
        }
        for (int i = 1; i < 5; i++) {
            displayCheckBox[0][i].setChecked(true);
            actualCheckBox[0][i][typeInstrument] = true;
            displayCheckBox[3][i].setChecked(true);
            actualCheckBox[3][i][typeInstrument] = true;
        }
        displayCheckBox[1][5].setChecked(true);
        actualCheckBox[1][5][typeInstrument] = true;
        displayCheckBox[2][5].setChecked(true);
        actualCheckBox[2][5][typeInstrument] = true;
    }

    // Method is called when first letter of name is Q.
    private void patternQ() {
        for (int j = 2; j < 5; j++) {
            displayCheckBox[0][j].setChecked(true);
            actualCheckBox[0][j][typeInstrument] = true;
            displayCheckBox[6][j].setChecked(true);
            actualCheckBox[6][j][typeInstrument] = true;
        }
        for (int i = 1; i < 6; i++) {
            displayCheckBox[i][1].setChecked(true);
            actualCheckBox[i][1][typeInstrument] = true;
            displayCheckBox[i][5].setChecked(true);
            actualCheckBox[i][5][typeInstrument] = true;
        }
        displayCheckBox[4][3].setChecked(true);
        actualCheckBox[4][3][typeInstrument] = true;
        displayCheckBox[5][4].setChecked(true);
        actualCheckBox[5][4][typeInstrument] = true;
        displayCheckBox[6][5].setChecked(true);
        actualCheckBox[6][5][typeInstrument] = true;
    }

    // Method is called when first letter of name is R.
    private void patternR() {
        for (int i = 0; i < 7; i++) {
            displayCheckBox[i][1].setChecked(true);
            actualCheckBox[i][1][typeInstrument] = true;
        }
        for (int i = 1; i < 5; i++) {
            displayCheckBox[0][i].setChecked(true);
            actualCheckBox[0][i][typeInstrument] = true;
            displayCheckBox[3][i].setChecked(true);
            actualCheckBox[3][i][typeInstrument] = true;
        }
        displayCheckBox[1][5].setChecked(true);
        actualCheckBox[1][5][typeInstrument] = true;
        displayCheckBox[2][5].setChecked(true);
        actualCheckBox[2][5][typeInstrument] = true;
        displayCheckBox[4][3].setChecked(true);
        actualCheckBox[4][3][typeInstrument] = true;
        displayCheckBox[5][4].setChecked(true);
        actualCheckBox[5][4][typeInstrument] = true;
        displayCheckBox[6][5].setChecked(true);
        actualCheckBox[6][5][typeInstrument] = true;

    }

    // Method is called when first letter of name is S.
    private void patternS() {
        for (int j = 2; j < 5; j++) {
            displayCheckBox[0][j].setChecked(true);
            actualCheckBox[0][j][typeInstrument] = true;
            displayCheckBox[3][j].setChecked(true);
            actualCheckBox[3][j][typeInstrument] = true;
            displayCheckBox[6][j].setChecked(true);
            actualCheckBox[6][j][typeInstrument] = true;
        }
        displayCheckBox[1][1].setChecked(true);
        actualCheckBox[1][1][typeInstrument] = true;
        displayCheckBox[1][5].setChecked(true);
        actualCheckBox[1][5][typeInstrument] = true;
        displayCheckBox[2][1].setChecked(true);
        actualCheckBox[2][1][typeInstrument] = true;
        displayCheckBox[5][1].setChecked(true);
        actualCheckBox[5][1][typeInstrument] = true;
        displayCheckBox[5][5].setChecked(true);
        actualCheckBox[5][5][typeInstrument] = true;
        displayCheckBox[4][5].setChecked(true);
        actualCheckBox[4][5][typeInstrument] = true;
    }

    // Method is called when first letter of name is T.
    private void patternT() {
        for (int j = 1; j < 6; j++) {
            displayCheckBox[0][j].setChecked(true);
            actualCheckBox[0][j][typeInstrument] = true;
        }
        for (int i = 0; i < 7; i++) {
            displayCheckBox[i][3].setChecked(true);
            actualCheckBox[i][3][typeInstrument] = true;
        }
    }

    // Method is called when first letter of name is U.
    private void patternU() {
        for (int i = 0; i < 6; i++) {
            displayCheckBox[i][1].setChecked(true);
            actualCheckBox[i][1][typeInstrument] = true;
            displayCheckBox[i][5].setChecked(true);
            actualCheckBox[i][5][typeInstrument] = true;
        }
        displayCheckBox[6][2].setChecked(true);
        actualCheckBox[6][2][typeInstrument] = true;
        displayCheckBox[6][3].setChecked(true);
        actualCheckBox[6][3][typeInstrument] = true;
        displayCheckBox[6][4].setChecked(true);
        actualCheckBox[6][4][typeInstrument] = true;
    }

    // Method is called when first letter of name is V.
    private void patternV() {
        displayCheckBox[0][0].setChecked(true);
        actualCheckBox[0][0][typeInstrument] = true;
        displayCheckBox[0][6].setChecked(true);
        actualCheckBox[0][6][typeInstrument] = true;
        displayCheckBox[1][0].setChecked(true);
        actualCheckBox[1][0][typeInstrument] = true;
        displayCheckBox[1][6].setChecked(true);
        actualCheckBox[1][6][typeInstrument] = true;
        displayCheckBox[2][1].setChecked(true);
        actualCheckBox[2][1][typeInstrument] = true;
        displayCheckBox[2][5].setChecked(true);
        actualCheckBox[2][5][typeInstrument] = true;
        displayCheckBox[3][1].setChecked(true);
        actualCheckBox[3][1][typeInstrument] = true;
        displayCheckBox[3][5].setChecked(true);
        actualCheckBox[3][5][typeInstrument] = true;
        displayCheckBox[4][2].setChecked(true);
        actualCheckBox[4][2][typeInstrument] = true;
        displayCheckBox[4][4].setChecked(true);
        actualCheckBox[4][4][typeInstrument] = true;
        displayCheckBox[5][2].setChecked(true);
        actualCheckBox[5][2][typeInstrument] = true;
        displayCheckBox[5][4].setChecked(true);
        actualCheckBox[5][4][typeInstrument] = true;
        displayCheckBox[6][3].setChecked(true);
        actualCheckBox[6][3][typeInstrument] = true;
    }

    // Method is called when first letter of name is W.
    private void patternW() {
        for (int i = 0; i < 7; i++) {
            displayCheckBox[i][0].setChecked(true);
            actualCheckBox[i][0][typeInstrument] = true;
            displayCheckBox[i][6].setChecked(true);
            actualCheckBox[i][6][typeInstrument] = true;
        }
        for (int j = 3; j <= 6; j++) {
            displayCheckBox[j][j].setChecked(true);
            actualCheckBox[j][j][typeInstrument] = true;
            displayCheckBox[j][6-j].setChecked(true);
            actualCheckBox[j][6-j][typeInstrument] = true;
        }
    }

    // Method is called when first letter of name is X.
    private void patternX() {
        for (int i = 1; i < 6; i++) {
            displayCheckBox[i][i].setChecked(true);
            actualCheckBox[i][i][typeInstrument] = true;
            displayCheckBox[i][6-i].setChecked(true);
            actualCheckBox[i][6-i][typeInstrument] = true;
        }
        displayCheckBox[0][1].setChecked(true);
        actualCheckBox[0][1][typeInstrument] = true;
        displayCheckBox[0][5].setChecked(true);
        actualCheckBox[0][5][typeInstrument] = true;
        displayCheckBox[6][1].setChecked(true);
        actualCheckBox[6][1][typeInstrument] = true;
        displayCheckBox[6][5].setChecked(true);
        actualCheckBox[6][5][typeInstrument] = true;
    }

    // Method is called when first letter of name is Y.
    private void patternY() {
        for (int i = 0; i <= 3; i++) {
            displayCheckBox[i][6-i].setChecked(true);
            actualCheckBox[i][6-i][typeInstrument] = true;
            displayCheckBox[i][i].setChecked(true);
            actualCheckBox[i][i][typeInstrument] = true;
        }
        for (int x = 3; x < 7; x++) {
            displayCheckBox[x][3].setChecked(true);
            actualCheckBox[x][3][typeInstrument] = true;
        }
    }

    // Method is called when first letter of name is Z.
    private void patternZ() {
        for (int j = 0; j < 7; j++) {
            displayCheckBox[0][j].setChecked(true);
            actualCheckBox[0][j][typeInstrument] = true;
            displayCheckBox[6][j].setChecked(true);
            actualCheckBox[6][j][typeInstrument] = true;
        }
        for (int i = 0; i < 7; i++) {
            displayCheckBox[i][6-i].setChecked(true);
            actualCheckBox[i][6-i][typeInstrument] = true;
        }
        displayCheckBox[0][6].setChecked(false);
        actualCheckBox[0][6][typeInstrument] = false;
        displayCheckBox[6][0].setChecked(false);
        actualCheckBox[6][0][typeInstrument] = false;
    }

        MediaPlayer.OnCompletionListener mOnCompletionListener = new MediaPlayer.OnCompletionListener() {
        @Override
        public void onCompletion(MediaPlayer mediaPlayer) {
            releaseMediaPlayer();
        }
    };
    */

    // TODO: Add save button functionality in an AlertDialog message
    private void saveAudioDialogBox() {

        // Initialise the alert dialog builder
        final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setMessage("Enter the file name");

        // Initialise the edit-text field
        final EditText option = new EditText(MainActivity.this);
        option.setImeOptions(EditorInfo.IME_ACTION_DONE);
        option.setInputType(InputType.TYPE_TEXT_VARIATION_PERSON_NAME);
        option.setInputType(InputType.TYPE_TEXT_FLAG_CAP_WORDS);
        // option.setTypeface(typeface);
        option.setMaxLines(1);

        /*
        if (!currentFileName.equals(getString(R.string.no_name))) {
            mCurrentAudioUri =
        }
        */

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
                    Toast.makeText(MainActivity.this, "Cannot save blank audio file", Toast.LENGTH_SHORT).show();
                    saveAudioDialogBox();
                } else {
                    // TODO: Add code to save file to database, etc etc.
                    audioName = option.getText().toString();

                    saveAudioToDb();

                    // startTimer();

                    saveView.setClickable(false);
                    saveView.setEnabled(false);
                    saveView.setVisibility(View.INVISIBLE);

                    Intent saveToListIntent = new Intent(MainActivity.this, PreviousRecords.class);
                    startActivity(saveToListIntent);
                }
            }
        });

        builder.setPositiveButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                AlertDialog.Builder builderCancel = new AlertDialog.Builder(MainActivity.this);
                builderCancel.setMessage("Changes unsaved. Continue?");

                builderCancel.setNegativeButton("YES", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (dialogInterface != null) {
                            dialogInterface.dismiss();

                            initMediaPlayer();

                            saveView.setClickable(true);
                            saveView.setEnabled(true);
                            saveView.setVisibility(View.VISIBLE);
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
        String audioXylo = tempXyloSequence.toString();
        String audioPiano = tempPianoSequence.toString();
        String audioCello = tempCelloSequence.toString();
        String audioTempo = tempoString;

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

                // Update the file-name-view to show the current file-name
                currentFileNameView.setText(values.getAsString(AudioContract.AudioEntry.COLUMN_AUDIO_NAME));

                // Set the text-color to WHITE to show that file is saved
                currentFileNameView.setTextColor(Color.WHITE);

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
                currentFileNameView.setText(values.getAsString(AudioContract.AudioEntry.COLUMN_AUDIO_NAME));
            }
        }

        initMediaPlayer();

    }

}
