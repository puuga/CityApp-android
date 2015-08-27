package com.appspace.cityapp.geo;

import android.util.Log;

import com.appspace.cityapp.Constant;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by siwaweswongcharoen on 8/27/2015 AD.
 */
public class GeoTracker {
    public static void track(String locationId, String userId, String action) {
        RequestParams params = new RequestParams();
        params.put("location_id", locationId);
        params.put("user_id", userId);
        params.put("app_id", Constant.API_APP_ID);
        params.put("action", action);

        AsyncHttpClient client = new AsyncHttpClient();

        client.post(Constant.kAPI_GEO_TRACK_URL, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    String result = response.get("result").toString();
                    if (result.equals("success")) {
                        Log.d("geo_track", "record success");
                    } else {
                        Log.d("geo_track", "response: " + response.toString());
                    }
                } catch (JSONException e) {
                    Log.d("geo_track", "record unsuccess");
                    e.printStackTrace();
                }
            }
        });
    }
}
