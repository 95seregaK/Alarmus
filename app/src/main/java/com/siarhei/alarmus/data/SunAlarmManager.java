package com.siarhei.alarmus.data;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.siarhei.alarmus.receivers.AlarmReceiver;

import static com.siarhei.alarmus.data.Alarm.ID;

public class SunAlarmManager {
    private final AlarmManager alarmManager;
    private final Context context;

    protected SunAlarmManager(Context context) {
        alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        this.context = context;
    }

    public static SunAlarmManager getService(Context context) {
        return new SunAlarmManager(context);
    }

    private PendingIntent prepareIntent(Alarm alarm) {
        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.putExtra(ID, alarm.getId());
        PendingIntent alarmIntent = PendingIntent.getBroadcast(context, alarm.getId(),
                intent, PendingIntent.FLAG_UPDATE_CURRENT);
        return alarmIntent;
    }

    public void set(Alarm alarm) {
        alarmManager.set(AlarmManager.RTC_WAKEUP, alarm.getTimeInMillis(), prepareIntent(alarm));
    }

    public void setDelayed(Alarm alarm, int delay) {
        long time = System.currentTimeMillis() + delay * 60000;
        alarmManager.set(AlarmManager.RTC_WAKEUP, time, prepareIntent(alarm));
    }

    public void cancel(Alarm alarm) {
        alarmManager.cancel(prepareIntent(alarm));
    }
}
