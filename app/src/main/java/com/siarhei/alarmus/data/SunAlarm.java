package com.siarhei.alarmus.data;

import android.os.Parcel;
import android.util.Log;

import com.siarhei.alarmus.activities.SetLocationActivity;
import com.siarhei.alarmus.sun.SunInfo;

import java.util.Calendar;
import java.util.Date;

public class SunAlarm extends Alarm {
    public static final int MODE_SUNRISE = 1;
    public static final int MODE_NOON = 2;
    public static final int MODE_SUNSET = 3;
    public static final Creator<SunAlarm> CREATOR = new Creator<SunAlarm>() {
        @Override
        public SunAlarm createFromParcel(Parcel in) {
            return new SunAlarm(in);
        }

        @Override
        public SunAlarm[] newArray(int size) {
            return new SunAlarm[size];
        }
    };
    public static int DElAY_DEFAULT = -10;
    protected int sunMode = MODE_SUNRISE;
    protected double latitude;
    protected double longitude;
    private int delay;

    public SunAlarm(Parcel in) {
        super(in);
        sunMode = in.readInt();
        latitude = in.readDouble();
        longitude = in.readDouble();
        delay = in.readInt();
    }

    public SunAlarm(int id) {
        super(id);
        latitude = SetLocationActivity.DEFAULT_LATITUDE;
        longitude = SetLocationActivity.DEFAULT_LONGITUDE;
        delay = DElAY_DEFAULT;
        defineTime();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeInt(sunMode);
        dest.writeDouble(latitude);
        dest.writeDouble(longitude);
        dest.writeInt(delay);
    }

    @Override
    public void setTimeNext() {
        long now = System.currentTimeMillis();
        time.setTimeInMillis(now);
        defineTime();
        while (now > time.getTimeInMillis() || (repeat && !days[(time.get(Calendar.DAY_OF_WEEK) + 5) % 7])) {
            time.add(Calendar.DAY_OF_MONTH, 1);
            defineTime();
        }
        Log.d("AlarmType", "Sun alarm");
    }

    public void defineTime() {
        SunInfo info = new SunInfo(time, latitude, longitude);
        double t = 0;
        switch (sunMode) {
            case MODE_SUNRISE:
                t = info.getSunriseLocalTime();
                break;
            case MODE_NOON:
                t = info.getNoonLocalTime();
                break;
            case MODE_SUNSET:
                t = info.getSunsetLocalTime();
                break;
            default:
                break;
        }
        setTime(SunInfo.getHour(t), SunInfo.getMinute(t));
        time.add(Calendar.MINUTE, delay);
    }

    public int getSunMode() {
        return sunMode;
    }

    public void setSunMode(int b) {
        sunMode = b;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setPosition(double lat, double lon) {
        latitude = lat;
        longitude = lon;
    }

    public void setDelay(int value) {
        delay = value;
    }

    public int getDelay() {
        return delay;
    }
}
