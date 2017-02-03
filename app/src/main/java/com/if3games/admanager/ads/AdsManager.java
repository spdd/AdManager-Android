package com.if3games.admanager.ads;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Debug;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.if3games.admanager.ads.controllers.InterstitialController;
import com.if3games.admanager.ads.controllers.VideoController;
import com.if3games.admanager.ads.utils.SettingsManager;

import org.json.JSONException;

/**
 * Created by supergoodd on 30.09.15.
 */
public class AdsManager {
    private static AdsManager instance;
    private Context mContext;
    private boolean autocache;
    private Handler mHandler;

    public static synchronized AdsManager getInstance() {
        if (instance == null) {
            instance = new AdsManager();
        }
        return instance;
    }

    private AdsManager() {}

    public static void initialize(Context context, String adsConfig, boolean autocache) {
        if (instance == null) {
            instance = getInstance();
            instance.mContext = context;
            instance.autocache = autocache;
            SharedPreferences pref = context.getSharedPreferences("SETTINGS", Context.MODE_PRIVATE);
            pref.edit().putString(SettingsManager.ADCONFIG_UNITY_KEY, adsConfig).apply();
            // init interstitial
            InterstitialController.initialize(context, autocache);
            // init video
            VideoController.initialize(context, autocache);
            Log.d("ANDROID UNITY_ADS", adsConfig);
        }
    }

    public static void initialize(Context context, boolean autocache) {
        if (instance == null) {
            instance = getInstance();
            instance.mContext = context;
            instance.autocache = autocache;
        }
    }

    public Context getAppContext() {
        return mContext;
    }

    public static void cacheInterstitial(Context context) {
        InterstitialController.getInstance().cache(context);
    }

    public static void cacheVideo(Context context) {
        VideoController.getInstance().cache(context);
    }

    /**
     * Interstitial section
     */

    public static void showInterstitial(Context context) {
        try {
            InterstitialController.getInstance().show(context);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public static boolean isInterstitialLoaded() {
        return InterstitialController.getInstance().isLoaded();
    }

    /**
     * Video section
     */

    public static void showVideo(Context context) {
        try {
            Log.d("ANDROID UNITY_ADS", "showVideo");
            VideoController.getInstance().show(context);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public static void showRewardInterstitial() {
        InterstitialController.getInstance().showReward();
    }

    public static boolean isVideoLoaded() {
        return VideoController.getInstance().isLoaded();
    }


    public static void setInterstitialCallbacks(InterstitialCallbacks callbacks) {
        InterstitialController.getInstance().setInterstitialCallbacks(callbacks);
    }

    public static void setVideoCallbacks(VideoCallbacks callbacks) {
        VideoController.getInstance().setVideoCallbacks(callbacks);
    }

    public static void setUserCallbacks(UserCallbacks callbacks) {
        VideoController.getInstance().setCallbacks(callbacks);
        InterstitialController.getInstance().setCallbacks(callbacks);
    }

    public static void onStart(Context context) {
        InterstitialController.getInstance().onStart(context);
        VideoController.getInstance().onStart(context);
    }

    public static void onStop(Context context) {
        InterstitialController.getInstance().onStop(context);
        VideoController.getInstance().onStop(context);
    }

    public static void onDestroy(Context context) {
        InterstitialController.getInstance().onDestroy(context);
        VideoController.getInstance().onDestroy(context);
    }

    public static boolean onBackPressed(Context context) {
        if (InterstitialController.getInstance().isOpened()) {
            InterstitialController.getInstance().onBackPressed(context);
            return true;
        }
        if (VideoController.getInstance().isOpened()) {
            VideoController.getInstance().onBackPressed(context);
            return true;
        }
        return false;
    }

    public static void onPause(Context context) {
        InterstitialController.getInstance().onPause(context);
        VideoController.getInstance().onPause(context);
    }

    public static void onResume(Context context) {
        InterstitialController.getInstance().onResume(context);
        VideoController.getInstance().onResume(context);
    }
}
