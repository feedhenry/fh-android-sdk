/**
 * Copyright (c) 2015 FeedHenry Ltd, All Rights Reserved.
 *
 * Please refer to your contract with FeedHenry for the software license agreement.
 * If you do not have a contract, you do not have a license to use this software.
 */
package com.feedhenry.sdk.sync;

import android.content.Context;
import android.os.Message;
import android.util.Log;
import com.feedhenry.sdk.FH;
import com.feedhenry.sdk.FHActCallback;
import com.feedhenry.sdk.FHRemote;
import com.feedhenry.sdk.FHResponse;
import com.feedhenry.sdk.utils.FHLog;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.json.fh.JSONArray;
import org.json.fh.JSONException;
import org.json.fh.JSONObject;

public class FHSyncDataset {

    private boolean mSyncRunning;
    private boolean mInitialised;
    private final String mDatasetId;
    private Date mSyncStart;
    private Date mSyncEnd;
    private boolean mSyncPending;
    private FHSyncConfig mSyncConfig = new FHSyncConfig();
    private final ConcurrentMap<String, FHSyncPendingRecord> mPendingRecords =
        new ConcurrentHashMap<>();
    
    private final ConcurrentMap<String, String> mUidMappings = new ConcurrentHashMap<>();
    private ConcurrentMap<String, FHSyncDataRecord> mDataRecords = new ConcurrentHashMap<>();
    
    private JSONObject mQueryParams = new JSONObject();
    private JSONObject mMetaData = new JSONObject();
    private JSONObject mCustomMetaData = new JSONObject();
    private String mHashvalue;
    private JSONArray mAcknowledgements = new JSONArray();
    private boolean mStopSync;

    private Context mContext;
    private FHSyncNotificationHandler mNotificationHandler;

    private static final String STORAGE_FILE_EXT = ".sync.json";

    private static final String KEY_DATE_SET_ID = "dataSetId";
    private static final String KEY_SYNC_LOOP_START = "syncLoopStart";
    private static final String KEY_SYNC_LOOP_END = "syncLoopEnd";
    private static final String KEY_SYNC_CONFIG = "syncConfig";
    private static final String KEY_PENDING_RECORDS = "pendingDataRecords";
    private static final String KEY_DATA_RECORDS = "dataRecords";
    private static final String KEY_HASHVALUE = "hashValue";
    private static final String KEY_ACKNOWLEDGEMENTS = "acknowledgements";
    private static final String KEY_QUERY_PARAMS = "queryParams";
    private static final String KEY_METADATA = "metaData";

    private static final String LOG_TAG = "com.feedhenry.sdk.sync.FHSyncDataset";

    public FHSyncDataset(
        Context pContext, FHSyncNotificationHandler pHandler, String pDatasetId,
        FHSyncConfig pConfig, JSONObject pQueryParams, JSONObject pMetaData) {
        mContext = pContext;
        mNotificationHandler = pHandler;
        mDatasetId = pDatasetId;
        mSyncConfig = pConfig;
        mQueryParams = pQueryParams;
        mCustomMetaData = pMetaData;
        readFromFile();
    }

    public JSONObject getJSON() {
        JSONObject ret = new JSONObject();
        if (mHashvalue != null) {
            ret.put(KEY_HASHVALUE, mHashvalue);
        }
        ret.put(KEY_DATE_SET_ID, mDatasetId);
        ret.put(KEY_SYNC_CONFIG, mSyncConfig.getJSON());
        JSONObject pendingJson = new JSONObject();
        for (String key : mPendingRecords.keySet()) {
            pendingJson.put(key, mPendingRecords.get(key).getJSON());
        }
        ret.put(KEY_PENDING_RECORDS, pendingJson);
        JSONObject dataJson = new JSONObject();
        for (String dkey : mDataRecords.keySet()) {
            dataJson.put(dkey, mDataRecords.get(dkey).getJSON());
        }
        ret.put(KEY_DATA_RECORDS, dataJson);
        if (this.mSyncStart != null) {
            ret.put(KEY_SYNC_LOOP_START, this.mSyncStart.getTime());
        }
        if (this.mSyncEnd != null) {
            ret.put(KEY_SYNC_LOOP_END, this.mSyncEnd.getTime());
        }
        ret.put(KEY_ACKNOWLEDGEMENTS, mAcknowledgements);
        ret.put(KEY_QUERY_PARAMS, mQueryParams);
        ret.put(KEY_METADATA, mMetaData);
        return ret;
    }

    public JSONObject listData() {
        JSONObject ret = new JSONObject();
        for (String key : this.mDataRecords.keySet()) {
            FHSyncDataRecord dataRecord = this.mDataRecords.get(key);
            JSONObject dataJson = new JSONObject();
            // return a copy of the data so that any changes made to the data will not affect the original data
            dataJson.put("data", new JSONObject(dataRecord.getData().toString()));
            dataJson.put("uid", key);
            ret.put(key, dataJson);
        }
        return ret;
    }

    public JSONObject readData(String pUid) {
        FHSyncDataRecord dataRecord = mDataRecords.get(pUid);
        if (dataRecord != null) {
            JSONObject ret = new JSONObject();
            // return a copy of the data so that any changes made to the data will not affect the original data
            ret.put("data", new JSONObject(dataRecord.getData().toString()));
            ret.put("uid", pUid);
            return ret;
        } else {
            return null;
        }
    }

