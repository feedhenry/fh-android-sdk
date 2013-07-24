package com.feedhenry.sdk;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.json.fh.JSONException;
import org.json.fh.JSONObject;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.app.Activity;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.webkit.WebView;

import com.feedhenry.sdk.api.FHActRequest;
import com.feedhenry.sdk.api.FHAuthRequest;
import com.feedhenry.sdk.api.FHInitializeRequest;
import com.feedhenry.sdk.exceptions.FHNotReadyException;
import com.feedhenry.sdk.utils.FHLog;
/**
 * The FH class provides static methods to initialize the library, create new instance of all the API request objects and configure global settings.
 */
public class FH {

  private static boolean mReady = false;
  private static Properties mProperties = null;
  private static JSONObject mCloudProps = null;
  private static final String PROPERTY_FILE = "fh.properties";
  private static final String LOG_TAG = "com.feedhenry.sdk.FH";
  
  private static final String FH_API_ACT = "cloud";
  private static final String FH_API_AUTH = "auth";
  private static String mUDID;
  
  
  public static final int LOG_LEVEL_VERBOSE = 1;
  public static final int LOG_LEVEL_DEBUG = 2;
  public static final int LOG_LEVEL_INFO = 3;
  public static final int LOG_LEVEL_WARNING = 4;
  public static final int LOG_LEVEL_ERROR = 5;
  public static final int LOG_LEVEL_NONE = Integer.MAX_VALUE;
  
  private static int mLogLevel = LOG_LEVEL_ERROR;
  
  public static final String VERSION = "1.1.1"; //TODO: need to find a better way to automatically update this version value
  private static String USER_AGENT = null;
  
  private static boolean mInitCalled = false;
  private static boolean mIsOnline = false;
  
  private static Context mContext;
  private static NetworkReceiver mReceiver;
  
  private FH() throws Exception {
    throw new Exception("Not Supported");
  }
  
  /**
   * Initialize the application. This must be called before the application can use the FH library. 
   * The initialization process happens in a background thread so that the UI thread won't be blocked.
   * If you need to call other FH API methods, you need to make sure they are called after the init finishes. The best way to do it is to provide a FHActCallback instance and implement the success method.
   * The callback functions are invoked on the main UI thread. For example, in your main activity class's onCreate method, you can do this
   * <pre>
   * {@code 
   *  FH.init(this, new FHActCallback() {
   *    public void success(FHResponse pRes) {
   *      //pRes will be null for init call if it succeeds, don't use it to access response data
   *      FHActRequest request = FH.buildActRequest("readData", new JSONObject());
   *      request.executeAsync(new FHActCallback(){
   *        public void success(FHResponse pResp){
   *          //process response data
   *        }
   *        
   *        public void fail(FHResponse pResp){
   *          //process error data
   *        }
   *      })
   *    }
   *     
   *    public void fail(FHResponse pRes) {
   *      Log.e("FHInit", pRes.getErrorMessage(), pRes.getError());
   *    }
   *  });
   * }
   *</pre>
   * @param pContext your application's context
   * @param pCallback the callback function to be executed after the initialization is finished
   */
  public static void init(Context pContext, FHActCallback pCallback){
	mContext = pContext;
	if(!mInitCalled){
      getDeviceId(pContext);
      setUserAgent(pContext);
      checkNetworkStatus();
      IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
      mReceiver = new NetworkReceiver();
      mContext.registerReceiver(mReceiver, filter);
      InputStream in = null;
      try{
        in = pContext.getAssets().open(PROPERTY_FILE);
        mProperties = new Properties();
        mProperties.load(in);
      } catch(IOException e){
        mReady = false;
        FHLog.e(LOG_TAG, "Can not load property file : " + PROPERTY_FILE, e);
      } finally{
        if(null != in){
          try {
            in.close();
          } catch (IOException ex) {
            FHLog.e(LOG_TAG, "Failed to close stream", ex);
          }
        }
      }
      mInitCalled = true;
    }
    if(!mReady){
    	FHInitializeRequest initRequest = new FHInitializeRequest(mContext, mProperties);
        initRequest.setUDID(mUDID);
        final FHActCallback cb = pCallback;
        try{
          initRequest.executeAsync(new FHActCallback() {
            @Override
            public void success(FHResponse pResponse) {            
              mReady = true;
              FHLog.v(LOG_TAG, "FH init response = " + pResponse.getJson().toString());
              mCloudProps = pResponse.getJson();
              
              // Save init
              SharedPreferences prefs = mContext.getSharedPreferences("init", Context.MODE_PRIVATE);
              SharedPreferences.Editor editor = prefs.edit();
              if (mCloudProps.has("init")) {
                try {
                  editor.putString("init", mCloudProps.getString("init"));
                  editor.commit();
                } catch (JSONException e) {
                  e.printStackTrace();
                }
              }

              if(null != cb){
                cb.success(null);
              }
            }
            
            @Override
            public void fail(FHResponse pResponse) {
              mReady = false;
              FHLog.e(LOG_TAG, "FH init failed with error = " + pResponse.getErrorMessage(), pResponse.getError());
              if(null != cb){
                cb.fail(pResponse);
              }
            }
          });
        }catch(Exception e){
          FHLog.e(LOG_TAG, "FH init exception = " + e.getMessage(), e); 
        }
    } else {
      pCallback.success(null);
    }
  }
  
