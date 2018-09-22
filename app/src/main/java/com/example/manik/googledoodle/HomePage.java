package com.example.manik.googledoodle;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by Manik on 30-07-2017.
 */

public class HomePage extends AppCompatActivity{

    SoundPool playSound;

    ImageView playIcon, infoIcon;

    private int colors[] = new int[5];

    RelativeLayout myLayout;

    TextView appName;

    Typeface typeface;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);

        appName = (TextView) findViewById(R.id.app_name_view);

        typeface = Typeface.createFromAsset(getAssets(), "fonts/archistico.ttf");

        appName.setTypeface(typeface);

        myLayout = (RelativeLayout) findViewById(R.id.my_home_layout);

        AudioAttributes attr = new AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .build();

        playSound = new SoundPool.Builder()
                .setAudioAttributes(attr)
                .setMaxStreams(5)
                .build();

        final int soundId = playSound.load(this, R.raw.squeaky_sound_play, 1);

        colors[0] = R.color.homeBg4;
        colors[1] = R.color.homeBg5;
        colors[2] = R.color.homeBg6;
        colors[3] = R.color.homeBg7;
        colors[4] = R.color.homeBg8;

        playIcon = (ImageView) findViewById(R.id.home_icon);

        // prevRecs = (ImageView) findViewById(R.id.previous_recordings);

        infoIcon = (ImageView) findViewById(R.id.information);

        final AlphaAnimation buttonClickEffect = new AlphaAnimation(1f, 0.8f);

        playIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                view.startAnimation(buttonClickEffect);
                playSound.play(soundId, 1, 1, 1, 0, 1);
                Intent playIntent = new Intent(HomePage.this, MainActivity.class);
                startActivity(playIntent);
            }
        });

        /*
        prevRecs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent previousRecs = new Intent(HomePage.this, PreviousRecords.class);
                startActivity(previousRecs);
            }
        });
        */

        infoIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*
                Toast.makeText(HomePage.this, getString(R.string.info1),
                        Toast.LENGTH_LONG).show();
                Toast.makeText(HomePage.this, getString(R.string.info2),
                        Toast.LENGTH_LONG).show();
                        */
                Intent introIntent = new Intent(HomePage.this, IntroManager.class);
                startActivity(introIntent);
            }
        });
    }

    /*
    int closeApp = 0;

    int exitTime = 0;

    // Handler object to handle runnable
    android.os.Handler handler = new android.os.Handler();

    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            if (closeApp == 1) {
                exitTime++;
                if (exitTime > 2) {
                    closeApp = 0;
                    exitTime = 0;
                }
            }
        }
    };

    // Used to add the PRESS BACK TO EXIT feature
    public void startExitTimer() {
        handler.postDelayed(runnable, 1000);
    }
    */

    @Override
    protected void onStop() {
        super.onStop();
        // handler.removeCallbacks(runnable);
    }

    @Override
    public void onBackPressed() {
        /*
        startExitTimer();
        closeApp++;
        if (closeApp != 2) {
            Toast.makeText(this, "Press BACK again to exit", Toast.LENGTH_SHORT).show();
        } else if (closeApp == 2) {
            finishAffinity();
        }
        */

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Are you sure you want to exit?");
        builder.setNegativeButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                finishAffinity();
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
}
