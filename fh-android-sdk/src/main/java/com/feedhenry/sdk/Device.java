package com.feedhenry.sdk;

import android.content.Context;
import com.feedhenry.sdk.utils.FHLog;
import java.lang.reflect.Field;


public class Device {

  private static String mDeviceId;
  private static String mDeviceName;
  private static String mUserAgent;

  private static final String MANUFACTURER_FIELD = "MANUFACTURER";
  private static final String USER_AGENT_TEMP = "Android %s; %s";

  private static final String LOG_TAG = "com.feedhenry.sdk.Device";

  /**
   * Return the unique device id.
   * @return the unique device id
   */
  public static String getDeviceId(Context context){
    if(null == mDeviceId){
      mDeviceId = android.provider.Settings.Secure.getString(context.getContentResolver(),
          android.provider.Settings.Secure.ANDROID_ID);
    }
    return mDeviceId;
  }

  /**
   * Return the name of the device (MANUFACTURER).
   * @return the name of the device
   */
  public static String getDeviceName(){
    if(null == mDeviceName){
      String manufacurer = "";
      String model = android.os.Build.MODEL;
      String deviceName = model;
      try {
        Field field = android.os.Build.class.getField(MANUFACTURER_FIELD);
        if (null != field) {
          manufacurer = (String) field.get(null);
          deviceName = manufacurer + " " + model;
        }
      } catch (Exception e) {

      }
      mDeviceName = deviceName;
    }
    return mDeviceName;
  }

  /**
   * Return the custom user agent string
   * @return the custom user agent string
   */
  public static String getUserAgent(){
    if(null == mUserAgent){
      mUserAgent = String.format(USER_AGENT_TEMP, android.os.Build.VERSION.RELEASE, getDeviceName());
      FHLog.d(LOG_TAG, "UA = " + mUserAgent);
    }
    return mUserAgent;
  }
}
