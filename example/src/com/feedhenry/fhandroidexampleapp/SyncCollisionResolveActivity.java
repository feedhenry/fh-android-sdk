package com.feedhenry.fhandroidexampleapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class SyncCollisionResolveActivity extends Activity{
  
  String pre;
  String post;
  String current;
  String uid;
  String collisionHash;
  
  EditText preField;
  EditText postField;
  EditText currentField;
  
  public void onCreate(Bundle savedInstance){
    super.onCreate(savedInstance);
    setContentView(R.layout.activity_resolve_collision);
    Bundle data = getIntent().getBundleExtra("collision");
    pre = data.getString("pre");
    post = data.getString("post");
    current = data.getString("current");
    collisionHash = data.getString("hash");
    uid = data.getString("uid");
    
    preField = (EditText) findViewById(R.id.editText1);
    postField = (EditText) findViewById(R.id.editText2);
    currentField = (EditText) findViewById(R.id.editText3);
    
    preField.setText(pre);
    postField.setText(post);
    currentField.setText(current);
    
    Button usePreButton = (Button) findViewById(R.id.button1);
    Button usePostButton = (Button) findViewById(R.id.button2);
    Button useCurrentButton = (Button) findViewById(R.id.button3);
    
    usePreButton.setOnClickListener(new OnClickListener() {
      
      @Override
      public void onClick(View arg0) {
        currentField.setText(pre);
      }
    });
    
    usePostButton.setOnClickListener(new OnClickListener() {
      
      @Override
      public void onClick(View arg0) {
        currentField.setText(post);
      }
    });
    
    useCurrentButton.setOnClickListener(new OnClickListener() {
      
      @Override
      public void onClick(View arg0) {
        currentField.setText(current);
      }
    });
  }
  
  public boolean onCreateOptionsMenu(Menu menu){
    MenuItem si = menu.add("Resolved Using Current Data");
    MenuItem di = menu.add("Discard Collision");
    si.setOnMenuItemClickListener(new OnMenuItemClickListener() {
      
      @Override
      public boolean onMenuItemClick(MenuItem item) {
        Intent i = new Intent();
        i.putExtra("com.feedhenry.collision.hash", collisionHash);
        i.putExtra("com.feedhenry.collision.uid", uid);
        i.putExtra("com.feedhenry.collision.value", currentField.getText().toString());
        i.putExtra("com.feedhenry.collision.action", "resolved");
        setResult(RESULT_OK, i);
        finish();
        return true;
      }
    });
    
    di.setOnMenuItemClickListener(new OnMenuItemClickListener() {
      
      @Override
      public boolean onMenuItemClick(MenuItem item) {
        Intent i = new Intent();
        i.putExtra("com.feedhenry.collision.hash", collisionHash);
        i.putExtra("com.feedhenry.collision.uid", uid);
        i.putExtra("com.feedhenry.collision.action", "delete");
        setResult(RESULT_OK, i);
        finish();
        return true;
      }
    });
    return true;
  }
}