    public JSONObject createData(JSONObject pData) {
        FHSyncPendingRecord pendingRecord = addPendingObject(null, pData, "create");
        FHSyncDataRecord dataRecord = mDataRecords.get(pendingRecord.getUid());
        JSONObject ret = new JSONObject();
        if (dataRecord != null) {
            ret.put("data", new JSONObject(dataRecord.getData().toString()));
            ret.put("uid", pendingRecord.getUid());
        }
        return ret;
    }

    public JSONObject updateData(String pUid, JSONObject pData) {
        addPendingObject(pUid, pData, "update");
        FHSyncDataRecord dataRecord = mDataRecords.get(pUid);
        JSONObject ret = new JSONObject();
        if (dataRecord != null) {
            ret.put("data", new JSONObject(dataRecord.getData().toString()));
            ret.put("uid", pUid);
        }
        return ret;
    }

    public JSONObject deleteData(String pUid) {
        FHSyncPendingRecord pendingRecord = addPendingObject(pUid, null, "delete");
        FHSyncDataRecord deleted = pendingRecord.getPreData();
        JSONObject ret = new JSONObject();
        if (deleted != null) {
            ret.put("data", new JSONObject(deleted.getData().toString()));
            ret.put("uid", pUid);
        }
        return ret;
    }

    public void startSyncLoop() {
        mSyncPending = false;
        mSyncRunning = true;
        mSyncStart = new Date();
        doNotify(null, NotificationMessage.SYNC_STARTED_CODE, null);
        if (!FH.isOnline()) {
            syncCompleteWithCode("offline");
        } else {
            JSONObject syncLoopParams = new JSONObject();
            syncLoopParams.put("fn", "sync");
            syncLoopParams.put("dataset_id", mDatasetId);
            syncLoopParams.put("meta_data", mCustomMetaData);
            syncLoopParams.put("query_params", mQueryParams);
            if (mHashvalue != null) {
                syncLoopParams.put("dataset_hash", mHashvalue);
            }
            syncLoopParams.put("acknowledgements", mAcknowledgements);
            JSONArray pendings = new JSONArray();
            for (String key : mPendingRecords.keySet()) {
                FHSyncPendingRecord pendingRecord = mPendingRecords.get(key);
                if (!pendingRecord.isInFlight() && !pendingRecord.isCrashed() && !pendingRecord.isDelayed()) {
                    pendingRecord.setInFlight(true);
                    pendingRecord.setInFlightDate(new Date());
                    JSONObject pendingJSON = pendingRecord.getJSON();
                    if ("create".equals(pendingRecord.getAction())) {
                        pendingJSON.put("hash", pendingRecord.getUid());
                    } else {
                        pendingJSON.put("hash", pendingRecord.getHashValue());
                    }
                    pendings.put(pendingJSON);
                }
            }

            syncLoopParams.put("pending", pendings);
            FHLog.d(LOG_TAG, "Starting sync loop -global hash = " + mHashvalue + " :: params = " + syncLoopParams);

            try {
                FHRemote actRequest = makeCloudRequest(syncLoopParams);
                actRequest.executeAsync(
                    new FHActCallback() {

                        @Override
                        public void success(FHResponse pResponse) {
                            JSONObject responseData = pResponse.getJson();
                            syncRequestSuccess(responseData);
                        }

                        @Override
                        public void fail(FHResponse pResponse) {
                            /*
                            The AJAX call failed to complete successfully, so the state of the current pending updates
                            is unknown. Mark them as "crashed". The next time a syncLoop completes successfully, we
                            will review the crashed records to see if we can determine their current state.
                            */
                            markInFlightAsCrashed();
                            FHLog.e(
                                LOG_TAG,
                                "syncLoop failed : msg = " + pResponse.getErrorMessage(),
                                pResponse.getError());
                            doNotify(null, NotificationMessage.SYNC_FAILED_CODE, pResponse.getRawResponse());
                            syncCompleteWithCode(pResponse.getRawResponse());
                        }
                    });
            } catch (Exception e) {
                FHLog.e(LOG_TAG, "Error performing sync", e);
                doNotify(null, NotificationMessage.SYNC_FAILED_CODE, e.getMessage());
                syncCompleteWithCode(e.getMessage());
            }
        }
    }

    private void syncRequestSuccess(JSONObject pData) {
        // Check to see if any previously crashed inflight records can now be resolved
        updateCrashedInFlightFromNewData(pData);
        updateDelayedFromNewData(pData);
        updateMetaFromNewData(pData);


        if (pData.has("updates")) {
            JSONArray ack = new JSONArray();
            JSONObject updates = pData.getJSONObject("updates");
            JSONObject applied = updates.optJSONObject("applied");
            checkUidChanges(applied);
            processUpdates(applied, NotificationMessage.REMOTE_UPDATE_APPLIED_CODE, ack);
            processUpdates(updates.optJSONObject("failed"), NotificationMessage.REMOTE_UPDATE_FAILED_CODE, ack);
            processUpdates(updates.optJSONObject("collisions"), NotificationMessage.COLLISION_DETECTED_CODE, ack);
            mAcknowledgements = ack;
        }

        if (pData.has("hash") && !pData.getString("hash").equals(mHashvalue)) {
            String remoteHash = pData.getString("hash");
            FHLog.d(
                LOG_TAG,
                "Local dataset stale - syncing records :: local hash= " + mHashvalue + " - remoteHash =" + remoteHash);
            // Different hash value returned - Sync individual records
            syncRecords();
        } else {
            FHLog.i(LOG_TAG, "Local dataset up to date");
        }

        syncCompleteWithCode("online");
    }

