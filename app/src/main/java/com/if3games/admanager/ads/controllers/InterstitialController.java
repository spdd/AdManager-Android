package com.if3games.admanager.ads.controllers;

import android.app.Activity;
import android.content.Context;

import com.if3games.admanager.ads.AdsConstants;
import com.if3games.admanager.ads.InterstitialCallbacks;
import com.if3games.admanager.ads.adapters.AdapterInterface;
import com.if3games.admanager.ads.common.AdAgent;
import com.if3games.admanager.ads.common.InstanceFactory;
import com.if3games.admanager.ads.utils.Logger;

/**
 * Created by supergoodd on 30.09.15.
 */
public class InterstitialController extends BaseAdsController {
    private static InterstitialController instance;
    private InterstitialCallbacks callbacks;

    /**
     * public methods
     */
    public static synchronized InterstitialController getInstance() {
        if (instance == null) {
            instance = new InterstitialController();
        }
        return instance;
    }

    public static void initialize(Context context, boolean autocache) {
        if (instance == null) {
            instance = getInstance();
            instance.mContext = context;
            instance.autocache = autocache;
            instance.controllerType = AdAgent.AdType.INTERSTITIAL;
            instance.controllerPrefix = "interstitial";

            instance.adapterInstances = InstanceFactory.getInstance().createInterstitialAdapters(instance);
            instance.adsAgent = InstanceFactory.getInstance().createAdAgent(instance, instance.controllerType);
            instance.loadConfig();
            Logger.log("Interstitial controller thread is: " + Thread.currentThread().getName());
            Logger.logAds(instance.controllerType, "Initialize interstitial controller");
        }
    }

    public void setInterstitialCallbacks(InterstitialCallbacks callbacks) {
        this.callbacks = callbacks;
    }

    public void showReward() {
        //PrecacheAdapter.getInstance(this).showRewardedInterstitial(mContext);
    }

    /**
     * Override superclass
     */

    @Override
    public void cache(Context context) {
        Logger.logAds(controllerType, "cache interstitial thread is: " + Thread.currentThread().getName());
        super.cache(context);
    }

    @Override
    protected void showAd(Context context) {
        Logger.log("Show Interstitial in thread : " + Thread.currentThread().getName());
        if (isLoaded) {
            Logger.logAds(controllerType, String.format("show banner status: %s", status));
            final AdapterInterface adapter = adapterInstances.get(status);
            if(adapter != null) {
                //scheduleShowPrecacheTimer();
                ((Activity)mContext).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        adapter.showInterstitial();
                    }
                });
            }
        } else if (isPrecacheLoaded && !adsAgent.isPrecacheTmpDisabled) {
            Logger.logAds(controllerType, String.format("show precache banner status: %s", precacheStatus));
            ((Activity)mContext).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    //PrecacheAdapter.getInstance(InterstitialController.this).showInterstitial();
                }
            });
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
    protected void evokeFailedToLoadAd(AdapterInterface adapter) {
        scheduleFailedToLoadAd();
    }

    @Override
    protected int getTimeIntervalForLoadSheduler() {
        return AdsConstants.INTERSTITIAL_TIMEOUT_INTERVAL;
    }

    /**
     * Implementation ad controller callbacks
     */
    @Override
    public void onLoaded(String adName) {
        super.onLoaded(adName);
        if (userDelegate != null) {
            userDelegate.onInterstitialLoaded();
        }

        if (callbacks != null) {
            callbacks.onInterstitialLoaded();
        }
    }

    @Override
    public void onFailedToLoad(String adname) {
        super.onFailedToLoad(adname);
        if (userDelegate != null) {
            if (isPrecacheLoaded) {
                userDelegate.onInterstitialLoaded();
            } else {
                userDelegate.onInterstitialFailedToLoad();
            }
        }

        if (callbacks != null) {
            if (isPrecacheLoaded) {
                callbacks.onInterstitialLoaded();
            } else {
                callbacks.onInterstitialFailedToLoad();
            }
        }
    }

    @Override
    public void onClicked(String adName) {
        if (!isClicked) {
            adsAgent.getAdObject().setClicketAd();
            if (userDelegate != null) {
                userDelegate.onInterstitialClicked();
            }
            if (callbacks != null) {
                callbacks.onInterstitialClicked();
            }
        }
        super.onClicked(adName);
    }

    @Override
    public void onClosed(String adName) {
        if (!isClosed) {
            if (userDelegate != null) {
                userDelegate.onInterstitialClosed();
            }
            if (callbacks != null) {
                callbacks.onInterstitialClosed();
            }
        }
        super.onClosed(adName);
    }

    @Override
    public void onOpened(String adName) {
        if (!isShown) {
            if (userDelegate != null) {
                userDelegate.onInterstitialOpened();
            }
            if (callbacks != null) {
                callbacks.onInterstitialOpened();
            }
        }
        super.onOpened(adName);
    }

    /**
     * Precache listener inmplementation
     */
    @Override
    public void onPrecacheLoaded(String adname) {
        super.onPrecacheLoaded(adname);
        if (userDelegate != null) {
            userDelegate.onInterstitialLoaded();
        }
        if (callbacks != null) {
            callbacks.onInterstitialLoaded();
        }
    }

    @Override
    public void onPrecacheFailedToLoad(String adname) {
        super.onPrecacheFailedToLoad(adname);
    }

    @Override
    public void onPrecacheClicked(String adname) {
        if (!isPrecacheClicked) {
            if (userDelegate != null)
                userDelegate.onInterstitialClicked();
            if (callbacks != null)
                callbacks.onInterstitialClicked();
        }
        super.onPrecacheClicked(adname);
    }

    @Override
    public void onPrecacheClosed(String adname) {
        if (!isPrecacheClosed) {
            if (userDelegate != null)
                userDelegate.onInterstitialClosed();
            if (callbacks != null)
                callbacks.onInterstitialClosed();
        }
        super.onPrecacheClosed(adname);
    }

    @Override
    public void onPrecacheOpened(String adname) {
        if (!isPrecacheShown) {
            if (userDelegate != null) {
                userDelegate.onInterstitialOpened();
            }
            if (callbacks != null) {
                callbacks.onInterstitialOpened();
            }
        }
        super.onPrecacheOpened(adname);
    }
}
