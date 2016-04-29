package com.if3games.admanager.ads.utils;

/**
 * Created by supergoodd on 11.10.15.
 */

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.widget.ImageView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;

public class ImageManager {
    public interface Listener {
        void onImageFailedToLoad(ImageView iv);
    }
    private Listener mListener;
    private static ImageManager instance;
    private final Map<String, Bitmap> imagesMap;
    private final Map<String, Listener> listenersMap;

    public static synchronized ImageManager getInstance() {
        if (instance == null) {
            instance = new ImageManager();
        }
        return instance;
    }

    private ImageManager() {
        imagesMap = new HashMap<String, Bitmap>();
        listenersMap = new HashMap<String, Listener>();
    }

    public void setImageListener(String url, Listener listener) {
        if (!listenersMap.containsKey(url))
            listenersMap.put(url, listener);
    }

    public void putBitmapFromUrl(String url, Bitmap bitmap) {
        if (!imagesMap.containsKey(url) && bitmap != null) {
            imagesMap.put(url, bitmap);
        }
    }

    public boolean hasImageFromUrl(String url) {
        if (imagesMap.containsKey(url))
            return true;
        else
            return false;
    }

    public Bitmap fetchBitmap(String urlString) {
        if (imagesMap.containsKey(urlString)) {
            return imagesMap.get(urlString);
        }

        Logger.log("image url:" + urlString);
        try {
            InputStream is = fetch(urlString);
            Bitmap bitmap = BitmapFactory.decodeStream(is);
            if (bitmap != null) {
                imagesMap.put(urlString, bitmap);
                Logger.log("got a thumbnail bitmap");
            } else {
                Logger.log( "could not get thumbnail");
            }

            return bitmap;
        } catch (MalformedURLException e) {
            Logger.log("fetchBitmap failed: " + e.getMessage());
            return null;
        } catch (IOException e) {
            Logger.log("fetchBitmap failed" + e.getMessage());
            return null;
        }
    }

    public void fetchBitmapOnThread(final String urlString, final ImageView imageView) {
        if (imagesMap.containsKey(urlString)) {
            imageView.setImageBitmap(imagesMap.get(urlString));
        }

        final Handler handler = new Handler() {
            @Override
            public void handleMessage(Message message) {
                if (((Bitmap) message.obj) == null) {
                    if (listenersMap.containsKey(urlString)) {
                        Logger.log("Failed load: " + urlString);
                        listenersMap.get(urlString).onImageFailedToLoad(imageView);
                    }
                } else {
                    imageView.setImageBitmap((Bitmap) message.obj);
                }
            }
        };

        Thread thread = new Thread() {
            @Override
            public void run() {
                //TODO : set imageView to a "pending" image
                Bitmap bitmap = fetchBitmap(urlString);
                Message message = handler.obtainMessage(1, bitmap);
                handler.sendMessage(message);
            }
        };
        thread.start();
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
        /*
        DefaultHttpClient httpClient = new DefaultHttpClient();
        HttpGet request = new HttpGet(urlString);
        HttpResponse response = httpClient.execute(request);
        return response.getEntity().getContent();
        */
    }

    public class CachedBitmap {
        private String imgPath;
        private boolean isLocalCached = false;
        private Bitmap bitmap;
        public CachedBitmap(Bitmap bitmap, String imgId) {
            this.bitmap = bitmap;
            try {
                this.isLocalCached = saveToSd(bitmap, imgId);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        }

        private boolean saveToSd(Bitmap bitmap, String imgId) throws MalformedURLException {
            String filepath = Environment.getExternalStorageDirectory().getAbsolutePath() +
                    "/" + "packagename" + "/img";
            File folder = new File(filepath);
            if(!folder.exists()) {
                folder.mkdirs();
            }
            Logger.log("Image Save: " + filepath);
            String extraPath = "/" + imgId + ".jpg";
            filepath += extraPath;
            if (folder.isDirectory()) {
                try {
                    String[] children = folder.list();
                    for (int i = 0; i < children.length; i++) {
                        if(children[i].contains(extraPath))
                            Logger.log("DELETE:" + extraPath + " Children: " + children[i]);
                        new File(folder, children[i]).delete();
                    }
                } catch (Exception e) {
                    // TODO: handle exception
                    return false;
                }
            }
            BitmapFactory.Options bmOptions;
            bmOptions = new BitmapFactory.Options();
            bmOptions.inSampleSize = 2;
            try {
                FileOutputStream fos = null;
                fos = new FileOutputStream(filepath);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 85, fos);
                fos.flush();
                fos.close();
                return true;

            } catch (FileNotFoundException e) {
                e.printStackTrace();
                Logger.log("FileNotFoundException: " + e.toString());
                return false;

            } catch (IOException e) {
                e.printStackTrace();
                Logger.log("IOException: " + e.toString());
                return false;
            }
        }

        public boolean isLocalCached() {
            return isLocalCached;
        }

        public String getImgPath() {
            return imgPath;
        }

        public Bitmap getBitmap() {
            return bitmap;
        }
    }
}
