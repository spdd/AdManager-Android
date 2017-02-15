package com.if3games.admanager.ads.config;

import android.content.Context;
import android.support.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.if3games.admanager.ads.utils.Logger;

/**
 * Created by supergoodd on 25.01.17.
 */

public class FirebaseConfigLoader extends ConfigLoader {
    private String mRemoteConfig = null;

    public FirebaseConfigLoader(Context context, Listener listener) {
        super(context, listener);
    }

    @Override
    public void runConfig() {
        final FirebaseRemoteConfig firebaseRemoteConfig =
                FirebaseRemoteConfig.getInstance();
        firebaseRemoteConfig.fetch()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Logger.log("Config fetched!");
                            firebaseRemoteConfig.activateFetched();

                            String configKey = getConfigKey();
                            Logger.log("Config key: " + configKey);
                            Logger.log("Fire config: " + firebaseRemoteConfig.getString(configKey));
                            if (firebaseRemoteConfig.getString(configKey) != null && firebaseRemoteConfig.getString(configKey).length() != 0) {
                                mRemoteConfig = firebaseRemoteConfig.getString(configKey);
                            } else {
                                Logger.log("Config not fetched");
                            }
                            runAdJob();
                        } else {
                            Logger.log("Config not fetched");
                            runAdJob();
                        }
                    }
                });
    }

    @Override
    public AdConfig fetch() {
        if (mRemoteConfig == null)
            return runTask();
        else
            return runTaskWithConfig(mRemoteConfig);
    }

    private String getConfigKey() {
        String[] packageParts = mContext.getPackageName().split("\\.");
        String prefix = packageParts[packageParts.length - 1].replace("-", "");
        return String.format("%s_ad_config", prefix);
    }
}