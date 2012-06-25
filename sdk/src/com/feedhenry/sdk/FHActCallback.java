package com.feedhenry.sdk;

public interface FHActCallback {

  public void success(FHResponse pResponse);
  
  public void fail(FHResponse pResponse);
}
