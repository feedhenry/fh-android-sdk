/**
 * Copyright Red Hat, Inc, and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.feedhenry.sdk2;

import com.feedhenry.sdk.*;
import com.feedhenry.sdk.utils.FHLog;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.SyncHttpClient;
import cz.msebera.android.httpclient.Consts;
import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.HttpHost;
import cz.msebera.android.httpclient.conn.params.ConnRoutePNames;
import cz.msebera.android.httpclient.entity.StringEntity;
import java.util.Iterator;
import org.json.fh.JSONArray;
import org.json.fh.JSONObject;

public class FHHttpClient {

    private final AsyncHttpClient mClient = new AsyncHttpClient();
    private final SyncHttpClient mSyncClient = new SyncHttpClient();

    private static final String LOG_TAG = "com.feedhenry.sdk.FHHttpClient";

    public void put(
        String pUrl,
        Header[] pHeaders,
        JSONObject pParams,
        FHActCallback pCallback,
        boolean pUseSync) {
        if (FH.isOnline()) {
            StringEntity entity = new StringEntity(new JSONObject().toString(), Consts.UTF_8);
            if (pParams != null) {
                entity = new StringEntity(pParams.toString(), Consts.UTF_8);
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

    public void get(
        String pUrl,
        Header[] pHeaders,
        JSONObject pParams,
        FHActCallback pCallback,
        boolean pUseSync) {
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

    public void post(
        String pUrl,
        Header[] pHeaders,
        JSONObject pParams,
        FHActCallback pCallback,
        boolean pUseSync) {
        if (FH.isOnline()) {

            StringEntity entity = new StringEntity(new JSONObject().toString(), Consts.UTF_8);
            if (pParams != null) {
                entity = new StringEntity(pParams.toString(), Consts.UTF_8);
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

    public void delete(
        String pUrl,
        Header[] pHeaders,
        JSONObject pParams,
        FHActCallback pCallback,
        boolean pUseSync) {
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

    private RequestParams convertToRequestParams(JSONObject pIn) {
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

    private static class FHJsonHttpResponseHandler extends JsonHttpResponseHandler {

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
     */
    public void setTimeout(int milliseconds) {
        mClient.setResponseTimeout(milliseconds);
        mSyncClient.setResponseTimeout(milliseconds);
    }
    
    public void setHttpProxy(HttpHost proxy) {
        mClient.getHttpClient().getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY,proxy);
        mSyncClient.getHttpClient().getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY,proxy);
    }
    
}
