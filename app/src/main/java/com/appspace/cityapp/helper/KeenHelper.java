package com.appspace.cityapp.helper;

import android.content.Context;

import com.appspace.cityapp.R;

import java.util.HashMap;
import java.util.Map;

import io.keen.client.android.AndroidKeenClientBuilder;
import io.keen.client.java.KeenClient;
import io.keen.client.java.KeenLogging;
import io.keen.client.java.KeenProject;

/**
 * Created by siwaweswongcharoen on 4/7/2015 AD.
 */
public class KeenHelper {

    private Context context;
    private boolean debugMode;

    public KeenHelper(Context context, boolean debugMode) {
        this.context = context;
        this.debugMode = debugMode;
    }

    public void initialize() {
        // If the Keen Client isn't already initialized, initialize it.
        if (!KeenClient.isInitialized()) {

            // Create a new instance of the client.
            KeenClient client = new AndroidKeenClientBuilder(context).build();

            // Get the project ID and write key from string resources, then create a project and set
            // it as the default for the client.
            String projectId = context.getString(R.string.keen_project_id);
            String writeKey = context.getString(R.string.keen_write_key);
            KeenProject project = new KeenProject(projectId, writeKey, null);
            client.setDefaultProject(project);

            // During testing, enable logging and debug mode.
            // NOTE: REMOVE THESE LINES BEFORE SHIPPING YOUR APPLICATION!
            if (debugMode) {
                KeenLogging.enableLogging();
                client.setDebugMode(true);
            }

            // Initialize the KeenClient singleton with the created client.
            KeenClient.initialize(client);
        }
    }

    public void track(String eventName, String itemName, Object item) {
        // Create an event to upload to Keen.
        Map<String, Object> event = new HashMap<String, Object>();
        event.put(itemName, item);

        // Add it to the "purchases" collection in your Keen Project.
//        KeenClient.client().addEvent("purchases", event);
        KeenClient.client().queueEvent(eventName, event);
    }

    public void pauseKeen() {
        KeenClient.client().sendQueuedEventsAsync();
    }
}
