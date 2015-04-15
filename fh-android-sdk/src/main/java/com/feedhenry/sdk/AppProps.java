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

  public static final String APP_HOST_KEY = "host";
  public static final String APP_PROJECT_KEY = "projectid";
  public static final String APP_CONNECTION_TAG_KEY = "connectiontag";
  public static final String APP_ID_KEY = "appid";
  public static final String APP_APIKEY_KEY = "appkey";
  public static final String APP_MODE_KEY = "mode";

  private static final String OLD_PROPERTY_FILE = "fh.properties";
  private static final String NEW_PROPERTY_FILE = "fhconfig.properties";
  private static final String DEBUG_PROPERTY_FILE = "fhconfig.local.properties";

  private static final String LOG_TAG = "com.feedhenry.sdk.AppProps";

  private Properties mProps;
  private boolean isLocalDev;

  private static AppProps mInstance;

  private AppProps(Properties props, boolean isLocalDev){
    this.mProps = props;
    this.isLocalDev = isLocalDev;
  }

  /**
   * Get the host value in fh.properties
   * @return the host value
   */
  public String getHost(){
    return this.mProps.getProperty(APP_HOST_KEY);
  }

  /**
   * Get the project id
   * @return the project id
   */
  public String getProjectId(){
    return this.mProps.getProperty(APP_PROJECT_KEY);
  }

  /**
   * Get the app id
   * @return the app id
   */
  public String getAppId(){
    return this.mProps.getProperty(APP_ID_KEY);
  }

  /**
   * Get the app API key
   * @return the app API key
   */
  public String getAppApiKey(){
    return this.mProps.getProperty(APP_APIKEY_KEY);
  }

  /**
   * Get the connection tag
   * @return the connection tag
   */
  public String getConnectionTag(){
    return this.mProps.getProperty(APP_CONNECTION_TAG_KEY);
  }

  @Deprecated
  /**
   * Return the legacy app mode. can be null.
   */
  public String getAppMode(){
    return this.mProps.getProperty(APP_MODE_KEY);
  }

  /**
   * Return if the app is running in local dev mode.
   * If this is true, the cloud host value returned in CloudProps will be the host value set in the property file.
   * @return
   */
  public boolean isLocalDevelopment(){
    return this.isLocalDev;
  }

  /**
   * Return the singleton object of AppProps
   * @return
   */
  public static AppProps getInstance(){
    if(null == mInstance){
      throw new RuntimeException("AppProps is not initialised");
    }
    return mInstance;
  }

  /**
   * Load the fh.properties file
   * @param context
   * @return
   * @throws IOException
   */
  public static AppProps load(Context context) throws IOException {
    if(null == mInstance){
      InputStream in = null;
      boolean isLocalDev = false;
      Properties props = null;
      try {
        //support local development
        try {
          in = context.getAssets().open(DEBUG_PROPERTY_FILE);
          isLocalDev = true;
        } catch (IOException dioe) {
          in = null;
          isLocalDev = false;
        }

        if (!isLocalDev && null == in) {
          try {
            in = context.getAssets().open(NEW_PROPERTY_FILE);
          } catch (IOException ioe) {
            try{
              in = context.getAssets().open(OLD_PROPERTY_FILE);
            } catch (IOException ioex){
              in = null;
            }
          }
        }

        if(null == in){
          throw new IOException("can no find " + NEW_PROPERTY_FILE);
        }

        props = new Properties();
        props.load(in);
      } catch (IOException e) {
        FHLog.e(LOG_TAG, "Can not load property file.", e);
      } finally {
        if (null != in) {
          try {
            in.close();
          } catch (IOException ex) {
            FHLog.e(LOG_TAG, "Failed to close stream", ex);
          }
        }
      }
      mInstance = new AppProps(props, isLocalDev);
    }
    return mInstance;
  }
}
