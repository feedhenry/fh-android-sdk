package com.feedhenry.fhandroidexampleapp;

import org.json.fh.JSONObject;

import com.feedhenry.sdk.FH;
import com.feedhenry.sdk.FHActCallback;
import com.feedhenry.sdk.FHResponse;
import com.feedhenry.sdk.api.FHAuthRequest;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class FHLoginActivity extends Activity {

  public void onCreate(Bundle savedInstanceState){
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_fh_auth);
    Button loginBtn = (Button) findViewById(R.id.fh_login_btn);
    loginBtn.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        loginWithFh();
      }
    });
  }
  
  private void loginWithFh(){
    final FHLoginActivity that = this;
    EditText userField = (EditText) findViewById(R.id.fh_login_user);
    EditText passField = (EditText) findViewById(R.id.fh_login_password);
    String userName = userField.getText().toString();
    String password = passField.getText().toString();
    if("".equals(userName)){
      FhUtil.showMessage(this, "Error", "User name is empty");
      return;
    }
    if("".equals(password)){
      FhUtil.showMessage(this, "Error", "Password is empty");
      return;
    }
    try{
      FHAuthRequest authRequest = FH.buildAuthRequest("MyFeedHenryPolicy", userName, password);
      authRequest.executeAsync(new FHActCallback() {
        
        @Override
        public void success(FHResponse resp) {
          Log.d("FHLoginActivity", "Login success");
          JSONObject result = resp.getJson();
          Bundle b = new Bundle();
          b.putString("result", result.toString());
          that.finishLogin(b);
        }
        
        @Override
        public void fail(FHResponse resp) {
          Log.d("FHLoginActivity", "Login fail");
          String error = resp.getErrorMessage();
          Bundle b = new Bundle();
          b.putString("error", error);
          that.finishLogin(b);
        }
      });
    }catch(Exception e){
      e.printStackTrace();
    }
    
  }
  
  private void finishLogin(Bundle pData){
    Intent t = new Intent();
    t.putExtras(pData);
    setResult(Activity.RESULT_OK, t);
    this.finish();
  }

  @Override
  public boolean onKeyDown(int keyCode, KeyEvent event) {
    if(keyCode == KeyEvent.KEYCODE_BACK){
      setResult(Activity.RESULT_CANCELED);
    }
    return super.onKeyDown(keyCode, event);
  }
  
  
}
