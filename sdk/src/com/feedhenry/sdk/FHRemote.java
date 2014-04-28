package com.feedhenry.sdk;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import org.apache.http.Header;
import org.apache.http.message.BasicHeader;
import org.json.fh.JSONException;
import org.json.fh.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;

import com.feedhenry.sdk.utils.FHLog;

/**
 * The base class that implements {@link FHAct}.
 */
public abstract class FHRemote implements FHAct{
  
 
  protected static final String PATH_PREFIX = "/box/srv/1.1/";
  
  protected static String LOG_TAG = "com.feedhenry.sdk.FHRemote";
  
  protected Properties mProperties;
  protected FHActCallback mCallback;
  protected String mUDID;
  protected Context mContext;
  
  public FHRemote(Context context, Properties pProps){
    mContext = context;
    mProperties = pProps;
  }
  
  public void setUDID(String pUDID){
    mUDID = pUDID;
  }

  @Override
  public void executeAsync() throws Exception {
    executeAsync(mCallback);
  }

  @Override
  public void executeAsync(FHActCallback pCallback) throws Exception {
    try{
      FHHttpClient.post(getApiURl(), buildHeaders(null), getRequestArgs(), pCallback);
    }catch(Exception e){
      FHLog.e(LOG_TAG, e.getMessage(), e);
      throw e;
    }
  }
  
  public void setCallback(FHActCallback pCallback){
    mCallback = pCallback;
  }
  
  protected String getApiURl(){
    String apiUrl = mProperties.getProperty(FH.APP_HOST_KEY);
    String url = (apiUrl.endsWith("/") ? apiUrl.substring(0, apiUrl.length() - 1) : apiUrl) + PATH_PREFIX + getPath();
    return url;
  }
  
  protected abstract String getPath();
  protected abstract JSONObject getRequestArgs();
  
  protected Header[] buildHeaders(Header[] pHeaders) throws Exception{
    return FH.getDefaultParamsAsHeaders(pHeaders);
  }

}
