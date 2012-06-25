package com.feedhenry.sdk;

import org.json.JSONObject;

public interface FHAct {
   public void setArgs(JSONObject pArgs);
   public void executeAsync() throws Exception;
   public void executeAsync(FHActCallback pCallback) throws Exception;
   public void setCallback(FHActCallback pCallback);
}
