package com.if3games.admanager.ads.config;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;

import com.if3games.admanager.ads.AdsConstants;
import com.if3games.admanager.ads.AdsManager;
import com.if3games.admanager.ads.common.InstanceFactory;
import com.if3games.admanager.ads.utils.Logger;
import com.if3games.admanager.ads.utils.SettingsManager;
import com.if3games.admanager.ads.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;

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
        void onConfigLoaded(JSONObject config);
    }
    private Listener mListener;
    private Context mContext;
    //private String adParseConfig = null;

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

    /*
    private void doParse() {
        ParseConfig.getInBackground(new ConfigCallback() {
            @Override
            public void done(ParseConfig config, ParseException e) {
                adParseConfig = config.getString("ad_config");
                Logger.log(String.format("Config from Parse is %s!", adParseConfig));
                runAdJob();
            }
        });
    }
    */

    private JSONObject loadFromParse() throws JSONException {
        try {
            long loadedTime = SettingsManager.getLongValue(SettingsManager.PARSE_LOAD_TIME);
            if (!Utils.isLoaderTimeout(loadedTime)) {
                JSONObject json = new JSONObject(SettingsManager.getStringValue(SettingsManager.ADCONFIG_PARSE_KEY));
                return json;
            } else {
                return loadFromUrl(AdsConstants.ServerType.SERVER);
            }
        } catch (Exception e) {
            return loadFromUrl(AdsConstants.ServerType.SERVER);
        }
    }

    private JSONObject loadFromFile() throws JSONException {
        //JSONObject obj = new JSONObject(loadJSONFromAsset());
        JSONObject obj = new JSONObject(loadJSONFromPrefs());
        return obj;
    }

    private String loadJSONFromPrefs() {
        return SettingsManager.getStringValue(SettingsManager.ADCONFIG_UNITY_KEY);
    }

    public String loadJSONFromAsset() {
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

    private JSONObject loadFromUrl(AdsConstants.ServerType serverType) {
        try {
            Logger.log("Loading config from Server");

            InputStream inputStream = null;
            URL url = new URL(InstanceFactory.getInstance().createServer(serverType));
            URLConnection conn = url.openConnection();

            try {
                HttpURLConnection httpConn = (HttpURLConnection)conn;
                httpConn.setRequestMethod("GET");
                if (serverType == AdsConstants.ServerType.PARSE) {
                    /*
                    httpConn.setRequestProperty("X-Parse-Application-Id"
                            , ConstantsManager.getInstance().getConstants().PARSE_APP_ID);
                    httpConn.setRequestProperty("X-Parse-REST-API-Key",
                            ConstantsManager.getInstance().getConstants().PARSE_REST_API_KEY);
                            */
                }
                httpConn.setConnectTimeout(AdsConstants.SERVER_NOT_RESPONDING_TIMEOUT);
                httpConn.connect();

                if (httpConn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    inputStream = httpConn.getInputStream();
                } else {
                    return null;
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
                    return null;
                }
                if (result == null || result.isEmpty() || result.equals(" ")) {
                    return null;
                }

                JSONObject json = new JSONObject(result);
                Logger.log("Config From Server HttpConnection: " + json.toString());
                if (serverType == AdsConstants.ServerType.PARSE)
                    return new JSONObject(json.getJSONObject("params").getString("ad_config"));
                else
                    return json;
            } catch (IOException e) {
                e.printStackTrace();
                Logger.log(e.getMessage());
                return null;
            } finally {
                if (inputStream != null) {
                    inputStream.close();
                }
            }
        } catch (Exception e) {
            Logger.log(e.getMessage());
            return null;
        }
    }

    private class JobAsyncTask extends AsyncTask<Void, Void, JSONObject> {

        @Override
        protected JSONObject doInBackground(Void... params) {
            try {
                JSONObject jsonObj = loadFromFile();
                String configFromUrl = jsonObj.getString("config_from_url");
                if (configFromUrl != null && configFromUrl.trim().equals("0")) {
                    return jsonObj;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            switch (AdsConstants.SERVER_TYPE) {
                case PARSE:
                    JSONObject res1 = null;
                    try {
                        res1 = loadFromParse();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    if (res1 != null) {
                        SettingsManager.setStringValue(SettingsManager.ADCONFIG_PARSE_KEY, res1.toString());
                        return res1;
                    } else {
                        try {
                            if (getAdConfig(AdsConstants.ServerType.PARSE) == null)
                                return loadFromFile();
                            else
                                return new JSONObject(getAdConfig(AdsConstants.ServerType.PARSE));
                        } catch (JSONException e) {
                            e.printStackTrace();
                            return null;
                        }
                    }
                case SERVER:
                    JSONObject res2 = loadFromUrl(AdsConstants.ServerType.SERVER);
                    if (res2 != null) {
                        SettingsManager.setStringValue(SettingsManager.ADCONFIG_URL_KEY, res2.toString());
                        return res2;
                    } else {
                        try {
                            JSONObject json = new JSONObject(getAdConfig(AdsConstants.ServerType.SERVER));
                            if (json == null)
                                return loadFromFile();
                            else
                                return json;
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                case FILE:
                    try {
                        return loadFromFile();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                default:
                    try {
                        return loadFromFile();
                    } catch (JSONException e) {
                        e.printStackTrace();
                        return null;
                    }
            }
        }

        @Override
        protected void onPostExecute(JSONObject json) {
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

    private String getAdConfig(AdsConstants.ServerType type) {
        switch (type) {
            case PARSE:
                return SettingsManager.getStringValue(SettingsManager.ADCONFIG_PARSE_KEY);
            case SERVER:
                return SettingsManager.getStringValue(SettingsManager.ADCONFIG_URL_KEY);
            default:
                return null;
        }
    }
}
