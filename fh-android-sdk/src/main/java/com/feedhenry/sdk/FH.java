/**
 * Copyright (c) 2015 FeedHenry Ltd, All Rights Reserved.
 *
 * Please refer to your contract with FeedHenry for the software license agreement.
 * If you do not have a contract, you do not have a license to use this software.
 */
package com.feedhenry.sdk;

import android.content.Context;
import com.feedhenry.sdk.api.FHActRequest;
import com.feedhenry.sdk.api.FHAuthRequest;
import com.feedhenry.sdk.api.FHAuthSession;
import com.feedhenry.sdk.api.FHCloudRequest;
import com.feedhenry.sdk.api.FHCloudRequest.Methods;
import com.feedhenry.sdk.api.FHInitializeRequest;
import com.feedhenry.sdk.exceptions.FHNotReadyException;
import com.feedhenry.sdk.utils.DataManager;
import com.feedhenry.sdk.utils.FHLog;
import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.message.BasicHeader;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import org.jboss.aerogear.android.core.Callback;
import org.jboss.aerogear.android.unifiedpush.RegistrarManager;
import org.jboss.aerogear.android.unifiedpush.gcm.AeroGearGCMPushConfiguration;
import org.jboss.aerogear.android.unifiedpush.gcm.AeroGearGCMPushRegistrar;
import org.jboss.aerogear.android.unifiedpush.metrics.UnifiedPushMetricsMessage;
import org.json.fh.JSONObject;

/**
 * The FH class provides static methods to initialize the library, create new instances of all the
 * API request objects, and configure global settings.
 */
public class FH {

    private static boolean mReady = false;

    private static final String LOG_TAG = "com.feedhenry.sdk.FH";

    private static final String FH_API_ACT = "act";
    private static final String FH_API_AUTH = "auth";
    private static final String FH_API_CLOUD = "cloud";

    private static final String FH_PUSH_NAME = "FH";

    public static final int LOG_LEVEL_VERBOSE = 1;
    public static final int LOG_LEVEL_DEBUG = 2;
    public static final int LOG_LEVEL_INFO = 3;
    public static final int LOG_LEVEL_WARNING = 4;
    public static final int LOG_LEVEL_ERROR = 5;
    public static final int LOG_LEVEL_NONE = Integer.MAX_VALUE;

    private static int mLogLevel = LOG_LEVEL_ERROR;

    public static final String VERSION = "2.1.0"; // DO NOT CHANGE, the ant build task will automatically update this value. Update it in VERSION.txt

    private static boolean mInitCalled = false;

    private static Context mContext;

    private FH() throws Exception {
        throw new Exception("Not Supported");
    }

    /**
     * Initializes the application.
     * This must be called before the application can use the FH library.
     * The initialization process happens in a background thread so that the UI thread won't be blocked.
     * If you need to call other FH API methods, you need to make sure they are called after the init
     * finishes. The best way to do it is to provide a FHActCallback instance and implement the success method.
     * The callback functions are invoked on the main UI thread.
     * For example, in your main activity class's onCreate method, you can do this:
     *
     * <pre>
     * {@code
     *  FH.init(this, new FHActCallback() {
     *    public void success(FHResponse pRes) {
     *      //pRes will be null for init call if it succeeds, don't use it to access response data
     *      FHActRequest request = FH.buildActRequest("readData", new JSONObject());
     *      request.executeAsync(new FHActCallback(){
     *        public void success(FHResponse pResp){
     *          //process response data
     *        }
     *
     *        public void fail(FHResponse pResp){
     *          //process error data
     *        }
     *      })
     *    }
     *
     *    public void fail(FHResponse pRes) {
     *      Log.e("FHInit", pRes.getErrorMessage(), pRes.getError());
     *    }
     *  });
     * }
     * </pre>
     *
     * @param pContext  your application's context
     * @param pCallback the callback function to be executed after initialization is finished
     */
    public static void init(Context pContext, FHActCallback pCallback) {
        mContext = pContext;
        if (!mInitCalled) {
            DataManager.init(mContext).migrateLegacyData();
            checkNetworkStatus();
            try {
                AppProps.load(mContext);
            } catch (IOException e) {
                mReady = false;
                FHLog.e(LOG_TAG, "Can not load property file.", e);
            }
            mInitCalled = true;
        }
        if (mReady) {
            if (pCallback != null) {
                pCallback.success(null);
            }
            return;
        }

        final FHActCallback cb = pCallback;
        if (AppProps.getInstance().isLocalDevelopment()) {
            FHLog.i(
                LOG_TAG,
                "Local development mode enabled, loading properties from assets/fhconfig.local.properties file");
            CloudProps.initDev();
            mReady = true;
            if (cb != null) {
                cb.success(null);
            }
            return;
        }
        FHInitializeRequest initRequest = new FHInitializeRequest(mContext);
        try {
            initRequest.executeAsync(
                new FHActCallback() {
                    @Override
                    public void success(FHResponse pResponse) {
                        mReady = true;
                        FHLog.v(LOG_TAG, "FH init response = " + pResponse.getJson().toString());
                        JSONObject cloudProps = pResponse.getJson();
                        CloudProps.init(cloudProps);
                        CloudProps.getInstance().save();
                        if (cb != null) {
                            cb.success(null);
                        }
                    }

                    @Override
                    public void fail(FHResponse pResponse) {
                        FHLog.e(
                            LOG_TAG,
                            "FH init failed with error = " + pResponse.getErrorMessage(),
                            pResponse.getError());
                        CloudProps props = CloudProps.load();
                        if (props != null) {
                            mReady = true;
                            FHLog.i(LOG_TAG, "Cached CloudProps data found");
                            if (cb != null) {
                                cb.success(null);
                            }
                        } else {
                            mReady = false;
                            FHLog.i(LOG_TAG, "No cache data found for CloudProps");
                            if (cb != null) {
                                cb.fail(pResponse);
                            }
                        }
                    }
                });
        } catch (Exception e) {
            FHLog.e(LOG_TAG, "FH init exception = " + e.getMessage(), e);
        }
    }

