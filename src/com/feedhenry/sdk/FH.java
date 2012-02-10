package com.feedhenry.sdk;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.json.JSONObject;

import android.app.Activity;
import android.util.Log;

import com.feedhenry.sdk.api.FHActRequest;
import com.feedhenry.sdk.api.FHAuthRequest;
import com.feedhenry.sdk.exceptions.FHNotReadyException;

public class FH {

  private boolean mReady = false;
  private Properties mProperties = null;
  private static final String PROPERTY_FILE = "fh.properties";
  private static FH mInstance = null;
  private static final String LOG_TAG = "FH_SDK";
  
  public static final String FH_ACTION_ACT = "act";
  public static final String FH_ACTION_AUTH = "auth";
  
  private FH(){
    
  }
  
  public static FH shared(){
    if(null == mInstance){
      mInstance = new FH();  
    }
    return mInstance;
  }
  
  public void initialize(Activity pActivity){
    if(!mReady){
      mReady = true;
      InputStream in = null;
      try{
        in = pActivity.getAssets().open(PROPERTY_FILE);
        mProperties = new Properties();
        mProperties.load(in);
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
  
  private FHAct buildAction(String pAction) throws FHNotReadyException {
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
  
  public FHActRequest act(String pRemoteAction, JSONObject pParams, FHActCallback pCallback) throws FHNotReadyException {
    FHActRequest request = (FHActRequest) buildAction(FH_ACTION_ACT);
    request.setRemoteAction(pRemoteAction);
    request.setArgs(pParams);
    request.setCallback(pCallback);
    return request;
  }
  
  public FHAuthRequest auth(JSONObject pParams, FHActCallback pCallback) throws FHNotReadyException{
    FHAuthRequest request = (FHAuthRequest) buildAction(FH_ACTION_AUTH);
    request.setArgs(pParams);
    request.setCallback(pCallback);
    return request;
  }
}
