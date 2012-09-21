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
import java.security.MessageDigest;
import java.util.Date;
import java.util.Iterator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.feedhenry.sdk.FH;
import com.feedhenry.sdk.FHActCallback;
import com.feedhenry.sdk.FHResponse;
import com.feedhenry.sdk.api.FHActRequest;
import com.feedhenry.sdk.utils.FHLog;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;

public class FHSyncClient {
  
  private static FHSyncClient mInstance;
  
  public static FHSyncClient getInstance(){
    if(null == mInstance){
      mInstance = new FHSyncClient();
    }
    return mInstance;
  }
  
  private static final String SYNC_STARTED_CODE = "SYNC_STARTED";
  private static final String SYNC_COMPLETE_CODE = "SYNC_COMPLETE";
  private static final String OFFLINE_UPDATE_CODE = "OFFLINE_UPDATE";
  private static final String COLLISION_DETECTED_CDOE = "COLLISION_DETECTED";
  private static final String UPDATE_FAILED_CDOE = "UPDATE_FAILED";
  private static final String UPDATE_APPLIED_CODE = "UPDATE_APPLIED";
  private static final String DELTA_RECEIVED = "DELTA_RECEIVED";
  
  protected static final String LOG_TAG = "com.feedhenry.sdk.sync.FHSyncClient";
  
  private static final String STORAGE_FILE = ".fh_sync.json";
  
