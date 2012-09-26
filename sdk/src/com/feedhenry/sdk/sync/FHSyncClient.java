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
import java.lang.ref.WeakReference;
import java.security.MessageDigest;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

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
import android.os.Looper;
import android.os.Message;

public class FHSyncClient {
  
  private static FHSyncClient mInstance;
  
  public static FHSyncClient getInstance(){
    if(null == mInstance){
      mInstance = new FHSyncClient();
    }
    return mInstance;
  }
  
  private static final int SYNC_STARTED_CODE = 0;
  private static final int SYNC_COMPLETE_CODE = 1;
  private static final int OFFLINE_UPDATE_CODE = 2;
  private static final int COLLISION_DETECTED_CDOE = 3;
  private static final int UPDATE_FAILED_CDOE = 4;
  private static final int UPDATE_APPLIED_CODE = 5;
  private static final int DELTA_RECEIVED_CODE= 6;
  private static final int CLIENT_STORAGE_FAILED_CODE = 7;
  private static final int SYNC_FAILED_CODE = 8;
  
  private static final String SYNC_STARTED_MESSAGE = "SYNC_STARTED";
  private static final String SYNC_COMPLETE_MESSAGE = "SYNC_COMPLETE";
  private static final String OFFLINE_UPDATE_MESSAGE = "OFFLINE_UPDATE";
  private static final String COLLISION_DETECTED_MESSAGE = "COLLISION_DETECTED";
  private static final String UPDATE_FAILED_MESSAGE = "UPDATE_FAILED";
  private static final String UPDATE_APPLIED_MESSAGE = "UPDATE_APPLIED";
  private static final String DELTA_RECEIVED_MESSAGE = "DELTA_RECEIVED";
  private static final String CLIENT_STORAGE_FAILED_MESSAGE = "CLIENT_STORAGE_FAILED";
  private static final String SYNC_FAILED_MESSAGE = "SYNC_FAILED";
  
  private static Map<Integer, String> mMessageMap = new HashMap<Integer, String>();
  
  protected static final String LOG_TAG = "com.feedhenry.sdk.sync.FHSyncClient";
  
  private static final String STORAGE_FILE = ".fh_sync.json";
  
