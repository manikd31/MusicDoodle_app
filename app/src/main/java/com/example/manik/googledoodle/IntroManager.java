package com.example.manik.googledoodle;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import static com.example.manik.googledoodle.R.string.msec_100;

/**
 * Created by Manik on 19-08-2017.
 */

public class IntroManager extends AppCompatActivity{

    private Spinner demoSpinner;

    private CheckBox[] timer_cb = new CheckBox[4];

    private CheckBox[] display_cb = new CheckBox[3];

    private int delay = 500;

    private ViewPager viewPager;

    private int[] layouts;

    private ImageView[] dots;

    Button nextBtn, skipBtn;

    TextView introHead;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro);

        Typeface typeface = Typeface.createFromAsset(getAssets(), "fonts/archistico.ttf");

        introHead = (TextView) findViewById(R.id.intro_heading);

        introHead.setTypeface(typeface);

        viewPager = (ViewPager) findViewById(R.id.intro_view_pager);

        LinearLayout dotsLayout = (LinearLayout) findViewById(R.id.layout_dots);

        layouts = new int[] {
                R.layout.info_screen_1,
                R.layout.info_screen_2,
                R.layout.info_screen_3,
                R.layout.info_screen_4,
                R.layout.info_screen_5,
                R.layout.info_screen_6
        };

        skipBtn = (Button) findViewById(R.id.button_skip);

        skipBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent introDone = new Intent(IntroManager.this, HomePage.class);
                startActivity(introDone);
                finish();
            }
        });

        nextBtn = (Button) findViewById(R.id.button_next);

        nextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int currentPage = getItem(+1);

                if (currentPage < layouts.length) {
                    viewPager.setCurrentItem(currentPage);
                } else {
                    Intent introDone = new Intent(IntroManager.this, HomePage.class);
                    startActivity(introDone);
                    finish();
                }
            }
        });

        ViewPagerAdapter pagerAdapter = new ViewPagerAdapter();

        viewPager.setAdapter(pagerAdapter);

        viewPager.addOnPageChangeListener(pageChangeListener);

        dots = new ImageView[layouts.length];

        for (int i = 0; i < layouts.length; i++) {
            dots[i] = new ImageView(this);

            dots[i].setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.inactive_dot));

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );

            params.setMargins(8, 0, 8, 0);

            dotsLayout.addView(dots[i], params);
        }

        dots[0].setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.active_dot));
    }

    private int getItem(int i) {
        return viewPager.getCurrentItem() + i;
    }

    @Override
    public void onBackPressed() {
        Intent goBack = new Intent(IntroManager.this, HomePage.class);
        startActivity(goBack);
    }

    ViewPager.OnPageChangeListener pageChangeListener = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        }

        @Override
        public void onPageSelected(int position) {
            for (int i = 0; i < layouts.length; i++) {
                dots[i].setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.inactive_dot));
            }

            dots[position].setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.active_dot));

            if (position == layouts.length - 1) {
                nextBtn.setText("DONE");
                skipBtn.setVisibility(View.INVISIBLE);
                skipBtn.setClickable(false);
            } else {
                nextBtn.setText("NEXT");
                skipBtn.setVisibility(View.VISIBLE);
                skipBtn.setClickable(true);
            }

            // Start a timer when page-2 is selected.
            if (position == 1) {
                startDisplayRun();
                startTimerRun();
                setupSpinner();
            } else {
                stopDisplayRun();
                stopTimerRun();
            }
        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }
    };

    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            return false;
        }
    };

    int elapsedTime = 0;

    int elapsedDisplay = 0;

    private Handler handlerTime = new Handler();

    private Handler handlerDisplay = new Handler();

    Runnable displayRunnable = new Runnable() {
        @Override
        public void run() {
            int checkBoxNumber =  elapsedDisplay % 4;
            elapsedDisplay++;

            startDisplayRun();

            if (checkBoxNumber == 0) {
                for (int i = 0; i < 3; i++) {
                    display_cb[i].setChecked(false);
                }
            } else {
                display_cb[checkBoxNumber - 1].setChecked(true);
            }
        }
    };

    Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {
            int checkBoxNumber = elapsedTime % 4;
            elapsedTime++;

            startTimerRun();

            timer_cb[checkBoxNumber].setChecked(true);

            if (checkBoxNumber == 0) {
                timer_cb[3].setChecked(false);
            } else {
                timer_cb[checkBoxNumber - 1].setChecked(false);
            }
        }
    };

    private void startTimerRun() {
        handlerTime.postDelayed(timerRunnable, delay);
    }

    private void startDisplayRun() {
        handlerDisplay.postDelayed(displayRunnable, 500);
    }

    private void stopTimerRun() {
        handlerTime.removeCallbacks(timerRunnable);
    }

    private void stopDisplayRun() {
        handlerDisplay.removeCallbacks(displayRunnable);
    }

    private class ViewPagerAdapter extends PagerAdapter {

        private LayoutInflater layoutInflater;

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            layoutInflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
            View view = layoutInflater.inflate(layouts[position], container, false);
            container.addView(view);

            if (position == 1) {

                timer_cb[0] = (CheckBox) view.findViewById(R.id.demo_timer_1);
                timer_cb[1] = (CheckBox) view.findViewById(R.id.demo_timer_2);
                timer_cb[2] = (CheckBox) view.findViewById(R.id.demo_timer_3);
                timer_cb[3] = (CheckBox) view.findViewById(R.id.demo_timer_4);

                display_cb[0] = (CheckBox) view.findViewById(R.id.demo_cb_1);
                display_cb[1] = (CheckBox) view.findViewById(R.id.demo_cb_2);
                display_cb[2] = (CheckBox) view.findViewById(R.id.demo_cb_3);


                demoSpinner = (Spinner) findViewById(R.id.demo_spinner);

                demoSpinner.setOnTouchListener(mTouchListener);

                setupSpinner();

            }

            return view;
        }

        @Override
        public int getCount() {
            return layouts.length;
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return (view == object);
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            View v = (View) object;
            container.removeView(v);
        }
    }

    private void setupSpinner() {
        ArrayAdapter spinnerAdapter = ArrayAdapter.createFromResource(this,
                R.array.array_tempo_options, android.R.layout.simple_spinner_item);

        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        demoSpinner.setAdapter(spinnerAdapter);

        demoSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selection = (String) parent.getItemAtPosition(position);
                if (!TextUtils.isEmpty(selection)) {
                    if (selection.equals(getString(R.string.msec_500))) {
                        delay = 500;
                    } else if (selection.equals(getString(R.string.msec_750))) {
                        delay = 750;
                    } else if (selection.equals(getString(R.string.msec_250))) {
                        delay = 250;
                    } else if (selection.equals(getString(msec_100))) {
                        delay = 100;
                    } else if (selection.equals(getString(R.string.msec_1000))){
                        delay = 1000;
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                delay = 500;
            }
        });
    }
}
