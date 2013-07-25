package com.feedhenry.sdk.sync;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.json.fh.JSONObject;

import android.content.Context;
import android.os.HandlerThread;
import android.os.Looper;
import android.util.Log;

import com.feedhenry.sdk.FH;
import com.feedhenry.sdk.FHActCallback;
import com.feedhenry.sdk.api.FHActRequest;
import com.feedhenry.sdk.exceptions.FHNotReadyException;
import com.feedhenry.sdk.utils.FHLog;

public class FHSyncClient {
  
  private static FHSyncClient mInstance;
  
  public static FHSyncClient getInstance(){
    if(null == mInstance){
      mInstance = new FHSyncClient();
    }
    return mInstance;
  }
  
  protected static final String LOG_TAG = "com.feedhenry.sdk.sync.FHSyncClient";
  
  private Context mContext;
  private Map<String, FHSyncDataset> mDataSets = new HashMap<String, FHSyncDataset>();
  private FHSyncConfig mConfig = new FHSyncConfig();
  private FHSyncListener mSyncListener = null;
  
  private FHSyncNotificationHandler mNotificationHandler;
  
  private boolean mInitialised = false;
  private MonitorTask mMonitorTask = null;
  
  private ExecutorService mExecutors = Executors.newFixedThreadPool(3);
 
  public void init(Context pContext, FHSyncConfig pConfig, FHSyncListener pListener){
    mContext = pContext;
    mConfig = pConfig;
    mSyncListener = pListener;
    initHanlders();
    mInitialised = true;
    if(null == mMonitorTask){
      mMonitorTask = new MonitorTask();
      mMonitorTask.start();
    }
  }
  
  private void initHanlders(){
    if(null != Looper.myLooper()){
      mNotificationHandler = new FHSyncNotificationHandler(this.mSyncListener);
    } else {
      HandlerThread ht = new HandlerThread("FHSyncClientNotificationHanlder");
      mNotificationHandler = new FHSyncNotificationHandler(ht.getLooper(), this.mSyncListener);
      ht.start();
    }
  }
  
  public void setListener(FHSyncListener pListener){
    mSyncListener = pListener;
    if(null != mNotificationHandler){
      mNotificationHandler.setSyncListener(mSyncListener);
    }
  }
  
  public void manage(String pDataId, FHSyncConfig pConfig, JSONObject pQueryParams) throws Exception {
    if(!mInitialised){
      throw new Exception("FHSyncClient isn't initialised. Have you called the init function?");
    }
    FHSyncDataset dataset = mDataSets.get(pDataId);
    FHSyncConfig syncConfig = mConfig;
    if(null != pConfig){
      syncConfig = pConfig;
    }
    if(null != dataset){
      dataset.setContext(mContext);
      dataset.setNotificationHandler(mNotificationHandler);
    } else {
      dataset = new FHSyncDataset(mContext, mNotificationHandler, pDataId, syncConfig, pQueryParams);
      mDataSets.put(pDataId, dataset);
      dataset.setSyncRunning(false);
      dataset.setInitialised(true);
    }
    
    dataset.setSyncConfig(syncConfig);
    dataset.setSyncPending(true);
    
    dataset.writeToFile();
  }
  
  public JSONObject list(String pDataId) {
    FHSyncDataset dataset = mDataSets.get(pDataId);
    JSONObject data = null;
    if(null != dataset){
      data = dataset.listData();
    }
    return data;
  }
  
  public JSONObject read(String pDataId, String pUID) {
    FHSyncDataset dataset = mDataSets.get(pDataId);
    JSONObject data = null;
    if(null != dataset){
      data = dataset.readData(pUID);
    }
    return data;
  }
  
  public JSONObject create(String pDataId, JSONObject pData) throws Exception {
    FHSyncDataset dataset = mDataSets.get(pDataId);
    if(null != dataset){
      return dataset.createData(pData);
    } else {
      throw new Exception("Unkonw dataId : " + pDataId);
    }
  }
  
  public JSONObject update(String pDataId, String pUID, JSONObject pData) throws Exception {
    FHSyncDataset dataset = mDataSets.get(pDataId);
    if(null != dataset){
      return dataset.updateData(pUID, pData);
    } else {
      throw new Exception("Unkonw dataId : " + pDataId);
    }
  }
  
  public JSONObject delete(String pDataId, String pUID) throws Exception {
    FHSyncDataset dataset = mDataSets.get(pDataId);
    if(null != dataset){
      return dataset.deleteData(pUID);
    } else {
      throw new Exception("Unkonw dataId : " + pDataId);
    }
    
  }
  
  public void listCollisions(String pDataId, FHActCallback pCallback) throws Exception {
    JSONObject params = new JSONObject();
    params.put("fn", "listCollisions");
    FHActRequest request = FH.buildActRequest(pDataId, params);
    request.executeAsync(pCallback);
  }
  
  public void removeCollision(String pDataId, String pCollisionHash, FHActCallback pCallback) throws Exception {
    JSONObject params = new JSONObject();
    params.put("fn", "removeCollision");
    FHActRequest request = FH.buildActRequest(pDataId, params);
    request.executeAsync(pCallback);
  }
  
  public void stop(String pDataId) throws Exception {
    FHSyncDataset dataset = mDataSets.get(pDataId);
    if(null != dataset){
      dataset.stopSync(true);
    }
  }
  
  public void destroy() throws Exception {
    if(mInitialised){
      if(null != mMonitorTask){
        mMonitorTask.stopRunning();
        mMonitorTask.stop();
      }
      for(String key: mDataSets.keySet()){
        stop(key);
      }
      mSyncListener = null;
      mNotificationHandler = null;
      mDataSets = null;
      mInitialised = false;
    }
  }
  
  private class MonitorTask extends Thread{

    private boolean mKeepRunning = true;
    
    public void stopRunning(){
      mKeepRunning = false;
    }
    
    private void checkDatasets(){
      if(null != mDataSets){
        for(Map.Entry<String, FHSyncDataset> entry: mDataSets.entrySet()){
          final FHSyncDataset dataset = entry.getValue();
          boolean syncRunning = dataset.isSyncRunning();
          if(!syncRunning && !dataset.isStopSync()){
            //sync isn't running for dataId at the moment, check if needs to start it
            Date lastSyncStart = dataset.getSyncStart();
            Date lastSyncEnd = dataset.getSyncEnd();
            if(null == lastSyncStart){
              dataset.setSyncPending(true);
            } else if(null != lastSyncEnd){
              long interval = new Date().getTime() - lastSyncEnd.getTime();
              if(interval > dataset.getSyncConfig().getSyncFrequency()*1000){
                Log.d(LOG_TAG, "Should start sync!!");
                dataset.setSyncPending(true);
              }
            }
            
            if(dataset.isSyncPending()){
              mExecutors.submit(new Runnable() {
                @Override
                public void run() {
                  dataset.startSyncLoop();
                }
              });
            }
          }
        }
      }
    }
    
    @Override
    public void run() {
      while(mKeepRunning && !isInterrupted()){
        checkDatasets();
        try{
          Thread.sleep(1000);
        }catch(Exception e){
          FHLog.e(LOG_TAG, "MonitorTask thread is interrupted", e);
          this.interrupt();
        }
        
      }
    }
    
  }
}
