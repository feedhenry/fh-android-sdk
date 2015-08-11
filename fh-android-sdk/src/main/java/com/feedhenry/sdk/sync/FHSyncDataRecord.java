/**
 * Copyright (c) 2015 FeedHenry Ltd, All Rights Reserved.
 *
 * Please refer to your contract with FeedHenry for the software license agreement.
 * If you do not have a contract, you do not have a license to use this software.
 */
package com.feedhenry.sdk.sync;

import org.json.fh.JSONObject;

public class FHSyncDataRecord {

    private String hashValue;

    private JSONObject data;

    private String uid;

    private static final String KEY_HASH = "hashValue";

    private static final String KEY_DATA = "data";

    private static final String KEY_UID = "uid";

    public FHSyncDataRecord() {

    }

    public FHSyncDataRecord(JSONObject pData) {
        setData(pData);
    }

    public FHSyncDataRecord(String pUid, JSONObject pData) {
        this.uid = pUid;
        setData(pData);
    }

    public String getHashValue() {
        return hashValue;
    }

    public JSONObject getData() {
        return data;
    }

    public String getUid() {
        return uid;
    }

    public void setData(JSONObject pData) {
        data = new JSONObject(pData.toString());
        hashValue = FHSyncUtils.generateObjectHash(data);
    }

    public void setUid(String pUid) {
        this.uid = pUid;
    }

    public void setHashValue(String pHashValue) {
        this.hashValue = pHashValue;
    }

    public JSONObject getJSON() {
        JSONObject ret = new JSONObject();
        if (this.uid != null) {
            ret.put(KEY_UID, this.uid);
        }
        if (this.data != null) {
            ret.put(KEY_DATA, this.data);
        }
        if (this.hashValue != null) {
            ret.put(KEY_HASH, this.hashValue);
        }
        return ret;
    }

    public String toString() {
        return this.getJSON().toString();
    }

    public boolean equals(Object pThat) {
        if (this == pThat) {
            return true;
        }

        if (pThat instanceof FHSyncDataRecord) {
            FHSyncDataRecord that = (FHSyncDataRecord) pThat;
            return this.getHashValue().equals(that.getHashValue());
        }
        return false;
    }

    public FHSyncDataRecord clone() {
        JSONObject jsonObj = this.getJSON();
        return FHSyncDataRecord.fromJSON(jsonObj);
    }

    public static FHSyncDataRecord fromJSON(JSONObject pObj) {
        FHSyncDataRecord record = new FHSyncDataRecord();
        if (pObj.has(KEY_UID)) {
            record.setUid(pObj.getString(KEY_UID));
        }
        if (pObj.has(KEY_DATA)) {
            record.setData(pObj.getJSONObject(KEY_DATA));
        }
        if (pObj.has(KEY_HASH)) {
            record.setHashValue(pObj.getString(KEY_HASH));
        }
        return record;
    }
}
