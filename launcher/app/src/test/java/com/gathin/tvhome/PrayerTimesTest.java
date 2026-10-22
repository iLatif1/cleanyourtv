package com.gathin.tvhome;

import org.junit.Test;

import java.time.LocalDate;
import java.time.ZoneId;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Plain JVM tests for the Umm al-Qura prayer-time calculation, checked against Madinah times
 * published by the Umm al-Qura method (aladhan.com, method 4, which documents Fajr 18.5 degrees
 * and Isha 90 minutes after Maghrib, 120 in Ramadan) for four dates across the year, within the
 * one-minute tolerance those published times are themselves given to.
 */
public class PrayerTimesTest {

    private static final double MADINAH_LAT = 24.4672;
    private static final double MADINAH_LON = 39.6111;
    private static final ZoneId RIYADH = ZoneId.of("Asia/Riyadh");

    private static void assertWithinAMinute(String label, int expectedMinutesAfterMidnight, Integer actual) {
        assertTrue(label + ": no time (sun never reaches that angle)", actual != null);
        assertTrue(label + ": expected " + clock(expectedMinutesAfterMidnight) + ", got " + clock(actual),
                Math.abs(actual - expectedMinutesAfterMidnight) <= 1);
    }

    private static String clock(int minutes) {
        return String.format("%02d:%02d", minutes / 60, minutes % 60);
    }

    private static int at(int h, int m) {
        return h * 60 + m;
    }

    @Test
    public void madinahWinter() {
        PrayerTimes.Times t = PrayerTimes.forDate(LocalDate.of(2026, 1, 15), MADINAH_LAT, MADINAH_LON, RIYADH);
        assertWithinAMinute("Fajr", at(5, 45), t.fajr);
        assertWithinAMinute("Sunrise", at(7, 7), t.sunrise);
        assertWithinAMinute("Dhuhr", at(12, 31), t.dhuhr);
        assertWithinAMinute("Asr", at(15, 34), t.asr);
        assertWithinAMinute("Maghrib", at(17, 55), t.maghrib);
        assertWithinAMinute("Isha", at(19, 25), t.isha);   // not Ramadan: Maghrib + 90
    }

    @Test
    public void madinahSummer() {
        PrayerTimes.Times t = PrayerTimes.forDate(LocalDate.of(2026, 6, 15), MADINAH_LAT, MADINAH_LON, RIYADH);
        assertWithinAMinute("Fajr", at(4, 1), t.fajr);
        assertWithinAMinute("Sunrise", at(5, 33), t.sunrise);
        assertWithinAMinute("Dhuhr", at(12, 22), t.dhuhr);
        assertWithinAMinute("Asr", at(15, 42), t.asr);
        assertWithinAMinute("Maghrib", at(19, 11), t.maghrib);
        assertWithinAMinute("Isha", at(20, 41), t.isha);
    }

    @Test
    public void madinahAutumn() {
        PrayerTimes.Times t = PrayerTimes.forDate(LocalDate.of(2026, 9, 22), MADINAH_LAT, MADINAH_LON, RIYADH);
        assertWithinAMinute("Fajr", at(4, 52), t.fajr);
        assertWithinAMinute("Sunrise", at(6, 10), t.sunrise);
        assertWithinAMinute("Dhuhr", at(12, 14), t.dhuhr);
        assertWithinAMinute("Asr", at(15, 41), t.asr);
        assertWithinAMinute("Maghrib", at(18, 18), t.maghrib);
        assertWithinAMinute("Isha", at(19, 48), t.isha);
    }

    @Test
    public void madinahRamadan() {
        // 5 March 2026 falls in Ramadan on the Umm al-Qura calendar: Isha is Maghrib + 120, not + 90.
        LocalDate date = LocalDate.of(2026, 3, 5);
        assertEquals(9, PrayerTimes.hijriMonth(date));
        PrayerTimes.Times t = PrayerTimes.forDate(date, MADINAH_LAT, MADINAH_LON, RIYADH);
        assertWithinAMinute("Fajr", at(5, 23), t.fajr);
        assertWithinAMinute("Sunrise", at(6, 41), t.sunrise);
        assertWithinAMinute("Dhuhr", at(12, 33), t.dhuhr);
        assertWithinAMinute("Asr", at(15, 55), t.asr);
        assertWithinAMinute("Maghrib", at(18, 26), t.maghrib);
        assertWithinAMinute("Isha", at(20, 26), t.isha);   // Ramadan: Maghrib + 120
        assertEquals((int) t.maghrib + 120, (int) t.isha);
    }

    @Test
    public void ishaIsAlwaysNinetyMinutesAfterMaghribOutsideRamadan() {
        PrayerTimes.Times t = PrayerTimes.forDate(LocalDate.of(2026, 1, 15), MADINAH_LAT, MADINAH_LON, RIYADH);
        assertEquals((int) t.maghrib + 90, (int) t.isha);
    }

    @Test
    public void timesAdvanceThroughTheDayInOrder() {
        PrayerTimes.Times t = PrayerTimes.forDate(LocalDate.of(2026, 1, 15), MADINAH_LAT, MADINAH_LON, RIYADH);
        assertTrue(t.fajr < t.sunrise);
        assertTrue(t.sunrise < t.dhuhr);
        assertTrue(t.dhuhr < t.asr);
        assertTrue(t.asr < t.maghrib);
        assertTrue(t.maghrib < t.isha);
    }

    @Test
    public void polarSummerHasNoFajrOrIsha() {
        // Tromso, Norway, midsummer: the sun never goes 18.5 degrees below the horizon.
        PrayerTimes.Times t = PrayerTimes.forDate(LocalDate.of(2026, 6, 21), 69.6492, 18.9553, ZoneId.of("Europe/Oslo"));
        assertNull(t.fajr);
    }
}
