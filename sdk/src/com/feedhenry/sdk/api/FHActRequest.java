package com.feedhenry.sdk.api;

import java.util.Properties;

import com.feedhenry.sdk.FHRemote;

/**
 * The request for calling the cloud side function of the app
 *
 */
public class FHActRequest extends FHRemote {

  private String mRemoteAct;
  private static final String METHOD = "act";
  
  /**
   * Constructor
   * @param pProps the app configuration
   */
  public FHActRequest(Properties pProps){
    super(pProps);
  }
  
  /**
   * The name of the cloud side function
   * @param pAction cloud side function name
   */
  public void setRemoteAction(String pAction){
    mRemoteAct = pAction;
  }

  @Override
  protected String getPath(String pDomain, String pAppGuid, String pInstGuid) {
    return METHOD + "/" + pDomain + "/" + pAppGuid + "/" + mRemoteAct + "/" + pInstGuid;
  }
}
