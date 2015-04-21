package com.feedhenry.fhandroidexampleapp;


import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;

public class FHAndroidExampleActivity extends TabActivity {

  public void onCreate(Bundle savedInstanceState){
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_tab);
    TabHost tabHost = getTabHost();
    
    TabSpec actSpec = tabHost.newTabSpec("FHCloud");
    actSpec.setIndicator("FHCloud");
    Intent actIntent = new Intent(this, FHActActivity.class);
    actSpec.setContent(actIntent);
    
    TabSpec authSpec = tabHost.newTabSpec("FHAuth");
    authSpec.setIndicator("FHAuth");
    Intent authIntent = new Intent(this, FHAuthActivity.class);
    authSpec.setContent(authIntent);
    
    TabSpec syncSpec = tabHost.newTabSpec("FHSync");
    syncSpec.setIndicator("FHSync");
    Intent syncIntent = new Intent(this, FHSyncActivity.class);
    syncSpec.setContent(syncIntent);
    
    tabHost.addTab(actSpec);
    tabHost.addTab(authSpec);
    tabHost.addTab(syncSpec);
  }
}
