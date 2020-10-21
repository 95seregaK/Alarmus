package com.siarhei.alarmus.data;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Parcel;
import android.os.Parcelable;
import android.widget.Toast;

import com.siarhei.alarmus.receivers.AlarmReceiver;

import java.util.Calendar;

public abstract class AlarmData implements Parcelable {
    public static final String ID = "id";

    protected int id;
    protected String name;
    protected Calendar time;

    protected boolean enabled = false;
    protected boolean once = true;

    //public boolean[] days = new boolean[7];

    protected AlarmData(Parcel in) {
        id = in.readInt();
        name = in.readString();
        time = Calendar.getInstance();
        time.setTimeInMillis(in.readLong());
        enabled = in.readByte() == 1;
        once = in.readByte() == 1;
        //days = in.createBooleanArray();
    }

    public AlarmData(int id) {
        this.id = id;
        this.time = Calendar.getInstance();
        setTime(time.getTimeInMillis());
        enabled = false;
        once = true;
        // days = new boolean[]{true, true, true, true, true, true, true};
    }

    public Calendar getTime() {
        return time;
    }

    public void setTime(int hourOfDay, int minute) {
        time.setTimeInMillis(System.currentTimeMillis());
        time.set(Calendar.HOUR_OF_DAY, hourOfDay);
        time.set(Calendar.MINUTE, minute);
        while (System.currentTimeMillis() > time.getTimeInMillis()) {
            time.add(Calendar.DAY_OF_MONTH, 1);
        }
    }

    public void setNextDay() {
        time.add(Calendar.DAY_OF_MONTH, 1);
    }

    public int getMinute() {
        return time.get(Calendar.MINUTE);
    }

    public int getHour() {
        return time.get(Calendar.HOUR_OF_DAY);
    }

    public long getTimeInMillis() {
        return time.getTimeInMillis();
    }

    public void setEnable(boolean b) {
        enabled = b;
    }

    public void setTime(long t) {
        time.setTimeInMillis(t);
        // while (System.currentTimeMillis() > time.getTimeInMillis()) {
        //     time.add(Calendar.DAY_OF_YEAR, 1);
        // }
    }

    public String toString() {
        int hour = time.get(Calendar.HOUR_OF_DAY);
        int minute = time.get(Calendar.MINUTE);
        int day = time.get(Calendar.DAY_OF_MONTH);
        int month = time.get(Calendar.MONTH) + 1;
        int year = time.get(Calendar.YEAR);

        return (hour < 10 ? "0" : "") + hour + ":"
                + (minute < 10 ? "0" : "") + minute + ", "
                + (day < 10 ? "0" : "") + day + "."
                + (month < 10 ? "0" : "") + month + "."
                + year
                ;
    }

    public void setAlarm(Context context) {
        Intent intent = new Intent(context, AlarmReceiver.class);
        //Intent intent = new Intent("android.intent.action.ALARM_CALL");
        intent.setAction(String.valueOf(id));
        intent.putExtra(ID, id);
        PendingIntent alarmIntent = PendingIntent.getBroadcast(context, id,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager manager = (AlarmManager) context.getSystemService(context.ALARM_SERVICE);
        manager.set(AlarmManager.RTC_WAKEUP, getTimeInMillis(), alarmIntent);
        Toast.makeText(context, "Будильник установлен на " + toString(), Toast.LENGTH_SHORT).show();
    }

    public void cancelAlarm(Context context) {
        Intent intent = new Intent(context, AlarmReceiver.class);
        //Intent intent = new Intent("android.intent.action.ALARM_CALL");
        intent.setAction(String.valueOf(id));
        PendingIntent alarmIntent = PendingIntent.getBroadcast(context, id,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager manager = (AlarmManager) context.getSystemService(context.ALARM_SERVICE);
        manager.cancel(alarmIntent);
        Toast.makeText(context, "Будильник выключен! " + toString(), Toast.LENGTH_SHORT).show();
    }


    public boolean isEnabled() {
        return enabled;
    }

    public void setOnce(boolean checked) {
        once = checked;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public boolean isOnce() {
        return once;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }



    public void setDelay(Context context, int delay) {
        Intent intent = new Intent(context, AlarmReceiver.class);
        //Intent intent = new Intent("android.intent.action.ALARM_CALL");
        intent.setAction(String.valueOf(id));
        intent.putExtra(ID, id);
        PendingIntent alarmIntent = PendingIntent.getBroadcast(context, id,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager manager = (AlarmManager) context.getSystemService(context.ALARM_SERVICE);
        manager.set(AlarmManager.RTC_WAKEUP,
                System.currentTimeMillis() + delay * 60000, alarmIntent);
        Toast.makeText(context, "Будильник сработает через " + delay + " минут",
                Toast.LENGTH_SHORT).show();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(name);
        dest.writeLong(time.getTimeInMillis());
        dest.writeByte((byte) (enabled ? 1 : 0));
        dest.writeByte((byte) (once ? 1 : 0));
        //dest.writeBooleanArray(days);

    }

    @Override
    public int describeContents() {
        return 0;
    }
}
