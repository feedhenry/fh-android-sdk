package com.feedhenry.sdk.sync;

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
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.json.fh.JSONArray;
import org.json.fh.JSONException;
import org.json.fh.JSONObject;

import android.content.Context;
import android.os.Message;

import com.feedhenry.sdk.FH;
import com.feedhenry.sdk.FHActCallback;
import com.feedhenry.sdk.FHResponse;
import com.feedhenry.sdk.api.FHActRequest;
import com.feedhenry.sdk.utils.FHLog;

public class FHSyncDataset {

  private boolean mSyncRunning;
  private boolean mInitialised;
  private String mDatasetId;
  private Date mSyncStart;
  private Date mSyncEnd;
  private boolean mSyncPending;
  private FHSyncConfig mSyncConfig = new FHSyncConfig();
  private Map<String, FHSyncPendingRecord> mPendingRecords = new HashMap<String, FHSyncPendingRecord>();
  private Map<String, FHSyncDataRecord> mDataRecords = new HashMap<String, FHSyncDataRecord>();
  private JSONObject mQueryParams = new JSONObject();
  private JSONObject mMetaData = new JSONObject();
  private String mHashvalue;
  private JSONArray mAcknowledgements = new JSONArray();
  private boolean mStopSync;
  
  private Context mContext;
  private FHSyncNotificationHandler mNotificationHandler;
  
  private static final String STORAGE_FILE_EXT = ".sync.json";
  
  private static final String KEY_DATESETID = "dataSetId";
  private static final String KEY_SYNC_LOOP_START = "syncLoopStart";
  private static final String KEY_SYNC_LOOP_END = "syncLoopEnd";
  private static final String KEY_SYNC_CONFIG = "syncConfig";
  private static final String KEY_PENDING_RECORDS = "pendingDataRecords";
  private static final String KEY_DATA_RECORDS = "dataRecords";
  private static final String KEY_HASHVALUE = "hashValue";
  private static final String KEY_ACKNOWLEDGEMENTS = "acknowledgements";
  private static final String KEY_QUERY_PARAMS = "queryParams";
  private static final String KEY_METADATA = "metaData";
  
  protected static final String LOG_TAG = "com.feedhenry.sdk.sync.FHSyncDataset";
  
  public FHSyncDataset(Context pContext, FHSyncNotificationHandler pHandler, String pDatasetId, FHSyncConfig pConfig, JSONObject pQueryParams){
    mContext = pContext;
    mNotificationHandler = pHandler;
    mDatasetId = pDatasetId;
    mSyncConfig = pConfig;
    mQueryParams = pQueryParams;
    readFromFile();
  }
  
  public JSONObject getJSON(){
    JSONObject ret = new JSONObject();
    if(null != mHashvalue){
      ret.put(KEY_HASHVALUE, mHashvalue);
    }
    ret.put(KEY_DATESETID, mDatasetId);
    ret.put(KEY_SYNC_CONFIG, mSyncConfig.getJSON());
    JSONObject pendingJson = new JSONObject();
    for(String key: mPendingRecords.keySet()){
      pendingJson.put(key, mPendingRecords.get(key).getJSON());
    }
    ret.put(KEY_PENDING_RECORDS, pendingJson);
    JSONObject dataJson = new JSONObject();
    for(String dkey: mDataRecords.keySet()){
      dataJson.put(dkey, mDataRecords.get(dkey).getJSON());
    }
    ret.put(KEY_DATA_RECORDS, dataJson);
    if(null != this.mSyncStart){
      ret.put(KEY_SYNC_LOOP_START, this.mSyncStart.getTime());
    }
    if(null != this.mSyncEnd){
      ret.put(KEY_SYNC_LOOP_END, this.mSyncEnd.getTime());
    }
    ret.put(KEY_ACKNOWLEDGEMENTS, mAcknowledgements);
    ret.put(KEY_QUERY_PARAMS, mQueryParams);
    ret.put(KEY_METADATA, mMetaData);
    return ret;
  }
  
  public JSONObject listData(){
    JSONObject ret = new JSONObject();
    for(String key: this.mDataRecords.keySet()){
      FHSyncDataRecord dataRecord = this.mDataRecords.get(key);
      JSONObject datajson = new JSONObject();
      datajson.put("data", dataRecord.getData());
      datajson.put("uid", key);
      ret.put(key, datajson);
    }
    return ret;
  }
  
  public JSONObject readData(String pUid){
    FHSyncDataRecord dataRecord = mDataRecords.get(pUid);
    if(null != dataRecord){
      JSONObject ret = new JSONObject();
      ret.put("data", dataRecord.getData());
      ret.put("uid", pUid);
      return ret;
    } else {
      return null;
    }
  }
  
  public JSONObject createData(JSONObject pData){
    FHSyncPendingRecord pendingRecord = addPendingObject(null, pData, "create");
    FHSyncDataRecord dataRecord = mDataRecords.get(pendingRecord.getUid());
    JSONObject ret = new JSONObject();
    if(null != dataRecord){
      ret.put("data", dataRecord.getData());
      ret.put("uid", pendingRecord.getUid());
    }
    return ret;
  }
  
