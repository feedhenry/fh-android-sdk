/**
 * Copyright (c) 2015 FeedHenry Ltd, All Rights Reserved.
 *
 * Please refer to your contract with FeedHenry for the software license
 * agreement. If you do not have a contract, you do not have a license to use
 * this software.
 */
package com.feedhenry.sdk.sync;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.util.Log;
import com.feedhenry.sdk.FH;
import com.feedhenry.sdk.FHActCallback;
import com.feedhenry.sdk.api.FHActRequest;
import com.feedhenry.sdk.utils.FHLog;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.json.fh.JSONObject;

/**
 * The sync client is part of the FeedHenry data sync framework. It provides a
 * mechanism to manage bi-direction data synchronization. For more details,
 * please check
 * <a href="http://docs.feedhenry.com/v3/guides/sync_service.html">data sync
 * framework docs</a>.
 */
public class FHSyncClient {

    private static FHSyncClient mInstance;

    protected static final String LOG_TAG = "com.feedhenry.sdk.sync.FHSyncClient";

    private final Handler mHandler;

    private Context mContext;
    private Map<String, FHSyncDataset> mDataSets = new HashMap<String, FHSyncDataset>();
    private FHSyncConfig mConfig = new FHSyncConfig();
    private FHSyncListener mSyncListener = null;

    private FHSyncNotificationHandler mNotificationHandler;

    private boolean mInitialised = false;
    private MonitorTask mMonitorTask = null;

    /**
     * Gets the singleton instance of the sync client.
     *
     * @return the sync client instance
     */
    public static FHSyncClient getInstance() {
        if (null == mInstance) {
            mInstance = new FHSyncClient();
        }
        return mInstance;
    }

    public FHSyncClient() {
        HandlerThread thread = new HandlerThread("FHSyncClient");
        thread.start();
        mHandler = new Handler(thread.getLooper());
    }

    /**
     * Initializes the sync client. Should be called every time an app/activity
     * starts.
     *
     * @param pContext The app context
     * @param pConfig The sync configuration
     * @param pListener The sync listener
     */
    public void init(Context pContext, FHSyncConfig pConfig, FHSyncListener pListener) {
        mContext = pContext.getApplicationContext();
        mConfig = pConfig;
        mSyncListener = pListener;
        initHandlers();
        mInitialised = true;
        if (null == mMonitorTask) {
            HandlerThread thread = new HandlerThread("monitor task");
            thread.start();
            Handler handler = new Handler(thread.getLooper());
            mMonitorTask = new MonitorTask();
            handler.post(mMonitorTask);
        }
    }

    /**
     * Initializes the notification handlers.
     */
    private void initHandlers() {
        if (null != Looper.myLooper()) {
            mNotificationHandler = new FHSyncNotificationHandler(this.mSyncListener);
        } else {
            HandlerThread ht = new HandlerThread("FHSyncClientNotificationHanlder");
            mNotificationHandler = new FHSyncNotificationHandler(ht.getLooper(), this.mSyncListener);
            ht.start();
        }
    }

    /**
     * Re-sets the sync listener.
     *
     * @param pListener the new sync listener
     */
    public void setListener(FHSyncListener pListener) {
        mSyncListener = pListener;
        if (null != mNotificationHandler) {
            mNotificationHandler.setSyncListener(mSyncListener);
        }
    }

    /**
     * Uses the sync client to manage a dataset.
     *
     * @param pDataId The id of the dataset.
     * @param pConfig The sync configuration for the dataset. If not specified,
     * the sync configuration passed in the initDev method will be used
     * @param pQueryParams Query parameters for the dataset
     * @throws Exception thrown if FHSyncClient isn't initialised.
     */
    public void manage(String pDataId, FHSyncConfig pConfig, JSONObject pQueryParams) throws Exception {
        manage(pDataId, pConfig, pQueryParams, new JSONObject());
    }

