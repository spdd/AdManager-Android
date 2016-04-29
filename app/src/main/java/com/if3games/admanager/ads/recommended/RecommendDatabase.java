package com.if3games.admanager.ads.recommended;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.if3games.admanager.R;
import com.if3games.admanager.ads.AdsManager;
import com.if3games.admanager.ads.utils.CSVReader;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by supergoodd on 28.09.15.
 */
public class RecommendDatabase extends SQLiteOpenHelper {

    private static String DB_PATH = "/data/data/" + AdsManager.getInstance().getAppContext().getPackageName() + "/databases/";

    private static String DB_NAME = "recommend.sqlite";

    private static String DB_CREATE_RECOMMENDED =
            "CREATE TABLE recommended (" +
                    "    _id TEXT PRIMARY KEY, " +
                    "    app_name TEXT, " +
                    "    app_descr TEXT, " +
                    "    app_package TEXT, " +
                    "    app_icon TEXT, " +
                    "    is_installed INTEGER, " +
                    "    click_count INTEGER, " +
                    "    cost REAL " +
                    "); ";

    private static String DB_CREATE_APP_TYPE =
            "CREATE TABLE apptype (" +
                    "    _id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "    type INTEGER, " +
                    "    rec_id TEXT NOT NULL " +
                    "); ";

    private SQLiteDatabase db;

    private final Context context;

    public RecommendDatabase(Context c) {
        super(c, DB_NAME, null, 1);
        this.context = c;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(DB_CREATE_RECOMMENDED);
        db.execSQL(DB_CREATE_APP_TYPE);
        //parseAndInsertCSVRec(db);
        //parseAndInsertCSVAppTypes(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE ID EXISTS " + "recommended");
        db.execSQL("DROP TABLE ID EXISTS " + "apptype");
        onCreate(db);
    }

    @Override
    public SQLiteDatabase getWritableDatabase() {
        SQLiteDatabase sqlite = super.getWritableDatabase();
        db = sqlite;
        return sqlite;
    }

    public int getLastRecRowId(int pos) {
        Cursor cursor = db.rawQuery("SELECT * from recommended;", null);
        boolean success = cursor.moveToPosition(pos);
        if (success) {
            int lastRow = cursor.getInt(0);
            return lastRow;
        } else {
            return -1;
        }
    }

    public int getLastTypeRowId(int pos) {
        Cursor cursor = db.rawQuery("SELECT * from apptype;", null);
        boolean success = cursor.moveToPosition(pos);
        if (success) {
            int lastRow = cursor.getInt(0);
            return lastRow;
        } else {
            return -1;
        }
    }

