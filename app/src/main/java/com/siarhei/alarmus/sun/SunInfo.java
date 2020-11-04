package com.siarhei.alarmus.sun;

import com.moodysalem.TimezoneMapper;

import java.util.Calendar;
import java.util.TimeZone;

public class SunInfo {

    public static final int HH_MM = 1;
    public static final int HH_MM_SS = 2;
    public static final int HH_MM_SS_MM = 3;
    public static final int SUNRISE_MODE = 1;
    public static final int NOON_MODE = 2;
    public static final int SUNSET_MODE = 3;

    private double meanSolarNoon;
    private double solarTransit;
    private double julianDate;
    private int year;
    private int month;
    private int day;
    private double longitude;
    private double latitude;
    private double sunsetJulianTime;
    private double sunriseJulianTime;
    private double sunriseLocalTime;
    private double sunsetLocalTime;
    private double noonLocalTime;
    private double dayDuration;
    private float timeZoneOffset;
    private String timeZoneName;


    public SunInfo(int day, int month, int year, double latitude, double longitude) {
        this.day = day;
        this.month = month;
        this.year = year;
        this.latitude = latitude;
        this.longitude = longitude;
        init();
    }

    public SunInfo(Calendar calendar, double lat, double lon) {
        this.day = calendar.get(Calendar.DAY_OF_MONTH);
        this.month = calendar.get(Calendar.MONTH) + 1;
        this.year = calendar.get(Calendar.YEAR);
        this.latitude = lat;
        this.longitude = lon;
        init();
    }

    public static SunInfo nextDaySunInfo(SunInfo info, int add) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(info.year, info.month - 1, info.day);
        calendar.add(Calendar.DAY_OF_MONTH, add);
        SunInfo infoNext = new SunInfo(calendar, info.latitude, info.longitude);
        return infoNext;
    }

    public static boolean afterNow(SunInfo info, int mode) {
        Calendar calendar = Calendar.getInstance();
        long currentTime = System.currentTimeMillis();
        double time;
        if (mode == SUNRISE_MODE) {
            time = info.getSunriseLocalTime();
        } else if (mode == NOON_MODE) {
            time = info.getNoonLocalTime();
        } else {
            time = info.getSunsetLocalTime();
        }
        int hour = (int) time;
        int minute = (int) (60 * (time - hour));
        calendar.set(Calendar.YEAR, info.year);
        calendar.set(Calendar.MONTH, info.month - 1);
        calendar.set(Calendar.DAY_OF_MONTH, info.day);
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        return calendar.getTimeInMillis() > currentTime;
    }

    public static double toLocalTime(double julianTime, float offset) {
        double time = (julianTime + 0.5 + offset / 24.0) % 1;
        return time >= 0 ? 24 * time : 24 * time + 24;
    }

    public String toString(int timeFormat) {
        String sunriseStr = "Sunrise: " + toTimeString(sunriseLocalTime, timeFormat) + '\n';
        String sunsetStr = "Sunset: " + toTimeString(sunsetLocalTime, timeFormat) + '\n';
        String noonStr = "Solar noon: " + toTimeString(noonLocalTime, timeFormat) + '\n';
        String durationStr = "Day duration: " + toTimeString(dayDuration, timeFormat) + '\n';
        return sunriseStr + sunsetStr + noonStr + durationStr;
    }

    @Override
    public String toString() {
        String sunriseStr = "Sunrise: " + toTimeString(sunriseLocalTime, HH_MM) + '\n';
        String sunsetStr = "Sunset: " + toTimeString(sunsetLocalTime, HH_MM) + '\n';
        String noonStr = "Solar noon: " + toTimeString(noonLocalTime, HH_MM) + '\n';
        String durationStr = "Day duration: " + toTimeString(dayDuration, HH_MM) + '\n';
        return sunriseStr + sunsetStr + noonStr + durationStr;
    }

    public double getSunriseLocalTime() {
        return sunriseLocalTime;
    }

    public double getSunsetLocalTime() {
        return sunsetLocalTime;
    }

    public float getTimeZoneOffset() {
        return timeZoneOffset;
    }

    public String getTimeZone() {
        return timeZoneName;
    }

    public double getDayDuration() {
        return dayDuration;
    }

    public double getNoonLocalTime() {
        return noonLocalTime;
    }

    public int getYear() {
        return year;
    }

    public int getMonth() {
        return month;
    }

    public int getDay() {
        return day;
    }

    public SunInfo setNewDate(int day, int month, int year) {
        this.day = day;
        this.month = month;
        this.year = year;
        init();
        return this;
    }

    private void init() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month - 1);
        calendar.set(Calendar.DAY_OF_MONTH, day);

        timeZoneName = TimezoneMapper.tzNameAt(latitude, longitude);
        TimeZone mTimeZone = TimeZone.getTimeZone(timeZoneName);
        timeZoneOffset = mTimeZone.getOffset(calendar.getTimeInMillis()) / 1000 / 60 / 60;

        julianDate = (double) SunMath.toJulianDayNumber(day, month, year);
        double n = SunMath.daysSince2000(julianDate);
        meanSolarNoon = SunMath.meanSolarNoon(n, longitude);
        double m = SunMath.solarMeanAnomaly(meanSolarNoon);
        double lambda = SunMath.eclipticLongitude(m);
        solarTransit = SunMath.solarTransit(meanSolarNoon, m);
        double omega = SunMath.hourAngle(latitude, lambda);

        sunriseJulianTime = SunMath.sunRise(solarTransit, omega) - julianDate;
        sunsetJulianTime = SunMath.sunSet(solarTransit, omega) - julianDate;

        sunriseLocalTime = toLocalTime(sunriseJulianTime, timeZoneOffset);
        sunsetLocalTime = toLocalTime(sunsetJulianTime, timeZoneOffset);
        noonLocalTime = toLocalTime(solarTransit - julianDate, timeZoneOffset);
        dayDuration = (sunsetJulianTime - sunriseJulianTime) * 24;
    }

    public String subDescription() {
        return ("Time zone: "
                + getTimeZoneOffset() + " (" + getTimeZone() + ") "
                + day + "."
                + month + "."
                + year);
    }

    public static int getMinute(double time) {
        int hour = (int) time;
        int minute = (int) (60 * (time % 1));
        return minute;
    }

    public static int getHour(double time) {
        int hour = (int) time;
        return hour;
    }

    public String toDateString() {
        return (day < 10 ? "0" : "") + day + "."
                + (month < 10 ? "0" : "") + month + "."
                + (year % 100 < 10 ? "0" : "") + year % 100;
    }

    public static String toLocationString(double lat, double lon, int d) {
        return Math.abs(SunMath.round(lat, d)) + (lat < 0 ? "S" : "N") + ", "
                + Math.abs(SunMath.round(lon, d)) + (lon < 0 ? "W" : "E");
    }

    public static String toTimeString(double time, int format) {
        int hour = (int) time;
        double minute = (time - hour) * 60;
        int min = (int) minute;
        double second = (minute - min) * 60;
        double sec = (int) second;
        int millis = (int) ((second - sec) * 1000);
        String timeStr = (hour > 9 ? "" : "0") + hour + (min > 9 ? ":" : ":0") + min;
        switch (format) {
            case HH_MM:
                break;
            case HH_MM_SS:
                timeStr += (sec > 9 ? ":" : ":0") + sec;
                break;
            case HH_MM_SS_MM:
                timeStr += sec > 9 ? ":" : ":0" + sec + SunMath.round(millis, 3);
                break;
            default:
                break;
        }
        return timeStr;
    }

    public SunInfo setNewPosition(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
        init();
        return this;
    }
}
