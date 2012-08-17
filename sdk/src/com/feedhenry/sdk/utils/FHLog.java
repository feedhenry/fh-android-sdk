package com.feedhenry.sdk.utils;

import android.util.Log;

import com.feedhenry.sdk.FH;

public class FHLog {

  private static void log(int pLogLevel, String pTag, String pMessage, Throwable pThrowable){
    if(pLogLevel >= FH.getLogLevel()){
      if(null == pThrowable){
        Log.println(FH.getLogLevel(), pTag, pMessage);
      } else {
        Log.println(FH.getLogLevel(), pTag, pMessage + "\n" + Log.getStackTraceString(pThrowable));
      }
    }
  }
  
  public static void v(String pTag, String pMessage){
    log(FH.LOG_LEVEL_VERBOSE, pTag, pMessage, null);
  }
  
  public static void d(String pTag, String pMessage){
    log(FH.LOG_LEVEL_DEBUG, pTag, pMessage, null);
  }
  
  public static void i(String pTag, String pMessage){
    log(FH.LOG_LEVEL_INFO, pTag, pMessage, null);
  }
  
  public static void w(String pTag, String pMessage){
    log(FH.LOG_LEVEL_WARNING, pTag, pMessage, null);
  }
  
  public static void e(String pTag, String pMessage, Throwable pThrowable){
    log(FH.LOG_LEVEL_ERROR, pTag, pMessage, pThrowable);
  }
  
}
