package com.if3games.admanager.ads.adapters;

import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.if3games.admanager.R;
import com.if3games.admanager.ads.common.AdAgent;
import com.if3games.admanager.ads.config.AdUnit;
import com.if3games.admanager.ads.controllers.PrecacheListener;
import com.if3games.admanager.ads.recommended.RecommendManager;
import com.if3games.admanager.ads.utils.ImageManager;
import com.if3games.admanager.ads.utils.Logger;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

/**
 * Created by supergoodd on 02.10.15.
 */
public class PrecacheAdapter implements AdapterInterface {
    private static PrecacheAdapter instance;
    private PrecacheListener mListener;
    private BannerDialog bannerDialog;

    public static synchronized PrecacheAdapter getInstance(PrecacheListener listener) {
        if (instance == null) {
            instance = new PrecacheAdapter();
            instance.mListener = listener;
        }
        return instance;
    }

    @Override
    public String getAdName() {
        return "image";
    }

    @Override
    public void initAd(final Context context, AdUnit params) {
        String appDescr = params.app_descr; //.getString("app_descr");
        String imageUrl = params.banner_url; //.getString("banner_url");
        String storeUrl = params.store_url; //.getString("store_url");

        Logger.logAds(AdAgent.AdType.INTERSTITIAL, "initialize Precache image");
        bannerDialog = new BannerDialog(context, imageUrl, storeUrl, appDescr);
    }

    private class ImageDownloader extends AsyncTask<String, Void, Bitmap> {
        private String url;

        @Override
        protected Bitmap doInBackground(String... param) {
            url = param[0];
            return downloadBitmap(url);
        }

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            if (result != null) {
                mListener.onPrecacheLoaded(getAdName());
                ImageManager.getInstance().putBitmapFromUrl(url, result);
            } else {
                mListener.onPrecacheFailedToLoad(getAdName());
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

        private Bitmap downloadBitmap(String urlStr) {
            InputStream inputStream = null;
            try {
                // getting contents from the stream
                inputStream = fetch(urlStr);

                // decoding stream data back into image Bitmap that android understands
                final Bitmap bitmap = BitmapFactory.decodeStream(inputStream);

                return bitmap;
            } catch (Exception e) {
                // You Could provide a more explicit error message for IOException
                Log.e("ImageDownloader", "Something went wrong while" +
                        " retrieving bitmap from " + url + e.toString());
            }
            return null;
        }
    }

    private void goToMarket(Context context, String url) {
        mListener.onPrecacheClicked(getAdName());
        Uri uri = Uri.parse("market://details?id=" + url); // FOR AMAZON: http://www.amazon.com/gp/mas/dl/android?p=
        Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);                                // FOR GOOGLE PLAY: market://details?id=
        try {
            context.startActivity(goToMarket);
        } catch (ActivityNotFoundException e) {
        }
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public boolean isCached() {
        return false;
    }

    @Override
    public boolean isAutoLoadingVideo() {
        return false;
    }

    @Override
    public void showInterstitial() {
        try {
            if (bannerDialog != null) {
                bannerDialog.show();
                mListener.onPrecacheOpened(getAdName());
            }
        } catch (Exception e) {
            Logger.log(e.getMessage());
        }
    }

    public void showRewardedInterstitial(Context context) {
        try {
            BannerDialog rewDialog = new BannerDialog(context);
            rewDialog.show();
        } catch (Exception e) {
            Logger.log(e.getMessage());
        }
    }

    @Override
    public void showVideo() {
        // show video from youtube
    }

    @Override
    public void onStart(Context context) {

    }

    @Override
    public void onResume(Context context) {

    }

    @Override
    public void onPause(Context context) {

    }

    @Override
    public void onStop(Context context) {

    }

    @Override
    public void onDestroy(Context context) {
    }

    @Override
    public void onBackPressed() {
        if (bannerDialog != null)
            bannerDialog.dismiss();
    }

    class BannerDialog extends Dialog {
        private Context mContext = null;
        private String imageUrl = null;
        private String storeUrl = null;
        private String appDescr = null;

        public BannerDialog(Context context) {
            super(context);
            mContext = context;
        }

        public BannerDialog(Context context, String imageUrl, String storeUrl, String appDescr) {
            super(context);
            mContext = context;
            this.imageUrl = imageUrl;
            this.storeUrl = storeUrl;
            this.appDescr = appDescr;
            if (imageUrl != null && !ImageManager.getInstance().hasImageFromUrl(imageUrl))
                new ImageDownloader().execute(imageUrl);
            else
                mListener.onPrecacheLoaded(getAdName());
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            setContentView(R.layout.image_banner_layout);
            try {
                if (imageUrl == null) {
                    ((FrameLayout) findViewById(R.id.promo_frag)).setVisibility(View.VISIBLE);
                    ((ImageView)findViewById(R.id.adsImageId)).setVisibility(View.GONE);
                    ((LinearLayout) findViewById(R.id.playImageBannerLL)).setVisibility(View.GONE);
                    ((LinearLayout) findViewById(R.id.closeImageBannerLL))
                            .setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                    RecommendManager.getInstance(mContext).setRecListView(findViewById(R.id.image_banner_dialog_id), R.id.promo_frag);
                } else {
                    ImageView imageBanner = (ImageView) findViewById(R.id.adsImageId);
                    imageBanner.setImageBitmap(ImageManager.getInstance().fetchBitmap(imageUrl));
                    imageBanner.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            BannerDialog.this.dismiss();
                            goToMarket(mContext, storeUrl);
                        }
                    });
                }

                ((TextView) findViewById(R.id.adsImageTitleId)).setText(appDescr);

                Button playBtn = (Button) findViewById(R.id.adsPlayButton);
                playBtn.setBackgroundResource(R.drawable.button_green_rect);
                playBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        BannerDialog.this.dismiss();
                        goToMarket(mContext, storeUrl);
                    }
                });

                Button closeBtn = (Button) findViewById(R.id.adsCloseButton);
                closeBtn.setBackgroundResource(R.drawable.button_green_rect);
                closeBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        BannerDialog.this.dismiss();
                        mListener.onPrecacheClosed(getAdName());
                    }
                });
            } catch (Exception e) {
                Logger.log(e.getMessage());
            }

        }

        @Override
        protected void onStop() {
            super.onStop();
            mListener.onPrecacheClosed(getAdName());
        }
    }
}
