package com.jennyabrahamson.smile;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.google.ads.conversiontracking.AdWordsConversionReporter;
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
 * V 1.3 : 7/8/2015
 *   Update SDK jars (FB, Audience Network, add Google Play Services)
 *   Change ad implementation to use different ad managers for each hscroll unit
 * V 1.4 : 6/14/2017
 *   Removed android.permission.READ_PHONE_STATE and "device_id" URL parameter.
 *   Removed usage of Parse.
 */
public class App extends Application {

    private BitmapLruCache mCache;

    @Override
    public void onCreate() {
        super.onCreate();
        File cacheDir = new File(getCacheDir(), "smoothie");
        cacheDir.mkdirs();

        BitmapLruCache.Builder builder = new BitmapLruCache.Builder();
        builder.setMemoryCacheEnabled(true).setMemoryCacheMaxSizeUsingHeapSize();
        builder.setDiskCacheEnabled(true).setDiskCacheLocation(cacheDir);

        // Smile First Open
        // Google Android first open conversion tracking snippet
        AdWordsConversionReporter.reportWithConversionId(this.getApplicationContext(),
                "977195602", "H29PCIaL8l4Q0qT70QM", "0.00", false);


        mCache = builder.build();
        FacebookSdk.setApplicationId("419566964845443");
        FacebookSdk.sdkInitialize(this);
    }

    private static final String LAST_RECORDED_VERSION_KEY = "last_recorded_app_version";

    public void onResume() {
        try {
            SharedPreferences mPrefs = getApplicationContext().getSharedPreferences(
                    LAST_RECORDED_VERSION_KEY, Context.MODE_PRIVATE);
            PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            int currentAppVersion = packageInfo.versionCode;
            int lastRecordedAppVersion = mPrefs.getInt(LAST_RECORDED_VERSION_KEY, -1);
            if (currentAppVersion > lastRecordedAppVersion) {

                // Smile App Upgrades
                // Google Android in-app conversion tracking snippet
                AdWordsConversionReporter.reportWithConversionId(this.getApplicationContext(),
                        "977195602", "W0xiCLbn5l4Q0qT70QM", "0.00", true);

                // Facebook app event
                AppEventsLogger logger = AppEventsLogger.newLogger(this);
                logger.logEvent("App upgrade to: " + currentAppVersion);

                SharedPreferences.Editor editor = mPrefs.edit();
                editor.putInt(LAST_RECORDED_VERSION_KEY, currentAppVersion);
                editor.commit();
            }
        } catch (PackageManager.NameNotFoundException e) {}
    }

    public BitmapLruCache getBitmapCache() {
        return mCache;
    }

    public static App getInstance(Context context) {
        return (App) context.getApplicationContext();
    }
}
