package com.siarhei.alarmus.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.siarhei.alarmus.data.Alarm;
import com.siarhei.alarmus.data.AlarmPreferences;
import com.siarhei.alarmus.data.SunAlarmManager;

import java.util.List;

public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        //Toast.makeText(context, "BOOT! " + toString(), Toast.LENGTH_LONG).show();
        //Log.d("BootReceiver", "BootReceiver");
        AlarmPreferences preferences = AlarmPreferences.getInstance(context);
        List<Alarm> alarms = preferences.readAllAlarms();
        for (Alarm alarm : alarms) {
            if (alarm.isEnabled()) {
                if (alarm.isActual())
                    SunAlarmManager.getService(context).set(alarm);
                else if (alarm.isRepeat()) {
                    alarm.setTimeNext(true);
                    SunAlarmManager.getService(context).set(alarm);
                } else {
                    alarm.setEnable(false);
                }
                preferences.writeAlarm(alarm);
            }
        }
    }
}
