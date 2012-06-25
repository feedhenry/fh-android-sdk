package com.feedhenry.sdk.api;

import java.util.Properties;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.feedhenry.sdk.FH;
import com.feedhenry.sdk.FHRemote;

public class FHInitializeRequest extends FHRemote {

  private String mUDID;
  
  public FHInitializeRequest(Properties pProps) {
    super(pProps);
  }
  
  public void setUDID(String pUDID){
    mUDID = pUDID;
  }

  @Override
  protected String getPath(String pDomain, String pAppGuid, String pInstGuid) {
    return "wid/" + pDomain + "/android/" + pAppGuid + "/initialise";
  }

  @Override
  protected JSONObject getRequestArgs(String pDomain, String pAppGuid, String pInstGuid) {
    JSONObject reqData = new JSONObject();
    try{
      reqData.put("appid", pAppGuid);
      reqData.put("instid", pInstGuid);
      reqData.put("uuid", mUDID);
      reqData.put("destination", "android");
      reqData.put("domain", pDomain);
    }catch(JSONException e){
      Log.w(FH.LOG_TAG, "Failed to add data to initialise request");
      Log.e(FH.LOG_TAG, e.getMessage(), e);
    }
    return reqData;
  }

}
