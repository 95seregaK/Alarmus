package com.siarhei.alarmus.data;

import android.os.Parcel;

import com.siarhei.alarmus.sun.SunInfo;

import java.util.Calendar;

public class SunAlarm extends Alarm {
    public static final int MODE_SUNRISE = 1;
    public static final int MODE_SUNSET = 2;
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
    protected int sunMode = 0;
    protected double latitude;
    protected double longitude;

    public SunAlarm(Parcel in) {
        super(in);
        sunMode = in.readInt();
        latitude = in.readDouble();
        longitude = in.readDouble();
        //days = in.createBooleanArray();
    }

    public SunAlarm(int id) {
        super(id);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeInt(sunMode);
        dest.writeDouble(latitude);
        dest.writeDouble(longitude);
        //dest.writeBooleanArray(days);
    }

    @Override
    public void setNextDay() {
        super.setNextDay();
        SunInfo info = new SunInfo(time.get(Calendar.DAY_OF_MONTH),
                time.get(Calendar.MONTH) + 1,
                time.get(Calendar.YEAR), latitude, longitude);
        double t = sunMode == MODE_SUNRISE ?
                info.getSunriseLocalTime() : info.getSunsetLocalTime();
        int hour = (int) t;
        int minute = (int) (60 * (t % 1));
        time.set(Calendar.HOUR_OF_DAY, hour);
        time.set(Calendar.MINUTE, minute);
    }

    public void setTime(int mode) {
        SunInfo info = new SunInfo(time.get(Calendar.DAY_OF_MONTH),
                time.get(Calendar.MONTH), time.get(Calendar.YEAR), latitude, longitude);
        int hour = (int) info.getSunriseLocalTime();
        int minute = (int) (60 * (info.getSunriseLocalTime() % 1));
        time.set(Calendar.HOUR_OF_DAY, hour);
        time.set(Calendar.MINUTE, minute);
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
}