  private static final char[] DIGITS = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a',
    'b', 'c', 'd', 'e', 'f' };
  
  private Context mContext;
  private JSONObject mDataSets;
  private FHSyncConfig mConfig = new FHSyncConfig();
  private FHSyncListener mSyncListener = null;
  
  private NetworkReceiver mReceiver;
  private boolean mIsOnLine;
  
  private SyncTask mSyncTask;
  private Thread mSyncTaskThread;
  
  private Handler mNotificationHandler;
 
  public void init(Context pContext, FHSyncConfig pConfig){
    mContext = pContext;
    mConfig = pConfig;
    checkNetworkStatus();
    IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
    mReceiver = new NetworkReceiver();
    mContext.registerReceiver(mReceiver, filter);
    try{
      FileInputStream fis = mContext.openFileInput(STORAGE_FILE);
      ByteArrayOutputStream bos = new ByteArrayOutputStream();
      writeStram(fis, bos);
      String content = bos.toString("UTF-8");
      mDataSets = new JSONObject(content);
    } catch(FileNotFoundException ex){
      FHLog.e(LOG_TAG, "File not found for reading: " + STORAGE_FILE, ex);
      mDataSets = new JSONObject();
    } catch(IOException e){
      FHLog.e(LOG_TAG, "Error reading file : " + STORAGE_FILE, e);
      mDataSets = new JSONObject();
    } catch(JSONException je){
      FHLog.e(LOG_TAG, "Failed to parse JSON file : " + STORAGE_FILE, je);
      mDataSets = new JSONObject();
    }
  }
  
  public void setListener(FHSyncListener pListener){
    mSyncListener = pListener;
    HandlerThread ht = new HandlerThread("FHSyncClientNotification");
    ht.start();
    mNotificationHandler = new Handler(ht.getLooper()){
      public void handleMessage(Message msg){
        FHSyncClient.this.handleMessage(msg);
      }
    }; 
  }
  
  public void manage(String pDataId, JSONObject pQueryParams) throws Exception {
    if(!mDataSets.has(pDataId)){
      JSONObject dataSet = new JSONObject();
      dataSet.put("pending", new JSONObject());
      JSONObject dataSetConfig = new JSONObject();
      dataSetConfig.put("query_params", pQueryParams);
      dataSet.put("config", dataSetConfig);
      mDataSets.put(pDataId, dataSet);
    }
    syncLoop(pDataId);
  }
  
  public JSONObject list(String pDataId) {
    JSONObject dataset = mDataSets.optJSONObject(pDataId);
    JSONObject data = null;
    if(null != dataset){
      data = dataset.optJSONObject("data");
    }
    return data;
  }
  
  public JSONObject read(String pDataId, String pUID) {
    JSONObject dataset = list(pDataId);
    JSONObject data = null;
    if(null != dataset){
      data = dataset.optJSONObject(pUID);
    }
    return data;
  }
  
  public JSONObject update(String pDataId, String pUID, JSONObject pData) throws Exception {
   return this.addPendingObject(pDataId, pUID, pData, "update"); 
  }
  
  public JSONObject delete(String pDataId, String pUID, JSONObject pData) throws Exception {
    return this.addPendingObject(pDataId, pUID, pData, "delete");
  }
  
  private JSONObject addPendingObject(String pDataId, String pUID, JSONObject pData, String pAction) throws Exception {
    if(!mIsOnLine){
      this.doNotify(pDataId, pUID, OFFLINE_UPDATE_CODE, pAction);
    }
    JSONObject data = read(pDataId, pUID);
    JSONObject pendingObj = null;
    if(null != data){
      pendingObj = new JSONObject();
      pendingObj.put("uid", pUID);
      pendingObj.put("timestamp", new Date().getTime());
      pendingObj.put("action", pAction);
      pendingObj.put("pre", data);
      pendingObj.put("post", pData);
      String hash = generateHash(pendingObj.toString());
      pendingObj.put("hash", hash);
      
      JSONObject dataset = mDataSets.optJSONObject(pDataId);
      if(null != dataset){
        dataset.getJSONObject("pending").put(hash, pendingObj);
      }
    }
    return pendingObj;
  }
  
  private void doNotify(String pDataId, String pUID, String pCode, String pMessage){
    
  }
  
  private void handleMessage(Message pMsg){
    
  }
  
  private void syncLoop(String pDataId) throws Exception {
    if(null == mSyncTask){
      mSyncTask = new SyncTask(pDataId);
      mSyncTask.setInterval(mConfig.getSyncFrequency() * 1000);
    }
    if(null == mSyncTaskThread){
      mSyncTaskThread = new Thread(mSyncTask);
    }
    mSyncTaskThread.start();
  }
  
  public void stop(String pDataId) throws Exception {
    mContext.unregisterReceiver(mReceiver);
    if(null != mSyncTask){
      mSyncTask.setContinue(false);
    }
    if(null != mSyncTaskThread){
      mSyncTaskThread.interrupt();
    }
    mSyncTask = null;
    mSyncTaskThread = null;
  }
  
  private String generateHash(String pData) {
    String hashValue = null;
    try{
      MessageDigest md = MessageDigest.getInstance("SHA-1");
      md.reset();
      md.update(pData.getBytes("UTF-8"));
      hashValue = encodeHex(md.digest());
    } catch(Exception e){
      FHLog.e(LOG_TAG, "Failed to generate hash value for data: " + pData, e);
    }
    return hashValue;
  }
  
  private String encodeHex(byte[] pData) {
    int l = pData.length;

    char[] out = new char[l << 1];

    // two characters form the hex value.
    for (int i = 0, j = 0; i < l; i++) {
      out[j++] = DIGITS[(0xF0 & pData[i]) >>> 4];
      out[j++] = DIGITS[0x0F & pData[i]];
    }

    return new String(out);
  }
  
  private void checkNetworkStatus() {
    ConnectivityManager connMgr = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
    NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
    if(null != networkInfo && networkInfo.isConnected()){
      String type = networkInfo.getTypeName();
      FHLog.i(LOG_TAG, "Device is online. Connection type : " + type);
      mIsOnLine = true;
    } else {
      FHLog.i(LOG_TAG, "Device is offline.");
      mIsOnLine = false;
    }
    
  }
  
  private void flushData() {
    try{
      FileOutputStream fos = mContext.openFileOutput(STORAGE_FILE, Context.MODE_PRIVATE);
      ByteArrayInputStream bis = new ByteArrayInputStream(mDataSets.toString().getBytes());
      writeStram(bis, fos);
    } catch(FileNotFoundException ex){
      FHLog.e(LOG_TAG, "File not found for writing: " + STORAGE_FILE, ex);
    } catch(IOException e){
      FHLog.e(LOG_TAG, "Error writing file: " + STORAGE_FILE, e);
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
  
  
  private class NetworkReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
      checkNetworkStatus();
    }
    
  }
  
  private class SyncTask implements Runnable{
    
    private String mDataId;
    private boolean mContinue;
    private long mInterval;
    private boolean mIntervalChanged;
    
    public SyncTask(String pDataId) {
      this.mDataId = pDataId;
    }
    
    public void setContinue(boolean pContinue){
      mContinue = pContinue;
    }
    
    public void setInterval(long pInterval){
      if(mInterval != pInterval){
        mInterval = pInterval;
        mIntervalChanged = true;
      }
    }
    
    private void startSync() throws Exception {
      doNotify(mDataId, null, SYNC_STARTED_CODE, null);
      if(!mIsOnLine){
        syncComplete("offline");
      } else {
        JSONObject dataset = mDataSets.optJSONObject(mDataId);
        if(null != dataset){
          JSONObject params = new JSONObject();
          params.put("fn", "sync");
          params.put("dataset_id", mDataId);
          params.put("query_params", dataset.getJSONObject("config").optJSONObject("query_params"));
          params.put("dataset_hash", dataset.getString("hash"));
          params.put("pending", dataset.getJSONObject("pending"));
          
          FHActRequest request = FH.buildActRequest(mDataId, params);
          request.executeAsync(new FHActCallback() {
            
            @Override
            public void success(FHResponse pResponse) {
              FHLog.d(LOG_TAG, "FHSync success response: " + pResponse.getJson().toString());
              try{
                JSONObject resData = pResponse.getJson();
                if(resData.has("updates")){
                  JSONObject updatesData = resData.getJSONObject("updates");
                  if(updatesData.has("applied")){
                    JSONArray appliedData = updatesData.getJSONArray("applied");
                    for(int i=0;i<appliedData.length();i++){
                      JSONObject rec = appliedData.getJSONObject(i);
                      String hash = rec.getString("hash");
                      mDataSets.getJSONObject(mDataId).getJSONObject("pending").remove(hash);
                      doNotify(mDataId, rec.getString("uid"), UPDATE_APPLIED_CODE, hash);
                    }
                  }
                  if(updatesData.has("failed")){
                    JSONArray failedData = updatesData.getJSONArray("failed");
                    for(int i=0;i<failedData.length();i++){
                      JSONObject rec = failedData.getJSONObject(i);
                      String hash = rec.getString("hash");
                      mDataSets.getJSONObject(mDataId).getJSONObject("pending").remove(hash);
                      doNotify(mDataId, rec.getString("uid"), COLLISION_DETECTED_CDOE, hash);
                    }
                  }
                }
                
                if(resData.has("dataset")){
                  mDataSets.put(mDataId, resData.getJSONObject("dataset"));
                  doNotify(mDataId, null, DELTA_RECEIVED, null);
                }
                
                if(resData.has("dataset_hash")){
                  String resDatasetHash = resData.getString("dataset_hash");
                  if(!resDatasetHash.equals(mDataSets.getJSONObject(mDataId).getString("hash"))){
                    syncRecords();
                  } else {
                    syncComplete("online");
                  }
                }
                
              } catch (Exception e){
                FHLog.e(LOG_TAG, "Error when processing SyncTask response: " + e.getMessage(), e);
                syncComplete(e.getMessage());
              }
            }
            
            @Override
            public void fail(FHResponse pResponse) {
              FHLog.e(LOG_TAG, "FHSync fail response:: " + pResponse.getRawResponse(), pResponse.getError());
              syncComplete(pResponse.getErrorMessage());
            }
          });
        }
      }
    }
    
    private void syncRecords() throws Exception {
      JSONObject dataset = mDataSets.optJSONObject(mDataId);
      if(null != dataset){
        JSONObject localdataset = dataset.getJSONObject("data");
        JSONObject recHash = new JSONObject();
        Iterator<String> keyIt  = localdataset.keys();
        while(keyIt.hasNext()){
          String key = keyIt.next();
          JSONObject data = localdataset.getJSONObject(key);
          String hash = data.getString("hash");
          recHash.put(key, hash);
        }
        JSONObject syncRecParams = new JSONObject();
        syncRecParams.put("fn", "syncRecords");
        syncRecParams.put("dataset_id", mDataId);
        syncRecParams.put("query_params", dataset.getJSONObject("config").optJSONObject("query_params"));
        syncRecParams.put("record_hashes", recHash);
        
        FHActRequest request = FH.buildActRequest(mDataId, syncRecParams);
        request.executeAsync(new FHActCallback() {
          
          @Override
          public void success(FHResponse pResponse) {
            FHLog.d(LOG_TAG, "FHSyncRecords success response: " + pResponse.getJson().toString());
            JSONObject resData = pResponse.getJson();
            try{
              JSONObject localdataset = mDataSets.getJSONObject(mDataId).getJSONObject("data");
              if(resData.has("create")){
                JSONObject createData = resData.getJSONObject("create");
                Iterator<String> it = createData.keys();
                while(it.hasNext()){
                  String key = it.next();
                  localdataset.put(key, createData.getJSONObject(key));
                  doNotify(mDataId, key, DELTA_RECEIVED, "create");
                }
              }
              if(resData.has("update")){
                JSONObject updateData = resData.getJSONObject("update");
                Iterator<String> it = updateData.keys();
                while(it.hasNext()){
                  String key = it.next();
                  localdataset.getJSONObject(key).put("hash", updateData.getJSONObject(key).getString("hash")).put("data", updateData.getJSONObject(key).getJSONObject("data"));
                  doNotify(mDataId, key, DELTA_RECEIVED, "update");
                }
              }
              if(resData.has("delete")){
                JSONObject deleteData = resData.getJSONObject("delete");
                Iterator<String> it = deleteData.keys();
                while(it.hasNext()){
                  String key = it.next();
                  localdataset.remove(key);
                  doNotify(mDataId, key, DELTA_RECEIVED, "delete");
                }
              }
              syncComplete("online");
            } catch (Exception e){
              FHLog.e(LOG_TAG, "Error when processing sycnRecords response: " + e.getMessage(), e);
              syncComplete(e.getMessage());
            }
            
          }
          
          @Override
          public void fail(FHResponse pResponse) {
            FHLog.e(LOG_TAG, "FHSyncRecords fail response:: " + pResponse.getRawResponse(), pResponse.getError());
            syncComplete(pResponse.getErrorMessage());
          }
        });
      }
    }
    
    private void syncComplete(String pStatus){
      flushData();
      doNotify(mDataId, null, SYNC_COMPLETE_CODE, pStatus);
      long threadId = Thread.currentThread().getId();
      if(mContinue && !Thread.interrupted()){
        try{
          Thread.sleep(mInterval);
          startSync();  
        } catch (InterruptedException e){
          FHLog.e(LOG_TAG, "[" + threadId + "] SyncTask - Thread interrupted unexpectly: " + e.getMessage(), e);
        } catch (Exception ex){
          FHLog.e(LOG_TAG, "Failed to start SyncTask", ex);
        } 
      } else {
        FHLog.i(LOG_TAG, "SyncTask discontinued.");
      }
      
    }
    
    @Override
    public void run() {
      try{
        startSync();
      } catch (Exception e){
        FHLog.e(LOG_TAG, "Failed to start SyncTask", e);
      }
    }
    
  }
  
  
  

}
