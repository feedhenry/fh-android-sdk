package com.feedhenry.sdk;

import java.util.Properties;

import org.json.JSONObject;

import com.feedhenry.sdk.utils.FHLog;

/**
 * The base class that implements {@link FHAct}.
 */
public abstract class FHRemote implements FHAct{
  
  protected static final String APP_HOST_KEY = "host";
  protected static final String APP_ID_KEY = "appid";
  protected static final String APP_APIKEY_KEY = "appkey";
  protected static final String APP_MODE_KEY = "mode";
  protected static final String PATH_PREFIX = "/box/srv/1.1/";
  
  protected static String LOG_TAG = "com.feedhenry.sdk.FHRemote";
  
  protected Properties mProperties;
  protected FHActCallback mCallback;
  protected String mUDID;
  
  public FHRemote(Properties pProps){
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
      FHHttpClient.post(getApiURl(), getRequestArgs(), pCallback);
    }catch(Exception e){
      FHLog.e(LOG_TAG, e.getMessage(), e);
      throw e;
    }
  }
  
  public void setCallback(FHActCallback pCallback){
    mCallback = pCallback;
  }
  
  protected String getApiURl(){
    String apiUrl = mProperties.getProperty(APP_HOST_KEY);
    String url = (apiUrl.endsWith("/") ? apiUrl.substring(0, apiUrl.length() - 1) : apiUrl) + PATH_PREFIX + getPath();
    return url;
  }
  
  protected void addDefaultParams(JSONObject pParams){
    try{
      if(null != pParams){
        JSONObject defaultParams = new JSONObject();
        defaultParams.put("cuid", mUDID);
        defaultParams.put("appid", mProperties.getProperty(APP_ID_KEY));
        defaultParams.put("appkey", mProperties.getProperty(APP_APIKEY_KEY));
        defaultParams.put("agent", FH.getUserAgent());
        if(!pParams.has("__fh")){
          pParams.put("__fh", defaultParams);
        }
      }
    } catch (Exception e){
      FHLog.e(LOG_TAG, e.getMessage(), e);
    }
  }
  
  protected abstract String getPath();
  protected abstract JSONObject getRequestArgs();

}
