package com.appspace.cityapp;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;

import com.appspace.cityapp.apimanager.LoginManager;
import com.appspace.cityapp.helper.SettingHelper;
import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

import java.security.MessageDigest;
import java.util.Arrays;


public class LoginActivity extends Activity {

//    private Session session;

    LoginButton authButton;
    CallbackManager callbackManager;
    AccessTokenTracker accessTokenTracker;

    // SharedPreferences
    SettingHelper settingHelper;

    public static GoogleAnalytics analytics;
    public static Tracker tracker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        bindSharedPreferences();
        initFacebookSdk();

        setContentView(R.layout.activity_login);

        printHashKey();

        initGoogleAnalytics();

        bindWidget();

    }

    private void initFacebookSdk() {
        FacebookSdk.sdkInitialize(getApplicationContext());
        callbackManager = CallbackManager.Factory.create();

        accessTokenTracker = new AccessTokenTracker() {
            @Override
            protected void onCurrentAccessTokenChanged(
                    AccessToken oldAccessToken,
                    AccessToken currentAccessToken) {
                // Set the access token using
                // currentAccessToken when it's loaded or set.
                if (currentAccessToken == null) {
                    //write your code here what to do when user logout
                    Log.d("fb_logout", "fb_logout");
                    settingHelper.setFacebookLoginStatus(false);
                    settingHelper.setFacebookToken("");
                    settingHelper.setUserID("");
                }
            }
        };
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        accessTokenTracker.stopTracking();
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
        initAuthButton();
    }

    private void initAuthButton() {
        authButton = (LoginButton) findViewById(R.id.authButton);
        authButton.setReadPermissions(Arrays.asList("public_profile", "email"));

        // Callback registration
        authButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                // App code

                accessTokenTracker.startTracking();

                getFacebookUserInfo(loginResult);
            }

            @Override
            public void onCancel() {
                // App code
                Log.d("FBonC", "onCancel");
            }

            @Override
            public void onError(FacebookException exception) {
                // App code
                Log.d("FBonE", exception.getCause().toString());
            }
        });
    }

    private void getFacebookUserInfo(final LoginResult loginResult) {
        Log.d("FBonS", "user id:" + loginResult.getAccessToken().getUserId());
        Log.d("FBonS", "user token:" + loginResult.getAccessToken().getToken());

        final String facebookToken = loginResult.getAccessToken().getToken();

        GraphRequest request = GraphRequest.newMeRequest(
                loginResult.getAccessToken(),
                new GraphRequest.GraphJSONObjectCallback() {
                    @Override
                    public void onCompleted(
                            JSONObject object,
                            GraphResponse response) {
                        // Application code
                        try {
                            object.getString("first_name");
                            String newBirthDay;
                            String birthDay;
                            String[] temp;
                            try {
                                birthDay = object.getString("birthday");
                                temp = birthDay.split("/");
                                newBirthDay = temp[2] + "-" + temp[0] + "-" + temp[1];
                            } catch (JSONException | NullPointerException e) {
                                newBirthDay = "0000-00-00";
                            }
                            Log.d("fb_info", "firstname: " + object.getString("first_name"));
                            Log.d("fb_info", "lastname: " + object.getString("last_name"));
                            Log.d("fb_info", "email: " + object.getString("email"));
                            Log.d("fb_info", "facebook_id: " + object.getString("id"));
                            Log.d("fb_info", "birthday: " + newBirthDay);
                            Log.d("fb_info", "gender: " + object.getString("gender"));
                            Log.d("fb_info", "facebook_token: " + facebookToken);

                            loginCity(LoginManager.getParamsByFacebookGraph(object, facebookToken));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });

        Bundle parameters = new Bundle();
        parameters.putString("fields", "id,name,first_name,last_name,email,gender,birthday");
        request.setParameters(parameters);
        request.executeAsync();
    }

    private void loginCity(RequestParams params) {
        AsyncHttpClient client = new AsyncHttpClient();
        client.post(Constant.kAPIBaseUrl, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                Log.d("JSONObject", response.toString());

                try {
                    String isSuccess = response.getString("result");
                    if (isSuccess.contains("success")) {
                        Log.d("callback", "result:" + response.getString("result"));
                        Log.d("callback", "accountid:" + response.getString("accountid"));
                        Log.d("callback", "firstname:" + response.getString("firstname"));
                        Log.d("callback", "lastname:" + response.getString("lastname"));
                        Log.d("callback", "email:" + response.getString("email"));
                        Log.d("callback", "facebook_id:" + response.getString("facebook_id"));
                        Log.d("callback", "birthday:" + response.getString("birthday"));
                        Log.d("callback", "gender:" + response.getString("gender"));
                        Log.d("callback", "facebook_token:" + response.getString("facebook_token"));

                        settingHelper.setUserID(response.getString("accountid"));
                        settingHelper.setFacebookLoginStatus(true);
                        settingHelper.setFacebookToken(response.getString("facebook_token"));

                        Intent intent = new Intent(getBaseContext(), MyActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                    } else {
                        Log.d("callback", statusCode + ":" + response.toString());
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        });
    }

    private void printHashKey() {
        // Add code to print out the key hash
        try {
            @SuppressLint
                    ("PackageManagerGetSignatures") PackageInfo info = getPackageManager().getPackageInfo(
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

}
