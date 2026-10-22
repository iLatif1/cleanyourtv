package com.gathin.tvhome;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/** Plain JVM test for the screensaver's weather line, "temperature · description". */
public class ClockSceneTest {

    @Test
    public void formatsTemperatureAndDescription() {
        Weather.Reading clear = new Weather.Reading(28.4, 0, 0L);
        assertEquals("28° · Clear", ClockScene.formatWeather(clear, false));
    }

    @Test
    public void roundsToNearestDegree() {
        Weather.Reading r = new Weather.Reading(28.6, 3, 0L);
        assertEquals("29° · Overcast", ClockScene.formatWeather(r, false));
    }

    @Test
    public void arabicUsesArabicDescription() {
        Weather.Reading r = new Weather.Reading(20, 61, 0L);
        String s = ClockScene.formatWeather(r, true);
        assertEquals("20° · مطر خفيف", s);
    }
}
