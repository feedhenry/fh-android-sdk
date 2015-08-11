/**
 * Copyright (c) 2015 FeedHenry Ltd, All Rights Reserved.
 *
 * Please refer to your contract with FeedHenry for the software license agreement.
 * If you do not have a contract, you do not have a license to use this software.
 */
package com.feedhenry.sdk.sync;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

public class FHSyncNotificationHandler extends Handler {

    private FHSyncListener mSyncListener;

    public FHSyncNotificationHandler(FHSyncListener pListener) {
        super();
        mSyncListener = pListener;
    }

    public FHSyncNotificationHandler(Looper pLooper, FHSyncListener pListener) {
        super(pLooper);
        mSyncListener = pListener;
    }

    public void setSyncListener(FHSyncListener pListener) {
        mSyncListener = pListener;
    }

    public void handleMessage(Message pMsg) {
        NotificationMessage notification = (NotificationMessage) pMsg.obj;
        if (mSyncListener != null) {
            switch (pMsg.what) {
                case NotificationMessage.SYNC_STARTED_CODE:
                    mSyncListener.onSyncStarted(notification);
                    break;
                case NotificationMessage.SYNC_COMPLETE_CODE:
                    mSyncListener.onSyncCompleted(notification);
                    break;
                case NotificationMessage.OFFLINE_UPDATE_CODE:
                    mSyncListener.onUpdateOffline(notification);
                    break;
                case NotificationMessage.COLLISION_DETECTED_CODE:
                    mSyncListener.onCollisionDetected(notification);
                    break;
                case NotificationMessage.REMOTE_UPDATE_FAILED_CODE:
                    mSyncListener.onRemoteUpdateFailed(notification);
                    break;
                case NotificationMessage.REMOTE_UPDATE_APPLIED_CODE:
                    mSyncListener.onRemoteUpdateApplied(notification);
                    break;
                case NotificationMessage.LOCAL_UPDATE_APPLIED_CODE:
                    mSyncListener.onLocalUpdateApplied(notification);
                    break;
                case NotificationMessage.DELTA_RECEIVED_CODE:
                    mSyncListener.onDeltaReceived(notification);
                    break;
                case NotificationMessage.SYNC_FAILED_CODE:
                    mSyncListener.onSyncFailed(notification);
                    break;
                case NotificationMessage.CLIENT_STORAGE_FAILED_CODE:
                    mSyncListener.onClientStorageFailed(notification);
                default:
                    break;
            }
        }
    }
}
