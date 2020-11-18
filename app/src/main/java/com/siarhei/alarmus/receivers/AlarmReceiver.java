package com.siarhei.alarmus.receivers;

import android.app.KeyguardManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.admin.DevicePolicyManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.siarhei.alarmus.R;
import com.siarhei.alarmus.activities.AlarmActivity;
import com.siarhei.alarmus.activities.EditAlarmActivity;
import com.siarhei.alarmus.activities.MapActivity;
import com.siarhei.alarmus.data.Alarm;
import com.siarhei.alarmus.data.AlarmPreferences;
import com.siarhei.alarmus.data.SunAlarm;
import com.siarhei.alarmus.data.SunAlarmManager;

public class AlarmReceiver extends BroadcastReceiver {
    public static final String ALARM = "alarm";
    private AlarmPreferences preferences;

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent alarmIntent = new Intent(context, AlarmActivity.class);
        alarmIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        //alarmIntent.putExtra(Alarm.ID, intent.getIntExtra(EditAlarmActivity.ID, 0));
        preferences = AlarmPreferences.getInstance(context);
        Alarm alarm = preferences.readAlarm(intent.getIntExtra(Alarm.ID, 0));
        if (intent.getAction() != SunAlarmManager.SOOZED) {
            if (alarm.isRepeat()) {
                alarm.setTimeNext(false);
                SunAlarmManager.getService(context).set(alarm);
            } else
                alarm.setEnable(false);
            preferences.writeAlarm(alarm);
        } else {
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
            notificationManager.cancel(AlarmActivity.DEFAULT_NOTIFICATION_ID);
        }
        alarmIntent.putExtra(ALARM, alarm);
        context.startActivity(alarmIntent);
    }
}
