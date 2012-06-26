package com.feedhenry.sdk;

import org.json.JSONObject;

/**
 * Representing a action call to FeedHenry's cloud side function
 *
 */

public interface FHAct {
   /**
    * Set the parameters for the action
    * @param pArgs the parameters for the cloud side function
    */
   public void setArgs(JSONObject pArgs);
   
   /**
    * Set the callback function to be executed when the action is finished
    * @param pCallback the callback function
    */
   public void setCallback(FHActCallback pCallback);
   
   /**
    * Execute the function asynchronously
    * @throws Exception
    */
   public void executeAsync() throws Exception;
   
   /**
    * Execute the function asynchronously. Execute the callback function when it finishes.
    * @param pCallback the callback function
    * @throws Exception
    */
   public void executeAsync(FHActCallback pCallback) throws Exception;
   
}
