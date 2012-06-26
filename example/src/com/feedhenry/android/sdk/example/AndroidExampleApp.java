package com.feedhenry.android.sdk.example;

import org.json.JSONArray;
import org.json.JSONObject;

import com.feedhenry.sdk.FH;
import com.feedhenry.sdk.FHActCallback;
import com.feedhenry.sdk.FHResponse;
import com.feedhenry.sdk.api.FHActRequest;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;

public class AndroidExampleApp extends ListActivity {

  private static final String TAG = "FHAndroidSDKExample";
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    final ProgressDialog diglog = ProgressDialog.show(this, "Loading", "Please wait...");
    FH.initializeFH(this);
    final ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,R.layout.list_event);
    getListView().setAdapter(adapter);
    try{
      FHActRequest request = FH.buildActRequest("getEventsByLocation", new JSONObject().put("longi", "-7.12").put("lati", "52.25"));
      request.executeAsync(new FHActCallback() {
        
        @Override
        public void success(FHResponse res) {
          JSONArray resObj = res.getArray();
          try{
            Log.d(TAG, resObj.toString(2));
            for(int i=0;i<resObj.length();i++){
              JSONObject event = resObj.getJSONObject(i);
              adapter.add(event.getString("title"));
            }
            diglog.dismiss();
          } catch(Exception e){
            Log.e(TAG, e.getMessage(), e);
          }
        }
        
        @Override
        public void fail(FHResponse res) {
          Log.e(TAG, res.getErrorMessage(), res.getError());
        }
      });
    } catch(Exception e){
      Log.e(TAG, e.getMessage(), e);
    }
  }
}
