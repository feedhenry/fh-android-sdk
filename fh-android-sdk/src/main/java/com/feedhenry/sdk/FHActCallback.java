package com.feedhenry.sdk;

/**
 * A FHActCallback will be used to execute code after a FH API request finishes running on a background thread. This will make sure the UI does not freeze.
 * The {@link #success} and {@link #fail} methods will run on the main UI thread.
 * You can either implement this interface in your app's own classes or using anonymous inner class. 
 * For example:
 * <pre>
 * {@code
 * FHActRequest request = FH.buildActRequest("readData", new JSONObject());
 *   request.executeAsync(new FHActCallback(){
 *     public void success(FHResponse pResp){
 *       //process response data
 *     }
 *        
 *     public void fail(FHResponse pResp){
 *       //process error data
 *     }
 * })
 * }
 * </pre>
 *
 */
public interface FHActCallback {

  /**
   * Will be run if the action call is successful
   * @param pResponse the response data
   */
  public void success(FHResponse pResponse);
  
  /**
   * Will be run if the action call is failed
   * @param pResponse the response data
   */
  public void fail(FHResponse pResponse);
}
