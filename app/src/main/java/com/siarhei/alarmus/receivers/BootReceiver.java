package com.siarhei.alarmus.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.siarhei.alarmus.data.AlarmData;
import com.siarhei.alarmus.data.AlarmPreferences;

import java.util.List;

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Toast.makeText(context, "BOOT! " + toString(), Toast.LENGTH_LONG).show();
        AlarmPreferences preferences = AlarmPreferences.getInstance(context);
        List<AlarmData> alarms = preferences.readAllAlarms();
        for (AlarmData alarm : alarms) {
            if(alarm.isEnabled())alarm.setAlarm(context);
        }
    }
}