    private void syncRecords() {
        JSONObject clientRecords = new JSONObject();
        for (Map.Entry<String, FHSyncDataRecord> entry : mDataRecords.entrySet()) {
            clientRecords.put(entry.getKey(), entry.getValue().getHashValue());
        }

        JSONObject syncRecsParams = new JSONObject();
        syncRecsParams.put("fn", "syncRecords");
        syncRecsParams.put("dataset_id", mDatasetId);
        syncRecsParams.put("query_params", mQueryParams);
        syncRecsParams.put("meta_data", mCustomMetaData);
        syncRecsParams.put("clientRecs", clientRecords);

        FHLog.d(LOG_TAG, "syncRecParams :: " + syncRecsParams);

        try {
            FHRemote request = makeCloudRequest(syncRecsParams);
            request.executeAsync(
                new FHActCallback() {

                    @Override
                    public void success(FHResponse pResponse) {
                        syncRecordsSuccess(pResponse.getJson());
                    }

                    @Override
                    public void fail(FHResponse pResponse) {
                        FHLog.e(
                            LOG_TAG, "syncRecords failed: " + pResponse.getRawResponse(),
                            pResponse.getError());
                        doNotify(null, NotificationMessage.SYNC_FAILED_CODE, pResponse.getRawResponse());
                        syncCompleteWithCode(pResponse.getRawResponse());
                    }
                });
        } catch (Exception e) {
            FHLog.e(LOG_TAG, "error when running syncRecords", e);
            doNotify(null, NotificationMessage.SYNC_FAILED_CODE, e.getMessage());
            syncCompleteWithCode(e.getMessage());
        }
    }

    private void syncRecordsSuccess(JSONObject pData) {
        applyPendingChangesToRecords(pData);
        handleCreated(pData);
        handleUpdated(pData);
        handleDeleted(pData);

        if (pData.has("hash")) {
            mHashvalue = pData.getString("hash");
        }

        syncCompleteWithCode("online");
    }

    private FHRemote makeCloudRequest(JSONObject pSyncLoopParams) throws Exception {
        FHRemote request = null;
        if(this.getSyncConfig().useCustomSync()){
            request = FH.buildActRequest(mDatasetId, pSyncLoopParams);
        } else {
            request = FH.buildCloudRequest("/mbaas/sync/" + mDatasetId, "POST", null, pSyncLoopParams);
        }
        return request;
    }

    private void handleDeleted(JSONObject pData) {
        JSONObject deleted = pData.optJSONObject("delete");
        if (deleted != null) {
            for (Iterator<String> it = deleted.keys(); it.hasNext(); ) {
                String key = it.next();
                mDataRecords.remove(key);
                doNotify(key, NotificationMessage.DELTA_RECEIVED_CODE, "delete");
            }
        }
    }

    private void handleUpdated(JSONObject pData) {
        JSONObject dataUpdated = pData.optJSONObject("update");
        if (dataUpdated != null) {
            for (Iterator<String> it = dataUpdated.keys(); it.hasNext(); ) {
                String key = it.next();
                JSONObject obj = dataUpdated.getJSONObject(key);
                FHSyncDataRecord rec = mDataRecords.get(key);
                if (rec != null) {
                    rec.setData(obj.getJSONObject("data"));
                    rec.setHashValue(obj.getString("hash"));
                    mDataRecords.put(key, rec);
                    doNotify(key, NotificationMessage.DELTA_RECEIVED_CODE, "update");
                }

            }
        }
    }

    private void handleCreated(JSONObject pData) {
        JSONObject created = pData.optJSONObject("create");
        if (created != null) {
            for (Iterator<String> it = created.keys(); it.hasNext(); ) {
                String key = it.next();
                
                JSONObject obj = created.getJSONObject(key);
                FHSyncDataRecord record = new FHSyncDataRecord(obj.getJSONObject("data"));
                record.setHashValue(obj.getString("hash"));
                mDataRecords.put(key, record);
                doNotify(key, NotificationMessage.DELTA_RECEIVED_CODE, "create");

            }
        }
    }

    private void processUpdates(JSONObject pUpdates, int pNotification, JSONArray pAck) {
        if (pUpdates != null) {
            for (Iterator<String> it = pUpdates.keys(); it.hasNext(); ) {
                String key = it.next();
                JSONObject up = pUpdates.getJSONObject(key);
                pAck.put(up);
                FHSyncPendingRecord pendingRec = mPendingRecords.get(key);
                if (pendingRec != null && pendingRec.isInFlight() && !pendingRec.isCrashed()) {
                    mPendingRecords.remove(key);
                    doNotify(up.getString("uid"), pNotification, up.toString());
                }
            }
        }
    }
    
