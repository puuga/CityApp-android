package com.appspace.cityapp.geo;

import android.text.TextUtils;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by siwaweswongcharoen on 8/30/2015 AD.
 */
public class LocationHelper {
    private GeofencingEvent geofencingEvent;
    private String id;
    private String name;
    private String transition;

    public LocationHelper(GeofencingEvent geofencingEvent) {
        this.geofencingEvent = geofencingEvent;

        transition = mapTransition(geofencingEvent.getGeofenceTransition());
        String[] temp = getGeofenceTransitionDetails(geofencingEvent.getTriggeringGeofences()).split(",");
        id = temp[0];
        name = temp[1];
    }

    public GeofencingEvent getGeofencingEvent() {
        return geofencingEvent;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getTransition() {
        return transition;
    }

    private String getGeofenceTransitionDetails( List<Geofence> triggeringGeofences) {

        // Get the name of each geofence that was triggered.
        ArrayList<String> triggeringGeofencesIdsList = new ArrayList<>();
        for (Geofence geofence : triggeringGeofences) {
            triggeringGeofencesIdsList.add(geofence.getRequestId());
        }

        return TextUtils.join(", ", triggeringGeofencesIdsList);
    }

    private String mapTransition(int event) {
        switch (event) {
            case Geofence.GEOFENCE_TRANSITION_ENTER:
                return "ENTER";
            case Geofence.GEOFENCE_TRANSITION_EXIT:
                return "EXIT";
            default:
                return "UNKNOWN";
        }
    }

    public static LocationHelper getInstance(GeofencingEvent geofencingEvent) {
        return new LocationHelper(geofencingEvent);
    }
}
