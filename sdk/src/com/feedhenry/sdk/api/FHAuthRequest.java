package com.feedhenry.sdk.api;

import java.util.Properties;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.feedhenry.sdk.FH;
import com.feedhenry.sdk.FHRemote;

public class FHAuthRequest extends FHRemote {

  private static final String AUTH_PATH = "arm/user/auth";
  private String mUDID;
  
  public FHAuthRequest(Properties pProps){
    super(pProps);
  }
  
  public void setUDID(String pUDID){
    mUDID = pUDID;
  }
  
  @Override
  protected String getPath(String pDomain, String pAppGuid, String pInstGuid) {
    return AUTH_PATH; 
  }

  @Override
  protected JSONObject getRequestArgs(String pDomain, String pAppGuid, String pInstGuid) {
    JSONObject reqData = new JSONObject();
    try{
      reqData.put("type", mArgs.optString("type", null) == null ? "default" : mArgs.opt("type"));
      JSONObject params = new JSONObject();
      params.put("appId", pInstGuid);
      params.put("device", mUDID);
      params.put("userId", mArgs.getString("user"));
      params.put("password", mArgs.getString("password"));
      reqData.put("params", params);
    }catch(JSONException e){
      Log.e(FH.LOG_TAG, e.getMessage(), e);
    }
    return reqData;
  }
}