  public JSONObject updateData(String pUid, JSONObject pData){
    FHSyncPendingRecord pendingRecord = addPendingObject(pUid, pData, "update");
    FHSyncDataRecord dataRecord = mDataRecords.get(pUid);
    JSONObject ret = new JSONObject();
    if(null != dataRecord){
      ret.put("data", dataRecord.getData());
      ret.put("uid", pUid);
    }
    return ret;
  }
  
  public JSONObject deleteData(String pUid){
    FHSyncPendingRecord pendingRecord = addPendingObject(pUid, null, "delete");
    FHSyncDataRecord deleted = pendingRecord.getPreData();
    JSONObject ret = new JSONObject();
    if(null != deleted){
      ret.put("data", deleted.getData());
      ret.put("uid", pUid);
    }
    return ret;
  }
  
  public void startSyncLoop(){
    mSyncPending = false;
    mSyncRunning = true;
    mSyncStart = new Date();
    doNotify(null, NotificationMessage.SYNC_STARTED_CODE, null);
    if(!FH.isOnline()){
      syncCompleteWithCode("offline");
    } else {
      JSONObject syncLoopParams = new JSONObject();
      syncLoopParams.put("fn", "sync");
      syncLoopParams.put("dataset_id", mDatasetId);
      syncLoopParams.put("query_params", mQueryParams);
      if(null != mHashvalue){
        syncLoopParams.put("dataset_hash", mHashvalue);
      }
      syncLoopParams.put("acknowledgements", mAcknowledgements);
      JSONArray pendings = new JSONArray();
      for(String key: mPendingRecords.keySet()){
        FHSyncPendingRecord pendingRecord = mPendingRecords.get(key);
        if(!pendingRecord.isInFight() && !pendingRecord.isCrashed()){
          pendingRecord.setInFight(true);
          pendingRecord.setInFlightDate(new Date());
          JSONObject pendingJSON = pendingRecord.getJSON();
          pendingJSON.put("hash", pendingRecord.getHashValue());
          pendings.put(pendingJSON);
        }
      }
      
      syncLoopParams.put("pending", pendings);
      //if(pendings.length() > 0){
        FHLog.d(LOG_TAG, "Starting sync loop -global hash = " + mHashvalue + " :: params = " + syncLoopParams);
      //}
      
      try{
        FHActRequest actRequest = FH.buildActRequest(mDatasetId, syncLoopParams);
        actRequest.executeAsync(new FHActCallback() {
          
          @Override
          public void success(FHResponse pResponse) {
            JSONObject responseData = pResponse.getJson();
            syncRequestSuccess(responseData);
          }
          
          @Override
          public void fail(FHResponse pResponse) {
            // The AJAX call failed to complete succesfully, so the state of the current pending updates is unknown
            // Mark them as "crashed". The next time a syncLoop completets successfully, we will review the crashed
            // records to see if we can determine their current state.
            markInFlightAsCrashed();
            FHLog.e(LOG_TAG, "syncLoop failed : msg = " + pResponse.getErrorMessage(), pResponse.getError());
            doNotify(null, NotificationMessage.SYNC_FAILED_CODE, pResponse.getRawResponse());
            syncCompleteWithCode(pResponse.getRawResponse());
          }
        });
      } catch (Exception e){
        FHLog.e(LOG_TAG, "Error performing sync", e);
        syncCompleteWithCode(e.getMessage());
      }
      
    }
    
  }
  
  private void syncRequestSuccess(JSONObject pData){
    // Check to see if any new pending records need to be updated to reflect the current state of play.
    updatePendingFromNewData(pData);
    
    //Check to see if any previously crashed inflight records can now be resolved
    updateCrashedInFlightFromNewData(pData);
    
    //Update the new dataset with details of any inflight updates which we have not received a response on
    updateNewDataFromInFlight(pData);
    
    // Update the new dataset with details of any pending updates
    updateNewDataFromPending(pData);
    
    if(pData.has("records")){
      resetDataRecords(pData);
    }
    
    if(pData.has("updates")){
      JSONArray ack = new JSONArray();
      JSONObject updates = pData.getJSONObject("updates");
      processUpdates(updates.optJSONObject("applied"), NotificationMessage.REMOTE_UPDATE_APPLIED_CODE, ack);
      processUpdates(updates.optJSONObject("failed"), NotificationMessage.REMOTE_UPDATE_FAILED_CDOE, ack);
      processUpdates(updates.optJSONObject("collisions"), NotificationMessage.COLLISION_DETECTED_CDOE, ack);
      mAcknowledgements = ack;
    } else if(pData.has("hash") && !pData.getString("hash").equals(mHashvalue)){
      String remoteHash = pData.getString("hash");
      FHLog.d(LOG_TAG, "Local dataset stale - syncing records :: local hash= "+mHashvalue+" - remoteHash =" + remoteHash);
      // Different hash value returned - Sync individual records
      syncRecords();
    } else {
      FHLog.i(LOG_TAG, "LOcal dataset up to date");
    }
    
    syncCompleteWithCode("online");
  }
  