    private static void checkNetworkStatus() {
        NetworkManager networkManager = NetworkManager.init(mContext);
        networkManager.registerNetworkListener();
        networkManager.checkNetworkStatus();
        if (networkManager.isOnline() && !mReady && mInitCalled) {
            init(mContext, null);
        }
    }

    private static FHAct buildAction(String pAction) throws FHNotReadyException {
        if (!mInitCalled) {
            throw new FHNotReadyException();
        }
        FHAct action = null;
        if (FH_API_ACT.equalsIgnoreCase(pAction)) {
            action = new FHActRequest(mContext);
        } else if (FH_API_AUTH.equalsIgnoreCase(pAction)) {
            action = new FHAuthRequest(mContext);
        } else if (FH_API_CLOUD.equalsIgnoreCase(pAction)) {
            action = new FHCloudRequest(mContext);
        } else {
            FHLog.w(LOG_TAG, "Invalid action : " + pAction);
        }
        return action;
    }

    public static boolean isOnline() {
        return NetworkManager.getInstance().isOnline();
    }

    public static void stop() {
        NetworkManager.getInstance().unregisterNetworkListener();
    }

    /**
     * Checks if FH is ready.
     *
     * @return whether FH has finished initialization
     */
    public static boolean isReady() {
        return mReady;
    }

    @Deprecated
    /**
     * Builds an instance of {@link FHActRequest} to perform an act request.
     * @param pRemoteAction the name of the cloud side function
     * @param pParams the parameters for the cloud side function
     * @return an instance of FHActRequest
     * @throws FHNotReadyException
     */
    public static FHActRequest buildActRequest(String pRemoteAction, JSONObject pParams) throws FHNotReadyException {
        FHActRequest request = (FHActRequest) buildAction(FH_API_ACT);
        request.setRemoteAction(pRemoteAction);
        request.setArgs(pParams);
        return request;
    }

    /**
     * Builds an instance of FHAuthRequest object to perform an authentication request.
     *
     * @return an instance of FHAuthRequest
     * @throws FHNotReadyException if init has not been called
     */
    public static FHAuthRequest buildAuthRequest() throws FHNotReadyException {
        return (FHAuthRequest) buildAction(FH_API_AUTH);
    }

    /**
     * Builds an instance of FHAuthRequest object to perform an authentication request with an auth policy id set.
     *
     * @param pPolicyId the auth policy id used by this auth request
     * @return an instance of FHAuthRequest
     * @throws FHNotReadyException if init has not been called
     */
    public static FHAuthRequest buildAuthRequest(String pPolicyId) throws FHNotReadyException {
        FHAuthRequest request = (FHAuthRequest) buildAction(FH_API_AUTH);
        request.setAuthPolicyId(pPolicyId);
        return request;
    }

