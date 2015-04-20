/**
 * Copyright (c) 2014 FeedHenry Ltd, All Rights Reserved.
 *
 * Please refer to your contract with FeedHenry for the software license agreement.
 * If you do not have a contract, you do not have a license to use this software.
 */
package com.feedhenry.sdk.sync;

import org.json.fh.JSONObject;

/**
 * The configuration options for the sync framework.
 */
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

    /**
     * Set the sync interval. In seconds.
     * 
     * @param pFrequency the new sync interval
     */
    public void setSyncFrequency(int pFrequency) {
        mSyncFrequency = pFrequency;
    }

    /**
     * Get the current sync interval
     * 
     * @return the current sync interval.
     */
    public int getSyncFrequency() {
        return mSyncFrequency;
    }

    /**
     * Does the sync client notify sync start event
     * 
     * @return true or false
     */
    public boolean isNotifySyncStarted() {
        return mNotifySyncStarted;
    }

    /**
     * Set if the sync client should notify start event
     * 
     * @param pNotifySyncStarted true or false
     */
    public void setNotifySyncStarted(boolean pNotifySyncStarted) {
        this.mNotifySyncStarted = pNotifySyncStarted;
    }

    /**
     * Does the sync client notify sync complete event
     * 
     * @return true or false
     */
    public boolean isNotifySyncComplete() {
        return mNotifySyncComplete;
    }

    /**
     * Set if the sync client should notify complete event
     * 
     * @param pNotifySyncComplete true or false
     */
    public void setNotifySyncComplete(boolean pNotifySyncComplete) {
        this.mNotifySyncComplete = pNotifySyncComplete;
    }

    /**
     * Does the sync client notify sync collision event
     * 
     * @return true or false
     */
    public boolean isNotifySyncCollisions() {
        return mNotifySyncCollisions;
    }

    /**
     * Set if the sync client should notify sync collision event
     * 
     * @param pNotifySyncCollsion true or false
     */
    public void setNotifySyncCollisions(boolean pNotifySyncCollsion) {
        this.mNotifySyncCollisions = pNotifySyncCollsion;
    }

    /**
     * Does the sync client notify offline update event
     * 
     * @return true or false
     */
    public boolean isNotifyOfflineUpdate() {
        return mNotifyOfflineUpdate;
    }

    /**
     * Set if the sync client notify offline update event.
     * 
     * @param pNotifyOfflineUpdate true of false
     */
    public void setNotifyOfflineUpdate(boolean pNotifyOfflineUpdate) {
        this.mNotifyOfflineUpdate = pNotifyOfflineUpdate;
    }

    /**
     * Does the sync client notify update failed event
     * 
     * @return true or false
     */
    public boolean isNotifyUpdateFailed() {
        return mNotifyRemoteUpdateFailed;
    }

    /**
     * Set if the sync client should notify update failed event
     * 
     * @param pNotifyUpdateFailed true or false
     */
    public void setNotifyUpdateFailed(boolean pNotifyUpdateFailed) {
        this.mNotifyRemoteUpdateFailed = pNotifyUpdateFailed;
    }

    /**
     * Does the sync client notify remote updates applied event
     * 
     * @return true or false
     */
    public boolean isNotifyRemoteUpdateApplied() {
        return mNotifyRemoteUpdateApplied;
    }

    /**
     * Set if the sync client should notify remote updates applied event
     * 
     * @param pNotifyRemoteUpdateApplied true or false
     */
    public void setNotifyRemoteUpdateApplied(boolean pNotifyRemoteUpdateApplied) {
        this.mNotifyRemoteUpdateApplied = pNotifyRemoteUpdateApplied;
    }

    /**
     * Does the sync client notify local updates applied event
     * 
     * @return true or false
     */
    public boolean isNotifyLocalUpdateApplied() {
        return mNotifyLocalUpdateApplied;
    }

    /**
     * Set if the sync client should notify local updates applied event
     * 
     * @param pNotifyLocalUpdateApplied true or false
     */
    public void setNotifyLocalUpdateApplied(boolean pNotifyLocalUpdateApplied) {
        this.mNotifyLocalUpdateApplied = pNotifyLocalUpdateApplied;
    }

    /**
     * Does the sync client notify delta received event
     * 
     * @return true or false
     */
    public boolean isNotifyDeltaReceived() {
        return mNotifyDeltaReceived;
    }

    /**
     * Set if the sync client should notify delta received event
     * 
     * @param pNotifyDeltaReceived true or false
     */
    public void setNotifyDeltaReceived(boolean pNotifyDeltaReceived) {
        this.mNotifyDeltaReceived = pNotifyDeltaReceived;
    }

    /**
     * Does the sync client notify sync failed event
     * 
     * @return true or false
     */
    public boolean isNotifySyncFailed() {
        return mNotifySyncFailed;
    }

    /**
     * Set if the sync client should notify sync failed event
     * 
     * @param pNotifySyncFailed true or false
     */
    public void setNotifySyncFailed(boolean pNotifySyncFailed) {
        this.mNotifySyncFailed = pNotifySyncFailed;
    }

    /**
     * Set if the sync client should notify client storage failed event
     * 
     * @param pNotifyClientStorageFailed true or false
     */
    public void setNotifyClientStorageFailed(boolean pNotifyClientStorageFailed) {
        this.mNotifyClientStorageFailed = pNotifyClientStorageFailed;
    }

    /**
     * Does the sync client notify client storage failed event
     * 
     * @return true or false
     */
    public boolean isNotifyClientStorageFailed() {
        return this.mNotifyClientStorageFailed;
    }

    /**
     * Does the sync client automatically update local changes
     * 
     * @return true or false
     */
    public boolean isAutoSyncLocalUpdates() {
        return mAutoSyncLocalUpdates;
    }

    /**
     * Set if the sync client should automatically update local changes
     * 
     * @param mAutoSyncLocalUpdates true of false
     */
    public void setAutoSyncLocalUpdates(boolean mAutoSyncLocalUpdates) {
        this.mAutoSyncLocalUpdates = mAutoSyncLocalUpdates;
    }

    /**
     * Get the maximum crash count.
     * 
     * @return the maximum crash count number
     */
    public int getCrashCountWait() {
        return mCrashCountWait;
    }

    /**
     * Set the maximum crash count number. Changes may fail to be applied(crash) due to various reasons (network issue for example). If the crash count reaches this limit, the changes will be either
     * re-submitted or abandoned.
     * 
     * @param mCrashCountWait the crash limit
     */
    public void setCrashCountWait(int mCrashCountWait) {
        this.mCrashCountWait = mCrashCountWait;
    }

    /**
     * If the crash limit is reached, should the changes be re-submitted or abandoned.
     * 
     * @return true or false
     */
    public boolean isResendCrashedUpdates() {
        return mResendCrashedUpdates;
    }

    /**
     * If this is set to true, crashed changes will be re-submitted if crash count limit is reached, otherwise they will be abandoned.
     * 
     * @param mResendCrashedUpdates true or false.
     */
    public void setResendCrashedUpdates(boolean mResendCrashedUpdates) {
        this.mResendCrashedUpdates = mResendCrashedUpdates;
    }

    /**
     * JSON representation of the configuration object
     * 
     * @return The JSON object
     */
    public JSONObject getJSON() {
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

    /**
     * Create a new configuration object from JSON.
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
