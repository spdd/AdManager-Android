package com.if3games.admanager.ads.config;

import com.google.gson.annotations.SerializedName;

/**
 * Created by supergoodd on 03.02.17.
 */

public class AdUnit {
    @SerializedName("adname")
    public String adname;

    @SerializedName("admob_id")
    public String admob_id;

    @SerializedName("cb_appId")
    public String cb_appId;
    @SerializedName("cb_appSigh")
    public String cb_appSigh;

    @SerializedName("unity_ads_id")
    public String unity_ads_id;

    @SerializedName("ac_appId")
    public String ac_appId;
    @SerializedName("ac_zoneId")
    public String ac_zoneId;
}
