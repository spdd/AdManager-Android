package com.if3games.admanager.ads.controllers;

import android.content.Context;
import android.util.Log;

import com.if3games.admanager.ads.VideoCallbacks;
import com.if3games.admanager.ads.adapters.AdapterInterface;
import com.if3games.admanager.ads.adapters.PrecacheAdapter;
import com.if3games.admanager.ads.common.AdAgent;
import com.if3games.admanager.ads.common.InstanceFactory;
import com.if3games.admanager.ads.utils.Logger;

import org.json.JSONException;

/**
 * Created by supergoodd on 30.09.15.
 */
public class VideoController extends BaseAdsController {
    private static VideoController instance;
    private VideoCallbacks callbacks;

    public static synchronized VideoController getInstance() {
        if (instance == null) {
            instance = new VideoController();
        }
        return instance;
    }

    public static void initialize(Context context, boolean autocache) {
        if (instance == null) {
            instance = getInstance();
            instance.mContext = context;
            instance.autocache = autocache;
            instance.controllerType = AdAgent.AdType.VIDEO;
            instance.controllerPrefix = "video";

            instance.adapterInstances = InstanceFactory.getInstance().createVideoAdapters(instance);
            instance.adsAgent = InstanceFactory.getInstance().createAdAgent(instance, instance.controllerType);
            instance.loadConfig();
            Logger.log("Video controller thread is: " + Thread.currentThread().getName());
            Logger.logAds(instance.controllerType, "Initialize video controller");
        }
    }

    public void setVideoCallbacks(VideoCallbacks callbacks) {
        this.callbacks = callbacks;
    }

    /**
     * Override superclass
     */
    @Override
    public boolean isLoaded() {
        return isLoaded;
    }

    @Override
    protected void showAd(Context context) throws JSONException {
        if (isLoaded) {
            Logger.logAds(controllerType, String.format("show banner status: %s", status));
            AdapterInterface adapter = adapterInstances.get(status);
            if(adapter != null && adapter.isCached()) {
                adapter.showVideo();
            } else if(adsAgent.getCachedVideo() != null) {
                AdapterInterface nextAdapter = adapterInstances.get(adsAgent.getCachedVideo());
                if(nextAdapter != null && nextAdapter.isCached())
                    nextAdapter.showVideo();
            }
        } else if(isPrecacheLoaded && !adsAgent.isPrecacheTmpDisabled) {
            Logger.logAds(controllerType, String.format("show precache banner status: %s", precacheStatus));
            PrecacheAdapter.getInstance(this).showVideo();
        } else {
            if (adsAgent.isPrecacheReady) {
                loadPrecache();
            }
            if (adsAgent.isAdsReady) {
                loadAd();
            } else {
                loadConfig();
            }
        }
    }

    /**
     * Internal ad management
     */
    @Override
    public void cache(Context context) {
        Logger.logAds(controllerType, "cache interstitial thread is: " + Thread.currentThread().getName());
        super.cache(context);
    }

    @Override
    protected void cacheNextAd() {
        Logger.logAds(controllerType, "cache next ad");
        adsAgent.getAdObject().cacheNextVideo();
        loadAd();
    }

    @Override
    protected void evokeFailedToLoadAd(AdapterInterface adapter) {
        if (adapter.isAutoLoadingVideo()) {
            scheduleFailedToLoadAd();
        }
    }

    @Override
    protected int getTimeIntervalForLoadSheduler() {
        return adsAgent.getAdObject().loaderTime;
    }

    /**
     * Implementation ad controller callbacks
     */
    @Override
    public void onLoaded(String adName) {
        super.onLoaded(adName);
        if (callbacks != null) {
            callbacks.onVideoLoaded();
        }
    }

    @Override
    public void onFailedToLoad(String adname) {
        super.onFailedToLoad(adname);
        if (callbacks != null) {
            if (isPrecacheLoaded) {
                callbacks.onVideoLoaded();
            } else {
                callbacks.onVideoFailedToLoad();
            }
        }
    }

    @Override
    public void onClicked(String adName) {
        if (!isClicked) {
            if (callbacks != null) {
                callbacks.onVideoClicked();
            }
        }
        super.onClicked(adName);
    }

    @Override
    public void onClosed(String adName) {
        if (!isClosed) {
            if (callbacks != null) {
                callbacks.onVideoClosed();
            }
        }
        super.onClosed(adName);
    }

    @Override
    public void onOpened(String adName) {
        if (!isShown) {
            if (callbacks != null) {
                callbacks.onVideoOpened();
            }
            adsAgent.getAdObject().didVideoShown(true);
        }
        super.onOpened(adName);
    }

    @Override
    public void onFinished(String adname) {
        super.onFinished(adname);
        if (callbacks != null) {
            callbacks.onVideoFinished();
        }
    }
}
