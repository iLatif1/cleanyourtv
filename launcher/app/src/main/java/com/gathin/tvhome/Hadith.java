package com.gathin.tvhome;

import java.time.LocalDate;

/**
 * Hadith of the day: a small bundled list from Sahih al-Bukhari and Sahih Muslim, each with its
 * Arabic text as narrated and an English translation, both with their source. One is shown at a
 * time on Home and the Clock screensaver (config.xml, hadith_enabled), picked by day of year so it
 * changes at midnight and repeats on the same yearly schedule. No network, no state.
 */
final class Hadith {

    static final class Entry {
        final String arabic;
        final String arabicSource;
        final String english;
        final String englishSource;

        Entry(String arabic, String arabicSource, String english, String englishSource) {
            this.arabic = arabic;
            this.arabicSource = arabicSource;
            this.english = english;
            this.englishSource = englishSource;
        }
    }

    private Hadith() {
    }

    static final Entry[] ALL = {
            new Entry("إِنَّمَا الْأَعْمَالُ بِالنِّيَّاتِ، وَإِنَّمَا لِكُلِّ امْرِئٍ مَا نَوَى", "رواه البخاري ١",
                    "Actions are judged by intentions, and everyone will get what was intended.", "Sahih al-Bukhari 1"),
            new Entry("لَا يُؤْمِنُ أَحَدُكُمْ حَتَّى يُحِبَّ لِأَخِيهِ مَا يُحِبُّ لِنَفْسِهِ", "رواه البخاري ١٣",
                    "None of you truly believes until he loves for his brother what he loves for himself.", "Sahih al-Bukhari 13"),
            new Entry("الْمُسْلِمُ مَنْ سَلِمَ الْمُسْلِمُونَ مِنْ لِسَانِهِ وَيَدِهِ", "رواه البخاري ١٠",
                    "A Muslim is one from whose tongue and hand the Muslims are safe.", "Sahih al-Bukhari 10"),
            new Entry("مَنْ كَانَ يُؤْمِنُ بِاللَّهِ وَالْيَوْمِ الْآخِرِ فَلَا يُؤْذِ جَارَهُ، وَمَنْ كَانَ يُؤْمِنُ بِاللَّهِ وَالْيَوْمِ الْآخِرِ "
                    + "فَلْيُكْرِمْ ضَيْفَهُ، وَمَنْ كَانَ يُؤْمِنُ بِاللَّهِ وَالْيَوْمِ الْآخِرِ فَلْيَقُلْ خَيْرًا أَوْ لِيَصْمُتْ", "رواه البخاري ٦٠١٨",
                    "Whoever believes in Allah and the Last Day should not harm his neighbor, should honor his guest, "
                            + "and should speak good or remain silent.", "Sahih al-Bukhari 6018"),
            new Entry("لَيْسَ الشَّدِيدُ بِالصُّرَعَةِ، إِنَّمَا الشَّدِيدُ الَّذِي يَمْلِكُ نَفْسَهُ عِنْدَ الْغَضَبِ", "رواه البخاري ٦١١٤",
                    "The strong one is not the one who overcomes people by strength, but the one who controls himself when angry.", "Sahih al-Bukhari 6114"),
            new Entry("لَيْسَ الْغِنَى عَنْ كَثْرَةِ الْعَرَضِ، وَلَكِنَّ الْغِنَى غِنَى النَّفْسِ", "رواه البخاري ٦٤٤٦",
                    "Richness is not in having many possessions, but richness is being content with oneself.", "Sahih al-Bukhari 6446"),
            new Entry("إِنَّ خِيَارَكُمْ أَحَاسِنُكُمْ أَخْلَاقًا", "رواه البخاري ٦٠٣٥",
                    "The best among you are those best in character.", "Sahih al-Bukhari 6035"),
            new Entry("الدِّينُ النَّصِيحَةُ", "رواه مسلم ٥٥",
                    "Religion is sincerity.", "Sahih Muslim 55"),
            new Entry("لَا يُؤْمِنُ أَحَدُكُمْ حَتَّى أَكُونَ أَحَبَّ إِلَيْهِ مِنْ وَالِدِهِ وَوَلَدِهِ وَالنَّاسِ أَجْمَعِينَ", "رواه البخاري ١٥",
                    "None of you truly believes until I am dearer to him than his father, his children, and all mankind.", "Sahih al-Bukhari 15"),
            new Entry("مَنْ يُرِدِ اللَّهُ بِهِ خَيْرًا يُفَقِّهْهُ فِي الدِّينِ", "رواه البخاري ٧١",
                    "Whoever Allah wishes good for, He gives him understanding of the religion.", "Sahih al-Bukhari 71"),
            new Entry("يَسِّرُوا وَلَا تُعَسِّرُوا، وَبَشِّرُوا وَلَا تُنَفِّرُوا", "رواه البخاري ٦٩",
                    "Make things easy and do not make them difficult; give glad tidings and do not repel people.", "Sahih al-Bukhari 69"),
            new Entry("مَنْ دَلَّ عَلَى خَيْرٍ فَلَهُ مِثْلُ أَجْرِ فَاعِلِهِ", "رواه مسلم ١٨٩٣",
                    "Whoever guides someone to goodness has a reward like the one who did it.", "Sahih Muslim 1893"),
            new Entry("تَرَى الْمُؤْمِنِينَ فِي تَرَاحُمِهِمْ وَتَوَادِّهِمْ وَتَعَاطُفِهِمْ كَمَثَلِ الْجَسَدِ، إِذَا اشْتَكَى عُضْوًا "
                    + "تَدَاعَى لَهُ سَائِرُ جَسَدِهِ بِالسَّهَرِ وَالْحُمَّى", "رواه البخاري ٦٠١١",
                    "The believers, in their mutual mercy, love and compassion, are like one body: if one part suffers, "
                            + "the whole body responds with fever and sleeplessness.", "Sahih al-Bukhari 6011"),
            new Entry("الْحَيَاءُ شُعْبَةٌ مِنَ الْإِيمَانِ", "رواه البخاري ٩",
                    "Modesty is a branch of faith.", "Sahih al-Bukhari 9"),
            new Entry("حَقُّ الْمُسْلِمِ عَلَى الْمُسْلِمِ خَمْسٌ: رَدُّ السَّلَامِ، وَعِيَادَةُ الْمَرِيضِ، وَاتِّبَاعُ الْجَنَائِزِ، "
                    + "وَإِجَابَةُ الدَّعْوَةِ، وَتَشْمِيتُ الْعَاطِسِ", "رواه البخاري ١٢٤٠",
                    "A Muslim has five rights over another Muslim: to return the greeting, visit the sick, follow the funeral, "
                            + "accept the invitation, and bless one who sneezes.", "Sahih al-Bukhari 1240"),
            new Entry("مَنْ نَفَّسَ عَنْ مُؤْمِنٍ كُرْبَةً مِنْ كُرَبِ الدُّنْيَا نَفَّسَ اللَّهُ عَنْهُ كُرْبَةً مِنْ كُرَبِ يَوْمِ الْقِيَامَةِ", "رواه مسلم ٢٦٩٩",
                    "Whoever relieves a believer's hardship in this world, Allah will relieve his hardship on the Day of Judgment.", "Sahih Muslim 2699"),
            new Entry("خَيْرُكُمْ مَنْ تَعَلَّمَ الْقُرْآنَ وَعَلَّمَهُ", "رواه البخاري ٥٠٢٧",
                    "The best among you are those who learn the Qur'an and teach it.", "Sahih al-Bukhari 5027"),
            new Entry("مَا نَقَصَتْ صَدَقَةٌ مِنْ مَالٍ", "رواه مسلم ٢٥٨٨",
                    "Charity does not decrease wealth.", "Sahih Muslim 2588"),
            new Entry("إِنَّ اللَّهَ لَا يَنْظُرُ إِلَى أَجْسَادِكُمْ وَلَا إِلَى صُوَرِكُمْ، وَلَكِنْ يَنْظُرُ إِلَى قُلُوبِكُمْ", "رواه مسلم ٢٥٦٤",
                    "Allah does not look at your bodies or your appearances, but looks at your hearts.", "Sahih Muslim 2564"),
            new Entry("مَنْ سَتَرَ مُسْلِمًا سَتَرَهُ اللَّهُ فِي الدُّنْيَا وَالْآخِرَةِ", "رواه مسلم ٢٦٩٩",
                    "Whoever conceals the faults of a Muslim, Allah will conceal his faults in this world and the Hereafter.", "Sahih Muslim 2699"),
            new Entry("حُجِبَتِ النَّارُ بِالشَّهَوَاتِ، وَحُجِبَتِ الْجَنَّةُ بِالْمَكَارِهِ", "رواه البخاري ٦٤٨٧",
                    "Paradise is surrounded by hardships, and the Fire is surrounded by desires.", "Sahih al-Bukhari 6487"),
            new Entry("مَثَلُ الَّذِي يَذْكُرُ رَبَّهُ وَالَّذِي لَا يَذْكُرُ مَثَلُ الْحَيِّ وَالْمَيِّتِ", "رواه البخاري ٦٤٠٧",
                    "The one who remembers his Lord and the one who does not are like the living and the dead.", "Sahih al-Bukhari 6407"),
            new Entry("لَا يَرْحَمُ اللَّهُ مَنْ لَا يَرْحَمُ النَّاسَ", "رواه البخاري ٧٣٧٦",
                    "Allah will not be merciful to those who are not merciful to people.", "Sahih al-Bukhari 7376"),
            new Entry("مَا سُئِلَ النَّبِيُّ ﷺ عَنْ شَيْءٍ قَطُّ فَقَالَ لَا", "رواه البخاري ٦٠٣٤",
                    "The Messenger of Allah was never asked for anything and said no.", "Sahih al-Bukhari 6034"),
            new Entry("الْيَدُ الْعُلْيَا خَيْرٌ مِنَ الْيَدِ السُّفْلَى", "رواه البخاري ١٤٢٩",
                    "The upper hand is better than the lower hand.", "Sahih al-Bukhari 1429"),
            new Entry("مَنْ يَتَصَبَّرْ يُصَبِّرْهُ اللَّهُ، وَلَنْ تُعْطَوْا عَطَاءً خَيْرًا وَأَوْسَعَ مِنَ الصَّبْرِ", "رواه البخاري ٦٤٧٠",
                    "Whoever remains patient, Allah will make him patient; no one is given a better or more abundant gift than patience.", "Sahih al-Bukhari 6470"),
            new Entry("كُلُّ مَعْرُوفٍ صَدَقَةٌ", "رواه مسلم ١٠٠٥",
                    "Every act of goodness is charity.", "Sahih Muslim 1005"),
            new Entry("الْكَلِمَةُ الطَّيِّبَةُ صَدَقَةٌ", "رواه البخاري ٢٩٨٩",
                    "A good word is charity.", "Sahih al-Bukhari 2989"),
            new Entry("لَا تَغْضَبْ", "رواه البخاري ٦١١٦",
                    "Do not become angry.", "Sahih al-Bukhari 6116"),
            new Entry("إِنَّ اللَّهَ رَفِيقٌ يُحِبُّ الرِّفْقَ فِي الْأَمْرِ كُلِّهِ", "رواه البخاري ٦٩٢٧",
                    "Allah is Gentle and loves gentleness in all matters.", "Sahih al-Bukhari 6927"),
            new Entry("أَحَبُّ الْأَعْمَالِ إِلَى اللَّهِ أَدْوَمُهَا وَإِنْ قَلَّ", "رواه البخاري ٦٤٦٤",
                    "The deeds most beloved to Allah are those done regularly, even if small.", "Sahih al-Bukhari 6464"),
    };

    /** The entry for today, picked by day of year so it changes at midnight. */
    static Entry today() {
        return ofDay(LocalDate.now().getDayOfYear());
    }

    /** Pure function for testing: the entry for a day of year (any int; wraps both ways). */
    static Entry ofDay(int dayOfYear) {
        return ALL[Math.floorMod(dayOfYear - 1, ALL.length)];
    }
}
