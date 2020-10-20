package com.siarhei.alarmus.sun;

import com.moodysalem.TimezoneMapper;

import java.util.Calendar;
import java.util.TimeZone;

public class SunInfo {

    public static final int HH_MM = 1;
    public static final int HH_MM_SS = 2;
    public static final int HH_MM_SS_MM = 3;

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
    private int timeZoneOffset;
    private String timeZone;


    public SunInfo(int day, int month, int year, double latitude, double longitude) {
        this.day = day;
        this.month = month;
        this.year = year;
        this.latitude = latitude;
        this.longitude = longitude;
        init();
    }

    public static SunInfo nextDaySunInfo(SunInfo info) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(info.year, info.month - 1, info.day);
        calendar.add(Calendar.DAY_OF_MONTH, 1);
        SunInfo infoNext = new SunInfo(calendar.get(Calendar.DAY_OF_MONTH),
                calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.YEAR),
                info.latitude, info.longitude);
        return infoNext;
    }

    public static boolean afterNow(double time) {
        Calendar calendar = Calendar.getInstance();
        long currentTime = System.currentTimeMillis();
        calendar.setTimeInMillis(currentTime);
        int hour = (int) time;
        int minute = (int) (60 * (time - hour));
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        return calendar.getTimeInMillis() > currentTime;
    }

    public static double toLocalTime(double julianTime, int offset) {
        double time = (julianTime + 0.5 + offset / 24.0) % 1;
        return time >= 0 ? 24 * time : 24 * time + 24;
    }

    public static int timeZoneOffset(double latitude, double longitude) {
        String timeZone = TimezoneMapper.tzNameAt(latitude, longitude);
        TimeZone mTimeZone = TimeZone.getTimeZone(timeZone);
        return mTimeZone.getOffset(Calendar.ZONE_OFFSET) / 1000 / 60 / 60;
    }

    public static String timeToString(double time, int format) {
        int hour = (int) time;
        double minute = (time - hour) * 60;
        double second = (minute - (int) minute) * 60;
        int millis = (int) ((second - (int) second) * 1000);
        String timeStr = hour + ":";
        switch (format) {
            case HH_MM:
                int min = (int) Math.round(minute);
                timeStr += min > 9 ? min : "0" + min;
                break;
            case HH_MM_SS:
                int min1 = (int) minute;
                timeStr += min1 > 9 ? min1 : "0" + min1;
                timeStr += ":";
                int sec = (int) Math.round(second);
                timeStr += sec > 9 ? sec : "0" + sec;
                break;
            case HH_MM_SS_MM:
                int min2 = (int) minute;
                timeStr += min2 > 9 ? min2 : "0" + min2;
                int sec1 = (int) second;
                timeStr += sec1 > 9 ? sec1 : "0" + sec1;
                timeStr += "." + millis;
                break;
            default:
                break;
        }
        return timeStr;
    }

    public String toString(int timeFormat) {
        String sunriseStr = "Sunrise: " + timeToString(sunriseLocalTime, timeFormat) + '\n';
        String sunsetStr = "Sunset: " + timeToString(sunsetLocalTime, timeFormat) + '\n';
        String noonStr = "Solar noon: " + timeToString(noonLocalTime, timeFormat) + '\n';
        String durationStr = "Day duration: " + timeToString(dayDuration, timeFormat) + '\n';
        return sunriseStr + sunsetStr + noonStr + durationStr;
    }

    @Override
    public String toString() {
        String sunriseStr = "Sunrise: " + timeToString(sunriseLocalTime, HH_MM) + '\n';
        String sunsetStr = "Sunset: " + timeToString(sunsetLocalTime, HH_MM) + '\n';
        String noonStr = "Solar noon: " + timeToString(noonLocalTime, HH_MM) + '\n';
        String durationStr = "Day duration: " + timeToString(dayDuration, HH_MM) + '\n';
        return sunriseStr + sunsetStr + noonStr + durationStr;
    }

    public double getSunriseLocalTime() {
        return sunriseLocalTime;
    }

    public double getSunsetLocalTime() {
        return sunsetLocalTime;
    }

    public int getTimeZoneOffset() {
        return timeZoneOffset;
    }

    public String getTimeZone() {
        return timeZone;
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

    public void setNewDate(int day, int month, int year) {
        this.day = day;
        this.month = month;
        this.year = year;
        init();
    }

    private void init() {
        timeZone = TimezoneMapper.tzNameAt(latitude, longitude);
        TimeZone mTimeZone = TimeZone.getTimeZone(timeZone);
        timeZoneOffset = mTimeZone.getOffset(Calendar.ZONE_OFFSET) / 1000 / 60 / 60;

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
}
