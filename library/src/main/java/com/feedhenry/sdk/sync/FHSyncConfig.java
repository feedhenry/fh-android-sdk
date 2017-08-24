/**
 * Copyright Red Hat, Inc, and individual contributors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.feedhenry.sdk.sync;

import android.support.annotation.NonNull;
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

    private FHSyncEvent mOnSyncStarted;
    private FHSyncEvent mOnSyncCompleted;
    private FHSyncEvent mOnUpdateOffline;
    private FHSyncEvent mOnCollisionDetected;
    private FHSyncEvent mOnRemoteUpdateFailed;
    private FHSyncEvent mOnRemoteUpdateApplied;
    private FHSyncEvent mOnLocalUpdateApplied;
    private FHSyncEvent mOnDeltaReceived;
    private FHSyncEvent mOnSyncFailed;
    private FHSyncEvent mOnClientStorageFailed;

    final FHSyncListener mSyncListener = new FHSyncListener() {

        @Override
        public void onSyncStarted(NotificationMessage pMessage) {
            if (mOnSyncStarted != null) {
                mOnSyncStarted.event(pMessage);
            }
        }

        @Override
        public void onSyncCompleted(NotificationMessage pMessage) {
            if (mOnSyncCompleted != null) {
                mOnSyncCompleted.event(pMessage);
            }
        }

        @Override
        public void onUpdateOffline(NotificationMessage pMessage) {
            if (mOnUpdateOffline != null) {
                mOnUpdateOffline.event(pMessage);
            }
        }

        @Override
        public void onCollisionDetected(NotificationMessage pMessage) {
            if (mOnCollisionDetected != null) {
                mOnCollisionDetected.event(pMessage);
            }
        }

        @Override
        public void onRemoteUpdateFailed(NotificationMessage pMessage) {
            if (mOnRemoteUpdateFailed != null) {
                mOnRemoteUpdateFailed.event(pMessage);
            }
        }

        @Override
        public void onRemoteUpdateApplied(NotificationMessage pMessage) {
            if (mOnRemoteUpdateApplied != null) {
                mOnRemoteUpdateApplied.event(pMessage);
            }
        }

        @Override
        public void onLocalUpdateApplied(NotificationMessage pMessage) {
            if (mOnLocalUpdateApplied != null) {
                mOnLocalUpdateApplied.event(pMessage);
            }
        }

        @Override
        public void onDeltaReceived(NotificationMessage pMessage) {
            if (mOnDeltaReceived != null) {
                mOnDeltaReceived.event(pMessage);
            }
        }

        @Override
        public void onSyncFailed(NotificationMessage pMessage) {
            if (mOnSyncFailed != null) {
                mOnSyncFailed.event(pMessage);
            }
        }

        @Override
        public void onClientStorageFailed(NotificationMessage pMessage) {
            if (mOnClientStorageFailed != null) {
                mOnClientStorageFailed.event(pMessage);
            }
        }
    };

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
    @Deprecated
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
    @Deprecated
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
    @Deprecated
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
    @Deprecated
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
    @Deprecated
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
    @Deprecated
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
    @Deprecated
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
    @Deprecated
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
    @Deprecated
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
    @Deprecated
    public void setNotifySyncFailed(boolean pNotifySyncFailed) {
        this.mNotifySyncFailed = pNotifySyncFailed;
    }

    /**
     * Sets if the sync client should notify on a client storage failed event.
     *
     * @param pNotifyClientStorageFailed whether to notify on client storage failed
     */
    @Deprecated
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
    @Deprecated
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
    @Deprecated
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
    @Deprecated
    public void setResendCrashedUpdates(boolean mResendCrashedUpdates) {
        this.mResendCrashedUpdates = mResendCrashedUpdates;
    }

    /**
     * Set if legacy mode is used
     *
     * @param mUseCustomSync
     */
    @Deprecated
    public void setUseCustomSync(boolean mUseCustomSync) {
        this.mUseCustomSync = mUseCustomSync;
    }

    /**
     * Check if legacy mode is enabled
     *
     * @return
     */
    public boolean useCustomSync() {
        return this.mUseCustomSync;
    }

    ;

    /**
     * Deprecated constructor, use {@link FHSyncConfig.Builder} instead.
     */
    public FHSyncConfig() {

    }

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
     *
     * @return the new sync config object
     */
    @Deprecated
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

    @Deprecated
    public FHSyncConfig clone() {
        JSONObject json = this.getJSON();
        return FHSyncConfig.fromJSON(json);
    }

    /**
     * Class for creating FHSyncConfig.
     */
    public static class Builder {

        private final FHSyncConfig mInstance = new FHSyncConfig();

        /**
         * Sets the sync interval in seconds.
         *
         * @param pSyncFrequencySeconds the new sync interval
         */
        public Builder syncFrequencySeconds(int pSyncFrequencySeconds) {
            mInstance.mSyncFrequencySeconds = pSyncFrequencySeconds;
            return this;
        }

        /**
         * Sets if the sync client should automatically update on local changes.
         *
         * @param pAutoSyncLocalUpdates whether local changes should automatically sync
         */
        public Builder autoSyncLocalUpdates(boolean pAutoSyncLocalUpdates) {
            mInstance.mAutoSyncLocalUpdates = pAutoSyncLocalUpdates;
            return this;
        }

        /**
         * Sets the maximum crash count number.
         * Changes may fail to be applied (crash) due to various reasons (e.g., network issues).
         * If the crash count reaches this limit, the changes will be either re-submitted or abandoned.
         *
         * @param pCrashCountWait the crash limit
         */
        public Builder crashCountWait(int pCrashCountWait) {
            mInstance.mCrashCountWait = pCrashCountWait;
            return this;
        }

        /**
         * Sets whether changes should be re-submitted once the crash limit is reached.
         * If false, changes will be discarded.
         *
         * @param pResendCrashedUpdates true or false.
         */
        public Builder resendCrashedUpdates(boolean pResendCrashedUpdates) {
            mInstance.mResendCrashedUpdates = pResendCrashedUpdates;
            return this;
        }

        /**
         * Set if legacy mode is used
         *
         * @param pUseCustomSync
         */
        public Builder useCustomSync(boolean pUseCustomSync) {
            mInstance.mUseCustomSync = pUseCustomSync;
            return this;
        }

        /**
         * Sets event which is invoked when a sync loop start event is emitted
         *
         * @param pOnSyncStarted event handler
         */
        public Builder onSyncStarted(@NonNull FHSyncEvent pOnSyncStarted) {
            mInstance.mOnSyncStarted = pOnSyncStarted;
            return this;
        }

        /**
         * Sets event which is invoked when a sync loop complete event is emitted
         *
         * @param pOnSyncCompleted event handler
         */
        public Builder onSyncCompleted(FHSyncEvent pOnSyncCompleted) {
            mInstance.mOnSyncCompleted = pOnSyncCompleted;
            return this;
        }

        /**
         * Sets event which is invoked when a offline update event is emitted.
         *
         * @param pOUpdateOffline event handler
         */
        public Builder onUpdateOffline(FHSyncEvent pOUpdateOffline) {
            mInstance.mOnUpdateOffline = pOUpdateOffline;
            return this;
        }

        /**
         * Sets event which is invoked when a collision event is emitted.
         *
         * @param pOnCollisionDetected event handler
         */
        public Builder onCollisionDetected(FHSyncEvent pOnCollisionDetected) {
            mInstance.mOnCollisionDetected = pOnCollisionDetected;
            return this;
        }

        /**
         * Sets event which is invoked when a remote update failed event is emitted.
         *
         * @param pOnRemoteUpdateFailed event handler
         */
        public Builder onRemoteUpdateFailed(FHSyncEvent pOnRemoteUpdateFailed) {
            mInstance.mOnRemoteUpdateFailed = pOnRemoteUpdateFailed;
            return this;
        }

        /**
         * Sets event which is invoked when a remote update event is emitted.
         *
         * @param pOnRemoteUpdateApplied event handler
         */
        public Builder onRemoteUpdateApplied(FHSyncEvent pOnRemoteUpdateApplied) {
            mInstance.mOnRemoteUpdateApplied = pOnRemoteUpdateApplied;
            return this;
        }

        /**
         * Sets event which is invoked when a local update applied event is emitted.
         *
         * @param pOnLocalUpdateApplied event handler
         */
        public Builder onLocalUpdateApplied(FHSyncEvent pOnLocalUpdateApplied) {
            mInstance.mOnLocalUpdateApplied = pOnLocalUpdateApplied;
            return this;
        }

        /**
         * Sets event which is invoked when a delta received event is emitted.
         *
         * @param pOnDeltaReceived event handler
         */
        public Builder onDeltaReceived(FHSyncEvent pOnDeltaReceived) {
            mInstance.mOnDeltaReceived = pOnDeltaReceived;
            return this;
        }

        /**
         * Sets event which is invoked when a sync failed event is emitted.
         *
         * @param pOnSyncFailed event handler
         */
        public Builder onSyncFailed(FHSyncEvent pOnSyncFailed) {
            mInstance.mOnSyncFailed = pOnSyncFailed;
            return this;
        }

        /**
         * Sets event which is invoked when a client storage failed event is emitted.
         *
         * @param pOnClientStorageFailed event handler
         */
        public Builder onClientStorageFailed(FHSyncEvent pOnClientStorageFailed) {
            mInstance.mOnClientStorageFailed = pOnClientStorageFailed;
            return this;
        }

        /**
         * Sets builder from JSON config.
         *
         * @param pObj the sync config JSON
         */
        public Builder fromJSON(JSONObject pObj) {
            mInstance.mSyncFrequencySeconds = pObj.optInt(KEY_SYNC_FREQUENCY);
            mInstance.mAutoSyncLocalUpdates = pObj.optBoolean(KEY_AUTO_SYNC_UPDATES);
            mInstance.mNotifyClientStorageFailed = pObj.optBoolean(KEY_NOTIFY_CLIENT_STORAGE_FAILED);
            mInstance.mNotifyDeltaReceived = pObj.optBoolean(KEY_NOTIFY_DELTA_RECEIVED);
            mInstance.mNotifyOfflineUpdate = pObj.optBoolean(KEY_NOTIFY_OFFLINE_UPDATED);
            mInstance.mNotifySyncCollisions = pObj.optBoolean(KEY_NOTIFY_SYNC_COLLISION);
            mInstance.mNotifySyncComplete = pObj.optBoolean(KEY_NOTIFY_SYNC_COMPLETED);
            mInstance.mNotifySyncStarted = pObj.optBoolean(KEY_NOTIFY_SYNC_STARTED);
            mInstance.mNotifyRemoteUpdateApplied = pObj.optBoolean(KEY_NOTIFY_REMOTE_UPDATED_APPLIED);
            mInstance.mNotifyLocalUpdateApplied = pObj.optBoolean(KEY_NOTIFY_LOCAL_UPDATE_APPLIED);
            mInstance.mNotifyRemoteUpdateFailed = pObj.optBoolean(KEY_NOTIFY_REMOTE_UPDATED_FAILED);
            mInstance.mNotifySyncFailed = pObj.optBoolean(KEY_NOTIFY_SYNC_FAILED);
            mInstance.mCrashCountWait = pObj.optInt(KEY_CRASHCOUNT, 10);
            mInstance.mResendCrashedUpdates = pObj.optBoolean(KEY_RESEND_CRASH);
            return this;
        }

        public FHSyncConfig build() {
            mInstance.mNotifyClientStorageFailed = (mInstance.mOnClientStorageFailed != null);
            mInstance.mNotifyDeltaReceived = (mInstance.mOnDeltaReceived != null);
            mInstance.mNotifyOfflineUpdate = (mInstance.mOnUpdateOffline != null);
            mInstance.mNotifySyncCollisions = (mInstance.mOnCollisionDetected != null);
            mInstance.mNotifySyncComplete = (mInstance.mOnSyncCompleted != null);
            mInstance.mNotifySyncStarted = (mInstance.mOnSyncStarted != null);
            mInstance.mNotifyRemoteUpdateApplied = (mInstance.mOnRemoteUpdateApplied != null);
            mInstance.mNotifyLocalUpdateApplied = (mInstance.mOnLocalUpdateApplied != null);
            mInstance.mNotifyRemoteUpdateFailed = (mInstance.mOnRemoteUpdateFailed != null);
            mInstance.mNotifySyncFailed = (mInstance.mOnSyncFailed != null);
            return mInstance;
        }
    }

}