    /**
     * Uses the sync client to manage a dataset.
     *
     * @param pDataId The id of the dataset.
     * @param pConfig The sync configuration for the dataset. If not specified,
     * the sync configuration passed in the initDev method will be used
     * @param pQueryParams Query parameters for the dataset
     * @param pMetaData Meta for the dataset
     * @throws Exception thrown if FHSyncClient isn't initialised.
     */
    public void manage(String pDataId, FHSyncConfig pConfig, JSONObject pQueryParams, JSONObject pMetaData)
            throws Exception {
        if (!mInitialised) {
            throw new Exception("FHSyncClient isn't initialised. Have you called the initDev function?");
        }
        FHSyncDataset dataset = mDataSets.get(pDataId);
        FHSyncConfig syncConfig = mConfig;
        if (null != pConfig) {
            syncConfig = pConfig;
        }
        if (null != dataset) {
            dataset.setContext(mContext);
            dataset.setNotificationHandler(mNotificationHandler);
        } else {
            dataset
                    = new FHSyncDataset(mContext, mNotificationHandler, pDataId, syncConfig, pQueryParams, pMetaData);
            mDataSets.put(pDataId, dataset);
            dataset.setSyncRunning(false);
            dataset.setInitialised(true);
        }

        dataset.setSyncConfig(syncConfig);
        dataset.setSyncPending(true);

        dataset.writeToFile();
    }

    /**
     * Causes the sync framework to schedule for immediate execution a sync.
     *
     * @param pDataId The id of the dataset
     */
    public void forceSync(String pDataId) {
        FHSyncDataset dataset = mDataSets.get(pDataId);

        if (null != dataset) {
            dataset.setSyncPending(true);
        }
    }

    /**
     * Lists all the data in the dataset with pDataId.
     *
     * @param pDataId The id of the dataset
     * @return all data records. Each record contains a key "uid" with the id
     * value and a key "data" with the JSON data.
     */
    public JSONObject list(String pDataId) {
        FHSyncDataset dataset = mDataSets.get(pDataId);
        JSONObject data = null;
        if (null != dataset) {
            data = dataset.listData();
        }
        return data;
    }

    /**
     * Reads a data record with pUID in dataset with pDataId.
     *
     * @param pDataId the id of the dataset
     * @param pUID the id of the data record
     * @return the data record. Each record contains a key "uid" with the id
     * value and a key "data" with the JSON data.
     */
    public JSONObject read(String pDataId, String pUID) {
        FHSyncDataset dataset = mDataSets.get(pDataId);
        JSONObject data = null;
        if (null != dataset) {
            data = dataset.readData(pUID);
        }
        return data;
    }

    /**
     * Creates a new data record in dataset with pDataId.
     *
     * @param pDataId the id of the dataset
     * @param pData the actual data
     * @return the created data record. Each record contains a key "uid" with
     * the id value and a key "data" with the JSON data.
     * @throws Exception if the dataId is not known
     */
    public JSONObject create(String pDataId, JSONObject pData) throws Exception {
        FHSyncDataset dataset = mDataSets.get(pDataId);
        if (null != dataset) {
            return dataset.createData(pData);
        } else {
            throw new Exception("Unknown dataId : " + pDataId);
        }
    }

    /**
     * Updates an existing data record in dataset with pDataId.
     *
     * @param pDataId the id of the dataset
     * @param pUID the id of the data record
     * @param pData the new content of the data record
     * @return the updated data record. Each record contains a key "uid" with
     * the id value and a key "data" with the JSON data.
     * @throws Exception if the dataId is not known
     */
    public JSONObject update(String pDataId, String pUID, JSONObject pData) throws Exception {
        FHSyncDataset dataset = mDataSets.get(pDataId);
        if (null != dataset) {
            return dataset.updateData(pUID, pData);
        } else {
            throw new Exception("Unknown dataId : " + pDataId);
        }
    }

    /**
     * Deletes a data record in the dataset with pDataId.
     *
     * @param pDataId the id of the dataset
     * @param pUID the id of the data record
     * @return the deleted data record. Each record contains a key "uid" with
     * the id value and a key "data" with the JSON data.
     * @throws Exception if the dataId is not known
     */
    public JSONObject delete(String pDataId, String pUID) throws Exception {
        FHSyncDataset dataset = mDataSets.get(pDataId);
        if (null != dataset) {
            return dataset.deleteData(pUID);
        } else {
            throw new Exception("Unknown dataId : " + pDataId);
        }
    }

