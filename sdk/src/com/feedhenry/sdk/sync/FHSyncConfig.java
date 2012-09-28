package com.feedhenry.sdk.sync;

public class FHSyncConfig {
  
  private int mSyncFrequency = 10;
  private boolean mNotifySyncStarted = false;
  private boolean mNotifySyncComplete = false;
  private boolean mNotifySyncCollisions = false;
  private boolean mNotifyOfflineUpdate = false;
  private boolean mNotifyRemoteUpdateFailed = false;
  private boolean mNotifyRemoteUpdateApplied = false;
  private boolean mNotifyLocalUpdateApplied = false;
  private boolean mNotifyDeltaReceived = false;
  private boolean mNotifySyncFailed = false;
  private boolean mNotifyClientStorageFailed = false;

  public void setSyncFrequency(int pFrequency){
    mSyncFrequency = pFrequency;
  }
  
  public int getSyncFrequency(){
    return mSyncFrequency;
  }

  public boolean isNotifySyncStarted() {
    return mNotifySyncStarted;
  }

  public void setNotifySyncStarted(boolean pNotifySyncStarted) {
    this.mNotifySyncStarted = pNotifySyncStarted;
  }

  public boolean isNotifySyncComplete() {
    return mNotifySyncComplete;
  }

  public void setNotifySyncComplete(boolean pNotifySyncComplete) {
    this.mNotifySyncComplete = pNotifySyncComplete;
  }

  public boolean isNotifySyncCollisions() {
    return mNotifySyncCollisions;
  }

  public void setNotifySyncCollisions(boolean pNotifySyncComplete) {
    this.mNotifySyncCollisions = pNotifySyncComplete;
  }

  public boolean isNotifyOfflineUpdate() {
    return mNotifyOfflineUpdate;
  }

  public void setNotifyOfflineUpdate(boolean pNotifyOfflineUpdate) {
    this.mNotifyOfflineUpdate = pNotifyOfflineUpdate;
  }

  public boolean isNotifyUpdateFailed() {
    return mNotifyRemoteUpdateFailed;
  }

  public void setNotifyUpdateFailed(boolean pNotifyUpdateFailed) {
    this.mNotifyRemoteUpdateFailed = pNotifyUpdateFailed;
  }

  public boolean isNotifyRemoteUpdateApplied() {
    return mNotifyRemoteUpdateApplied;
  }

  public void setNotifyRemoteUpdateApplied(boolean pNotifyRemoteUpdateApplied) {
    this.mNotifyRemoteUpdateApplied = pNotifyRemoteUpdateApplied;
  }

  public boolean isNotifyLocalUpdateApplied() {
    return mNotifyLocalUpdateApplied;
  }

  public void setNotifyLocalUpdateApplied(boolean pNotifyLocalUpdateApplied) {
    this.mNotifyLocalUpdateApplied = pNotifyLocalUpdateApplied;
  }

  public boolean isNotifyDeltaReceived() {
    return mNotifyDeltaReceived;
  }

  public void setNotifyDeltaReceived(boolean pNotifyDeltaReceived) {
    this.mNotifyDeltaReceived = pNotifyDeltaReceived;
  }

  public boolean isNotifySyncFailed() {
    return mNotifySyncFailed;
  }

  public void setNotifySyncFailed(boolean pNotifySyncFailed) {
    this.mNotifySyncFailed = pNotifySyncFailed;
  }
  
  public void setNotifyClientStorageFailed(boolean pNotifyClientStorageFailed){
    this.mNotifyClientStorageFailed = pNotifyClientStorageFailed;
  }
  
  public boolean isNotifyClientStorageFailed(){
    return this.mNotifyClientStorageFailed;
  }
  
  
}
