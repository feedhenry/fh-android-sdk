/**
 * Copyright (c) 2015 FeedHenry Ltd, All Rights Reserved.
 *
 * Please refer to your contract with FeedHenry for the software license agreement.
 * If you do not have a contract, you do not have a license to use this software.
 */
package com.feedhenry.sdk;

/**
 * An FHActCallback will be used to execute code after a FH API request finishes running on a background thread.
 * This will make sure the UI thread does not block.
 * The {@link #success} and {@link #fail} methods will run on the UI thread.
 * You can either implement this interface in your app's own classes or using anonymous inner classes.
 * For example:
 *
 * Implementing as part of a class:
 *
 * <code>
 * public class Foo implements FHActCallback {
 *   {@literal @}Override
 *   public void success(FHResponse pResp){
 *       //process response data
 *   }
 *
 *   {@literal @}Override
 *   public void fail(FHResponse pResp){
 *       //process error data
 *   }
 * }
 *
 * ...
 *
 * FHActRequest request = FH.buildActRequest("readData, new JSONObject());
 * request.executeAsync(this);
 * </code>
 *
 * Using an anonymous class:
 *
 * <code>
 * FHActRequest request = FH.buildActRequest("readData", new JSONObject());
 * request.executeAsync(new FHActCallback(){
 *   {@literal @}Override
 *   public void success(FHResponse pResp){
 *     //process response data
 *   }
 *
 *   {@literal @}Override
 *   public void fail(FHResponse pResp){
 *     //process error data
 *   }
 * });
 * </code>
 */
public interface FHActCallback {

    /**
     * Will be run if the action call is successful
     *
     * @param pResponse the response data
     */
    void success(FHResponse pResponse);

    /**
     * Will be run if the action call is failed
     *
     * @param pResponse the response data
     */
    void fail(FHResponse pResponse);
}
