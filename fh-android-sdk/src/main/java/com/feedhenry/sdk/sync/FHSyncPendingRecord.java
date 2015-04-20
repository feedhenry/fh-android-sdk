/**
 * Copyright (c) 2014 FeedHenry Ltd, All Rights Reserved.
 *
 * Please refer to your contract with FeedHenry for the software license agreement.
 * If you do not have a contract, you do not have a license to use this software.
 */
package com.feedhenry.sdk.sync;

import java.util.Date;

import org.json.fh.JSONObject;

public class FHSyncPendingRecord {

    private boolean inFight;

    private Date inFlightDate;

    private boolean crashed;

    private String action;

    private long timestamp;

    private String uid;

    private FHSyncDataRecord preData;

    private FHSyncDataRecord postData;

    private String hashValue;

    private int crashedCount;

    private static final String KEY_INFLIGHT = "inFlight";

    private static final String KEY_ACTION = "action";

    private static final String KEY_TIMESTAMP = "timestamp";

    private static final String KEY_UID = "uid";

    private static final String KEY_PRE = "pre";

    private static final String KEY_PRE_HASH = "preHash";

    private static final String KEY_POST = "post";

    private static final String KEY_POST_HASH = "postHash";

    private static final String KEY_INFLIGHT_DATE = "inFlightDate";

    private static final String KEY_CRASHED = "crashed";

    private static final String KEY_HASH = "hash";

    public FHSyncPendingRecord() {
        this.timestamp = new Date().getTime();
    }

    public JSONObject getJSON() {
        JSONObject ret = new JSONObject();
        ret.put(KEY_INFLIGHT, this.inFight);
        ret.put(KEY_CRASHED, this.crashed);
        ret.put(KEY_TIMESTAMP, this.timestamp);
        if (null != this.inFlightDate) {
            ret.put(KEY_INFLIGHT_DATE, this.inFlightDate.getTime());
        }
        if (null != this.action) {
            ret.put(KEY_ACTION, this.action);
        }
        if (null != this.uid) {
            ret.put(KEY_UID, this.uid);
        }
        if (null != this.preData) {
            ret.put(KEY_PRE, this.preData.getData());
            ret.put(KEY_PRE_HASH, this.preData.getHashValue());
        }
        if (null != this.postData) {
            ret.put(KEY_POST, this.postData.getData());
            ret.put(KEY_POST_HASH, this.postData.getHashValue());
        }
        return ret;
    }

    public static FHSyncPendingRecord fromJSON(JSONObject pObj) {
        FHSyncPendingRecord record = new FHSyncPendingRecord();
        if (pObj.has(KEY_INFLIGHT)) {
            record.setInFight(pObj.getBoolean(KEY_INFLIGHT));
        }
        if (pObj.has(KEY_INFLIGHT_DATE)) {
            record.setInFlightDate(new Date(pObj.getLong(KEY_INFLIGHT_DATE)));
        }
        if (pObj.has(KEY_CRASHED)) {
            record.setCrashed(pObj.getBoolean(KEY_CRASHED));
        }
        if (pObj.has(KEY_TIMESTAMP)) {
            record.setTimestamp(pObj.getLong(KEY_TIMESTAMP));
        }
        if (pObj.has(KEY_ACTION)) {
            record.setAction(pObj.getString(KEY_ACTION));
        }
        if (pObj.has(KEY_UID)) {
            record.setUid(pObj.getString(KEY_UID));
        }
        if (pObj.has(KEY_PRE)) {
            FHSyncDataRecord preData = new FHSyncDataRecord();
            preData.setData(pObj.getJSONObject(KEY_PRE));
            preData.setHashValue(pObj.getString(KEY_PRE_HASH));
            record.setPreData(preData);
        }
        if (pObj.has(KEY_POST)) {
            FHSyncDataRecord postData = new FHSyncDataRecord();
            postData.setData(pObj.getJSONObject(KEY_POST));
            postData.setHashValue(pObj.getString(KEY_POST_HASH));
            record.setPostData(postData);
        }
        return record;
    }

    public boolean equals(Object pThat) {
        if (pThat instanceof FHSyncPendingRecord) {
            FHSyncPendingRecord that = (FHSyncPendingRecord) pThat;
            if (this.getHashValue().equals(that.getHashValue())) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    public String toString() {
        return this.getJSON().toString();
    }

    public boolean isInFight() {
        return inFight;
    }

    public void setInFight(boolean inFight) {
        this.inFight = inFight;
    }

    public Date getInFlightDate() {
        return inFlightDate;
    }

    public void setInFlightDate(Date inFlightDate) {
        this.inFlightDate = inFlightDate;
    }

    public boolean isCrashed() {
        return crashed;
    }

    public void setCrashed(boolean crashed) {
        this.crashed = crashed;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public FHSyncDataRecord getPreData() {
        return preData;
    }

    public void setPreData(FHSyncDataRecord preData) {
        this.preData = preData;
    }

    public FHSyncDataRecord getPostData() {
        return postData;
    }

    public void setPostData(FHSyncDataRecord postData) {
        this.postData = postData;
    }

    public String getHashValue() {
        if (null == this.hashValue) {
            JSONObject jsonobj = this.getJSON();
            this.hashValue = FHSyncUtils.generateObjectHash(jsonobj);
        }
        return this.hashValue;
    }

    public void setHashValue(String hashValue) {
        this.hashValue = hashValue;
    }

    public int getCrashedCount() {
        return crashedCount;
    }

    public void incrementCrashCount() {
        crashedCount++;
    }

    public void setCrashedCount(int crashedCount) {
        this.crashedCount = crashedCount;
    }
}
