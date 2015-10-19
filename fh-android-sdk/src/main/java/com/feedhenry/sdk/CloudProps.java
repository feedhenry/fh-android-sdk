/**
 * Copyright (c) 2015 FeedHenry Ltd, All Rights Reserved.
 *
 * Please refer to your contract with FeedHenry for the software license agreement.
 * If you do not have a contract, you do not have a license to use this software.
 */
package com.feedhenry.sdk;

import com.feedhenry.sdk.utils.DataManager;
import com.feedhenry.sdk.utils.FHLog;
import com.feedhenry.sdk.utils.StringUtils;
import java.util.regex.Pattern;
import org.json.fh.JSONException;
import org.json.fh.JSONObject;

public class CloudProps {

    private static final Pattern INVALID_URI_SEGMENT = Pattern.compile("(_.*?)\\.");
    private JSONObject mCloudProps;
    private String mHostUrl;
    private String mEnv;
    private static String mInitValue;

    private static final String LOG_TAG = "com.feedhenry.sdk.CloudProps";
    private static final String HOSTS_KEY = "hosts";
    private static final String INIT_KEY = "init";

    private static CloudProps mInstance;

    private CloudProps(JSONObject pCloudProps) {
        mCloudProps = pCloudProps;
    }

    /**
     * Construct a cloudProps instance for local development. The cloud url will be the value of
     * "host" specified in assets/fhconfig.local.properties file.
     */
    private CloudProps() {
        mCloudProps = new JSONObject();
        mCloudProps.put("url", AppProps.getInstance().getHost());
    }

    /**
     * Return the cloud host of the app.
     *
     * @return the cloud host with no trailing "/"
     */
    public String getCloudHost() {
        if (mHostUrl == null) {
            String hostUri = null;
            try {
                if (mCloudProps.has("url")) {
                    hostUri = mCloudProps.getString("url");
                } else if (mCloudProps.has("hosts")) {
                    JSONObject hosts = mCloudProps.getJSONObject("hosts");
                    if (hosts.has("url")) {
                        hostUri = hosts.getString("url");
                    } else {
                        String appMode = AppProps.getInstance().getAppMode();
                        String key = "dev".equalsIgnoreCase(appMode) ? "debugCloudUrl" : "releaseCloudUrl";
                        hostUri = hosts.getString(key);
                    }
                }
                if (hostUri == null) {
                    throw new Exception("Could not get cloud host URL");
                }

                hostUri = StringUtils.removeTrailingSlash(hostUri);
                /*
                Previously, the cloud host URI could look like this:
                testing-nge0bsskhnq2slb3b1luvbwr-dev_testing.df.dev.e111.feedhenry.net
                However, "_" is not a valid character in a Java URI. This will cause getHost() to return null.
                Since dynofarm now accepts URIs like this:
                testing-nge0bsskhnq2slb3b1luvbwr-dev.df.dev.e111.feedhenry.net
                We need to remove the "_" + environment part if it exists.
                */
                hostUri = INVALID_URI_SEGMENT.matcher(hostUri).replaceFirst(".");
                mHostUrl = hostUri;
                FHLog.v(LOG_TAG, "host url = " + mHostUrl);
            } catch (Exception e) {
                FHLog.e(LOG_TAG, e.getMessage(), e);
            }
        }

        return mHostUrl;
    }

    /**
     * Gets the environment of the cloud app.
     *
     * @return The environment of the cloud app
     */
    public String getEnv() {
        if (mEnv == null) {
            JSONObject hosts = mCloudProps.getJSONObject("hosts");
            if (hosts.has("environment")) {
                mEnv = hosts.getString("environment");
            }
        }
        return mEnv;
    }

    /**
     * Saves the details of the cloud app to the device.
     */
    public void save() {
        if (mCloudProps != null) {
            DataManager.getInstance().save(HOSTS_KEY, mCloudProps.toString());
            // Save init
            if (mCloudProps.has(INIT_KEY)) {
                try {
                    mInitValue = mCloudProps.getString(INIT_KEY);
                    DataManager.getInstance().save(INIT_KEY, mInitValue);
                } catch (JSONException e) {
                    FHLog.w(LOG_TAG, e.getMessage());
                }
            }
        }
    }

    /**
     * Gets the instance of CloudProps for local development.
     *
     * @return The instance of CloudProps for local development
     */
    public static CloudProps initDev() {
        if (mInstance == null) {
            mInstance = new CloudProps();
        }
        return mInstance;
    }

    /**
     * Gets the instance of CloudProps via a JSONObject.
     *
     * @param pCloudProps the JSONObjct contains the details of the cloud app
     * @return CloudProps
     */
    public static CloudProps init(JSONObject pCloudProps) {
        if (mInstance == null) {
            mInstance = new CloudProps(pCloudProps);
        }
        return mInstance;
    }

    /**
     * Gets the instance of the CloudProps based on local cached data if exists.
     *
     * @return CloudProps
     */
    public static CloudProps load() {
        if (mInstance == null) {
            String saved = DataManager.getInstance().read(HOSTS_KEY);
            if (saved != null) {
                try {
                    JSONObject parsed = new JSONObject(saved);
                    mInstance = new CloudProps(parsed);
                } catch (Exception e) {
                    mInstance = null;
                    FHLog.w(LOG_TAG, e.getMessage());
                }
            }
        }
        return mInstance;
    }

    public static CloudProps getInstance() {
        return mInstance;
    }

    /**
     * Gets tracking data from init data.
     *
     * @return the tracking data
     */
    public static String getInitValue() {
        if (mInitValue == null) {
            mInitValue = DataManager.getInstance().read(INIT_KEY);
        }
        return mInitValue;
    }
}
