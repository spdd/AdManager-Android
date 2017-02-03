package com.if3games.admanager.ads.common;

import android.content.Context;
import android.util.Log;

import com.if3games.admanager.ads.AdsConstants;
import com.if3games.admanager.ads.adapters.AdColonyAdapter;
import com.if3games.admanager.ads.adapters.AdMobAdapter;
import com.if3games.admanager.ads.adapters.AdapterInterface;
import com.if3games.admanager.ads.adapters.ChartboostAdapter;
import com.if3games.admanager.ads.adapters.ChartboostVideoAdapter;
import com.if3games.admanager.ads.adapters.UnityAdsAdapter;
import com.if3games.admanager.ads.config.ConfigLoader;
import com.if3games.admanager.ads.config.FirebaseConfigLoader;
import com.if3games.admanager.ads.controllers.AdsListener;
import com.if3games.admanager.ads.utils.SettingsManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

/**
 * Created by supergoodd on 07.10.15.
 */
public class InstanceFactory {
    private static InstanceFactory instance;

    public static synchronized InstanceFactory getInstance() {
        if (instance == null) {
            instance = new InstanceFactory();
        }
        return instance;
    }

    public HashMap<String, AdapterInterface> createInterstitialAdapters(final AdsListener listener) {
        HashMap<String, AdapterInterface> adapters = new HashMap<String, AdapterInterface>(){{
            put(AdMobAdapter.getName(), AdMobAdapter.getInstance(listener));
            put(ChartboostAdapter.getName(), ChartboostAdapter.getInstance(listener));
        }};
        return adapters;
    }

    public HashMap<String, AdapterInterface> createVideoAdapters(final AdsListener listener) {
        HashMap<String, AdapterInterface> adapters = new HashMap<String, AdapterInterface>(){{
            put(ChartboostAdapter.getName(), ChartboostVideoAdapter.getInstance(listener));
            put(AdColonyAdapter.getName(), AdColonyAdapter.getInstance(listener));
            put(UnityAdsAdapter.getName(), UnityAdsAdapter.getInstance(listener));
        }};
        return adapters;
    }

    public AdAgent createAdAgent(AdAgent.AdAgentListener listener, AdAgent.AdType adType) {
        return new AdAgent(listener, adType);
    }

    public ConfigLoader createConfigLoader(Context contex, ConfigLoader.Listener listener) {
        try  {
            Class.forName("com.google.firebase.remoteconfig.FirebaseRemoteConfig");
            return new FirebaseConfigLoader(contex, listener);
        }  catch (final ClassNotFoundException e) {
            Log.d("ADMANAGER" , "Firebase Not found");
            return new ConfigLoader(contex, listener);
        }
        //return new ConfigLoader(contex, listener);
    }

    public String createRecServer(AdsConstants.ServerType serverType) {
        switch (serverType) {
            case PARSE:
                return "https://api.parse.com/1/config";
            case SERVER:
                try {
                    return getServerFromUnityConfig();
                } catch (JSONException e) {
                    e.printStackTrace();
                    return null;
                }
            default:
                return "https://raw.githubusercontent.com/spdd/testAds/master/recommended.json";
        }
    }

    private String getServerFromUnityConfig() throws JSONException {
        String json = SettingsManager.getStringValue(SettingsManager.ADCONFIG_UNITY_KEY);
        JSONObject obj = new JSONObject(json);
        return obj.getString("config_url");
    }
}
