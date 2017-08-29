package com.feedhenry.sdk.sync;

/**
 * Interface for sync client events.
 */
public interface FHSyncEvent {
    void event(NotificationMessage pMessage);
}
