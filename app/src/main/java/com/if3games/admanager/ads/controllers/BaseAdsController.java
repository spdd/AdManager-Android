package com.if3games.admanager.ads.controllers;

/**
 * Created by supergoodd on 05.10.15.
 */

import android.app.Activity;
import android.content.Context;
import android.os.CountDownTimer;
import android.os.Handler;
import android.util.Log;

import com.if3games.admanager.ads.AdsConstants;
import com.if3games.admanager.ads.UserCallbacks;
import com.if3games.admanager.ads.adapters.AdapterInterface;
import com.if3games.admanager.ads.adapters.PrecacheAdapter;
import com.if3games.admanager.ads.common.AdAgent;
import com.if3games.admanager.ads.common.InstanceFactory;
import com.if3games.admanager.ads.config.ConfigLoader;
import com.if3games.admanager.ads.utils.Logger;
import com.if3games.admanager.ads.utils.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

/**
 * Created by supergoodd on 30.09.15.
 */
public class BaseAdsController implements AdsListener, PrecacheListener, ConfigLoader.Listener, AdAgent.AdAgentListener {
    protected String status;
    protected String precacheStatus;

    protected boolean isLoading = false;
    protected boolean isLoaded = false;
    protected boolean isClicked = false;
    protected boolean isClosed = false;
    protected boolean isShown = false;
    protected boolean isOpened = false;
    protected boolean isPrecacheLoaded = false;
    protected boolean isPrecacheShown = false;
    protected boolean isPrecacheClicked = false;
    protected boolean isPrecacheClosed = true;

    protected boolean isServerError = false;
    protected boolean isNoNeedLoad = false;
    protected boolean autocache = false;
    protected AdAgent.AdType controllerType;
    protected String controllerPrefix = "base";

    protected Map<String, AdapterInterface> adapterInstances;
    protected Context mContext;
    protected UserCallbacks userDelegate;

    // Timers
    protected CountDownTimer refreshCachBannerTimer;
    protected CountDownTimer loadAdTimer;

    protected AdAgent adsAgent;

    /**
     * public methods
     */

    public void onStart(Context context) {
        isOpened = false;
        if (status != null) {
            AdapterInterface adapter = adapterInstances.get(status);
            if (adapter != null)
                adapter.onStart(context);
        }

    }

    public void onStop(Context context) {
        if (status != null) {
            AdapterInterface adapter = adapterInstances.get(status);
            if (adapter != null)
                adapter.onStop(context);
        }
    }

    public void onDestroy(Context context) {
        if (status != null) {
            AdapterInterface adapter = adapterInstances.get(status);
            if (adapter != null)
                adapter.onDestroy(context);
        }
    }

    public void onBackPressed(Context context) {
        if (status != null) {
            AdapterInterface adapter = adapterInstances.get(status);
            if (adapter != null)
                adapter.onBackPressed();
        }
    }

    public void onPause(Context context) {
        if (status != null) {
            AdapterInterface adapter = adapterInstances.get(status);
            if (adapter != null)
                adapter.onPause(context);
        }
    }

    public void onResume(Context context) {
        if (status != null) {
            AdapterInterface adapter = adapterInstances.get(status);
            if (adapter != null)
                adapter.onResume(context);
        }
    }

    public void disableNetwork(String adname) throws JSONException {
        adapterInstances.remove(adname);
        loadAd();
    }

    public void setCallbacks(UserCallbacks callbacks) {
        userDelegate = callbacks;
    }

    public boolean isLoaded() {
        return isLoaded;
    }

    public boolean isOpened() {
        return false;
    }

    public void cache(Context context) {
        isOpened = false;
        mContext = context;
        loadAd();
    }

    public void show(Context context) throws JSONException {
        showAd(context, null);
    }

    public void show(Context context, String adName) throws JSONException {
        showAd(context, adName);
    }

