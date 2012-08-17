package com.feedhenry.sdk.api;

import java.util.Properties;

import org.json.JSONObject;

import com.feedhenry.sdk.FHRemote;
import com.feedhenry.sdk.utils.FHLog;

/**
 * The request for calling the cloud side function of the app.
 * Example:
 * <pre>
 * {@code
 *   //calling a cloud side function called "getTweets" and pass in the keywords
 *   FHActRequest request = FH.buildActRequest("getTweets", new JSONObject().put("keyword", "FeedHenry"));
 *   reqeust.executeAsync(new FHActCallback(){
 *     public void success(FHResponse pResp){
 *       JSONObject tweetsObj = pResp.getJson();
 *       ...
 *     }
 *        
 *     public void fail(FHResponse pResp){
 *       //process error data
 *       ...
 *     }
 *   });
 * }
 * </pre>
 * @see <a href="http://docs.feedhenry.com/v2/feedhenry-api.html#$fh.act">FH Act API doc</a>
 */
public class FHActRequest extends FHRemote {

  private String mRemoteAct;
  private static final String METHOD = "cloud";
  private JSONObject mCloudProps;
  protected JSONObject mArgs;
  
  protected static String LOG_TAG = "com.feedhenry.sdk.api.FHActRequest";
  
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
      FHLog.v(LOG_TAG, "act url = " + hostUrl);
    } catch (Exception e){
      FHLog.e(LOG_TAG, e.getMessage(), e);
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
