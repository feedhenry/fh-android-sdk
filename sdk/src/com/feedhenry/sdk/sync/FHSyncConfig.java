package com.feedhenry.sdk.sync;

import org.json.JSONObject;

public class FHSyncConfig {
  
  private int mSyncFrequency = 10;
  private boolean mAutoSyncLocalUpdates = false;
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
  private int mCrashCountWait = 10;
  private boolean mResendCrashedUpdates = true;
  
  private static final String KEY_SYNC_FREQUENCY = "syncFrequency";
  private static final String KEY_AUTO_SYNC_UPDATES = "autoSyncLocalUpdates";
  private static final String KEY_NOTIFY_CLIENT_STORAGE_FAILED = "notifyClientStorageFailed";
  private static final String KEY_NOTIFY_DELTA_RECEIVED = "notifyDeltaReceived";
  private static final String KEY_NOTIFY_OFFLINE_UPDATED = "notifyOfflineUpdated";
  private static final String KEY_NOTIFY_SYNC_COLLISION = "notifySyncCollision";
  private static final String KEY_NOTIFY_SYNC_COMPLETED = "notifySyncCompleted";
  private static final String KEY_NOTIFY_SYNC_STARTED = "notifySyncStarted";
  private static final String KEY_NOTIFY_REMOTE_UPDATED_APPLIED = "notifyRemoteUpdatedApplied";
  private static final String KEY_NOTIFY_LOCAL_UPDATE_APPLIED = "notifyLocalUpdateApplied";
  private static final String KEY_NOTIFY_REMOTE_UPDATED_FAILED = "notifyRemoteUpdateFailed";
  private static final String KEY_NOTIFY_SYNC_FAILED = "notifySyncFailed";
  private static final String KEY_CRASHCOUNT = "crashCountWait";
  private static final String KEY_RESEND_CRASH = "resendCrashdUpdates";

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
  
  public boolean isAutoSyncLocalUpdates() {
    return mAutoSyncLocalUpdates;
  }

  public void setAutoSyncLocalUpdates(boolean mAutoSyncLocalUpdates) {
    this.mAutoSyncLocalUpdates = mAutoSyncLocalUpdates;
  }

  public int getCrashCountWait() {
    return mCrashCountWait;
  }

  public void setCrashCountWait(int mCrashCountWait) {
    this.mCrashCountWait = mCrashCountWait;
  }

  public boolean isResendCrashedUpdates() {
    return mResendCrashedUpdates;
  }

  public void setResendCrashedUpdates(boolean mResendCrashedUpdates) {
    this.mResendCrashedUpdates = mResendCrashedUpdates;
  }
  
  public JSONObject getJSON(){
    JSONObject ret = new JSONObject();
    ret.put(KEY_SYNC_FREQUENCY, this.mSyncFrequency);
    ret.put(KEY_AUTO_SYNC_UPDATES, this.mAutoSyncLocalUpdates);
    ret.put(KEY_NOTIFY_CLIENT_STORAGE_FAILED, this.mNotifyClientStorageFailed);
    ret.put(KEY_NOTIFY_DELTA_RECEIVED, this.mNotifyDeltaReceived);
    ret.put(KEY_NOTIFY_OFFLINE_UPDATED, this.mNotifyOfflineUpdate);
    ret.put(KEY_NOTIFY_SYNC_COLLISION, this.mNotifySyncCollisions);
    ret.put(KEY_NOTIFY_SYNC_COMPLETED, this.mNotifySyncComplete);
    ret.put(KEY_NOTIFY_SYNC_STARTED, this.mNotifySyncStarted);
    ret.put(KEY_NOTIFY_REMOTE_UPDATED_APPLIED, this.mNotifyRemoteUpdateApplied);
    ret.put(KEY_NOTIFY_LOCAL_UPDATE_APPLIED, this.mNotifyLocalUpdateApplied);
    ret.put(KEY_NOTIFY_REMOTE_UPDATED_FAILED, this.mNotifyRemoteUpdateFailed);
    ret.put(KEY_NOTIFY_SYNC_FAILED, this.mNotifySyncFailed);
    ret.put(KEY_CRASHCOUNT, this.mCrashCountWait);
    ret.put(KEY_RESEND_CRASH, this.mResendCrashedUpdates);
    return ret;
  }
  
  public static FHSyncConfig fromJSON(JSONObject pObj){
    FHSyncConfig config = new FHSyncConfig();
    config.setSyncFrequency(pObj.optInt(KEY_SYNC_FREQUENCY));
    config.setAutoSyncLocalUpdates(pObj.optBoolean(KEY_AUTO_SYNC_UPDATES));
    config.setNotifyClientStorageFailed(pObj.optBoolean(KEY_NOTIFY_CLIENT_STORAGE_FAILED));
    config.setNotifyDeltaReceived(pObj.optBoolean(KEY_NOTIFY_DELTA_RECEIVED));
    config.setNotifyOfflineUpdate(pObj.optBoolean(KEY_NOTIFY_OFFLINE_UPDATED));
    config.setNotifySyncCollisions(pObj.optBoolean(KEY_NOTIFY_SYNC_COLLISION));
    config.setNotifySyncComplete( pObj.optBoolean(KEY_NOTIFY_SYNC_COMPLETED));
    config.setNotifySyncStarted(pObj.optBoolean(KEY_NOTIFY_SYNC_STARTED));
    config.setNotifyRemoteUpdateApplied(pObj.optBoolean(KEY_NOTIFY_REMOTE_UPDATED_APPLIED));
    config.setNotifyLocalUpdateApplied(pObj.optBoolean(KEY_NOTIFY_LOCAL_UPDATE_APPLIED));
    config.setNotifyUpdateFailed(pObj.optBoolean(KEY_NOTIFY_REMOTE_UPDATED_FAILED));
    config.setNotifySyncFailed(pObj.optBoolean(KEY_NOTIFY_SYNC_FAILED));
    config.setCrashCountWait(pObj.optInt(KEY_CRASHCOUNT, 10));
    config.setResendCrashedUpdates(pObj.optBoolean(KEY_RESEND_CRASH));
    return config;
  }
  
  public FHSyncConfig clone(){
    JSONObject json = this.getJSON();
    return FHSyncConfig.fromJSON(json);
  }
  
}