    /**
     * Builds an instance of FHAuthRequest to perform an authentication request with an auth policy id,
     * user name, and password set.
     *
     * @param pPolicyId the auth policy id used by this auth request
     * @param pUserName the required user name for the auth request
     * @param pPassword the required password for the auth request
     * @return an instance of FHAuthRequest
     * @throws FHNotReadyException if init has not been called
     */
    public static FHAuthRequest buildAuthRequest(String pPolicyId, String pUserName, String pPassword)
        throws FHNotReadyException {
        FHAuthRequest request = (FHAuthRequest) buildAction(FH_API_AUTH);
        request.setAuthUser(pPolicyId, pUserName, pPassword);
        return request;
    }

    /**
     * Builds an instance of FHCloudRequest to call cloud APIs.
     *
     * @param pPath    the path of the cloud API
     * @param pMethod  currently supports GET, POST, PUT and DELETE
     * @param pHeaders headers need to be set, can be null
     * @param pParams  the request params, can be null
     * @return an instance of FHCloudRequest
     * @throws FHNotReadyException if init has not been called
     * @throws Exception           if pMethod is not one of GET, POST, PUT and DELETE
     */
    public static FHCloudRequest buildCloudRequest(String pPath, String pMethod, Header[] pHeaders, JSONObject pParams)
        throws Exception {
        FHCloudRequest request = (FHCloudRequest) buildAction(FH_API_CLOUD);
        request.setPath(pPath);
        request.setHeaders(pHeaders);
        request.setMethod(Methods.parse(pMethod));
        request.setRequestArgs(pParams);
        return request;
    }

    /**
     * Gets the cloud host.
     *
     * @return the cloud host of the app
     * @throws FHNotReadyException if init has not been called
     */
    public static String getCloudHost() throws FHNotReadyException {
        if (CloudProps.getInstance() == null) {
            throw new FHNotReadyException();
        }
        return CloudProps.getInstance().getCloudHost();
    }

    /**
     * Gets the default params for customised HTTP Requests.
     * These params will be required to enable app analytics on the FH platform.
     * You can either add the params to your request body as a JSONObject with the key "__fh", or use the
     * {@link #getDefaultParamsAsHeaders(Header[]) getDefaultParamsAsHeaders} method to add them as HTTP request
     * headers.
     *
     * @return a JSONObject contains the default params
     * @throws Exception if the app property file is not loaded
     */
    public static JSONObject getDefaultParams() throws Exception {
        AppProps appProps = AppProps.getInstance();
        JSONObject defaultParams = new JSONObject();
        defaultParams.put("appid", appProps.getAppId());
        defaultParams.put("appkey", appProps.getAppApiKey());
        defaultParams.put("cuid", Device.getDeviceId(mContext));
        defaultParams.put("destination", "android");
        defaultParams.put("sdk_version", "FH_ANDROID_SDK/" + FH.VERSION);
        String projectId = appProps.getProjectId();
        if (projectId != null && !projectId.isEmpty()) {
            defaultParams.put("projectid", projectId);
        }
        String connectionTag = appProps.getConnectionTag();
        if (connectionTag != null && !connectionTag.isEmpty()) {
            defaultParams.put("connectiontag", connectionTag);
        }
        // Load init
        String init = CloudProps.getInitValue();
        if (init != null) {
            JSONObject initObj = new JSONObject(init);
            defaultParams.put("init", initObj);
        }

        if (FHAuthSession.exists()) {
            defaultParams.put(FHAuthSession.SESSION_TOKEN_KEY, FHAuthSession.getToken());
        }

        return defaultParams;
    }

    /**
     * Similar to {@link #getDefaultParams() getDefaultParams}, but returns HTTP headers instead.
     *
     * @param pHeaders existing headers
     * @return new headers by combining existing headers and default headers
     * @throws Exception if the app property file is not loaded
     */
    public static Header[] getDefaultParamsAsHeaders(Header[] pHeaders) throws Exception {
        JSONObject defaultParams = FH.getDefaultParams();
        ArrayList<Header> headers = new ArrayList<Header>(defaultParams.length());

        for (Iterator<String> it = defaultParams.keys(); it.hasNext(); ) {
            String key = it.next();
            headers.add(new BasicHeader("X-FH-" + key, defaultParams.getString(key)));
        }
        if (pHeaders != null) {
            headers.ensureCapacity(pHeaders.length + 1);
            Collections.addAll(headers, pHeaders);
        }
        return headers.toArray(new Header[headers.size()]);
    }

