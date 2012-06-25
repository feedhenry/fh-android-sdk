package com.feedhenry.sdk;

import org.json.JSONObject;

public class FHResponse {

  private JSONObject mResults;
  private Throwable mError;
  private String mErrorMessage;
  
  public FHResponse(JSONObject pResults, Throwable e, String pError){
    mResults = pResults;
    mError = e;
    mErrorMessage = pError;
  }
  
  public JSONObject getResult(){
    return mResults;
  }
  
  public Throwable getError(){
    return mError;
  }
  
  public String getErrorMessage(){
    return mErrorMessage;
  }
}
