package com.feedhenry.sdk.sync;

/**
 * Implement the listener interface to monitor events invoked by the sync framework.
 */
public interface FHSyncListener {
  /**
   * Invoked when a sync loop start event is emitted
   * @param pMessage The message
   */
  public void onSyncStarted(NotificationMessage pMessage);
  
  /**
   * Invoked when a sync loop complete event is emitted
   * @param pMessage The message
   */
  public void onSyncCompleted(NotificationMessage pMessage);
  
  /**
   * Invoked when a offline update event is emitted.
   * @param pMessage The message
   */
  public void onUpdateOffline(NotificationMessage pMessage);
  
  /**
   * Invoked when a collision event is emitted.
   * @param pMessage The message
   */
  public void onCollisionDetected(NotificationMessage pMessage);
  
  /**
   * Invoked when a remote update failed event is emitted.
   * @param pMessage The message
   */
  public void onRemoteUpdateFailed(NotificationMessage pMessage);
  
  /**
   * Invoked when a remote update event is emitted.
   * @param pMessage The message
   */
  public void onRemoteUpdateApplied(NotificationMessage pMessage);
  
  /**
   * Invoked when a local update applied event is emitted.
   * @param pMessage The message
   */
  public void onLocalUpdateApplied(NotificationMessage pMessage);
  
  /**
   * Invoked when a delta received event is emitted.
   * @param pMessage The message
   */
  public void onDeltaReceived(NotificationMessage pMessage);
  
  /**
   * Invoked when a sync failed event is emitted.
   * @param pMessage The message
   */
  public void onSyncFailed(NotificationMessage pMessage);
  
  /**
   * Invoked when a client storage failed event is emitted.
   * @param pMessage The message
   */
  public void onClientStorageFailed(NotificationMessage pMessage);
}
