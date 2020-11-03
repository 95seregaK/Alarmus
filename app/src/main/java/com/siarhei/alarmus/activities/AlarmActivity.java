package com.siarhei.alarmus.activities;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
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
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.siarhei.alarmus.R;
import com.siarhei.alarmus.data.Alarm;
import com.siarhei.alarmus.data.AlarmPreferences;
import com.siarhei.alarmus.data.SunAlarm;
import com.siarhei.alarmus.data.SunAlarmManager;
import com.siarhei.alarmus.views.CircleSlider;

import java.io.IOException;
import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;


public class AlarmActivity extends AppCompatActivity implements CircleSlider.OnSliderMoveListener {

    private TextView time, date, label;
    private CircleSlider sunSlider;
    private MediaPlayer mMediaPlayer;
    private Alarm currentAlarm;
    private AlarmPreferences preferences;
    private SunAlarmManager alarmManager;
    private int timerDelay = 1000 * 60 * 3;
    private static final int s0 = 335;
    private static final int s1 = 8;
    private static final int s2 = 41;
    private static final int s3 = 74;
    private static final int s4 = 107;
    private static final int s5 = 140;
    private static final int s6 = 173;
    private static final int s7 = 206;
    private static final int d1 = 1;
    private static final int d2 = 3;
    private static final int d3 = 5;
    private static final int d4 = 10;
    private static final int d5 = 15;
    private static final int d6 = 20;
    private static final int d7 = 30;
    private Timer timer;


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
        Calendar calendar = Calendar.getInstance();
        time.setText(Alarm.toTime(calendar));
        date.setText(Alarm.toDate(calendar) + ", " + Alarm.toDay(calendar, Alarm.FULL));
        label.setText(currentAlarm.getLabel());
        sunSlider.setOnSliderMoveListener(this);
        timer = new Timer();
        timer.schedule(new MyTimerTask(), timerDelay);
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
        timer.cancel();
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
            if (dir > s7 && dir <= s0) {
                if (currentAlarm instanceof SunAlarm && ((SunAlarm) (currentAlarm)).isUpdate())
                    defineCurrentLocation();
                else {
                    setIfRepeating();
                    preferences.writeAlarm(currentAlarm);
                }

            } else {
                if (dir > s1 && dir <= s2) {
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
            }
            finish();
        } else if (action == CircleSlider.ACTION_FAILURE)
            Log.d("ACTION_FAILURE", "radius=" + sunSlider.getRadius());
    }

    public void defineCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            setAndFinish();
            Toast.makeText(this, "Location cannot be determined", Toast.LENGTH_LONG);
        } else {
            FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
            fusedLocationClient.getLastLocation().addOnCompleteListener(task -> {
                Location location = task.getResult();
                if (location != null) {
                    SunAlarm sunAlarm = (SunAlarm) currentAlarm;
                    sunAlarm.setPosition(location.getLatitude(), location.getLongitude());
                    setAndFinish();
                } else {
                    setAndFinish();
                    Toast.makeText(this, "Location cannot be determined! Please set location manually", Toast.LENGTH_LONG);
                }
            });
        }
        finish();
    }

    public void setIfRepeating() {
        if (currentAlarm.isRepeat()) {
            currentAlarm.setTimeNext(false);
            alarmManager.set(currentAlarm);
        } else {
            currentAlarm.setEnable(false);
        }
    }

    public void setAndFinish() {
        setIfRepeating();
        preferences.writeAlarm(currentAlarm);
        finish();
    }

    class MyTimerTask extends TimerTask {
        @Override
        public void run() {
            runOnUiThread(new Runnable() {
                // Отображаем информацию в текстовом поле count:
                @Override
                public void run() {
                    mMediaPlayer.stop();
                    alarmManager.setDelayed(currentAlarm, d3);
                    timer.cancel();
                    finish();
                }
            });
        }
    }

}
