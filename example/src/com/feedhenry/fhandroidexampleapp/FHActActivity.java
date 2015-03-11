package com.feedhenry.fhandroidexampleapp;

import org.json.JSONArray;
import org.json.JSONObject;

import com.feedhenry.sdk.FH;
import com.feedhenry.sdk.FHActCallback;
import com.feedhenry.sdk.FHResponse;
import com.feedhenry.sdk.api.FHActRequest;
import com.feedhenry.sdk.api.FHCloudRequest;

import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;


public class FHActActivity extends ListActivity {

  private static final String TAG = "FHAndroidSDKExample";
  private ProgressDialog mDialog = null;
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setTitle("Tweets About FeedHenry");
    final Context that = this;
    mDialog = ProgressDialog.show(this, "Loading", "Please wait...");
    FH.setLogLevel(FH.LOG_LEVEL_VERBOSE);
    FH.init(this, new FHActCallback() {
      
      @Override
      public void success(FHResponse arg0) {
        loadTweets();
      }
      
      @Override
      public void fail(FHResponse arg0) {
        mDialog.dismiss();
        FhUtil.showMessage(that, "Error", arg0.getErrorMessage());
      }
    });
    
  }
    
  private void loadTweets(){
    final Context that = this;
    final ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,R.layout.activity_act);
    getListView().setAdapter(adapter);
    try{
      //build the request object. The first parameter is the name of the cloud side function to be called,
      //the second parameter is the data parameter for the function
      FHCloudRequest request = FH.buildCloudRequest("getTweets", "GET", null, null);
      //the request will be executed asynchronously
      request.executeAsync(new FHActCallback() {
        @Override
        public void success(FHResponse res) {
          //the function to execute if the request is successful
          try{
            JSONArray resObj = res.getJson().getJSONArray("tweets");
            Log.d(TAG, resObj.toString(2));
            for(int i=0;i<resObj.length();i++){
              JSONObject event = resObj.getJSONObject(i);
              adapter.add(event.getString("text"));
            }
            mDialog.dismiss();
          } catch(Exception e){
            Log.e(TAG, e.getMessage(), e);
          }
        }
        
        @Override
        public void fail(FHResponse res) {
          //the function to execute if the request is failed
          Log.e(TAG, res.getErrorMessage(), res.getError());
          mDialog.dismiss();
          FhUtil.showMessage(that, "Error", res.getErrorMessage());
        }
      });
    } catch(Exception e){
      Log.e(TAG, e.getMessage(), e);
    }
  }
}
