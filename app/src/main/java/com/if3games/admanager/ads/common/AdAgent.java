package com.if3games.admanager.ads.common;

import android.os.CountDownTimer;

import com.if3games.admanager.ads.AdsConstants;
import com.if3games.admanager.ads.utils.Logger;
import com.if3games.admanager.ads.utils.SettingsManager;
import com.if3games.admanager.ads.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by supergoodd on 01.10.15.
 */
public class AdAgent implements AdObject.AdObjectListener {

    private boolean isDisabledNetwork = false;
    private boolean isCachedVideoAdded = false;

    public interface AdAgentListener {
        void reCacheBanner();
        void loadBanner();
        void loadPrecacheBanner();
    }
    private AdAgentListener mListener;

    public static enum AdType { INTERSTITIAL, VIDEO, BANNER }
    public AdType mAdType;
    public int adsCount;
    public int precacheCount;
    public boolean isAdsReady = false;
    public boolean isPrecacheReady = false;
    public boolean isPrecacheDisabled = false;
    public boolean isPrecacheTmpDisabled = false;
    public boolean isRecaching  = false;

    private List<AdObject> adsList = new ArrayList<>();
    private List<AdObject> cachedAdsList;
    private List<AdObject> precacheList = new ArrayList<>();
    private List<AdObject> cachedPrecacheList;
    private List<String> disabledNetworks;
    private List<String> cachedVideoAds;
    private int precacheIndex = 0;
    private int failedLoadsCounter = 0;
    private int failedPrecacheLoadsCounter = 0;
    private CountDownTimer precacheResetCounterTimer;
    private CountDownTimer failedLoadsCounterTimer;
    private CountDownTimer failedPrecacheLoadsCounterTimer;
    private CountDownTimer serverNotRespTimer;

    public AdAgent(AdAgentListener listener, AdType adType) {
        this.mListener = listener;
        this.mAdType = adType;
        Logger.log("AdsAgent in thread : " + Thread.currentThread().getName());
    }

    public void push(JSONObject data, int cost) throws JSONException {
        if (data.has("adname")) {
            String adName = data.getString("adname");
            long lastClickedTime = SettingsManager.getLongValue(adName);
            if (lastClickedTime > 0 && !Utils.isAdOverClickerTimeout(lastClickedTime))
                return;
        }
        if (disabledNetworks != null) {
            if (data.has("adname")) {
                String adName = data.getString("adname");
                for (String ad : disabledNetworks) {
                    if (adName.trim().equals(ad)) {
                        return;
                    }
                }
            }
        }

        AdObject adObj = new AdObject(data, cost, this);
        adsList.add(adObj);
        isAdsReady = true;
        isRecaching = false;
        //failedLoadsCounter = 0;
    }

    public void pushPrecache(JSONObject data, int cost) throws JSONException {
        if (disabledNetworks != null) {
            if (data.has("adname")) {
                String adName = data.getString("adname");
                for (String ad : disabledNetworks) {
                    if (adName.trim().equals(ad)) {
                        return;
                    }
                }
            }
        }
        AdObject adObj = new AdObject(data,cost,null);
        adObj.isPrecache = true;
        precacheList.add(adObj);
        isPrecacheReady = true;
    }

    public void clear() {
        isAdsReady = false;
        isPrecacheReady = false;
        save();
        adsList.clear();
        precacheList.clear();
    }

    public void save() {
        if(adsList.size() != 0) {
            cachedAdsList =  new ArrayList<>(adsList);
            reset(cachedAdsList);
        }
        if(precacheList.size() != 0) {
            cachedPrecacheList = new ArrayList<>(precacheList);
            reset(cachedPrecacheList);
        }
    }

    public void reset(List<AdObject> array) {
        for (AdObject item : array) {
            item.resetAd();
        }
        isPrecacheTmpDisabled =false;
    }

    public AdObject getAdObject() {
        if(adsList.size() == 0 && cachedAdsList.size() == 0)
            return null;

         AdObject item = adsList.get(0); //isAdsReady ? adsList.get(0) : cachedAdsList.get(0);
        if(mAdType == AdType.INTERSTITIAL) {
            Logger.log(String.format("Cost Current Object: %f", item.getCalculatedCost()));
        }
        return item;
    }

    public AdObject getAdObjectWithIndex(int index) {
         AdObject item = adsList.get(index);
        if(mAdType == AdType.INTERSTITIAL) {
            Logger.log(String.format("Cost Selected Object: %f", item.getCalculatedCost()));
        }
        return item;
    }

    public AdObject getPrecacheAdObject() {
        AdObject item = null;
        try {
            if(precacheList.size() == 0 && cachedPrecacheList.size() == 0)
                return null;

            if (precacheIndex < 0)
                return null;
            item = precacheList.get(precacheIndex);  // isPrecacheReady ? precacheList.get(precacheIndex) : cachedPrecacheList.get(precacheIndex);

            if (item.isFailed) {
                precacheIndex++;
                if(precacheIndex >= precacheList.size()) {
                    precacheIndex = -1;
                    isPrecacheReady =false;
                    scheduleResetPrecacheCounter();
                    return null;
                }
            }
        } catch (Exception e) {
            Logger.log(String.format(e.getMessage()));
        }

        return item;
    }

