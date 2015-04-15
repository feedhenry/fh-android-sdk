package com.feedhenry.sdk;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import com.feedhenry.sdk.utils.FHLog;

/**
 * Created by weili on 15/04/15.
 */
public class NetworkManager {
  private Context mContext;
  private boolean mIsOnline;
  private boolean mIsListenerRegistered;
  private NetworkReceiver mReceiver;

  private static final String LOG_TAG = "com.feedhenry.sdk.NetworkManager";

  private static NetworkManager mInstance;

  public NetworkManager(Context pContext){
    this.mContext = pContext;
  }

  public void registerNetworkListener(){
    if(!mIsListenerRegistered){
      IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
      mReceiver = new NetworkReceiver();
      mContext.registerReceiver(mReceiver, filter);
      mIsListenerRegistered = true;
    }
  }

  public void unregisterNetworkListener(){
    if(mIsListenerRegistered){
      try {
        mContext.unregisterReceiver(mReceiver);
      } catch (Exception e) {
        FHLog.w(LOG_TAG, "Failed to unregister receiver");
      }
      mIsListenerRegistered = false;
    }
  }

  public void checkNetworkStatus(){
    ConnectivityManager connMgr = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
    NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
    if (null != networkInfo && networkInfo.isConnected()) {
      String type = networkInfo.getTypeName();
      FHLog.i(LOG_TAG, "Device is online. Connection type : " + type);
      mIsOnline = true;
    } else {
      FHLog.i(LOG_TAG, "Device is offline.");
      mIsOnline = false;
    }
  }

  public boolean isOnline(){
    return mIsOnline;
  }

  private class NetworkReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
      checkNetworkStatus();
    }
  }

  public static NetworkManager init(Context pContext){
    if(null == mInstance){
      mInstance = new NetworkManager(pContext);
    }
    return mInstance;
  }

  public static NetworkManager getInstance(){
    return mInstance;
  }
}
