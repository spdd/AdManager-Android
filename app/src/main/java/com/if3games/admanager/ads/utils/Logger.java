package com.if3games.admanager.ads.utils;

import android.util.Log;

import com.if3games.admanager.ads.AdsConstants;
import com.if3games.admanager.ads.ParamsManager;
import com.if3games.admanager.ads.common.AdAgent;

/**
 * Created by supergoodd on 30.09.15.
 */
public class Logger {
    public final static String TAG = "AD_MANAGER";
    public static void logAds(AdAgent.AdType adType, String msg) {
        if (msg != null && ParamsManager.getInstance().isTestMode() && AdsConstants.LOG_FULL_BANNER == 1) {
            switch (adType) {
                case INTERSTITIAL:
                    Log.d(TAG + "/" + "INTERSTITIAL_ADS", msg);
                    break;
                case VIDEO:
                    Log.d(TAG + "/" + "VIDEO_ADS", msg);
                    break;
            }
        }
    }

    public static void log(String msg) {
        if (msg != null && ParamsManager.getInstance().isTestMode() && AdsConstants.LOG_INFO == 1)
            Log.d(TAG, msg);
    }
}
