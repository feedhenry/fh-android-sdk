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
package com.feedhenry.sdk;

import org.json.fh.JSONArray;
import org.json.fh.JSONObject;

/**
 * Represents the response data from FeedHenry when an API call completes.
 */

public class FHResponse {

    private JSONObject mResults;
    private JSONArray mResultArray;
    private Throwable mError;
    private String mErrorMessage;

    public FHResponse(JSONObject pResults, JSONArray pResultArray, Throwable e, String pError) {
        mResults = pResults;
        mResultArray = pResultArray;
        mError = e;
        mErrorMessage = pError;
    }

    /**
     * Gets the response data as a JSONObject.
     *
     * @return a JSONObject
     */
    public JSONObject getJson() {
        return mResults;
    }

    /**
     * Gets the response data as a JSONArray.
     *
     * @return a JSONArray
     */
    public JSONArray getArray() {
        return mResultArray;
    }

    /**
     * Gets the error.
     *
     * @return the error
     */
    public Throwable getError() {
        return mError;
    }

    /**
     * Gets the error message.
     *
     * @return the error message
     */
    public String getErrorMessage() {
        return mErrorMessage;
    }

    /**
     * Gets the raw response content.
     *
     * @return the raw response content
     */
    public String getRawResponse() {
        if (mResults != null) {
            return mResults.toString();
        } else if (mResultArray != null) {
            return mResultArray.toString();
        } else {
            return mErrorMessage;
        }
    }
}
