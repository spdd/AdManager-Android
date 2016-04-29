package com.if3games.admanager.ads.recommended;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.SQLException;
import android.view.View;
import android.widget.ListView;

import com.if3games.admanager.R;
import com.if3games.admanager.ads.utils.Logger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by supergoodd on 28.09.15.
 */
public class RecommendManager implements RecommendLoader.Listener {
    private static RecommendManager instance;
    private Map<String, Boolean> appsMap;
    private ListView recListView;
    private RecommendAdapter recAdapter;
    private List<RecommendObject> objects;

    private View tmpView;
    private int tmpResId;
    private static SharedPreferences settings;
    private static SharedPreferences.Editor edit;
    private int reqId;
    private boolean isItemClicked = false;
    private Context mContext;

    public static synchronized RecommendManager getInstance(Context context) {
        if (instance == null) {
            instance = new RecommendManager();
            instance.mContext = context;
            settings = context.getSharedPreferences("SETTINGS", Context.MODE_PRIVATE);
        } else {
            instance.mContext = context;
        }
        return instance;
    }

    public RecommendManager() {
        appsMap = new HashMap<String, Boolean>();
    }

    public void putApp(String packageName, boolean state) {
        if (!appsMap.containsKey(packageName))
            appsMap.put(packageName, state);
    }

    public void setRecListView(View context, int resId) {
        tmpView = context;
        tmpResId = resId;
        reqId = getRecId();
        new RecommendLoader(context.getContext(), this);
    }

    private void createRecListView(View view, int resId, int requestId) {
        if (!isItemClicked && objects != null && recAdapter != null && requestId == reqId) {
            view.findViewById(R.id.promoLayout).setVisibility(View.VISIBLE);
            setAdapterToListView(view, recAdapter);
            Logger.log("load cached rec objects");
        } else {
            isItemClicked = false;
            RecommendDatabase db = new RecommendDatabase(mContext);
            try {
                db.getWritableDatabase();
            } catch (SQLException e) {
                throw new Error("Unable to open database");
            }
            int appsType = 1; //FactoryManager.getInstance().getFactory().createAppType();
            objects = db.getRecObjects(appsType);
            db.close();

            if (objects.size() == 0) {
                view.findViewById(resId).setVisibility(View.GONE);
                return;
            }
            view.findViewById(R.id.promoLayout).setVisibility(View.VISIBLE);
            recAdapter = new RecommendAdapter(mContext, objects);
            setAdapterToListView(view, recAdapter);
        }
    }

    private void setAdapterToListView(View view, RecommendAdapter adapter) {
        recListView = (ListView) view.findViewById(R.id.promo_list);
        recListView.setAdapter(recAdapter);
        recListView.invalidateViews();
    }

    public void storeRecObject(Context context, RecommendObject recObj) {
        isItemClicked = true;
        RecommendDatabase db = new RecommendDatabase(context);
        try {
            db.getWritableDatabase();
        } catch (SQLException e) {
            throw new Error("Unable to open database");
        }
        db.storeRecommended(recObj);
        db.close();
    }

    @Override
    public void onConfigFailedToLoad(int errorCode) {
        tmpView.findViewById(tmpResId).setVisibility(View.GONE);
    }

    @Override
    public void onConfigLoaded(int request_id) {
        createRecListView(tmpView, tmpResId, request_id);
    }

    private int getRecId() {
        return settings.getInt("recId", -1);
    }

    public void putRewAppToInstall(String packageName) {
        edit = settings.edit();
        edit.putBoolean(packageName, true);
        edit.commit();
    }

    public boolean rewAppInstalled(String packageName) {
        return settings.getBoolean(packageName, false);
    }

    public void rewardForInstall(String packageName) {
        Logger.log("Reward for app install");
        if (appsMap.containsKey(packageName)) {
            appsMap.put(packageName, true);
        }

        //LevelManager.getInstance().setGamePoints(ConstantsManager.getInstance().getConstants().REWARD_FOR_APP_INSTALL);
    }

    public boolean isAppInstalled() {
        for (String key: appsMap.keySet()) {
            if (appsMap.get(key)) {
                appsMap.put(key, false);
                return true;
            }
        }
        return false;
    }

    public void onDestroy(Activity activity) {
    }

    public void onPause(Activity activity) {
    }

    public void onResume(Activity activity) {
    }
}