    private void updateCrashedInFlightFromNewData(JSONObject remoteData) {
        JSONObject updateNotifications = new JSONObject();
        updateNotifications.put("applied", NotificationMessage.REMOTE_UPDATE_APPLIED_CODE);
        updateNotifications.put("failed", NotificationMessage.REMOTE_UPDATE_FAILED_CODE);
        updateNotifications.put("collisions", NotificationMessage.COLLISION_DETECTED_CODE);

        JSONObject resolvedCrashed = new JSONObject();
        List<String> keysToRemove = new ArrayList<String>();

        for ( Map.Entry<String, FHSyncPendingRecord> pendingRecordEntry : mPendingRecords.entrySet()) {
            FHSyncPendingRecord pendingRecord = pendingRecordEntry.getValue();
            String pendingHash = pendingRecordEntry.getKey();
            if (pendingRecord.isInFlight() && pendingRecord.isCrashed()) {
                Log.d(LOG_TAG, 
                        String.format("updateCrashedInFlightFromNewData - Found crashed inFlight pending record uid= %s :: hash %s", pendingRecord.getUid(), pendingRecord.getHashValue()));
                if (remoteData != null && remoteData.has("updates") && remoteData.getJSONObject("updates").has("hashes")) {
                    JSONObject hashes = remoteData.getJSONObject("updates").getJSONObject("hashes");
                    JSONObject crashedUpdate = hashes.optJSONObject(pendingHash);
                    if (crashedUpdate != null) {
                        resolvedCrashed.put(crashedUpdate.getString("uid"), crashedUpdate);
                        Log.d(LOG_TAG, "updateCrashedInFlightFromNewData - Resolving status for crashed inflight pending record " + crashedUpdate.toString());
                        String crashedType = crashedUpdate.optString("type");
                        String crashedAction = crashedUpdate.optString("action");
                        
                        if (crashedType != null && crashedType.equals("failed")) {
                            // Crashed updated failed - revert local dataset
                            if (crashedAction != null && crashedAction.equals("create")) {
                                Log.d(LOG_TAG,"updateCrashedInFlightFromNewData - Deleting failed create from dataset");
                                this.mDataRecords.remove(crashedUpdate.get("uid"));
                            } else if (crashedAction != null && (crashedAction.equals("update") ||
                                                         crashedAction.equals("delete"))) {
                                Log.d(LOG_TAG,"updateCrashedInFlightFromNewData - Reverting failed %@ in dataset" + crashedAction);
                                this.mDataRecords.put(crashedUpdate.getString("uid"), pendingRecord.getPreData());
                            }
                        }
                        
                    keysToRemove.add(pendingHash);
                    if ("applied".equals(crashedUpdate.opt("type"))) {
                        doNotify(crashedUpdate.getString("uid"), NotificationMessage.REMOTE_UPDATE_APPLIED_CODE, crashedUpdate.toString());
                    } else if ("applied".equals(crashedUpdate.opt("type"))) {
                        doNotify(crashedUpdate.getString("uid"), NotificationMessage.REMOTE_UPDATE_FAILED_CODE, crashedUpdate.toString());
                    } else if ("collisions".equals(crashedUpdate.opt("type"))) {
                        doNotify(crashedUpdate.getString("uid"), NotificationMessage.COLLISION_DETECTED_CODE, crashedUpdate.toString());
                    }
                    
                        
                    } else {
                        // No word on our crashed update - increment a counter to reflect another sync
                        // that did not give us
                        // any update on our crashed record.
                        pendingRecord.incrementCrashCount();
                    }
                } else {
                    // No word on our crashed update - increment a counter to reflect another sync that
                    // did not give us
                    // any update on our crashed record.
                    pendingRecord.incrementCrashCount();
                }
                
            }
            
        }
        for (String keyToRemove : keysToRemove) {
            this.mPendingRecords.remove(keyToRemove);
        }
        keysToRemove.clear();
        
        for ( Map.Entry<String, FHSyncPendingRecord> pendingRecordEntry : mPendingRecords.entrySet()) {
            FHSyncPendingRecord pendingRecord = pendingRecordEntry.getValue();
            String pendingHash = pendingRecordEntry.getKey();
            
            if (pendingRecord.isInFlight() && pendingRecord.isCrashed()) {
                if (pendingRecord.getCrashedCount() > mSyncConfig.getCrashCountWait()) {
                    Log.d(LOG_TAG, "updateCrashedInFlightFromNewData - Crashed inflight pending record has " +
                          "reached crashed_count_wait limit : " + 
                          pendingRecord);
                    if (mSyncConfig.isResendCrashedUpdates()) {
                        Log.d(LOG_TAG, "updateCrashedInFlightFromNewData - Retryig crashed inflight pending record");
                        pendingRecord.setCrashed(false);
                        pendingRecord.setInFlight(false);
                    } else {
                        Log.d(LOG_TAG, "updateCrashedInFlightFromNewData - Deleting crashed inflight pending record");
                        keysToRemove.add(pendingHash);
                    }
                }
            } else if (!pendingRecord.isInFlight() && pendingRecord.isCrashed()) {
                Log.d(LOG_TAG, "updateCrashedInFlightFromNewData - Trying to resolve issues with crashed non in flight record - uid =" + pendingRecord.getUid());
                // Stalled pending record because a previous pending update on the same record crashed
                JSONObject dict = resolvedCrashed.optJSONObject(pendingRecord.getUid());
                if (null != dict) {
                    Log.d(LOG_TAG, String.format("updateCrashedInFlightFromNewData - Found a stalled pending record backed " +
                          "up behind a resolved crash uid=%s :: hash=%s",
                          pendingRecord.getUid(), pendingRecord.getHashValue()));
                    pendingRecord.setCrashed(false);
                }
            }
            
        }
        
        for (String keyToRemove : keysToRemove) {
            this.mPendingRecords.remove(keyToRemove);
        }
        
        keysToRemove.clear();
    }




