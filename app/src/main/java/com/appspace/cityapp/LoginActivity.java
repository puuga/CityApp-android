package com.appspace.cityapp;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;

import com.appspace.cityapp.apimanager.FacebookManager;
import com.appspace.cityapp.helper.SettingHelper;
import com.facebook.Session;
import com.facebook.widget.LoginButton;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import java.security.MessageDigest;


public class LoginActivity extends Activity {

    private Session session;

    LoginButton authButton;

    // SharedPreferences
    SettingHelper settingHelper;

    public static GoogleAnalytics analytics;
    public static Tracker tracker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        initGoogleAnalytics();

        printHashKey();
        bindSharedPreferences();

        bindWidget();

    }

    private void initGoogleAnalytics() {
        analytics = GoogleAnalytics.getInstance(this);
        analytics.setLocalDispatchPeriod(1800);

        tracker = analytics.newTracker("UA-40963799-5");
        tracker.enableExceptionReporting(true);
        tracker.enableAutoActivityTracking(true);

        tracker.setScreenName("login screen");

        tracker.send(new HitBuilders.EventBuilder()
                .setCategory("UI")
                .setAction("load")
                .setLabel("login screen")
                .build());
    }

    @Override
    protected void onStart() {
        super.onStart();
        GoogleAnalytics.getInstance(LoginActivity.this).reportActivityStart(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        GoogleAnalytics.getInstance(LoginActivity.this).reportActivityStop(this);
    }

    @Override
    protected void onResume() {
        super.onResume();

//        if (settingHelper.getFacebookLoginStatus()) {
//            finish();
//        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    private void bindSharedPreferences() {
        settingHelper = new SettingHelper(this);
    }

    private void bindWidget() {
        // authButton = (LoginButton) findViewById(R.id.authButton);
        // authButton.setReadPermissions(Arrays.asList("public_profile", "basic_info", "user_birthday", "email"));
    }

    private void printHashKey() {
        // Add code to print out the key hash
        try {
            PackageInfo info = getPackageManager().getPackageInfo(
                    "com.appspace.cityapp",
                    PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.d("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void loginWithFacebook(View view) {
        view.setEnabled(false);
        session = FacebookManager.callFacebookLogin(this,getBaseContext());

    }

    public void logoutFromFacebook(View view) {
        FacebookManager.callFacebookLogout(getApplicationContext(), this);

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Session.getActiveSession().onActivityResult(this, requestCode, resultCode, data);
        if (session.isOpened()) {
            //Log.d("facebook token","facebook token:"+session.getAccessToken());
            if (session.getAccessToken().length() > 0) {
                settingHelper.setFacebookLoginStatus(true);
                settingHelper.setFacebookToken(session.getAccessToken());

                Intent intent = new Intent(this, MyActivity.class);
                // intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                //finish();
            }
        }
    }

}
