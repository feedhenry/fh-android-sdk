package com.feedhenry.sdk.sync;

public class NotificationMessage {
  private String mDataId;
  private String mUID;
  private String mCodeMessage;
  private String mExtraMessage;
  
  public NotificationMessage(String pDataId, String pUID, String pCodeMessage, String pExtraMessage){
    this.mDataId = pDataId;
    this.mUID = pUID;
    this.mCodeMessage = pCodeMessage;
    this.mExtraMessage = pExtraMessage;
  }
  
  public String getDataId(){
    return mDataId;
  }
  
  public String getUID(){
    return mUID;
  }
  
  public String getCode(){
    return mCodeMessage;
  }
  
  public String getMessage(){
    return mExtraMessage;
  }
  
  public String toString(){
    return "DataId:" + mDataId + "-UID:" + mUID + "-Code:" + mCodeMessage + "-Message:" + mExtraMessage;
  }
  
}
