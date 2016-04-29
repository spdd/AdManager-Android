package com.if3games.admanager.ads;

import com.if3games.admanager.BuildConfig;

/**
 * Created by supergoodd on 01.10.15.
 */
public class AdsConstants {
    public static final String SDK_VERSION      = "1.0";

    public static final int DEBUG_SDK           = BuildConfig.DEBUG ? 1 : 0;

    public static final int LOG_ERROR = 1;
    public static final int LOG_FULL_BANNER   = 1;
    public static final int LOG_SMALL_BANNER  = 0;
    public static final int LOG_VIDEO         = 1;
    public static final int LOG_DEBUG         = 1;
    public static final int LOG_INFO          = 1;

    public static final int REWARD_FOR_INSTALL = 2000;
    // Timers (sec.)
    public static final int INTERSTITIAL_PRECACHE_TIMEOUT_FOR_TIMER         = 2 * 1000;
    public static final int RESET_FL_COUNTER_PRECACHE_TIMEOUT_FOR_TIMER     = 120 * 1000; // 2 min
    public static final int RESET_FL_COUNTER_BANNER_TIMEOUT_FOR_TIMER       = 120 * 1000; // 2 min
    public static final int CACHE_REFRESH_TIMEOUT_FOR_TIMER                 = 1800 * 1000; // 30 min
    public static final int INTERSTITIAL_TIMEOUT_INTERVAL                   = 10 * 1000;
    public static final int VIDEO_TIMEOUT_INTERVAL                          = 20 * 1000;
    public static final int PRELOADER_TIMEOUT_FOR_TIMER                     = 3 * 1000;
    public static final int PRELOADER_TIMEOUT_FOR_TIMER_VIDEO               = 10 * 1000;
    public static final int SERVER_NOT_RESPONDING_TIMEOUT                   = 15 * 1000;
    // Timeouts for ad which loaded
    public static final int CONFIG_STORE_TIMEOUT                            = 12; // in hours
    public static final int ANTICLICKER_TIMEOUT                             = 24; // in hours

    public static enum ServerType { PARSE, SERVER, FILE }
    public static final ServerType SERVER_TYPE = ServerType.PARSE;
}
