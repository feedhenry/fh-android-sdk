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

import com.feedhenry.sdk.AppProps;
import com.feedhenry.sdk.FHActCallback;
import com.feedhenry.sdk.FHHttpClient;
import com.feedhenry.sdk.FHRemote;
import com.feedhenry.sdk.FHResponse;
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
     * Calls the remote server to check if the existing sessionToken is actually
     * valid.
     *
     * @param pCallback a callback to be executed when remote call is completed
     * @param pSync A flag to call it sync
     *
     */
    public void verify(Callback pCallback, boolean pSync) {
        String sessionToken = mDataManager.read(SESSION_TOKEN_KEY);
        if (sessionToken != null) {
            callRemote(VERIFY_SESSION_ENDPOINT, sessionToken, pCallback, pSync);
        }
    }

    /**
     * Removes the session token on the device and tries to remove it remotely
     * as well.
     *
     * @param pSync A flag to call it sync
     *
     */
    public void clear(boolean pSync) {
        String sessionToken = mDataManager.read(SESSION_TOKEN_KEY);
        if (sessionToken != null) {
            mDataManager.remove(SESSION_TOKEN_KEY);
            callRemote(REVOKE_SESSION_ENDPOINT, sessionToken, null, pSync);
        }
    }

    private void callRemote(String pPath, String pSessionToken, final Callback pCallback, boolean pUseSync) {
        String host = AppProps.getInstance().getHost();
        String url = StringUtils.removeTrailingSlash(host) + FHRemote.PATH_PREFIX + pPath;
        JSONObject params = new JSONObject().put(SESSION_TOKEN_KEY, pSessionToken);

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
        });
    }

    public interface Callback {

        void handleSuccess(boolean isValid);

        void handleError(FHResponse pRes);
    }

}
