package com.siarhei.alarmus.receivers;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.siarhei.alarmus.activities.AddAlarmActivity;
import com.siarhei.alarmus.data.AlarmData;

public class AlarmReceiver extends BroadcastReceiver {
    private NotificationManager nm;

    @Override
    public void onReceive(Context context, Intent intent) {
       /*
        nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
        builder.setColor(ContextCompat.getColor(context, R.color.colorAccent));
        builder.setContentTitle(context.getString(R.string.app_name));
        builder.setContentText("Hello!");
        builder.setTicker("Hello!");
        builder.setVibrate(new long[]{1000, 500, 1000, 500, 1000, 500});
        builder.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM));
        builder.setContentIntent(launchAlarmLandingPage(context));
        builder.setAutoCancel(true);
        builder.setPriority(Notification.PRIORITY_HIGH);
        Notification notification = builder.build();
        nm.notify(1, notification);
        */
        //AlarmData alarm = (AlarmData) intent.getParcelableExtra("alarm");
        /*Intent alarmIntent = new Intent(context, AlarmActivity.class);
        alarmIntent.setAction(intent.getAction());*/

        Intent alarmIntent = new Intent("android.intent.action.ALARM");
        alarmIntent.putExtra(AlarmData.ID, intent.getIntExtra(AddAlarmActivity.ID, 0));
        context.startActivity(alarmIntent);

    }

}