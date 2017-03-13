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
    private static com.feedhenry.sdk.api2.FHAuthSession instance;

    
    
    private FHAuthSession() {

    }

    /**
     * Checks if a sessionToken value exists on the device.
     *
     * @return if the sessionToken exists
     * @deprecated please use com.feedhenry.sdk.api2.FHAuthSession.exists instead
     */
    @Deprecated
    public static boolean exists() {
        return getInstance(DataManager.getInstance()).exists();
    }

    /**
     * Gets the value of the current session token.
     *
     * @return the current session token value
     * @deprecated please use com.feedhenry.sdk.api2.FHAuthSession.getToken instead
     */
    @Deprecated
    public static String getToken() {
        return getInstance(DataManager.getInstance()).getToken();
    }

    /**
     * Saves the seesionToken value on the device.
     *
     * @param sessionToken Session token
     * @deprecated please use com.feedhenry.sdk.api2.FHAuthSession.save instead
     */
    @Deprecated
    protected static void save(String sessionToken) {
        getInstance(DataManager.getInstance()).save(sessionToken);
    }

    /**
     * Calls the remote server to check if the existing sessionToken is actually valid.
     *
     * @param pCallback a callback to be executed when remote call is completed
     * @param pSync     A flag to call it sync
     * 
     * @deprecated please use com.feedhenry.sdk.api2.FHAuthSession.verify instead
     */
    @Deprecated
    public static void verify(Callback pCallback, boolean pSync) {
        getInstance(DataManager.getInstance()).verify(pCallback, pSync);
    }

    /**
     * Removes the session token on the device and tries to remove it remotely as well.
     *
     * @param pSync     A flag to call it sync
     * 
     * @deprecated please use com.feedhenry.sdk.api2.FHAuthSession.clear instead 
     */
    @Deprecated
    public static void clear(boolean pSync) {
        getInstance(DataManager.getInstance()).clear(pSync);
    }

    private static synchronized com.feedhenry.sdk.api2.FHAuthSession getInstance(DataManager dataManager) {
        if (instance == null) {
            instance = new com.feedhenry.sdk.api2.FHAuthSession(dataManager, new com.feedhenry.sdk2.FHHttpClient());
        }
        return instance;
    }
    
    public interface Callback {

        void handleSuccess(boolean isValid);

        void handleError(FHResponse pRes);
    }
}
