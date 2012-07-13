package com.feedhenry.sdk;

import java.util.Properties;

import org.json.JSONObject;

import android.os.AsyncTask;
import android.util.Log;

public abstract class FHRemote implements FHAct{
  
  private static final String API_URL_KEY = "apiurl";
  private static final String GUID_KEY = "app";
  private static final String DOMAIN_KEY = "domain";
  private static final String PATH_PREFIX = "/box/srv/1.1/";
  
  protected Properties mProperties;
  protected JSONObject mArgs;
  protected FHActCallback mCallback;
  
  private String mAppGuid;
  private String mDomain;
  
  public FHRemote(Properties pProps){
    mProperties = pProps;
    mAppGuid = mProperties.getProperty(GUID_KEY);
    mDomain = mProperties.getProperty(DOMAIN_KEY);
  }

  @Override
  public void executeAsync() throws Exception {
    executeAsync(mCallback);
  }

  @Override
  public void executeAsync(FHActCallback pCallback) throws Exception {
    try{
      FHHttpClient.post(getApiURl(), getRequestArgs(mDomain, mAppGuid), pCallback);
    }catch(Exception e){
      Log.e(FH.LOG_TAG, e.getMessage(), e);
      throw e;
    }
  }
  
  public void setCallback(FHActCallback pCallback){
    mCallback = pCallback;
  }

  @Override
  public void setArgs(JSONObject pArgs) {
    mArgs = pArgs;
  }
  
  private String getApiURl(){
    String apiUrl = mProperties.getProperty(API_URL_KEY);
    String url = (apiUrl.endsWith("/") ? apiUrl.substring(0, apiUrl.length() - 1) : apiUrl) + PATH_PREFIX + getPath(mDomain, mAppGuid);
    return url;
  }
  
  protected abstract String getPath(String pDomain, String pAppGuid);
  
  protected JSONObject getRequestArgs(String pDomain, String pAppGuid){
    return mArgs;
  }

}
