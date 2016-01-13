/**
 * Copyright Red Hat, Inc, and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.feedhenry.sdk.api;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import com.feedhenry.sdk.AppProps;
import com.feedhenry.sdk.CloudProps;
import com.feedhenry.sdk.Device;
import com.feedhenry.sdk.FH;
import com.feedhenry.sdk.FHActCallback;
import com.feedhenry.sdk.FHRemote;
import com.feedhenry.sdk.FHResponse;
import com.feedhenry.sdk.oauth.FHOAuthIntent;
import com.feedhenry.sdk.oauth.FHOAuthWebView;
import com.feedhenry.sdk.utils.DataManager;
import com.feedhenry.sdk.utils.FHLog;
import com.feedhenry.sdk2.FHHttpClient;
import cz.msebera.android.httpclient.Header;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;
import org.json.fh.JSONException;
import org.json.fh.JSONObject;

/**
 * The request for calling the authentication function.
 * Example:
 *
 * <pre>
 * {@code
 *  FHAuthRequest authRequest = FH.buildAuthRequest();
 *  // This is an oAuth auth policy. Setting a presenting activity will allow the library to
 * automatically handle the interaction between the user and the oAuth provider.
 *  // You also need to add the following code to your application's AndroidManifest.xml file
 * (inside the <application> element):
 *  // <activity android:name="com.feedhenry.sdk.oauth.FHOAuthIntent" />;
 *  authRequest.setPresentingActivity(this);
 *  authRequest.setAuthPolicyId("MyGooglePolicy");
 *  authRequest.executeAsync(new FHActCallback() {
 *    public void success(FHResponse resp) {
 *      Log.d("FHAuthActivity", "user sessionToken = "+ resp.getJson().getString("sessionToken"));
 *    }
 *
 *    public void fail(FHResponse resp) {
 *      Log.d("FHAuthActivity", resp.getErrorMessage());
 *   }
 *  });
 * }
 * </pre>
 */

public class FHAuthRequest extends FHRemote {

    private static final String AUTH_PATH = "admin/authpolicy/auth";

    private String mPolicyId;
    private String mUserName;
    private String mPassword;
    private Context mPresentingActivity;
    private OAuthURLRedirectReceiver mReceiver;

    protected static String LOG_TAG = "com.feedhenry.sdk.FHAuthRequest";
    private final com.feedhenry.sdk.api2.FHAuthSession mAuthSession;

    /**
     * 
     * @param context The Android application context
     * @deprecated please use FHActRequest(Context, FHAuthSession) instead
     */
    @Deprecated
    public FHAuthRequest(Context context) {
        super(context);
        mPresentingActivity = context;
        mAuthSession = new com.feedhenry.sdk.api2.FHAuthSession(DataManager.init(context), new FHHttpClient());
    }
    
    
    /**
     * 
     * @param context The Android application context
     * @param pAuthSession an FHAuthSession to store and verify tokens
     */
    public FHAuthRequest(Context context, com.feedhenry.sdk.api2.FHAuthSession pAuthSession) {
        super(context);
        mPresentingActivity = context;
        mAuthSession = pAuthSession;
    }


    /**
     * Sets the policy ID for this auth request.
     *
     * @param pPolicyId the auth policy id. It is required for all the auth requests
     */
    public void setAuthPolicyId(String pPolicyId) {
        mPolicyId = pPolicyId;
    }

    /**
     * Sets the user name for the auth request.
     * Only required if the auth policy type is FeedHenry or LDAP.
     *
     * @param pPolicyId the auth policy id
     * @param pUserName the user name
     * @param pPassword the password
     */
    public void setAuthUser(String pPolicyId, String pUserName, String pPassword) {
        mPolicyId = pPolicyId;
        mUserName = pUserName;
        mPassword = pPassword;
    }

    @Override
    protected String getPath() {
        return AUTH_PATH;
    }

    @Override
    protected JSONObject getRequestArgs() {
        JSONObject reqData = new JSONObject();
        try {
            reqData.put("__fh", FH.getDefaultParams()); // keep backward compatibility
            reqData.put("policyId", mPolicyId);
            reqData.put("device", Device.getDeviceId(mContext));
            reqData.put("clientToken", AppProps.getInstance().getAppId());
            JSONObject params = new JSONObject();
            if (mUserName != null && mPassword != null) {
                params.put("userId", mUserName);
                params.put("password", mPassword);
            }
            reqData.put("params", params);
            String env = CloudProps.getInstance().getEnv();
            if (env != null) {
                reqData.put("environment", env);
            }
            FHLog.v(LOG_TAG, "auth params = " + reqData.toString());
        } catch (Exception e) {
            FHLog.e(LOG_TAG, e.getMessage(), e);
        }
        return reqData;
    }

    /**
     * If the auth policy type is OAuth, user need to enter their username and password for the OAuth provider.
     * If an Activity instance is provided, the SDK will automatically handle this (By presenting the
     * OAuth login page in a WebView and back to the application once the authentication process is finished).
     * If it's not provided, the application will need to handle the OAuth process itself.
     *
     * @param pActivity the parent Activity instance to invoke the WebView
     */
    public void setPresentingActivity(Context pActivity) {
        mPresentingActivity = pActivity;
    }

