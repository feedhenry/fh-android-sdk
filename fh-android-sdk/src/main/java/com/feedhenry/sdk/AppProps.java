/**
 * Copyright (c) 2015 FeedHenry Ltd, All Rights Reserved.
 *
 * Please refer to your contract with FeedHenry for the software license agreement.
 * If you do not have a contract, you do not have a license to use this software.
 */
package com.feedhenry.sdk;

import android.content.Context;
import com.feedhenry.sdk.utils.FHLog;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Class represents the settings in fh.properties
 */
public class AppProps {

    private static final String APP_HOST_KEY = "host";
    private static final String APP_PROJECT_KEY = "projectid";
    private static final String APP_CONNECTION_TAG_KEY = "connectiontag";
    private static final String APP_ID_KEY = "appid";
    private static final String APP_APIKEY_KEY = "appkey";
    private static final String APP_MODE_KEY = "mode";

    // UnifiedPush properties
    private static final String PUSH_SERVER_URL = "PUSH_SERVER_URL";
    private static final String PUSH_SENDER_ID = "PUSH_SENDER_ID";
    private static final String PUSH_VARIANT = "PUSH_VARIANT";
    private static final String PUSH_SECRET = "PUSH_SECRET";

    private static final String OLD_PROPERTY_FILE = "fh.properties";
    private static final String NEW_PROPERTY_FILE = "fhconfig.properties";
    private static final String DEBUG_PROPERTY_FILE = "fhconfig.local.properties";

    private static final String LOG_TAG = "com.feedhenry.sdk.AppProps";

    private Properties mProps;
    private boolean isLocalDev;

    private static AppProps mInstance;

    private AppProps(Properties props, boolean isLocalDev) {
        this.mProps = props;
        this.isLocalDev = isLocalDev;
    }

    /**
     * Gets the value of "host" in the fh.properties file.
     *
     * @return the host value
     */
    public String getHost() {
        return this.mProps.getProperty(APP_HOST_KEY);
    }

    /**
     * Gets the value of "projectid" in the fh.properties file.
     *
     * @return the project id
     */
    public String getProjectId() {
        return this.mProps.getProperty(APP_PROJECT_KEY);
    }

    /**
     * Gets the value of "appid" in the fh.properties file.
     *
     * @return the app id
     */
    public String getAppId() {
        return this.mProps.getProperty(APP_ID_KEY);
    }

    /**
     * Gets the value of "appkey" in the fh.properties file.
     *
     * @return the app API key
     */
    public String getAppApiKey() {
        return this.mProps.getProperty(APP_APIKEY_KEY);
    }

    /**
     * Gets the value of "connectiontag" in the fh.properties file.
     *
     * @return the connection tag
     */
    public String getConnectionTag() {
        return this.mProps.getProperty(APP_CONNECTION_TAG_KEY);
    }

    @Deprecated
    /**
     * Gets the value of "mode" in the fh.properties file.
     * This is a legacy field and should not be used anymore.
     * @return the legacy app mode. Can be null.
     */
    public String getAppMode() {
        return this.mProps.getProperty(APP_MODE_KEY);
    }

    /**
     * Return if the app is running in local dev mode
     * (i.e., if fhconfig.local.properties file is found in the assets directory).
     * If true, the cloud host value returned in CloudProps will be the host value set in the property file.
     *
     * @return if the app is running in local dev mode
     */
    public boolean isLocalDevelopment() {
        return this.isLocalDev;
    }

    /**
     * Gets the value of the UnifiedPush server URL in the fhconfig.properties file.
     *
     * @return UnifiedPush server URL
     */
    public String getPushServerUrl() {
        return this.mProps.getProperty(PUSH_SERVER_URL);
    }

    /**
     * Gets the value of the Sender ID in the fhconfig.properties file.
     *
     * @return Sender ID
     */
    public String getPushSenderId() {
        return this.mProps.getProperty(PUSH_SENDER_ID);
    }

    /**
     * Gets the value of the UnifiedPush variant in the fhconfig.properties file.
     *
     * @return UnifiedPush variant
     */
    public String getPushVariant() {
        return this.mProps.getProperty(PUSH_VARIANT);
    }

    /**
     * Gets the value of the variant secret in the fhconfig.properties file.
     *
     * @return Variant secret
     */
    public String getPushSecret() {
        return this.mProps.getProperty(PUSH_SECRET);
    }

    /**
     * A method to retrieve the singleton instance of AppProps.
     *
     * @return The singleton instance of AppProps
     */
    public static AppProps getInstance() {
        if (mInstance == null) {
            throw new RuntimeException("AppProps is not initialised");
        }
        return mInstance;
    }

    /**
     * Loads the fh.properties file.
     *
     * @param context Application context
     * @return the AppProps after read the properties file
     * @throws IOException thrown if an error occurred while reading the properties file
     */
    public static AppProps load(Context context) throws IOException {
        if (mInstance == null) {
            InputStream in;
            boolean isLocalDev;
            Properties props = new Properties();

            // support local development
            in = getAssetInputStream(context, DEBUG_PROPERTY_FILE);
            isLocalDev = in != null;
            if (!isLocalDev) { //in == null
                in = getAssetInputStream(context, NEW_PROPERTY_FILE);
                if (in == null) {
                    in = getAssetInputStream(context, OLD_PROPERTY_FILE);
                }
            }

            if (in == null) {
                FHLog.e(LOG_TAG, "Can not load property file.", null);
            } else {
                try {
                    props.load(in);
                } catch (IOException e) {
                    FHLog.e(LOG_TAG, "Can not load property file.", e);
                }
            }

            if (in != null) {
                try {
                    in.close();
                } catch (IOException ex) {
                    FHLog.e(LOG_TAG, "Failed to close stream", ex);
                }
            }

            mInstance = new AppProps(props, isLocalDev);
        }
        return mInstance;
    }

    /**
     * Attempts to open an asset and return an InputStream associated with it.
     *
     * @return an InputStream if opening the asset was successful, null otherwise.
     */
    private static InputStream getAssetInputStream(Context context, String path) {
        try {
            return context.getAssets().open(path);
        } catch (IOException e) {
            FHLog.e(LOG_TAG, "Could not find asset " + path, e);
            return null;
        }
    }
}
