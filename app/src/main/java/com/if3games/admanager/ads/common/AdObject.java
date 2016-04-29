package com.if3games.admanager.ads.common;

import com.if3games.admanager.ads.utils.Logger;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by supergoodd on 01.10.15.
 */
public class AdObject {
    public interface AdObjectListener {
        void refreshAdObjects() throws JSONException;
        AdAgent.AdType getAdType();
        void onAdFailedLoad();
        void onAdPrecacheFailedLoad();
        void onAdLoaded(AdObject adObject);
        void addCachedVideo(String adName);
        void onDisableNetwork(String adName);
    }
    private AdObjectListener mListener;

    public JSONObject requestData;
    public String adName = null;
    public boolean isFailed = false;
    public boolean isVideoCached = false;
    public boolean isPrecache = false;
    public int loaderTime;

    private int failedCounter = 0;
    private int loadCounter = 0;
    private int cost = 0;
    private int initCost;
    private int tryLoaderCounter = 0;
    private int clickCounter = 0;

    public AdObject(JSONObject requestData, int cost, AdObjectListener listener) {
        this.requestData = requestData;
        try {
            this.adName = requestData.getString("adname");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        this.mListener = listener;
        this.cost = cost;
        this.initCost = cost;
        loaderTime = 12;
        if (adName.equals("adcolony"))
            loaderTime = 15;
    }

    public String getAdName() {
        return adName;
    }

    public void setClicketAd() {
        clickCounter++;
        if (!isPrecache && mListener.getAdType() != AdAgent.AdType.VIDEO && clickCounter >= 2) {
            isFailed = true;
            mListener.onDisableNetwork(adName);
        }
    }

    public void setLoadedAd(boolean loaded) throws JSONException {
        if (!loaded) {
            if (mListener != null && mListener.getAdType() == AdAgent.AdType.VIDEO) {
                isVideoCached = false;
                failedCounter = 10;
            }
            failedCounter++;
            if (failedCounter >= 3) {
                isFailed = true;
            }
            if(isFailed) {
                failedCounter++;
            }
            if (mListener != null && isPrecache) {
                mListener.onAdPrecacheFailedLoad();
                return;
            }
            if (mListener != null)
                mListener.onAdFailedLoad();
        } else {
            if (mListener != null && mListener.getAdType() == AdAgent.AdType.VIDEO) {
                isVideoCached = true;
                loaderTime = -1;
                mListener.addCachedVideo(adName);
            }
            loadCounter++;
            if (failedCounter >= 1) {
                failedCounter = 0;
            }
            if (mListener != null)
                mListener.onAdLoaded(this);
        }
        if (isPrecache) {
            return;
        }
        if (mListener != null) {
            mListener.refreshAdObjects();
        }
    }

    public void didVideoShown(boolean shown) {
        mListener.onAdLoaded(this);
        if (mListener != null) {
            try {
                mListener.refreshAdObjects();
            } catch (Exception e) {
                Logger.log(String.format("%s", e.getMessage()));
            }
        }
    }

    public void resetAd() { // timeout reset (evoke from AdAgent)
        isFailed = false;
        failedCounter = 0;
        loadCounter = 0;
        cost = initCost;
    }

    public int loadsCount() {
        return loadCounter;
    }

    public float getCalculatedCost() {
        float c = (1.0f/(1.0f + (failedCounter + this.cost)));
        return c;
    }

    /**
     * Video Ads
      */

    public void setTryLoadingCount() {
        tryLoaderCounter++;
    }

    public int getTryLoadingCount() {
        return tryLoaderCounter;
    }

    public void cacheNextVideo() {
        loaderTime = 12;
        isVideoCached = false;
        tryLoaderCounter = 0;
    }

    /**
     * manipulate costs
      */
    public void setObjectToTop() throws JSONException {
        failedCounter = 0;
        cost = 0;
        if (mListener != null) {
            mListener.refreshAdObjects();
        }
    }
}
