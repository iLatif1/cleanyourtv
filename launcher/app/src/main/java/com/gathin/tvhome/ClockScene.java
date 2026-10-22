package com.gathin.tvhome;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.TextView;

import java.io.File;
import java.util.Locale;
import java.util.Random;

/**
 * The clock screensaver's scene (layout dream_clock): the thin clock and date on black over a slow ambient
 * glow. Every minute the clock drifts to a new spot and the glow shifts colour, so nothing burns in.
 * Shared by ClockDream (the system screensaver) and ClockActivity (Home left idle).
 */
final class ClockScene {

    private static final long MOVE_MS = 60_000L;
    /** Deep, low-brightness glow colours; the glow is drawn at partial alpha on black. */
    private static final int[] PALETTE = {0xFF2B5CFF, 0xFF00A39A, 0xFF8A3FFC, 0xFFE0661B, 0xFFD6336C};

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Random random = new Random();
    private final View content;
    private final GradientDrawable glow;
    private int colorIndex;
    private int glowColor;

    private final Context ctx;
    private final boolean weatherEnabled;
    private final boolean arabic;
    private final TextView weatherView;

    private final Runnable tick = new Runnable() {
        @Override
        public void run() {
            moveClock();
            shiftGlow();
            handler.postDelayed(this, MOVE_MS);
        }
    };

    /** root is the inflated dream_clock layout. */
    ClockScene(View root) {
        content = root.findViewById(R.id.dreamContent);
        View ambient = root.findViewById(R.id.dreamAmbient);
        glow = new GradientDrawable();
        glow.setGradientType(GradientDrawable.RADIAL_GRADIENT);
        ambient.setBackground(glow);
        ambient.addOnLayoutChangeListener((v, l, t, r, b, ol, ot, or, ob) ->
                glow.setGradientRadius((r - l) * 0.55f));
        colorIndex = random.nextInt(PALETTE.length);
        setGlow(PALETTE[colorIndex]);

        ctx = root.getContext();
        Config cfg = Config.load(ctx);
        arabic = Locale.getDefault().getLanguage().equals("ar");
        weatherEnabled = cfg.weatherEnabled;
        weatherView = root.findViewById(R.id.dreamWeather);
        showIslamicContent(root, cfg);
    }

    /**
     * Hijri date and hadith of the day, set once: neither changes while the screensaver is up. Weather
     * shows the last cached reading immediately, then {@link #start} refreshes it if that reading is
     * stale, the only place in the launcher that fetches weather. Views the layout for this locale does
     * not have (every locale but Arabic and the default) are simply absent, so this is a no-op there.
     */
    private void showIslamicContent(View root, Config cfg) {
        TextView hijri = root.findViewById(R.id.dreamHijriDate);
        if (hijri != null && cfg.hijriDate) {
            hijri.setText(Hijri.today(arabic));
            hijri.setVisibility(View.VISIBLE);
        }

        if (weatherView != null && weatherEnabled) {
            Weather.Reading reading = Weather.cached(Weather.file(ctx));
            if (reading != null) {
                weatherView.setText(formatWeather(reading, arabic));
                weatherView.setVisibility(View.VISIBLE);
            }
        }

        TextView prayer = root.findViewById(R.id.dreamPrayerTimes);
        if (prayer != null && cfg.prayerTimesEnabled) showPrayerTimes(prayer);

        if (cfg.hadithEnabled) {
            TextView hadith = root.findViewById(R.id.dreamHadith);
            TextView source = root.findViewById(R.id.dreamHadithSource);
            if (hadith != null) {
                Hadith.Entry entry = Hadith.today();
                hadith.setText(arabic ? entry.arabic : entry.english);
                hadith.setVisibility(View.VISIBLE);
                if (source != null) {
                    source.setText(arabic ? entry.arabicSource : entry.englishSource);
                    source.setVisibility(View.VISIBLE);
                }
            }
        }
    }

