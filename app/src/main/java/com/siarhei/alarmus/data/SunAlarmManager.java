package com.siarhei.alarmus.data;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.siarhei.alarmus.receivers.AlarmReceiver;

import static com.siarhei.alarmus.data.Alarm.ID;

public class SunAlarmManager {
    private AlarmManager alarmManager;
    private Context context;

    protected SunAlarmManager(Context context) {
        alarmManager = (AlarmManager) context.getSystemService(context.ALARM_SERVICE);
        this.context = context;
    }

    public static SunAlarmManager getService(Context context) {
        return new SunAlarmManager(context);
    }

    private PendingIntent prepareIntent(Alarm alarm) {
        Intent intent = new Intent(context, AlarmReceiver.class);
        //intent.setAction(String.valueOf(id));
        intent.putExtra(ID, alarm.getId());
        PendingIntent alarmIntent = PendingIntent.getBroadcast(context, alarm.getId(),
                intent, PendingIntent.FLAG_UPDATE_CURRENT);
        return alarmIntent;
    }

    public void set(Alarm alarm) {
        alarmManager.set(AlarmManager.RTC_WAKEUP, alarm.getTimeInMillis(), prepareIntent(alarm));
        Toast.makeText(context, "Будильник установлен на " + alarm.toString(), Toast.LENGTH_SHORT).show();
    }

    public void setDelayed(Alarm alarm, int delay) {
        long time = System.currentTimeMillis() + delay * 60000;
        alarmManager.set(AlarmManager.RTC_WAKEUP, time, prepareIntent(alarm));
        Toast.makeText(context, "Будильник сработает через " + delay + " минут",
                Toast.LENGTH_SHORT).show();
    }

    public void cancel(Alarm alarm) {
        alarmManager.cancel(prepareIntent(alarm));
        Toast.makeText(context, "Будильник выключен! " + alarm.toString(), Toast.LENGTH_SHORT).show();
    }
}
