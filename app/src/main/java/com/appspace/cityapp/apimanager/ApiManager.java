package com.appspace.cityapp.apimanager;

import android.content.Context;

import com.androidquery.AQuery;
import com.appspace.cityapp.helper.SettingHelper;

import java.util.Map;

/**
 * Created by siwaweswongcharoen on 9/16/14 AD.
 */
public class ApiManager {

    // SharedPreferences
    SettingHelper settingHelper;

    Context context;

    AQuery aq;

    public ApiManager(Context context) {
        this.context = context;
        bindSharedPreferences(context);

        aq = new AQuery(context);
    }

    private void bindSharedPreferences(Context context) {
        settingHelper = new SettingHelper(context);
    }

    void post(String url, String json) {
    }

    void post(String url, Map<String, String> params) {

    }

    void get(String url, String parameters) {

    }
}