    private void markInFlightAsCrashed() {
        Map<String, FHSyncPendingRecord> crashedRecords = new HashMap<String, FHSyncPendingRecord>();
        for (Map.Entry<String, FHSyncPendingRecord> entry : mPendingRecords.entrySet()) {
            FHSyncPendingRecord pendingRecord = entry.getValue();
            String pendingHash = entry.getKey();
            if (pendingRecord.isInFlight()) {
                FHLog.d(LOG_TAG, "Marking in flight pending record as crashed : " + pendingHash);
                pendingRecord.setCrashed(true);
                crashedRecords.put(pendingRecord.getUid(), pendingRecord);
            }
        }

    }

    public void syncCompleteWithCode(String pCode) {
        mSyncRunning = false;
        mSyncEnd = new Date();
        writeToFile();
        doNotify(mHashvalue, NotificationMessage.SYNC_COMPLETE_CODE, pCode);
    }

    private FHSyncPendingRecord addPendingObject(String pUid, JSONObject pData, String pAction) {
        if (!FH.isOnline()) {
            doNotify(pUid, NotificationMessage.OFFLINE_UPDATE_CODE, pAction);
        }
        FHSyncPendingRecord pending = new FHSyncPendingRecord();
        pending.setInFlight(false);
        pending.setAction(pAction);

        if (pData != null) {
            FHSyncDataRecord dataRecord = new FHSyncDataRecord(pData);
            pending.setPostData(dataRecord);
        }

        if ("create".equalsIgnoreCase(pAction)) {
            pending.setUid(pending.getHashValue());
            storePendingObj(pending);
        } else {
            FHSyncDataRecord existingData = mDataRecords.get(pUid);
            if (existingData != null) {
                pending.setUid(pUid);
                pending.setPreData(existingData.clone());
                storePendingObj(pending);
            }
        }
        return pending;
    }

    private void storePendingObj(FHSyncPendingRecord pPendingObj) {
        mPendingRecords.put(pPendingObj.getHashValue(), pPendingObj);
        updateDatasetFromLocal(pPendingObj);
        if (mSyncConfig.isAutoSyncLocalUpdates()) {
            mSyncPending = true;
        }
        writeToFile();
        doNotify(
            pPendingObj.getUid(),
            NotificationMessage.LOCAL_UPDATE_APPLIED_CODE,
            pPendingObj.getAction());
    }

