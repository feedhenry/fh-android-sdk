package com.feedhenry.sdk.sync;

public class FHSyncConfig {
  
  private int mSyncFrequency = 10;
  private boolean mNotifySyncStarted = false;
  private boolean mNotifySyncComplete = false;
  private boolean mNotifySyncCollisions = false;
  private boolean mNotifyOfflineUpdate = false;
  private boolean mNotifyUpdateFailed = false;
  private boolean mNotifyUpdateApplied = false;
  private boolean mNotifyDeltaReceived = false;
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
    return mNotifyUpdateFailed;
  }

  public void setNotifyUpdateFailed(boolean pNotifyUpdateFailed) {
    this.mNotifyUpdateFailed = pNotifyUpdateFailed;
  }

  public boolean isNotifyUpdateApplied() {
    return mNotifyUpdateApplied;
  }

  public void setNotifyUpdateApplied(boolean pNotifyUpdateApplied) {
    this.mNotifyUpdateApplied = pNotifyUpdateApplied;
  }

  public boolean isNotifyDeltaReceived() {
    return mNotifyDeltaReceived;
  }

  public void setNotifyDeltaReceived(boolean pNotifyDeltaReceived) {
    this.mNotifyDeltaReceived = pNotifyDeltaReceived;
  }
  
  public void setNotifyClientStorageFailed(boolean pNotifyClientStorageFailed){
    this.mNotifyClientStorageFailed = pNotifyClientStorageFailed;
  }
  
  public boolean isNotifyClientStorageFailed(){
    return this.mNotifyClientStorageFailed;
  }
  
  
}
