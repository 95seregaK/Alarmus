package com.siarhei.alarmus.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.siarhei.alarmus.data.Alarm;
import com.siarhei.alarmus.data.AlarmPreferences;
import com.siarhei.alarmus.data.SunAlarmManager;

import java.util.List;

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Toast.makeText(context, "BOOT! " + toString(), Toast.LENGTH_LONG).show();
        AlarmPreferences preferences = AlarmPreferences.getInstance(context);
        List<Alarm> alarms = preferences.readAllAlarms();
        for (Alarm alarm : alarms) {
            if (alarm.isEnabled() && alarm.isActual())
                SunAlarmManager.getService(context).set(alarm);
        }
    }
}