  private static void checkNetworkStatus() {
    ConnectivityManager connMgr = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
    NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
    if(null != networkInfo && networkInfo.isConnected()){
      String type = networkInfo.getTypeName();
      FHLog.i(LOG_TAG, "Device is online. Connection type : " + type);
      mIsOnline = true;
    } else {
      FHLog.i(LOG_TAG, "Device is offline.");
      mIsOnline = false;
    }
    if(mIsOnline && !mReady && mInitCalled){
      init(mContext, null);
    }
  }
  
  private static FHAct buildAction(String pAction) throws FHNotReadyException {
    if(!mInitCalled){
      throw new FHNotReadyException();
    }
    FHAct action = null;
    if(FH_API_ACT.equalsIgnoreCase(pAction)){
      action = new FHActRequest(mContext, mProperties, mCloudProps);
    } else if(FH_API_AUTH.equalsIgnoreCase(pAction)){
      action = new FHAuthRequest(mContext, mProperties);
    } else {
      FHLog.w(LOG_TAG, "Invalid action : " + pAction);
    }
    return action;
  }
  
  public static boolean isOnline(){
    return mIsOnline;
  }
  
  public static void stop(){
    if(null != mReceiver){
      mContext.unregisterReceiver(mReceiver);
    }
  }
  
  /**
   * Check if FH is ready
   * @return A boolean value to indicate if FH finishes initialization
   */
  public static boolean isReady(){
    return mReady;
  }
  
  /**
   * Build an instance of {@link FHActRequest} object to perform act request. 
   * @param pRemoteAction the name of the cloud side function
   * @param pParams the parameters for the cloud side function
   * @return an instance of FHActRequest
   * @throws FHNotReadyException
   */
  public static FHActRequest buildActRequest(String pRemoteAction, JSONObject pParams) throws FHNotReadyException {
    FHActRequest request = (FHActRequest) buildAction(FH_API_ACT);
    request.setRemoteAction(pRemoteAction);
    request.setArgs(pParams);
    return request;
  }
  
  /**
   * Build an instance of FHAuthRequest object to perform authentication request.
   * @return an instance of FHAuthRequest
   * @throws FHNotReadyException
   */
  public static FHAuthRequest buildAuthRequest() throws FHNotReadyException{
    FHAuthRequest request = (FHAuthRequest) buildAction(FH_API_AUTH);
    request.setUDID(mUDID);
    return request;
  }
  
  /**
   * Build an instance of FHAuthRequest object to perform authentication request and set the auth policy id
   * @param pPolicyId the auth policy id used by this auth request
   * @return an instance of FHAuthRequest
   * @throws FHNotReadyException
   */
  public static FHAuthRequest buildAuthRequest(String pPolicyId) throws FHNotReadyException{
    FHAuthRequest request = (FHAuthRequest) buildAction(FH_API_AUTH);
    request.setUDID(mUDID);
    request.setAuthPolicyId(pPolicyId);
    return request;
  }
  
  /**
   * Build an instance of FHAuthRequest object to perform authentication request and set the auth policy id, user name and passowrd
   * @param pPolicyId the auth policy id used by this auth request
   * @param pUserName the required user name for the auth request
   * @param pPassword the required password for the auth request
   * @return an instance of FHAuthRequest
   * @throws FHNotReadyException
   */
  public static FHAuthRequest buildAuthRequest(String pPolicyId, String pUserName, String pPassword) throws FHNotReadyException {
    FHAuthRequest request = (FHAuthRequest) buildAction(FH_API_AUTH);
    request.setUDID(mUDID);
    request.setAuthUser(pPolicyId, pUserName, pPassword);
    return request;
  }
  
  /**
   * Set the log level for the library. The default level is {@link #LOG_LEVEL_ERROR}. Please make sure this is set to {@link #LOG_LEVEL_ERROR} or {@link #LOG_LEVEL_NONE} before releasing the application.
   * The log level can be one of
   * <ul>
   *   <li>{@link #LOG_LEVEL_VERBOSE}</li>
   *   <li>{@link #LOG_LEVEL_DEBUG}</li>
   *   <li>{@link #LOG_LEVEL_INFO}</li>
   *   <li>{@link #LOG_LEVEL_WARNING}</li>
   *   <li>{@link #LOG_LEVEL_ERROR}</li>
   *   <li>{@link #LOG_LEVEL_NONE}</li>
   * </ul>
   * @param pLogLevel The level of logging for the FH library
   */
  public static void setLogLevel(int pLogLevel){
    mLogLevel = pLogLevel; 
  }
  
  /**
   * Get the current log level for the FH library
   * @return The current log level
   */
  public static int getLogLevel(){
    return mLogLevel;
  }
  
  /**
   * Get the customized user-agent string for the SDK
   * @return customized user-agent string
   */
  public static String getUserAgent(){
    return USER_AGENT;
  }
  
  private static void getDeviceId(Context pContext){
    mUDID = android.provider.Settings.System.getString(pContext.getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);
  }
  
  private static void setUserAgent(Context pContext){
    if(null == USER_AGENT){
      USER_AGENT = new WebView(pContext).getSettings().getUserAgentString();
    }
  }
  
  private static class NetworkReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
      checkNetworkStatus();
    }
    
  }
}
