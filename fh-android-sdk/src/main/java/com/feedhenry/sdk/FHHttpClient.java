/**
 * Copyright (c) 2015 FeedHenry Ltd, All Rights Reserved.
 *
 * Please refer to your contract with FeedHenry for the software license agreement.
 * If you do not have a contract, you do not have a license to use this software.
 */
package com.feedhenry.sdk;

import com.feedhenry.sdk.utils.FHLog;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.SyncHttpClient;
import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.HttpHost;
import cz.msebera.android.httpclient.conn.params.ConnRoutePNames;
import cz.msebera.android.httpclient.entity.StringEntity;
import java.util.Iterator;
import org.json.fh.JSONArray;
import org.json.fh.JSONObject;

public class FHHttpClient {

    private static final AsyncHttpClient mClient = new AsyncHttpClient();
    private static final SyncHttpClient mSyncClient = new SyncHttpClient();
    private static final com.feedhenry.sdk2.FHHttpClient instance = new com.feedhenry.sdk2.FHHttpClient();
    private static final String LOG_TAG = "com.feedhenry.sdk.FHHttpClient";

    /**
     * 
     * This method executes an HTTP PUT Command
     * 
     * @param pUrl url to PUT to
     * @param pHeaders HTTP headers for the request
     * @param pParams The Body of the Request
     * @param pCallback A callback to be handed the responses of the call
     * @param pUseSync whether or not to make the call synchronously
     * @throws Exception thrown if any exception occurs
     * @deprecated please use com.feedhenry.sdk2.FHHttpClient.put instead
     */
    @Deprecated
    public static void put(
        String pUrl,
        Header[] pHeaders,
        JSONObject pParams,
        FHActCallback pCallback,
        boolean pUseSync)
        throws Exception {
        instance.put(pUrl, pHeaders, pParams, pCallback, pUseSync);
    }

    /**
     * 
     * This method executes an HTTP GET Command
     * 
     * @param pUrl url to GET 
     * @param pHeaders HTTP headers for the request
     * @param pParams Addition parameters to send with the get request
     * @param pCallback A callback to be handed the responses of the call
     * @param pUseSync whether or not to make the call synchronously
     * @throws Exception thrown if any exception occurs
     * @deprecated please use com.feedhenry.sdk2.FHHttpClient.get instead
     */
    @Deprecated
    public static void get(
        String pUrl,
        Header[] pHeaders,
        JSONObject pParams,
        FHActCallback pCallback,
        boolean pUseSync)
        throws Exception {
        instance.get(pUrl, pHeaders, pParams, pCallback, pUseSync);
    }

    /**
     * 
     * This method executes an HTTP POST Command
     * 
     * @param pUrl url to POST to
     * @param pHeaders HTTP headers for the request
     * @param pParams The Body of the Request
     * @param pCallback A callback to be handed the responses of the call
     * @param pUseSync whether or not to make the call synchronously
     * @throws Exception thrown if any exception occurs
     * @deprecated please use com.feedhenry.sdk2.FHHttpClient.post instead
     */
    @Deprecated
    public static void post(
        String pUrl,
        Header[] pHeaders,
        JSONObject pParams,
        FHActCallback pCallback,
        boolean pUseSync)
        throws Exception {
        instance.post(pUrl, pHeaders, pParams, pCallback, pUseSync);
    }

    /**
     * 
     * This method executes an HTTP DELETE Command
     * 
     * @param pUrl url to DELETE 
     * @param pHeaders HTTP headers for the request
     * @param pParams Addition parameters to send with the DELETE request
     * @param pCallback A callback to be handed the responses of the call
     * @param pUseSync whether or not to make the call synchronously
     * @throws Exception thrown if any exception occurs
     * @deprecated please use com.feedhenry.sdk2.FHHttpClient.delete instead
     */
    @Deprecated
    public static void delete(
        String pUrl,
        Header[] pHeaders,
        JSONObject pParams,
        FHActCallback pCallback,
        boolean pUseSync)
        throws Exception {
        instance.delete(pUrl, pHeaders, pParams, pCallback, pUseSync);
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
            String errorResponse = (pErrorResponse != null) ? pErrorResponse.toString() : "{}";
            if (callback != null) {
                FHResponse fhres = new FHResponse(
                    new JSONObject(errorResponse),
                    null,
                    pError,
                    errorResponse);
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
    
    /**
     * Set both the connection and socket timeouts. By default, both are set to
     * 10 seconds.
     *
     * @param milliseconds the connect/socket timeout in milliseconds, at least 1 second
     * @deprecated please use com.feedhenry.sdk2.FHHttpClient.setTimeout instead
     */
    @Deprecated
    public static void setTimeout(int milliseconds) {
        instance.setTimeout(milliseconds);
    }
    
    /**
     * Setups a proxy to use for HTTP requests in the application.  This is 
     * primarily useful for debugging.
     * 
     * @param proxy a proxy to use.
     * @deprecated please use com.feedhenry.sdk2.FHHttpClient.setHttpProxy instead
     */
    @Deprecated
    public static void setHttpProxy(HttpHost proxy) {
        instance.setHttpProxy(proxy);
    }
    
}
