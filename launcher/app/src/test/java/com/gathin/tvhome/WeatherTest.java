package com.gathin.tvhome;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/** Plain JVM tests for parsing Open-Meteo's response and the cache round-trip. No network, no Android types. */
public class WeatherTest {

    private static final String SAMPLE = "{"
            + "\"latitude\":24.47,\"longitude\":39.6,"
            + "\"current\":{\"time\":\"2026-10-22T15:00\",\"temperature_2m\":28.4,\"weather_code\":3}"
            + "}";

    @Test
    public void parsesTemperatureAndWeatherCode() {
        Weather.Reading r = Weather.parse(SAMPLE);
        assertEquals(28.4, r.temperatureC, 0.001);
        assertEquals(3, r.weatherCode);
        assertTrue(r.fetchedAt > 0);
    }

    @Test
    public void malformedBodyParsesToNull() {
        assertNull(Weather.parse("not json"));
        assertNull(Weather.parse("{}"));
        assertNull(Weather.parse("{\"current\":{\"temperature_2m\":28.4}}"));   // no weather_code
    }

    @Test
    public void encodeDecodeRoundTrips() {
        Weather.Reading r = new Weather.Reading(31.2, 61, 1_700_000_000_000L);
        Weather.Reading back = Weather.decode(Weather.encode(r));
        assertEquals(r.temperatureC, back.temperatureC, 0.001);
        assertEquals(r.weatherCode, back.weatherCode);
        assertEquals(r.fetchedAt, back.fetchedAt);
    }

    @Test
    public void decodeRejectsGarbage() {
        assertNull(Weather.decode(""));
        assertNull(Weather.decode("not\nvalid\nnumbers\n"));
    }

    @Test
    public void staleIsTrueForNullOrOldReadings() {
        assertTrue(Weather.stale(null));
        Weather.Reading old = new Weather.Reading(20, 0, System.currentTimeMillis() - Weather.REFRESH_MS - 1000);
        assertTrue(Weather.stale(old));
        Weather.Reading fresh = new Weather.Reading(20, 0, System.currentTimeMillis());
        assertTrue(!Weather.stale(fresh));
    }

    @Test
    public void everyWmoCodeHasADescriptionInBothLanguages() {
        int[] codes = {0, 1, 2, 3, 45, 48, 51, 53, 55, 61, 63, 65, 71, 73, 75, 77, 80, 81, 82, 85, 86, 95, 96, 99};
        for (int code : codes) {
            assertTrue(!Weather.description(code, false).isEmpty());
            assertTrue(!Weather.description(code, true).isEmpty());
        }
    }

    @Test
    public void unknownCodeFallsBackInsteadOfBlank() {
        assertEquals("Clear", Weather.description(12345, false));
    }
}
