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
import com.siarhei.alarmus.views.CircleSlider;

import java.io.IOException;


public class AlarmActivity extends AppCompatActivity implements CircleSlider.OnSliderMoveListener {

    private TextView time, date, label;
    private CircleSlider sunSlider;
    private MediaPlayer mMediaPlayer;
    private Alarm currentAlarm;
    private AlarmPreferences preferences;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        /*final int flags = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN;
        // This work only for android 4.4+
        getWindow().getDecorView().setSystemUiVisibility(flags);*/
       /* InputMethodManager inputManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        if (inputManager != null)
            inputManager.hideSoftInputFromWindow(getWindow().getDecorView().getApplicationWindowToken(), 0);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);*/
       /* KeyguardManager km = (KeyguardManager) getSystemService(KEYGUARD_SERVICE);
        KeyguardManager.KeyguardLock kl = km.newKeyguardLock("MyApp");
        kl.disableKeyguard();

        PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
        wl = pm.newWakeLock(
                PowerManager.PARTIAL_WAKE_LOCK
                        | PowerManager.ACQUIRE_CAUSES_WAKEUP
                        | PowerManager.ON_AFTER_RELEASE, "Alarmus:tag");
        wl.acquire();*/


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
        currentAlarm.setDelayedAlarm(this, 1);
        super.onBackPressed();

    }

    @Override
    public void onSliderMoved(int action, float direction) {
        if (action == CircleSlider.ACTION_SUCCESS) {
            if (direction < 180) {
                if (!currentAlarm.isRepeat()) currentAlarm.setEnable(false);
                else {
                    currentAlarm.setTimeNext();
                    currentAlarm.setAlarm(this);
                    Toast.makeText(getApplicationContext(), "Будильник установлен!!!", Toast.LENGTH_SHORT).show();
                }
                preferences.writeAlarm(currentAlarm);
            } else {
                currentAlarm.setDelayedAlarm(this, 1);
            }
            finish();

        } else if (action == CircleSlider.ACTION_FAILURE)
            Log.d("ACTION_FAILURE", "radius=" + sunSlider.getRadius());
    }
}
