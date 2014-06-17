package com.feedhenry.sdk.api;

import org.apache.http.Header;
import org.json.fh.JSONException;
import org.json.fh.JSONObject;

import android.content.Context;

import com.feedhenry.sdk.CloudProps;
import com.feedhenry.sdk.FH;
import com.feedhenry.sdk.FHRemote;

/**
 * The request for calling the cloud side function of the app. Example:
 * 
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
 */
public class FHActRequest extends FHRemote {

  private String mRemoteAct;
  private CloudProps mCloudProps;
  protected JSONObject mArgs = new JSONObject();

  protected static String LOG_TAG = "com.feedhenry.sdk.api.FHActRequest";

  /**
   * Constructor
   * 
   * @param pProps
   *          the app configuration
   * @param pCloudProps
   *          the properties returned from the cloud
   */
  public FHActRequest(Context context, CloudProps pCloudProps) {
    super(context, pCloudProps.getAppProperties());
    mCloudProps = pCloudProps;
  }
  
  
  protected String getApiURl() {
    String host = mCloudProps.getCloudHost();
    String path = getPath();
    String hostUrl = host + (path.startsWith("/")? path: ("/" + path));
    return hostUrl;
  }

  /**
   * The name of the cloud side function
   * 
   * @param pAction
   *          cloud side function name
   */
  public void setRemoteAction(String pAction) {
    mRemoteAct = pAction;
  }

  /**
   * Set the parameters for the cloud side function
   * 
   * @param pArgs
   *          the parameters that will be passed to the cloud side function
   * @throws Exception 
   * @throws JSONException 
   */
  public void setArgs(JSONObject pArgs){
    mArgs = pArgs;
  //keep backward compatibility
    if(!mArgs.has("__fh")){
      try{
        mArgs.put("__fh", FH.getDefaultParams());
      } catch(Exception e){
        
      }
    }
  }

  protected JSONObject getRequestArgs() {
    return mArgs;
  }

  @Override
  protected String getPath() {
    return "cloud/" + mRemoteAct;
  }


  @Override
  protected Header[] buildHeaders(Header[] pHeaders) throws Exception {
    return null;
  }
}
