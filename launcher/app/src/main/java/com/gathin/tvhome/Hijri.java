package com.gathin.tvhome;

import java.time.LocalDate;
import java.time.chrono.HijrahDate;
import java.time.temporal.ChronoField;

/**
 * Hijri date from java.time's built-in Umm al-Qura calendar (config.xml, hijri_date). Month names
 * are bundled here rather than read from the system locale, the same choice {@link Shamsi} makes,
 * so the date reads the same on every Android version regardless of its ICU data.
 */
final class Hijri {

    private static final String[] MONTHS_AR = {
            "محرم", "صفر", "ربيع الأول", "ربيع الآخر", "جمادى الأولى", "جمادى الآخرة",
            "رجب", "شعبان", "رمضان", "شوال", "ذو القعدة", "ذو الحجة"};
    private static final String[] MONTHS_EN = {
            "Muharram", "Safar", "Rabi' al-awwal", "Rabi' al-thani", "Jumada al-awwal", "Jumada al-thani",
            "Rajab", "Sha'ban", "Ramadan", "Shawwal", "Dhu al-Qi'dah", "Dhu al-Hijjah"};

    private Hijri() {
    }

    /** e.g. "٢٧ رمضان ١٤٤٧" (Arabic) or "27 Ramadan 1447" (English). */
    static String today(boolean arabic) {
        return format(LocalDate.now(), arabic);
    }

    /** Pure function for testing. */
    static String format(LocalDate date, boolean arabic) {
        HijrahDate h = HijrahDate.from(date);
        int day = h.get(ChronoField.DAY_OF_MONTH);
        int month = h.get(ChronoField.MONTH_OF_YEAR);
        int year = h.get(ChronoField.YEAR_OF_ERA);
        String monthName = (arabic ? MONTHS_AR : MONTHS_EN)[month - 1];
        return arabic
                ? toArabicDigits(day) + " " + monthName + " " + toArabicDigits(year)
                : day + " " + monthName + " " + year;
    }

    /** Western digits to Eastern Arabic-Indic digits (٠-٩). */
    static String toArabicDigits(int n) {
        StringBuilder sb = new StringBuilder();
        for (char c : String.valueOf(n).toCharArray()) {
            sb.append(c == '-' ? '-' : (char) ('٠' + (c - '0')));
        }
        return sb.toString();
    }
}
