package com.feedhenry.sdk;


/**
 * Representing a request call to FeedHenry.
 * 
 * When the asynchronous mode is used, the request will be executed on a separate thread and the calling thread will be returned immediately.
 * When the request is completed, the callback to process the response will be executed on the calling thread by using Looper class.
 * Therefore the calling thread needs to be alive for the callback to be processed properly.
 * 
 * If you are using APIs on short-lived threads, consider using the sync mode instead.
 * 
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
   
   /**
    * Execute the request synchronously.
    * @param pCallback
    * @throws Exception
    */
   public void execute(FHActCallback pCallback) throws Exception;
   
   /**
    * Set the client unique id (device id) for the request
    * @param pUDID the unique device id
    */
   public void setUDID(String pUDID);
   
}
