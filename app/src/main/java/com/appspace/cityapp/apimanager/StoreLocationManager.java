package com.appspace.cityapp.apimanager;

/**
 * Created by siwaweswongcharoen on 7/27/2015 AD.
 */
public class StoreLocationManager {
    public static final String API_LOCATION_URL = "http://128.199.133.166/roomlink/location.php";

    public static String API_LOCATION_URL(String name) {
        return API_LOCATION_URL + "?name=" + name;
    }
}