  private void syncRecords(){
    JSONObject clientRecords = new JSONObject();
    for(Map.Entry<String, FHSyncDataRecord> entry: mDataRecords.entrySet()){
      String uid = entry.getKey();
      FHSyncDataRecord record = entry.getValue();
      
      clientRecords.put(uid, record.getHashValue());
    }
    
    JSONObject syncRecsParams = new JSONObject();
    syncRecsParams.put("fn", "syncRecords");
    syncRecsParams.put("dataset_id", mDatasetId);
    syncRecsParams.put("query_params", mQueryParams);
    syncRecsParams.put("clientRecs", clientRecords);
    
    FHLog.d(LOG_TAG, "syncRecParams :: " + syncRecsParams);
    
    try{
      FHActRequest request = FH.buildActRequest(mDatasetId, syncRecsParams);
      request.executeAsync(new FHActCallback() {
        
        @Override
        public void success(FHResponse pResponse) {
          syncRecordsSuccess(pResponse.getJson());
        }
        
        @Override
        public void fail(FHResponse pResponse) {
          FHLog.e(LOG_TAG, "syncRecords failed: " + pResponse.getRawResponse(), pResponse.getError());
        }
      });
    } catch(Exception e){
      FHLog.e(LOG_TAG, "error when running syncRecords", e);
    }
    
  }
  
  private void syncRecordsSuccess(JSONObject pData){
    JSONObject dataCreated = pData.optJSONObject("create");
    if(null != dataCreated){
      Iterator<String> dit = dataCreated.keys();
      while(dit.hasNext()){
        String dk = dit.next();
        JSONObject data = dataCreated.getJSONObject(dk);
        FHSyncDataRecord rec = new FHSyncDataRecord(data.getJSONObject("data"));
        rec.setHashValue(data.getString("hash"));
        mDataRecords.put(dk, rec);
        doNotify(dk, NotificationMessage.DELTA_RECEIVED_CODE, "create");
      }
    }
    
    JSONObject dataUpdated = pData.optJSONObject("update");
    if(null != dataUpdated){
      Iterator<String> uit = dataUpdated.keys();
      while(uit.hasNext()){
        String uk = uit.next();
        JSONObject updatedata = dataUpdated.getJSONObject(uk);
        FHSyncDataRecord urec = mDataRecords.get(uk);
        if(null != urec){
          urec.setData(updatedata.getJSONObject("data"));
          urec.setHashValue(updatedata.getString("hash"));
          mDataRecords.put(uk, urec);
          doNotify(uk, NotificationMessage.DELTA_RECEIVED_CODE, "update");
        }
      }
    }
    
    JSONObject deleted = pData.optJSONObject("delete");
    if(null != deleted){
      Iterator<String> rit = deleted.keys();
      while(rit.hasNext()){
        String rkey = rit.next();
        mDataRecords.remove(rkey);
        doNotify(rkey, NotificationMessage.DELTA_RECEIVED_CODE, "delete");
      }
    }
    
    if(pData.has("hash")){
      mHashvalue = pData.getString("hash");
    }
    
    syncCompleteWithCode("online");
  }
  
  
  private void processUpdates(JSONObject pUpdates, int pNotification, JSONArray pAck){
    if(null != pUpdates){
      Iterator<String> it = pUpdates.keys();
      while(it.hasNext()){
        String key = it.next();
        JSONObject up = pUpdates.getJSONObject(key);
        pAck.put(up);
        FHSyncPendingRecord pendingRec = mPendingRecords.get(key);
        if(null != pendingRec && pendingRec.isInFight() && !pendingRec.isCrashed()){
          mPendingRecords.remove(key);
          doNotify(up.getString("uid"), pNotification, up.toString());
        }
      }
    }
  }
  
  private void resetDataRecords(JSONObject pData){
    JSONObject records = pData.getJSONObject("records");
    Map<String, FHSyncDataRecord> allrecords = new HashMap<String, FHSyncDataRecord>();
    
    Iterator<String> it = records.keys();
    while(it.hasNext()){
      String key = it.next();
      JSONObject data = records.getJSONObject(key);
      FHSyncDataRecord record = new FHSyncDataRecord(data.getJSONObject("data"));
      allrecords.put(key, record);
    }
    
    mDataRecords = allrecords;
    mHashvalue = pData.getString("hash");
    doNotify(mHashvalue, NotificationMessage.DELTA_RECEIVED_CODE, "full dataset");
  }
  
