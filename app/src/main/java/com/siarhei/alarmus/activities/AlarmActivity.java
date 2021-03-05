package com.siarhei.alarmus.activities;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.RemoteViews;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

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
    private static final int FINISH_DELAY = 500;
    private static final int CODE_SUCCESS = 1;
    private TextView time, date, label;
    private CircleSlider sunSlider;
    private MediaPlayer mMediaPlayer;
    private Alarm alarm;
    private int timerDelay = 1000 * 60 * 3;
    private Timer timer;
    private SunAlarmManager alarmManager;
    private AlarmPreferences preferences;
    private int finishCode = 0;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm);
        unblockScreen();
        alarmManager = SunAlarmManager.getService(this);
        sunSlider = findViewById(R.id.slideButton);
        time = findViewById(R.id.time);
        date = findViewById(R.id.date);
        label = findViewById(R.id.label);
        mMediaPlayer = new MediaPlayer();
        //alarm = getIntent().getParcelableExtra(AlarmReceiver.ALARM);
        preferences = AlarmPreferences.getInstance(this);
        alarm = preferences.readAlarm(getIntent().getIntExtra(Alarm.ID, 0));
        Calendar calendar = Calendar.getInstance();
        time.setText(Alarm.toTime(calendar));
        date.setText(Alarm.toDate(calendar) + ", " + Alarm.toDay(calendar, Alarm.FULL));
        label.setText(makeLabel());
        if (!getIntent().getBooleanExtra(SunAlarmManager.SOOZED, false)) setNextAlarm();
        else NotificationManagerCompat.from(this).cancel(AlarmActivity.DEFAULT_NOTIFICATION_ID);
        sunSlider.setOnSliderMoveListener(this);
        timer = new Timer();
        timer.schedule(new MyTimerTask(), timerDelay);
        /*if (alarm instanceof SunAlarm && ((SunAlarm) alarm).isUpdate())
            setNewLocation();*/
        try {
            play();
        } catch (IOException e) {
            // e.printStackTrace();
        }
    }

    private void unblockScreen() {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
    }

    private String makeLabel() {
        String text = alarm.getLabel();
        if (alarm instanceof SunAlarm) {
            if (text != "" && text != null) text += '\n';
            SunAlarm sunAlarm = (SunAlarm) alarm;
            int delay = (int) (System.currentTimeMillis() - sunAlarm.getTimeInMillis()) / 60000 + sunAlarm.getDelay();
            if (delay == 0)
                text += "It is ";
            else if (delay > 0)
                text += delay + " " + getResources().getString(R.string.after) + " ";
            else if (delay < 0)
                text += (-delay) + " " + getResources().getString(R.string.before) + " ";
            if (sunAlarm.getSunMode() == SunAlarm.MODE_SUNRISE)
                text += getResources().getString(R.string.sunrise);
            else if (sunAlarm.getSunMode() == SunAlarm.MODE_NOON)
                text += getResources().getString(R.string.noon);
            else if (sunAlarm.getSunMode() == SunAlarm.MODE_SUNSET)
                text += getResources().getString(R.string.sunset);
        }
        return text;
    }

    @Override
    public void finish() {
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(AlarmActivity.super::finish);
            }
        }, FINISH_DELAY);
    }

    private void setNewLocation() {
        MapActivity.defineCurrentLocation(this, (code, location) -> {
            if (code == MapActivity.CODE_SUCCESS) {
                SunAlarm sunAlarm = (SunAlarm) alarm;
                sunAlarm.setPosition(location.getLatitude(), location.getLongitude());
                Log.d("defineCurrentLocation", "yes!!!");
                sunAlarm.defineTime();
                sunAlarm.setCity(MapActivity.defineCityName(getBaseContext(),
                        location.getLatitude(), location.getLongitude()));
                SunAlarmManager.getService(this).set(sunAlarm);
                AlarmPreferences.getInstance(this).writeAlarm(sunAlarm);
            }
        });
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

  /*  @Override
    public void onBackPressed() {
        snooze(1);
        super.onBackPressed();
    }
*/

    @Override
    public void onStop() {
        if (finishCode != CODE_SUCCESS)  snooze(1);
        timer.cancel();
        mMediaPlayer.release();
        super.onStop();
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
            }
            finish();
        } else if (action == CircleSlider.ACTION_FAILURE) ;
    }

    private void dismiss() {
        finishCode = CODE_SUCCESS;
        if (alarm instanceof SunAlarm && ((SunAlarm) (alarm)).isUpdate())
            MapActivity.defineCurrentLocation(this, (code, location) -> {
                if (code == MapActivity.CODE_SUCCESS)
                    ((SunAlarm) (alarm)).setPosition(location.getLatitude(), location.getLongitude());
                else
                    Toast.makeText(this, R.string.message_location_cannot, Toast.LENGTH_SHORT).show();
                finish();
            });
        else {
            finish();
        }
    }

    private void snooze(int d) {
        finishCode = CODE_SUCCESS;
        alarmManager.setSnoozed(alarm, d);
        makeNotification(d);
        Toast.makeText(this, getResources().getString(R.string.message_delayed)
                + " " + d + " " + getResources().getString(R.string.minutes), Toast.LENGTH_SHORT).show();
    }

    public void makeNotification(int d) {
        Intent intent = new Intent(this, SnoozeReceiver.class);
        intent.putExtra(AlarmReceiver.ALARM, alarm);
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
                        .setSmallIcon(R.drawable.ic_sun)
                        .setContentTitle(getResources().getString(R.string.message_snoozed_to) + " " + Alarm.toTime(calendar))
                        .setContentText(getResources().getString(R.string.tap_to_cancel))
                        //.addAction(R.drawable.ic_alarm, "Dismiss", pendingIntent)
                        .setContentIntent(pendingIntent)
                        //.setTimeoutAfter(d * 60 * 1000)
                        .setPriority(NotificationCompat.PRIORITY_HIGH);
        notificationManager.notify(DEFAULT_NOTIFICATION_ID, builder.build());
    }

    public void setNextAlarm() {
        if (alarm.isRepeat()) {
            alarm.setTimeNext(false);
            SunAlarmManager.getService(this).set(alarm);
            Log.d("setNextAlarm", "setNextAlarm AlarmActivity");

        } else
            alarm.setEnable(false);
        preferences.writeAlarm(alarm);

    }

    class MyTimerTask extends TimerTask {
        @Override
        public void run() {
            runOnUiThread(() -> {
                mMediaPlayer.release();
                snooze(d3);
                timer.cancel();
                finish();
            });
        }
    }

}
