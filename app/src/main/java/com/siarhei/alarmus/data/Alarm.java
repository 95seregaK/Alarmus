package com.siarhei.alarmus.data;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
import android.widget.Toast;

import com.siarhei.alarmus.receivers.AlarmReceiver;

import java.util.Calendar;

public class Alarm implements Parcelable {
    public static final String ID = "id";

    protected int id;
    protected String name;
    protected Calendar time;

    protected boolean enabled = false;
    protected boolean repeat = false;
    protected boolean[] days = {true, true, true, true, true, false, false};

    public static final Creator<Alarm> CREATOR = new Creator<Alarm>() {
        @Override
        public Alarm createFromParcel(Parcel in) {
            return new Alarm(in);
        }

        @Override
        public Alarm[] newArray(int size) {
            return new Alarm[size];
        }
    };

    public Alarm(Parcel in) {
        id = in.readInt();
        name = in.readString();
        time = Calendar.getInstance();
        time.setTimeInMillis(in.readLong());
        enabled = in.readByte() == 1;
        repeat = in.readByte() == 1;
        days = AlarmPreferences.toBooleanArray(in.readInt());
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(name);
        dest.writeLong(time.getTimeInMillis());
        dest.writeByte((byte) (enabled ? 1 : 0));
        dest.writeByte((byte) (repeat ? 1 : 0));
        dest.writeInt(AlarmPreferences.toInteger(days));

    }

    public Alarm(int id) {
        this.id = id;
        this.time = Calendar.getInstance();
        setTime(time.getTimeInMillis());
    }


    public Calendar getTime() {
        return time;
    }

    public void setTime(int hourOfDay, int minute) {
        time.setTimeInMillis(System.currentTimeMillis());
        time.set(Calendar.HOUR_OF_DAY, hourOfDay);
        time.set(Calendar.MINUTE, minute);
    }

    public void setTimeNext() {
        long now = System.currentTimeMillis();
        while (now > time.getTimeInMillis() || (repeat && !days[(time.get(Calendar.DAY_OF_WEEK) + 5) % 7])) {
            time.add(Calendar.DAY_OF_MONTH, 1);
        }
        Log.d("alarmType", "Alarm");
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
        return toTime() + ", " + toDate();
    }

    public String toTime() {
        int hour = time.get(Calendar.HOUR_OF_DAY);
        int minute = time.get(Calendar.MINUTE);

        return (hour < 10 ? "0" : "") + hour + ":"
                + (minute < 10 ? "0" : "") + minute;
    }

    public String toDate() {

        int day = time.get(Calendar.DAY_OF_MONTH);
        int month = time.get(Calendar.MONTH) + 1;
        int year = time.get(Calendar.YEAR);

        return (day < 10 ? "0" : "") + day + "."
                + (month < 10 ? "0" : "") + month + "."
                + year;
    }

    private PendingIntent prepareIntent(Context context) {
        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.setAction(String.valueOf(id));
        intent.putExtra(ID, id);
        PendingIntent alarmIntent = PendingIntent.getBroadcast(context, id,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);
        return alarmIntent;
    }

    public void setAlarm(Context context) {
        setTimeNext();
        AlarmManager manager = (AlarmManager) context.getSystemService(context.ALARM_SERVICE);
        manager.set(AlarmManager.RTC_WAKEUP, getTimeInMillis(), prepareIntent(context));
        Toast.makeText(context, "Будильник установлен на " + toString(), Toast.LENGTH_SHORT).show();
    }

    public void setDelayedAlarm(Context context, int delay) {
        AlarmManager manager = (AlarmManager) context.getSystemService(context.ALARM_SERVICE);
        long t = System.currentTimeMillis() + delay * 60000;
        manager.set(AlarmManager.RTC_WAKEUP, t, prepareIntent(context));
        Toast.makeText(context, "Будильник сработает через " + delay + " минут",
                Toast.LENGTH_SHORT).show();
    }

    public void cancelAlarm(Context context) {
        AlarmManager manager = (AlarmManager) context.getSystemService(context.ALARM_SERVICE);
        manager.cancel(prepareIntent(context));
        Toast.makeText(context, "Будильник выключен! " + toString(), Toast.LENGTH_SHORT).show();
    }


    public boolean isEnabled() {
        return enabled;
    }

    public void setRepeat(boolean checked) {
        repeat = checked;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public boolean isRepeat() {
        return repeat;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    public void setDays(boolean[] days) {
        this.days = days;
    }

    public boolean[] getDays() {
        return days;
    }
}
