package com.if3games.admanager.ads.adapters;

import android.app.Activity;
import android.content.Context;

import com.if3games.admanager.ads.ParamsManager;
import com.if3games.admanager.ads.config.AdUnit;
import com.if3games.admanager.ads.controllers.AdsListener;
import com.unity3d.ads.IUnityAdsListener;
import com.unity3d.ads.UnityAds;

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
    public void initAd(Context context, AdUnit params) {
        String appId = params.unity_ads_id; //.getString("unity_ads_id"); //ConstantsManager.getInstance().getConstants().UNITYADS_APPID; //
        if (!isInitialized) {
            UnityAds.initialize((Activity) context, appId, this); //.init((Activity) context, appId, this);
            isInitialized = true;
        }
        if (ParamsManager.getInstance().isTestMode()) {
            UnityAds.setDebugMode(true);
        }
        if (longLoadAdCounter > 0) {
            if (UnityAds.isReady("rewardedVideo")) {
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
            Class.forName("com.unity3d.ads.adunit.AdUnitActivity");
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
    public void showVideo(Context context) {
        try {
            if(UnityAds.isReady("rewardedVideo")) {
                UnityAds.show((Activity) context, "rewardedVideo");
                longLoadAdCounter++;
            } else {
                if (mListener != null)
                    mListener.onFailedToLoad(getAdName());
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
/*
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
    */

    /**
     * UnityAds 2.x listener
     */

    @Override
    public void onUnityAdsReady(String s) {
        longLoadAdCounter++;
        isVideoCached = true;
        if (mListener != null)
            mListener.onLoaded(getAdName());
    }

    @Override
    public void onUnityAdsStart(String s) {
        if (mListener != null)
            mListener.onOpened(getAdName());
    }

    @Override
    public void onUnityAdsFinish(String s, UnityAds.FinishState finishState) {
        if (mListener != null && finishState == UnityAds.FinishState.COMPLETED)
            mListener.onFinished(getAdName());
    }

    @Override
    public void onUnityAdsError(UnityAds.UnityAdsError unityAdsError, String s) {
        isVideoCached = false;
        if (mListener != null)
            mListener.onFailedToLoad(getAdName());
    }
}
