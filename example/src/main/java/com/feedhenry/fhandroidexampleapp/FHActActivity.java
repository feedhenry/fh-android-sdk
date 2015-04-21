package com.feedhenry.fhandroidexampleapp;

import android.app.Activity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import org.json.fh.JSONArray;
import org.json.fh.JSONObject;

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


public class FHActActivity extends Activity {

  private static final String TAG = "FHAndroidSDKExample";
  private ProgressDialog mDialog = null;
  private Button callBtn = null;
  private TextView responseField = null;
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setTitle("Hello");
    setContentView(R.layout.activity_act);
    final Context that = this;
    callBtn = (Button) findViewById(R.id.callButton);
    responseField = (TextView) findViewById(R.id.responseText);

    callBtn.setEnabled(false);
    mDialog = ProgressDialog.show(this, "Loading", "Please wait...");
    FH.setLogLevel(FH.LOG_LEVEL_VERBOSE);
    FH.init(this, new FHActCallback() {

      @Override public void success(FHResponse arg0) {
        mDialog.dismiss();
        callBtn.setEnabled(true);
      }

      @Override public void fail(FHResponse arg0) {
        mDialog.dismiss();
        FhUtil.showMessage(that, "Error", arg0.getErrorMessage());
      }
    });

  }

  public void callRemote(View view){
    responseField.setText("Calling");
    loadRemote();
  }


  private void loadRemote(){
    final Context that = this;
    try{
      //build the request object. The first parameter is the name of the cloud side function to be called,
      //the second parameter is the data parameter for the function
      FHCloudRequest request = FH.buildCloudRequest("/hello", "GET", null, null);
      //the request will be executed asynchronously
      request.executeAsync(new FHActCallback() {
        @Override
        public void success(FHResponse res) {
          //the function to execute if the request is successful
          try{
            String text = res.getJson().getString("msg");
            Log.d(TAG, text);
            responseField.setText(text);
            mDialog.dismiss();
          } catch(Exception e){
            Log.e(TAG, e.getMessage(), e);
          }
        }
        
        @Override
        public void fail(FHResponse res) {
          //the function to execute if the request is failed
          Log.e(TAG, res.getErrorMessage(), res.getError());
          responseField.setText(res.getErrorMessage());
          FhUtil.showMessage(that, "Error", res.getErrorMessage());
        }
      });
    } catch(Exception e){
      Log.e(TAG, e.getMessage(), e);
    }
  }
}
