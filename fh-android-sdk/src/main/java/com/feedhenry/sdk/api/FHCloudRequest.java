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
import com.feedhenry.sdk.CloudProps;
import com.feedhenry.sdk.FH;
import com.feedhenry.sdk.FHActCallback;
import com.feedhenry.sdk.FHHttpClient;
import com.feedhenry.sdk.FHRemote;
import cz.msebera.android.httpclient.Header;
import org.json.fh.JSONObject;

public class FHCloudRequest extends FHRemote {

    private final FHHttpClient fhHTTPClient;

    public enum Methods {
        GET, POST, PUT, DELETE;

        /**
         * Casts a HTTP Method name to a Methods enum.
         * 
         * @param pMethod the HTTP method to retrieve the enumerated value of.
         * @return a Methods enum value
         * @throws IllegalArgumentException if pMethod is not one of GET, POST, PUT, or DELETE
         */
        public static Methods parse(String pMethod)  {
            try {
                return Methods.valueOf(pMethod.toUpperCase());
            } catch (Exception e) {
                throw new IllegalArgumentException("Unsupported HTTP method: " + pMethod);
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
        this.fhHTTPClient = new FHHttpClient();
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
    public void execute(FHActCallback pCallback)  {
            switch (mMethod) {
                case GET:
                    fhHTTPClient.get(getURL(), buildHeaders(mHeaders), mArgs, pCallback, false);
                    break;
                case PUT:
                    fhHTTPClient.put(getURL(), buildHeaders(mHeaders), mArgs, pCallback, false);
                    break;
                case POST:
                    fhHTTPClient.post(getURL(), buildHeaders(mHeaders), mArgs, pCallback, false);
                    break;
                case DELETE:
                    fhHTTPClient.delete(getURL(), buildHeaders(mHeaders), mArgs, pCallback, false);
                    break;
                default:
                    break;
            }
        
    }

    private String getURL() {
        String host = CloudProps.getInstance().getCloudHost();
        return host + (getPath().startsWith("/") ? getPath() : '/' + getPath());
    }

    protected Header[] buildHeaders(Header[] pHeaders) {
        return FH.getDefaultParamsAsHeaders(pHeaders);
    }
}
