package com.siarhei.alarmus.data;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Parcelable;
import android.util.Log;

import com.siarhei.alarmus.receivers.AlarmReceiver;

import static com.siarhei.alarmus.data.Alarm.ID;

public class SunAlarmManager {
    public static final String SOOZED = "snoozed";
    private final AlarmManager alarmManager;
    private final Context context;

    protected SunAlarmManager(Context context) {
        alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        this.context = context;
    }

    public static SunAlarmManager getService(Context context) {
        return new SunAlarmManager(context);
    }

    private PendingIntent prepareIntent(Alarm alarm, boolean snoozed) {
        Intent intent = new Intent(context, AlarmReceiver.class);
        if (snoozed) intent.setAction(SOOZED);
        intent.putExtra(ID, alarm.getId());
        PendingIntent alarmIntent = PendingIntent.getBroadcast(context, alarm.getId(),
                intent, PendingIntent.FLAG_UPDATE_CURRENT);
        return alarmIntent;
    }

    public void set(Alarm alarm) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,
                    alarm.getTimeInMillis(), prepareIntent(alarm, false));
            Log.d("alarmmanager", "setAndAllowWhileIdle");
        } else alarmManager.set(AlarmManager.RTC_WAKEUP, alarm.getTimeInMillis(),
                prepareIntent(alarm, false));
    }

    public void setDelayed(Alarm alarm, int delay) {
        long time = System.currentTimeMillis() + delay * 60000;
        alarmManager.set(AlarmManager.RTC_WAKEUP, time, prepareIntent(alarm, true));
    }

    public void cancel(Alarm alarm) {
        alarmManager.cancel(prepareIntent(alarm, false));
    }

    public void cancelSnoozed(Alarm alarm) {
        alarmManager.cancel(prepareIntent(alarm, true));
    }
}
