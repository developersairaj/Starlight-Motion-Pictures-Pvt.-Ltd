package com.example.starlight.utils;

    public class Constants {

        // ══════════════════════════════════════════════════════════
        //  TMDB API
        //  Get key: https://www.themoviedb.org/settings/api
        // ══════════════════════════════════════════════════════════
        public static final String TMDB_API_KEY  = "e8d8535c4f28cf931d1112e30d918379";
        public static final String TMDB_BASE_URL = "https://api.themoviedb.org/3/";
        public static final String TMDB_IMG_BASE = "https://image.tmdb.org/t/p/w500";
        public static final String TMDB_IMG_ORIG = "https://image.tmdb.org/t/p/original";

        // ══════════════════════════════════════════════════════════
        //  OMDB API — Real IMDb ratings
        //  Already working key below
        // ══════════════════════════════════════════════════════════
        public static final String OMDB_API_KEY  = "89fa7b76";
        public static final String OMDB_BASE_URL = "https://www.omdbapi.com/";

        // ══════════════════════════════════════════════════════════
        //  JIKAN API — Anime + Manga (MyAnimeList)
        //  100% Free — No API key needed!
        // ══════════════════════════════════════════════════════════
        public static final String JIKAN_BASE_URL = "https://api.jikan.moe/v4/";

        // ══════════════════════════════════════════════════════════
        //  Navigation Extras (Intent keys)
        // ══════════════════════════════════════════════════════════
        public static final String EXTRA_MEDIA_ID   = "extra_media_id";
        public static final String EXTRA_MEDIA_TYPE = "extra_media_type";
        public static final String EXTRA_MEDIA_NAME = "extra_media_name";

        // ══════════════════════════════════════════════════════════
        //  Media Types
        // ══════════════════════════════════════════════════════════
        public static final String TYPE_MOVIE   = "movie";
        public static final String TYPE_TV      = "tv";
        public static final String TYPE_CARTOON = "cartoon";
        public static final String TYPE_ANIME   = "anime";
        public static final String TYPE_MANGA   = "manga";

        // ══════════════════════════════════════════════════════════
        //  Main Tab Indices (Movies/Shows/etc screen)
        // ══════════════════════════════════════════════════════════
        public static final int TAB_TRENDING = 0;
        public static final int TAB_MOVIES   = 1;
        public static final int TAB_SHOWS    = 2;
        public static final int TAB_CARTOONS = 3;

        // ══════════════════════════════════════════════════════════
        //  Anime Screen Sub-Tab Indices
        // ══════════════════════════════════════════════════════════
        public static final int ANIME_TAB_TOP      = 0;
        public static final int ANIME_TAB_AIRING   = 1;
        public static final int ANIME_TAB_UPCOMING = 2;
        public static final int ANIME_TAB_MANGA    = 3;

        // ══════════════════════════════════════════════════════════
        //  Bottom Navigation IDs (used in MainActivity)
        // ══════════════════════════════════════════════════════════
        public static final int BOTTOM_NAV_HOME  = 0;
        public static final int BOTTOM_NAV_ANIME = 1;

        // ══════════════════════════════════════════════════════════
        //  Search
        // ══════════════════════════════════════════════════════════
        public static final long SEARCH_DEBOUNCE_MS = 400;

        // ══════════════════════════════════════════════════════════
        //  Shared Preferences
        // ══════════════════════════════════════════════════════════
        public static final String PREFS_REVIEWS = "starlight_reviews";

        // ══════════════════════════════════════════════════════════
        //  TMDB Genre IDs
        // ══════════════════════════════════════════════════════════
        public static final int GENRE_ANIMATION = 16;
        public static final int GENRE_ACTION    = 28;
        public static final int GENRE_COMEDY    = 35;
        public static final int GENRE_DRAMA     = 18;
        public static final int GENRE_THRILLER  = 53;
        public static final int GENRE_HORROR    = 27;
        public static final int GENRE_ROMANCE   = 10749;
        public static final int GENRE_SCIFI     = 878;

        // ══════════════════════════════════════════════════════════
        //  Language Codes (ISO 639-1)
        // ══════════════════════════════════════════════════════════

        // Indian Languages
        public static final String LANG_HINDI     = "hi";
        public static final String LANG_TAMIL     = "ta";
        public static final String LANG_TELUGU    = "te";
        public static final String LANG_BENGALI   = "bn";
        public static final String LANG_MARATHI   = "mr";
        public static final String LANG_MALAYALAM = "ml";
        public static final String LANG_KANNADA   = "kn";
        public static final String LANG_PUNJABI   = "pa";

        // International Languages
        public static final String LANG_ENGLISH   = "en";
        public static final String LANG_KOREAN    = "ko";
        public static final String LANG_JAPANESE  = "ja";
        public static final String LANG_SPANISH   = "es";
        public static final String LANG_FRENCH    = "fr";
        public static final String LANG_TURKISH   = "tr";
        public static final String LANG_ARABIC    = "ar";
        public static final String LANG_GERMAN    = "de";
        public static final String LANG_ITALIAN   = "it";
        public static final String LANG_PORTUGUESE= "pt";
        public static final String LANG_CHINESE   = "zh";
        public static final String LANG_THAI      = "th";

        // ══════════════════════════════════════════════════════════
        //  Streaming Platforms by Language
        // ══════════════════════════════════════════════════════════
        public static final String STREAM_HINDI    =
                "Netflix • Prime • JioCinema • Hotstar";
        public static final String STREAM_TAMIL    =
                "Hotstar • Sun NXT • Aha • Prime";
        public static final String STREAM_TELUGU   =
                "Hotstar • Aha • Prime • ZEE5";
        public static final String STREAM_BENGALI  =
                "Hoichoi • ZEE5 • Hotstar";
        public static final String STREAM_MALAYALAM=
                "SonyLIV • Hotstar • Prime";
        public static final String STREAM_ENGLISH  =
                "Netflix • Prime • Apple TV+ • Hotstar";
        public static final String STREAM_KOREAN   =
                "Netflix • Viki • Prime";
        public static final String STREAM_JAPANESE =
                "Crunchyroll • Netflix • Funimation";
        public static final String STREAM_ANIME    =
                "Crunchyroll • Netflix • Funimation • Bilibili";
        public static final String STREAM_DEFAULT  =
                "Netflix • Prime • Hotstar";

        // ══════════════════════════════════════════════════════════
        //  Jikan Anime Genre IDs (MyAnimeList)
        // ══════════════════════════════════════════════════════════
        public static final int JIKAN_GENRE_ACTION    = 1;
        public static final int JIKAN_GENRE_ADVENTURE = 2;
        public static final int JIKAN_GENRE_COMEDY    = 4;
        public static final int JIKAN_GENRE_DRAMA     = 8;
        public static final int JIKAN_GENRE_FANTASY   = 10;
        public static final int JIKAN_GENRE_HORROR    = 14;
        public static final int JIKAN_GENRE_ROMANCE   = 22;
        public static final int JIKAN_GENRE_SCIFI     = 24;
        public static final int JIKAN_GENRE_SPORTS    = 30;
        public static final int JIKAN_GENRE_SHONEN    = 27;
        public static final int JIKAN_GENRE_SHOJO     = 25;
        public static final int JIKAN_GENRE_SEINEN    = 42;
        public static final int JIKAN_GENRE_ISEKAI    = 62;

        // ══════════════════════════════════════════════════════════
        //  Pagination
        // ══════════════════════════════════════════════════════════
        public static final int TMDB_DEFAULT_PAGE  = 1;
        public static final int JIKAN_DEFAULT_LIMIT = 25;

        // ══════════════════════════════════════════════════════════
        //  Image Search
        //  Get free key: console.cloud.google.com
        //  (1000 requests/month free)
        // ══════════════════════════════════════════════════════════
        public static final String GOOGLE_VISION_KEY = "AIzaSyCgJ6MCaUH6a3erUC8v7NqbH-7o6ukMQtI";
    }