package com.if3games.admanager.ads.adapters;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import com.chartboost.sdk.CBLocation;
import com.chartboost.sdk.Chartboost;
import com.chartboost.sdk.ChartboostDelegate;
import com.chartboost.sdk.Model.CBError;
import com.if3games.admanager.ads.controllers.AdsListener;
import com.if3games.admanager.ads.utils.Logger;

import org.json.JSONObject;

/**
 * Created by supergoodd on 03.10.15.
 */
public class ChartboostAdapter implements AdapterInterface {
    private static ChartboostAdapter instance;
    private AdsListener mInterstitialListener;

    private ChartboostDelegate adListener = new ChartboostDelegate() {
        /**
         * Interstitial methods
         */
        // Called after an interstitial has been displayed on the screen.
        public void didDisplayInterstitial(String location) {
            mInterstitialListener.onOpened(getAdName());
        }

        // Called after an interstitial has been loaded from the Chartboost API
        // servers and cached locally.
        public void didCacheInterstitial(String location) {
            mInterstitialListener.onLoaded(getAdName());
        }

        // Called after an interstitial has attempted to load from the Chartboost API
        // servers but failed.
        public void didFailToLoadInterstitial(String location, CBError.CBImpressionError error) {
            mInterstitialListener.onFailedToLoad(getAdName());
        }

        // Called after an interstitial has been dismissed.
        public void didDismissInterstitial(String location) {
            mInterstitialListener.onClosed(getAdName());
        }

        // Called after an interstitial has been closed.
        public void didCloseInterstitial(String location) {
            mInterstitialListener.onClosed(getAdName());
        }

        // Called after an interstitial has been clicked.
        public void didClickInterstitial(String location) {
            mInterstitialListener.onClicked(getAdName());
        }
    };

    public static synchronized ChartboostAdapter getInstance(AdsListener listener) {
        if (instance == null) {
            instance = new ChartboostAdapter();
            instance.mInterstitialListener = listener;
        }
        return instance;
    }

    @Override
    public String getAdName() {
        return "chartboost";
    }

    public static String getName() {
        return "chartboost";
    }

    @Override
    public void initAd(Context context, JSONObject params) {
        String appId = null;
        String appSign = null;
        try {
            appId = params.getString("cb_appId"); //ConstantsManager.getInstance().getConstants().CHARBOOST_APPID;
            appSign = params.getString("cb_appSigh"); //ConstantsManager.getInstance().getConstants().CHARBOOST_APPSIGH;
            Log.d("CB_Inter : appId", appId == null ? "null" : appId);
            Log.d("CB_Inter : appSigh", appSign == null ? "null" : appSign);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (appId == null || appSign == null) {
            return;
        }
        Chartboost.startWithAppId((Activity)context, appId, appSign);
        Chartboost.setDelegate(adListener);
        Chartboost.onCreate((Activity) context);
        Chartboost.onStart((Activity) context);
        Chartboost.cacheInterstitial(CBLocation.LOCATION_DEFAULT);
        Logger.log("initialized chartboost");
    }

    @Override
    public boolean isAvailable() {
        try  {
            Class.forName("com.chartboost.sdk.CBImpressionActivity");
            return true;
        }  catch (final ClassNotFoundException e) {
            return false;
        }
    }

    @Override
    public boolean isCached() {
        return false;
    }

    @Override
    public boolean isAutoLoadingVideo() {
        return false;
    }

    @Override
    public void showInterstitial() {
        Chartboost.showInterstitial(CBLocation.LOCATION_DEFAULT);
    }

    @Override
    public void showVideo() {}

    @Override
    public void onStart(Context context) {
        Chartboost.onStart((Activity) context);
    }

    @Override
    public void onResume(Context context) {
        Chartboost.onResume((Activity) context);
    }

    @Override
    public void onPause(Context context) {
        Chartboost.onPause((Activity) context);
    }

    @Override
    public void onStop(Context context) {
        Chartboost.onStop((Activity) context);
    }

    @Override
    public void onDestroy(Context context) {
        Chartboost.onDestroy((Activity)context);
    }

    @Override
    public void onBackPressed() {
        Chartboost.onBackPressed();
    }
}