    private void updateDatasetFromLocal(FHSyncPendingRecord pPendingObj) {
        String previousPendingUid;
        FHSyncPendingRecord previousPendingObj;
        String uid = pPendingObj.getUid();
        String uidToSave = pPendingObj.getHashValue();
        FHLog.d(
            LOG_TAG,
            "updating local dataset for uid " + uid + " - action = " + pPendingObj.getAction());
        JSONObject metadata = mMetaData.optJSONObject(uid);
        if (metadata == null) {
            metadata = new JSONObject();
            mMetaData.put(uid, metadata);
        }
        FHSyncDataRecord existing = mDataRecords.get(uid);
        boolean fromPending = metadata.optBoolean("fromPending");
        
        if ("create".equalsIgnoreCase(pPendingObj.getAction())) {
            if (existing != null) {
                FHLog.d(LOG_TAG, "dataset already exists for uid for create :: " + existing.toString());
                if (fromPending) {
                    // We are trying to create on top of an existing pending record
                    // Remove the previous pending record and use this one instead
                    previousPendingUid = metadata.optString("pendingUid", null);
                    if (previousPendingUid != null) {
                        mPendingRecords.remove(previousPendingUid);
                    }
                }
            }
            mDataRecords.put(uid, new FHSyncDataRecord());
        }

        if ("update".equalsIgnoreCase(pPendingObj.getAction())) {
            if (existing != null) {
                if (fromPending) {
                    FHLog.d(
                        LOG_TAG,
                        "Updating an existing pending record for dataset :: " + existing.toString());
                    // We are trying to update an existing pending record
                    previousPendingUid = metadata.optString("pendingUid", null);
                    metadata.put("previousPendingUid", previousPendingUid);
                        if (previousPendingUid != null) {
                            previousPendingObj = mPendingRecords.get(previousPendingUid);
                            if (previousPendingObj != null) {
                                if (!previousPendingObj.isInFlight()) {
                                    FHLog.d(LOG_TAG, "existing pre-flight pending record = " + previousPendingObj);
                                    // We are trying to perform an update on an existing pending record
                                    // modify the original record to have the latest value and delete the pending update
                                    previousPendingObj.setPostData(pPendingObj.getPostData());
                                    mPendingRecords.remove(pPendingObj.getHashValue());
                                    uidToSave = previousPendingUid;
                                } else if (!previousPendingObj.getHashValue().equals(pPendingObj.getHashValue())) {
                                    //Don't make a delayed update wait for itself, that is just rude
                                    pPendingObj.setDelayed(true);
                                    pPendingObj.setWaitingFor(previousPendingObj.getHashValue());
                                }
                            }
                        }
                    }
            }
        }

        if ("delete".equalsIgnoreCase(pPendingObj.getAction())) {
            if (existing != null && fromPending) {
                FHLog.d(LOG_TAG, "Deleting an existing pending record for dataset :: " + existing);
                // We are trying to delete an existing pending record
                previousPendingUid = metadata.optString("pendingUid", null);
                metadata.put("previousPendingUid", previousPendingUid);
                if (previousPendingUid != null) {
                previousPendingObj = mPendingRecords.get(previousPendingUid);
                    if (previousPendingObj != null) {
                        if (!previousPendingObj.isInFlight()) {
                            FHLog.d(LOG_TAG, "existing pending record = " + previousPendingObj);
                            if ("create".equalsIgnoreCase(previousPendingObj.getAction())) {
                                // We are trying to perform a delete on an existing pending create
                                // These cancel each other out so remove them both
                                mPendingRecords.remove(pPendingObj.getHashValue());
                                mPendingRecords.remove(previousPendingUid);
                            }
                            if ("update".equalsIgnoreCase(previousPendingObj.getAction())) {
                                // We are trying to perform a delete on an existing pending update
                                // Use the pre value from the pending update for the delete and
                                // get rid of the pending update
                                pPendingObj.setPreData(previousPendingObj.getPreData());
                                pPendingObj.setInFlight(false);
                                mPendingRecords.remove(previousPendingUid);
                            } else if (!previousPendingObj.getHashValue().equals(pPendingObj.getHashValue())) {
                                //Don't make a delayed update wait for itself, that is just rude
                                pPendingObj.setDelayed(true);
                                pPendingObj.setWaitingFor(previousPendingObj.getHashValue());
                            }
                        }
                    }
                }
                
            }
            mDataRecords.remove(uid);
        }

        if (mDataRecords.containsKey(uid)) {
            FHSyncDataRecord record = pPendingObj.getPostData();
            mDataRecords.put(uid, record);
            metadata.put("fromPending", true);
            metadata.put("pendingUid", uidToSave);
        }
    }

    private void fromJSON(JSONObject pObj) {
        JSONObject syncConfigJson = pObj.getJSONObject(KEY_SYNC_CONFIG);
        this.mSyncConfig = FHSyncConfig.fromJSON(syncConfigJson);
        this.mHashvalue = pObj.optString(KEY_HASHVALUE, null);
        JSONObject pendingJSON = pObj.getJSONObject(KEY_PENDING_RECORDS);
        for (Iterator<String> it = pendingJSON.keys(); it.hasNext(); ) {
            String key = it.next();
            JSONObject pendObjJson = pendingJSON.getJSONObject(key);
            FHSyncPendingRecord pending = FHSyncPendingRecord.fromJSON(pendObjJson);
            this.mPendingRecords.put(key, pending);
        }
        JSONObject dataJSON = pObj.getJSONObject(KEY_DATA_RECORDS);
        for (Iterator<String> dit = dataJSON.keys(); dit.hasNext(); ) {
            String dkey = dit.next();
            JSONObject dataObjJson = dataJSON.getJSONObject(dkey);
            FHSyncDataRecord datarecord = FHSyncDataRecord.fromJSON(dataObjJson);
            this.mDataRecords.put(dkey, datarecord);
        }
        if (pObj.has(KEY_SYNC_LOOP_START)) {
            this.mSyncStart = new Date(pObj.getLong(KEY_SYNC_LOOP_START));
        }
        if (pObj.has(KEY_SYNC_LOOP_END)) {
            this.mSyncEnd = new Date(pObj.getLong(KEY_SYNC_LOOP_END));
        }
        if (pObj.has(KEY_ACKNOWLEDGEMENTS)) {
            this.mAcknowledgements = pObj.getJSONArray(KEY_ACKNOWLEDGEMENTS);
        }
        if (pObj.has(KEY_QUERY_PARAMS)) {
            this.mQueryParams = pObj.getJSONObject(KEY_QUERY_PARAMS);
        }
        if (pObj.has(KEY_METADATA)) {
            this.mMetaData = pObj.getJSONObject(KEY_METADATA);
        }
    }

    private void readFromFile() {
        String filePath = mDatasetId + STORAGE_FILE_EXT;
        try {
            FileInputStream fis = mContext.openFileInput(filePath);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            writeStream(fis, bos);
            String content = bos.toString("UTF-8");
            JSONObject json = new JSONObject(content);
            fromJSON(json);
            doNotify(null, NotificationMessage.LOCAL_UPDATE_APPLIED_CODE, "load");
        } catch (FileNotFoundException ex) {
            FHLog.w(LOG_TAG, "File not found for reading: " + filePath);
        } catch (IOException e) {
            FHLog.e(LOG_TAG, "Error reading file : " + filePath, e);
        } catch (JSONException je) {
            FHLog.e(LOG_TAG, "Failed to parse JSON file : " + filePath, je);
        }
    }

