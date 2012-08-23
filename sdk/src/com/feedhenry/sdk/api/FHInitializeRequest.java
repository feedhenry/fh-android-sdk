package com.feedhenry.sdk.api;

import java.util.Properties;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.feedhenry.sdk.FH;
import com.feedhenry.sdk.FHRemote;
import com.feedhenry.sdk.utils.FHLog;

/**
 * The request for calling the initialization function
 */
public class FHInitializeRequest extends FHRemote {
  
  protected static String LOG_TAG = "com.feedhenry.sdk.FHInitializeRequest";
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
      reqData = getFHParams();
      FHLog.v(LOG_TAG, "FH init request data : " + reqData.toString());
    }catch(JSONException e){
      FHLog.w(LOG_TAG, "Failed to add data to initialise request");
      FHLog.e(LOG_TAG, e.getMessage(), e);
    }
    return reqData;
  }

}
