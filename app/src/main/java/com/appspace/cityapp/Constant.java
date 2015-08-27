package com.appspace.cityapp;

import com.appspace.cityapp.helper.SettingHelper;

/**
 * Created by siwaweswongcharoen on 8/29/14 AD.
 */
public class Constant {
    public static final String cAppTag = String.valueOf(R.string.app_name);
    public static final String GA_ID = String.valueOf("UA-40963799-5");

    public static final String PARSE_CHANNEL_DEBUG = String.valueOf("DEBUG");
    public static final int REQUEST_CODE_ASK_ACCESS_FINE_LOCATION = 1001;
    public static final int REQUEST_CODE_ASK_WRITE_EXTERNAL_STORAGE = 1002;
    public static final int REQUEST_CODE_ASK_GOOGLE_C2DM_RECEIVE = 1003;

    public static final String setting_facebook_login_status_boolean = "setting_facebook_login_status_boolean";
    public static final String facebook_token = "facebook_token";
    public static final String userID = "userID";

    // public static final String kAPIBaseUrl = "http://128.199.172.185/cityapp/register_user.php";
    // public static final String kAPIBaseUrl = "http://citylogin.azurewebsites.net/register_user.php";
    public static final String kAPIBaseUrl = "http://128.199.133.166/roomlink/register_user.php";
    public static final String API_APP_ID = "1";
    public static final String kAPI_GEO_TRACK_URL = "http://128.199.133.166/roomlink/track_geo.php";

    public static final String kInternetUrl = "http://128.199.208.34/internet-service/service.html";

    // beta.roomlinksaas.com/RoomlinkindoorCo.aspx?userId=ข้อมูลยูสเซอร์ไอดี&idAccess=rlsRoomlinksaas2011
    // public static final String kWebURL = "http://beta.roomlinksaas.com/RoomlinkindoorCo.aspx";
    public static final String kWebURL = "http://roomlinksaascity.azurewebsites.net/roomlinkindoorCo.aspx";
    public static final String kWeb404 = "file:///android_asset/noInternet.html";
    public static final String kWebTest = "file:///android_asset/test.html";

    public static String getkWebUrl(SettingHelper settingHelper) {
        if (settingHelper.getUserID().equals(""))
            return "";
        return kWebURL+"?userId="+settingHelper.getUserID()+"&idAccess=rlsRoomlinksaas2011";
//        return kWebTest;
    }

    public static final String kIBeaconDevices = "http://roomlinksaascity.azurewebsites.net/eeialldevice.aspx?eeiid=rlseei2011";
}