  private void updatePendingFromNewData(JSONObject pData){
    if(null != mPendingRecords && pData.has("records")){
      for(Map.Entry<String, FHSyncPendingRecord> entry: mPendingRecords.entrySet()){
        FHSyncPendingRecord pendingRecord = entry.getValue();
        JSONObject metadata = mMetaData.optJSONObject(pendingRecord.getUid());
        if(null == metadata){
          metadata = new JSONObject();
          mMetaData.put(pendingRecord.getUid(), metadata);
        }
        if(!pendingRecord.isInFight()){
          //pending record that has not been submitted
          FHLog.d(LOG_TAG, "updatePendingFromNewData - Found Non inFlight record -> action = " + pendingRecord.getAction() + " :: uid = " + pendingRecord.getUid() + " :: hash = " + pendingRecord.getHashValue());
          if("update".equalsIgnoreCase(pendingRecord.getAction()) || "delete".equalsIgnoreCase(pendingRecord.getAction())){
            // Update the pre value of pending record to reflect the latest data returned from sync.
            JSONObject remoteRec = pData.getJSONObject("records").optJSONObject(pendingRecord.getUid());
            if(null != remoteRec){
              FHLog.d(LOG_TAG, "updatePendingFromNewData - updating pre values for existing pending record " + pendingRecord.getUid());
              FHSyncDataRecord rec = new FHSyncDataRecord(remoteRec);
              pendingRecord.setPreData(rec);
            } else {
              //The update/delete may be for a newly created record in which case the uid will be changed.
              String previousPendingUid = metadata.optString("previousPendingUid");
              if(null != previousPendingUid){
                FHSyncPendingRecord previousPendingRec = mPendingRecords.get(previousPendingUid);
                if(null != previousPendingRec){
                  if(pData.has("updates")){
                    JSONObject updates = pData.getJSONObject("updates");
                    if(updates.has("applied")){
                      JSONObject applied = updates.getJSONObject("applied");
                      if(applied.has(previousPendingRec.getHashValue())){
                        //There is an update in from a previous pending action
                        String remoteUid = applied.getJSONObject(previousPendingRec.getHashValue()).optString("uid", null);
                        if(null != remoteUid){
                          remoteRec = pData.getJSONObject("records").optJSONObject(remoteUid);
                          if(null != remoteRec){
                            FHLog.d(LOG_TAG, "updatePendingFromNewData - Updating pre values for existing pending record which was previously a create " + pendingRecord.getUid() + " => " + remoteUid);
                            FHSyncDataRecord record = new FHSyncDataRecord(remoteRec);
                            pendingRecord.setPreData(record);
                            pendingRecord.setUid(remoteUid);
                          }
                        }
                      }
                    }
                  }
                }
              }
            }
          }
        }
        
        String pendingHash = entry.getKey();
        if("create".equalsIgnoreCase(pendingRecord.getAction())){
          if(null != pData && pData.has("updates")){
            JSONObject updates = pData.getJSONObject("updates");
            if(updates.has("applied")){
              JSONObject applied = updates.getJSONObject("applied");
              if(applied.has(pendingHash)){
                JSONObject appliedData = applied.getJSONObject(pendingHash);
                FHLog.d(LOG_TAG, "updatePendingFromNewData - Found an update for a pending create " + appliedData);
                JSONObject remoteRec = pData.optJSONObject(applied.optString("uid", ""));
                if(null != remoteRec){
                  FHLog.d(LOG_TAG, "updatePendingFromNewData - Changing pending create to an update based on new record " + remoteRec);
                  //setup the pending as an update
                  pendingRecord.setAction("update");
                  FHSyncDataRecord preData = new FHSyncDataRecord(remoteRec);
                  pendingRecord.setPreData(preData);
                  pendingRecord.setUid(applied.optString("uid"));
                }
              }
            }
          }
        }
        
      }
    }
  }
  
