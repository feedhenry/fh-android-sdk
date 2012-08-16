package com.feedhenry.sdk.api;

import java.util.Properties;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.feedhenry.sdk.FH;
import com.feedhenry.sdk.FHRemote;

/**
 * The request for calling the initialization function
 */
public class FHInitializeRequest extends FHRemote {
  /**
   * Constructor
   * @param pProps the app configuration
   */
  public FHInitializeRequest(Properties pProps) {
    super(pProps);
  }

  @Override
  protected String getPath() {
    return "app/init";
  }

  @Override
  protected JSONObject getRequestArgs() {
    JSONObject reqData = new JSONObject();
    try{
      reqData.put("appId", mProperties.getProperty(APP_ID_KEY));
      reqData.put("appKey", mProperties.getProperty(APP_APIKEY_KEY));
      reqData.put("deviceID", mUDID);
      reqData.put("destination", "android");
    }catch(JSONException e){
      Log.w(FH.LOG_TAG, "Failed to add data to initialise request");
      Log.e(FH.LOG_TAG, e.getMessage(), e);
    }
    return reqData;
  }

}
