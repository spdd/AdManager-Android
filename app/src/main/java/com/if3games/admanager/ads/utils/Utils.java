package com.if3games.admanager.ads.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.text.TextUtils;

import com.if3games.admanager.ads.AdsConstants;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by supergoodd on 01.10.15.
 */
public class Utils {
    public static Map<String, String> jsonStringToMap(String jsonParams) throws JSONException {
        Map<String, String> jsonMap = new HashMap<String, String>();

        if (TextUtils.isEmpty(jsonParams)) return jsonMap;

        JSONObject jsonObject = (JSONObject) new JSONTokener(jsonParams).nextValue();
        Iterator<?> keys = jsonObject.keys();

        while (keys.hasNext()) {
            String key = (String) keys.next();
            jsonMap.put(key, jsonObject.getString(key));
        }

        return jsonMap;
    }

    public static String mapToJsonString(Map<String, String> map) {
        if (map == null) {
            return "{}";
        }

        StringBuilder builder = new StringBuilder();
        builder.append("{");
        boolean first = true;

        for (Map.Entry<String, String> entry : map.entrySet()) {
            if (!first) {
                builder.append(",");
            }
            builder.append("\"");
            builder.append(entry.getKey());
            builder.append("\":\"");
            builder.append(entry.getValue());
            builder.append("\"");
            first = false;
        }

        builder.append("}");
        return builder.toString();
    }

    public static boolean isNetworkConnected(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();
        if (ni == null) {
            // There are no active networks.
            return false;
        } else
            return true;
    }

    public static boolean isLoaderTimeout(long savedTime) {
        long millis = System.currentTimeMillis() - savedTime;
        int seconds = (int) (millis / 1000);
        int minutes = seconds / 60;
        int hours = minutes / 60;
        Logger.log(String.format("Loaded %d hours ago", hours));
        if (hours > AdsConstants.CONFIG_STORE_TIMEOUT)
            return true;
        else
            return false;
    }

    public static boolean isAdOverClickerTimeout(long savedTime) {
        long millis = System.currentTimeMillis() - savedTime;
        int seconds = (int) (millis / 1000);
        int minutes = seconds / 60;
        int hours = minutes / 60;
        Logger.log(String.format("Was clicked %d hours ago", hours));
        if (hours > AdsConstants.ANTICLICKER_TIMEOUT)
            return true;
        else
            return false;
    }
}
