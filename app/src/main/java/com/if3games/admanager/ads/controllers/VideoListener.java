package com.if3games.admanager.ads.controllers;

/**
 * Created by supergoodd on 30.09.15.
 */
public interface VideoListener {
    void onLoaded();
    void onFailedToLoad();
    void onClicked();
    void onClosed();
    void onOpened();
    void onFinished();
}