    /** The five prayer times from PrayerTimes (offline, config.xml latitude/longitude); nothing shows without a location. */
    private void showPrayerTimes(TextView view) {
        double latitude, longitude;
        try {
            latitude = Double.parseDouble(ctx.getString(R.string.latitude));
            longitude = Double.parseDouble(ctx.getString(R.string.longitude));
        } catch (NumberFormatException e) {
            return;   // empty (the default) or not a number
        }
        PrayerTimes.Times t = PrayerTimes.today(latitude, longitude, java.time.ZoneId.systemDefault());
        if (t.fajr == null) return;   // the sun never reaches Fajr's angle here today (high-latitude summer)
        String[] names = arabic
                ? new String[]{"الفجر", "الظهر", "العصر", "المغرب", "العشاء"}
                : new String[]{"Fajr", "Dhuhr", "Asr", "Maghrib", "Isha"};
        Integer[] minutes = {t.fajr, t.dhuhr, t.asr, t.maghrib, t.isha};
        StringBuilder line = new StringBuilder();
        for (int i = 0; i < names.length; i++) {
            if (minutes[i] == null) continue;
            if (line.length() > 0) line.append("   ");
            line.append(names[i]).append(' ').append(clockTime(minutes[i]));
        }
        view.setText(line);
        view.setVisibility(View.VISIBLE);
    }

    private static String clockTime(int minutesAfterMidnight) {
        int m = ((minutesAfterMidnight % 1440) + 1440) % 1440;
        int h = m / 60;
        return String.format(Locale.US, "%d:%02d", h == 0 ? 12 : h > 12 ? h - 12 : h, m % 60);
    }

    /**
     * Refreshes the weather on a plain background thread if the cached reading is stale (or there is
     * none), the way show_stats already reads off the main thread. Weather is fetched here and nowhere
     * else: Home no longer polls hourly, only the screensaver, and only while it is actually showing.
     */
    private void fetchWeatherIfStale() {
        if (!weatherEnabled || weatherView == null) return;
        File file = Weather.file(ctx);
        if (!Weather.stale(Weather.cached(file))) return;
        double latitude, longitude;
        try {
            latitude = Double.parseDouble(ctx.getString(R.string.latitude));
            longitude = Double.parseDouble(ctx.getString(R.string.longitude));
        } catch (NumberFormatException e) {
            return;   // empty (the default) or not a number
        }
        new Thread(() -> {
            Weather.Reading fresh = Weather.refresh(file, latitude, longitude);
            if (fresh != null) handler.post(() -> {
                weatherView.setText(formatWeather(fresh, arabic));
                weatherView.setVisibility(View.VISIBLE);
            });
        }, "weather-fetch").start();
    }

    static String formatWeather(Weather.Reading r, boolean arabic) {
        String temp = Math.round(r.temperatureC) + "°";
        return temp + " · " + Weather.description(r.weatherCode, arabic);
    }

    void start() {
        content.setAlpha(0f);
        content.post(() -> {
            placeClock();
            content.animate().alpha(1f).setDuration(1200).start();
        });
        handler.removeCallbacks(tick);
        handler.postDelayed(tick, MOVE_MS);
        fetchWeatherIfStale();
    }

    void stop() {
        handler.removeCallbacks(tick);
    }

    private void moveClock() {
        content.animate().alpha(0f).setDuration(900).withEndAction(() -> {
            placeClock();
            content.animate().alpha(1f).setDuration(1200).start();
        }).start();
    }

    /** A random spot that keeps the whole clock on screen, with the glow centred behind it. */
    private void placeClock() {
        View parent = (View) content.getParent();
        int maxX = Math.max(0, parent.getWidth() - content.getWidth());
        int maxY = Math.max(0, parent.getHeight() - content.getHeight());
        float x = maxX * (0.1f + 0.8f * random.nextFloat());
        float y = maxY * (0.1f + 0.8f * random.nextFloat());
        content.setX(x);
        content.setY(y);
        if (parent.getWidth() > 0 && parent.getHeight() > 0) {
            glow.setGradientCenter((x + content.getWidth() / 2f) / parent.getWidth(),
                    (y + content.getHeight() / 2f) / parent.getHeight());
        }
    }

    private void shiftGlow() {
        int from = glowColor;
        colorIndex = (colorIndex + 1) % PALETTE.length;
        ValueAnimator anim = ValueAnimator.ofObject(new ArgbEvaluator(), from, PALETTE[colorIndex]);
        anim.setDuration(8000);
        anim.addUpdateListener(a -> setGlow((int) a.getAnimatedValue()));
        anim.start();
    }

    private void setGlow(int color) {
        glowColor = color;
        int c = color & 0x00FFFFFF;
        glow.setColors(new int[]{0x40000000 | c, 0x14000000 | c, 0x00000000 | c});
    }
}
