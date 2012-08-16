package com.feedhenry.sdk.api;

import java.util.Properties;

import org.json.JSONObject;

import android.util.Log;

import com.feedhenry.sdk.FH;
import com.feedhenry.sdk.FHRemote;

/**
 * The request for calling the cloud side function of the app
 *
 */
public class FHActRequest extends FHRemote {

  private String mRemoteAct;
  private static final String METHOD = "cloud";
  private JSONObject mCloudProps;
  protected JSONObject mArgs;
  
  /**
   * Constructor
   * @param pProps the app configuration
   * @param pCloudProps the properties returned from the cloud
   */
  public FHActRequest(Properties pProps, JSONObject pCloudProps){
    super(pProps);
    mCloudProps = pCloudProps;
  }
  
  protected String getApiURl(){
    String hostUrl = null;
    String appMode = mProperties.getProperty(APP_MODE_KEY);
    try{
      JSONObject hosts = mCloudProps.getJSONObject("hosts");
      if("dev".equalsIgnoreCase(appMode)){
        hostUrl = hosts.getString("debugCloudUrl");
      } else {
        hostUrl = hosts.getString("releaseCloudUrl");
      }
    } catch (Exception e){
       Log.e(FH.LOG_TAG, e.getMessage(), e);
    }
    return hostUrl.endsWith("/") ? hostUrl + getPath() : hostUrl + "/" + getPath();
  }
  
  /**
   * The name of the cloud side function
   * @param pAction cloud side function name
   */
  public void setRemoteAction(String pAction){
    mRemoteAct = pAction;
  }
  
  /**
   * Set the parameters for the cloud side function
   * @param pArgs the parameters that will be passed to the cloud side function
   */
  public void setArgs(JSONObject pArgs) {
    mArgs = pArgs;
  }
  
  protected JSONObject getRequestArgs(){
    return mArgs;
  }

  @Override
  protected String getPath() {
    return "cloud/" + mRemoteAct;
  }
}