    /**
     * Calls cloud APIs asynchronously.
     *
     * @param pPath     the path to the cloud API
     * @param pMethod   currently supports GET, POST, PUT and DELETE
     * @param pHeaders  headers need to be set, can be null
     * @param pParams   the request params, can be null. Will be converted to query strings depending
     *                  on the HTTP method
     * @param pCallback the callback to be executed when the cloud call is finished
     * @throws FHNotReadyException if init has not been called
     * @throws Exception           if pMethod is not one of GET, POST, PUT and DELETE OR if the cloud request
     *                             fails
     */
    public static void cloud(
        String pPath,
        String pMethod,
        Header[] pHeaders,
        JSONObject pParams,
        FHActCallback pCallback)
        throws Exception {
        FHCloudRequest cloudRequest = buildCloudRequest(pPath, pMethod, pHeaders, pParams);
        cloudRequest.executeAsync(pCallback);
    }

    /**
     * Sets the log level for the library.
     * The default level is {@link #LOG_LEVEL_ERROR}. Please make sure this is set to {@link #LOG_LEVEL_ERROR}
     * or {@link #LOG_LEVEL_NONE} before releasing the application.
     * The log level can be one of
     * <ul>
     * <li>{@link #LOG_LEVEL_VERBOSE}</li>
     * <li>{@link #LOG_LEVEL_DEBUG}</li>
     * <li>{@link #LOG_LEVEL_INFO}</li>
     * <li>{@link #LOG_LEVEL_WARNING}</li>
     * <li>{@link #LOG_LEVEL_ERROR}</li>
     * <li>{@link #LOG_LEVEL_NONE}</li>
     * </ul>
     *
     * @param pLogLevel The level of logging for the FH library
     */
    public static void setLogLevel(int pLogLevel) {
        mLogLevel = pLogLevel;
    }

    /**
     * Gets the current log level for the FH library.
     *
     * @return The current log level
     */
    public static int getLogLevel() {
        return mLogLevel;
    }

    /**
     * Gets the customized user-agent string for the SDK.
     *
     * @return customized user-agent string
     */
    public static String getUserAgent() {
        return Device.getUserAgent();
    }

    /**
     * Registers a device on a push network.
     * The push information will be loaded from fhconfig.properties.
     *
     * This method need to be called <b>after</b> an {@link #init(android.content.Context, FHActCallback)} success
     * callback.
     *
     * @param pCallback the pCallback function to be executed after the device registration is finished
     */
    public static void pushRegister(FHActCallback pCallback) {
        pushRegister(new PushConfig(), pCallback);
    }

    /**
     * Registers a device on a push network.
     * The push information will be loaded from fhconfig.properties.
     *
     * This method need to be called <b>after</b> an {@link #init(android.content.Context, FHActCallback)} success
     * callback.
     *
     * @param pPushConfig extra configuration for push
     * @param pCallback   the pCallback function to be executed after the device registration is finished
     */
    public static void pushRegister(final PushConfig pPushConfig, final FHActCallback pCallback) {
        RegistrarManager.config(FH_PUSH_NAME, AeroGearGCMPushConfiguration.class)
            .setPushServerURI(URI.create(AppProps.getInstance().getPushServerUrl()))
            .setSenderIds(AppProps.getInstance().getPushSenderId())
            .setVariantID(AppProps.getInstance().getPushVariant())
            .setSecret(AppProps.getInstance().getPushSecret())
            .setAlias(pPushConfig.getAlias())
            .setCategories(pPushConfig.getCategories())
            .asRegistrar()
            .register(
                mContext, new Callback<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        pCallback.success(new FHResponse(null, null, null, null));
                    }

                    @Override
                    public void onFailure(Exception e) {
                        pCallback.fail(new FHResponse(null, null, e, e.getMessage()));
                    }
                });
    }

    /**
     * Sends confirmation a message was opened.
     *
     * @param pMessageId Id of the message received
     * @param pCallback  the pCallback function to be executed after the metrics sent
     */
    public static void sendPushMetrics(String pMessageId, final FHActCallback pCallback) {
        AeroGearGCMPushRegistrar registrar = (AeroGearGCMPushRegistrar) RegistrarManager.getRegistrar(FH_PUSH_NAME);
        registrar.sendMetrics(
            new UnifiedPushMetricsMessage(pMessageId), new Callback<UnifiedPushMetricsMessage>() {
                @Override
                public void onSuccess(UnifiedPushMetricsMessage data) {
                    pCallback.success(new FHResponse(null, null, null, null));
                }

                @Override
                public void onFailure(Exception e) {
                    pCallback.fail(new FHResponse(null, null, e, e.getMessage()));
                }
            });
    }
}
