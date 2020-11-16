package com.siarhei.alarmus.data;

import android.os.Parcel;

import com.siarhei.alarmus.activities.MapActivity;
import com.siarhei.alarmus.sun.SunInfo;

import java.util.Calendar;

public class SunAlarm extends Alarm implements Cloneable{
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
    public static int DElAY_DEFAULT = 0;
    protected int sunMode = MODE_SUNRISE;
    protected double latitude;
    protected double longitude;
    protected int delay;
    protected boolean update = true;
    protected String city = "";

    public SunAlarm(Parcel in) {
        super(in);
        sunMode = in.readInt();
        latitude = in.readDouble();
        longitude = in.readDouble();
        delay = in.readInt();
        update = in.readByte() == 1;
        city = in.readString();
    }

    public SunAlarm(int id) {
        super(id);
        latitude = MapActivity.DEFAULT_LATITUDE;
        longitude = MapActivity.DEFAULT_LONGITUDE;
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
        dest.writeByte((byte) (update ? 1 : 0));
        dest.writeString(city);
    }

    @Override
    public void setToday() {
        time.setTimeInMillis(System.currentTimeMillis());
        defineTime();
    }

    @Override
    public void addDay() {
        time.add(Calendar.DAY_OF_MONTH, 1);
        defineTime();
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
        time.add(Calendar.MINUTE, value - delay);
        delay = value;
    }

    public int getDelay() {
        return delay;
    }

    public String getCity() {
        return city;
    }

    public boolean isUpdate() {
        return update;
    }

    public void setUpdate(boolean update) {
        this.update = update;
    }

    public void setCity(String city) {
        this.city = city;
    }
}
