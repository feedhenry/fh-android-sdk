package com.feedhenry.sdk.sync;

import org.json.JSONObject;

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
    if (null != this.uid) {
      ret.put(KEY_UID, this.uid);
    }
    if (null != this.data) {
      ret.put(KEY_DATA, this.data);
    }
    if (null != this.hashValue) {
      ret.put(KEY_HASH, this.hashValue);
    }
    return ret;
  }

  public String toString() {
    return this.getJSON().toString();
  }

  public boolean equals(Object pThat) {
    if (pThat instanceof FHSyncDataRecord) {
      FHSyncDataRecord that = (FHSyncDataRecord) pThat;
      if (this.getHashValue().equals(that.getHashValue())) {
        return true;
      } else {
        return false;
      }
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
