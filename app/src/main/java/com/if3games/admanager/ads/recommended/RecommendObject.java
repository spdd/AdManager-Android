package com.if3games.admanager.ads.recommended;

import android.content.Context;
import android.content.pm.PackageManager;

/**
 * Created by supergoodd on 27.09.15.
 */
public class RecommendObject {

    private int id;
    private String appName;
    private String appDescr;
    private String appPackage;
    private String appIcon;
    private Context context;
    private boolean isInstelled = false;
    private boolean isRewareded = false;
    private int clickCount = 0;

    public RecommendObject(Context context) {
        this.context = context;
    }

    private boolean isAppInstalled(String uri) {
        isRewareded = RecommendManager.getInstance(context).rewAppInstalled(uri);
        if (isRewareded)
            return true;
        PackageManager pm = context.getPackageManager();
        boolean app_installed;
        try {
            pm.getPackageInfo(uri, PackageManager.GET_ACTIVITIES);
            app_installed = true;
            RecommendManager.getInstance(context).putRewAppToInstall(uri);
            RecommendManager.getInstance(context).rewardForInstall(uri);
        }
        catch (PackageManager.NameNotFoundException e) {
            app_installed = false;
        }
        return app_installed;
    }

    public boolean isInstelled() {
        return isInstelled;
    }

    public String getAppIcon() {
        return appIcon;
    }

    public void setAppIcon(String appIcon) {
        this.appIcon = appIcon;
    }

    public String getAppPackage() {
        return appPackage;
    }

    public void setAppPackage(String appPackage) {
        this.isInstelled = isAppInstalled(appPackage);
        this.appPackage = appPackage;
    }

    public String getAppDescr() {
        return appDescr;
    }

    public void setAppDescr(String appDescr) {
        this.appDescr = appDescr;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public int getClickCount() {
        return clickCount;
    }

    public void setClickCount() {
        clickCount++;
    }
}
