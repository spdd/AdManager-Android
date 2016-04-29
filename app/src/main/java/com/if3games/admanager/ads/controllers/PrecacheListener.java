package com.if3games.admanager.ads.controllers;

/**
 * Created by supergoodd on 01.10.15.
 */
public interface PrecacheListener {
    void onPrecacheLoaded(String adname);
    void onPrecacheFailedToLoad(String adname);
    void onPrecacheClicked(String adname);
    void onPrecacheClosed(String adname);
    void onPrecacheOpened(String adname);
}
