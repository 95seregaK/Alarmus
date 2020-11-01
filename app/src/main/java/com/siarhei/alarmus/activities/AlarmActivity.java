package com.siarhei.alarmus.activities;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.siarhei.alarmus.R;
import com.siarhei.alarmus.data.Alarm;
import com.siarhei.alarmus.data.AlarmPreferences;
import com.siarhei.alarmus.data.SunAlarmManager;
import com.siarhei.alarmus.views.CircleSlider;

import java.io.IOException;
import java.util.Timer;


public class AlarmActivity extends AppCompatActivity implements CircleSlider.OnSliderMoveListener {

    private TextView time, date, label;
    private CircleSlider sunSlider;
    private MediaPlayer mMediaPlayer;
    private Alarm currentAlarm;
    private AlarmPreferences preferences;
    private SunAlarmManager alarmManager;
    private static final int s1 = 8;
    private static final int s2 = 41;
    private static final int s3 = 74;
    private static final int s4 = 107;
    private static final int s5 = 140;
    private static final int s6 = 173;
    private static final int s7 = 206;
    private static final int d1 = 1;
    private static final int d2 = 2;
    private static final int d3 = 5;
    private static final int d4 = 10;
    private static final int d5 = 15;
    private static final int d6 = 20;
    private static final int d7 = 30;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        alarmManager = SunAlarmManager.getService(this);
        sunSlider = findViewById(R.id.slideButton);
        time = findViewById(R.id.time);
        date = findViewById(R.id.date);
        label = findViewById(R.id.label);
        mMediaPlayer = new MediaPlayer();
        preferences = AlarmPreferences.getInstance(this);
        currentAlarm = preferences.readAlarm(getIntent().getIntExtra(Alarm.ID, 0));
        time.setText(currentAlarm.toTime());
        date.setText(currentAlarm.toDate());
        label.setText(currentAlarm.getLabel());
        sunSlider.setOnSliderMoveListener(this);
        try {
            play();
        } catch (IOException e) {
            // e.printStackTrace();
            Toast.makeText(getApplicationContext(),
                    "Мелодия не проигрывается!!!", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void finish() {
        //wl.release();
        mMediaPlayer.stop();
        super.finish();
    }

    public void play() throws IOException {

        AudioManager mAudioManager = (AudioManager) this.getSystemService(AUDIO_SERVICE);
        mAudioManager.setMode(AudioManager.MODE_NORMAL);
        mAudioManager.setSpeakerphoneOn(false);


        mMediaPlayer.reset();
        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_ALARM);
        mMediaPlayer.setDataSource(this, RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM));
        mMediaPlayer.setLooping(true);
        mMediaPlayer.prepare();
        mMediaPlayer.start();
    }


    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }

    @Override
    public void onBackPressed() {
        SunAlarmManager.getService(this).setDelayed(currentAlarm, 1);
        super.onBackPressed();

    }

    @Override
    public void onSliderMoved(int action, float dir) {
        if (action == CircleSlider.ACTION_SUCCESS) {
            if (dir > 195 && dir <= 345) {
                if (!currentAlarm.isRepeat()) currentAlarm.setEnable(false);
                else {
                    currentAlarm.setTimeNext();
                    alarmManager.set(currentAlarm);
                    Toast.makeText(getApplicationContext(), "Будильник установлен!!!", Toast.LENGTH_SHORT).show();
                }
                preferences.writeAlarm(currentAlarm);
            } else if (dir > s1 && dir <= s2) {
                alarmManager.setDelayed(currentAlarm, d2);
            } else if (dir > s2 && dir <= s3) {
                alarmManager.setDelayed(currentAlarm, d3);
            } else if (dir > s3 && dir <= s4) {
                alarmManager.setDelayed(currentAlarm, d4);
            } else if (dir > s4 && dir <= s5) {
                alarmManager.setDelayed(currentAlarm, d5);
            } else if (dir > s5 && dir <= s6) {
                alarmManager.setDelayed(currentAlarm, d6);
            } else if (dir > s6 && dir <= s7) {
                alarmManager.setDelayed(currentAlarm, d7);
            } else {
                alarmManager.setDelayed(currentAlarm, d1);
            }
            finish();
            Timer timerTask = new Timer();
        } else if (action == CircleSlider.ACTION_FAILURE)
            Log.d("ACTION_FAILURE", "radius=" + sunSlider.getRadius());
    }
}