  private static final char[] DIGITS = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a',
    'b', 'c', 'd', 'e', 'f' };
  
  static {
    mMessageMap.put(SYNC_STARTED_CODE, SYNC_STARTED_MESSAGE);
    mMessageMap.put(SYNC_COMPLETE_CODE, SYNC_COMPLETE_MESSAGE);
    mMessageMap.put(OFFLINE_UPDATE_CODE, OFFLINE_UPDATE_MESSAGE);
    mMessageMap.put(COLLISION_DETECTED_CDOE, COLLISION_DETECTED_MESSAGE);
    mMessageMap.put(UPDATE_FAILED_CDOE, UPDATE_FAILED_MESSAGE);
    mMessageMap.put(UPDATE_APPLIED_CODE, UPDATE_APPLIED_MESSAGE);
    mMessageMap.put(DELTA_RECEIVED_CODE, DELTA_RECEIVED_MESSAGE);
    mMessageMap.put(CLIENT_STORAGE_FAILED_CODE, CLIENT_STORAGE_FAILED_MESSAGE);
    mMessageMap.put(SYNC_FAILED_CODE, SYNC_FAILED_MESSAGE);
  }
  
  private Context mContext;
  private JSONObject mDataSets;
  private FHSyncConfig mConfig = new FHSyncConfig();
  private FHSyncListener mSyncListener = null;
  
  private NetworkReceiver mReceiver;
  private boolean mIsOnLine;
  
  private HashMap<String, SyncTask> mSyncTasks;
  private HashMap<String, Thread> mSyncTaskThreads;
  
  private NotificationHandler mNotificationHandler;
  
  private boolean mInitialised = false;
 
  public void init(Context pContext, FHSyncConfig pConfig){
    mContext = pContext;
    mConfig = pConfig;
    mSyncTasks = new HashMap<String, SyncTask>();
    mSyncTaskThreads = new HashMap<String, Thread>();
    initHanlders();
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
    mInitialised = true;
  }
  
  private void initHanlders(){
    if(null == mNotificationHandler){
      if(null != Looper.myLooper()){
        mNotificationHandler = new NotificationHandler(this);
      } else {
        HandlerThread ht = new HandlerThread("FHSyncClientNotificationHanlder");
        mNotificationHandler = new NotificationHandler(ht.getLooper(), this);
        ht.start();
      }
    }
  }
  
  public void setListener(FHSyncListener pListener){
    mSyncListener = pListener;
  }
  
  public void manage(String pDataId, JSONObject pQueryParams) throws Exception {
    if(!mInitialised){
      throw new Exception("FHSyncClient isn't initialised. Have you called the init function?");
    }
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
  
  public JSONObject create(String pDataId, JSONObject pData) throws Exception {
    return this.addPendingObject(pDataId, null, pData, "create");
  }
  
  public JSONObject update(String pDataId, String pUID, JSONObject pData) throws Exception {
   return this.addPendingObject(pDataId, pUID, pData, "update"); 
  }
  
  public JSONObject delete(String pDataId, String pUID) throws Exception {
    return this.addPendingObject(pDataId, pUID, null, "delete");
  }
  
  private JSONObject addPendingObject(String pDataId, String pUID, JSONObject pData, String pAction) throws Exception {
    if(!mIsOnLine){
      this.doNotify(pDataId, pUID, OFFLINE_UPDATE_CODE, pAction);
    }
    JSONObject data = read(pDataId, pUID);
    JSONObject pendingObj = null;
    pendingObj = new JSONObject();
    pendingObj.put("uid", pUID);
    pendingObj.put("action", pAction);
    pendingObj.put("pre", data.optJSONObject("data"));
    pendingObj.put("post", pData);
    String hash = generateHash(pendingObj.toString());
    pendingObj.put("hash", hash);
    pendingObj.put("timestamp", new Date().getTime());
    
    JSONObject dataset = mDataSets.optJSONObject(pDataId);
    if(null != dataset){
      dataset.getJSONObject("pending").put(hash, pendingObj);
      flushData();
    }
    return pendingObj;
  }
  
  private void doNotify(String pDataId, String pUID, int pCode, String pMessage){
    NotificationMessage notification = new NotificationMessage(pDataId, pUID, mMessageMap.get(pCode), pMessage);
    Message message = mNotificationHandler.obtainMessage(pCode, notification);
    mNotificationHandler.sendMessage(message);
  }
  
  private void handleMessage(Message pMsg){
    int code = pMsg.what;
    NotificationMessage notification = (NotificationMessage) pMsg.obj;
    FHLog.d(LOG_TAG, notification.toString());
    if(null != mSyncListener){
      switch(code){
      case SYNC_STARTED_CODE:
        if(mConfig.isNotifySyncStarted()){
          mSyncListener.onSyncStarted(notification);
        }
        break;
      case SYNC_COMPLETE_CODE:
        if(mConfig.isNotifySyncComplete()){
          mSyncListener.onSyncCompleted(notification);
        }
        break;
      case OFFLINE_UPDATE_CODE:
        if(mConfig.isNotifyOfflineUpdate()){
          mSyncListener.onUpdateOffline(notification);
        }
        break;
      case COLLISION_DETECTED_CDOE:
        if(mConfig.isNotifySyncCollisions()){
          mSyncListener.onCollisionDetected(notification);
        }
        break;
      case UPDATE_FAILED_CDOE:
        if(mConfig.isNotifyUpdateFailed()){
          mSyncListener.onUpdateFailed(notification);
        }
        break;
      case UPDATE_APPLIED_CODE:
        if(mConfig.isNotifyUpdateApplied()){
          mSyncListener.onUpdateApplied(notification);
        }
        break;
      case DELTA_RECEIVED_CODE:
        if(mConfig.isNotifyDeltaReceived()){
          mSyncListener.onDeltaReceived(notification);
        }
        break;
      case SYNC_FAILED_CODE:
        if(mConfig.isNotifySyncFailed()){
          mSyncListener.onSyncFailed(notification);
        }
        break;
      case CLIENT_STORAGE_FAILED_CODE:
        if(mConfig.isNotifyClientStorageFailed()){
          mSyncListener.onClientStorageFailed(notification);
        }
      default: 
        break;
      }
    }
  }
  
  private void syncLoop(String pDataId) throws Exception {
    SyncTask task = mSyncTasks.get(pDataId);
    Thread syncTaskThread = mSyncTaskThreads.get(pDataId);
    if(null == task){
      task = new SyncTask(pDataId);
      task.setInterval(mConfig.getSyncFrequency() * 1000);
      mSyncTasks.put(pDataId, task);
    }
    if(null == syncTaskThread){
      syncTaskThread = new Thread(task);
      mSyncTaskThreads.put(pDataId, syncTaskThread);
      syncTaskThread.start();
    }
   
  }
  
  public void stop(String pDataId) throws Exception {
    SyncTask task = mSyncTasks.get(pDataId);
    Thread syncTaskThread = mSyncTaskThreads.get(pDataId);
    if(null != task){
      task.setContinue(false);
      mSyncTasks.remove(pDataId);
    }
    if(null != syncTaskThread){
      syncTaskThread.interrupt();
      mSyncTaskThreads.remove(pDataId);
    }
  }
  
  public void destroy() throws Exception {
    if(mInitialised){
      Iterator<String> keyIt = mDataSets.keys();
      while(keyIt.hasNext()){
        String dataId = keyIt.next();
        stop(dataId);
      }
      mSyncTasks = null;
      mSyncTaskThreads = null;
      mSyncListener = null;
      mNotificationHandler = null;
      mDataSets = null;
      mContext.unregisterReceiver(mReceiver);
      mInitialised = false;
    }
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
  
  private synchronized void flushData() {
    try{
      FileOutputStream fos = mContext.openFileOutput(STORAGE_FILE, Context.MODE_PRIVATE);
      ByteArrayInputStream bis = new ByteArrayInputStream(mDataSets.toString().getBytes());
      writeStram(bis, fos);
    } catch(FileNotFoundException ex){
      FHLog.e(LOG_TAG, "File not found for writing: " + STORAGE_FILE, ex);
      doNotify(null, null, CLIENT_STORAGE_FAILED_CODE, null);
    } catch(IOException e){
      FHLog.e(LOG_TAG, "Error writing file: " + STORAGE_FILE, e);
      doNotify(null, null, CLIENT_STORAGE_FAILED_CODE, null);
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
  
  static class NotificationHandler extends Handler {
    private final WeakReference<FHSyncClient> mService;
    
    public NotificationHandler(FHSyncClient pService) {
      super();
      mService = new WeakReference<FHSyncClient>(pService);
    }
    
    public NotificationHandler(Looper pLooper, FHSyncClient pService) {
      super(pLooper);
      mService = new WeakReference<FHSyncClient>(pService);
    }
    
    public void handleMessage(Message msg){
      FHSyncClient target = mService.get();
      if(null != target){
        target.handleMessage(msg);
      }
    }
    
  }
  
  private class SyncTask implements Runnable{
    
    private String mDataId;
    private boolean mContinue = true;
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
          params.put("dataset_hash", dataset.optString("hash", null));
          JSONObject pendings = dataset.getJSONObject("pending");
          JSONArray pendingArray = new JSONArray();
          Iterator<String> it = pendings.keys();
          while(it.hasNext()){
            String key = it.next();
            pendingArray.put(pendings.get(key));
          }
          params.put("pending", pendingArray);
          
          FHActRequest request = FH.buildActRequest(mDataId, params);
          request.executeAsync(new FHActCallback() {
            
            private void processData(JSONObject pUpdateData, int pNotificationCode) throws JSONException{
              Iterator<String> it = pUpdateData.keys();
              while(it.hasNext()){
                String key = it.next();
                JSONObject rec = pUpdateData.getJSONObject(key);
                mDataSets.getJSONObject(mDataId).getJSONObject("pending").remove(key);
                doNotify(mDataId, rec.getString("uid"), pNotificationCode, rec.toString());
              }
            }
            
            @Override
            public void success(FHResponse pResponse) {
              FHLog.d(LOG_TAG, "FHSync success response: " + pResponse.getJson().toString());
              try{
                JSONObject resData = pResponse.getJson();
                if(resData.has("updates")){
                  JSONObject updatesData = resData.getJSONObject("updates");
                  if(updatesData.has("applied")){
                    processData(updatesData.getJSONObject("applied"), UPDATE_APPLIED_CODE);
                  }
                  if(updatesData.has("failed")){
                    processData(updatesData.getJSONObject("failed"), UPDATE_FAILED_CDOE);
                  }
                  if(updatesData.has("collisions")){
                    processData(updatesData.getJSONObject("collisions"), COLLISION_DETECTED_CDOE);
                  }
                }
                
                if(resData.has("records")){
                  JSONObject dataSet = mDataSets.getJSONObject(mDataId);
                  dataSet.put("data", resData.getJSONObject("records"));
                  dataSet.put("hash", resData.getString("hash"));
                  doNotify(mDataId, null, DELTA_RECEIVED_CODE, null);
                  syncComplete("online");
                } else if(resData.has("hash")){
                  String resDatasetHash = resData.getString("hash");
                  if(!resDatasetHash.equals(mDataSets.getJSONObject(mDataId).optString("hash", null))){
                    syncRecords();
                  } else {
                    syncComplete("online");
                  }
                } else {
                  syncComplete("online");
                }
                
              } catch (Exception e){
                FHLog.e(LOG_TAG, "Error when processing SyncTask response: " + e.getMessage(), e);
                syncComplete(e.getMessage());
              }
            }
            
            @Override
            public void fail(FHResponse pResponse) {
              FHLog.e(LOG_TAG, "FHSync fail response:: " + pResponse.getRawResponse(), pResponse.getError());
              doNotify(mDataId, null, SYNC_FAILED_CODE, pResponse.getRawResponse() + " :: " + pResponse.getError().toString());
              syncComplete(pResponse.getErrorMessage());
            }
          });
        }
      }
    }
    
    private void syncRecords() throws Exception {
      JSONObject dataset = mDataSets.optJSONObject(mDataId);
      if(null != dataset){
        JSONObject localdataset = dataset.optJSONObject("data");
        JSONObject recHash = new JSONObject();
        if(null != localdataset){
          Iterator<String> keyIt  = localdataset.keys();
          while(keyIt.hasNext()){
            String key = keyIt.next();
            Object tmp = localdataset.get(key);
            if(tmp instanceof JSONObject){
              JSONObject data = (JSONObject) tmp;
              String hash = data.getString("hash");
              recHash.put(key, hash);
            }
          }
        }
        JSONObject syncRecParams = new JSONObject();
        syncRecParams.put("fn", "syncRecords");
        syncRecParams.put("dataset_id", mDataId);
        syncRecParams.put("query_params", dataset.getJSONObject("config").optJSONObject("query_params"));
        syncRecParams.put("clientRecs", recHash);
        
        FHActRequest request = FH.buildActRequest(mDataId, syncRecParams);
        request.executeAsync(new FHActCallback() {
          
          @Override
          public void success(FHResponse pResponse) {
            FHLog.d(LOG_TAG, "FHSyncRecords success response: " + pResponse.getJson().toString());
            JSONObject resData = pResponse.getJson();
            try{
              JSONObject localdataset = mDataSets.getJSONObject(mDataId).optJSONObject("data");
              if(null == localdataset){
                localdataset = new JSONObject();
                mDataSets.getJSONObject(mDataId).put("data", localdataset);
              }
              if(resData.has("create")){
                JSONObject createData = resData.getJSONObject("create");
                Iterator<String> it = createData.keys();
                while(it.hasNext()){
                  String key = it.next();
                  JSONObject obj = createData.getJSONObject(key);
                  localdataset.put(key, new JSONObject().put("hash", obj.getString("hash")).put("data", obj.getJSONObject("data")));
                  doNotify(mDataId, key, DELTA_RECEIVED_CODE, "create");
                }
              }
              if(resData.has("update")){
                JSONObject updateData = resData.getJSONObject("update");
                Iterator<String> it = updateData.keys();
                while(it.hasNext()){
                  String key = it.next();
                  localdataset.getJSONObject(key).put("hash", updateData.getJSONObject(key).getString("hash")).put("data", updateData.getJSONObject(key).getJSONObject("data"));
                  doNotify(mDataId, key, DELTA_RECEIVED_CODE, "update");
                }
              }
              if(resData.has("delete")){
                JSONObject deleteData = resData.getJSONObject("delete");
                Iterator<String> it = deleteData.keys();
                while(it.hasNext()){
                  String key = it.next();
                  localdataset.remove(key);
                  doNotify(mDataId, key, DELTA_RECEIVED_CODE, "delete");
                }
              }
              
              if(resData.has("hash")){
                localdataset.put("hash", resData.getString("hash"));
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