    @Override
    public void executeAsync(FHActCallback pCallback) {
        if (mPresentingActivity == null) {
            // the app didn't provide an activity to presenting the webview, let the app handle the oauth process
            super.executeAsync(pCallback);
        } else {
            final FHActCallback callback = pCallback;
            FHActCallback tmpCallback = new FHActCallback() {
                @Override
                public void success(FHResponse pResponse) {
                    final JSONObject jsonRes = pResponse.getJson();
                    try {
                        String status = jsonRes.getString("status");
                        if ("ok".equalsIgnoreCase(status)) {
                            if (jsonRes.has("url")) {
                                startAuthIntent(jsonRes, callback);
                            } else {
                                if (jsonRes.has(FHAuthSession.SESSION_TOKEN_KEY)) {
                                    mAuthSession.save(jsonRes.getString(FHAuthSession.SESSION_TOKEN_KEY));
                                }
                                callback.success(pResponse);
                            }
                        } else {
                            callback.fail(pResponse);
                        }
                    } catch (Exception e) {
                        FHLog.e(LOG_TAG, "Error handling response", e);
                    }
                }

                @Override
                public void fail(FHResponse pResponse) {
                    callback.fail(pResponse);
                }
            };
            super.executeAsync(tmpCallback);
        }
    }

    @Override
    public void execute(FHActCallback pCallback) {
        if (mPresentingActivity == null) {
            super.execute(pCallback);
        } else {
            final FHActCallback callback = pCallback;
            FHActCallback tmpCallback = new FHActCallback() {
                @Override
                public void success(FHResponse pResponse) {
                    final JSONObject jsonRes = pResponse.getJson();
                    try {
                        String status = jsonRes.getString("status");
                        if ("ok".equalsIgnoreCase(status)) {
                            if (jsonRes.has("url")) {
                                startAuthIntent(jsonRes, callback);
                            } else {
                                if (jsonRes.has(FHAuthSession.SESSION_TOKEN_KEY)) {
                                    mAuthSession.save(jsonRes.getString(FHAuthSession.SESSION_TOKEN_KEY));
                                }
                                callback.success(pResponse);
                            }
                        } else {
                            callback.fail(pResponse);
                        }
                    } catch (Exception e) {
                        FHLog.e(LOG_TAG, "Error handling response", e);
                    }
                }

                @Override
                public void fail(FHResponse pResponse) {
                    callback.fail(pResponse);
                }
            };
            super.execute(tmpCallback);
        }
    }

    private void startAuthIntent(final JSONObject pJsonRes, final FHActCallback pCallback) throws Exception {
        String url = pJsonRes.getString("url");
        FHLog.v(LOG_TAG, "Got oAuth url back, url = " + url + ". Open it in new intent.");
        Bundle data = new Bundle();
        data.putString("url", url);
        data.putString("title", "Login");
        Intent i = new Intent(mPresentingActivity, FHOAuthIntent.class);
        mReceiver = new OAuthURLRedirectReceiver(pCallback);
        IntentFilter filter = new IntentFilter(FHOAuthWebView.BROADCAST_ACTION_FILTER);
        mPresentingActivity.registerReceiver(mReceiver, filter);
        i.putExtra("settings", data);
        mPresentingActivity.startActivity(i);
    }

    private class OAuthURLRedirectReceiver extends BroadcastReceiver {

        private FHActCallback mCallback = null;

        public OAuthURLRedirectReceiver(FHActCallback pCallback) {
            mCallback = pCallback;
        }

        @Override
        public void onReceive(Context pContext, Intent pIntent) {
            FHLog.d(LOG_TAG, "received event, data : " + pIntent.getStringExtra("url"));
            String data = pIntent.getStringExtra("url");
            FHResponse res = null;
            if ("NOT_FINISHED".equalsIgnoreCase(data)) {
                res = new FHResponse(null, null, new Exception("Cancelled"), "Cancelled");
                mCallback.fail(res);
            } else {
                if (data.contains("status=complete")) {
                    data = data.split("#")[0];//Remove everything after an anchor
                    String query = data.split("\\?")[1];
                    String[] parts = query.split("&");
                    Map<String, String> queryMap = new HashMap<String, String>();
                    for (String part : parts) {
                        String[] kv = part.split("=");
                        queryMap.put(kv[0], kv[1]);
                    }
                    String result = queryMap.get("result");
                    if ("success".equals(result)) {
                        JSONObject resJson = new JSONObject();
                        try {
                            String sessionToken = queryMap.get("fh_auth_session");
                            if (sessionToken != null) {
                                mAuthSession.save(sessionToken);
                            }
                            resJson.put(FHAuthSession.SESSION_TOKEN_KEY, sessionToken);
                            resJson.put(
                                "authResponse",
                                new JSONObject(URLDecoder.decode(queryMap.get("authResponse"), "UTF-8")));
                            res = new FHResponse(resJson, null, null, null);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }
                        mPresentingActivity.unregisterReceiver(this);
                        mCallback.success(res);
                    } else {
                        res = new FHResponse(
                            null,
                            null,
                            new Exception("Authentication failed"),
                            "Authentication Failed");
                        mCallback.fail(res);
                    }
                } else {
                    res = new FHResponse(null, null, new Exception("Unknown error"), "Unknown error");
                    mCallback.fail(res);
                }
            }
        }
    }

    @Override
    protected Header[] buildHeaders(Header[] pHeaders) {
        return null;
    }
}
