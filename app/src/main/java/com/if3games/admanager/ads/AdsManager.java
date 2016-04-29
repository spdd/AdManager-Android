package com.if3games.admanager.ads;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.widget.Toast;

import com.if3games.admanager.ads.controllers.InterstitialController;
import com.if3games.admanager.ads.controllers.VideoController;
import com.if3games.admanager.ads.utils.SettingsManager;

import org.json.JSONException;

/**
 * Created by supergoodd on 30.09.15.
 */
public class AdsManager extends Thread {
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

    private AdsManager() {
        super("AdsManager init thread");
    }

    public static void initialize(Context context, String adsConfig, boolean autocache) {
        if (instance == null) {
            instance = getInstance();
            instance.mContext = context;
            instance.autocache = autocache;
            SharedPreferences pref = context.getSharedPreferences("SETTINGS", Context.MODE_PRIVATE);
            pref.edit().putString(SettingsManager.ADCONFIG_UNITY_KEY, adsConfig).apply();
            Toast.makeText(context, adsConfig, Toast.LENGTH_SHORT).show();
            instance.start();
        }
    }

    public static void initialize(Context context, boolean autocache) {
        if (instance == null) {
            instance = getInstance();
            instance.mContext = context;
            instance.autocache = autocache;
            instance.start();
        }
    }

    public Context getAppContext() {
        return mContext;
    }

    @Override
    public void run() {
        // Moves the current Thread into the background
        android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);

        // init interstitial
        InterstitialController.initialize(mContext, autocache);
        // init video
        VideoController.initialize(mContext, autocache);

        // handle AdsManager thread looper
        /*
        try {
            Looper.prepare();
            mHandler = new Handler() {
                @Override
                public void handleMessage(Message msg) {
                    switch (msg.what) {
                        case 0:
                            InterstitialController.getInstance().cache((Context) msg.obj);
                            break;
                        case 1:
                            VideoController.getInstance().cache((Context) msg.obj);
                            break;
                        default:
                            break;
                    }
                }
            };
            Looper.loop();
        } catch (Throwable t) {
            Logger.log("Halted");
        }
        */
    }

    public static void cacheInterstitial(Context context) {
        //Message message = instance.mHandler.obtainMessage(0, context);
        //instance.mHandler.sendMessage(message);
        InterstitialController.getInstance().cache(context);
    }

    public static void cacheVideo(Context context) {
        //Message message = instance.mHandler.obtainMessage(1, context);
        //instance.mHandler.sendMessage(message);
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
