package com.feedhenry.sdk;

import android.content.Context;
import com.feedhenry.sdk.utils.FHLog;
import java.lang.reflect.Field;

/**
 * Created by weili on 15/04/15.
 */
public class Device {

  private String mDeviceId;
  private String mDeviceName;
  private String mUserAgent;
  private Context mContext;

  private static Device mInstance;

  private static final String MANUFACTURER_FIELD = "MANUFACTURER";
  private static final String USER_AGENT_TEMP = "Android %s; %s";

  private static final String LOG_TAG = "com.feedhenry.sdk.Device";

  private Device(Context pContext){
    this.mContext = pContext;
  }

  /**
   * Return the unique device id.
   * @return the unique device id
   */
  public String getDeviceId(){
    if(null == mDeviceId){
      mDeviceId = android.provider.Settings.Secure.getString(mContext.getContentResolver(),
          android.provider.Settings.Secure.ANDROID_ID);
    }
    return mDeviceId;
  }

  /**
   * Return the name of the device (MANUFACTURER).
   * @return the name of the device
   */
  public String getDeviceName(){
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
  public String getUserAgent(){
    if(null == mUserAgent){
      mUserAgent = String.format(USER_AGENT_TEMP, android.os.Build.VERSION.RELEASE, getDeviceName());
      FHLog.d(LOG_TAG, "UA = " + mUserAgent);
    }
    return mUserAgent;
  }

  public static Device init(Context pContext){
    if(null == mInstance){
      mInstance = new Device(pContext);
    }
    return mInstance;
  }

  public static Device getInstance(){
    return mInstance;
  }
}
