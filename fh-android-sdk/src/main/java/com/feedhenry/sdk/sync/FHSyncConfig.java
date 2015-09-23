/**
 * Copyright (c) 2015 FeedHenry Ltd, All Rights Reserved.
 *
 * Please refer to your contract with FeedHenry for the software license agreement.
 * If you do not have a contract, you do not have a license to use this software.
 */
package com.feedhenry.sdk.sync;

import org.json.fh.JSONObject;

/**
 * Configuration options for the sync framework.
 */
public class FHSyncConfig {

    private int mSyncFrequencySeconds = 10;
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
    private boolean mUseCustomSync = false;

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

    /**
     * Sets the sync interval in seconds.
     *
     * @param pFrequencySeconds the new sync interval
     */
    public void setSyncFrequency(int pFrequencySeconds) {
        mSyncFrequencySeconds = pFrequencySeconds;
    }

    /**
     * Gets the current sync interval.
     *
     * @return the current sync interval.
     */
    public int getSyncFrequency() {
        return mSyncFrequencySeconds;
    }

    /**
     * Gets whether the sync client notifies on a sync start event.
     *
     * @return whether a sync start event will trigger a notification
     */
    public boolean isNotifySyncStarted() {
        return mNotifySyncStarted;
    }

    /**
     * Sets if the sync client should notify on a sync start event.
     *
     * @param pNotifySyncStarted whether to notify on sync start
     */
    public void setNotifySyncStarted(boolean pNotifySyncStarted) {
        this.mNotifySyncStarted = pNotifySyncStarted;
    }

    /**
     * Gets whether the sync client notifies on a sync complete event.
     *
     * @return whether a sync complete event will trigger a notification
     */
    public boolean isNotifySyncComplete() {
        return mNotifySyncComplete;
    }

    /**
     * Sets if the sync client should notify on a sync complete event.
     *
     * @param pNotifySyncComplete whether to notify on sync complete
     */
    public void setNotifySyncComplete(boolean pNotifySyncComplete) {
        this.mNotifySyncComplete = pNotifySyncComplete;
    }

    /**
     * Gets whether the sync client notifies on a sync collision event.
     *
     * @return whether a sync collision event will trigger a notification
     */
    public boolean isNotifySyncCollisions() {
        return mNotifySyncCollisions;
    }

    /**
     * Sets if the sync client should notify on a sync collision event.
     *
     * @param pNotifySyncCollsion whether to notify on sync collision
     */
    public void setNotifySyncCollisions(boolean pNotifySyncCollsion) {
        this.mNotifySyncCollisions = pNotifySyncCollsion;
    }

    /**
     * Gets whether the sync client notifies on an offline update event.
     *
     * @return whether an offline update event will trigger a notification
     */
    public boolean isNotifyOfflineUpdate() {
        return mNotifyOfflineUpdate;
    }

    /**
     * Sets if the sync client should notify on an offline update event.
     *
     * @param pNotifyOfflineUpdate whether to notify on offline update
     */
    public void setNotifyOfflineUpdate(boolean pNotifyOfflineUpdate) {
        this.mNotifyOfflineUpdate = pNotifyOfflineUpdate;
    }

    /**
     * Gets whether the sync client notifies on an update failed event.
     *
     * @return whether an update failed event will trigger a notification
     */
    public boolean isNotifyUpdateFailed() {
        return mNotifyRemoteUpdateFailed;
    }

    /**
     * Sets if the sync client should notify on an update failed event.
     *
     * @param pNotifyUpdateFailed whether to notify on update failed
     */
    public void setNotifyUpdateFailed(boolean pNotifyUpdateFailed) {
        this.mNotifyRemoteUpdateFailed = pNotifyUpdateFailed;
    }

    /**
     * Gets whether the sync client notifies on a remote update applied event.
     *
     * @return whether a remote update applied event will trigger a notification
     */
    public boolean isNotifyRemoteUpdateApplied() {
        return mNotifyRemoteUpdateApplied;
    }

    /**
     * Sets if the sync client should notify on a remote updates applied event.
     *
     * @param pNotifyRemoteUpdateApplied whether to notify on remote updates applied
     */
    public void setNotifyRemoteUpdateApplied(boolean pNotifyRemoteUpdateApplied) {
        this.mNotifyRemoteUpdateApplied = pNotifyRemoteUpdateApplied;
    }

    /**
     * Gets whether the sync client notifies on a local updates applied event.
     *
     * @return whether a local updates applied event will trigger a notification
     */
    public boolean isNotifyLocalUpdateApplied() {
        return mNotifyLocalUpdateApplied;
    }

    /**
     * Sets if the sync client should notify on a local updates applied event.
     *
     * @param pNotifyLocalUpdateApplied whether to notify on local updates applied
     */
    public void setNotifyLocalUpdateApplied(boolean pNotifyLocalUpdateApplied) {
        this.mNotifyLocalUpdateApplied = pNotifyLocalUpdateApplied;
    }

    /**
     * Gets whether the sync client notifies on a delta received event.
     *
     * @return whether a delta received event will trigger a notification
     */
    public boolean isNotifyDeltaReceived() {
        return mNotifyDeltaReceived;
    }

    /**
     * Sets if the sync client should notify on a delta received event.
     *
     * @param pNotifyDeltaReceived whether to notify on delta received
     */
    public void setNotifyDeltaReceived(boolean pNotifyDeltaReceived) {
        this.mNotifyDeltaReceived = pNotifyDeltaReceived;
    }

