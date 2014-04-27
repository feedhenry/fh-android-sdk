package com.feedhenry.sdk;

import java.util.Properties;

import org.json.fh.JSONObject;

import com.feedhenry.sdk.utils.FHLog;

public class CloudProps {

  private Properties mProperties;
  private JSONObject mCloudProps;
  
  private static final String LOG_TAG = "com.feedhenry.sdk.CloudProps";
  
  public CloudProps(Properties mAppProps, JSONObject pCloudProps){
    mCloudProps = pCloudProps;
  }
  
  /**
   * Return the cloud host of the app
   * @return the cloud host (no trailing "/")
   */
  public String getCloudHost(){
    String hostUrl = null;
    String appMode = mProperties.getProperty(FH.APP_MODE_KEY);
    try {
      if (mCloudProps.has("url")) {
        hostUrl = mCloudProps.getString("url");
      } else {
        JSONObject hosts = mCloudProps.getJSONObject("hosts");
        if ("dev".equalsIgnoreCase(appMode)) {
          hostUrl = hosts.getString("debugCloudUrl");
        } else {
          hostUrl = hosts.getString("releaseCloudUrl");
        }
      }
      hostUrl = hostUrl.endsWith("/") ? hostUrl.substring(0,hostUrl.length() - 1) : hostUrl;
      FHLog.v(LOG_TAG, "host url = " + hostUrl);
    } catch (Exception e) {
      FHLog.e(LOG_TAG, e.getMessage(), e);
    }
    return hostUrl;
  }
  
  public Properties getAppProperties(){
    return mProperties;
  }
}
