package com.siarhei.alarmus.data;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Calendar;

public class Alarm implements Parcelable {
    public static final String ID = "id";
    public static final int FULL = 1;
    public static final int SHORT = 2;
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
    private static final int HOUR_DEFAULT = 6;
    private static final int MINUTE_DEFAULT = 0;
    private static final long MAX_DELAY = 10 * 60 * 1000;
    // public static String[] DAYS = {"Mo", "Tu", "We", "Th", "Fr", "Sa", "Su"};
    public static String[] DAYS_SHORT = {"M", "T", "W", "T", "F", "S", "S"};
    // public static String[] DAYS_SHORT = {"m", "t", "w", "t", "f", "s", "s"};
    public static String[] DAYS_FULL = {"monday", "tuesday", "wednesday", "thursday", "friday", "saturday", "sunday"};
    final private int id;
    protected String label;
    protected Calendar time;
    protected boolean enabled = false;
    protected boolean repeat = true;
    protected boolean[] days = {true, true, true, true, true, true, true};

    public Alarm(Parcel in) {
        id = in.readInt();
        label = in.readString();
        time = Calendar.getInstance();
        time.setTimeInMillis(in.readLong());
        enabled = in.readByte() == 1;
        repeat = in.readByte() == 1;
        days = AlarmPreferences.toBooleanArray(in.readInt());
    }

    public Alarm(int id) {
        this.id = id;
        this.time = Calendar.getInstance();
        setTime(HOUR_DEFAULT, MINUTE_DEFAULT);
        setTimeNext(true);
    }

    public static String toDay(Calendar calendar, int mode) {
        if (mode == FULL)
            return DAYS_FULL[(calendar.get(Calendar.DAY_OF_WEEK) + 5) % 7];
        else return DAYS_SHORT[(calendar.get(Calendar.DAY_OF_WEEK) + 5) % 7];
    }

    public static String toTime(Calendar calendar) {
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        return (hour < 10 ? "0" : "") + hour + ":"
                + (minute < 10 ? "0" : "") + minute;
    }

    public static String toDate(Calendar calendar) {
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int month = calendar.get(Calendar.MONTH) + 1;
        int year = calendar.get(Calendar.YEAR) % 100;

        return (day < 10 ? "0" : "") + day + "."
                + (month < 10 ? "0" : "") + month + "."
                + (year < 10 ? "0" : "") + year;
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

    public Calendar getTime() {
        return time;
    }

    public void setTime(long t) {
        time.setTimeInMillis(t);
        // while (System.currentTimeMillis() > time.getTimeInMillis()) {
        //     time.add(Calendar.DAY_OF_YEAR, 1);
        // }
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
            calendar.add(Calendar.DAY_OF_MONTH, 1);
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            now = calendar.getTimeInMillis();
        }
        while (now > time.getTimeInMillis() || (repeat && !days[(time.get(Calendar.DAY_OF_WEEK) + 5) % 7])) {
            addDay();
        }
        //Log.d("alarmType", "Alarm");
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

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public boolean isRepeat() {
        return repeat;
    }

    public void setRepeat(boolean checked) {
        repeat = checked;
    }

    public int getId() {
        return id;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public boolean[] getDays() {
        return days;
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

    public boolean isActual() {
        return getTimeInMillis() > System.currentTimeMillis() - MAX_DELAY;
    }
}
