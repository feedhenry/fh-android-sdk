package com.feedhenry.sdk.exceptions;

/**
 * This exception will be thrown if an FH API method is called before FH.init finishes.
 */
public class FHNotReadyException extends Exception {

  private static final String mMessage =
      "FH SDK is not ready. You need to ensure FH.init is called.";

  public FHNotReadyException() {
    super(mMessage);
  }
}
