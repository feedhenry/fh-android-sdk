package com.feedhenry.sdk.exceptions;

public class FHNotReadyException extends Exception {

  private static final String mMessage = "FH SDK is not ready. You need to ensure FH.initialize is called in Activity.onStart()";
  
  public FHNotReadyException(){
    super(mMessage);
  }
}
