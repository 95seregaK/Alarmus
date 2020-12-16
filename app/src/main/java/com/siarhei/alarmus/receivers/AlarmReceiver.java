package com.siarhei.alarmus.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.core.app.NotificationManagerCompat;

import com.siarhei.alarmus.activities.AlarmActivity;
import com.siarhei.alarmus.data.Alarm;
import com.siarhei.alarmus.data.AlarmPreferences;
import com.siarhei.alarmus.data.SunAlarmManager;

public class AlarmReceiver extends BroadcastReceiver {
    public static final String ALARM = "alarm";
    private AlarmPreferences preferences;

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent alarmIntent = new Intent(context, AlarmActivity.class);
        alarmIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        int id = intent.getIntExtra(Alarm.ID, 0);
        boolean snoozed = intent.getAction() == SunAlarmManager.SOOZED;
         //setNextAlarm(context, intent);
        alarmIntent.putExtra(Alarm.ID, id);
        alarmIntent.putExtra(SunAlarmManager.SOOZED, snoozed);
        context.startActivity(alarmIntent);
    }

    public void setNextAlarm(Context context, Intent intent) {
        preferences = AlarmPreferences.getInstance(context);
        Alarm alarm = preferences.readAlarm(intent.getIntExtra(Alarm.ID, 0));
        if (intent.getAction() != SunAlarmManager.SOOZED) {
            if (alarm.isRepeat()) {
                alarm.setTimeNext(false);
                SunAlarmManager.getService(context).set(alarm);
                Log.d("setNextAlarm","setNextAlarm BroadcastReceiver");
            } else
                alarm.setEnable(false);
            preferences.writeAlarm(alarm);
        } else {
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
            notificationManager.cancel(AlarmActivity.DEFAULT_NOTIFICATION_ID);
        }
    }
}
