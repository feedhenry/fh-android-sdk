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
package com.feedhenry.sdk.api;

import android.content.Context;
import com.feedhenry.sdk.FH;
import com.feedhenry.sdk.FHRemote;
import com.feedhenry.sdk.utils.FHLog;
import cz.msebera.android.httpclient.Header;
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
    protected Header[] buildHeaders(Header[] pHeaders) {
        return null;
    }
}
