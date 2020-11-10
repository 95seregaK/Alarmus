package com.siarhei.alarmus.receivers;

import android.app.KeyguardManager;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import com.siarhei.alarmus.activities.AlarmActivity;
import com.siarhei.alarmus.activities.EditAlarmActivity;
import com.siarhei.alarmus.data.Alarm;

public class AlarmReceiver extends BroadcastReceiver {
    private NotificationManager nm;

    @Override
    public void onReceive(Context context, Intent intent) {
        //AlarmData alarm = (AlarmData) intent.getParcelableExtra("alarm");
        /*Intent alarmIntent = new Intent(context, AlarmActivity.class);
        alarmIntent.setAction(intent.getAction());*/
        //Log.d("receiver", "receive");
        Intent alarmIntent = new Intent(context, AlarmActivity.class);
        alarmIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        alarmIntent.putExtra(Alarm.ID, intent.getIntExtra(EditAlarmActivity.ID, 0));
        context.startActivity(alarmIntent);

    }

}
