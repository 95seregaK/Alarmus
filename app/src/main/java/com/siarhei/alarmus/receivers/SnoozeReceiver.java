package com.siarhei.alarmus.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.NotificationManagerCompat;

import com.siarhei.alarmus.activities.AlarmActivity;
import com.siarhei.alarmus.data.SunAlarmManager;

public class SnoozeReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        SunAlarmManager.getService(context).cancelSnoozed(intent.getParcelableExtra(AlarmReceiver.ALARM));
        //NotificationManagerCompat.from(context).cancel(AlarmActivity.DEFAULT_NOTIFICATION_ID);
    }
    //Log.d("SnoozeReceiver","alarm canceled");
}
