package com.feedhenry.sdk;

import org.apache.http.entity.StringEntity;
import org.json.JSONArray;
import org.json.JSONObject;

import com.feedhenry.sdk.utils.FHLog;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;

public class FHHttpClient {

  private static AsyncHttpClient mClient = new AsyncHttpClient();
  
  private static final String LOG_TAG = "com.feedhenry.sdk.FHHttpClient";
  
  public static void post(String pUrl, JSONObject pParams, FHActCallback pCallback) throws Exception {
    mClient.setUserAgent(FH.getUserAgent());
    StringEntity entity = new StringEntity(new JSONObject().toString());
    if(null != pParams){
      entity = new StringEntity(pParams.toString(), "UTF-8");
    }
    mClient.post(null, pUrl, entity, "application/json", new FHJsonHttpResponseHandler(pCallback));
  }
  
  static class FHJsonHttpResponseHandler extends JsonHttpResponseHandler {
    
    private FHActCallback callback = null;
    
    public FHJsonHttpResponseHandler(FHActCallback pCallback){
      super();
      callback = pCallback;
    }
    
    @Override
    public void onSuccess(JSONArray pRes) {
      FHLog.v(LOG_TAG, "Got response : " + pRes.toString());
      if(null != callback){
        FHResponse fhres = new FHResponse(null, pRes, null, null);
        callback.success(fhres);
      }
    }
    
    public void onSuccess(JSONObject pRes){
      FHLog.d(LOG_TAG, "Got response : " + pRes.toString());
      if(null != callback){
        FHResponse fhres = new FHResponse(pRes,null, null, null);
        callback.success(fhres);
      }
    }
    
    public void onFailure(Throwable e, String content){
      FHLog.e(LOG_TAG, e.getMessage(), e);
      if(null != callback){
        FHResponse fhres = new FHResponse(null, null, e, content);
        callback.fail(fhres);
      }
    }
  }
}
