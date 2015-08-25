package com.appspace.cityapp;

import android.util.Log;

import com.appspace.cityapp.helper.GeofenceHelper;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParsePush;
import com.parse.SaveCallback;

import pl.charmas.android.reactivelocation.ReactiveLocationProvider;

/**
 * Created by siwaweswongcharoen on 7/2/2015 AD.
 */
public class Application extends android.app.Application {

    ReactiveLocationProvider locationProvider;

    public static GoogleAnalytics analytics;
    public static Tracker tracker;

    public Application() {
    }

    @Override
    public void onCreate() {
        super.onCreate();

        initParse();
        subscribeParseNotification();

        initGeofences();

        initGoogleAnalytics();

    }

    private void initGoogleAnalytics() {
        analytics = GoogleAnalytics.getInstance(this);
        analytics.setLocalDispatchPeriod(1800);

        tracker = analytics.newTracker(Constant.GA_ID);
        tracker.enableExceptionReporting(true);
        tracker.enableAdvertisingIdCollection(true);
        tracker.enableAutoActivityTracking(true);
    }

    private void initGeofences() {
        locationProvider = new ReactiveLocationProvider(getApplicationContext());
        GeofenceHelper geofenceHelper = new GeofenceHelper(locationProvider, this);
        geofenceHelper.activeGeofence();
    }

    private void subscribeParseNotification() {
        ParsePush.subscribeInBackground("", new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    Log.d("com.parse.push", "successfully subscribed to the broadcast channel.");
                } else {
                    Log.e("com.parse.push", "failed to subscribe for push", e);
                }
            }
        });

        if (BuildConfig.DEBUG) {
            ParsePush.subscribeInBackground(Constant.PARSE_CHANNEL_DEBUG, new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    if (e == null) {
                        Log.d("com.parse.push", "successfully subscribed to the debug channel.");
                    } else {
                        Log.e("com.parse.push", "failed to subscribe for push", e);
                    }
                }
            });
        }
    }

    private void initParse() {
        // Initialize the Parse SDK.
        Parse.enableLocalDatastore(this);
        Parse.initialize(this, "PdbY0J1f0LBXJoEWNeID0nIiVlO7b5dpcVJwVicd", "b3SyEuAzKeJPTn4xi7FCPMqucpokyxex42rA7c7j");
//        ParseInstallation.getCurrentInstallation().saveInBackground();

    }
}
