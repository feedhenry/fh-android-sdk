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
  private static final String PROPERTY_FILE = "fh.properties";
  public static final String LOG_TAG = "FH_SDK";
  
  private static final String FH_INIT_PREF_NAME = "__fhinitpref__";
  private static final String FH_INIT_DATA_NAME = "__fhinit__";
  
  public static final String FH_ACTION_ACT = "act";
  public static final String FH_ACTION_AUTH = "auth";
  private static String mUDID;
  
  /**
   * Initialize the application. It must be called before any other FH method can be used. Otherwise FH will throw an exception.
   * @param pActivity an instance of the application's activity
   */
  public static void initializeFH(Activity pActivity){
    if(!mReady){
      mReady = true;
      InputStream in = null;
      mUDID = android.provider.Settings.System.getString(pActivity.getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);
      final SharedPreferences sp = pActivity.getSharedPreferences(FH_INIT_PREF_NAME, 0);
      String initData = sp.getString(FH_INIT_DATA_NAME, null);
      try{
        in = pActivity.getAssets().open(PROPERTY_FILE);
        mProperties = new Properties();
        mProperties.load(in);
        FHInitializeRequest initRequest = new FHInitializeRequest(mProperties);
        initRequest.setUDID(mUDID);
        try{
          if(null != initData){
            initRequest.setInitData(new JSONObject(initData));
          }
          initRequest.executeAsync(new FHActCallback() {
            @Override
            public void success(FHResponse pResponse) {
              Log.d(LOG_TAG, pResponse.getJson().toString());
              SharedPreferences.Editor editor = sp.edit();
              editor.putString(FH_INIT_DATA_NAME, pResponse.getJson().toString());
              editor.commit();
            }
            
            @Override
            public void fail(FHResponse pResponse) {
              mReady = false;
              Log.d(LOG_TAG, pResponse.getErrorMessage(), pResponse.getError());
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
    if(FH_ACTION_ACT.equalsIgnoreCase(pAction)){
      action = new FHActRequest(mProperties);
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
    FHActRequest request = (FHActRequest) buildAction(FH_ACTION_ACT);
    request.setRemoteAction(pRemoteAction);
    request.setArgs(pParams);
    return request;
  }
  
  /**
   * Build an instance of FHAuthRequest object to perform authentication request
   * @param pParams authentication details
   * @return an instance of FHAuthRequest
   * @throws FHNotReadyException
   */
  public static FHAuthRequest buildAuthRequest(JSONObject pParams) throws FHNotReadyException{
    FHAuthRequest request = (FHAuthRequest) buildAction(FH_ACTION_AUTH);
    request.setUDID(mUDID);
    request.setArgs(pParams);
    return request;
  }
  
}
