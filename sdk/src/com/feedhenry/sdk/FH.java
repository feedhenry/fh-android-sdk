package com.feedhenry.sdk;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.json.JSONObject;

import android.app.Activity;
import android.util.Log;

import com.feedhenry.sdk.api.FHActRequest;
import com.feedhenry.sdk.api.FHAuthRequest;
import com.feedhenry.sdk.api.FHInitializeRequest;
import com.feedhenry.sdk.exceptions.FHNotReadyException;

public class FH {

  private static boolean mReady = false;
  private static Properties mProperties = null;
  private static final String PROPERTY_FILE = "fh.properties";
  public static final String LOG_TAG = "FH_SDK";
  
  public static final String FH_ACTION_ACT = "act";
  public static final String FH_ACTION_AUTH = "auth";
  private static String mUDID;
  
  public static void initializeFH(Activity pActivity){
    if(!mReady){
      InputStream in = null;
      mUDID = android.provider.Settings.System.getString(pActivity.getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);
      try{
        in = pActivity.getAssets().open(PROPERTY_FILE);
        mProperties = new Properties();
        mProperties.load(in);
        FHInitializeRequest initRequest = new FHInitializeRequest(mProperties);
        initRequest.setUDID(mUDID);
        try{
          initRequest.executeAsync(new FHActCallback() {
            @Override
            public void success(FHResponse pResponse) {
              mReady = true;
              Log.d(LOG_TAG, pResponse.getResult().toString());
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
  
  public static FHActRequest buildActRequest(String pRemoteAction, JSONObject pParams) throws FHNotReadyException {
    FHActRequest request = (FHActRequest) buildAction(FH_ACTION_ACT);
    request.setRemoteAction(pRemoteAction);
    request.setArgs(pParams);
    return request;
  }
  
  public static FHAuthRequest buildAuthRequest(JSONObject pParams) throws FHNotReadyException{
    FHAuthRequest request = (FHAuthRequest) buildAction(FH_ACTION_AUTH);
    request.setUDID(mUDID);
    request.setArgs(pParams);
    return request;
  }
  
}
