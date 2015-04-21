/**
 * Copyright (c) 2015 FeedHenry Ltd, All Rights Reserved.
 *
 * Please refer to your contract with FeedHenry for the software license agreement.
 * If you do not have a contract, you do not have a license to use this software.
 */
package com.feedhenry.sdk;

import com.feedhenry.sdk.utils.DataManager;
import org.json.fh.JSONException;
import org.json.fh.JSONObject;

import com.feedhenry.sdk.utils.FHLog;

public class CloudProps {

    private JSONObject mCloudProps;
    private String mHostUrl;
    private String mEnv;
    private static String mInitValue;

    private static final String LOG_TAG = "com.feedhenry.sdk.CloudProps";
    public static final String HOSTS_KEY = "hosts";
    private static final String INIT_KEY = "init";

    private static String INVALID_URI_PATTERN = "(_.*?)\\.";

    private static CloudProps mInstance;

    private CloudProps(JSONObject pCloudProps) {
        mCloudProps = pCloudProps;
    }

    /**
     * Construct a cloudProps instance for local development. The cloud url will be the value of
     * "host" specified in
     * assets/fhconfig.local.properties file.
     * 
     */
    private CloudProps() {
        mCloudProps = new JSONObject();
        mCloudProps.put("url", AppProps.getInstance().getHost());
    }

    /**
     * Return the cloud host of the app
     * 
     * @return the cloud host (no trailing "/")
     */
    public String getCloudHost() {
        if (null == mHostUrl) {
            String hostUrl = null;
            try {
                if (mCloudProps.has("url")) {
                    hostUrl = mCloudProps.getString("url");
                } else {
                    String appMode = AppProps.getInstance().getAppMode();
                    JSONObject hosts = mCloudProps.getJSONObject("hosts");
                    if (hosts.has("url")) {
                        hostUrl = hosts.getString("url");
                    } else {
                        if ("dev".equalsIgnoreCase(appMode)) {
                            hostUrl = hosts.getString("debugCloudUrl");
                        } else {
                            hostUrl = hosts.getString("releaseCloudUrl");
                        }
                    }
                }
                hostUrl = hostUrl.endsWith("/") ? hostUrl.substring(0, hostUrl.length() - 1) : hostUrl;
                // previously cloud host url could look like this: testing-nge0bsskhnq2slb3b1luvbwr-dev_testing.df.dev.e111.feedhenry.net
                // however, "_" is not valid in JAVA as URI host, that will cause the parsed URI contains null host.
                // since dynofarm now accept urls like this: testing-nge0bsskhnq2slb3b1luvbwr-dev.df.dev.e111.feedhenry.net
                // we need to remove the "_" + dynomame part if it exists
                hostUrl = hostUrl.replaceFirst(INVALID_URI_PATTERN, ".");
                mHostUrl = hostUrl;
                FHLog.v(LOG_TAG, "host url = " + mHostUrl);
            } catch (Exception e) {
                FHLog.e(LOG_TAG, e.getMessage(), e);
            }
        }

        return mHostUrl;
    }

    /**
     * Get the environment of the cloud app
     * 
     * @return
     */
    public String getEnv() {
        if (null == mEnv) {
            JSONObject hosts = mCloudProps.getJSONObject("hosts");
            if (hosts.has("environment")) {
                mEnv = hosts.getString("environment");
            }
        }
        return mEnv;
    }

    /**
     * Save the details of the cloud app to the device
     */
    public void save() {
        if (null != mCloudProps) {
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
     * Get the instance of CloudProps for local development
     * 
     * @return
     */
    public static CloudProps initDev() {
        if (null == mInstance) {
            mInstance = new CloudProps();
        }
        return mInstance;
    }

    /**
     * Get the instance of the CloudProps via a JSONObject
     * 
     * @param pCloudProps the JSONObjct contains the details of the cloud app
     * @return
     */
    public static CloudProps init(JSONObject pCloudProps) {
        if (null == mInstance) {
            mInstance = new CloudProps(pCloudProps);
        }
        return mInstance;
    }

    /**
     * Get the instance of the CloudProps based on local cached data if exists.
     * 
     * @return
     */
    public static CloudProps load() {
        if (null == mInstance) {
            String saved = DataManager.getInstance().read(HOSTS_KEY);
            if (null != saved) {
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
     * Get the tracking data from the init data
     * 
     * @return
     */
    public static String getInitValue() {
        if (null == mInitValue) {
            mInitValue = DataManager.getInstance().read(INIT_KEY);
        }
        return mInitValue;
    }
}