  private void updateCrashedInFlightFromNewData(JSONObject pData){
    JSONObject updateNotifications = new JSONObject();
    updateNotifications.put("applied", NotificationMessage.REMOTE_UPDATE_APPLIED_CODE);
    updateNotifications.put("failed", NotificationMessage.REMOTE_UPDATE_FAILED_CDOE);
    updateNotifications.put("collisions", NotificationMessage.COLLISION_DETECTED_CDOE);
    
    JSONObject resolvedCrashed = new JSONObject();
    List<String> keysToRemove = new ArrayList<String>();
    
    for(Map.Entry<String, FHSyncPendingRecord> entry: mPendingRecords.entrySet()){
      FHSyncPendingRecord pendingRecord = entry.getValue();
      String pendingHash = entry.getKey();
      if(pendingRecord.isInFight() && pendingRecord.isCrashed()){
        FHLog.d(LOG_TAG, "updateCrashedInFlightFromNewData - Found crashed inFlight pending record uid= " + pendingRecord.getUid() + ":: hash = " + pendingRecord.getHashValue());
        if(null != pData && pData.has("updates")){
          JSONObject updates = pData.getJSONObject("updates");
          if(updates.has("hashes")){
            JSONObject hashes = updates.getJSONObject("hashes");
            //check if the updates received contain any info about the crashed inflight update
            JSONObject crashedUpdate = hashes.optJSONObject(pendingHash);
            if(null != crashedUpdate){
              resolvedCrashed.put(crashedUpdate.optString("uid"), crashedUpdate);
              FHLog.d(LOG_TAG, "updateCrashedInFlightFromNewData - Resolving status for crashed inflight pending record " + crashedUpdate);
              String crashedType = crashedUpdate.optString("type", null);
              String crashedAction = crashedUpdate.optString("action", null);
              if(null != crashedType && "failed".equalsIgnoreCase(crashedType)){
                //Crashed updated failed - revert local dataset
                if(null != crashedAction && "create".equalsIgnoreCase(crashedAction)){
                  FHLog.d(LOG_TAG, "updateCrashedInFlightFromNewData - Deleting failed create from dataset");
                  mDataRecords.remove(crashedUpdate.getString("uid"));
                } else if(null != crashedAction && ("update".equalsIgnoreCase(crashedAction) || "delete".equalsIgnoreCase(crashedAction))){
                  FHLog.d(LOG_TAG, "updateCrashedInFlightFromNewData - Reverting failed " +crashedAction+ " in dataset");
                  mDataRecords.put(crashedUpdate.getString("uid"), pendingRecord.getPreData());
                }
              }
              
              keysToRemove.add(pendingHash);
              doNotify(crashedUpdate.getString("uid"), updateNotifications.getInt(crashedUpdate.getString("type")), crashedUpdate.toString());
              
            } else {
               // No word on our crashed update - increment a counter to reflect another sync that did not give us
              // any update on our crashed record.
              pendingRecord.incrementCrashCount();
            }
          } else {
            // No word on our crashed update - increment a counter to reflect another sync that did not give us
            // any update on our crashed record.
            pendingRecord.incrementCrashCount();
          }
        } else {
          pendingRecord.incrementCrashCount();
        }
      }
    }
    
    for(int i=0;i<keysToRemove.size();i++){
      mPendingRecords.remove(keysToRemove.get(i));
    }
    
    keysToRemove.clear();
    
    for(Map.Entry<String, FHSyncPendingRecord> entry: mPendingRecords.entrySet()){
      FHSyncPendingRecord pendingRecord = entry.getValue();
      String pendingHash = entry.getKey();
      if(pendingRecord.isInFight() && pendingRecord.isCrashed()){
        if(pendingRecord.getCrashedCount() > mSyncConfig.getCrashCountWait()){
          FHLog.d(LOG_TAG, "updateCrashedInFlightFromNewData - Crashed inflight pending record has reached crashed_count_wait limit : " + pendingRecord);
          if(mSyncConfig.isResendCrashedUpdates()){
            FHLog.d(LOG_TAG, "updateCrashedInFlightFromNewData - Retryig crashed inflight pending record");
            pendingRecord.setCrashed(false);
            pendingRecord.setInFight(false);
          } else {
            FHLog.d(LOG_TAG, "updateCrashedInFlightFromNewData - Deleting crashed inflight pending record");
            keysToRemove.add(pendingHash);
          }
        }
      } else if(!pendingRecord.isInFight() && pendingRecord.isCrashed()){
        FHLog.d(LOG_TAG, "updateCrashedInFlightFromNewData - Trying to resolve issues with crashed non in flight record - uid =" + pendingRecord.getUid());
        // Stalled pending record because a previous pending update on the same record crashed
        JSONObject data = resolvedCrashed.optJSONObject(pendingRecord.getUid());
        if(null != data){
          FHLog.d(LOG_TAG, "updateCrashedInFlightFromNewData - Found a stalled pending record backed up behind a resolved crash uid=" + pendingRecord.getUid() +  " :: hash=" + pendingRecord.getHashValue());
          pendingRecord.setCrashed(false);
        }
      }
    }
    
    for(int i=0;i<keysToRemove.size();i++){
      mPendingRecords.remove(keysToRemove.get(i));
    }
  }
  
  public void updateNewDataFromInFlight(JSONObject pData){
    if(null != mPendingRecords && pData.has("records")){
      for(Map.Entry<String, FHSyncPendingRecord> entry: mPendingRecords.entrySet()){
        FHSyncPendingRecord pendingRecord = entry.getValue();
        String pendingHash = entry.getKey();
        
        if(pendingRecord.isInFight()){
          boolean updateReceivedForPending = false;
          if(pData.has("updates")){
            JSONObject updates = pData.getJSONObject("updates");
            if(updates.has("hashes")){
              JSONObject hashes = updates.getJSONObject("hashes");
              if(hashes.has(pendingHash)){
                updateReceivedForPending = true;
              }
            }
          }
          FHLog.d(LOG_TAG, "updateNewDataFromInFlight - Found inflight pending Record - action = " + pendingRecord.getAction() + " :: hash = " +  pendingRecord.getHashValue() + " :: updateReceivedForPending= " + updateReceivedForPending);
          if(!updateReceivedForPending){
            JSONObject remoteRecord = pData.getJSONObject("records").optJSONObject(pendingRecord.getUid());
            if("update".equalsIgnoreCase(pendingRecord.getAction()) && null != remoteRecord){
              // Modify the new Record to have the updates from the pending record so the local dataset is consistent
              remoteRecord.put("data", pendingRecord.getPostData().getData());
              remoteRecord.put("hash", pendingRecord.getPostData().getHashValue());
            } else if("delete".equalsIgnoreCase(pendingRecord.getAction()) && null != remoteRecord){
              // Remove the record from the new dataset so the local dataset is consistent
              pData.getJSONObject("records").remove(pendingRecord.getUid());
            } else if("create".equalsIgnoreCase(pendingRecord.getAction())){
               // Add the pending create into the new dataset so it is not lost from the UI
              FHLog.d(LOG_TAG, "updateNewDataFromInFlight - re adding pending create to incomming dataset");
              JSONObject dict = new JSONObject();
              dict.put("data", pendingRecord.getPostData().getData());
              dict.put("hash", pendingRecord.getPostData().getHashValue());
              pData.getJSONObject("records").put(pendingRecord.getUid(), dict);
            }
          }
        }
      }
    }
  }
  
