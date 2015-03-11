package com.feedhenry.fhandroidexampleapp;

import java.util.Iterator;

import org.json.JSONObject;

import com.feedhenry.sdk.FHActCallback;
import com.feedhenry.sdk.FHResponse;
import com.feedhenry.sdk.sync.FHSyncClient;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Adapter;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class SyncCollisionsListActivity extends ListActivity {

  private ProgressDialog mDialog = null;
  private FHSyncClient mSyncClient = FHSyncClient.getInstance();
  private ArrayAdapter adapter = null;
  private SyncCollisionRecord selectedCollison = null;
  
  private static final String TAG = "SyncCollisionsListActivity";
  
  public void onCreate(Bundle savedInstance){
    super.onCreate(savedInstance);
    adapter = new ArrayAdapter<SyncCollisionRecord>(this, R.layout.activity_collision_list);
    getListView().setAdapter(adapter);
    loadCollisions();
  }
  
  private void loadCollisions(){
    final Context that = this;
    mDialog = ProgressDialog.show(this, "Loading", "Please wait...");
    try {
      mSyncClient.listCollisions(FHSyncActivity.DATAID, new FHActCallback() {
        
        @Override
        public void success(FHResponse pResponse) {
          adapter.clear();
          mDialog.dismiss();
          JSONObject collisionList = pResponse.getJson();
          if(collisionList.length() == 0){
            runOnUiThread(new Runnable() {
              @Override
              public void run() {
                FhUtil.showMessage(that, "Message", "No Collisions Found!");
              }
            });
          } else {
            Iterator<String> it = collisionList.keys();
            while(it.hasNext()){
              String key = it.next();
              SyncCollisionRecord record = new SyncCollisionRecord(collisionList.getJSONObject(key));
              adapter.add(record);
            }
          }
        }
        
        @Override
        public void fail(FHResponse pResponse) {
          mDialog.dismiss();
          Log.e(TAG, "listCollision request failed: " +  pResponse.getRawResponse().toString(), pResponse.getError());
          runOnUiThread(new Runnable() {
            @Override
            public void run() {
              FhUtil.showMessage(that, "Error", "Failed to list collisions!");
            }
          });
        }
      });
    } catch (Exception e) {
      mDialog.dismiss();
      Log.e(TAG, e.getMessage(), e);
      FhUtil.showMessage(that, "Error", "Failed to list collisions!");
    }
  }
  
  @Override
  protected void onListItemClick(ListView l, View v, int position, long id) {
    selectedCollison = (SyncCollisionRecord) adapter.getItem(position);
    Bundle b = new Bundle();
    b.putString("uid", selectedCollison.getUid());
    b.putString("hash", selectedCollison.getHash());
    b.putString("pre", selectedCollison.getPreData().getString("name"));
    b.putString("post", selectedCollison.getPostData().getString("name"));
    b.putString("current", selectedCollison.getCurrentData().getString("name"));
    Intent i = new Intent(this, SyncCollisionResolveActivity.class);
    i.putExtra("collision", b);
    startActivityForResult(i, 1);
  }
  
  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    if(resultCode == RESULT_OK){
      if(requestCode == 1){
        String action = data.getStringExtra("com.feedhenry.collision.action");
        if("delete".equalsIgnoreCase(action)){
          final String hash = data.getStringExtra("com.feedhenry.collision.hash");
          deleteCollision(hash);
        } else if("resolved".equalsIgnoreCase(action)){
          final String hash = data.getStringExtra("com.feedhenry.collision.hash");
          String uid = data.getStringExtra("com.feedhenry.collision.uid");
          String resolvedData = data.getStringExtra("com.feedhenry.collision.value");
          JSONObject currentData = selectedCollison.getCurrentData();
          currentData.put("name", resolvedData);
          try {
            mSyncClient.update(FHSyncActivity.DATAID, uid, currentData);
            deleteCollision(hash);
          } catch (Exception e) {
            // TODO Auto-generated catch block
            Log.e(TAG, "failed to update data", e);
          }
          
        }
      }
    }
  }
  
  private void deleteCollision(final String pHash){
    try {
      mSyncClient.removeCollision(FHSyncActivity.DATAID, pHash, new FHActCallback() {
        
        @Override
        public void success(FHResponse pResponse) {
          Log.d(TAG, "collision deleted: " + pHash);
          loadCollisions();
        }
        
        @Override
        public void fail(FHResponse pResponse) {
          // TODO Auto-generated method stub
          Log.e(TAG, "failed to delete collision: " + pResponse.getRawResponse(), pResponse.getError());
        }
      });
    } catch (Exception e) {
      Log.e(TAG, "failed to delete collision: " + pHash, e);
    }
  }
  
  private class SyncCollisionRecord {
    private String hashValue = null;
    private String uid = null;
    private JSONObject postData;
    private JSONObject preData;
    private JSONObject currentData;
    
    public SyncCollisionRecord(JSONObject pCollision){
      uid = pCollision.getString("uid");
      hashValue = pCollision.getString("hash");
      preData = pCollision.getJSONObject("pre");
      postData = pCollision.getJSONObject("post");
      
      FHSyncClient syncCli = FHSyncClient.getInstance();
      JSONObject cd = syncCli.read(FHSyncActivity.DATAID, uid);
      currentData = cd.getJSONObject("data");
    }
    
    public JSONObject getPreData(){
      return preData;
    }
    
    public JSONObject getPostData(){
      return postData;
    }
    
    public String getHash(){
      return hashValue;
    }
    
    public JSONObject getCurrentData(){
      return currentData;
    }
    
    public String getUid(){
      return uid;
    }
    
    public String toString(){
      return preData.getString("name") + " -> " + currentData.getString("name");
    }
  }
}
