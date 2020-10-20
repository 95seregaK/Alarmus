package com.siarhei.alarmus.activities;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.siarhei.alarmus.data.AlarmData;
import com.siarhei.alarmus.data.AlarmPreferences;
import com.siarhei.alarmus.R;
import com.siarhei.alarmus.views.SlideButton;
import com.siarhei.alarmus.views.SunSlider;


import java.io.IOException;


public class AlarmActivity extends AppCompatActivity implements SlideButton.SlideButtonListener {

    private TextView time;
    private SunSlider sunSlider;
    private MediaPlayer mMediaPlayer;
    private AlarmData currentAlarm;
    private AlarmPreferences preferences;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm);
        sunSlider = (SunSlider) findViewById(R.id.slideButton);
        time = findViewById(R.id.time);
        mMediaPlayer = new MediaPlayer();
        preferences = AlarmPreferences.getInstance(this);
        currentAlarm = preferences.readAlarm(getIntent().getIntExtra(AlarmData.ID, 0));
        time.setText(currentAlarm.toString());
        sunSlider.setRadius(getResources().getDisplayMetrics().widthPixels / 2 - 100);
        sunSlider.setOnSliderMoveListener((action, direction) -> {
            if (action == SunSlider.ACTION_SUCCESS) {
                if (direction < 180) {
                    if (currentAlarm.isOnce()) currentAlarm.setEnable(false);
                    else {
                        currentAlarm.setNextDay();
                        currentAlarm.setAlarm(this);
                        Toast.makeText(getApplicationContext(), "Будильник установлен!!!", Toast.LENGTH_SHORT).show();
                    }
                    preferences.writeAlarm(currentAlarm);
                } else {
                    currentAlarm.setDelay(this, 1);
                }
                finish();

            } else if (action == SunSlider.ACTION_FAILURE)
                Log.d("ACTION_FAILURE", "radius=" + sunSlider.getRadius());
        });
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

        mMediaPlayer.stop();
        super.finish();
    }

    public void play() throws IOException {

        AudioManager mAudioManager = (AudioManager) this.getSystemService(this.AUDIO_SERVICE);
        mAudioManager.setMode(AudioManager.MODE_NORMAL);
        mAudioManager.setSpeakerphoneOn(false);


        mMediaPlayer.reset();
        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mMediaPlayer.setDataSource(this, RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM));
        mMediaPlayer.setLooping(true);
        mMediaPlayer.prepare();
        mMediaPlayer.start();
    }

    @Override
    public void handleSlide(int res) {
        if (res == -1) {
            AlarmActivity.this.finish();
        } else if (res == 1) {
            AlarmActivity.this.finish();
        }
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }

}