    public void parseAndInsertCSVRec(SQLiteDatabase db1) {
        String next[] = {};
        try {
            CSVReader reader = new CSVReader(new InputStreamReader(context.getResources()
                    .openRawResource(R.raw.recommended)));
            for(int i = 0;;i++) {
                next = reader.readNext();
                if(next != null) {
                    if(i == 0) continue;
                    insertRec(next[0], next[1], next[2], next[3], next[4]);
                } else {
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void parseAndInsertCSVAppTypes(SQLiteDatabase db1) {
        String next[] = {};
        try {
            CSVReader reader = new CSVReader(new InputStreamReader(context.getResources()
                    .openRawResource(R.raw.apptype)));
            for(int i = 0;;i++) {
                next = reader.readNext();
                if(next != null) {
                    if(i == 0) continue;
                    insertAppType(Integer.parseInt(next[1]), next[2]);
                } else {
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public long insertRec(String _id, String appname,
                              String appdescr, String appackage, String appicon) {

        ContentValues in = new ContentValues();
        in.put("_id", _id);
        in.put("app_name", appname);
        in.put("app_descr", appdescr);
        in.put("app_package", appackage);
        in.put("app_icon", appicon);

        return db.insert("recommended", null, in);
    }

    public long insertAppType(int type, String rec_id) {
        ContentValues in = new ContentValues();
        in.put("type", type);
        in.put("rec_id", rec_id);

        return db.insert("apptype", null, in);
    }

    public long updateRec(String _id, String appname,
                              String appdescr, String appackage, String appicon) {

        ContentValues in = new ContentValues();
        in.put("_id", _id);
        in.put("app_name", appname);
        in.put("app_descr", appdescr);
        in.put("app_package", appackage);
        in.put("app_icon", appicon);
        in.put("click_count", 0);
        int id = Integer.parseInt(_id.replace("p", ""));
        in.put("cost", getCost(id, 0));

        return db.update("recommended", in, "_id=?", new String[]{_id});
    }

    public long updateAppType(int type, String rec_id) {
        ContentValues in = new ContentValues();
        in.put("type", type);
        in.put("rec_id", rec_id);

        return db.update("apptype", in, "rec_id=?", new String[]{ rec_id });
    }

    public void updateDBFromCSV(int updaterInt, boolean isUpdate) {
        parseAndUpdateCSVRec(db, updaterInt, isUpdate);
        parseAndUpdateCSVAppTypes(db, updaterInt, isUpdate);
    }

    public void parseAndUpdateCSVRec(SQLiteDatabase db1, int updaterInt, boolean isUpdate) {
        String next[] = {};
        try {
            CSVReader reader = new CSVReader(new InputStreamReader(context.getResources()
                    .openRawResource(getResourceIdFromString(context, "raw", "recommended"))));
            for(int i = 0;;i++) {
                next = reader.readNext();
                if(next != null) {
                    if(i == 0) continue;

                    if(i < updaterInt) continue;
                    if (updaterInt != 0 && !isUpdate) {
                        insertRec(next[0], next[1], next[2], next[3], next[4]);
                    } else {
                        updateRec(next[0], next[1], next[2], next[3], next[4]);
                    }
                } else {
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void parseAndUpdateCSVAppTypes(SQLiteDatabase db1, int updaterInt, boolean isUpdate) {
        String next[] = {};
        try {
            CSVReader reader = new CSVReader(new InputStreamReader(context.getResources()
                    .openRawResource(getResourceIdFromString(context, "raw", "apptype"))));
            for(int i = 0;;i++) {
                next = reader.readNext();
                if(next != null) {
                    if(i == 0) continue;
                    if(i < updaterInt) continue;
                    if (updaterInt != 0 && !isUpdate) {
                        insertAppType(Integer.parseInt(next[1]), next[2]);
                    } else {
                        updateAppType(Integer.parseInt(next[1]), next[2]);
                    }
                } else {
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<RecommendObject> getRecObjects(int appType) {
        List<RecommendObject> objs = new ArrayList<RecommendObject>();
        Cursor c = db.rawQuery(
                "SELECT a._id, " +
                        "r.app_name, r.app_descr, app_package, r.app_icon, click_count " +
                        "FROM apptype a " +
                        "JOIN recommended r ON a.rec_id=r._id " +
                        "WHERE a.type=" + appType + " ORDER BY r.cost",
                null
        );
        while(c.moveToNext()) {
            RecommendObject rec = new RecommendObject(context);
            rec.setAppPackage(c.getString(3));
            boolean isInstalled = rec.isInstelled();
            if (isInstalled)
                continue;

            int clicked = c.getInt(5);
            if (clicked > 2)
                continue;
            rec.setId(c.getInt(0));
            rec.setAppName(c.getString(1));
            rec.setAppDescr(c.getString(2));
            rec.setAppPackage(c.getString(3));
            rec.setAppIcon(c.getString(4));
            objs.add(rec);
        }
        c.close();
        return objs;
    }

    public void storeRecommended(RecommendObject object) {
        int click_count = 0;
        String row_id = null;
        Cursor c = db.rawQuery("SELECT _id, click_count FROM recommended WHERE _id=?;", new String[]{ "p"+object.getId() });
        while(c.moveToNext()) {
            row_id = c.getString(0);
            click_count += c.getInt(1);
        }
        c.close();

        ContentValues values = new ContentValues();
        values.put("cost", getCost(object.getId(), click_count+1));
        values.put("is_installed", object.isInstelled() ? 1 : 0);
        values.put("click_count", click_count + 1);

        if(row_id != null) {
            db.update("recommended", values, "_id=?", new String[]{ row_id });
        } else {
            db.insert("recommended", null, values);
        }
    }

    private float getCost(int id, int click_count) {
        return 1.0F/(1.0F + (id + click_count));
    }

    protected int getResourceIdFromString(Context context, String category, String resourceStr) {
        int res = context.getResources().getIdentifier(
                String.format(
                        AdsManager.getInstance().getAppContext().getPackageName() + ":%s/%s", category, resourceStr.toLowerCase()
                ),
                null, null
        );
        return res;
    }
}
