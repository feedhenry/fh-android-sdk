/**
 * Copyright (c) 2015 FeedHenry Ltd, All Rights Reserved.
 *
 * Please refer to your contract with FeedHenry for the software license agreement.
 * If you do not have a contract, you do not have a license to use this software.
 */
package com.feedhenry.sdk;

import android.content.Context;
import com.feedhenry.sdk.utils.FHLog;
import com.feedhenry.sdk.utils.StringUtils;
import org.apache.http.Header;
import org.json.fh.JSONObject;

/**
 * The base class that implements {@link FHAct}.
 */
public abstract class FHRemote implements FHAct {

    public static final String PATH_PREFIX = "/box/srv/1.1/";

    protected static String LOG_TAG = "com.feedhenry.sdk.FHRemote";

    protected FHActCallback mCallback;
    protected Context mContext;

    public FHRemote(Context context) {
        mContext = context;
    }

    @Override
    public void executeAsync() throws Exception {
        executeAsync(mCallback);
    }

    @Override
    public void executeAsync(FHActCallback pCallback) throws Exception {
        try {
            FHHttpClient.post(getApiURl(), buildHeaders(null), getRequestArgs(), pCallback, false);
        } catch (Exception e) {
            FHLog.e(LOG_TAG, e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public void execute(FHActCallback pCallback) throws Exception {
        try {
            FHHttpClient.post(getApiURl(), buildHeaders(null), getRequestArgs(), pCallback, true);
        } catch (Exception e) {
            FHLog.e(LOG_TAG, e.getMessage(), e);
            throw e;
        }
    }

    public void setCallback(FHActCallback pCallback) {
        mCallback = pCallback;
    }

    protected String getApiURl() {
        String apiUrl = StringUtils.removeTrailingSlash(AppProps.getInstance().getHost());
        return apiUrl + PATH_PREFIX + getPath();
    }

    protected abstract String getPath();

    protected abstract JSONObject getRequestArgs();

    protected abstract Header[] buildHeaders(Header[] pHeaders) throws Exception;
}
