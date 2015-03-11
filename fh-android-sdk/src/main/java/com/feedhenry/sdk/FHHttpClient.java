package com.feedhenry.sdk;

import java.util.Iterator;

import org.apache.http.Header;
import org.apache.http.entity.StringEntity;
import org.json.JSONArray;
import org.json.JSONObject;

import com.feedhenry.sdk.utils.FHLog;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.SyncHttpClient;

public class FHHttpClient {

  private static AsyncHttpClient mClient = new AsyncHttpClient();
  private static SyncHttpClient mSyncClient = new SyncHttpClient();

  private static final String LOG_TAG = "com.feedhenry.sdk.FHHttpClient";
  
  public static void put(String pUrl, Header[] pHeaders, JSONObject pParams, FHActCallback pCallback, boolean pUseSync) throws Exception {
    if (FH.isOnline()) {
      StringEntity entity = new StringEntity(new JSONObject().toString());
      if (null != pParams) {
        entity = new StringEntity(pParams.toString(), "UTF-8");
      }
      if(pUseSync){
    	mSyncClient.setUserAgent(FH.getUserAgent());
    	mSyncClient.put(null, pUrl, pHeaders, entity, "application/json", new FHJsonHttpResponseHandler(pCallback));
      } else {
    	mClient.setUserAgent(FH.getUserAgent());
        mClient.put(null, pUrl, pHeaders, entity, "application/json", new FHJsonHttpResponseHandler(pCallback));
      }
    } else {
      FHResponse res = new FHResponse(null, null, new Exception("offline"),
          "offline");
      pCallback.fail(res);
    }
  }
  
  public static void get(String pUrl, Header[] pHeaders, JSONObject pParams, FHActCallback pCallback, boolean pUseSync) throws Exception {
    if (FH.isOnline()) {
      if(pUseSync){
        mSyncClient.setUserAgent(FH.getUserAgent());
        mSyncClient.get(null, pUrl, pHeaders, convertToRequestParams(pParams), new FHJsonHttpResponseHandler(pCallback));
      } else {
    	mClient.setUserAgent(FH.getUserAgent());
        mClient.get(null, pUrl, pHeaders, convertToRequestParams(pParams), new FHJsonHttpResponseHandler(pCallback));
      }
    } else {
      FHResponse res = new FHResponse(null, null, new Exception("offline"),
          "offline");
      pCallback.fail(res);
    }
  }

  public static void post(String pUrl, Header[] pHeaders, JSONObject pParams, FHActCallback pCallback, boolean pUseSync) throws Exception {
    if (FH.isOnline()) {
      
      StringEntity entity = new StringEntity(new JSONObject().toString());
      if (null != pParams) {
        entity = new StringEntity(pParams.toString(), "UTF-8");
      }
      if(pUseSync){
    	mSyncClient.setUserAgent(FH.getUserAgent());
    	mSyncClient.post(null, pUrl, pHeaders, entity, "application/json",
              new FHJsonHttpResponseHandler(pCallback));
      } else {
    	mClient.setUserAgent(FH.getUserAgent());
        mClient.post(null, pUrl, pHeaders, entity, "application/json",
              new FHJsonHttpResponseHandler(pCallback));
      }
    } else {
      FHResponse res = new FHResponse(null, null, new Exception("offline"),
          "offline");
      pCallback.fail(res);
    }
  }
  
  public static void delete(String pUrl, Header[] pHeaders, JSONObject pParams, FHActCallback pCallback, boolean pUseSync) throws Exception {
    if (FH.isOnline()) {
      if(pUseSync){
    	mSyncClient.setUserAgent(FH.getUserAgent());
    	mSyncClient.delete(null, pUrl, pHeaders, convertToRequestParams(pParams), new FHJsonHttpResponseHandler(pCallback));  
      } else {
    	mClient.setUserAgent(FH.getUserAgent());
        mClient.delete(null, pUrl, pHeaders, convertToRequestParams(pParams), new FHJsonHttpResponseHandler(pCallback));  
      }
    } else {
      FHResponse res = new FHResponse(null, null, new Exception("offline"),
          "offline");
      pCallback.fail(res);
    }
  }
  
  private static RequestParams convertToRequestParams(JSONObject pIn) {
    RequestParams rp = null;
    if(null != pIn){
      rp = new RequestParams();
      Iterator<String> it = pIn.keys();
      while(it.hasNext()){
        String key = (String)it.next();
        rp.put(key, pIn.get(key));
      }
    }
    return rp;
  }

  static class FHJsonHttpResponseHandler extends JsonHttpResponseHandler {

    private FHActCallback callback = null;

    public FHJsonHttpResponseHandler(FHActCallback pCallback) {
      super();
      callback = pCallback;
    }
    

    @Override
    public void onSuccess(int pStatusCode, Header[] pHeaders, org.json.JSONObject pRes) {
      FHLog.v(LOG_TAG, "Got response : " + pRes.toString());
      if (null != callback) {
        FHResponse fhres = new FHResponse(new JSONObject(pRes.toString()), null, null, null);
        callback.success(fhres);
      }
    }
    
    @Override
    public void onSuccess(int pStatusCode, Header[] pHeaders, org.json.JSONArray pRes) {
      FHLog.v(LOG_TAG, "Got response : " + pRes.toString());
      if (null != callback) {
        FHResponse fhres = new FHResponse(null, new JSONArray(pRes.toString()), null, null);
        callback.success(fhres);
      }
    }

    @Override
    public void onFailure(int pStatusCode, Header[] pHeaders, String pContent, Throwable pError) {
      FHLog.e(LOG_TAG, pError.getMessage(), pError);
      if (null != callback) {
        FHResponse fhres = new FHResponse(null, null, pError, pContent);
        callback.fail(fhres);
      }
    }
  }
}
