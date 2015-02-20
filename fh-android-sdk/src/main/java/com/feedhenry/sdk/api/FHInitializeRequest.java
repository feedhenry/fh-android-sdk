package com.feedhenry.sdk.api;

import java.util.Properties;

import org.apache.http.Header;
import org.json.fh.JSONException;
import org.json.fh.JSONObject;

import android.content.Context;
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
   * @param context Android's applicaiton context
   * @param pProps the app configuration
   */
  public FHInitializeRequest(Context context, Properties pProps) {
    super(context, pProps);
  }

  @Override
  protected String getPath() {
    return "app/init";
  }

  @Override
  protected JSONObject getRequestArgs() {
    JSONObject reqData = new JSONObject();
    try{
      reqData = FH.getDefaultParams();
      FHLog.v(LOG_TAG, "FH init request data : " + reqData.toString());
    }catch(Exception e){
      FHLog.w(LOG_TAG, "Failed to add data to initialise request");
      FHLog.e(LOG_TAG, e.getMessage(), e);
    }
    return reqData;
  }

  @Override
  protected Header[] buildHeaders(Header[] pHeaders) throws Exception {
    return null;
  }

}
