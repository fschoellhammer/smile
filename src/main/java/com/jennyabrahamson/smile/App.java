package com.jennyabrahamson.smile;

import android.app.Application;
import android.content.Context;
import com.parse.Parse;
import com.parse.ParseCrashReporting;
import uk.co.senab.bitmapcache.BitmapLruCache;

import java.io.File;

/**
 * V 1.0 : 4/25/2015
 *   Initial version
 * V 1.1 : 5/8/2015
 *   Improved image loading/caching using Smoothie
 *   Updated Audience Network jar to 4.1.1
 *   Added Parse Analytics for better crash insights
 * V 1.2 : 5/8/2015
 *   Increase inset for hScroll
 *
 */
public class App extends Application {

    private BitmapLruCache mCache;

    @Override
    public void onCreate() {
        ParseCrashReporting.enable(this);

        // Setup Parse
        Parse.initialize(
                this,
                "QUCNrW39fzpWtKU8If5kfpXxMYdPO0Tg0mL9Kbel",
                "4WiPsy3r9eV2zb1wp9D22m6DPo15YGBnqGLgG20A");

        File cacheDir = new File(getCacheDir(), "smoothie");
        cacheDir.mkdirs();

        BitmapLruCache.Builder builder = new BitmapLruCache.Builder();
        builder.setMemoryCacheEnabled(true).setMemoryCacheMaxSizeUsingHeapSize();
        builder.setDiskCacheEnabled(true).setDiskCacheLocation(cacheDir);

        mCache = builder.build();
    }

    public BitmapLruCache getBitmapCache() {
        return mCache;
    }

    public static App getInstance(Context context) {
        return (App) context.getApplicationContext();
    }
}
