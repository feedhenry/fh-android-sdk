package com.feedhenry.sdk.sync;

public interface FHSyncListener {
  public void onSyncStarted(NotificationMessage pMessage);
  public void onSyncCompleted(NotificationMessage pMessage);
  public void onUpdateOffline(NotificationMessage pMessage);
  public void onCollisionDetected(NotificationMessage pMessage);
  public void onUpdateFailed(NotificationMessage pMessage);
  public void onUpdateApplied(NotificationMessage pMessage);
  public void onDeltaReceived(NotificationMessage pMessage);
  public void onClientStorageFailed(NotificationMessage pMessage);
}
