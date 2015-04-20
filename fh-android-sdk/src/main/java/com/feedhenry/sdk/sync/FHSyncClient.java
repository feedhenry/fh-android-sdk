/**
 * Copyright (c) 2014 FeedHenry Ltd, All Rights Reserved.
 *
 * Please refer to your contract with FeedHenry for the software license agreement.
 * If you do not have a contract, you do not have a license to use this software.
 */
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

/**
 * The sync client is part of the FeedHenry data sync framework. It provides a mechanism to manage bi-direction data synchronization.
 * For more details, please check <a href="http://docs.feedhenry.com/v2/development_sync_service.html">data sync framewrok docs</a>.
 */
public class FHSyncClient {

    private static FHSyncClient mInstance;

    protected static final String LOG_TAG = "com.feedhenry.sdk.sync.FHSyncClient";

    private Context mContext;
    private Map<String, FHSyncDataset> mDataSets = new HashMap<String, FHSyncDataset>();
    private FHSyncConfig mConfig = new FHSyncConfig();
    private FHSyncListener mSyncListener = null;

    private FHSyncNotificationHandler mNotificationHandler;

    private boolean mInitialised = false;
    private MonitorTask mMonitorTask = null;

    private ExecutorService mExecutors = Executors.newFixedThreadPool(3);

    /**
     * Get the singleton instance of the sync client.
     * 
     * @return the sync client instance
     */
    public static FHSyncClient getInstance() {
        if (null == mInstance) {
            mInstance = new FHSyncClient();
        }
        return mInstance;
    }

    /**
     * Initialize the sync client. Should be called every time an app/activity starts.
     * 
     * @param pContext The app context
     * @param pConfig The sync configuration
     * @param pListener The sync listener
     */

    public void init(Context pContext, FHSyncConfig pConfig, FHSyncListener pListener) {
        mContext = pContext;
        mConfig = pConfig;
        mSyncListener = pListener;
        initHanlders();
        mInitialised = true;
        if (null == mMonitorTask) {
            mMonitorTask = new MonitorTask();
            mMonitorTask.start();
        }
    }

    /**
     * Initialize the notification handlers
     */
    private void initHanlders() {
        if (null != Looper.myLooper()) {
            mNotificationHandler = new FHSyncNotificationHandler(this.mSyncListener);
        } else {
            HandlerThread ht = new HandlerThread("FHSyncClientNotificationHanlder");
            mNotificationHandler = new FHSyncNotificationHandler(ht.getLooper(), this.mSyncListener);
            ht.start();
        }
    }

    /**
     * Re-set the sync listener
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
     * Use the sync client to manage a dataset.
     * 
     * @param pDataId The id of the dataset.
     * @param pConfig The sync configuration for the dataset. If not specified, the sync configuration passed in the init method will be used
     * @param pQueryParams Query parameters for the dataset
     * @throws Exception thrown if FHSyncClient isn't initialised.
     */
    public void manage(String pDataId, FHSyncConfig pConfig, JSONObject pQueryParams) throws Exception {
        if (!mInitialised) {
            throw new Exception("FHSyncClient isn't initialised. Have you called the init function?");
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
            dataset = new FHSyncDataset(mContext, mNotificationHandler, pDataId, syncConfig, pQueryParams);
            mDataSets.put(pDataId, dataset);
            dataset.setSyncRunning(false);
            dataset.setInitialised(true);
        }

        dataset.setSyncConfig(syncConfig);
        dataset.setSyncPending(true);

        dataset.writeToFile();
    }

    /**
     * List all the data in the dataset with pDataId.
     * 
     * @param pDataId The id of the dataset
     * @return all data records. Each record contains a key "uid" with the id value and a key "data" with the JSON data.
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
     * Read a data record with pUID in dataset with pDataId
     * 
     * @param pDataId the id of the dataset
     * @param pUID the id of the data record
     * @return the data record. Each record contains a key "uid" with the id value and a key "data" with the JSON data.
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
     * Create a new data record in dataset with pDataId
     * 
     * @param pDataId the id of the dataset
     * @param pData the actual data
     * @return the created data record. Each record contains a key "uid" with the id value and a key "data" with the JSON data.
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
     * Update an existing data record in dataset with pDataId
     * 
     * @param pDataId the id of the dataset
     * @param pUID the id of the data record
     * @param pData the new content of the data record
     * @return the updated data record. Each record contains a key "uid" with the id value and a key "data" with the JSON data.
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
     * Delete a data record in the dataset with pDataId
     * 
     * @param pDataId the id of the dataset
     * @param pUID the id of the data record
     * @return the deleted data record. Each record contains a key "uid" with the id value and a key "data" with the JSON data.
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
     * List sync collisions in dataset with id pDataId
     * 
     * @param pDataId the id of the dataset
     * @param pCallback the callback function
     * @throws Exception thrown if building the list request or executing the list request fails
     */
    public void listCollisions(String pDataId, FHActCallback pCallback) throws Exception {
        JSONObject params = new JSONObject();
        params.put("fn", "listCollisions");
        FHActRequest request = FH.buildActRequest(pDataId, params);
        request.executeAsync(pCallback);
    }

    /**
     * Remove a sync collision record in the dataset with id pDataId
     * 
     * @param pDataId the id of the dataset
     * @param pCollisionHash the hash value of the collision record
     * @param pCallback the callback function
     * @throws Exception thrown if building the remove request or executing the remove request fails
     */
    public void removeCollision(String pDataId, String pCollisionHash, FHActCallback pCallback) throws Exception {
        JSONObject params = new JSONObject();
        params.put("fn", "removeCollision");
        params.put("hash", pCollisionHash);
        FHActRequest request = FH.buildActRequest(pDataId, params);
        request.executeAsync(pCallback);
    }

    /**
     * Stop the sync process for dataset with id pDataId
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
     * Stop all sync processes for all the datasets managed by the sync client.
     */
    public void destroy() {
        if (mInitialised) {
            if (null != mMonitorTask) {
                mMonitorTask.stopRunning();
                mMonitorTask.stop();
            }
            for (String key : mDataSets.keySet()) {
                stop(key);
            }
            mSyncListener = null;
            mNotificationHandler = null;
            mDataSets = null;
            mInitialised = false;
        }
    }

    private class MonitorTask extends Thread {

        private boolean mKeepRunning = true;

        public void stopRunning() {
            mKeepRunning = false;
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
            while (mKeepRunning && !isInterrupted()) {
                checkDatasets();
                try {
                    Thread.sleep(1000);
                } catch (Exception e) {
                    FHLog.e(LOG_TAG, "MonitorTask thread is interrupted", e);
                    this.interrupt();
                }

            }
        }

    }
}