    public synchronized void writeToFile() {
        String filePath = mDatasetId + STORAGE_FILE_EXT;
        try {
            FileOutputStream fos = mContext.openFileOutput(filePath, Context.MODE_PRIVATE);
            String content = getJSON().toString();
            ByteArrayInputStream bis = new ByteArrayInputStream(content.getBytes());
            writeStream(bis, fos);
        } catch (FileNotFoundException ex) {
            FHLog.e(LOG_TAG, "File not found for writing: " + filePath, ex);
            doNotify(null, NotificationMessage.CLIENT_STORAGE_FAILED_CODE, null);
        } catch (IOException e) {
            FHLog.e(LOG_TAG, "Error writing file: " + filePath, e);
            doNotify(null, NotificationMessage.CLIENT_STORAGE_FAILED_CODE, null);
        }
    }

    private void doNotify(String pUID, int pCode, String pMessage) {
        boolean sendMessage = false;
        switch (pCode) {
            case NotificationMessage.SYNC_STARTED_CODE:
                if (mSyncConfig.isNotifySyncStarted()) {
                    sendMessage = true;
                }
                break;
            case NotificationMessage.SYNC_COMPLETE_CODE:
                if (mSyncConfig.isNotifySyncComplete()) {
                    sendMessage = true;
                }
                break;
            case NotificationMessage.OFFLINE_UPDATE_CODE:
                if (mSyncConfig.isNotifyOfflineUpdate()) {
                    sendMessage = true;
                }
                break;
            case NotificationMessage.COLLISION_DETECTED_CODE:
                if (mSyncConfig.isNotifySyncCollisions()) {
                    sendMessage = true;
                }
                break;
            case NotificationMessage.REMOTE_UPDATE_FAILED_CODE:
                if (mSyncConfig.isNotifyUpdateFailed()) {
                    sendMessage = true;
                }
                break;
            case NotificationMessage.REMOTE_UPDATE_APPLIED_CODE:
                if (mSyncConfig.isNotifyRemoteUpdateApplied()) {
                    sendMessage = true;
                }
                break;
            case NotificationMessage.LOCAL_UPDATE_APPLIED_CODE:
                if (mSyncConfig.isNotifyLocalUpdateApplied()) {
                    sendMessage = true;
                }
                break;
            case NotificationMessage.DELTA_RECEIVED_CODE:
                if (mSyncConfig.isNotifyDeltaReceived()) {
                    sendMessage = true;
                }
                break;
            case NotificationMessage.SYNC_FAILED_CODE:
                if (mSyncConfig.isNotifySyncFailed()) {
                    sendMessage = true;
                }
                break;
            case NotificationMessage.CLIENT_STORAGE_FAILED_CODE:
                if (mSyncConfig.isNotifyClientStorageFailed()) {
                    sendMessage = true;
                }
            default:
                break;
        }
        if (sendMessage) {
            NotificationMessage notification =
                NotificationMessage.getMessage(mDatasetId, pUID, pCode, pMessage);
            Message message = mNotificationHandler.obtainMessage(pCode, notification);
            mNotificationHandler.sendMessage(message);
        }
    }

