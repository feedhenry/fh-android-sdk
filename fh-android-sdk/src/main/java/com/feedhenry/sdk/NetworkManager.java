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
package com.feedhenry.sdk;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import com.feedhenry.sdk.utils.FHLog;

public class NetworkManager {
    private Context mContext;
    private boolean mIsOnline;
    private boolean mIsListenerRegistered;
    private NetworkReceiver mReceiver;

    private static final String LOG_TAG = "com.feedhenry.sdk.NetworkManager";

    private static NetworkManager mInstance;

    public NetworkManager(Context pContext) {
        this.mContext = pContext;
    }

    public void registerNetworkListener() {
        if (!mIsListenerRegistered) {
            IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
            mReceiver = new NetworkReceiver();
            mContext.registerReceiver(mReceiver, filter);
            mIsListenerRegistered = true;
        }
    }

    public void unregisterNetworkListener() {
        if (mIsListenerRegistered) {
            try {
                mContext.unregisterReceiver(mReceiver);
            } catch (Exception e) {
                FHLog.w(LOG_TAG, "Failed to unregister receiver");
            }
            mIsListenerRegistered = false;
        }
    }

    public void checkNetworkStatus() {
        ConnectivityManager connMgr = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        mIsOnline = networkInfo != null && networkInfo.isConnected();
        if (mIsOnline) {
            String type = networkInfo.getTypeName();
            FHLog.i(LOG_TAG, "Device is online. Connection type : " + type);
        } else {
            FHLog.i(LOG_TAG, "Device is offline.");
        }
    }

    public boolean isOnline() {
        return mIsOnline;
    }

    private class NetworkReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            checkNetworkStatus();
        }
    }

    public static NetworkManager init(Context pContext) {
        if (mInstance == null) {
            mInstance = new NetworkManager(pContext);
        }
        return mInstance;
    }

    public static NetworkManager getInstance() {
        return mInstance;
    }
}
