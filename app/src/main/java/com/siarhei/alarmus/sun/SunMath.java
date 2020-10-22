package com.siarhei.alarmus.sun;

public class SunMath {
    public final static double LEAP_CORRECTION = 0.0008;
    public final static double SUN_REFRACTION_CORRECTION = -0.6;
    public final static double SUN_RADIUS_CORRECTION = 0.23;
    public final static double ARGUMENT_OF_PERIHELION = 102.9372;
    public final static double JULIAN_NOON = 2451545.0;
    public final static double EARTH_AXIAL_TILT = 23.44;

    private final static double solarMeanAnomalyA = 357.5291;
    private final static double solarMeanAnomalyB = 0.98560028;
    private final static double equationOfTheCenterA = 1.9148;
    private final static double equationOfTheCenterB = 0.02;
    private final static double equationOfTheCenterC = 0.0003;
    private final static double equationOfTimeA = 0.0053;
    private final static double equationOfTimeB = -0.0069;


    public static double daysSince2000(double julianDate) {
        return julianDate - JULIAN_NOON + LEAP_CORRECTION;
    }

    public static double meanSolarNoon(double solarNoon, double longitudeWest) {
        return solarNoon - longitudeWest / 360.0;
    }

    public static double solarMeanAnomaly(double meanSolarNoon) {
        return (solarMeanAnomalyA + solarMeanAnomalyB * meanSolarNoon) % 360;
    }

    public static double equationOfTheCenter(double solarMeanAnomaly) {
        double m = toRad(solarMeanAnomaly);
        return equationOfTheCenterA * Math.sin(m) +
                equationOfTheCenterB * Math.sin(2 * m) +
                equationOfTheCenterC * Math.sin(3 * m);
    }

    public static double eclipticLongitude(double solarMeanAnomaly) {
        final double equationOfTheCenter = equationOfTheCenter(solarMeanAnomaly);
        return (ARGUMENT_OF_PERIHELION + equationOfTheCenter + solarMeanAnomaly + 180) % 360;
    }

    public static double equationOfTime(double solarMeanAnomaly) {
        double eclipticLongitude = eclipticLongitude(solarMeanAnomaly);
        return equationOfTimeA * Math.sin(toRad(solarMeanAnomaly)) +
                equationOfTimeB * Math.sin(2 * toRad(eclipticLongitude));
    }

    public static double solarTransit(double meanSolarTime, double solarMeanAnomaly) {
        final double equationOfTime = equationOfTime(solarMeanAnomaly);
        return meanSolarTime + JULIAN_NOON + equationOfTime;
    }

    public static double sunDeclinationSin(double eclipticLongitude) {
        return Math.sin(toRad(eclipticLongitude)) * Math.sin(toRad(EARTH_AXIAL_TILT));
    }

    public static double hourAngle(double latitude, double eclipticLongitude) {
        double sunDeclinationSin = sunDeclinationSin(eclipticLongitude);
        double sunDeclinationCos = Math.cos(Math.asin(sunDeclinationSin));
        double latitudeSin = Math.sin(toRad(latitude));
        double latitudeCos = Math.cos(toRad(latitude));
        double correctionSin = Math.sin(toRad(SUN_REFRACTION_CORRECTION-SUN_RADIUS_CORRECTION));
        double cosHourAngle = (correctionSin - sunDeclinationSin * latitudeSin) /
                (latitudeCos * sunDeclinationCos);
        return toDegree(Math.acos(cosHourAngle));
    }

    public static double sunRise(double solarTransit, double hourAngle) {
        return solarTransit - hourAngle / 360;
    }

    public static double sunSet(double solarTransit, double hourAngle) {
        return solarTransit + hourAngle / 360;
    }

    public static double toRad(double degree) {
        return degree * Math.PI / 180;
    }

    public static double toDegree(double rad) {
        return rad / Math.PI * 180;
    }

    public static int toJulianDayNumber(int day, int month, int year) {
        int a = (14 - month) / 12;
        int y = year + 4800 - a;
        int m = month + 12 * a - 3;
        int jdn = day + (153 * m + 2) / 5 + 365 * y
                + y / 4 - y / 100 + y / 400 - 32045;
        return jdn;
    }

   public static double round(double number, int d) {
       int m = (int) Math.pow(10, d);
       double res = (double) Math.round(number * m);
       return res / m;
   }
}
