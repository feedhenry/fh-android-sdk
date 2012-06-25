package com.feedhenry.sdk.api;

import java.util.Properties;

import com.feedhenry.sdk.FHRemote;

public class FHActRequest extends FHRemote {

  private String mRemoteAct;
  private static final String METHOD = "act";
  
  public FHActRequest(Properties pProps){
    super(pProps);
  }
  
  public void setRemoteAction(String pAction){
    mRemoteAct = pAction;
  }

  @Override
  protected String getPath(String pDomain, String pAppGuid, String pInstGuid) {
    return METHOD + "/" + pDomain + "/" + pAppGuid + "/" + mRemoteAct + "/" + pInstGuid;
  }
}
