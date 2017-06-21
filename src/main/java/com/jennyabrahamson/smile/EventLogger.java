package com.jennyabrahamson.smile;

import android.os.Bundle;



/**
 * Created by root on 6/21/17.
 */

public class EventLogger {
    private Bundle mBundle;

    public EventLogger() {
        mBundle = new Bundle();
    }

    public EventLogger parameter(String name, String value) {
        mBundle.putString(name, value);
        return this;
    }

    public void logEvent(String eventName) {
        App.getFirebaseAnalytics().logEvent(eventName, mBundle);
        App.getFacebookAnalytics().logEvent(eventName, mBundle);
    }
}