    private static void writeStream(InputStream pInput, OutputStream pOutput) throws IOException {
        if (pInput != null && pOutput != null) {
            BufferedInputStream bis = new BufferedInputStream(pInput);
            BufferedOutputStream bos = new BufferedOutputStream(pOutput);
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = bis.read(buffer)) != -1) {
                bos.write(buffer, 0, bytesRead);
            }
            bos.close();
            bis.close();
        }
    }

    /**
     * If the records returned from syncRecord request contains elements in pendings,
     * it means there are local changes that haven't been applied to the cloud yet.
     * Remove those records from the response to make sure local data will not be
     * overridden (blinking disappear / reappear effect).
    */
    private void applyPendingChangesToRecords(JSONObject resData) {
        Log.d(LOG_TAG, String.format("SyncRecords result = %s pending = %s", resData.toString(), mPendingRecords.toString()));
        for (FHSyncPendingRecord pendingRecord : mPendingRecords.values()) {
            JSONObject resRecord = null;
            if (resData.has("create")) {
                resRecord = resData.optJSONObject("create");
                if (resRecord != null && resRecord.has(pendingRecord.getUid())) {
                    resRecord.remove(pendingRecord.getUid());
                }
            }
            
            if (resData.has("update")) {
                resRecord = resData.optJSONObject("update");
                if (resRecord != null && resRecord.has(pendingRecord.getUid())) {
                    resRecord.remove(pendingRecord.getUid());
                }
            }
            
            if (resData.has("delete")) {
                resRecord = resData.optJSONObject("delete");
                if (resRecord != null && resRecord.has(pendingRecord.getUid())) {
                    resRecord.remove(pendingRecord.getUid());
                }
            }
            Log.d(LOG_TAG, String.format("SyncRecords result after pending removed = %s", resData.toString()));
        }
    }
    
    private void updateDelayedFromNewData(JSONObject responseData) {
        for (Map.Entry<String, FHSyncPendingRecord> record : this.mPendingRecords.entrySet()) {
            
            FHSyncPendingRecord pendingObject = record.getValue();
            if (pendingObject.isDelayed() && pendingObject.getWaitingFor() != null) {
                if( responseData.has("updates")) {
                    JSONObject updatedHashes = responseData.getJSONObject("updates").optJSONObject("hashes");
                    if (updatedHashes != null && updatedHashes.has(pendingObject.getWaitingFor())) {
                        pendingObject.setDelayed(false);
                        pendingObject.setWaitingFor(null);
                    } if ( updatedHashes == null ) {
                        boolean waitingForIsStillPending = false; 
                        String waitingFor = pendingObject.getWaitingFor();
                        if (pendingObject.getWaitingFor().equals(pendingObject.getHashValue())) {
                            //Somehow a pending object is waiting on itself, lets not do that
                            pendingObject.setDelayed(false);
                            pendingObject.setWaitingFor(null);    
                        } else {
                            for (FHSyncPendingRecord pending : mPendingRecords.values()) {

                                if (pending.getHashValue().equals(waitingFor) || pending.getUid().equals(waitingFor)) {

                                    waitingForIsStillPending = true;
                                    break;
                                }
                            }
                            if (!waitingForIsStillPending) {
                                pendingObject.setDelayed(false);
                                pendingObject.setWaitingFor(null);    
                            }
                        }
                    }
                } 
            } else if (pendingObject.isDelayed() && pendingObject.getWaitingFor() == null) {
                pendingObject.setDelayed(false);
            }
        }
    }
    
    private void updateMetaFromNewData(JSONObject responseData) {
        Iterator keysIter = this.mMetaData.keys();
        Set<String> keysToRemove = new HashSet<>(this.mMetaData.length());
        while(keysIter.hasNext()) {
            String key = (String) keysIter.next();
            JSONObject metaData = this.mMetaData.optJSONObject(key);
            JSONObject updates = responseData.optJSONObject("updates");
            if ( updates != null ) {
                JSONObject updatedHashes = updates.optJSONObject("hashes");
                String pendingHash = metaData.optString("pendingUid");
                if (pendingHash != null && updatedHashes != null && updatedHashes.has(pendingHash)) {
                    keysToRemove.add(key);
                }
            }
            
        }
        
        for (String keyToRemove : keysToRemove) {
            mMetaData.remove(keyToRemove);
        }
        
    }
    
    
    private void checkUidChanges(JSONObject appliedUpdates) {
        if (appliedUpdates != null && appliedUpdates.length() > 0) {
            Iterator keysIterator = appliedUpdates.keys();
            Map<String, String> newUids = new HashMap<>();
            List<String> keys = new ArrayList<>();
            while (keysIterator.hasNext()) {
                keys.add((String) keysIterator.next());
            }
            
            for (String key : keys ) {
                JSONObject obj = appliedUpdates.getJSONObject(key);
                String action = obj.getString("action");
                if ("create".equalsIgnoreCase(action)) {
                    String newUid = obj.getString("uid");
                    String oldUid = obj.getString("hash");
                    //remember the mapping
                    this.mUidMappings.put(oldUid, newUid);
                    newUids.put(oldUid, newUid);
                    //we should update the data records to make sure they are now using the new UID
                    FHSyncDataRecord dataRecord = this.mDataRecords.get(oldUid);
                    if (dataRecord != null) {
                        this.mDataRecords.put(newUid, dataRecord);
                        this.mDataRecords.remove(oldUid);
                    }
                    
                }
                
                if (newUids.size() > 0) {
                    //we need to check all existing pendingRecords and update their UIDs if they are still the old values
                    for (Map.Entry<String, FHSyncPendingRecord> keyRecord : mPendingRecords.entrySet()) {
                        FHSyncPendingRecord pendingRecord = keyRecord.getValue();
                        String pendingRecordUid = pendingRecord.getUid();
                        String newUID = newUids.get(pendingRecordUid);
                        if (newUID != null) {
                            pendingRecord.setUid(newUID);
                        }    
                    }
                    
                }
                
            }
            
        }
    }
    
    public void setSyncRunning(boolean pSyncRunning) {
        this.mSyncRunning = pSyncRunning;
    }

    public boolean isSyncRunning() {
        return mSyncRunning;
    }

    public void setInitialised(boolean pInitialised) {
        this.mInitialised = pInitialised;
    }

    public void setSyncPending(boolean pSyncPending) {
        this.mSyncPending = pSyncPending;
    }

    public boolean isSyncPending() {
        return mSyncPending;
    }

    public void setSyncConfig(FHSyncConfig pSyncConfig) {
        this.mSyncConfig = pSyncConfig;
    }

    public FHSyncConfig getSyncConfig() {
        return mSyncConfig;
    }

    public void setQueryParams(JSONObject pQueryParams) {
        this.mQueryParams = pQueryParams;
    }

    public void stopSync(boolean pStopSync) {
        this.mStopSync = pStopSync;
    }

    public boolean isStopSync() {
        return mStopSync;
    }

    public Date getSyncStart() {
        return mSyncStart;
    }

    public Date getSyncEnd() {
        return mSyncEnd;
    }

    public void setContext(Context pContext) {
        mContext = pContext;
    }

    public void setNotificationHandler(FHSyncNotificationHandler pHandler) {
        mNotificationHandler = pHandler;
    }

}
