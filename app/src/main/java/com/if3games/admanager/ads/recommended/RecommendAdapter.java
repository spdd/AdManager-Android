package com.if3games.admanager.ads.recommended;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.if3games.admanager.R;
import com.if3games.admanager.ads.AdsManager;
import com.if3games.admanager.ads.utils.ImageManager;
import com.if3games.admanager.ads.utils.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;

/**
 * Created by supergoodd on 27.09.15.
 */
public class RecommendAdapter extends BaseAdapter {
    private Context context;
    private List<RecommendObject> recApps;

    public RecommendAdapter(Context c, List<RecommendObject> l) {
        context = c;
        recApps = l;
    }

    @Override
    public int getCount() {
        if(recApps != null) {
            return recApps.size();
        } else {
            return 0;
        }
    }

    @Override
    public Object getItem(int index) {
        return recApps.get(index);
    }

    @Override
    public long getItemId(int index) {
        return index;
    }

    @Override
    public boolean areAllItemsEnabled() {
        return false;
    }

    @Override
    public boolean isEnabled(int index) {
        return true;
    }
    static class ViewHolder {
        ImageView icon;
        TextView recAppTitle;
        TextView recAppDescr;
        Button recAppButton;
    }

    @Override
    public View getView(final int index, View convertView, ViewGroup parent) {
        ViewHolder holder;
        final RecommendObject recObj = recApps.get(index);

        LayoutInflater li = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if(convertView == null) {
            convertView = li.inflate(R.layout.rec_game_item, null);
            holder = new ViewHolder();
            holder.icon = (ImageView) convertView.findViewById(R.id.rec_icon);
            holder.recAppTitle = (TextView) convertView.findViewById(R.id.recAppTitle);
            holder.recAppDescr = (TextView) convertView.findViewById(R.id.recAppDescr);
            holder.recAppButton = (Button) convertView.findViewById(R.id.recButton);
            holder.recAppButton.setBackgroundResource(R.drawable.button_green_rect);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        convertView.setFocusable(true);
        convertView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                goToMarket(recObj);
            }
        });

        int r = context.getResources().getIdentifier(
                String.format(
                        AdsManager.getInstance().getAppContext().getPackageName() + ":drawable/%s", recObj.getAppIcon()), null, null);
        if (r == 0) { // not found

            String urlStr = recObj.getAppIcon();
            ImageManager.getInstance().fetchBitmapOnThread(urlStr, holder.icon);
            ImageManager.getInstance().setImageListener(urlStr, new ImageManager.Listener() {
                @Override
                public void onImageFailedToLoad(ImageView iv) {
                    iv.setImageResource(R.drawable.ic_fw);
                }
            });
            /*
            Bitmap bitmap = loadImageFromWebOperations(recObj.getAppIcon());
            if (bitmap != null) {
                holder.icon.setImageBitmap(bitmap);
            } else {
                r = R.drawable.ic_launcher;
                holder.icon.setImageResource(r);
            }
            */

        } else {
            holder.icon.setImageResource(r);
        }

        holder.recAppTitle.setText(recObj.getAppName());
        holder.recAppDescr.setText(recObj.getAppDescr());
        holder.recAppButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                goToMarket(recObj);
            }
        });
        return convertView;
    }

    private void goToMarket(RecommendObject recObj) {
        RecommendManager.getInstance(context).storeRecObject(context, recObj);
        RecommendManager.getInstance(context).putApp(recObj.getAppPackage(), false);
        Uri uri = Uri.parse("market://details?id=" + recObj.getAppPackage()); // FOR AMAZON: http://www.amazon.com/gp/mas/dl/android?p=
        Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);                                // FOR GOOGLE PLAY: market://details?id=
        try {
            context.startActivity(goToMarket);
        } catch (ActivityNotFoundException e) {
        }
    }

    private void showAlert(int lvl) {
        /*
        new AlertDialogPro.Builder(context, R.style.Theme_AlertDialogPro_Holo_Light)
                .setTitle(R.string.str_ls_alert_title)
                .setMessage(msg)
                .setPositiveButton("OK", null)
                .show();
                */
        // TODO: new alert dialog
    }

    public Bitmap loadImageFromWebOperations(String url) {
        try {
            InputStream is = fetch(url);
            Bitmap bitmap = BitmapFactory.decodeStream(is);
            return bitmap;
        } catch (Exception e) {
            return null;
        }
    }

    private InputStream fetch(String urlString) throws IOException {
        InputStream inputStream = null;
        URL url = new URL(urlString);
        URLConnection conn = url.openConnection();

        try{
            HttpURLConnection httpConn = (HttpURLConnection)conn;
            httpConn.setRequestMethod("GET");
            httpConn.connect();

            if (httpConn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                inputStream = httpConn.getInputStream();
            }
        }
        catch (Exception ex) {
            Logger.log(ex.getMessage());
        }
        return inputStream;
    }
}