    /**
     * Gets whether the sync client notifies on a sync failed event.
     *
     * @return whether a sync failed event will trigger a notification
     */
    public boolean isNotifySyncFailed() {
        return mNotifySyncFailed;
    }

    /**
     * Sets if the sync client should notify on a sync failed event.
     *
     * @param pNotifySyncFailed whether to notify on sync failed
     */
    public void setNotifySyncFailed(boolean pNotifySyncFailed) {
        this.mNotifySyncFailed = pNotifySyncFailed;
    }

    /**
     * Sets if the sync client should notify on a client storage failed event.
     *
     * @param pNotifyClientStorageFailed whether to notify on client storage failed
     */
    public void setNotifyClientStorageFailed(boolean pNotifyClientStorageFailed) {
        this.mNotifyClientStorageFailed = pNotifyClientStorageFailed;
    }

    /**
     * Gets whether the sync client notifies on a client storage failed event.
     *
     * @return whether a client storage failed event will trigger a notification
     */
    public boolean isNotifyClientStorageFailed() {
        return this.mNotifyClientStorageFailed;
    }

    /**
     * Gets whether the sync client automatically updates on local changes.
     *
     * @return whether local changes are automatically synced
     */
    public boolean isAutoSyncLocalUpdates() {
        return mAutoSyncLocalUpdates;
    }

    /**
     * Sets if the sync client should automatically update on local changes.
     *
     * @param mAutoSyncLocalUpdates whether local changes should automatically sync
     */
    public void setAutoSyncLocalUpdates(boolean mAutoSyncLocalUpdates) {
        this.mAutoSyncLocalUpdates = mAutoSyncLocalUpdates;
    }

    /**
     * Gets the maximum crash count.
     *
     * @return the maximum crash count number
     */
    public int getCrashCountWait() {
        return mCrashCountWait;
    }

    /**
     * Sets the maximum crash count number.
     * Changes may fail to be applied (crash) due to various reasons (e.g., network issues).
     * If the crash count reaches this limit, the changes will be either re-submitted or abandoned.
     *
     * @param mCrashCountWait the crash limit
     */
    public void setCrashCountWait(int mCrashCountWait) {
        this.mCrashCountWait = mCrashCountWait;
    }

    /**
     * Gets whether changes should be re-submitted or abandoned when the crash limit is reached.
     *
     * @return true or false
     */
    public boolean isResendCrashedUpdates() {
        return mResendCrashedUpdates;
    }

    /**
     * Sets whether changes should be re-submitted once the crash limit is reached.
     * If false, changes will be discarded.
     *
     * @param mResendCrashedUpdates true or false.
     */
    public void setResendCrashedUpdates(boolean mResendCrashedUpdates) {
        this.mResendCrashedUpdates = mResendCrashedUpdates;
    }

    /**
     * Set if legacy mode is used
     * @param mUseCustomSync
     */
    public void setUseCustomSync(boolean mUseCustomSync) { this.mUseCustomSync = mUseCustomSync; }

    /**
     * Check if legacy mode is enabled
     * @return
     */
    public boolean useCustomSync() { return this.mUseCustomSync; };

    /**
     * Gets a JSON representation of the configuration object.
     *
     * @return The JSON object
     */
    public JSONObject getJSON() {
        JSONObject ret = new JSONObject();
        ret.put(KEY_SYNC_FREQUENCY, this.mSyncFrequencySeconds);
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

    /**
     * Creates a new configuration object from JSON.
     *
     * @param pObj the sync config JSON
     * @return the new sync config object
     */
    public static FHSyncConfig fromJSON(JSONObject pObj) {
        FHSyncConfig config = new FHSyncConfig();
        config.setSyncFrequency(pObj.optInt(KEY_SYNC_FREQUENCY));
        config.setAutoSyncLocalUpdates(pObj.optBoolean(KEY_AUTO_SYNC_UPDATES));
        config.setNotifyClientStorageFailed(pObj.optBoolean(KEY_NOTIFY_CLIENT_STORAGE_FAILED));
        config.setNotifyDeltaReceived(pObj.optBoolean(KEY_NOTIFY_DELTA_RECEIVED));
        config.setNotifyOfflineUpdate(pObj.optBoolean(KEY_NOTIFY_OFFLINE_UPDATED));
        config.setNotifySyncCollisions(pObj.optBoolean(KEY_NOTIFY_SYNC_COLLISION));
        config.setNotifySyncComplete(pObj.optBoolean(KEY_NOTIFY_SYNC_COMPLETED));
        config.setNotifySyncStarted(pObj.optBoolean(KEY_NOTIFY_SYNC_STARTED));
        config.setNotifyRemoteUpdateApplied(pObj.optBoolean(KEY_NOTIFY_REMOTE_UPDATED_APPLIED));
        config.setNotifyLocalUpdateApplied(pObj.optBoolean(KEY_NOTIFY_LOCAL_UPDATE_APPLIED));
        config.setNotifyUpdateFailed(pObj.optBoolean(KEY_NOTIFY_REMOTE_UPDATED_FAILED));
        config.setNotifySyncFailed(pObj.optBoolean(KEY_NOTIFY_SYNC_FAILED));
        config.setCrashCountWait(pObj.optInt(KEY_CRASHCOUNT, 10));
        config.setResendCrashedUpdates(pObj.optBoolean(KEY_RESEND_CRASH));
        return config;
    }

    public FHSyncConfig clone() {
        JSONObject json = this.getJSON();
        return FHSyncConfig.fromJSON(json);
    }
}