    public List<AdObject> getAdsList() {
        return adsList;
    }

    public String getCachedVideo() {
        return cachedVideoAds != null ? cachedVideoAds.get(0) : null;
    }

    /**
     * Disable network
      */

    public void disableNetwork(String network) throws JSONException {
        if (mAdType == AdType.INTERSTITIAL && network.trim().equals("admob")) {
            isPrecacheDisabled = true;
        }

        disableAds(network, adsList);
        disableAds(network, precacheList);

        if(isDisabledNetwork) {
            disabledNetworks = new ArrayList<>();
        }
        disabledNetworks.add(network);
    }

    private void disableAds(String network, List<AdObject> ads) throws JSONException {
        List<AdObject> tmpList = new ArrayList<>(ads);
        for (AdObject obj : tmpList) {
            if (obj.requestData.has("adname")) {
                if(obj.requestData.getString("adname").trim().equals(network)) {
                    ads.remove(obj);
                }
            }
        }
    }

    /**
     * networks info
      */

    public boolean isContainNetwork(String network) throws JSONException {
        List<AdObject> tmpList = new ArrayList<>(adsList);
        for (AdObject obj : tmpList) {
            if (obj.requestData.has("adname")) {
                if(obj.requestData.getString("adname").trim().equals(network)) {
                    return true;
                }
            }
        }
        return false;
    }

    public void setNetworkToTop(String adName) throws JSONException {
        isPrecacheTmpDisabled = true;
        List<AdObject> tmpList = new ArrayList<>(adsList);
        for (AdObject obj : tmpList) {
            if (obj.requestData.has("adname")) {
                if(obj.requestData.getString("adname").trim().equals(adName)) {
                    obj.setObjectToTop();
                    return;
                }
            }
        }
    }

    /**
     * Load Precache timer to reset precache failed loads counter
     */

    public void scheduleResetPrecacheCounter() {
        if(precacheResetCounterTimer != null)
            precacheResetCounterTimer.cancel();

        int timeInterval = AdsConstants.RESET_FL_COUNTER_PRECACHE_TIMEOUT_FOR_TIMER;

        if (timeInterval > 0) {
            precacheResetCounterTimer = new CountDownTimer(timeInterval, 1000) {
                public void onTick(long millisUntilFinished) {
                }
                public void onFinish() {
                    resetPrecacheCounter();
                }
            }
                    .start();
            Logger.log(String.format("Scheduled the auto reset precache failed loades counter (timer to fire in %d seconds).", timeInterval));
        }
    }

    private void resetPrecacheCounter() {
        Logger.log("evoke resetPrecacheCounter from timer");
        precacheIndex = 0;
    }

    /**
     * Failed loads timer to reset failed loads counter
      */

    public void scheduleResetFailedLoadsCounter() {
        if(failedLoadsCounterTimer != null)
            failedLoadsCounterTimer.cancel();

        int timeInterval = AdsConstants.RESET_FL_COUNTER_BANNER_TIMEOUT_FOR_TIMER;

        if (timeInterval > 0) {
            failedLoadsCounterTimer = new CountDownTimer(timeInterval, 1000) {
                public void onTick(long millisUntilFinished) {
                }
                public void onFinish() {
                    resetFailedCounter();
                }
            }
                    .start();
            Logger.log(String.format("Scheduled the auto reset failed loads counter (timer to fire in %d seconds).", timeInterval));
        }
    }

    public void resetFailedCounter() {
        Logger.log("evoke resetFailedCounter from timer");
        isAdsReady = true;
        failedLoadsCounter = 0;
        mListener.loadBanner();
    }

    /**
     * Failed loads timer to reset failed loads counter
      */

    public void scheduleResetPrecacheFailedLoadsCounter() {
        if(failedPrecacheLoadsCounterTimer != null)
            failedPrecacheLoadsCounterTimer.cancel();

        int timeInterval = AdsConstants.RESET_FL_COUNTER_BANNER_TIMEOUT_FOR_TIMER;

        if (timeInterval > 0) {
            failedPrecacheLoadsCounterTimer = new CountDownTimer(timeInterval, 1000) {
                public void onTick(long millisUntilFinished) {
                }
                public void onFinish() {
                    resetPrecacheFailedCounter();
                }
            }
                    .start();
            Logger.log(String.format("Scheduled the auto reset precache failed loads counter (timer to fire in %d seconds).", timeInterval));
        }
    }

    public void resetPrecacheFailedCounter() {
        Logger.log("evoke resetPrecacheFailedCounter from timer");
        isPrecacheReady = true;
        failedPrecacheLoadsCounter = 0;
        mListener.loadPrecacheBanner();
    }

    /**
     * schedule server responsing
      */

