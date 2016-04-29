package com.if3games.admanager.ads.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;

import com.if3games.admanager.ads.controllers.AdsListener;
import com.if3games.admanager.ads.utils.Logger;
import com.jirbo.adcolony.AdColony;
import com.jirbo.adcolony.AdColonyAd;
import com.jirbo.adcolony.AdColonyAdAvailabilityListener;
import com.jirbo.adcolony.AdColonyAdListener;
import com.jirbo.adcolony.AdColonyV4VCAd;
import com.jirbo.adcolony.AdColonyV4VCListener;
import com.jirbo.adcolony.AdColonyV4VCReward;

import org.json.JSONObject;

/**
 * Created by supergoodd on 06.10.15.
 */
public class AdColonyAdapter implements AdapterInterface, AdColonyAdListener, AdColonyAdAvailabilityListener, AdColonyV4VCListener {
    private static AdColonyAdapter instance;
    private AdColonyV4VCAd v4vc_ad;
    private AdsListener mListener;
    private String zoneId;
    private boolean isVideoCached = false;

    public static synchronized AdColonyAdapter getInstance(AdsListener listener) {
        if (instance == null) {
            instance = new AdColonyAdapter();
            instance.mListener = listener;
        }
        return instance;
    }

    public static String getName() {
        return "adcolony";
    }

    @Override
    public String getAdName() {
        return "adcolony";
    }

    @Override
    public void initAd(Context context, JSONObject params) {
        try {
            String appId = null;
            zoneId = null;
            try {
                appId = params.getString("ac_appId"); //ConstantsManager.getInstance().getConstants().AC_APP_ID;
                zoneId = params.getString("ac_zoneId"); //ConstantsManager.getInstance().getConstants().AC_ZONE_ID;
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (appId == null || zoneId == null) {
                return;
            }
            // AC_APP_ID  = "app000c69c7bbd8452ba0";
            // AC_ZONE_ID = "vz692700c8578041fb84";
            AdColony.configure((Activity)context, "version:1.0,store:google", appId, zoneId);
            ((Activity)context).setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            // Notify this object about confirmed virtual currency.
            AdColony.addV4VCListener(this);
            // Notify this object about ad availability changes.
            AdColony.addAdAvailabilityListener(this);
            v4vc_ad = new AdColonyV4VCAd(zoneId).withListener(this);
        } catch (Exception e) {
            Logger.log(e.getMessage());
        }
    }

    @Override
    public boolean isAvailable() {
        try  {
            Class.forName("com.jirbo.adcolony.AdColonyFullscreen");
            return true;
        }  catch (final ClassNotFoundException e) {
            return false;
        }
    }

    @Override
    public boolean isCached() {
        return isVideoCached;
    }

    @Override
    public boolean isAutoLoadingVideo() {
        return false;
    }

    @Override
    public void showInterstitial() {

    }

    @Override
    public void showVideo() {
        v4vc_ad.show();
    }

    @Override
    public void onStart(Context context) {

    }

    @Override
    public void onResume(Context context) {
        AdColony.resume((Activity)context);
    }

    @Override
    public void onPause(Context context) {
        AdColony.pause();
    }

    @Override
    public void onStop(Context context) {

    }

    @Override
    public void onDestroy(Context context) {
    }

    @Override
    public void onBackPressed() {
        AdColony.onBackPressed();
    }


    // Adcolony listeners
    @Override
    public void onAdColonyAdAttemptFinished(AdColonyAd ad) {
        if(ad.noFill() || ad.notShown()) {
            mListener.onFailedToLoad(getAdName());
        }
        if(ad.skipped() || ad.canceled()) {
            mListener.onClosed(getAdName());
        }
        if (ad.shown()) {
            mListener.onOpened(getAdName());
        }
    }

    @Override
    public void onAdColonyAdStarted(AdColonyAd ad) {
    }

    @Override
    public void onAdColonyV4VCReward(AdColonyV4VCReward reward) {
        if (reward.success()) {
            mListener.onFinished(getAdName());
        }
    }

    @Override
    public void onAdColonyAdAvailabilityChange(boolean av, String zId) {
        if (av && zId.trim().equals(zoneId)) {
            mListener.onLoaded(getAdName());
            isVideoCached = true;
        } else {
            mListener.onFailedToLoad(getAdName());
            isVideoCached = false;
        }

    }
}
