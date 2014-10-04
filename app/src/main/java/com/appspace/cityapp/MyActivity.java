package com.appspace.cityapp;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.location.Location;
import android.net.Uri;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.appspace.cityapp.helper.CustomLocation;
import com.appspace.cityapp.helper.SettingHelper;
import com.appspace.cityapp.helper.WifiData;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;


public class MyActivity extends Activity implements
        GooglePlayServicesClient.ConnectionCallbacks,
        GooglePlayServicesClient.OnConnectionFailedListener {

    // SharedPreferences
    SettingHelper settingHelper;

    Location mCurrentLocation;
    LocationClient mLocationClient;

    // widget
    WebView webView;
    String webURL = "http://beta.roomlinksaas.com";

    Gson gson;

    // wifi manager
    WifiManager wifi;
    int size = 0;
    List<ScanResult> results;
    WifiBroadcastReceiver wifiBroadcastReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my);

        settingHelper = new SettingHelper(this);
        gson = new Gson();

        //check facebook login
        if (settingHelper.getUserID().equals("")) {
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        } else {
            Log.d(Constant.cAppTag,"start app with user id:"+settingHelper.getUserID());
        }



        mLocationClient = new LocationClient(this, this, this);

        bindWidget();
        initWebView();
        initWifiManager();


    }

    private void initWifiManager() {
        wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        if (!wifi.isWifiEnabled()) {
            Toast.makeText(getApplicationContext(), "wifi is disabled..making it enabled", Toast.LENGTH_LONG).show();
            wifi.setWifiEnabled(true);
        }

        wifiBroadcastReceiver = new WifiBroadcastReceiver();

    }

    @SuppressLint("SetJavaScriptEnabled")
    private void initWebView() {
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setGeolocationEnabled(true);
        webView.loadUrl(webURL+"?userID="+settingHelper.getUserID());
        webView.setWebViewClient(new myWebClient());
        webView.addJavascriptInterface((this), "Android");
        webView.setWebChromeClient(new WebChromeClient() {
            public void onGeolocationPermissionsShowPrompt(String origin,
                                                           android.webkit.GeolocationPermissions.Callback callback) {
                Log.d("geolocation permission", "permission >>>" + origin);
                callback.invoke(origin, true, false);
            }
        });

    }

    private void bindWidget() {
        webView = (WebView) findViewById(R.id.webView);
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.i("GPS","connected");
    }

    @Override
    public void onDisconnected() {
        Log.i("GPS","disconnected");
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.i("GPS","ConnectionFailed:"+connectionResult.toString());
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Connect the client.
        mLocationClient.connect();

        registerReceiver(wifiBroadcastReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));

        wifi.startScan();
    }

    @Override
    protected void onStop() {
        // Disconnecting the client invalidates it.
        mLocationClient.disconnect();
        unregisterReceiver(wifiBroadcastReceiver);
        super.onStop();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @JavascriptInterface
    // option 0 = no sound, no vibrate
    // option 1 = no sound, vibrate
    // option 2 = sound, no vibrate
    // option 3 = sound, vibrate
    public void showNoti(String title, String content, int option) {
        Log.d("show noti", "show noti:" + title + "," + content);
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentTitle(title).setContentText(content);
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
    public void getLocationFromPlayService() {
        mCurrentLocation = mLocationClient.getLastLocation();

        CustomLocation cLocation = new CustomLocation();
        cLocation.setAltitude(mCurrentLocation.getAltitude());
        cLocation.setLatitude(mCurrentLocation.getLatitude());
        cLocation.setLongitude(mCurrentLocation.getLongitude());
        final String temp = gson.toJson(cLocation);

        // return current location to webview();
        MyActivity.this.runOnUiThread(new Runnable() {
            public void run() {
                webView.loadUrl("javascript:getAndroidLocation('" + temp + "')");
            }
        });
    }

    @JavascriptInterface
    public void getWifiData() {
        wifi.startScan();

        Log.d("wifi result", "Scanning...." + size);
        ArrayList<WifiData> wifiData = new ArrayList<WifiData>();
        try {
            size = size - 1;
            while (size >= 0) {

                WifiData temp = new WifiData();
                temp.setSsid(results.get(size).SSID);
                temp.setBSsid(results.get(size).BSSID);
                temp.setCapabilities(results.get(size).capabilities);
                temp.setLevel(results.get(size).level);
                temp.setFrequency(results.get(size).frequency);

                Log.d("wifi result", temp.toString());
                wifiData.add(temp);

                size--;
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        // return to javascript in webview
        final String temp = gson.toJson(wifiData);
        Log.d("wifi json", temp);
        MyActivity.this.runOnUiThread(new Runnable() {
            public void run() {
                webView.loadUrl("javascript:getWifiDataFromAndroid('"
                        + temp + "')");
            }
        });
    }

    private class myWebClient extends WebViewClient {
        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
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
        }
    }

    private class WifiBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            results = wifi.getScanResults();
            size = results.size();
        }
    }

}
