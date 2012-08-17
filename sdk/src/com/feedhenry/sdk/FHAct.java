package com.feedhenry.sdk;


/**
 * Representing a request call to FeedHenry. All the requests are executed on a background thread so that the main UI thread does not freeze.
 * The callback function will be executed on the main UI thread.
 */

public interface FHAct {
   /**
    * Set the callback function to be executed when the action is finished.
    * @param pCallback the callback function
    */
   public void setCallback(FHActCallback pCallback);
   
   /**
    * Execute the request asynchronously. Execute the callback function set by {@link #setCallback(FHActCallback pCallback)} when the request finishes.
    * @throws Exception
    */
   public void executeAsync() throws Exception;
   
   /**
    * Execute the request asynchronously. Execute the pCallback function when it finishes.
    * @param pCallback the callback function
    * @throws Exception
    */
   public void executeAsync(FHActCallback pCallback) throws Exception;
   
}
