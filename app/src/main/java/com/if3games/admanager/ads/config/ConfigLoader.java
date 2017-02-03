package com.if3games.admanager.ads.config;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;

import com.google.gson.Gson;
import com.if3games.admanager.ads.AdsConstants;
import com.if3games.admanager.ads.AdsManager;
import com.if3games.admanager.ads.utils.Logger;
import com.if3games.admanager.ads.utils.SettingsManager;
import com.if3games.admanager.ads.utils.Utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

/**
 * Created by supergoodd on 30.09.15.
 */
public class ConfigLoader {
    public interface Listener {
        void onConfigFailedToLoad(int errorCode);
        void onConfigLoaded(AdConfig config);
    }
    private Listener mListener;
    public Context mContext;

    public ConfigLoader(Context context, Listener listener) {
        this.mContext = context;
        this.mListener = listener;
    }

    public void runAdJob() {
        if (Build.VERSION.SDK_INT >= 11) {
            new JobAsyncTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } else {
            new JobAsyncTask().execute();
        }
    }

    public void runConfig() {
        runAdJob();
    }

    private AdConfig loadFromFile(String config) {
        if (config == null && Utils.hasValidConfig()) {
            Logger.log("Config from cache: " + Utils.getAdConfig());
            //mListener.onConfigLoaded(getDataDict(config));
            return getDataDict(config);
        }

        AdConfig json = getDataDict(config);
        if (json == null) {
            String result = getLocalFileConfig();
            try {
                Gson gson = new Gson();
                json = gson.fromJson(result, AdConfig.class);
                //json = new JSONObject(result);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return json;
    }

    private String loadJSONFromPrefs() {
        return SettingsManager.getStringValue(SettingsManager.ADCONFIG_UNITY_KEY);
    }

    public String getLocalFileConfig() {
        String json = null;
        try {
            int res = mContext.getResources().getIdentifier(
                    String.format(
                            AdsManager.getInstance().getAppContext().getPackageName() + ":raw/%s", "response"
                    ),
                    null, null
            );
            InputStream is = mContext.getResources()
                    .openRawResource(res); //mContext.getAssets().open("response.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
            Logger.log("From file: " + json);
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return json;
    }

    private AdConfig loadFromUrl(String urlString) {
        try {
            Logger.log("Loading config from URL: " + urlString);

            InputStream inputStream = null;
            URL url = new URL(urlString);
            URLConnection conn = url.openConnection();

            try {
                HttpURLConnection httpConn = (HttpURLConnection)conn;
                httpConn.setRequestMethod("GET");
                httpConn.setConnectTimeout(AdsConstants.SERVER_NOT_RESPONDING_TIMEOUT);
                httpConn.connect();

                if (httpConn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    inputStream = httpConn.getInputStream();
                } else {
                    return loadFromFile(null);
                }
                BufferedReader br = new BufferedReader(new InputStreamReader((inputStream)));
                StringBuilder sb = new StringBuilder();
                String output;
                while ((output = br.readLine()) != null) {
                    sb.append(output);
                }
                br.close();
                httpConn.disconnect();

                String result = sb.toString();
                if (result == null) {
                    return loadFromFile(null);
                }
                if (result == null || result.isEmpty() || result.equals(" ")) {
                    return loadFromFile(null);
                }
                Logger.log("Config From custom URL: " + result);
                return loadFromFile(result);

            } catch (IOException e) {
                e.printStackTrace();
                Logger.log(e.getMessage());
                return loadFromFile(null);
            } finally {
                if (inputStream != null) {
                    inputStream.close();
                }
            }
        } catch (Exception e) {
            Logger.log(e.getMessage());
            return loadFromFile(null);
        }
    }

    private AdConfig getDataDict(String config) {
        String json;
        boolean isLocalConfig = false;
        if (config == null) {
            json = loadJSONFromPrefs();
            isLocalConfig = true;
        } else {
            json = config;
        }
        AdConfig result = null;
        try {
            Gson gson = new Gson();
            result = gson.fromJson(json, AdConfig.class);
            //result = new JSONObject(json);
            // save ad config for 24 hours (only from internet!), if ad config not available load from cache
            if(!Utils.hasValidConfig() && !isLocalConfig) {
                Logger.log("Save Config");
                Utils.saveAdConfig(json);
            }
        } catch (Exception e) {
            Logger.log("Config json error");
            e.printStackTrace();
        }
        return result;
    }

    public AdConfig runTask() {
        if (Utils.hasValidConfig()) {
            String config = Utils.getAdConfig();
            Logger.log("Config from cache: " + config);
            //mListener.onConfigLoaded(getDataDict(config));
            return getDataDict(config);
        }

        AdConfig configJson = getDataDict(null);
        String configFromUrl = configJson.getIsConfigFromUrl();

        if (configFromUrl != null && configFromUrl.trim().equals("0")) {
            return loadFromFile(null);
        }

        String urlString = configJson.getConfigUrl();
        if (urlString != null && !urlString.equals(""))
            return loadFromUrl(urlString);
        else
            return loadFromFile(null);
    }

    public AdConfig runTaskWithConfig(String config) {
        return loadFromFile(config);
    }

    public AdConfig fetch() {
        return runTask();
    }


    private class JobAsyncTask extends AsyncTask<Void, Void, AdConfig> {

        @Override
        protected AdConfig doInBackground(Void... params) {
            return fetch();
        }

        @Override
        protected void onPostExecute(AdConfig json) {
            super.onPostExecute(json);
            try {
                if (mListener != null) {
                    if (json == null) {
                        mListener.onConfigFailedToLoad(0);
                    } else {
                        mListener.onConfigLoaded(json);
                    }
                }
            } catch (Exception e) {
                Logger.log(e.getMessage());
            }
        }
    }
}
