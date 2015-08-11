/**
 * Copyright (c) 2015 FeedHenry Ltd, All Rights Reserved.
 *
 * Please refer to your contract with FeedHenry for the software license agreement.
 * If you do not have a contract, you do not have a license to use this software.
 */
package com.feedhenry.sdk.sync;

/**
 * Implement the listener interface to monitor events invoked by the sync framework.
 */
public interface FHSyncListener {
    /**
     * Invoked when a sync loop start event is emitted
     *
     * @param pMessage The message
     */
    void onSyncStarted(NotificationMessage pMessage);

    /**
     * Invoked when a sync loop complete event is emitted
     *
     * @param pMessage The message
     */
    void onSyncCompleted(NotificationMessage pMessage);

    /**
     * Invoked when a offline update event is emitted.
     *
     * @param pMessage The message
     */
    void onUpdateOffline(NotificationMessage pMessage);

    /**
     * Invoked when a collision event is emitted.
     *
     * @param pMessage The message
     */
    void onCollisionDetected(NotificationMessage pMessage);

    /**
     * Invoked when a remote update failed event is emitted.
     *
     * @param pMessage The message
     */
    void onRemoteUpdateFailed(NotificationMessage pMessage);

    /**
     * Invoked when a remote update event is emitted.
     *
     * @param pMessage The message
     */
    void onRemoteUpdateApplied(NotificationMessage pMessage);

    /**
     * Invoked when a local update applied event is emitted.
     *
     * @param pMessage The message
     */
    void onLocalUpdateApplied(NotificationMessage pMessage);

    /**
     * Invoked when a delta received event is emitted.
     *
     * @param pMessage The message
     */
    void onDeltaReceived(NotificationMessage pMessage);

    /**
     * Invoked when a sync failed event is emitted.
     *
     * @param pMessage The message
     */
    void onSyncFailed(NotificationMessage pMessage);

    /**
     * Invoked when a client storage failed event is emitted.
     *
     * @param pMessage The message
     */
    void onClientStorageFailed(NotificationMessage pMessage);
}
