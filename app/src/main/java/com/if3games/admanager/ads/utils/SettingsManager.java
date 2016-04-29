package com.if3games.admanager.ads.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.if3games.admanager.ads.AdsManager;

/**
 * Created by supergoodd on 20.10.15.
 */
public class SettingsManager {
    private static final String FILE_SETTINGS = "SETTINGS";
    public static final String PARSE_LOAD_TIME = "PARSE_LOAD_TIME";
    public static final String ADCONFIG_PARSE_KEY = "adConfigParse";
    public static final String ADCONFIG_UNITY_KEY = "UNITY_ADS_CONFIG";
    public static final String ADCONFIG_URL_KEY = "adConfigUrl";
    public static final String SHOW_AD_FREQ_KEY = "showAdFreq";

    private static synchronized SharedPreferences getPreference() {
        return AdsManager.getInstance().getAppContext().getSharedPreferences(FILE_SETTINGS, Context.MODE_PRIVATE);
    }

    private static synchronized SharedPreferences.Editor getEditor() {
        return getPreference().edit();
    }

    public static synchronized String getStringValue(final String key) {
        return getPreference().getString(key, null);
    }

    public static synchronized int  getIntValue(final String key) {
        return getPreference().getInt(key, -1);
    }

    public static synchronized int  getIntValue(final String key, final int defValue) {
        return getPreference().getInt(key, defValue);
    }

    public static synchronized float getFloatValue(final String key) {
        return getPreference().getFloat(key, -1.0F);
    }

    public static synchronized long getLongValue(final String key) {
        return getPreference().getLong(key, -1);
    }

    public static synchronized boolean getBoolValue(final String key) {
        return getPreference().getBoolean(key, false);
    }

    public static synchronized void setStringValue(final String key, final String value) {
        getEditor().putString(key, value).apply();
    }

    public static synchronized void setIntValue(final String key, final int value) {
        getEditor().putInt(key, value).apply();
    }

    public static synchronized void setFloatValue(final String key, final float value) {
        getEditor().putFloat(key, value).apply();
    }

    public static synchronized void setLongValue(final String key, final long value) {
        getEditor().putLong(key, value).apply();
    }

    public static synchronized void setBoolValue(final String key, final boolean value) {
        getEditor().putBoolean(key, value).apply();
    }
}