package com.if3games.admanager.ads.config;

import com.google.gson.annotations.SerializedName;
import com.if3games.admanager.ads.common.AdAgent;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by supergoodd on 03.02.17.
 */

public class AdConfig {
    @SerializedName("title")
    private String status;

    @SerializedName("show_freq")
    private String showFreq;

    @SerializedName("config_from_url")
    private String isConfigFromUrl;

    @SerializedName("config_url")
    private String configUrl;

    @SerializedName("ads_interstitial")
    public List<AdUnit> adsInterstitial = new ArrayList<AdUnit>();

    @SerializedName("ads_video")
    public List<AdUnit> adsVideo = new ArrayList<AdUnit>();


    public void setAdsInterstitial(List<AdUnit> adsInterstitial) {
        this.adsInterstitial = adsInterstitial;
    }

    public void setAdsVideo(List<AdUnit> adsVideo) {
        this.adsVideo = adsVideo;
    }

    private List<AdUnit> getAdsInterstitial() {
        return adsInterstitial;
    }

    private List<AdUnit> getAdsVideo() {
        return adsVideo;
    }

    public List<AdUnit> getAdsList(AdAgent.AdType type) {
        if (type == AdAgent.AdType.INTERSTITIAL)
            return getAdsInterstitial();
        else if (type == AdAgent.AdType.VIDEO)
            return getAdsVideo();
        return null;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getShowFreq() {
        return showFreq;
    }

    public void setShowFreq(String showFreq) {
        this.showFreq = showFreq;
    }

    public String getIsConfigFromUrl() {
        return isConfigFromUrl;
    }

    public void setIsConfigFromUrl(String isConfigFromUrl) {
        this.isConfigFromUrl = isConfigFromUrl;
    }

    public String getConfigUrl() {
        return configUrl;
    }

    public void setConfigUrl(String configUrl) {
        this.configUrl = configUrl;
    }
}
