/**
 * Copyright (c) 2015 FeedHenry Ltd, All Rights Reserved.
 *
 * Please refer to your contract with FeedHenry for the software license agreement.
 * If you do not have a contract, you do not have a license to use this software.
 */
package com.feedhenry.sdk.api2;


import com.feedhenry.sdk.AppProps;
import com.feedhenry.sdk.FHActCallback;
import com.feedhenry.sdk2.FHHttpClient;
import com.feedhenry.sdk.FHRemote;
import com.feedhenry.sdk.FHResponse;
import com.feedhenry.sdk.api.FHAuthSession.Callback;
import com.feedhenry.sdk.utils.DataManager;
import com.feedhenry.sdk.utils.FHLog;
import com.feedhenry.sdk.utils.StringUtils;
import org.json.fh.JSONObject;

public class FHAuthSession {
    public static final String SESSION_TOKEN_KEY = "sessionToken";
    private static final String LOG_TAG = "com.feedhenry.sdk.api.FHAuthSession";

    private static final String VERIFY_SESSION_ENDPOINT = "admin/authpolicy/verifysession";
    private static final String REVOKE_SESSION_ENDPOINT = "admin/authpolicy/revokesession";

    private final DataManager mDataManager;
    private final FHHttpClient mHttpClient;
    
    /**
     * Boring constructor with no side effects.
     * 
     * @param pDataManager a DataManager instance to inject
     * @param pHttpClient a FHHttpClient to inject
     */
    public FHAuthSession(DataManager pDataManager, FHHttpClient pHttpClient) {
        mDataManager = pDataManager;
        mHttpClient = pHttpClient;
    }

    /**
     * Checks if a sessionToken value exists on the device.
     *
     * @return if the sessionToken exists
     */
    public boolean exists() {
        return mDataManager.read(SESSION_TOKEN_KEY) != null;
    }

    /**
     * Gets the value of the current session token.
     *
     * @return the current session token value
     */
    public String getToken() {
        return mDataManager.read(SESSION_TOKEN_KEY);
    }

    /**
     * Saves the seesionToken value on the device.
     *
     * @param sessionToken Session token
     */
    public void save(String sessionToken) {
        mDataManager.save(SESSION_TOKEN_KEY, sessionToken);
    }

    /**
     * Calls the remote server to check if the existing sessionToken is actually valid.
     *
     * @param pCallback a callback to be executed when remote call is completed
     * @param pSync     A flag to call it sync
     * @throws Exception An exception will be thrown when callRemote fail
     */
    public void verify(Callback pCallback, boolean pSync) throws Exception {
        String sessionToken = mDataManager.read(SESSION_TOKEN_KEY);
        if (sessionToken != null) {
            callRemote(VERIFY_SESSION_ENDPOINT, sessionToken, pCallback, pSync);
        }
    }

    /**
     * Removes the session token on the device and tries to remove it remotely as well.
     *
     * @param pSync     A flag to call it sync
     * @throws Exception An exception will be thrown when callRemote fail
     */
    public void clear(boolean pSync) throws Exception {
        String sessionToken = mDataManager.read(SESSION_TOKEN_KEY);
        if (sessionToken != null) {
            mDataManager.remove(SESSION_TOKEN_KEY);
            try {
                callRemote(REVOKE_SESSION_ENDPOINT, sessionToken, null, pSync);
            } catch (Exception e) {
                FHLog.w(LOG_TAG, e.getMessage());
            }
        }
    }

    private void callRemote(String pPath, String pSessionToken, final Callback pCallback, boolean pUseSync)
        throws Exception {
        String host = AppProps.getInstance().getHost();
        String url = StringUtils.removeTrailingSlash(host) + FHRemote.PATH_PREFIX + pPath;
        JSONObject params = new JSONObject().put(SESSION_TOKEN_KEY, pSessionToken);
        try {
            mHttpClient.post(
                url,
                null,
                params,
                new FHActCallback() {
                    @Override
                    public void success(FHResponse pResponse) {
                        JSONObject res = pResponse.getJson();
                        if (pCallback != null) {
                            pCallback.handleSuccess(res.getBoolean("isValid"));
                        }
                    }

                    @Override
                    public void fail(FHResponse pResponse) {
                        FHLog.w(LOG_TAG, pResponse.getRawResponse());
                        if (pCallback != null) {
                            pCallback.handleError(pResponse);
                        }
                    }
                },
                pUseSync);
        } catch (Exception e) {
            FHLog.e(LOG_TAG, e.getMessage(), e);
            throw e;
        }
    }

}