  private void updateNewDataFromPending(JSONObject pData){
    if(null != mPendingRecords && pData.has("records")){
      for(Map.Entry<String, FHSyncPendingRecord> entry: mPendingRecords.entrySet()){
        FHSyncPendingRecord pendingRecord = entry.getValue();
        if(!pendingRecord.isInFight()){
          FHLog.d(LOG_TAG, "updateNewDataFromPending - Found Non inFlight record -> action="+pendingRecord.getAction()+" :: uid="+pendingRecord.getUid()+" :: hash=" + pendingRecord.getHashValue());
          JSONObject remoteRecord = pData.getJSONObject("records").optJSONObject(pendingRecord.getUid());
          if(null != remoteRecord && "update".equalsIgnoreCase(pendingRecord.getAction())){
            // Modify the new Record to have the updates from the pending record so the local dataset is consistent
            remoteRecord.put("data", pendingRecord.getPostData().getData());
            remoteRecord.put("hash", pendingRecord.getPostData().getHashValue());
          } else if(null != remoteRecord && "delete".equalsIgnoreCase(pendingRecord.getAction())){
            pData.getJSONObject("records").remove(pendingRecord.getUid());
          } else if("create".equalsIgnoreCase(pendingRecord.getAction())){
            // Add the pending create into the new dataset so it is not lost from the UI
            FHLog.d(LOG_TAG, "updateNewDataFromPending - re adding pending create to incomming dataset");
            JSONObject dict = new JSONObject();
            dict.put("data", pendingRecord.getPostData().getData());
            dict.put("hash", pendingRecord.getPostData().getHashValue());
            pData.getJSONObject("records").put(pendingRecord.getUid(), dict);
          }
        }
      }
    }
  }
  
  private void markInFlightAsCrashed(){
    Map<String, FHSyncPendingRecord> crashedRecords = new HashMap<String, FHSyncPendingRecord>();
    for(Map.Entry<String, FHSyncPendingRecord> entry: mPendingRecords.entrySet()){
      FHSyncPendingRecord pendingRecord = entry.getValue();
      String pendingHash = entry.getKey();
      if(pendingRecord.isInFight()){
        FHLog.d(LOG_TAG, "Marking in flight pending record as crashed : " + pendingHash);
        pendingRecord.setCrashed(true);
        crashedRecords.put(pendingRecord.getUid(), pendingRecord);
      }
    }
    
    // Check for any pending updates that would be modifying a crashed record. These can not go out until the
    // status of the crashed record is determined
    for(Map.Entry<String, FHSyncPendingRecord> entry: mPendingRecords.entrySet()){
      FHSyncPendingRecord pendingRecord = entry.getValue();
      if(!pendingRecord.isInFight()){
        if(crashedRecords.containsKey(pendingRecord.getUid())){
          pendingRecord.setCrashed(true);
        }
      }
    }
  }
  
  public void syncCompleteWithCode(String pCode){
    mSyncRunning = false;
    mSyncEnd = new Date();
    writeToFile();
    doNotify(mHashvalue, NotificationMessage.SYNC_COMPLETE_CODE, pCode);
  }
  
  private FHSyncPendingRecord addPendingObject(String pUid, JSONObject pData, String pAction){
    if(!FH.isOnline()){
      doNotify(pUid, NotificationMessage.OFFLINE_UPDATE_CODE, pAction);
    }
    FHSyncPendingRecord pending = new FHSyncPendingRecord();
    pending.setInFight(false);
    pending.setAction(pAction);
    
    if(null != pData){
      FHSyncDataRecord dataRecord = new FHSyncDataRecord(pData);
      pending.setPostData(dataRecord);
    }
    
    if("create".equalsIgnoreCase(pAction)){
      pending.setUid(pending.getPostData().getHashValue());
      storePendingObj(pending);
    } else {
      FHSyncDataRecord existingData = mDataRecords.get(pUid);
      if(null != existingData){
        pending.setUid(pUid);
        pending.setPreData(existingData.clone());
        storePendingObj(pending);
      }
    }
    return pending;
  }
  
  private void storePendingObj(FHSyncPendingRecord pPendingObj){
    mPendingRecords.put(pPendingObj.getHashValue(), pPendingObj);
    updateDatasetFromLocal(pPendingObj);
    if(mSyncConfig.isAutoSyncLocalUpdates()){
      mSyncPending = true;
    }
    writeToFile();
    doNotify(pPendingObj.getUid(), NotificationMessage.LOCAL_UPDATE_APPLIED_CODE, pPendingObj.getAction());
  }
  
