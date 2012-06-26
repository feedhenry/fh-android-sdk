package com.feedhenry.sdk;

/**
 * Representing the callback function. The callback function will be executed once the action call is finished.
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
