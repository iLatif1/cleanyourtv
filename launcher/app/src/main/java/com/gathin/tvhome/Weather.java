package com.gathin.tvhome;

import android.content.Context;
import android.util.AtomicFile;
import android.util.Log;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * Current weather for a place (config.xml, weather_enabled; latitude/longitude), from Open-Meteo,
 * which needs no account or key. Fetched at most once an hour on a background thread while Home is
 * on screen (MainActivity's startWeather/stopWeather, the same HandlerThread pattern show_stats
 * uses); the last reading is cached to a private file and kept on screen while offline, and nothing
 * is shown before the first successful fetch. One permission: INTERNET.
 */
final class Weather {
    private static final String TAG = "TvHome.Weather";
    static final String FILE_NAME = "weather.txt";
    static final long REFRESH_MS = 60 * 60 * 1000L;
    private static final int TIMEOUT_MS = 8000;

    static final class Reading {
        final double temperatureC;
        final int weatherCode;   // WMO code, open-meteo.com/en/docs "WMO Weather interpretation codes"
        final long fetchedAt;    // epoch millis

        Reading(double temperatureC, int weatherCode, long fetchedAt) {
            this.temperatureC = temperatureC;
            this.weatherCode = weatherCode;
            this.fetchedAt = fetchedAt;
        }
    }

    private Weather() {
    }

    static File file(Context ctx) {
        return new File(ctx.getFilesDir(), FILE_NAME);
    }

    /** The cached reading, or null when there is none or it does not parse. Never touches the network. */
    static Reading cached(File file) {
        if (!file.exists()) return null;
        try {
            return decode(new String(new AtomicFile(file).readFully(), StandardCharsets.UTF_8));
        } catch (IOException e) {
            return null;
        }
    }

    static boolean stale(Reading r) {
        return r == null || System.currentTimeMillis() - r.fetchedAt > REFRESH_MS;
    }

    /**
     * Fetches a fresh reading over the network and caches it. Null on any failure (no network, a bad
     * response, a timeout) and the old cache is left as it was. Call this off the main thread.
     */
    static Reading refresh(File file, double latitude, double longitude) {
        HttpURLConnection conn = null;
        try {
            String url = "https://api.open-meteo.com/v1/forecast?latitude=" + latitude + "&longitude=" + longitude
                    + "&current=temperature_2m,weather_code&timezone=auto";
            conn = (HttpURLConnection) new URL(url).openConnection();
            conn.setConnectTimeout(TIMEOUT_MS);
            conn.setReadTimeout(TIMEOUT_MS);
            conn.setRequestProperty("User-Agent", "cleanyourtv-islamic-home");
            if (conn.getResponseCode() != 200) return null;
            Reading reading = parse(readAll(conn.getInputStream()));
            if (reading != null) save(file, reading);
            return reading;
        } catch (Exception e) {
            Log.w(TAG, "weather fetch failed: " + e.getMessage());
            return null;
        } finally {
            if (conn != null) conn.disconnect();
        }
    }

    /** Pure function for testing: parses an Open-Meteo {@code /v1/forecast} response body. */
    static Reading parse(String body) {
        try {
            JSONObject current = new JSONObject(body).getJSONObject("current");
            return new Reading(current.getDouble("temperature_2m"), current.getInt("weather_code"),
                    System.currentTimeMillis());
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * A short description for a WMO weather code (open-meteo.com/en/docs). Codes outside the table
     * (a future addition to the API) fall back to "Clear", never a blank line.
     */
    static String description(int code, boolean arabic) {
        String[] table = arabic ? DESCRIPTIONS_AR : DESCRIPTIONS_EN;
        for (int i = 0; i < CODES.length; i++) {
            if (CODES[i] == code) return table[i];
        }
        return table[0];
    }

    // Grouped WMO codes: clear/cloud, fog, drizzle, rain, snow, showers, thunderstorm.
    private static final int[] CODES = {0, 1, 2, 3, 45, 48, 51, 53, 55, 61, 63, 65, 71, 73, 75, 77, 80, 81, 82, 85, 86, 95, 96, 99};
    private static final String[] DESCRIPTIONS_EN = {
            "Clear", "Mostly clear", "Partly cloudy", "Overcast", "Fog", "Fog",
            "Light drizzle", "Drizzle", "Dense drizzle", "Light rain", "Rain", "Heavy rain",
            "Light snow", "Snow", "Heavy snow", "Snow grains",
            "Rain showers", "Rain showers", "Heavy rain showers", "Snow showers", "Snow showers",
            "Thunderstorm", "Thunderstorm", "Thunderstorm with hail"};
    private static final String[] DESCRIPTIONS_AR = {
            "صافٍ", "صافٍ غالبًا", "غائم جزئيًا", "غائم", "ضباب", "ضباب",
            "رذاذ خفيف", "رذاذ", "رذاذ كثيف", "مطر خفيف", "مطر", "مطر غزير",
            "ثلج خفيف", "ثلج", "ثلج كثيف", "حبيبات ثلجية",
            "زخات مطر", "زخات مطر", "زخات مطر غزيرة", "زخات ثلج", "زخات ثلج",
            "عاصفة رعدية", "عاصفة رعدية", "عاصفة رعدية مع برَد"};

    private static String readAll(InputStream in) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buf = new byte[4096];
        int n;
        while ((n = in.read(buf)) != -1) out.write(buf, 0, n);
        return out.toString("UTF-8");
    }

    private static void save(File file, Reading r) {
        AtomicFile af = new AtomicFile(file);
        FileOutputStream out = null;
        try {
            out = af.startWrite();
            out.write(encode(r).getBytes(StandardCharsets.UTF_8));
            af.finishWrite(out);
        } catch (IOException e) {
            Log.w(TAG, "cannot cache weather: " + e.getMessage());
            if (out != null) af.failWrite(out);
        }
    }

    static String encode(Reading r) {
        return r.temperatureC + "\n" + r.weatherCode + "\n" + r.fetchedAt + "\n";
    }

    static Reading decode(String text) {
        String[] lines = text.split("\n", -1);
        if (lines.length < 3) return null;
        try {
            return new Reading(Double.parseDouble(lines[0]), Integer.parseInt(lines[1]), Long.parseLong(lines[2]));
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