    /**
     * Lists sync collisions in dataset with id pDataId.
     *
     * @param pDataId the id of the dataset
     * @param pCallback the callback function
     * @throws Exception thrown if building the list request or executing the
     * list request fails
     */
    public void listCollisions(String pDataId, FHActCallback pCallback) throws Exception {
        JSONObject params = new JSONObject();
        params.put("fn", "listCollisions");
        FHActRequest request = FH.buildActRequest(pDataId, params);
        request.executeAsync(pCallback);
    }

    /**
     * Removes a sync collision record in the dataset with id pDataId.
     *
     * @param pDataId the id of the dataset
     * @param pCollisionHash the hash value of the collision record
     * @param pCallback the callback function
     * @throws Exception thrown if building the remove request or executing the
     * remove request fails
     */
    public void removeCollision(String pDataId, String pCollisionHash, FHActCallback pCallback)
            throws Exception {
        JSONObject params = new JSONObject();
        params.put("fn", "removeCollision");
        params.put("hash", pCollisionHash);
        FHActRequest request = FH.buildActRequest(pDataId, params);
        request.executeAsync(pCallback);
    }

    /**
     * This method will begin synchronization. It should be called in the
     * {@link Activity#onResume()} block.
     *
     * @param listener the listener to.  If null the current listener will be 
     * used.
     * 
     */
    public void resumeSync(FHSyncListener listener) {
        
        if (listener != null) {
            this.mSyncListener = listener;
        }
        
        for (FHSyncDataset dataSet : mDataSets.values()) {
                dataSet.stopSync(false);
        }
        
    }
    

    /**
     * This method will pause synchronization. It should be called in the
     * {@link Activity#onPause()} block.
     */
    public void pauseSync() {
        
        for (FHSyncDataset dataSet : mDataSets.values()) {
                dataSet.stopSync(true);
        }
        
        this.mSyncListener = null;
    }
    
    /**
     * Stops the sync process for dataset with id pDataId.
     *
     * @param pDataId the id of the dataset
     */
    public void stop(String pDataId) {
        FHSyncDataset dataset = mDataSets.get(pDataId);
        if (null != dataset) {
            dataset.stopSync(true);
        }
    }

    /**
     * Stops all sync processes for all the datasets managed by the sync client.
     */
    public void destroy() {
        if (mInitialised) {
            if (null != mMonitorTask) {
                mMonitorTask.stopRunning();
            }
            for (String key : mDataSets.keySet()) {
                stop(key);
            }
            mSyncListener = null;
            mNotificationHandler = null;
            mDataSets = new HashMap<String, FHSyncDataset>();
            mInitialised = false;
        }
    }

    private class MonitorTask implements Runnable {

        private boolean mKeepRunning = true;

        public void stopRunning() {
            mKeepRunning = false;
            Thread.currentThread().interrupt();
        }

        private void checkDatasets() {
            if (null != mDataSets) {
                for (Map.Entry<String, FHSyncDataset> entry : mDataSets.entrySet()) {
                    final FHSyncDataset dataset = entry.getValue();
                    boolean syncRunning = dataset.isSyncRunning();
                    if (!syncRunning && !dataset.isStopSync()) {
                        // sync isn't running for dataId at the moment, check if needs to start it
                        Date lastSyncStart = dataset.getSyncStart();
                        Date lastSyncEnd = dataset.getSyncEnd();
                        if (null == lastSyncStart) {
                            dataset.setSyncPending(true);
                        } else if (null != lastSyncEnd) {
                            long interval = new Date().getTime() - lastSyncEnd.getTime();
                            if (interval > dataset.getSyncConfig().getSyncFrequency() * 1000) {
                                Log.d(LOG_TAG, "Should start sync!!");
                                dataset.setSyncPending(true);
                            }
                        }

                        if (dataset.isSyncPending()) {
                            mHandler.post(
                                    new Runnable() {
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
            while (mKeepRunning && !Thread.currentThread().isInterrupted()) {
                checkDatasets();
                try {
                    Thread.sleep(1000);
                } catch (Exception e) {
                    FHLog.e(LOG_TAG, "MonitorTask thread is interrupted", e);
                    Thread.currentThread().interrupt();
                }
            }
        }
    }
}
