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

import java.security.MessageDigest;


public class LoginActivity extends Activity {

    private Session session;

    // SharedPreferences
    SettingHelper settingHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        printHashKey();
        bindSharedPreferences();

        bindWidget();
    }

    private void bindSharedPreferences() {
        settingHelper = new SettingHelper(this);
    }

    private void bindWidget() {

    }

    private void printHashKey() {
        // Add code to print out the key hash
        try {
            PackageInfo info = getPackageManager().getPackageInfo(
                    "com.worldmotorracingclub.app",
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
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);

            }
        }
    }

}
