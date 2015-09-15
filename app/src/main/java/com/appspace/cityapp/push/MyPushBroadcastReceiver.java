package com.appspace.cityapp.push;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.appspace.cityapp.R;
import com.parse.ParsePushBroadcastReceiver;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Locale;
import java.util.Random;


/**
 * Created by siwaweswongcharoen on 9/15/2015 AD.
 */
public class MyPushBroadcastReceiver extends ParsePushBroadcastReceiver {

    final String TAG_A = "c_receiver";

    protected void onPushReceive(Context mContext, Intent intent) {
        JSONObject pushData = null;
        try {
            pushData = new JSONObject(intent.getStringExtra(KEY_PUSH_DATA));
            Log.d(TAG_A, pushData.toString());
        } catch (JSONException e) {
            Log.e(TAG_A, "Unexpected JSONException when receiving push data: ", e);
        }

        // If the push data includes an action string, that broadcast intent is fired.
        String action = null;
        if (pushData != null) {
            action = pushData.optString("action", null);
        }
        if (action != null) {
            Log.d("c_receiver","action: "+action);
            Bundle extras = intent.getExtras();
            Intent broadcastIntent = new Intent();
            broadcastIntent.putExtras(extras);
            broadcastIntent.setAction(action);
            broadcastIntent.setPackage(mContext.getPackageName());
            mContext.sendBroadcast(broadcastIntent);
        }

        Notification notification = getNotification(mContext, intent);

        if (notification != null) {
            MyNotificationManager.getInstance().showNotification(mContext, notification);
        }
    }

    private JSONObject getPushData(Intent intent) {
        try {
            return new JSONObject(intent.getStringExtra(KEY_PUSH_DATA));
        } catch (JSONException e) {
            Log.e(TAG_A, "Unexpected JSONException when receiving push data: ", e);
            return null;
        }
    }

    protected Notification getNotification(Context context, Intent intent) {
        JSONObject pushData = getPushData(intent);
        if (pushData == null || (!pushData.has("alert") && !pushData.has("title"))) {
            return null;
        }

        String title = pushData.optString("title", getDisplayName(context));
        String alert = pushData.optString("alert", "Notification received.");
        String tickerText = String.format(Locale.getDefault(), "%s: %s", title, alert);

        Bundle extras = intent.getExtras();

        Random random = new Random();
        int contentIntentRequestCode = random.nextInt();
        int deleteIntentRequestCode = random.nextInt();

        // Security consideration: To protect the app from tampering, we require that intent filters
        // not be exported. To protect the app from information leaks, we restrict the packages which
        // may intercept the push intents.
        String packageName = context.getPackageName();

        Intent contentIntent = new Intent(ParsePushBroadcastReceiver.ACTION_PUSH_OPEN);
        contentIntent.putExtras(extras);
        contentIntent.setPackage(packageName);

        Intent deleteIntent = new Intent(ParsePushBroadcastReceiver.ACTION_PUSH_DELETE);
        deleteIntent.putExtras(extras);
        deleteIntent.setPackage(packageName);

        PendingIntent pContentIntent = PendingIntent.getBroadcast(context, contentIntentRequestCode,
                contentIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        PendingIntent pDeleteIntent = PendingIntent.getBroadcast(context, deleteIntentRequestCode,
                deleteIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        // The purpose of setDefaults(Notification.DEFAULT_ALL) is to inherit notification properties
        // from system defaults
        NotificationCompat.Builder parseBuilder = new NotificationCompat.Builder(context);
        parseBuilder.setContentTitle(title)
                .setContentText(alert)
                .setTicker(tickerText)
                .setSmallIcon(this.getSmallIconId(context, intent))
                .setLargeIcon(getLargeIcon(context))
                .setContentIntent(pContentIntent)
                .setDeleteIntent(pDeleteIntent)
                .setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_ALL);
        if (alert != null
                && alert.length() > ParsePushBroadcastReceiver.SMALL_NOTIFICATION_MAX_CHARACTER_LIMIT) {
            parseBuilder.setStyle(new NotificationCompat.BigTextStyle().bigText(alert));
        }
        return parseBuilder.build();
    }

    public static String getDisplayName(Context context) {
        ApplicationInfo appInfo = context.getApplicationInfo();

        return context.getPackageManager().getApplicationLabel(appInfo).toString();
    }

    private Bitmap getLargeIcon(Context context) {
        return BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_launcher);
    }
}
