package com.if3games.admanager.ads.adapters;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import com.chartboost.sdk.CBLocation;
import com.chartboost.sdk.Chartboost;
import com.chartboost.sdk.ChartboostDelegate;
import com.chartboost.sdk.Model.CBError;
import com.if3games.admanager.ads.config.AdUnit;
import com.if3games.admanager.ads.controllers.AdsListener;
import com.if3games.admanager.ads.utils.Logger;

/**
 * Created by supergoodd on 03.10.15.
 */
public class ChartboostVideoAdapter implements AdapterInterface {
    private static ChartboostVideoAdapter instance;
    private AdsListener mVideoListener;
    private boolean isVideoCached = false;

    private ChartboostDelegate adListener = new ChartboostDelegate() {
        /**
         * Video methods
         */
        // Called after a rewarded video has been displayed on the screen.
        public void didDisplayRewardedVideo(String location) {
            if (mVideoListener != null) mVideoListener.onOpened(getAdName());
        }

        // Called after a rewarded video has been loaded from the Chartboost API
        // servers and cached locally.
        public void didCacheRewardedVideo(String location) {
            isVideoCached = true;
            if (mVideoListener != null) mVideoListener.onLoaded(getAdName());
        }

        // Called after a rewarded video has attempted to load from the Chartboost API
        // servers but failed.
        public void didFailToLoadRewardedVideo(String location, CBError.CBImpressionError error) {
            if (mVideoListener != null) mVideoListener.onFailedToLoad(getAdName());
        }

        // Called after a rewarded video has been dismissed.
        public void didDismissRewardedVideo(String location) {
            if (mVideoListener != null) mVideoListener.onClosed(getAdName());
        }

        // Called after a rewarded video has been closed.
        public void didCloseRewardedVideo(String location) {
            if (mVideoListener != null) mVideoListener.onClosed(getAdName());
        }

        // Called after a rewarded video has been clicked.
        public void didClickRewardedVideo(String location) {
            if (mVideoListener != null) mVideoListener.onClicked(getAdName());
        }

        // Called after a rewarded video has been viewed completely and user is eligible for reward.
        public void didCompleteRewardedVideo(String location, int reward) {
            if (mVideoListener != null) mVideoListener.onFinished(getAdName());
        }
    };

    public static synchronized ChartboostVideoAdapter getInstance(AdsListener listener) {
        if (instance == null) {
            instance = new ChartboostVideoAdapter();
            instance.mVideoListener = listener;
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
    public void initAd(Context context, AdUnit params) {
        String appId = null;
        String appSign = null;
        try {
            appId = params.cb_appId; //.getString("cb_appId"); //ConstantsManager.getInstance().getConstants().CHARBOOST_APPID;
            appSign = params.cb_appSigh; //.getString("cb_appSigh"); //ConstantsManager.getInstance().getConstants().CHARBOOST_APPSIGH;
            Log.d("CB_Video : appId", appId == null ? "null" : appId);
            Log.d("CB_Video : appSigh", appSign == null ? "null" : appSign);
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
        Chartboost.cacheRewardedVideo(CBLocation.LOCATION_DEFAULT);
        if (Chartboost.hasRewardedVideo(CBLocation.LOCATION_DEFAULT)) {
            isVideoCached = true;
        } else {
            isVideoCached = false;
        }
        Logger.log("initialized chartboost video");
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
        return isVideoCached;
    }

    @Override
    public boolean isAutoLoadingVideo() {
        return false;
    }

    @Override
    public void showInterstitial() {
    }

    @Override
    public void showVideo() {
        Chartboost.showRewardedVideo(CBLocation.LOCATION_DEFAULT);
        Chartboost.cacheRewardedVideo(CBLocation.LOCATION_DEFAULT);
    }

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