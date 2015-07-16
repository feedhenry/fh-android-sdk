/**
 * Copyright (c) 2015 FeedHenry Ltd, All Rights Reserved.
 *
 * Please refer to your contract with FeedHenry for the software license agreement.
 * If you do not have a contract, you do not have a license to use this software.
 */
package com.feedhenry.sdk;

import com.feedhenry.sdk.utils.FHLog;
import com.loopj.android.http.*;
import org.apache.http.Header;
import org.apache.http.entity.StringEntity;
import org.json.fh.JSONArray;
import org.json.fh.JSONObject;

import java.util.Iterator;

public class FHHttpClient {

    private static AsyncHttpClient mClient = new AsyncHttpClient();
    private static SyncHttpClient mSyncClient = new SyncHttpClient();

    private static final String LOG_TAG = "com.feedhenry.sdk.FHHttpClient";

    public static void put(
        String pUrl,
        Header[] pHeaders,
        JSONObject pParams,
        FHActCallback pCallback,
        boolean pUseSync)
        throws Exception {
        if (FH.isOnline()) {
            StringEntity entity = new StringEntity(new JSONObject().toString());
            if (pParams != null) {
                entity = new StringEntity(pParams.toString(), "UTF-8");
            }
            if (pUseSync) {
                mSyncClient.setUserAgent(FH.getUserAgent());
                mSyncClient.put(
                    null,
                    pUrl,
                    pHeaders,
                    entity,
                    "application/json",
                    new FHJsonHttpResponseHandler(pCallback));
            } else {
                mClient.setUserAgent(FH.getUserAgent());
                mClient.put(
                    null,
                    pUrl,
                    pHeaders,
                    entity,
                    "application/json",
                    new FHJsonHttpResponseHandler(pCallback));
            }
        } else {
            FHResponse res = new FHResponse(null, null, new Exception("offline"), "offline");
            pCallback.fail(res);
        }
    }

    public static void get(
        String pUrl,
        Header[] pHeaders,
        JSONObject pParams,
        FHActCallback pCallback,
        boolean pUseSync)
        throws Exception {
        if (FH.isOnline()) {
            if (pUseSync) {
                mSyncClient.setUserAgent(FH.getUserAgent());
                mSyncClient.get(
                    null,
                    pUrl,
                    pHeaders,
                    convertToRequestParams(pParams),
                    new FHJsonHttpResponseHandler(pCallback));
            } else {
                mClient.setUserAgent(FH.getUserAgent());
                mClient.get(
                    null,
                    pUrl,
                    pHeaders,
                    convertToRequestParams(pParams),
                    new FHJsonHttpResponseHandler(pCallback));
            }
        } else {
            FHResponse res = new FHResponse(null, null, new Exception("offline"), "offline");
            pCallback.fail(res);
        }
    }

    public static void post(
        String pUrl,
        Header[] pHeaders,
        JSONObject pParams,
        FHActCallback pCallback,
        boolean pUseSync)
        throws Exception {
        if (FH.isOnline()) {

            StringEntity entity = new StringEntity(new JSONObject().toString());
            if (pParams != null) {
                entity = new StringEntity(pParams.toString(), "UTF-8");
            }
            if (pUseSync) {
                mSyncClient.setUserAgent(FH.getUserAgent());
                mSyncClient.post(
                    null,
                    pUrl,
                    pHeaders,
                    entity,
                    "application/json",
                    new FHJsonHttpResponseHandler(pCallback));
            } else {
                mClient.setUserAgent(FH.getUserAgent());
                mClient.post(
                    null,
                    pUrl,
                    pHeaders,
                    entity,
                    "application/json",
                    new FHJsonHttpResponseHandler(pCallback));
            }
        } else {
            FHResponse res = new FHResponse(null, null, new Exception("offline"), "offline");
            pCallback.fail(res);
        }
    }

    public static void delete(
        String pUrl,
        Header[] pHeaders,
        JSONObject pParams,
        FHActCallback pCallback,
        boolean pUseSync)
        throws Exception {
        if (FH.isOnline()) {
            if (pUseSync) {
                mSyncClient.setUserAgent(FH.getUserAgent());
                mSyncClient.delete(
                    null,
                    pUrl,
                    pHeaders,
                    convertToRequestParams(pParams),
                    new FHJsonHttpResponseHandler(pCallback));
            } else {
                mClient.setUserAgent(FH.getUserAgent());
                mClient.delete(
                    null,
                    pUrl,
                    pHeaders,
                    convertToRequestParams(pParams),
                    new FHJsonHttpResponseHandler(pCallback));
            }
        } else {
            FHResponse res = new FHResponse(null, null, new Exception("offline"), "offline");
            pCallback.fail(res);
        }
    }

    private static RequestParams convertToRequestParams(JSONObject pIn) {
        RequestParams rp = null;
        if (pIn != null) {
            rp = new RequestParams();
            for (Iterator<String> it = pIn.keys(); it.hasNext(); ) {
                String key = it.next();
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
            if (callback != null) {
                FHResponse fhres = new FHResponse(new JSONObject(pRes.toString()), null, null, null);
                callback.success(fhres);
            }
        }

        @Override
        public void onSuccess(int pStatusCode, Header[] pHeaders, org.json.JSONArray pRes) {
            FHLog.v(LOG_TAG, "Got response : " + pRes.toString());
            if (callback != null) {
                FHResponse fhres = new FHResponse(null, new JSONArray(pRes.toString()), null, null);
                callback.success(fhres);
            }
        }

        @Override
        public void onFailure(int pStatusCode, Header[] pHeaders, String pContent, Throwable pError) {
            FHLog.e(LOG_TAG, pError.getMessage(), pError);
            if (callback != null) {
                FHResponse fhres = new FHResponse(null, null, pError, pContent);
                callback.fail(fhres);
            }
        }

        @Override
        public void onFailure(
            int pStatusCode,
            Header[] pHeaders,
            Throwable pError,
            org.json.JSONObject pErrorResponse) {
            FHLog.e(LOG_TAG, pError.getMessage(), pError);
            if (callback != null) {
                FHResponse fhres = new FHResponse(
                    new JSONObject(pErrorResponse.toString()),
                    null,
                    pError,
                    pErrorResponse.toString());
                callback.fail(fhres);
            }
        }

        @Override
        public void onFailure(int pStatusCode, Header[] pHeaders, Throwable pError, org.json.JSONArray pErrorResponse) {
            FHLog.e(LOG_TAG, pError.getMessage(), pError);
            if (callback != null) {
                FHResponse fhres = new FHResponse(
                    null,
                    new JSONArray(pErrorResponse.toString()),
                    pError,
                    pErrorResponse.toString());
                callback.fail(fhres);
            }
        }
    }
}
