package com.gathin.tvhome;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.chrono.HijrahDate;
import java.time.temporal.ChronoField;

/**
 * Today's five prayer times for a place, computed offline with the Umm al-Qura method: Fajr at an
 * 18.5-degree sun depression, Dhuhr and Maghrib from the sun's own noon and sunset, Asr by the
 * Shafi'i shadow rule, and Isha a fixed 90 minutes after Maghrib (120 in Ramadan) rather than an
 * angle. The solar position uses the full NOAA algorithm (true anomaly and the equation of time
 * from the Julian century, not the shorter Fourier-series approximation {@link Sun} uses for the
 * sky backdrop), accurate to under a minute; that precision is what Fajr and Isha need and a
 * sunset glow does not. Pure maths, no network and no location permission; the place comes from
 * config.xml (latitude, longitude).
 */
final class PrayerTimes {

    private static final double FAJR_ANGLE = 18.5;
    private static final double TWILIGHT_ZENITH = 90.833;   // sunrise/sunset, upper limb with refraction
    private static final double ASR_SHADOW_FACTOR = 1;      // Shafi'i (and Maliki/Hanbali); Hanafi uses 2
    private static final int ISHA_AFTER_MAGHRIB_MINUTES = 90;
    private static final int ISHA_AFTER_MAGHRIB_MINUTES_RAMADAN = 120;
    private static final int RAMADAN_MONTH = 9;

    /** Local minutes after midnight for each prayer; null when the sun does not reach that angle that day. */
    static final class Times {
        final Integer fajr, sunrise, dhuhr, asr, maghrib, isha;

        Times(Integer fajr, Integer sunrise, Integer dhuhr, Integer asr, Integer maghrib, Integer isha) {
            this.fajr = fajr;
            this.sunrise = sunrise;
            this.dhuhr = dhuhr;
            this.asr = asr;
            this.maghrib = maghrib;
            this.isha = isha;
        }
    }

    private PrayerTimes() {
    }

    static Times today(double latitude, double longitude, ZoneId zone) {
        return forDate(LocalDate.now(zone), latitude, longitude, zone);
    }

    /** Pure function for testing: times for a specific date, independent of the system clock. */
    static Times forDate(LocalDate date, double latitude, double longitude, ZoneId zone) {
        double jd = julianDay(date) + 0.5;
        double t = (jd - 2451545.0) / 36525.0;   // Julian century since J2000

        double l0 = norm360(280.46646 + t * (36000.76983 + t * 0.0003032));
        double m = Math.toRadians(357.52911 + t * (35999.05029 - 0.0001537 * t));
        double e = 0.016708634 - t * (0.000042037 + 0.0000001267 * t);
        double c = Math.sin(m) * (1.914602 - t * (0.004817 + 0.000014 * t))
                + Math.sin(2 * m) * (0.019993 - 0.000101 * t)
                + Math.sin(3 * m) * 0.000289;
        double trueLong = l0 + c;
        double omega = Math.toRadians(125.04 - 1934.136 * t);
        double lambda = Math.toRadians(trueLong - 0.00569 - 0.00478 * Math.sin(omega));
        double eps0 = 23 + (26 + (21.448 - t * (46.815 + t * (0.00059 - t * 0.001813))) / 60) / 60;
        double eps = Math.toRadians(eps0 + 0.00256 * Math.cos(omega));
        double decl = Math.asin(Math.sin(eps) * Math.sin(lambda));
        double y = Math.tan(eps / 2) * Math.tan(eps / 2);
        double l0r = Math.toRadians(l0);
        double eqTime = 4 * Math.toDegrees(y * Math.sin(2 * l0r) - 2 * e * Math.sin(m)
                + 4 * e * y * Math.sin(m) * Math.cos(2 * l0r)
                - 0.5 * y * y * Math.sin(4 * l0r) - 1.25 * e * e * Math.sin(2 * m));

        double lat = Math.toRadians(latitude);
        int offset = zone.getRules().getOffset(date.atTime(12, 0)).getTotalSeconds() / 60;
        double noon = 720 - 4 * longitude - eqTime + offset;

        Double fajrHa = hourAngle(90 + FAJR_ANGLE, lat, decl);
        Double twilightHa = hourAngle(TWILIGHT_ZENITH, lat, decl);
        double shadowFactor = ASR_SHADOW_FACTOR + Math.tan(Math.abs(lat - decl));
        double asrZenith = 90 - Math.toDegrees(Math.atan(1 / shadowFactor));
        Double asrHa = hourAngle(asrZenith, lat, decl);

        Integer fajr = fajrHa == null ? null : round(noon - 4 * fajrHa);
        Integer sunrise = twilightHa == null ? null : round(noon - 4 * twilightHa);
        Integer dhuhr = round(noon);
        Integer asr = asrHa == null ? null : round(noon + 4 * asrHa);
        Integer maghrib = twilightHa == null ? null : round(noon + 4 * twilightHa);
        boolean ramadan = hijriMonth(date) == RAMADAN_MONTH;
        Integer isha = maghrib == null ? null
                : maghrib + (ramadan ? ISHA_AFTER_MAGHRIB_MINUTES_RAMADAN : ISHA_AFTER_MAGHRIB_MINUTES);

        return new Times(fajr, sunrise, dhuhr, asr, maghrib, isha);
    }

    /**
     * Hour angle in degrees for a zenith angle (measured from straight up; 90 is the horizon), or
     * null at a latitude/season where the sun never reaches it (polar Fajr in high-latitude summer).
     */
    private static Double hourAngle(double zenithDegrees, double latRadians, double declRadians) {
        double cosHa = (Math.cos(Math.toRadians(zenithDegrees)) - Math.sin(latRadians) * Math.sin(declRadians))
                / (Math.cos(latRadians) * Math.cos(declRadians));
        if (cosHa > 1 || cosHa < -1) return null;
        return Math.toDegrees(Math.acos(cosHa));
    }

    private static int round(double minutes) {
        return (int) Math.round(minutes);
    }

    private static double norm360(double degrees) {
        double d = degrees % 360;
        return d < 0 ? d + 360 : d;
    }

    private static double julianDay(LocalDate date) {
        int y = date.getYear(), mo = date.getMonthValue(), d = date.getDayOfMonth();
        if (mo <= 2) {
            y -= 1;
            mo += 12;
        }
        int a = y / 100;
        int b = 2 - a + a / 4;
        return Math.floor(365.25 * (y + 4716)) + Math.floor(30.6001 * (mo + 1)) + d + b - 1524.5;
    }

    /** 1 (Muharram) to 12 (Dhu al-Hijjah) in the Umm al-Qura calendar; 9 is Ramadan. */
    static int hijriMonth(LocalDate date) {
        return HijrahDate.from(date).get(ChronoField.MONTH_OF_YEAR);
    }
}
