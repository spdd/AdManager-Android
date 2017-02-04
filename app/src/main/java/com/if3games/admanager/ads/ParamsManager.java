package com.if3games.admanager.ads;

import com.google.gson.Gson;
import com.if3games.admanager.ads.config.AdConfig;
import com.if3games.admanager.ads.utils.SettingsManager;

/**
 * Created by supergoodd on 03.02.17.
 */

public class ParamsManager {

    private static ParamsManager instance;

    private boolean testMode = false;
    private AdConfig adConfig;

    private final static String TEST_MODE_KEY = "test_mode_state";
    public final static String LAST_AD_CONFIG_TIME = "LAST_AD_CONFIG_TIME";

    public static synchronized ParamsManager getInstance() {
        if (instance == null) {
            instance = new ParamsManager();
            instance.testMode = SettingsManager.getBoolValue(TEST_MODE_KEY);
        }
        return instance;
    }

    public boolean isTestMode() {
        return testMode;
    }

    public void setTestMode(boolean testMode) {
        SettingsManager.setBoolValue(TEST_MODE_KEY, testMode);
        this.testMode = testMode;
    }

    public AdConfig getAdConfig() {
        if (adConfig == null) {
            String config = SettingsManager.getStringValue(LAST_AD_CONFIG_TIME);
            if(config != null) {
                Gson gson = new Gson();
                return gson.fromJson(config, AdConfig.class);
            }
            return null;
        }

        return adConfig;
    }

    public void setAdConfig(AdConfig adConfig) {
        this.adConfig = adConfig;
    }
}