  private void updateDatasetFromLocal(FHSyncPendingRecord pPendingObj){
    String previousPendingUid = null;
    FHSyncPendingRecord previousPendingObj = null;
    String uid = pPendingObj.getUid();
    FHLog.d(LOG_TAG, "updating local dataset for uid " + uid + " - action = " + pPendingObj.getAction());
    JSONObject metadata = mMetaData.optJSONObject(uid);
    if(null == metadata){
      metadata = new JSONObject();
      mMetaData.put(uid,metadata);
    }
    FHSyncDataRecord existing = mDataRecords.get(uid);
    boolean fromPending = metadata.optBoolean("fromPending");
    if("create".equalsIgnoreCase(pPendingObj.getAction())){
      if(null != existing){
        FHLog.d(LOG_TAG, "dataset already exists for uid for create :: " + existing.toString());
        if(fromPending){
          // We are trying to create on top of an existing pending record
          // Remove the previous pending record and use this one instead
          previousPendingUid = metadata.optString("pendingUid", null);
          if(null != previousPendingUid){
            mPendingRecords.remove(previousPendingUid);
          }
        }
      }
      mDataRecords.put(uid, new FHSyncDataRecord());
    }
    
    if("update".equalsIgnoreCase(pPendingObj.getAction())){
      if(null != existing){
        if(fromPending){
          FHLog.d(LOG_TAG, "Updating an existing pending record for dataset :: " + existing.toString());
          // We are trying to update an existing pending record
          previousPendingUid = metadata.optString("pendingUid", null);
          metadata.put("previousPendingUid", previousPendingUid);
          previousPendingObj = mPendingRecords.get(previousPendingUid);
          if(null != previousPendingObj && !previousPendingObj.isInFight()){
            FHLog.d(LOG_TAG, "existing pre-flight pending record = " + previousPendingObj);
            // We are trying to perform an update on an existing pending record
            // modify the original record to have the latest value and delete the pending update
            previousPendingObj.setPostData(pPendingObj.getPostData());
            mPendingRecords.remove(pPendingObj.getHashValue());
          }
        }
      }
    }
    
    if("delete".equalsIgnoreCase(pPendingObj.getAction())){
      if(null != existing && fromPending){
        FHLog.d(LOG_TAG, "Deleting an existing pending record for dataset :: " + existing);
        // We are trying to delete an existing pending record
        previousPendingUid = metadata.optString("pendingUid", null);
        metadata.put("previousPendingUid", previousPendingUid);
        previousPendingObj = mPendingRecords.get(previousPendingUid);
        if(null != previousPendingObj && !previousPendingObj.isInFight()){
          FHLog.d(LOG_TAG, "existing pending record = " + previousPendingObj);
          if("create".equalsIgnoreCase(previousPendingObj.getAction())){
            // We are trying to perform a delete on an existing pending create
            // These cancel each other out so remove them both
            mPendingRecords.remove(pPendingObj.getHashValue());
            mPendingRecords.remove(previousPendingUid);
          }
          if("update".equalsIgnoreCase(previousPendingObj.getAction())){
            // We are trying to perform a delete on an existing pending update
            // Use the pre value from the pending update for the delete and
            // get rid of the pending update
            pPendingObj.setPreData(previousPendingObj.getPreData());
            pPendingObj.setInFight(false);
            mPendingRecords.remove(previousPendingUid);
          }
        }
      }
      mDataRecords.remove(uid);
    }
    
    if(mDataRecords.containsKey(uid)){
      FHSyncDataRecord record = pPendingObj.getPostData();
      mDataRecords.put(uid, record);
      metadata.put("fromPending", true);
      metadata.put("pendingUid", pPendingObj.getHashValue());
    }
  }
  
  private void fromJSON(JSONObject pObj){
    JSONObject syncConfigJson = pObj.getJSONObject(KEY_SYNC_CONFIG);
    FHSyncConfig syncConfig = FHSyncConfig.fromJSON(syncConfigJson);
    this.mSyncConfig = syncConfig;
    this.mHashvalue = pObj.optString(KEY_HASHVALUE, null);
    JSONObject pendingJSON = pObj.getJSONObject(KEY_PENDING_RECORDS);
    Iterator<String> it = pendingJSON.keys();
    while(it.hasNext()){
      String key = it.next();
      JSONObject pendObjJson = pendingJSON.getJSONObject(key);
      FHSyncPendingRecord pending = FHSyncPendingRecord.fromJSON(pendObjJson);
      this.mPendingRecords.put(key, pending);
    }
    JSONObject dataJSON = pObj.getJSONObject(KEY_DATA_RECORDS);
    Iterator<String> dit = dataJSON.keys();
    while(dit.hasNext()){
      String dkey = dit.next();
      JSONObject dataObjJson = dataJSON.getJSONObject(dkey);
      FHSyncDataRecord datarecord = FHSyncDataRecord.fromJSON(dataObjJson);
      this.mDataRecords.put(dkey, datarecord);
    }
    if(pObj.has(KEY_SYNC_LOOP_START)){
      this.mSyncStart = new Date(pObj.getLong(KEY_SYNC_LOOP_START));
    }
    if(pObj.has(KEY_SYNC_LOOP_END)){
      this.mSyncEnd = new Date(pObj.getLong(KEY_SYNC_LOOP_END));
    }
    if(pObj.has(KEY_ACKNOWLEDGEMENTS)){
      this.mAcknowledgements = pObj.getJSONArray(KEY_ACKNOWLEDGEMENTS);
    }
    if(pObj.has(KEY_QUERY_PARAMS)){
      this.mQueryParams = pObj.getJSONObject(KEY_QUERY_PARAMS);
    }
    if(pObj.has(KEY_METADATA)){
      this.mMetaData = pObj.getJSONObject(KEY_METADATA);
    }
  }
  
  
  private void readFromFile(){
    String filePath = mDatasetId + STORAGE_FILE_EXT;
    try{
      FileInputStream fis = mContext.openFileInput(filePath);
      ByteArrayOutputStream bos = new ByteArrayOutputStream();
      writeStram(fis, bos);
      String content = bos.toString("UTF-8");
      JSONObject json = new JSONObject(content);
      fromJSON(json);
      doNotify(null, NotificationMessage.LOCAL_UPDATE_APPLIED_CODE, "load");
    } catch(FileNotFoundException ex){
      FHLog.w(LOG_TAG, "File not found for reading: " + filePath);
    } catch(IOException e){
      FHLog.e(LOG_TAG, "Error reading file : " + filePath, e);
    } catch(JSONException je){
      FHLog.e(LOG_TAG, "Failed to parse JSON file : " + filePath, je);
    }
  }
  
