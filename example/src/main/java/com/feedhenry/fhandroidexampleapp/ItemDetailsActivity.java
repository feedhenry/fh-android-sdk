package com.feedhenry.fhandroidexampleapp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

public class ItemDetailsActivity extends Activity {
  
  TextView idLabel;
  EditText idField;
  TextView createdLabel;
  EditText createdField;
  EditText nameField;
  String action;
  String uid;
  String name;
  String created;
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_item_detail);
    Bundle item = getIntent().getBundleExtra("item");
    uid = item.getString("uid");
    name = item.getString("name");
    created = item.getString("created");
    idLabel = (TextView) findViewById(R.id.textView1);
    idField = (EditText) findViewById(R.id.idField);
    createdLabel = (TextView) findViewById(R.id.textView3);
    createdField = (EditText) findViewById(R.id.createdField);
    nameField = (EditText) findViewById(R.id.nameField);
    if(null == uid){
      //this is a create action
      action = "create";
      idLabel.setVisibility(View.INVISIBLE);
      idField.setVisibility(View.INVISIBLE);
      createdLabel.setVisibility(View.INVISIBLE);
      createdField.setVisibility(View.INVISIBLE);
    } else {
      action = "update";
      idLabel.setVisibility(View.VISIBLE);
      idField.setVisibility(View.VISIBLE);
      createdLabel.setVisibility(View.VISIBLE);
      createdField.setVisibility(View.VISIBLE);
      
      idField.setText(uid);
      nameField.setText(name);
      createdField.setText(created);
    }
  }
  
  public boolean onCreateOptionsMenu(Menu menu){
    MenuItem saveItem = menu.add("Save");
    final Context that = this;
    saveItem.setOnMenuItemClickListener(new OnMenuItemClickListener() {
      
      @Override
      public boolean onMenuItemClick(MenuItem item) {
        String name = nameField.getText().toString();
        if("".equals(name)){
          FhUtil.showMessage(that, "Error", "Please enter name");
          return false;
        } else {
          Intent resultInt = new Intent();
          resultInt.putExtra("com.feedhenry.fhandroidexampleapp.name", name)
                   .putExtra("com.feedhenry.fhandroidexampleapp.uid", uid)
                   .putExtra("com.feedhenry.fhandroidexampleapp.created", created)
                   .putExtra("com.feedhenry.fhandroidexampleapp.action", "save");
          setResult(RESULT_OK, resultInt);
          finish();
          return true;
        }
      }
    });
    
    if("update".equals(action)){
      MenuItem delteItem = menu.add("Delete");
      delteItem.setOnMenuItemClickListener(new OnMenuItemClickListener() {
        
        @Override
        public boolean onMenuItemClick(MenuItem item) {
          Intent resultInt = new Intent();
          resultInt.putExtra("com.feedhenry.fhandroidexampleapp.uid", uid)
                   .putExtra("com.feedhenry.fhandroidexampleapp.action", "delete");
          setResult(RESULT_OK, resultInt);
          finish();
          return true;
        }
      });
    }
    return true;
  }

  
}
