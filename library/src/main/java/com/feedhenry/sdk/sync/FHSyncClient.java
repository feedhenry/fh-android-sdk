/**
 * Copyright Red Hat, Inc, and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.feedhenry.sdk.sync;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.util.Log;
import com.feedhenry.sdk.FH;
import com.feedhenry.sdk.FHActCallback;
import com.feedhenry.sdk.api.FHActRequest;
import com.feedhenry.sdk.exceptions.DataSetNotFound;
import com.feedhenry.sdk.exceptions.FHNotReadyException;
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
public class FHSyncClient implements Application.ActivityLifecycleCallbacks {

    private static FHSyncClient mInstance;

    protected static final String LOG_TAG = "FHSyncClient";

    private final Handler mHandler;

    private Context mContext;
    private Map<String, FHSyncDataset> mDataSets = new HashMap<String, FHSyncDataset>();
    private FHSyncConfig mConfig = new FHSyncConfig();
    private FHSyncListener mSyncListener = null;

    private FHSyncNotificationHandler mNotificationHandler;

    private boolean mInitialised = false;
    private MonitorTask mMonitorTask = null;

    /**
     * FHSyncClient will perform some sniffing of its environment and, if it thinks it is being
     * reference from an activity it will do sanity checks to keep in sync with the Activity state
     * and log an warning otherwise.
     */
    private boolean checkActivity = false;

    /**
     * FHSyncClient the activity class to monitor for events of checkActivity.
     */

    private Class<? extends Activity> checkActivityClass;

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

        if ((contextIsActivity(pContext) || listenerIsInnerClass(pListener)) && !pConfig.isSupressActivityWarnings()) {
            checkActivityClass = fetchActivity(pContext, pListener);
            checkActivity = true;
            if (mContext instanceof Application) {
                ((Application)mContext).registerActivityLifecycleCallbacks(this);
            }
        }

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
     *
     * If we are monitoring activity events to prevent pListener from leaking then we need to know
     * what class to monitor.  This method will inspect FHSyncListener for an enclosing activity and
     * then pContext for an activity.
     *
     * @param pContext The context to listen for Activity events from.
     * @param pListener The Listener to listen to Activity events from.
     * @return pListeners enclosing class, or pContext's class, or Activity.class
     */
    private Class<? extends Activity> fetchActivity(Context pContext, FHSyncListener pListener) {

        Class<?> testClass = pListener.getClass().getEnclosingClass();
        if (testClass != null && Activity.class.isAssignableFrom(pListener.getClass().getDeclaredConstructors()[0].getParameterTypes()[0])) {
            return (Class<? extends Activity>) pListener.getClass().getDeclaredConstructors()[0].getParameterTypes()[0];
        }
        else if (pContext instanceof Activity) {
            return (Class<? extends Activity>) pContext.getClass();
        } else {
            return Activity.class;
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
            ht.start();
            mNotificationHandler = new FHSyncNotificationHandler(ht.getLooper(), this.mSyncListener);
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
     * @throws IllegalStateException thrown if FHSyncClient isn't initialised.
     */
    public void manage(String pDataId, FHSyncConfig pConfig, JSONObject pQueryParams) {
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
     * @throws IllegalStateException thrown if FHSyncClient isn't initialised.
     */
    public void manage(String pDataId, FHSyncConfig pConfig, JSONObject pQueryParams, JSONObject pMetaData) {
        if (!mInitialised) {
            throw new IllegalStateException("FHSyncClient isn't initialised. Have you called the initDev function?");
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
     * @throws DataSetNotFound if the dataId is not known
     */
    public JSONObject create(String pDataId, JSONObject pData) throws DataSetNotFound {
        FHSyncDataset dataset = mDataSets.get(pDataId);
        if (null != dataset) {
            return dataset.createData(pData);
        } else {
            throw new DataSetNotFound("Unknown dataId : " + pDataId);
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
     * @throws DataSetNotFound if the dataId is not known
     */
    public JSONObject update(String pDataId, String pUID, JSONObject pData) throws DataSetNotFound {
        FHSyncDataset dataset = mDataSets.get(pDataId);
        if (null != dataset) {
            return dataset.updateData(pUID, pData);
        } else {
            throw new DataSetNotFound("Unknown dataId : " + pDataId);
        }
    }

    /**
     * Deletes a data record in the dataset with pDataId.
     *
     * @param pDataId the id of the dataset
     * @param pUID the id of the data record
     * @return the deleted data record. Each record contains a key "uid" with
     * the id value and a key "data" with the JSON data.
     * @throws DataSetNotFound if the dataId is not known
     */
    public JSONObject delete(String pDataId, String pUID) throws DataSetNotFound {
        FHSyncDataset dataset = mDataSets.get(pDataId);
        if (null != dataset) {
            return dataset.deleteData(pUID);
        } else {
            throw new DataSetNotFound("Unknown dataId : " + pDataId);
        }
    }

    /**
     * Lists sync collisions in dataset with id pDataId.
     *
     * @param pDataId the id of the dataset
     * @param pCallback the callback function
     * @throws FHNotReadyException if FH is not initialized.
     * 
     */
    public void listCollisions(String pDataId, FHActCallback pCallback) throws FHNotReadyException {
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
     * @throws FHNotReadyException thrown if FH is not initialized.
     */
    public void removeCollision(String pDataId, String pCollisionHash, FHActCallback pCallback) throws FHNotReadyException {
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

    @Override
    public void onActivityCreated(Activity activity, Bundle bundle) {

    }

    @Override
    public void onActivityStarted(Activity activity) {

    }

    @Override
    public void onActivityResumed(Activity activity) {

    }

    @Override
    public void onActivityPaused(Activity activity) {
    }

    @Override
    public void onActivityStopped(Activity activity) {
        if (checkActivity && this.mSyncListener != null && activity.getClass().equals(checkActivityClass)) {
            Log.w(LOG_TAG, "Activity " + activity.getLocalClassName() + " was stopped however there is still an active sync listener.  Please call FHSyncListener.pauseSync in the Activity.onPause method.");
        }
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle bundle) {

    }

    @Override
    public void onActivityDestroyed(Activity activity) {

    }

    private boolean listenerIsInnerClass(FHSyncListener pListener) {
        Class<?> testClass = pListener.getClass().getEnclosingClass();
        return testClass != null && Activity.class.isAssignableFrom(pListener.getClass().getDeclaredConstructors()[0].getParameterTypes()[0]);
    }

    private boolean contextIsActivity(Context pContext) {
        return pContext instanceof Activity;

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