  public synchronized void writeToFile() {
    String filePath = mDatasetId + STORAGE_FILE_EXT;
    try{
      FileOutputStream fos = mContext.openFileOutput(filePath, Context.MODE_PRIVATE);
      String content = getJSON().toString();
      ByteArrayInputStream bis = new ByteArrayInputStream(content.getBytes());
      writeStram(bis, fos);
    } catch(FileNotFoundException ex){
      FHLog.e(LOG_TAG, "File not found for writing: " + filePath, ex);
      doNotify(null, NotificationMessage.CLIENT_STORAGE_FAILED_CODE, null);
    } catch(IOException e){
      FHLog.e(LOG_TAG, "Error writing file: " + filePath, e);
      doNotify(null, NotificationMessage.CLIENT_STORAGE_FAILED_CODE, null);
    }
  }
  
  private void doNotify(String pUID, int pCode, String pMessage){
    boolean sendMessage = false;
    switch(pCode){
    case NotificationMessage.SYNC_STARTED_CODE:
      if(mSyncConfig.isNotifySyncStarted()){
        sendMessage = true;
      }
      break;
    case NotificationMessage.SYNC_COMPLETE_CODE:
      if(mSyncConfig.isNotifySyncComplete()){
        sendMessage = true;
      }
      break;
    case NotificationMessage.OFFLINE_UPDATE_CODE:
      if(mSyncConfig.isNotifyOfflineUpdate()){
        sendMessage = true;
      }
      break;
    case NotificationMessage.COLLISION_DETECTED_CDOE:
      if(mSyncConfig.isNotifySyncCollisions()){
        sendMessage = true;
      }
      break;
    case NotificationMessage.REMOTE_UPDATE_FAILED_CDOE:
      if(mSyncConfig.isNotifyUpdateFailed()){
        sendMessage = true;
      }
      break;
    case NotificationMessage.REMOTE_UPDATE_APPLIED_CODE:
      if(mSyncConfig.isNotifyRemoteUpdateApplied()){
        sendMessage = true;
      }
      break;
    case NotificationMessage.LOCAL_UPDATE_APPLIED_CODE:
      if(mSyncConfig.isNotifyLocalUpdateApplied()){
        sendMessage = true;
      }
        break;
    case NotificationMessage.DELTA_RECEIVED_CODE:
      if(mSyncConfig.isNotifyDeltaReceived()){
        sendMessage = true;
      }
      break;
    case NotificationMessage.SYNC_FAILED_CODE:
      if(mSyncConfig.isNotifySyncFailed()){
        sendMessage = true;
      }
      break;
    case NotificationMessage.CLIENT_STORAGE_FAILED_CODE:
      if(mSyncConfig.isNotifyClientStorageFailed()){
        sendMessage = true;
      }
    default: 
      break;
    }
    if(sendMessage){
      NotificationMessage notification = NotificationMessage.getMessage(mDatasetId, pUID, pCode, pMessage);
      Message message = mNotificationHandler.obtainMessage(pCode, notification);
      mNotificationHandler.sendMessage(message);
    }
  }
  
  private void writeStram(InputStream pInput, OutputStream pOutput) throws IOException {
    if(null != pInput && null != pOutput){
      BufferedInputStream bis = new BufferedInputStream(pInput);
      BufferedOutputStream bos = new BufferedOutputStream(pOutput);
      byte[] buffer = new byte[1024];
      int count;
      while((count = bis.read(buffer)) != -1){
        bos.write(buffer, 0, count);
      }
      bos.close();
      bis.close();
    }
  }

  public void setSyncRunning(boolean pSyncRunning) {
    this.mSyncRunning = pSyncRunning;
  }
  
  public boolean isSyncRunning(){
    return mSyncRunning;
  }

  public void setInitialised(boolean pInitialised) {
    this.mInitialised = pInitialised;
  }

  public void setSyncPending(boolean pSyncPending) {
    this.mSyncPending = pSyncPending;
  }
  
  public boolean isSyncPending(){
    return mSyncPending;
  }

  public void setSyncConfig(FHSyncConfig pSyncConfig) {
    this.mSyncConfig = pSyncConfig;
  }
  
  public FHSyncConfig getSyncConfig(){
    return mSyncConfig;
  }

  public void setQueryParams(JSONObject pQueryParams) {
    this.mQueryParams = pQueryParams;
  }
  
  public void stopSync(boolean pStopSync){
    this.mStopSync = pStopSync;
  }
  
  public boolean isStopSync(){
    return mStopSync;
  }
  
  public Date getSyncStart(){
    return mSyncStart;
  }
  
  public Date getSyncEnd(){
    return mSyncEnd;
  }
  
  public void setContext(Context pContext){
    mContext = pContext;
  }
  
  public void setNotificationHandler(FHSyncNotificationHandler pHandler){
    mNotificationHandler = pHandler;
  }
  
}
