package com.siarhei.alarmus.data;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Parcel;
import android.os.Parcelable;
import android.provider.CalendarContract;
import android.util.Log;
import android.widget.Toast;

import com.siarhei.alarmus.receivers.AlarmReceiver;

import java.util.Calendar;

public class Alarm implements Parcelable {
    public static final String ID = "id";
    public static String[] DAYS = {"M", "Tu", "W", "Th", "F", "Sa", "Su"};
    private static final int HOUR_DEFAULT = 6;
    private static final int MINUTE_DEFAULT = 0;
    private static final long MAX_DELAY = 10 * 60 * 1000;

    final private int id;
    protected String label;
    protected Calendar time;

    protected boolean enabled = false;
    protected boolean repeat = false;
    protected boolean[] days = {true, true, true, true, true, true, true};

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
        label = in.readString();
        time = Calendar.getInstance();
        time.setTimeInMillis(in.readLong());
        enabled = in.readByte() == 1;
        repeat = in.readByte() == 1;
        days = AlarmPreferences.toBooleanArray(in.readInt());
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(label);
        dest.writeLong(time.getTimeInMillis());
        dest.writeByte((byte) (enabled ? 1 : 0));
        dest.writeByte((byte) (repeat ? 1 : 0));
        dest.writeInt(AlarmPreferences.toInteger(days));

    }

    public Alarm(int id) {
        this.id = id;
        this.time = Calendar.getInstance();
        setTime(HOUR_DEFAULT, MINUTE_DEFAULT);
        setTimeNext(true);
    }


    public Calendar getTime() {
        return time;
    }

    public void setTime(int hourOfDay, int minute) {
        time.set(Calendar.HOUR_OF_DAY, hourOfDay);
        time.set(Calendar.MINUTE, minute);
        time.set(Calendar.SECOND, 0);
    }

    public void addDay() {
        time.add(Calendar.DAY_OF_MONTH, 1);
    }

    public void setTimeNext(boolean fromNow) {
        long now;
        if (fromNow) {
            setToday();
            now = System.currentTimeMillis();
        } else {
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            now = calendar.getTimeInMillis();
        }
        while (now > time.getTimeInMillis() || (repeat && !days[(time.get(Calendar.DAY_OF_WEEK) + 5) % 7])) {
            addDay();
        }
        Log.d("alarmType", "Alarm");
    }

    public void setToday() {
        int hour = getHour();
        int minute = getMinute();
        time.setTimeInMillis(System.currentTimeMillis());
        setTime(hour, minute);
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
        int year = time.get(Calendar.YEAR) % 100;

        return (day < 10 ? "0" : "") + day + "."
                + (month < 10 ? "0" : "") + month + "."
                + (year < 10 ? "0" : "") + year;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setRepeat(boolean checked) {
        repeat = checked;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public boolean isRepeat() {
        return repeat;
    }

    public int getId() {
        return id;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public void setDays(boolean[] days) {
        boolean check = false;
        for (int i = 0; i < 7; i++) {
            check = check || days[i];
        }
        if (check) {
            this.days = days;
            //setTimeNext();
        }
    }

    public boolean[] getDays() {
        return days;
    }

    public boolean isActual() {
        return getTimeInMillis() > System.currentTimeMillis() - MAX_DELAY;
    }
}
