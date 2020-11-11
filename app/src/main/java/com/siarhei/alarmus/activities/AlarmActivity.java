package com.siarhei.alarmus.activities;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.admin.DevicePolicyManager;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.os.Build;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.RemoteViews;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import com.siarhei.alarmus.R;
import com.siarhei.alarmus.data.Alarm;
import com.siarhei.alarmus.data.AlarmPreferences;
import com.siarhei.alarmus.data.SunAlarm;
import com.siarhei.alarmus.data.SunAlarmManager;
import com.siarhei.alarmus.receivers.AlarmReceiver;
import com.siarhei.alarmus.receivers.SnoozeReceiver;
import com.siarhei.alarmus.views.CircleSlider;

import java.io.IOException;
import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;


public class AlarmActivity extends AppCompatActivity implements CircleSlider.OnSliderMoveListener {
    public static final int DEFAULT_NOTIFICATION_ID = 101;

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
    private TextView time, date, label;
    private CircleSlider sunSlider;
    private MediaPlayer mMediaPlayer;
    private Alarm currentAlarm;
    private int timerDelay = 1000 * 60 * 3;
    private Timer timer;
    private SunAlarmManager alarmManager;


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
        currentAlarm = getIntent().getParcelableExtra(AlarmReceiver.ALARM);
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

        }
    }

    @Override
    public void finish() {
        timer.cancel();
        mMediaPlayer.release();
        //mMediaPlayer.stop();
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
        snooze(1);
        super.onBackPressed();

    }

    @Override
    public void onSliderMoved(int action, float dir) {
        if (action == CircleSlider.ACTION_SUCCESS) {
            if (dir > s7 && dir <= s0) {
                dismiss();
            } else {
                int d;
                if (dir > s1 && dir <= s2) d = d2;
                else if (dir > s2 && dir <= s3) d = d3;
                else if (dir > s3 && dir <= s4) d = d4;
                else if (dir > s4 && dir <= s5) d = d5;
                else if (dir > s5 && dir <= s6) d = d6;
                else if (dir > s6 && dir <= s7) d = d7;
                else d = d1;
                snooze(d);
                finish();
            }
        } else if (action == CircleSlider.ACTION_FAILURE) ;
    }

    private void dismiss() {
        if (currentAlarm instanceof SunAlarm && ((SunAlarm) (currentAlarm)).isUpdate())
            MapActivity.defineCurrentLocation(this, (code, location) -> {
                if (code == MapActivity.CODE_SUCCESS)
                    ((SunAlarm) (currentAlarm)).setPosition(location.getLatitude(), location.getLongitude());
                else
                    Toast.makeText(this, R.string.message_location_cannot, Toast.LENGTH_SHORT).show();
                //setIfRepeating();
                // preferences.writeAlarm(currentAlarm);
                finish();
            });
        else {
            //setIfRepeating();
            // preferences.writeAlarm(currentAlarm);
            finish();
        }
    }

    private void snooze(int d) {
        alarmManager.setDelayed(currentAlarm, d);
        makeNotification(d);
        Toast.makeText(this, getResources().getString(R.string.message_delayed)
                + " " + d + " " + getResources().getString(R.string.minutes), Toast.LENGTH_SHORT).show();
    }

 /*   public void setIfRepeating() {
        if (!currentAlarm.isRepeat()) {
            currentAlarm.setEnable(false);
        }
    }*/

    public void setAndFinish() {
        //setIfRepeating();
        //preferences.writeAlarm(currentAlarm);
        finish();
    }

    class MyTimerTask extends TimerTask {
        @Override
        public void run() {
            runOnUiThread(() -> {
                mMediaPlayer.stop();
                alarmManager.setDelayed(currentAlarm, d3);
                timer.cancel();
                finish();
            });
        }
    }

    public void makeNotification(int d) {
        Intent intent = new Intent(this, SnoozeReceiver.class);
        intent.putExtra(AlarmReceiver.ALARM, currentAlarm);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 1001,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        if (Build.VERSION.SDK_INT >= 26) {
            NotificationChannel channel = new NotificationChannel("default",
                    "Channel Alarm",
                    NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription("Channel description");
            notificationManager.createNotificationChannel(channel);
        }
        RemoteViews notificationView = new RemoteViews(getPackageName(), R.layout.layout_notification);
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MINUTE, d);
        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(this, "default")
                        .setSmallIcon(R.drawable.ic_sun2)
                        .setContentTitle(getResources().getString(R.string.message_snoozed_to) +" "+ Alarm.toTime(calendar))
                        .setContentText(getResources().getString(R.string.tap_to_cancel))
                        //.addAction(R.drawable.ic_alarm, "Dismiss", pendingIntent)
                        .setContentIntent(pendingIntent)
                        //.setTimeoutAfter(d * 60 * 1000)
                        .setPriority(NotificationCompat.PRIORITY_HIGH);
        notificationManager.notify(DEFAULT_NOTIFICATION_ID, builder.build());
    }

}
