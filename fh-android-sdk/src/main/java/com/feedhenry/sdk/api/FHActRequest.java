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
import com.feedhenry.sdk.FHRemote;
import cz.msebera.android.httpclient.Header;
import org.json.fh.JSONObject;

/**
 * The request for calling the cloud side function of the app. Example:
 * 
 * <pre>
 * {@code
 *   //calling a cloud side function called "getTweets" and pass in the keywords
 *   FHActRequest request = FH.buildActRequest("getTweets", new JSONObject().put("keyword",
 * "FeedHenry"));
 *   reqeust.executeAsync(new FHActCallback(){
 *     public void success(FHResponse pResp){
 *       JSONObject tweetsObj = pResp.getJson();
 *       ...
 *     }
 * 
 *     public void fail(FHResponse pResp){
 *       //process error data
 *       ...
 *     }
 *   });
 * }
 * </pre>
 */
public class FHActRequest extends FHRemote {

    private String mRemoteAct;
    protected JSONObject mArgs = new JSONObject();

    protected static String LOG_TAG = "com.feedhenry.sdk.api.FHActRequest";

    /**
     * Constructor
     * 
     * @param context the applicaiton context
     */
    public FHActRequest(Context context) {
        super(context);
    }

    protected String getApiURl() {
        String host = CloudProps.getInstance().getCloudHost();
        String path = getPath();
        String hostUrl = host + (path.startsWith("/") ? path : '/' + path);
        return hostUrl;
    }

    /**
     * The name of the cloud side function
     * 
     * @param pAction cloud side function name
     */
    public void setRemoteAction(String pAction) {
        mRemoteAct = pAction;
    }

    /**
     * Set the parameters for the cloud side function
     * 
     * @param pArgs the parameters that will be passed to the cloud side function
     */
    public void setArgs(JSONObject pArgs) {
        mArgs = pArgs;
        // keep backward compatibility
        if (!mArgs.has("__fh")) {
            try {
                mArgs.put("__fh", FH.getDefaultParams());
            } catch (Exception e) {

            }
        }
    }

    protected JSONObject getRequestArgs() {
        return mArgs;
    }

    @Override
    protected String getPath() {
        return "cloud/" + mRemoteAct;
    }

    @Override
    protected Header[] buildHeaders(Header[] pHeaders) throws Exception {
        return FH.getDefaultParamsAsHeaders(pHeaders);
    }
}
