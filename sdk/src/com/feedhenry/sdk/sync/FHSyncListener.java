package com.feedhenry.sdk.sync;

public interface FHSyncListener {
  public void onNotify(String pDataSetId, String pUID, String pCode, String pMessage);
}
