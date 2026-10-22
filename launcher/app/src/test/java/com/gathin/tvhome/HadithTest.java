package com.gathin.tvhome;

import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/** Plain JVM tests for the daily hadith rotation: picks one entry per day, wraps both ways, no gaps. */
public class HadithTest {

    @Test
    public void everyEntryHasArabicAndEnglishWithASource() {
        for (Hadith.Entry e : Hadith.ALL) {
            assertFalse(e.arabic.trim().isEmpty());
            assertFalse(e.arabicSource.trim().isEmpty());
            assertFalse(e.english.trim().isEmpty());
            assertFalse(e.englishSource.trim().isEmpty());
        }
    }

    @Test
    public void dayOneIsTheFirstEntry() {
        assertEquals(Hadith.ALL[0], Hadith.ofDay(1));
    }

    @Test
    public void wrapsPastTheEndOfTheList() {
        int n = Hadith.ALL.length;
        assertEquals(Hadith.ALL[0], Hadith.ofDay(n + 1));
        assertEquals(Hadith.ALL[n - 1], Hadith.ofDay(n));
    }

    @Test
    public void leapYearDay366StillPicksAnEntry() {
        assertNotNull(Hadith.ofDay(366));
    }

    @Test
    public void consecutiveDaysStepThroughTheListInOrder() {
        int n = Hadith.ALL.length;
        for (int day = 1; day < n; day++) {
            assertEquals(Hadith.ALL[day], Hadith.ofDay(day + 1));
        }
    }

    @Test
    public void everyEntryIsReachable() {
        Set<Hadith.Entry> seen = new HashSet<>();
        for (int day = 1; day <= Hadith.ALL.length; day++) seen.add(Hadith.ofDay(day));
        assertEquals(Hadith.ALL.length, seen.size());
    }

    @Test
    public void listHasNoDuplicateText() {
        // Two entries may share a source (Muslim 2699 covers both #16 and #20, two different clauses
        // of the same long hadith), but never the same wording twice.
        Set<String> texts = new HashSet<>();
        for (Hadith.Entry e : Hadith.ALL) assertTrue(e.english, texts.add(e.english));
    }
}
