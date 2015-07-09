package com.appspace.cityapp;

import android.util.Log;

import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParsePush;
import com.parse.PushService;
import com.parse.SaveCallback;

/**
 * Created by siwaweswongcharoen on 7/2/2015 AD.
 */
public class Application extends android.app.Application {
    public Application() {
    }

    @Override
    public void onCreate() {
        super.onCreate();

        // Initialize the Parse SDK.
        Parse.initialize(this, "PdbY0J1f0LBXJoEWNeID0nIiVlO7b5dpcVJwVicd", "b3SyEuAzKeJPTn4xi7FCPMqucpokyxex42rA7c7j");

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
