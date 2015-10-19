/**
 * Copyright (c) 2015 FeedHenry Ltd, All Rights Reserved.
 *
 * Please refer to your contract with FeedHenry for the software license agreement.
 * If you do not have a contract, you do not have a license to use this software.
 */
package com.feedhenry.sdk.api;

import android.content.Context;
import com.feedhenry.sdk.CloudProps;
import com.feedhenry.sdk.FH;
import com.feedhenry.sdk.FHActCallback;
import com.feedhenry.sdk.FHHttpClient;
import com.feedhenry.sdk.FHRemote;
import com.feedhenry.sdk.utils.FHLog;
import cz.msebera.android.httpclient.Header;
import org.json.fh.JSONObject;

public class FHCloudRequest extends FHRemote {

    public enum Methods {
        GET, POST, PUT, DELETE;

        public static Methods parse(String pMethod) throws Exception {
            try {
                return Methods.valueOf(pMethod.toUpperCase());
            } catch (Exception e) {
                throw new Exception("Unsupported HTTP method: " + pMethod);
            }
        }
    }

    protected static final String LOG_TAG = "com.feedhenry.sdk.api.FHCloudRequest";

    private String mPath = "";
    private Methods mMethod = Methods.GET;
    private Header[] mHeaders = null;
    private JSONObject mArgs = new JSONObject();

    public FHCloudRequest(Context context) {
        super(context);
    }

    public void setPath(String pPath) {
        mPath = pPath;
    }

    public void setMethod(Methods pMethod) {
        mMethod = pMethod;
    }

    public void setHeaders(Header[] pHeaders) {
        mHeaders = pHeaders;
    }

    public void setRequestArgs(JSONObject pArgs) {
        mArgs = pArgs;
    }

    @Override
    protected String getPath() {
        return mPath;
    }

    @Override
    protected JSONObject getRequestArgs() {
        return mArgs;
    }

    @Override
    public void executeAsync(FHActCallback pCallback) throws Exception {
        try {
            switch (mMethod) {
                case GET:
                    FHHttpClient.get(getURL(), buildHeaders(mHeaders), mArgs, pCallback, false);
                    break;
                case PUT:
                    FHHttpClient.put(getURL(), buildHeaders(mHeaders), mArgs, pCallback, false);
                    break;
                case POST:
                    FHHttpClient.post(getURL(), buildHeaders(mHeaders), mArgs, pCallback, false);
                    break;
                case DELETE:
                    FHHttpClient.delete(getURL(), buildHeaders(mHeaders), mArgs, pCallback, false);
                    break;
                default:
                    break;
            }
        } catch (Exception e) {
            FHLog.e(LOG_TAG, e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public void execute(FHActCallback pCallback) throws Exception {
        try {
            switch (mMethod) {
                case GET:
                    FHHttpClient.get(getURL(), buildHeaders(mHeaders), mArgs, pCallback, true);
                    break;
                case PUT:
                    FHHttpClient.put(getURL(), buildHeaders(mHeaders), mArgs, pCallback, true);
                    break;
                case POST:
                    FHHttpClient.post(getURL(), buildHeaders(mHeaders), mArgs, pCallback, true);
                    break;
                case DELETE:
                    FHHttpClient.delete(getURL(), buildHeaders(mHeaders), mArgs, pCallback, true);
                    break;
            }
        } catch (Exception e) {
            FHLog.e(LOG_TAG, e.getMessage(), e);
            throw e;
        }
    }

    private String getURL() {
        String host = CloudProps.getInstance().getCloudHost();
        return host + (getPath().startsWith("/") ? getPath() : '/' + getPath());
    }

    protected Header[] buildHeaders(Header[] pHeaders) throws Exception {
        return FH.getDefaultParamsAsHeaders(pHeaders);
    }
}
