package com.feedhenry.sdk;

import java.util.Properties;

import org.json.JSONObject;

public abstract class FHRemote implements FHAct{
  
  public FHRemote(Properties pProps){
    
  }
  
  public void setCallback(FHActCallback pCallback){
    
  }
  
  @Override
  public void execute() {
    // TODO Auto-generated method stub

  }

  @Override
  public void executeAsync() {
    // TODO Auto-generated method stub

  }

  @Override
  public void executeAsync(FHActCallback pCallback) {
    // TODO Auto-generated method stub

  }

  @Override
  public void setArgs(JSONObject pArgs) {
    // TODO Auto-generated method stub
    
  }

}
