package com.if3games.admanager.ads.adapters;

import android.app.Activity;
import android.content.Context;

import com.if3games.admanager.ads.AdsConstants;
import com.if3games.admanager.ads.controllers.AdsListener;
import com.unity3d.ads.android.IUnityAdsListener;
import com.unity3d.ads.android.UnityAds;

import org.json.JSONObject;

/**
 * Created by supergoodd on 07.10.15.
 */
public class UnityAdsAdapter implements AdapterInterface, IUnityAdsListener {
    private static UnityAdsAdapter instance;
    private AdsListener mListener;
    private boolean isVideoCached = false;
    private boolean isInitialized = false;
    private int longLoadAdCounter = 0;

    public static synchronized UnityAdsAdapter getInstance(AdsListener listener) {
        if (instance == null) {
            instance = new UnityAdsAdapter();
            instance.mListener = listener;
        }
        return instance;
    }

    public static String getName() {
        return "unity_ads";
    }

    @Override
    public String getAdName() {
        return "unity_ads";
    }

    @Override
    public void initAd(Context context, JSONObject params) {
        String appId = null;
        try {
            appId = params.getString("unity_ads_id"); //ConstantsManager.getInstance().getConstants().UNITYADS_APPID; //
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (!isInitialized) {
            UnityAds.init((Activity) context, appId, this);
            isInitialized = true;
        }
        UnityAds.changeActivity((Activity)context);
        if (AdsConstants.DEBUG_SDK == 1) {
            UnityAds.setDebugMode(true);
            UnityAds.setTestMode(true);
        }
        if (longLoadAdCounter > 0) {
            if (UnityAds.canShow() && UnityAds.canShowAds()) {
                mListener.onLoaded(getAdName());
                UnityAds.setListener(this);
            } else {
                mListener.onFailedToLoad(getAdName());
            }
        }
    }

    @Override
    public boolean isAvailable() {
        try  {
            Class.forName("com.unity3d.ads.android.view.UnityAdsFullscreenActivity");
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
        return true;
    }

    @Override
    public void showInterstitial() {

    }

    @Override
    public void showVideo() {
        try {
            if(UnityAds.canShow() && UnityAds.canShowAds()) {
                UnityAds.show();
                longLoadAdCounter++;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onStart(Context context) {

    }

    @Override
    public void onResume(Context context) {
        UnityAds.changeActivity((Activity)context);
    }

    @Override
    public void onPause(Context context) {

    }

    @Override
    public void onStop(Context context) {

    }

    @Override
    public void onDestroy(Context context) {

    }

    @Override
    public void onBackPressed() {

    }

    /**
     * Unity Ads listener implementation
     */

    @Override
    public void onHide() {
        mListener.onClosed(getAdName());
    }

    @Override
    public void onShow() {
    }

    @Override
    public void onVideoStarted() {
        mListener.onOpened(getAdName());
    }

    @Override
    public void onVideoCompleted(String s, boolean skipped) {
        if (!skipped)
            mListener.onFinished(getAdName());
    }

    @Override
    public void onFetchCompleted() {
        mListener.onLoaded(getAdName());
        longLoadAdCounter++;
        isVideoCached = true;
    }

    @Override
    public void onFetchFailed() {
        mListener.onFailedToLoad(getAdName());
        isVideoCached = false;
    }
}