    public void scheduleServerNotResponsing() {
        if(serverNotRespTimer != null)
            serverNotRespTimer.cancel();

        int timeInterval = AdsConstants.SERVER_NOT_RESPONDING_TIMEOUT;

        if (timeInterval > 0) {
            serverNotRespTimer = new CountDownTimer(timeInterval, 1000) {
                public void onTick(long millisUntilFinished) {
                }
                public void onFinish() {
                    resetServerNotRespTimer();
                }
            }
                    .start();
            Logger.log(String.format("Scheduled serverfalset responsing (timer to fire in %d seconds).", timeInterval));
        }
    }

    public void resetServerNotRespTimer() {
        Logger.log("Reset ServerNotResp Timer");
        if(serverNotRespTimer != null) {
            serverNotRespTimer.cancel();
        }
        mListener.reCacheBanner();
    }

    public void serverNotResponding() {
        scheduleServerNotResponsing();
    }

    /**
     * AdObjectDelegate
      */

    @Override
    public void refreshAdObjects() throws JSONException {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                synchronized (adsList) {
                    try {
                        sort(adsList);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    if (adsList.get(0).getCalculatedCost() < 0.05) {
                        if(!isRecaching) {
                            if (mListener != null) {
                                mListener.reCacheBanner();
                                isRecaching = true;
                            }
                        }
                    }
                }
            }});
        thread.setName("Sort thread");
        thread.start();
        //thread.join();
    }

    @Override
    public AdType getAdType() {
        return mAdType;
    }

    @Override
    public void onAdFailedLoad() {
        failedLoadsCounter++;

        if((mAdType == AdType.BANNER) && failedLoadsCounter >= 10) {
            isAdsReady = false;
            scheduleResetFailedLoadsCounter();
            Logger.log(String.format("Banner Failed counter: %d", failedLoadsCounter));
        }

        if((mAdType == AdType.INTERSTITIAL) && failedLoadsCounter >= 20) {
            isAdsReady = false;
            scheduleResetFailedLoadsCounter();
            Logger.log(String.format("Interstitial Failed counter: %d", failedLoadsCounter));
        }

        if(mAdType == AdType.VIDEO && failedLoadsCounter >= 5) {
            isAdsReady = false;
            scheduleResetFailedLoadsCounter();
            Logger.log(String.format("Video Failed counter clip: %d", failedLoadsCounter));
        }
    }

    @Override
    public void onAdPrecacheFailedLoad() {
        failedPrecacheLoadsCounter++;
        if((mAdType == AdType.INTERSTITIAL) && failedPrecacheLoadsCounter >= 15) {
            isPrecacheReady = false;
            scheduleResetPrecacheFailedLoadsCounter();
            Logger.log(String.format("Precache Failed counter: %d", failedPrecacheLoadsCounter));
        }
    }

    @Override
    public void onAdLoaded(AdObject adObject) {
        if (adObject.isPrecache) {
            return;
        }
        failedLoadsCounter = 0;
        isAdsReady = true;
        reset(adsList);
    }

    @Override
    public void addCachedVideo(String adName) {
        if(!isCachedVideoAdded) {
            cachedVideoAds = new ArrayList<>();
            isCachedVideoAdded = true;
        }
        cachedVideoAds.add(adName);
    }

    @Override
    public void onDisableNetwork(String adName) {
        try {
            disableNetwork(adName);
            SettingsManager.setLongValue(adName, System.currentTimeMillis());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /** sort ads
     *
     */

    public void sort(List<AdObject> items) throws JSONException {
        Logger.log("Sort in thread : " + Thread.currentThread().getName());
        int sortedRangeEnd = 0;
        while (sortedRangeEnd < items.size()) {
            int nextIndex = findNextIndexOfHighestFromIndex(items, sortedRangeEnd);
            Collections.swap(items, sortedRangeEnd, nextIndex);
            sortedRangeEnd++;
        }

        if(AdsConstants.DEBUG_SDK == 1) {
            for (AdObject item : items){
                if (mAdType == AdType.BANNER) {
                    Logger.log(String.format("Sorted List Banner: %s, cost: %f",
                            item.requestData.getString("adname"), item.getCalculatedCost()));
                } else if (mAdType == AdType.INTERSTITIAL) {
                    Logger.log(String.format("Sorted List Interstitial: %s, cost: %f",
                            item.requestData.getString("adname"), item.getCalculatedCost()));
                } else if (mAdType == AdType.VIDEO) {
                    Logger.log(String.format("Sorted List Video: %s, cost: %f",
                            item.requestData.getString("adname"), item.getCalculatedCost()));
                }
            }
        }
    }

    private int findNextIndexOfHighestFromIndex(List<AdObject> items, int sortedRangeEnd) {
         AdObject currentHighest = items.get(sortedRangeEnd);
        int currentHighestIndex = sortedRangeEnd;

        for (int i = sortedRangeEnd + 1; i < items.size(); i++) {
             AdObject next = items.get(i);
            if (currentHighest.getCalculatedCost() < next.getCalculatedCost()) {
                currentHighest = items.get(i);
                currentHighestIndex = i;
            }
        }
        return currentHighestIndex;
    }
}
