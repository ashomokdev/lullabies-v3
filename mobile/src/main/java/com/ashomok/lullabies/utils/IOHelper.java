package com.ashomok.lullabies.utils;

import android.content.Context;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

//todo possible sourse of futere bugs //add dagger injection context here
public class IOHelper {

    private Context context;
    private static final String TAG = LogHelper.makeLogTag(IOHelper.class);

    public void setContext(Context context) {
        this.context = context;
    }

    public InputStream pathToInputStream(String path) throws IOException {
        if (path.contains("http")) {
            URL url = new URL(path);
            URLConnection urlConnection = url.openConnection();
            return new BufferedInputStream(urlConnection.getInputStream());

        } else {
            if (context == null) {
                LogHelper.e(TAG, "context must be set before, context == null");
                return null;
            } else {
                return context.getAssets().open(path);
            }
        }
    }
}
