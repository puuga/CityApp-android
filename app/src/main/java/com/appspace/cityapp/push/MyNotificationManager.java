package com.appspace.cityapp.push;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by siwaweswongcharoen on 9/15/2015 AD.
 */
public class MyNotificationManager {
    public static final String TAG = "com.appspace.cityapp.push.MyNotificationManager";

    public static class Singleton {
        private static final MyNotificationManager INSTANCE = new MyNotificationManager();
    }

    public static MyNotificationManager getInstance() {
        return Singleton.INSTANCE;
    }

    private final AtomicInteger notificationCount = new AtomicInteger(0);
    private volatile boolean shouldShowNotifications = true;

    public void setShouldShowNotifications(boolean show) {
        shouldShowNotifications = show;
    }

    public int getNotificationCount() {
        return notificationCount.get();
    }

    public void showNotification(Context context, Notification notification) {
        if (context != null && notification != null) {
            notificationCount.incrementAndGet();

            if (shouldShowNotifications) {
                // Fire off the notification
                NotificationManager nm =
                        (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

                // Pick an id that probably won't overlap anything
                int notificationId = (int)System.currentTimeMillis();

                try {
                    nm.notify(notificationId, notification);
                } catch (SecurityException e) {
                    // Some phones throw an exception for unapproved vibration
                    notification.defaults = Notification.DEFAULT_LIGHTS | Notification.DEFAULT_SOUND;
                    nm.notify(notificationId, notification);
                }
            }
        }
    }
}
