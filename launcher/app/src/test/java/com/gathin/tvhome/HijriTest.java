package com.gathin.tvhome;

import org.junit.Test;

import java.time.LocalDate;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Plain JVM tests for the Hijri date formatter, checked against the Umm al-Qura dates published by
 * aladhan.com (method 4) for the same Gregorian dates used in PrayerTimesTest.
 */
public class HijriTest {

    @Test
    public void matchesTheUmmAlQuraCalendarForKnownDates() {
        // 15 Jan 2026 = 26 Rajab 1447
        assertEquals("26 Rajab 1447", Hijri.format(LocalDate.of(2026, 1, 15), false));
        // 5 Mar 2026 falls in Ramadan 1447
        assertTrue(Hijri.format(LocalDate.of(2026, 3, 5), false).contains("Ramadan 1447"));
    }

    @Test
    public void arabicUsesArabicMonthNameAndEasternDigits() {
        String s = Hijri.format(LocalDate.of(2026, 1, 15), true);
        assertEquals("٢٦ رجب ١٤٤٧", s);
    }

    @Test
    public void digitConversionIsOneToOne() {
        assertEquals("١٢٣٤٥٦٧٨٩", Hijri.toArabicDigits(123456789));
        assertEquals("١٤٤٧", Hijri.toArabicDigits(1447));
        assertEquals("٥", Hijri.toArabicDigits(5));
    }

    @Test
    public void monthNamesCoverAllTwelveMonths() {
        // Walk a full lunar year (354/355 days) in 30-day steps and confirm every call resolves to a
        // real month name on both sides, i.e. the month index never runs off the array.
        LocalDate date = LocalDate.of(2026, 1, 1);
        for (int i = 0; i < 13; i++) {
            String en = Hijri.format(date, false);
            String ar = Hijri.format(date, true);
            assertTrue(en, en.matches("\\d+ [A-Za-z' -]+ \\d+"));
            assertTrue(ar, ar.length() > 0);
            date = date.plusDays(30);
        }
    }
}
