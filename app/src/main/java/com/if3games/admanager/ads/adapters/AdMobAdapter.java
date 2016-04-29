package com.if3games.admanager.ads.adapters;

import android.content.Context;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.if3games.admanager.ads.AdsConstants;
import com.if3games.admanager.ads.controllers.AdsListener;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by supergoodd on 30.09.15.
 */
public class AdMobAdapter implements AdapterInterface {
    private static AdMobAdapter instance;
    private AdsListener mListener;
    private InterstitialAd interstitial;

    private AdListener adListener = new AdListener() {
        @Override
        public void onAdClosed() {
            super.onAdClosed();
            mListener.onClosed(getAdName());
        }

        @Override
        public void onAdFailedToLoad(int errorCode) {
            mListener.onFailedToLoad(getAdName());
            super.onAdFailedToLoad(errorCode);
        }

        @Override
        public void onAdLeftApplication() {
            mListener.onClicked(getAdName());
            super.onAdLeftApplication();
        }

        @Override
        public void onAdOpened() {
            mListener.onOpened(getAdName());
            super.onAdOpened();
        }

        @Override
        public void onAdLoaded() {
            mListener.onLoaded(getAdName());
            super.onAdLoaded();
        }
    };

    public static synchronized AdMobAdapter getInstance(AdsListener listener) {
        if (instance == null) {
            instance = new AdMobAdapter();
            instance.mListener = listener;
        }
        return instance;
    }

    @Override
    public String getAdName() {
        return "admob";
    }

    public static String getName() {
        return "admob";
    }

    @Override
    public void initAd(Context context, JSONObject params) {
        String adId = null; //ConstantsManager.getInstance().getConstants().INTERSTITIAL_APPID;
        try {
            adId = params.getString("admob_inter_id");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        interstitial = new InterstitialAd(context);
        interstitial.setAdUnitId(adId);
        AdRequest.Builder reqBuilder = new AdRequest.Builder();

        if (AdsConstants.DEBUG_SDK == 1) {
            //reqBuilder.addTestDevice(AdRequest.DEVICE_ID_EMULATOR);
        }
        AdRequest adRequest = reqBuilder.build();
        interstitial.setAdListener(adListener);
        interstitial.loadAd(adRequest);
    }

    @Override
    public boolean isAvailable() {
        try  {
            Class.forName("com.google.android.gms.ads.AdActivity");
            return true;
        }  catch (final ClassNotFoundException e) {
            return false;
        }
    }

    @Override
    public boolean isCached() {
        return false;
    }

    @Override
    public boolean isAutoLoadingVideo() {
        return false;
    }

    @Override
    public void showInterstitial() {
        if(interstitial.isLoaded())
            interstitial.show();
    }

    @Override
    public void showVideo() {

    }

    @Override
    public void onStart(Context context) {

    }

    @Override
    public void onResume(Context context) {

    }

    @Override
    public void onPause(Context context) {

    }

    @Override
    public void onStop(Context context) {

    }

    @Override
    public void onDestroy(Context context) {

    }

    @Override
    public void onBackPressed() {

    }
}
