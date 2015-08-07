/**
 * Copyright (c) 2015 FeedHenry Ltd, All Rights Reserved.
 *
 * Please refer to your contract with FeedHenry for the software license agreement.
 * If you do not have a contract, you do not have a license to use this software.
 */
package com.feedhenry.sdk.api;

import android.content.Context;
import com.feedhenry.sdk.FH;
import com.feedhenry.sdk.FHRemote;
import com.feedhenry.sdk.utils.FHLog;
import org.apache.http.Header;
import org.json.fh.JSONObject;

/**
 * The request for calling the initialization function.
 */
public class FHInitializeRequest extends FHRemote {

    protected static String LOG_TAG = "com.feedhenry.sdk.FHInitializeRequest";

    public FHInitializeRequest(Context context) {
        super(context);
    }

    @Override
    protected String getPath() {
        return "app/init";
    }

    @Override
    protected JSONObject getRequestArgs() {
        try {
            JSONObject reqData = FH.getDefaultParams();
            FHLog.v(LOG_TAG, "FH init request data : " + reqData.toString());
            return reqData;
        } catch (Exception e) {
            FHLog.w(LOG_TAG, "Failed to add data to initialise request");
            FHLog.e(LOG_TAG, e.getMessage(), e);
            return new JSONObject();
        }
    }

    @Override
    protected Header[] buildHeaders(Header[] pHeaders) throws Exception {
        return null;
    }
}
