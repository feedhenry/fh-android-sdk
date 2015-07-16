/**
 * Copyright (c) 2015 FeedHenry Ltd, All Rights Reserved.
 *
 * Please refer to your contract with FeedHenry for the software license agreement.
 * If you do not have a contract, you do not have a license to use this software.
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
