package com.feedhenry.sdk;

import java.util.Iterator;

import org.apache.http.Header;
import org.apache.http.entity.StringEntity;
import org.json.fh.JSONArray;
import org.json.fh.JSONObject;

import android.os.Looper;

import com.feedhenry.sdk.utils.FHLog;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

public class FHHttpClient {

  private static AsyncHttpClient mClient = new AsyncHttpClient();

  private static final String LOG_TAG = "com.feedhenry.sdk.FHHttpClient";
  
  private static void ensureThreadLooperExist() {
	if(null == Looper.myLooper()){
	  Looper.prepare();
	}
  }
  
  public static void put(String pUrl, Header[] pHeaders, JSONObject pParams, FHActCallback pCallback) throws Exception {
    if (FH.isOnline()) {
      mClient.setUserAgent(FH.getUserAgent());
      StringEntity entity = new StringEntity(new JSONObject().toString());
      if (null != pParams) {
        entity = new StringEntity(pParams.toString(), "UTF-8");
      }
      ensureThreadLooperExist();
      mClient.put(null, pUrl, pHeaders, entity, "application/json", new FHJsonHttpResponseHandler(pCallback));
    } else {
      FHResponse res = new FHResponse(null, null, new Exception("offline"),
          "offline");
      pCallback.fail(res);
    }
  }
  
  public static void get(String pUrl, Header[] pHeaders, JSONObject pParams, FHActCallback pCallback) throws Exception {
    if (FH.isOnline()) {
      mClient.setUserAgent(FH.getUserAgent());
      ensureThreadLooperExist();
      mClient.get(null, pUrl, pHeaders, convertToRequestParams(pParams), new FHJsonHttpResponseHandler(pCallback));
    } else {
      FHResponse res = new FHResponse(null, null, new Exception("offline"),
          "offline");
      pCallback.fail(res);
    }
  }

  public static void post(String pUrl, Header[] pHeaders, JSONObject pParams, FHActCallback pCallback) throws Exception {
    if (FH.isOnline()) {
      mClient.setUserAgent(FH.getUserAgent());
      StringEntity entity = new StringEntity(new JSONObject().toString());
      if (null != pParams) {
        entity = new StringEntity(pParams.toString(), "UTF-8");
      }
      ensureThreadLooperExist();
      mClient.post(null, pUrl, pHeaders, entity, "application/json",
          new FHJsonHttpResponseHandler(pCallback));
    } else {
      FHResponse res = new FHResponse(null, null, new Exception("offline"),
          "offline");
      pCallback.fail(res);
    }
  }
  
  public static void delete(String pUrl, Header[] pHeaders, JSONObject pParams, FHActCallback pCallback) throws Exception {
    if (FH.isOnline()) {
      mClient.setUserAgent(FH.getUserAgent());
      ensureThreadLooperExist();
      mClient.delete(null, pUrl, null, convertToRequestParams(pParams), new FHJsonHttpResponseHandler(pCallback));
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
