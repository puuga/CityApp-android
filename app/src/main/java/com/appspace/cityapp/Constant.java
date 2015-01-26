package com.appspace.cityapp;

import com.appspace.cityapp.helper.SettingHelper;

/**
 * Created by siwaweswongcharoen on 8/29/14 AD.
 */
public class Constant {
    public static final String cAppTag = String.valueOf(R.string.app_name);
    public static final String setting_facebook_login_status_boolean = "setting_facebook_login_status_boolean";
    public static final String facebook_token = "facebook_token";
    public static final String userID = "userID";

    // public static final String kAPIBaseUrl = "http://128.199.172.185/cityapp/register_user.php";
    public static final String kAPIBaseUrl = "http://citylogin.azurewebsites.net/register_user.php";

    // beta.roomlinksaas.com/RoomlinkindoorCo.aspx?userId=ข้อมูลยูสเซอร์ไอดี&idAccess=rlsRoomlinksaas2011
    public static final String kWebURL = "http://beta.roomlinksaas.com/RoomlinkindoorCo.aspx";

    public static final String getkWebUrl(SettingHelper settingHelper) {
        return kWebURL+"?userId="+settingHelper.getUserID()+"&idAccess=rlsRoomlinksaas2011";
    }
}