    protected void showAd(final Context context, final String adName) throws JSONException {
        if(!isLoaded && !isPrecacheLoaded && !isServerError && !isNoNeedLoad) {
            if (!isLoading) {
                if (adsAgent.isPrecacheReady) {
                    loadPrecache();
                }

                if (adsAgent.isAdsReady) {
                    loadAd();
                }
            }

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    try {
                        showAd(context);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }, AdsConstants.PRELOADER_TIMEOUT_FOR_TIMER);

        } else if(adName != null) {
            if (adsAgent.isAdsReady) {
                Logger.logAds(controllerType, String.format("show with name: %s", adName));
                adsAgent.setNetworkToTop(adName);
                loadAd();

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            showAd(context);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, AdsConstants.PRELOADER_TIMEOUT_FOR_TIMER);
            }
        } else {
            showAd(context);
        }
    }

    protected void showAd(Context context) throws JSONException {
    }

    /**
     * Internal ad management
     */
    protected void cacheNextAd() {
        Logger.logAds(controllerType, "chache next ad");
        loadAd();
    }

    protected void loadNextAd() {
        Logger.logAds(controllerType, "Load next ad");
        loadAd();
    }

    protected synchronized void loadAd() {
        if (!adsAgent.isAdsReady)
            return;

        status = adsAgent.getAdObject().getAdName();
        Logger.logAds(controllerType, String.format("loadAd Status: %s", status));

        final AdapterInterface adapter = adapterInstances.get(status);
        if (adapter != null) {
            if (adapter.isAvailable()) {
                ((Activity)mContext).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        evokeFailedToLoadAd(adapter);
                        adapter.initAd(mContext, adsAgent.getAdObject().requestData);
                    }
                });
            } else {
                onFailedToLoad(adapter.getAdName());
                return;
            }
        } else {
            onFailedToLoad();
            return;
        }
    }

    protected void evokeFailedToLoadAd(AdapterInterface adapter) {}

    protected synchronized void loadPrecache() {
        loadPrecache(0);
    }

    protected synchronized void loadPrecache(int index) {
        if (!adsAgent.isPrecacheReady)
            return;
        if (adsAgent.isPrecacheDisabled) {
            return;
        }
        if(adsAgent.getPrecacheAdObject() == null) {
            onPrecacheFailedToLoad();
            return;
        }
        precacheStatus = adsAgent.getPrecacheAdObject().getAdName();
        Logger.logAds(controllerType, String.format("Precache Status: %s", precacheStatus));

        final AdapterInterface adapter = PrecacheAdapter.getInstance(this);
        if (adapter.getAdName().trim().equals(precacheStatus)) {
            if (adapter.isAvailable()) {
                ((Activity)mContext).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        adapter.initAd(mContext, adsAgent.getPrecacheAdObject().requestData);
                    }
                });
            } else {
                onPrecacheFailedToLoad(adapter.getAdName());
                return;
            }
        } else {
            onPrecacheFailedToLoad();
            return;
        }
    }

    /**
     * Loading config from web
     */

    protected void loadConfig() {
        isLoading = false;
        isLoaded = false;
        status = null;
        Logger.logAds(controllerType, String.format("Loading %s config", controllerPrefix));

        if (Utils.isNetworkConnected(mContext)) { // network available
            isLoading = true;
            // download config from network
            InstanceFactory.getInstance().createConfigLoader(mContext, this).runConfig();
            if (autocache) {
                //scheduleRefreshCacheBanner();
            }
        }
    }

    /**
     * Implementation config loader callbacks
     */
    @Override
    public void onConfigFailedToLoad(int errorCode) {
        Logger.logAds(controllerType, String.format("%d", errorCode));
        adsAgent.serverNotResponding();
        isServerError = true;
    }

    @Override
    public void onConfigLoaded(final JSONObject config) {
        Logger.log("onConfigLoaded in thread: " + Thread.currentThread().getName());
        new Thread(new Runnable() {
            public void run() {
                isServerError = false;
                if (config != null) {
                    //adsAgent.clear();
                    try {
                        if (!config.has(String.format("ads_%s", controllerPrefix))) {
                            if(config.has("message")) {
                                String message = config.getString("message");
                                Logger.logAds(controllerType, String.format("%s", message));
                                if (message.trim().equals("Null")) {
                                    isNoNeedLoad = true;
                                    Logger.logAds(controllerType, "No need load from web");
                                }
                            }
                            return;
                        }
                        isNoNeedLoad = false;

                        // first precache
                        JSONArray precacheArray = config.getJSONArray(String.format("precache_%s", controllerPrefix));
                        if(precacheArray != null) {
                            int cost = 1;
                            for (int i = 0; i < precacheArray.length(); i++) {
                                adsAgent.pushPrecache(precacheArray.getJSONObject(i), cost);
                                cost++;
                            }
                        }

                        if (adsAgent.isPrecacheReady) {
                            if (autocache)
                                loadPrecache();
                        } else {
                            Logger.logAds(controllerType, "Precache is empty or new launch");
                        }

                        // second ads
                        JSONArray adsArray = config.getJSONArray(String.format("ads_%s", controllerPrefix));
                        Logger.logAds(controllerType, String.format("adsArray size: %d", adsArray.length()));
                        if (adsArray != null) {
                            int cost = 1;
                            for (int i = 0; i < adsArray.length(); i++) {
                                adsAgent.push(adsArray.getJSONObject(i), cost);
                                Logger.logAds(controllerType, String.format("ads: %s", adsArray.getJSONObject(i).getString("adname")));
                                cost++;
                            }
                        }

                        if (adsAgent.isAdsReady) {
                            if(autocache)
                                loadAd();
                        } else
                            Logger.logAds(controllerType, "Ads is empty");

                    } catch (Exception e) {
                        Logger.logAds(controllerType, String.format("onConfigLoaded %s", e.getMessage()));
                        onFailedToLoad();
                    }
                }
            }
        }).start();
    }

    /**
     * Timers
     */

    protected void scheduleRefreshCacheBanner() {
        if(refreshCachBannerTimer != null)
            refreshCachBannerTimer.cancel();

        int timeInterval = AdsConstants.CACHE_REFRESH_TIMEOUT_FOR_TIMER;

        if (timeInterval > 0) {
            refreshCachBannerTimer = new CountDownTimer(timeInterval, 1000) {
                public void onTick(long millisUntilFinished) {
                }
                public void onFinish() {
                    refreshCacheBanner();
                }
            }
                    .start();
            Logger.logAds(controllerType, String.format("Scheduled the auto refresh CacheBanner timer to fire in %d seconds.", timeInterval));
        }
    }

    protected void refreshCacheBanner() {
        Logger.logAds(controllerType, "evoke cacheBanner from timer");
        //loadConfig();
    }

    protected void scheduleFailedToLoadAd() {
        if(loadAdTimer != null)
            loadAdTimer.cancel();

        int timeInterval = getTimeIntervalForLoadSheduler();

        if (timeInterval > 0) {
            loadAdTimer = new CountDownTimer(timeInterval, 1000) {
                public void onTick(long millisUntilFinished) {
                }
                public void onFinish() {
                    timeoutLoadAd();
                }
            }
                    .start();
            Logger.logAds(controllerType, String.format("Scheduled the auto evoke onAdFailedLoad timer to fire in %d seconds.", timeInterval));
        }
    }

    protected void timeoutLoadAd() {
        Logger.logAds(controllerType, String.format("Timeout Load Ad status: %s", status));
        onFailedToLoad();
    }

    protected int getTimeIntervalForLoadSheduler() {
        return AdsConstants.INTERSTITIAL_TIMEOUT_INTERVAL;
    }

    /**
     * Implementation ad controller callbacks
     */
    @Override
    public void onLoaded(String adName) {
        Logger.log("onLoaded in thread: " + Thread.currentThread().getName());
        if(loadAdTimer != null)
            loadAdTimer.cancel();
        try {
            Logger.logAds(controllerType, String.format("onLoaded %s", adName));
            if (adsAgent.getAdObject() != null)
                adsAgent.getAdObject().setLoadedAd(true);
            isLoaded = true;
            isLoading = false;
            isClicked = false;
            isClosed = false;
            isShown = false;
        } catch (JSONException e) {
            e.printStackTrace();
            Logger.log(String.format("%s", e.getMessage()));
        }
    }

    protected void onFailedToLoad() {
        onFailedToLoad(null);
    }

    @Override
    public void onFailedToLoad(String adname) {
        if(loadAdTimer != null)
            loadAdTimer.cancel();
        try {
            isLoading = false;
            isLoaded = false;
            if (adname == null) {
                Logger.logAds(controllerType, "onAdFailedLoad");
            } else {
                Logger.logAds(controllerType, String.format("onAdFailedLoad %s", adname));
            }
            if (adsAgent.getAdObject() != null)
                adsAgent.getAdObject().setLoadedAd(false);
            if (adsAgent.isAdsReady) {
                loadNextAd();
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Logger.log(String.format("%s", e.getMessage()));
        }
    }

    @Override
    public void onClicked(String adName) {
        try {
            if (!isClicked) {
                Logger.logAds(controllerType, String.format("Clicked %s", adName));
                // todo: track click
                isClicked = true;
            }
        } catch (Exception e) {
            Logger.log(String.format("%s", e.getMessage()));
        }
    }

    @Override
    public void onClosed(String adName) {
        try {
            if (!isClosed) {
                isClosed = true;
                isOpened = false;
                Logger.logAds(controllerType, String.format("onClosed %s", adName));
            }
        } catch (Exception e) {
            Logger.log(String.format("%s", e.getMessage()));
        }
    }

    @Override
    public void onOpened(String adName) {
        try {
            if (!isShown) {
                Logger.logAds(controllerType, String.format("onOpened %s", adName));
                isLoaded = false;
                isShown = true;
                isOpened = true;
                // todo: track impession

                if(adsAgent.isAdsReady) {
                    cacheNextAd();
                }
            }
        } catch (Exception e) {
            Logger.log(String.format("%s", e.getMessage()));
        }
    }

    @Override
    public void onFinished(String adname) {
        Logger.logAds(controllerType, String.format("onFinished %s", adname));
    }

    /**
     * Precache listener inmplementation
     */

    @Override
    public void onPrecacheLoaded(String adname) {
        try {
            Logger.logAds(controllerType, String.format("Precache Loaded: %s", adname));
            if (adsAgent.getPrecacheAdObject() != null)
                adsAgent.getPrecacheAdObject().setLoadedAd(true);
            isLoading = true;
            isPrecacheLoaded = true;
            isPrecacheShown = false;
            isPrecacheClicked = false;
            isPrecacheClosed = false;
        } catch (JSONException e) {
            e.printStackTrace();
            Logger.log(String.format("%s", e.getMessage()));
        }
    }

    protected void onPrecacheFailedToLoad() {
        onPrecacheFailedToLoad(null);
    }

    @Override
    public void onPrecacheFailedToLoad(String adname) {
        try {
            Logger.logAds(controllerType, String.format("Precache FailedToLoaded: %s", adname));

            if (adsAgent.getPrecacheAdObject() != null)
                adsAgent.getPrecacheAdObject().setLoadedAd(false);
            isPrecacheLoaded = false;
            if (adsAgent.isPrecacheReady) {
                loadPrecache();
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Logger.log(String.format("%s", e.getMessage()));
        }
    }

    @Override
    public void onPrecacheClicked(String adname) {
        try {
            if (!isPrecacheClicked) {
                Logger.logAds(controllerType, String.format("Precache Clicked: %s", adname));
                isPrecacheClicked = true;
                // todo: track precache click
            }
        } catch (Exception e) {
            Logger.log(String.format("%s", e.getMessage()));
        }
    }

    @Override
    public void onPrecacheClosed(String adname) {
        if (!isPrecacheClosed) {
            Logger.logAds(controllerType, String.format("Precache Closed: %s", adname));
            isPrecacheClosed = true;
            isOpened = false;
        }
    }

    @Override
    public void onPrecacheOpened(String adname) {
        if (!isPrecacheShown) {
            Logger.logAds(controllerType, String.format("Precache Opened: %s", adname));
            isPrecacheShown = true;
            isPrecacheLoaded = false;
            isOpened = true;
            // todo: track precache impression
            if(adsAgent.isPrecacheReady)
                loadPrecache();

            if(adsAgent.isAdsReady)
                loadAd();
        }
    }

    /**
     * AdAgent Listener implementation
     */

    @Override
    public void reCacheBanner() {
        //loadConfig();
    }

    @Override
    public void loadBanner() {
        if (adsAgent.isAdsReady) {
            loadAd();
        }
    }

    @Override
    public void loadPrecacheBanner() {
        if(adsAgent.isPrecacheReady) {
            loadPrecache();
        }
    }
}

