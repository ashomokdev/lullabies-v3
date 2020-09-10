package com.ashomok.lullabies.utils;

import android.content.Context;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import javax.inject.Inject;

//todo possible sourse of futere bugs //add dagger injection context here
public class IOHelper {

    Context context;

    @Inject
    public IOHelper(Context context){
        this.context = context;
    }

    private static final String TAG = LogHelper.makeLogTag(IOHelper.class);

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
