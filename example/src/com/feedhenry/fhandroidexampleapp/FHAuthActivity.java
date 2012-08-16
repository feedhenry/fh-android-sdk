package com.feedhenry.fhandroidexampleapp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;

import com.feedhenry.sdk.FH;
import com.feedhenry.sdk.FHActCallback;
import com.feedhenry.sdk.FHResponse;
import com.feedhenry.sdk.api.FHAuthRequest;

public class FHAuthActivity extends Activity {
  
  public static final int FH_LOGIN_REQUEST = 101;

  public void onCreate(Bundle savedInstanceState){
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_auth);
    ImageButton fhBtn = (ImageButton) findViewById(R.id.fhIcon);
    fhBtn.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        Log.d("FHAuth", "FeedHenry clicked");
        doFhAuth();
      }
    });
    ImageButton googleBtn = (ImageButton) findViewById(R.id.googleIcon);
    googleBtn.setOnClickListener(new OnClickListener() {
      
      @Override
      public void onClick(View v) {
        Log.d("FHAuth", "Google clicked");
        doGoogleAuth();
      }
    });
  }
  
  private void doFhAuth(){
    try{
      Intent i = new Intent(this, FHLoginActivity.class);
      startActivityForResult(i, FH_LOGIN_REQUEST);
    } catch (Exception e){
      Log.e("FHAuthActivity", e.getMessage(), e);
    }
  }
  
  private void doGoogleAuth(){
    try{
      final Context that = this;
      FHAuthRequest authRequest = FH.buildAuthRequest();
      authRequest.setPresentingActivity(this);
      authRequest.setAuthPolicyId("MyGooglePolicy");
      authRequest.executeAsync(new FHActCallback() {
        
        @Override
        public void success(FHResponse resp) {
          Log.d("FHAuthActivity", resp.getJson().toString());
          FhUtil.showMessage(that, "Success", resp.getJson().toString());
        }
        
        @Override
        public void fail(FHResponse resp) {
          // TODO Auto-generated method stub
          Log.d("FHAuthActivity", resp.getErrorMessage());
          FhUtil.showMessage(that, "Error", resp.getErrorMessage());
        }
      });
    } catch(Exception e){
      Log.e("FHAuthActivity", e.getMessage(), e);
    }
    
  }
  
  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if(resultCode == Activity.RESULT_OK){
      if(requestCode == FH_LOGIN_REQUEST){
        Bundle resData = data.getExtras();
        if(resData.containsKey("result")){
          String result = resData.getString("result");
          FhUtil.showMessage(this, "Success", result);
        } else {
          String error = resData.getString("error");
          FhUtil.showMessage(this, "Error", error);
        }
      }
    } else {
      FhUtil.showMessage(this, "Error", "Cancelled");
    }
    
  }

  
  
}
