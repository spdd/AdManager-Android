package com.if3games.admanager.ads.recommended;

import android.content.Context;
import android.database.SQLException;
import android.os.AsyncTask;
import android.os.Build;

import com.if3games.admanager.ads.AdsConstants;
import com.if3games.admanager.ads.AdsManager;
import com.if3games.admanager.ads.common.InstanceFactory;
import com.if3games.admanager.ads.utils.Logger;
import com.if3games.admanager.ads.utils.SettingsManager;
import com.if3games.admanager.ads.utils.Utils;

import org.json.JSONArray;
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
 * Created by supergoodd on 10.10.15.
 */
public class RecommendLoader {
    private Context mContext;
    //private String recParseConfig = null;
    private static final String REQUEST_ID = "recId";

    public interface Listener {
        void onConfigFailedToLoad(int errorCode);
        void onConfigLoaded(int request_id);
    }
    private Listener mListener;

    public RecommendLoader(Context context, Listener listener) {
        this.mContext = context;
        this.mListener = listener;
        runRecJob();
    }

    private void runRecJob() {
        if (Build.VERSION.SDK_INT >= 11) {
            new JobRecAsyncTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } else {
            new JobRecAsyncTask().execute();
        }
    }

    /*
    private void doParse() {
        ParseConfig.getInBackground(new ConfigCallback() {
            @Override
            public void done(ParseConfig config, ParseException e) {
                recParseConfig = config.getString("rec_config");
                Logger.log(String.format("Rec Config from Parse is %s!", recParseConfig));
                runRecJob();
            }
        });
    }
    */

    private boolean loadFromParse() {
        try {
            long loadedTime = SettingsManager.getLongValue(SettingsManager.PARSE_LOAD_TIME);
            if (!Utils.isLoaderTimeout(loadedTime)) {
                return true;
            } else {
                return loadFromUrl(AdsConstants.ServerType.PARSE);
            }
        } catch (Exception e) {
            return loadFromUrl(AdsConstants.ServerType.SERVER);
        }
    }

    private boolean loadFromFile() {
        try {
            JSONObject obj = new JSONObject(loadJSONFromAsset());
            return updateRecDB(obj);
        } catch (JSONException e) {
            e.printStackTrace();
            return false;
        }

    }

    public String loadJSONFromAsset() {
        String json = null;
        try {
            int res = mContext.getResources().getIdentifier(
                    String.format(
                            AdsManager.getInstance().getAppContext().getPackageName() + ":raw/%s", "recommended"
                    ),
                    null, null
            );
            InputStream is = mContext.getResources()
                    .openRawResource(res);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
            Logger.log("Recommended From file: " + json);
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return json;
    }

    private boolean loadFromUrl(AdsConstants.ServerType serverType) {
        try {
            Logger.log("Loading Recommended config from Server");

            InputStream inputStream = null;
            URL url = new URL(InstanceFactory.getInstance().createRecServer(serverType));
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
                    return loadFromFile();
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
                    return loadFromFile();
                }
                if (result == null || result.isEmpty() || result.equals(" ")) {
                    return loadFromFile();
                }
                final JSONObject json = new JSONObject(result);
                Logger.log("From Server HttpConnection: " + json.toString());
                if (serverType == AdsConstants.ServerType.PARSE) {
                    if (json.getJSONObject("params").has("ad_config")) {
                        SettingsManager.setStringValue(SettingsManager.ADCONFIG_PARSE_KEY,
                                json.getJSONObject("params").getString("ad_config"));
                    }
                    if (json.getJSONObject("params").has("show_freq")) {
                        SettingsManager.setIntValue(SettingsManager.SHOW_AD_FREQ_KEY,
                                json.getJSONObject("params").getInt("show_freq"));
                    }
                    SettingsManager.setLongValue(SettingsManager.PARSE_LOAD_TIME, System.currentTimeMillis());
                    return updateRecDB(new JSONObject(json.getJSONObject("params").getString("rec_config")));
                } else {
                    return updateRecDB(json);
                }
            } catch (IOException e) {
                e.printStackTrace();
                Logger.log(e.getMessage());
                return loadFromFile();
            } finally {
                if (inputStream != null) {
                    inputStream.close();
                }
            }
        } catch (Exception e) {
            Logger.log(e.getMessage());
            return loadFromFile();
        }
    }

    private boolean updateRecDB(JSONObject obj) {
        try {
            int request_id = obj.getInt("id");
            if (request_id <= SettingsManager.getIntValue(REQUEST_ID)) {
                return true;
            } else {
                SettingsManager.setIntValue(REQUEST_ID, request_id);
            }
            JSONArray recArray = obj.getJSONArray("recs");
            JSONArray atypesArray = obj.getJSONArray("apptypes");
            if(recArray != null && atypesArray != null) {
                RecommendDatabase db = new RecommendDatabase(mContext);
                try {
                    db.getWritableDatabase();
                } catch (SQLException e) {
                    throw new Error("Unable to open database");
                }
                for (int i = 0; i < recArray.length(); i++) {
                    String appname = recArray.getJSONObject(i).getString("app_name");
                    String appDescr = recArray.getJSONObject(i).getString("app_descr");
                    String appPackage = recArray.getJSONObject(i).getString("app_package");
                    String appIcon = recArray.getJSONObject(i).getString("app_icon");
                    if (db.getLastRecRowId(i) == -1) {
                        db.insertRec(String.format("p%d", (i + 1)), appname, appDescr, appPackage, appIcon);
                    } else {
                        String rec_id = String.format("p%d", (i + 1));
                        long row=db.updateRec(rec_id, appname, appDescr, appPackage, appIcon);
                        Logger.log(String.format("update rec: %s/%d", rec_id, row));
                    }
                }
                for (int i = 0; i < atypesArray.length(); i++) {
                    int type = atypesArray.getJSONObject(i).getInt("type");
                    String recId = atypesArray.getJSONObject(i).getString("rec_id");
                    if (db.getLastTypeRowId(i) == -1) {
                        db.insertAppType(type, recId);
                    } else {
                        long row = db.updateAppType(type, recId);
                        Logger.log(String.format("update apptype: %s/%d", recId, row));
                    }
                }
                db.close();
                return true;
            }
        } catch (JSONException e) {
            Logger.log(e.getMessage());
            return false;
        }
        return false;
    }

    private class JobRecAsyncTask extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Void... params) {
            switch (AdsConstants.SERVER_TYPE) {
                case PARSE:
                    return loadFromParse();
                case SERVER:
                    return loadFromUrl(AdsConstants.ServerType.SERVER);
                case FILE:
                    return loadFromFile();
                default:
                    return loadFromFile();
            }
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            if (mListener != null) {
                if (result)
                    mListener.onConfigLoaded(SettingsManager.getIntValue(REQUEST_ID));
                else
                    mListener.onConfigFailedToLoad(0);
            }
        }
    }
}
