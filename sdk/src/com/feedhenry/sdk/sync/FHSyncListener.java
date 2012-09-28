package com.feedhenry.sdk.sync;

public interface FHSyncListener {
  public void onSyncStarted(NotificationMessage pMessage);
  public void onSyncCompleted(NotificationMessage pMessage);
  public void onUpdateOffline(NotificationMessage pMessage);
  public void onCollisionDetected(NotificationMessage pMessage);
  public void onRemoteUpdateFailed(NotificationMessage pMessage);
  public void onRemoteUpdateApplied(NotificationMessage pMessage);
  public void onLocalUpdateApplied(NotificationMessage pMessage);
  public void onDeltaReceived(NotificationMessage pMessage);
  public void onSyncFailed(NotificationMessage pMessage);
  public void onClientStorageFailed(NotificationMessage pMessage);
}
