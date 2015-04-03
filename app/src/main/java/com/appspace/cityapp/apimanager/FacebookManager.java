package com.appspace.cityapp.apimanager;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;
import com.appspace.cityapp.Constant;
import com.appspace.cityapp.MyActivity;
import com.appspace.cityapp.helper.SettingHelper;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.model.GraphUser;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by siwaweswongcharoen on 9/15/14 AD.
 */
public class FacebookManager {
    static final String facebookLoginTag = "facebook login";
    static final String facebookLogoutTag = "facebook logout";

    /**
     * Login to Facebook
     */
    public static Session callFacebookLogin(final Activity activity, final Context context) {
        // start Facebook Login
        List<String> permissions = new ArrayList<String>();
        permissions.add("email");
        permissions.add("user_birthday");
        permissions.add("public_profile");
        permissions.add("basic_info");

        Session session = Session.openActiveSession(activity, true, permissions, new Session.StatusCallback() {

            // callback when session changes state
            @Override
            public void call(final Session session, SessionState state, Exception exception) {
                if (session.isOpened()) {

                    // make request to the /me API
                    Request.newMeRequest(session, new Request.GraphUserCallback() {

                        // callback after Graph API response with user object
                        @Override
                        public void onCompleted(GraphUser user, Response response) {
                            if (user != null) {
                                String id = user.getId();
                                String firstname = user.getFirstName();
                                String lastname = user.getLastName();
                                String birthDay = null;
                                String[] temp = null;
                                String newBirthDay = null;
                                try {
                                    birthDay = user.getBirthday();
                                    temp = birthDay.split("/");
                                    newBirthDay = temp[2]+"-"+temp[0]+"-"+temp[1];
                                } catch (NullPointerException e) {
                                    newBirthDay = "0000-00-00";
                                }
                                String gender = user.getProperty("gender").toString();
                                String email = user.getProperty("email").toString();

                                Log.d(facebookLoginTag, "user id: " + id);
                                Log.d(facebookLoginTag, "user firstname: " + firstname);
                                Log.d(facebookLoginTag, "user lastname: " + firstname);
                                Log.d(facebookLoginTag, "user birthday: " + lastname);
                                Log.d(facebookLoginTag, "user new birthday: " + newBirthDay);
                                Log.d(facebookLoginTag, "user gender: " + gender);
                                Log.d(facebookLoginTag, "user email: " + email);
                                Log.d(facebookLoginTag, "Token: " + session.getAccessToken());
                                Log.d(facebookLoginTag, "response: " + response.toString());

                                Map<String, String> params = new HashMap<String, String>();
                                params.put("firstname", firstname);
                                params.put("lastname", lastname);
                                params.put("email", email);
                                params.put("facebook_id", id);
                                params.put("birthday", newBirthDay);
                                params.put("gender", gender);
                                params.put("facebook_token", session.getAccessToken());

                                AQuery aq = new AQuery(context);
                                //aq.ajax(Constant.kAPIBaseUrl,params,JSONObject.class,context,"callback");
                                aq.ajax(Constant.kAPIBaseUrl, params, JSONObject.class, new AjaxCallback<JSONObject>() {

                                    @Override
                                    public void callback(String url, JSONObject json, AjaxStatus status) {

                                        if(json != null){
                                            //successful ajax call, show status code and json content
                                            //Toast.makeText(aq.getContext(), status.getCode() + ":" + json.toString(), Toast.LENGTH_LONG).show();
                                            Log.d("callback", status.getCode() + ":" + json.toString());
                                            try {
                                                String isSuccess = json.getString("result");
                                                if (isSuccess.equals("register success") || isSuccess.equals("read success")) {
                                                    Log.d("callback", "result:"+json.getString("result"));
                                                    Log.d("callback", "accountid:"+json.getString("accountid"));
                                                    Log.d("callback", "firstname:"+json.getString("firstname"));
                                                    Log.d("callback", "lastname:"+json.getString("lastname"));
                                                    Log.d("callback", "email:"+json.getString("email"));
                                                    Log.d("callback", "facebook_id:"+json.getString("facebook_id"));
                                                    Log.d("callback", "birthday:"+json.getString("birthday"));
                                                    Log.d("callback", "gender:"+json.getString("gender"));
                                                    Log.d("callback", "facebook_token:"+json.getString("facebook_token"));

                                                    SettingHelper settingHelper = new SettingHelper(context);
                                                    settingHelper.setUserID(json.getString("accountid"));
                                                    settingHelper.setFacebookLoginStatus(true);
                                                    settingHelper.setFacebookToken(json.getString("facebook_token"));

                                                    Intent intent = new Intent(context, MyActivity.class);
                                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                    context.startActivity(intent);
                                                } else {
                                                    Log.d("callback", status.getCode() + ":" + json.toString());
                                                    new AlertDialog.Builder(context)
                                                            .setTitle("Error")
                                                            .setMessage("Can not connect to server")
                                                            .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                                                                public void onClick(DialogInterface dialog, int which) {
                                                                    // do nothing
                                                                }
                                                            })
                                                            .setIcon(android.R.drawable.ic_dialog_alert)
                                                            .show();
                                                }
                                            } catch (JSONException e) {
                                                e.printStackTrace();
                                            }

                                        }else{
                                            //ajax error, show error code
                                            //Toast.makeText(aq.getContext(), "Error:" + status.getCode(), Toast.LENGTH_LONG).show();
                                            Log.d("callback","Error:" + status.getCode());
                                        }

                                    }
                                });
                            }
                        }

                    }).executeAsync();
                }
            }
        });
        try {
            //Log.d(facebookLoginTag, "Token: " + session.getAccessToken());

        } catch (Exception e) {
            e.printStackTrace();
        }
        return session;
    }

    /**
     * Logout from Facebook
     */
    public static void callFacebookLogout(Context context, Activity activity) {
        Session session = Session.getActiveSession();
        SettingHelper settingHelper = new SettingHelper(context);
        if (session != null) {
            if (!session.isClosed()) {
                session.closeAndClearTokenInformation();
                //clear your preferences if saved
                settingHelper.setFacebookLoginStatus(false);
                settingHelper.setFacebookToken("");
                settingHelper.setUserID("");
            }
        } else {
            session = new Session(context);
            Session.setActiveSession(session);
            session.closeAndClearTokenInformation();
            //clear your preferences if saved
            settingHelper.setFacebookLoginStatus(false);
            settingHelper.setFacebookToken("");
            settingHelper.setUserID("");
        }
        settingHelper.setFacebookLoginStatus(false);
        settingHelper.setFacebookToken("");
        settingHelper.setUserID("");
        Log.d(facebookLogoutTag, "facebook logout complete");
    }
}
