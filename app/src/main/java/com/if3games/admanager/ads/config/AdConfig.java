package com.if3games.admanager.ads.config;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by supergoodd on 03.02.17.
 */

public class AdConfig {
    @SerializedName("title")
    public String status;

    @SerializedName("show_freq")
    public String showFreq;

    @SerializedName("config_from_url")
    public String isConfigFromUrl;

    @SerializedName("config_url")
    public String configUrl;

    @SerializedName("ads_interstitial")
    private List<AdUnit> adsInterstitial = new ArrayList<AdUnit>();

    @SerializedName("ads_video")
    private List<AdUnit> adsVideo = new ArrayList<AdUnit>();


    public List<AdUnit> getAdsInterstitial() {
        return adsInterstitial;
    }

    public List<AdUnit> getAdsVideo() {
        return adsVideo;
    }
}
