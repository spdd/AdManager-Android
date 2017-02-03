package com.if3games.admanager.ads.adapters;

import android.content.Context;

import com.if3games.admanager.ads.config.AdUnit;

/**
 * Created by supergoodd on 01.10.15.
 */
public interface AdapterInterface {
    String getAdName();
    void initAd(Context context, AdUnit params);
    boolean isAvailable();
    boolean isCached();
    boolean isAutoLoadingVideo();
    void showInterstitial();
    void showVideo();
    void onStart(Context context);
    void onResume(Context context);
    void onPause(Context context);
    void onStop(Context context);
    void onDestroy(Context context);
    void onBackPressed();
}


