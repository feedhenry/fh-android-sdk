package com.feedhenry.sdk;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Representing the response data from FeedHenry when an API call is finished
 */

public class FHResponse {

  private JSONObject mResults;
  private JSONArray mResultArray;
  private Throwable mError;
  private String mErrorMessage;
  
  public FHResponse(JSONObject pResults, JSONArray pResultArray, Throwable e, String pError){
    mResults = pResults;
    mResultArray = pResultArray;
    mError = e;
    mErrorMessage = pError;
  }
  
  /**
   * Get the response data as JSONObject
   * @return a JSONObject
   */
  public JSONObject getJson(){
    return mResults;
  }
  
  /**
   * Get the response data as JSONArray
   * @return a JSONArray
   */
  public JSONArray getArray(){
    return mResultArray;
  }
  
  /**
   * Get the error
   * @return the error
   */
  public Throwable getError(){
    return mError;
  }
  
  /**
   * Get the error message
   * @return the error message
   */
  public String getErrorMessage(){
    return mErrorMessage;
  }
  
  /**
   * Get the raw response content
   * @return the raw response content
   */
  public String getRawResponse(){
    if(null != mResults){
      return mResults.toString();
    }
    else if(null != mResultArray){
      return mResultArray.toString();
    } else {
      return mErrorMessage;
    }
  }
}
