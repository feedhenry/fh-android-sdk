package com.feedhenry.sdk.exceptions;

public class FHInvalidActionException extends Exception {

  private static final String mMessage = "Invalid action : ";
  public FHInvalidActionException(String pAction){
    super(mMessage + pAction);
  }
}
