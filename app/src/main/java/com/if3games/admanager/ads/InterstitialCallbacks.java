package com.if3games.admanager.ads;

/**
 * Created by supergoodd on 30.09.15.
 */
public interface InterstitialCallbacks {
    void onInterstitialLoaded();
    void onInterstitialFailedToLoad();
    void onInterstitialClicked();
    void onInterstitialClosed();
    void onInterstitialOpened();
}
