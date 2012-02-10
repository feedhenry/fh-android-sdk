package com.feedhenry.sdk;

import java.util.Properties;

import org.json.JSONObject;

public interface FHAct {
   public void setArgs(JSONObject pArgs);
   public void execute();
   public void executeAsync();
   public void executeAsync(FHActCallback pCallback);
}
