package com.appspace.cityapp;

import android.util.Log;

import com.appspace.cityapp.helper.GeofenceHelper;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParsePush;
import com.parse.PushService;
import com.parse.SaveCallback;

import pl.charmas.android.reactivelocation.ReactiveLocationProvider;

/**
 * Created by siwaweswongcharoen on 7/2/2015 AD.
 */
public class Application extends android.app.Application {

    ReactiveLocationProvider locationProvider;

    public Application() {
    }

    @Override
    public void onCreate() {
        super.onCreate();

        initParse();

        initGeofences();

    }

    private void initGeofences() {
        locationProvider = new ReactiveLocationProvider(getApplicationContext());
        GeofenceHelper geofenceHelper = new GeofenceHelper(locationProvider, this);
        geofenceHelper.activeGeofence();
    }

    private void initParse() {
        // Initialize the Parse SDK.
        Parse.enableLocalDatastore(this);
        Parse.initialize(this, "PdbY0J1f0LBXJoEWNeID0nIiVlO7b5dpcVJwVicd", "b3SyEuAzKeJPTn4xi7FCPMqucpokyxex42rA7c7j");
//        Parse.initialize(this, "PbyxNX4PkKSc1DPftcCvcFsGXuN03VHNM68mqWCX", "4jXn7ks10tE3mgMyrKHoBCKLKnI4eqMR7YXSuoNI");

        // Specify an Activity to handle all pushes by default.
        PushService.setDefaultPushCallback(this, MyActivity.class);

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
    }
}
