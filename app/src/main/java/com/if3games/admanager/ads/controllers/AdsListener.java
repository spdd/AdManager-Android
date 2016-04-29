package com.if3games.admanager.ads.controllers;

/**
 * Created by supergoodd on 30.09.15.
 */
public interface AdsListener {
    void onLoaded(String adname);
    void onFailedToLoad(String adname);
    void onClicked(String adname);
    void onClosed(String adname);
    void onOpened(String adname);
    void onFinished(String adname);
}
