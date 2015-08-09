package com.appspace.cityapp;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.pm.ApplicationInfo;
import android.graphics.Bitmap;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.Toast;

import com.appspace.cityapp.geo.StoreLocation;
import com.appspace.cityapp.helper.CustomLocation;
import com.appspace.cityapp.helper.KeenHelper;
import com.appspace.cityapp.helper.SettingHelper;
import com.appspace.cityapp.helper.WifiData;
import com.appspace.cityapp.model.IBeacon;
import com.crashlytics.android.Crashlytics;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.gson.Gson;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.parse.ParseInstallation;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import io.fabric.sdk.android.Fabric;

import static android.net.wifi.WifiManager.calculateSignalLevel;


public class MyActivity extends AppCompatActivity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        OnInitListener {

    // SharedPreferences
    SettingHelper settingHelper;

    // google api
    Location mLastLocation;
    GoogleApiClient mGoogleApiClient;
    // Request code to use when launching the resolution activity
    private static final int REQUEST_RESOLVE_ERROR = 1001;
    // Unique tag for the error dialog fragment
    private static final String DIALOG_ERROR = "dialog_error";
    // Bool to track whether the app is already resolving an error
    private boolean mResolvingError = false;

    // keen
    KeenHelper keenHelper;

    // widget
    WebView webView;
    String webURL;
    ImageView ivBackground;


    Gson gson;

    // wifi manager
    WifiManager wifi;
    int size = 0;
    List<ScanResult> results;
    WifiBroadcastReceiver wifiBroadcastReceiver;

    // iBeacon
    AsyncHttpClient client;
    IBeacon iBeacon[];
    boolean readIBeaconSuccess = false;

    // internet
    boolean isInternetAvailable = false;

    // TTS object
    private TextToSpeech myTTS;
    // status check code
    private int MY_DATA_CHECK_CODE = 0;
    private boolean isReadyToSpeech = false;

