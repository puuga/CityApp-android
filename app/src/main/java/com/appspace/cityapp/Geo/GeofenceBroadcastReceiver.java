package com.appspace.cityapp.geo;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

import com.appspace.cityapp.MyActivity;
import com.appspace.cityapp.R;
import com.appspace.cityapp.helper.SettingHelper;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;

import java.util.List;

/**
 * Created by siwaweswongcharoen on 7/22/2015 AD.
 */
public class GeofenceBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        GeofencingEvent event = GeofencingEvent.fromIntent(intent);

        LocationHelper locationHelper = LocationHelper.getInstance(event);

        geoTrack(context, locationHelper);

        makeNotification(context, locationHelper, event.getTriggeringGeofences());
    }

    private void makeNotification(Context context, LocationHelper locationHelper, List<Geofence> geofences) {
        Intent myIntent = new Intent(context, MyActivity.class);
        myIntent.putExtra(StoreLocation.LOCATION_ID, locationHelper.getId());
        PendingIntent activity = PendingIntent.getActivity(context, 0, myIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        Notification notification = null;
        if (geofences.size() == 1) {
            notification = buildSingleNotification(context, locationHelper, activity);
        } else if (geofences.size() > 1){
            notification = buildMultipleNotification(context, locationHelper, activity, geofences);
        }
        nm.notify(0, notification);
    }

    private Notification buildMultipleNotification(Context context, LocationHelper locationHelper, PendingIntent activity, List<Geofence> geofences) {
        NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();
        inboxStyle.setBigContentTitle(geofences.size() + " contract locations");
        inboxStyle.setSummaryText("");
        for (Geofence geofence : geofences) {
            String[] temp = geofence.getRequestId().split(",");
            String id = temp[0];
            String name = temp[1];
            inboxStyle.addLine(name);
        }

        NotificationCompat.Builder notificationCompat = new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentTitle(locationHelper.getTransition() + " " + locationHelper.getName())
                .setContentText(locationHelper.getName())
                .setTicker(context.getText(R.string.city_hello))
                .setDefaults(Notification.DEFAULT_ALL)
                .setAutoCancel(true)
                .setOnlyAlertOnce(true)
                .setContentIntent(activity)
                .setStyle(inboxStyle);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            notificationCompat
                    .setCategory(Notification.CATEGORY_PROMO)
                    .setVisibility(Notification.VISIBILITY_PUBLIC);
        }

        return notificationCompat.build();
    }

    private Notification buildSingleNotification(Context context, LocationHelper locationHelper, PendingIntent activity) {
        NotificationCompat.Builder notificationCompat = new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentTitle(locationHelper.getTransition() + " " + locationHelper.getName())
                .setContentText(locationHelper.getName())
                .setTicker(context.getText(R.string.city_hello))
                .setDefaults(Notification.DEFAULT_ALL)
                .setAutoCancel(true)
                .setOnlyAlertOnce(true)
                .setContentIntent(activity);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            notificationCompat
                    .setCategory(Notification.CATEGORY_PROMO)
                    .setVisibility(Notification.VISIBILITY_PUBLIC);
        }

        return notificationCompat.build();
    }

    private void geoTrack(Context context, LocationHelper locationHelper) {
        SettingHelper settingHelper = new SettingHelper(context);
        String userId = settingHelper.getUserID();
        if (!userId.equals("")) {
            GeoTracker.track(locationHelper.getId(), userId, locationHelper.getTransition());
        }
    }

}
