/**
 * Copyright (c) 2015 FeedHenry Ltd, All Rights Reserved.
 *
 * Please refer to your contract with FeedHenry for the software license agreement.
 * If you do not have a contract, you do not have a license to use this software.
 */
package com.feedhenry.sdk.sync;

import java.util.HashMap;
import java.util.Map;

/**
 * The message object sent to the listener when an event happened.
 */
public class NotificationMessage {

    public static final int SYNC_STARTED_CODE = 0;
    public static final int SYNC_COMPLETE_CODE = 1;
    public static final int OFFLINE_UPDATE_CODE = 2;
    public static final int COLLISION_DETECTED_CODE = 3;
    public static final int REMOTE_UPDATE_FAILED_CODE = 4;
    public static final int REMOTE_UPDATE_APPLIED_CODE = 5;
    public static final int DELTA_RECEIVED_CODE = 6;
    public static final int CLIENT_STORAGE_FAILED_CODE = 7;
    public static final int SYNC_FAILED_CODE = 8;
    public static final int LOCAL_UPDATE_APPLIED_CODE = 9;

    public static final String SYNC_STARTED_MESSAGE = "SYNC_STARTED";
    public static final String SYNC_COMPLETE_MESSAGE = "SYNC_COMPLETE";
    public static final String OFFLINE_UPDATE_MESSAGE = "OFFLINE_UPDATE";
    public static final String COLLISION_DETECTED_MESSAGE = "COLLISION_DETECTED";
    public static final String REMOTE_UPDATE_FAILED_MESSAGE = "REMOTE_UPDATE_FAILED";
    public static final String REMOTE_UPDATE_APPLIED_MESSAGE = "REMOTE_UPDATE_APPLIED";
    public static final String LOCAL_UPDATE_APPLIED_MESSAGE = "LOCAL_UPDATE_APPLIED";
    public static final String DELTA_RECEIVED_MESSAGE = "DELTA_RECEIVED";
    public static final String CLIENT_STORAGE_FAILED_MESSAGE = "CLIENT_STORAGE_FAILED";
    public static final String SYNC_FAILED_MESSAGE = "SYNC_FAILED";

    private static Map<Integer, String> mMessageMap = new HashMap<Integer, String>();

    static {
        mMessageMap.put(SYNC_STARTED_CODE, SYNC_STARTED_MESSAGE);
        mMessageMap.put(SYNC_COMPLETE_CODE, SYNC_COMPLETE_MESSAGE);
        mMessageMap.put(OFFLINE_UPDATE_CODE, OFFLINE_UPDATE_MESSAGE);
        mMessageMap.put(COLLISION_DETECTED_CODE, COLLISION_DETECTED_MESSAGE);
        mMessageMap.put(REMOTE_UPDATE_FAILED_CODE, REMOTE_UPDATE_FAILED_MESSAGE);
        mMessageMap.put(REMOTE_UPDATE_APPLIED_CODE, REMOTE_UPDATE_APPLIED_MESSAGE);
        mMessageMap.put(LOCAL_UPDATE_APPLIED_CODE, LOCAL_UPDATE_APPLIED_MESSAGE);
        mMessageMap.put(DELTA_RECEIVED_CODE, DELTA_RECEIVED_MESSAGE);
        mMessageMap.put(CLIENT_STORAGE_FAILED_CODE, CLIENT_STORAGE_FAILED_MESSAGE);
        mMessageMap.put(SYNC_FAILED_CODE, SYNC_FAILED_MESSAGE);
    }

    private String mDataId;
    private String mUID;
    private String mCodeMessage;
    private String mExtraMessage;

    public NotificationMessage(String pDataId, String pUID, String pCodeMessage, String pExtraMessage) {
        this.mDataId = pDataId;
        this.mUID = pUID;
        this.mCodeMessage = pCodeMessage;
        this.mExtraMessage = pExtraMessage;
    }

    /**
     * The id of the dataset associated with the event
     *
     * @return the id of the dataset associated with the event
     */
    public String getDataId() {
        return mDataId;
    }

    /**
     * The id of the data record associated with the event
     *
     * @return the id of the data record associated with the event
     */
    public String getUID() {
        return mUID;
    }

    /**
     * The code message associated with the event
     *
     * @return the code message associated with the event
     */
    public String getCode() {
        return mCodeMessage;
    }

    /**
     * Extra message associated with the event
     *
     * @return the extra message associated with the event
     */
    public String getMessage() {
        return mExtraMessage;
    }

    @Override
    public String toString() {
        return "DataId:"
            + mDataId
            + "-UID:"
            + mUID
            + "-Code:"
            + mCodeMessage
            + "-Message:"
            + mExtraMessage;
    }

    public static NotificationMessage getMessage(String pDatasetId, String pUid, int pCode, String pMessage) {
        return new NotificationMessage(pDatasetId, pUid, mMessageMap.get(pCode), pMessage);
    }
}
