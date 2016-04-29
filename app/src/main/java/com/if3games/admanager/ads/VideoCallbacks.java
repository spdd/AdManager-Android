package com.if3games.admanager.ads;

/**
 * Created by supergoodd on 30.09.15.
 */
public interface VideoCallbacks {
    void onVideoLoaded();
    void onVideoFailedToLoad();
    void onVideoClicked();
    void onVideoClosed();
    void onVideoOpened();
    void onVideoFinished();
}
