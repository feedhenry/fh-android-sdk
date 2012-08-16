package com.feedhenry.sdk;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.json.JSONObject;

import android.app.Activity;
import android.content.SharedPreferences;
import android.util.Log;

import com.feedhenry.sdk.api.FHActRequest;
import com.feedhenry.sdk.api.FHAuthRequest;
import com.feedhenry.sdk.api.FHInitializeRequest;
import com.feedhenry.sdk.exceptions.FHNotReadyException;
/**
 * Provides access to FeedHenry's cloud action calls.
 *
 */
public class FH {

  private static boolean mReady = false;
  private static Properties mProperties = null;
  private static JSONObject mCloudProps = null;
  private static final String PROPERTY_FILE = "fh.properties";
  public static final String LOG_TAG = "FH_SDK";
  
  public static final String FH_ACTION_CLOUD = "cloud";
  public static final String FH_ACTION_AUTH = "auth";
  private static String mUDID;
  
  /**
   * Initialize the application. It must be called before any other FH method can be used. Otherwise FH will throw an exception.
   * @param pActivity an instance of the application's activity
   * @param pCallback the callback function to be executed after the initialization is finished
   */
  public static void init(Activity pActivity, FHActCallback pCallback){
    if(!mReady){
      getDeviceId(pActivity);
      InputStream in = null;
      try{
        in = pActivity.getAssets().open(PROPERTY_FILE);
        mProperties = new Properties();
        mProperties.load(in);
        FHInitializeRequest initRequest = new FHInitializeRequest(mProperties);
        initRequest.setUDID(mUDID);
        final FHActCallback cb = pCallback;
        try{
          initRequest.executeAsync(new FHActCallback() {
            @Override
            public void success(FHResponse pResponse) {
              mReady = true;
              Log.d(LOG_TAG, pResponse.getJson().toString());
              mCloudProps = pResponse.getJson();
              if(null != cb){
                cb.success(null);
              }
            }
            
            @Override
            public void fail(FHResponse pResponse) {
              mReady = false;
              Log.d(LOG_TAG, pResponse.getErrorMessage(), pResponse.getError());
              if(null != cb){
                cb.fail(pResponse);
              }
            }
          });
        }catch(Exception e){
          Log.w(LOG_TAG, e.getMessage(), e); 
        }
      } catch(IOException e){
        mReady = false;
        Log.e(LOG_TAG, "Can not load property file : " + PROPERTY_FILE, e);
      } finally{
        if(null != in){
          try {
            in.close();
          } catch (IOException ex) {
            Log.e(LOG_TAG, "Failed to close stream");
          }
        }
      }
    }
  }
  
  private static FHAct buildAction(String pAction) throws FHNotReadyException {
    if(!mReady){
      throw new FHNotReadyException();
    }
    FHAct action = null;
    if(FH_ACTION_CLOUD.equalsIgnoreCase(pAction)){
      action = new FHActRequest(mProperties, mCloudProps);
    } else if(FH_ACTION_AUTH.equalsIgnoreCase(pAction)){
      action = new FHAuthRequest(mProperties);
    } else {
      Log.e(LOG_TAG, "Invalid action : " + pAction);
    }
    return action;
  }
  
  /**
   * Build an instance of FHActRequest object to perform act request
   * @param pRemoteAction the name of the cloud side function
   * @param pParams the parameters for the cloud side function
   * @return an instance of FHActRequest
   * @throws FHNotReadyException
   */
  public static FHActRequest buildActRequest(String pRemoteAction, JSONObject pParams) throws FHNotReadyException {
    FHActRequest request = (FHActRequest) buildAction(FH_ACTION_CLOUD);
    request.setRemoteAction(pRemoteAction);
    request.setArgs(pParams);
    return request;
  }
  
  /**
   * Build an instance of FHAuthRequest object to perform authentication request
   * @return an instance of FHAuthRequest
   * @throws FHNotReadyException
   */
  public static FHAuthRequest buildAuthRequest() throws FHNotReadyException{
    FHAuthRequest request = (FHAuthRequest) buildAction(FH_ACTION_AUTH);
    request.setUDID(mUDID);
    return request;
  }
  
  /**
   * Build an instance of FHAuthRequest object to perform authentication request and set the auth policy id
   * @param pPolicyId the auth policy id used by this auth request
   * @return
   * @throws FHNotReadyException
   */
  public static FHAuthRequest buildAuthRequest(String pPolicyId) throws FHNotReadyException{
    FHAuthRequest request = (FHAuthRequest) buildAction(FH_ACTION_AUTH);
    request.setUDID(mUDID);
    request.setAuthPolicyId(pPolicyId);
    return request;
  }
  
  /**
   * Build an instance of FHAuthRequest object to perform authentication request and set the auth policy id, user name and passowrd
   * @param pPolicyId the auth policy id used by this auth request
   * @param pUserName the required user name for the auth request
   * @param pPassword the required password for the auth request
   * @return
   * @throws FHNotReadyException
   */
  public static FHAuthRequest buildAuthRequest(String pPolicyId, String pUserName, String pPassword) throws FHNotReadyException {
    FHAuthRequest request = (FHAuthRequest) buildAction(FH_ACTION_AUTH);
    request.setUDID(mUDID);
    request.setAuthUser(pPolicyId, pUserName, pPassword);
    return request;
  }
  
  
  private static void getDeviceId(Activity pActivity){
    mUDID = android.provider.Settings.System.getString(pActivity.getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);
  }
}
