package com.feedhenry.sdk;

import org.apache.http.entity.StringEntity;
import org.json.JSONObject;

import android.util.Log;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;

public class FHHttpClient {

  private static AsyncHttpClient mClient = new AsyncHttpClient();
  private static final String USER_AGENT = "fh_android_sdk";
  
  public static void post(String pUrl, JSONObject pParams, FHActCallback pCallback) throws Exception {
    mClient.setUserAgent(USER_AGENT);
    final FHActCallback callback = pCallback;
    StringEntity entity = new StringEntity(new JSONObject().toString());
    if(null != pParams){
      entity = new StringEntity(pParams.toString(), "UTF-8");
    }
    mClient.post(null, pUrl, entity, "application/json", new JsonHttpResponseHandler(){
      public void onSuccess(JSONObject pRes){
        Log.d(FH.LOG_TAG, "Got response : " + pRes.toString());
        if(null != callback){
          FHResponse fhres = new FHResponse(pRes, null, null);
          callback.success(fhres);
        }
      }
      
      public void onFailure(Throwable e, String content){
        Log.e(FH.LOG_TAG, e.getMessage(), e);
        if(null != callback){
          FHResponse fhres = new FHResponse(null, e, content);
          callback.fail(fhres);
        }
      }
    });
  }
}
