package com.feedhenry.sdk.api;

import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.json.fh.JSONException;
import org.json.fh.JSONObject;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

import com.feedhenry.sdk.FHActCallback;
import com.feedhenry.sdk.FHRemote;
import com.feedhenry.sdk.FHResponse;
import com.feedhenry.sdk.oauth.FHOAuthIntent;
import com.feedhenry.sdk.oauth.FHOAuthWebView;
import com.feedhenry.sdk.utils.FHLog;

/**
 * The request for calling the authentication function.
 * Example:
 * <pre>
 * {@code
 *  FHAuthRequest authRequest = FH.buildAuthRequest();
 *  // This is an oAuth auth policy. Setting a presenting activity will allow the library to automatically handle the interaction between the user and the oAuth provider.
 *  // You also need to add the following code to your application's AndroidManifest.xml file (inside the <application> element):
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
  /**
   * Constructor
   * @param pProps the app configurations
   */
  public FHAuthRequest(Context context, Properties pProps){
    super(context, pProps);
  }
  
  /**
   * Set the policy id for this auth request
   * @param pPolicyId the auth policy id. It is required for all the auth requests
   */
  public void setAuthPolicyId(String pPolicyId){
    mPolicyId = pPolicyId;
  }
  
  /**
   * Set the user name for the auth request. Only required if the auth policy type is FeedHenry or LDAP.
   * @param pPolicyId the auth policy id
   * @param pUserName the user name
   * @param pPassword the password
   */
  public void setAuthUser(String pPolicyId, String pUserName, String pPassword){
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
    try{
      reqData.put("policyId", mPolicyId);
      reqData.put("device", mUDID);
      reqData.put("clientToken", mProperties.getProperty(APP_ID_KEY));
      JSONObject params = new JSONObject();
      if(null != mUserName && null != mPassword){
        params.put("userId", mUserName);
        params.put("password", mPassword);
      }
      reqData.put("params", params);
      addDefaultParams(reqData);
      FHLog.v(LOG_TAG, "auth params = " + reqData.toString());
    }catch(JSONException e){
      FHLog.e(LOG_TAG, e.getMessage(), e);
    }
    return reqData;
  }
  
  /**
   * If the auth policy type is OAuth, user need to enter their username and password for the OAuth provider. 
   * If an Activity instance is provided, the SDK will automatically handle this (By presenting the OAuth login page in a WebView and back to the application once the authentication process is finished).
   * If it's not provided, the application need to handle the OAuth process itself.
   * @param pActivity the parent Activity instance to invoke the WebView
   */
  public void setPresentingActivity(Context pActivity){
    mPresentingActivity = pActivity;
  }
  
  @Override
  public void executeAsync(FHActCallback pCallback) throws Exception {
    if(null == mPresentingActivity){
      //the app didn't provide an activity to presenting the webview, let the app handle the oauth process
      super.executeAsync(pCallback);
    } else {
      final FHActCallback callback = pCallback;
      FHActCallback tmpCallback = new FHActCallback() {
        @Override
        public void success(FHResponse pResponse) {
          JSONObject jsonRes = pResponse.getJson();
          try{
            String status = jsonRes.getString("status");
            if("ok".equalsIgnoreCase(status)){
              if(jsonRes.has("url")){
                String url = jsonRes.getString("url");
                FHLog.v(LOG_TAG, "Got oAuth url back, url = " + url + ". Open it in new intent.");
                Bundle data = new Bundle();
                data.putString("url", url);
                data.putString("title", "Login");
                Intent i = new Intent(mPresentingActivity, FHOAuthIntent.class);
                mReceiver = new OAuthURLRedirectReceiver(callback);
                IntentFilter filter = new IntentFilter(FHOAuthWebView.BROADCAST_ACTION_FILTER);
                mPresentingActivity.registerReceiver(mReceiver, filter);
                i.putExtra("settings", data);
                mPresentingActivity.startActivity(i);
              } else {
                callback.success(pResponse);
              }
            } else {
              callback.fail(pResponse);
            }
          } catch (Exception e){
            
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
  
  private class OAuthURLRedirectReceiver extends BroadcastReceiver {

    private FHActCallback mCallback = null;
    
    public OAuthURLRedirectReceiver(FHActCallback pCallback){
      mCallback = pCallback;
    }
    
    @Override
    public void onReceive(Context pContext, Intent pIntent) {
      FHLog.d(LOG_TAG, "received event, data : " + pIntent.getStringExtra("url"));
      String data = pIntent.getStringExtra("url");
      FHResponse res = null;
      if("NOT_FINISHED".equalsIgnoreCase(data)){
        res = new FHResponse(null, null, new Exception("Cancelled"), "Cancelled");
        mCallback.fail(res);
      } else {
        if(data.indexOf("status=complete") > -1){
          String query = data.split("\\?")[1];
          String[] parts = query.split("&");
          Map<String, String> queryMap = new HashMap<String, String>();
          for(int i=0;i<parts.length;i++){
            String[] kv = parts[i].split("=");
            queryMap.put(kv[0], kv[1]);
          }
          String result = queryMap.get("result");
          if("success".equals(result)){
            JSONObject resJson = new JSONObject();
            try {
              resJson.put("sessionToken", queryMap.get("fh_auth_session"));
              resJson.put("authResponse", new JSONObject(URLDecoder.decode(queryMap.get("authResponse"))));
              res = new FHResponse(resJson, null, null, null);
            } catch (JSONException e) {
              e.printStackTrace();
            }
            mPresentingActivity.unregisterReceiver(this);
            mCallback.success(res);
          } else {
            res = new FHResponse(null, null, new Exception("Authentication failed"), "Authentication Failed");
            mCallback.fail(res);
          }
        } else {
          res = new FHResponse(null, null, new Exception("Unknown error"), "Unknown error");
          mCallback.fail(res);
        }
      }
    }
    
  }
}