    public static GoogleAnalytics analytics;
    public static Tracker tracker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());

        initParse();

        initGoogleAnalytics();

        setContentView(R.layout.activity_my);

        client = new AsyncHttpClient();

        // keen
        keenHelper = new KeenHelper(this, BuildConfig.DEBUG);
        keenHelper.initialize();
        keenHelper.track("StartApp", "App", "CityApp");

        // check internet connection
        checkInternetAvailable();

        settingHelper = new SettingHelper(this);
        // webURL = Constant.getkWebUrl(settingHelper);
        gson = new Gson();

        buildGoogleApiClient();

        bindWidget();

        //initWebView();
        initWifiManager();

        // iBeacon
        getIBeaconDevices();

        // TTS
        prepareTTS();

        webURL = Constant.getkWebUrl(settingHelper);
        initWebView();
    }

    private void initGoogleAnalytics() {
        analytics = GoogleAnalytics.getInstance(this);
        analytics.setLocalDispatchPeriod(1800);

        tracker = analytics.newTracker("UA-40963799-5");
        tracker.enableExceptionReporting(true);
        tracker.enableAutoActivityTracking(true);

        tracker.setScreenName("main screen");

        tracker.send(new HitBuilders.EventBuilder()
                .setCategory("UI")
                .setAction("load")
                .setLabel("main screen")
                .build());
    }

    private void initParse() {
        ParseInstallation.getCurrentInstallation().saveInBackground();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (myTTS != null) {
            myTTS.shutdown();
        }
    }

    private void prepareTTS() {
        Intent checkIntent = new Intent();
        checkIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
        startActivityForResult(checkIntent, MY_DATA_CHECK_CODE);
    }

    private void checkFacebookLogin() {
        // check facebook login
        if (!settingHelper.getFacebookLoginStatus()) {
            keenHelper.track("Facebook", "FacebookLogin", "Login");
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            //finish();
        } else {
            Log.d(Constant.cAppTag, "start app with user id:" + settingHelper.getUserID());
        }
    }

    private void exitAppWithDialog() {
        // Log.d("exit", "exitAppWithDialog");
        keenHelper.track("ExitApp", "reason", "no internet");
        new AlertDialog.Builder(this)
                .setTitle("Can not access the Internet")
                .setMessage("City App need Internet access. Please connect to the Internet.")
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        MyActivity.this.finish();
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setCancelable(false)
                .show();
    }

    private void checkInternetAvailable() {
        client.get(Constant.kInternetUrl, new AsyncHttpResponseHandler() {

            @Override
            public void onStart() {
                // called before request is started
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] response) {
                // called when response HTTP status is "200 OK"
                try {
                    String str = new String(response, "UTF-8");
                    if (!str.contains("internet ok")) {
                        isInternetAvailable = false;
                        exitAppWithDialog();
                    }
                    isInternetAvailable = true;
//                    initWebView();
                    loadWebView(webURL);
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                    isInternetAvailable = false;
                    exitAppWithDialog();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] errorResponse, Throwable e) {
                // called when response HTTP status is "4XX" (eg. 401, 403, 404)
                isInternetAvailable = false;
                exitAppWithDialog();
            }

            @Override
            public void onRetry(int retryNo) {
                if (retryNo >= 3) {
                    isInternetAvailable = false;
                    exitAppWithDialog();
                }

            }
        });
    }

    private void getIBeaconDevices() {
        client.get(Constant.kIBeaconDevices, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                Log.i("JSONObject", response.toString());
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                Log.i("JSONArray", response.toString());
                iBeacon = gson.fromJson(response.toString(), IBeacon[].class);
                readIBeaconSuccess = true;
                Log.i("iBeacon", "There are " + iBeacon.length + " beacons to monitor");
            }
        });
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    private void initWifiManager() {
        wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        if (!wifi.isWifiEnabled()) {
            keenHelper.track("Device", "setting", "turn on wifi");

            new AlertDialog.Builder(this)
                    .setTitle("Need WIFI enabled")
                    .setMessage("Do you want to enable WIFI?")
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            Toast.makeText(getApplicationContext(), "Enabling WIFI", Toast.LENGTH_LONG).show();
                            wifi.setWifiEnabled(true);
                        }
                    })
                    .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            MyActivity.this.finish();
                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();

        }

        wifiBroadcastReceiver = new WifiBroadcastReceiver();

    }

    @SuppressLint("SetJavaScriptEnabled")
    private void initWebView() {
        if (Constant.getkWebUrl(settingHelper).equals(""))
            return;

        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setGeolocationEnabled(true);
//        webView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            if (0 != (getApplicationInfo().flags &= ApplicationInfo.FLAG_DEBUGGABLE)) {
                WebView.setWebContentsDebuggingEnabled(true);
            }
        }

        webView.setWebViewClient(new myWebClient());
        webView.addJavascriptInterface((this), "Android");
        webView.setWebChromeClient(new WebChromeClient() {
            public void onGeolocationPermissionsShowPrompt(String origin,
                                                           android.webkit.GeolocationPermissions.Callback callback) {
                // Log.d("geolocation permission", "permission >>>" + origin);
                callback.invoke(origin, true, false);
            }
        });

        loadWebView(webURL);
    }

    private void loadWebView(String url) {
        keenHelper.track("AppWebview", "url", webURL);
        webView.loadUrl(webURL);
    }

    private void bindWidget() {
        webView = (WebView) findViewById(R.id.webView);
        ivBackground = (ImageView) findViewById(R.id.ivBackground);
    }

    @Override
    public void onConnected(Bundle bundle) {
        // Log.i("GPS", "connected");
        callLastLocation();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        // Log.i("GPS", "ConnectionFailed:" + connectionResult.toString());
        if (mResolvingError) {
            // Already attempting to resolve an error.
            return;
        } else if (connectionResult.hasResolution()) {
            try {
                mResolvingError = true;
                connectionResult.startResolutionForResult(this, REQUEST_RESOLVE_ERROR);
            } catch (IntentSender.SendIntentException e) {
                // There was an error with the resolution intent. Try again.
                mGoogleApiClient.connect();
            }
        } else {
            // Show dialog using GooglePlayServicesUtil.getErrorDialog()
            showErrorDialog(connectionResult.getErrorCode());
            mResolvingError = true;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Connect the client.
        if (!mResolvingError) {  // more about this later
            mGoogleApiClient.connect();
        }

        //registerReceiver(wifiBroadcastReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));

        wifi.startScan();

        GoogleAnalytics.getInstance(MyActivity.this).reportActivityStart(this);
    }

    @Override
    protected void onStop() {
        // Disconnecting the client invalidates it.
        mGoogleApiClient.disconnect();

        // unregisterReceiver(wifiBroadcastReceiver);
        super.onStop();

        GoogleAnalytics.getInstance(MyActivity.this).reportActivityStop(this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        checkFacebookLogin();

        //initWebView();

        // check location service enable
        checkLocationEnabled();

        registerReceiver(wifiBroadcastReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));

        getExtra();

    }

    private void getExtra() {
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            if (extras.containsKey(StoreLocation.LOCATION_ID) && extras.getString(StoreLocation.LOCATION_ID, "") != "") {
                String temp = Constant.getkWebUrl(settingHelper) + "&geo_id=" + extras.getString(StoreLocation.LOCATION_ID);
                Toast.makeText(this, temp, Toast.LENGTH_LONG).show();
                keenHelper.track("AppWebview", "geo url", temp);
//                Log.d("geourl", temp);
                loadWebView(temp);

                // clear
                extras.putString(StoreLocation.LOCATION_ID, "");
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        unregisterReceiver(wifiBroadcastReceiver);

        keenHelper.pauseKeen();

    }

    private void callLastLocation() {
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (mLastLocation != null) {
            Log.i("location", mLastLocation.toString());
        } else {
            Log.i("location", "null object");
        }

    }

    private void checkLocationEnabled() {
        boolean gps_enabled = false;
        boolean network_enabled = false;
        LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        try {
            gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch (Exception ex) {
            Log.d("gps_enabled", String.valueOf(gps_enabled));
        }
        try {
            network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch (Exception ex) {
            Log.d("network_enabled", String.valueOf(network_enabled));
        }

        if (!gps_enabled && !network_enabled) {
            AlertDialog.Builder dialog = new AlertDialog.Builder(this);
            dialog.setTitle("Need Location Service Enabled");
            dialog.setMessage("Do you want to enable location service?");
            dialog.setPositiveButton("Yes, Sure!", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                    Intent myIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivity(myIntent);
                }
            });
            dialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                    // do nothing
                }
            });
            dialog.show();
        }
    }

    @JavascriptInterface
    // option 0 = no sound, no vibrate
    // option 1 = no sound, vibrate
    // option 2 = sound, no vibrate
    // option 3 = sound, vibrate
    public void showNoti(String title, String content, int option) {
        keenHelper.track("AppNotification", "noti", title + " : " + content);
//        Log.d("show noti", "show noti:" + title + "," + content);
        Intent intent = new Intent(this, MyActivity.class);
        //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent activity = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentTitle(title)
                .setContentText(content)
                .setAutoCancel(true)
                .setContentIntent(activity);
        Notification notification = mBuilder.build();
        switch (option) {
            case 0:
                break;
            case 1:
                notification.defaults = Notification.DEFAULT_VIBRATE;
                break;
            case 2:
                notification.defaults = Notification.DEFAULT_SOUND;
                break;
            case 3:
                notification.defaults = Notification.DEFAULT_ALL;
                break;
            default:
                break;
        }
        //notification.defaults = Notification.DEFAULT_ALL;

        // Sets an ID for the notification
        int mNotificationId = 1;
        // Gets an instance of the NotificationManager service
        NotificationManager mNotifyMgr = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        // Builds the notification and issues it.
        mNotifyMgr.notify(mNotificationId, notification);

    }

    @JavascriptInterface
    public String getUserIdPreferences() {
        return settingHelper.getUserID();
    }

    @JavascriptInterface
    public void share(String text) {
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, text);
        sendIntent.setType("text/plain");
        startActivity(Intent.createChooser(sendIntent, "Share"));
    }

    @JavascriptInterface
    public void getLocationFromPlayService() {
        callLastLocation();

        CustomLocation cLocation = new CustomLocation();
        if (mLastLocation != null) {
            cLocation.setAltitude(mLastLocation.getAltitude());
            cLocation.setLatitude(mLastLocation.getLatitude());
            cLocation.setLongitude(mLastLocation.getLongitude());
        } else {
            cLocation.setAltitude(0);
            cLocation.setLatitude(0);
            cLocation.setLongitude(0);
        }
        final String jsonOutput = gson.toJson(cLocation);

        // return current location to webview();
        MyActivity.this.runOnUiThread(new Runnable() {
            public void run() {
                webView.loadUrl("javascript:getAndroidLocation('" + jsonOutput + "')");
            }
        });
    }

    @JavascriptInterface
    public void getWifiData() {
        wifi.startScan();

//        Log.d("wifi result", "Scanning...." + size);
        ArrayList<WifiData> wifiData = new ArrayList<>();
        try {
            size = size - 1;
            while (size >= 0) {

                WifiData temp = new WifiData();
                temp.setSsid(results.get(size).SSID);
                temp.setBSsid(results.get(size).BSSID);
                temp.setCapabilities(results.get(size).capabilities);
                temp.setLevel(results.get(size).level);
                temp.setFrequency(results.get(size).frequency);
                temp.setCalLevel(calculateSignalLevel(results.get(size).level, 10));

//                Log.d("wifi result", temp.toString());
                wifiData.add(temp);

                size--;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

//        wifiData = sortWifiData(wifiData);
        //Sorting
        Collections.sort(wifiData, new Comparator<WifiData>() {
            @Override
            public int compare(WifiData wifiData1, WifiData wifiData2) {
                return wifiData2.getCalLevel() - wifiData1.getCalLevel();
            }
        });

        // return to javascript in webview
        final String temp = gson.toJson(wifiData);
//        Log.d("wifi json", temp);
        MyActivity.this.runOnUiThread(new Runnable() {
            public void run() {
                webView.loadUrl("javascript:getWifiDataFromAndroid('"
                        + temp + "')");
            }
        });
    }

    private ArrayList<WifiData> sortWifiData(ArrayList<WifiData> wifiData) {
        if (wifiData.size() <= 1) {
            return wifiData;
        }
        // Bubble Sort
        WifiData[] arrs = (WifiData[]) wifiData.toArray();
        for (int i = 1; i < arrs.length; i++) {
            boolean swapped = false;
            for (int j = 0; j < arrs.length - i; j++) {
                if (arrs[j].getCalLevel() < arrs[j + 1].getCalLevel()) {
                    WifiData temp = arrs[j];
                    arrs[j] = arrs[j + 1];
                    arrs[j + 1] = temp;
                    swapped = true;
                }
            }
            if (!swapped) {
                break;
            }
        }
        wifiData.clear();
//        for (WifiData arr : arrs) {
//            wifiData.add(arr);
//        }
        Collections.addAll(wifiData, arrs);
        return wifiData;
    }

    @Override
    public void onInit(int initStatus) {
        if (initStatus == TextToSpeech.SUCCESS) {
            //myTTS.setLanguage(Locale.US);
            Log.d("TTS", "TextToSpeech.SUCCESS" + TextToSpeech.SUCCESS);
            // speech("en", "hello");
            isReadyToSpeech = true;
        } else if (initStatus == TextToSpeech.ERROR) {
            Log.d("TTS", "TextToSpeech.ERROR" + TextToSpeech.ERROR);
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == MY_DATA_CHECK_CODE) {
            if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
                myTTS = new TextToSpeech(this, this);
            } else {
                Intent installTTSIntent = new Intent();
                installTTSIntent.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
                startActivity(installTTSIntent);
            }
        }
    }

    @JavascriptInterface
    public void speech(String language, String message) {
        boolean canSetLocale = false;
        switch (language.toLowerCase()) {
            case "en":
                if (myTTS.isLanguageAvailable(Locale.ENGLISH) == TextToSpeech.LANG_AVAILABLE) {
                    myTTS.setLanguage(Locale.ENGLISH);
                    canSetLocale = true;
                    Log.d("TTS", "setLanguage(Locale.ENGLISH): true");
                } else {
                    canSetLocale = false;
                    Log.d("TTS", "setLanguage(Locale.ENGLISH): false");
                }
                break;
            case "cn":
                if (myTTS.isLanguageAvailable(Locale.CHINESE) == TextToSpeech.LANG_AVAILABLE) {
                    myTTS.setLanguage(Locale.CHINESE);
                    canSetLocale = true;
                    Log.d("TTS", "setLanguage(china): true");
                } else {
                    canSetLocale = false;
                    Log.d("TTS", "setLanguage(china): false");
                }
                break;
            case "th":
                if (myTTS.isLanguageAvailable(new Locale("th", "TH")) == TextToSpeech.LANG_COUNTRY_AVAILABLE) {
                    myTTS.setLanguage(new Locale("th", "TH"));
                    canSetLocale = true;
                    Log.d("TTS", "setLanguage(th): true");
                } else {
                    canSetLocale = false;
                    Log.d("TTS", "setLanguage(th): false");
                }
                break;
            default:
                canSetLocale = false;
                Log.d("TTS", "no Language(): " + language);
        }

        if (canSetLocale && isReadyToSpeech) {
            speech(message);
        }
    }

    private void speech(String message) {
        if (Build.VERSION.SDK_INT >= 21) {
            myTTS.speak(message, TextToSpeech.QUEUE_FLUSH, null, null);
        } else {
            myTTS.speak(message, TextToSpeech.QUEUE_FLUSH, null);
        }

    }

    private class myWebClient extends WebViewClient {
        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
            Log.d("onPageStarted", url);
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            if (url.startsWith("tel:")) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(intent);
                return true;
            }
            view.loadUrl(url);
            return true;

        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            Log.d("onPageFinished", url);
            ivBackground.setVisibility(View.GONE);
        }
    }

    private class WifiBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            results = wifi.getScanResults();
            size = results.size();
        }
    }

    // The rest of this code is all about building the error dialog

    /* Creates a dialog for an error message */
    private void showErrorDialog(int errorCode) {
        // Create a fragment for the error dialog
        ErrorDialogFragment dialogFragment = new ErrorDialogFragment();
        // Pass the error that should be displayed
        Bundle args = new Bundle();
        args.putInt(DIALOG_ERROR, errorCode);
        dialogFragment.setArguments(args);
        dialogFragment.show(getSupportFragmentManager(), "errordialog");
    }

    /* Called from ErrorDialogFragment when the dialog is dismissed. */
    public void onDialogDismissed() {
        mResolvingError = false;
    }

    /* A fragment to display an error dialog */
    public static class ErrorDialogFragment extends DialogFragment {
        public ErrorDialogFragment() {
        }

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Get the error code and retrieve the appropriate dialog
            int errorCode = this.getArguments().getInt(DIALOG_ERROR);
            return GooglePlayServicesUtil.getErrorDialog(errorCode,
                    this.getActivity(), REQUEST_RESOLVE_ERROR);
        }

        @Override
        public void onDismiss(DialogInterface dialog) {
            ((MyActivity) getActivity()).onDialogDismissed();
        }
    }

}
