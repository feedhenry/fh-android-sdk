package com.feedhenry.sdk.api;

import android.content.Context;
import com.feedhenry.sdk.AppProps;
import com.feedhenry.sdk.FH;
import com.feedhenry.sdk.FHActCallback;
import com.feedhenry.sdk.FHHttpClient;
import com.feedhenry.sdk.FHRemote;
import com.feedhenry.sdk.FHResponse;
import com.feedhenry.sdk.utils.DataManager;
import com.feedhenry.sdk.utils.FHLog;
import javax.xml.crypto.Data;
import org.json.fh.JSONObject;

public class FHAuthSession {
  public static final String SESSION_TOKEN_KEY = "sessionToken";
  private static final String LOG_TAG = "com.feedhenry.sdk.api.FHAuthSession";

  private static final String VERIFY_SESSION_ENDPOINT = "verifysession";
  private static final String REVOKE_SESSION_ENDPOINT = "revokesession";

  private static FHAuthSession session;

  private FHAuthSession() {

  }

  public static FHAuthSession getInstance() {
    if (null == session) {
      session = new FHAuthSession();
    }
    return session;
  }

  /**
   * Check if a sessionToken value exists on the device
   * @return if the sessionToken exists
   */
  public boolean exists() {
    String sessionToken = DataManager.getInstance().read(SESSION_TOKEN_KEY);
    return null != sessionToken;
  }

  /**
   * Return the value of the current session token
   * @return the current session token value
   */
  public String getToken(){
    return DataManager.getInstance().read(SESSION_TOKEN_KEY);
  }

  /**
   * Save the seesionToken value on the device
   * @param sessionToken
   */
  protected void save(String sessionToken) {
    DataManager.getInstance().save(SESSION_TOKEN_KEY, sessionToken);
  }

  /**
   * Call remote server to check if the existing sessionToken is actually valid
   * @param pCallback a callback to be executed when remote call is completed
   * @throws Exception
   */
  public void verify(Callback pCallback, boolean pSync) throws Exception {
    String sessionToken = DataManager.getInstance().read(SESSION_TOKEN_KEY);
    if (null != sessionToken) {
      callRemote(VERIFY_SESSION_ENDPOINT, sessionToken, pCallback, pSync);
    }
  }

  /**
   * Remove the session token on the device and try to remove it remotely as well.
   * @throws Exception
   */
  public void clear(boolean pSync) throws Exception {
    String sessionToken = DataManager.getInstance().read(SESSION_TOKEN_KEY);
    if(null != sessionToken){
      DataManager.getInstance().remove(SESSION_TOKEN_KEY);
      try{
        callRemote(REVOKE_SESSION_ENDPOINT, sessionToken, null, pSync);
      } catch (Exception e){
        FHLog.w(LOG_TAG, e.getMessage());
      }
    }
  }

  private void callRemote(String pPath, String pSessionToken, final Callback pCallback, boolean pUseSync) throws Exception {
    String host = AppProps.getInstance().getHost();
    String url = (host.endsWith("/") ? host.substring(0, host.length() - 1) : host) + FHRemote.PATH_PREFIX + pPath;
    JSONObject params = new JSONObject().put(SESSION_TOKEN_KEY, pSessionToken);
    try{
      FHHttpClient.post(url, null, params, new FHActCallback() {
        @Override
        public void success(FHResponse pResponse) {
          JSONObject res = pResponse.getJson();
          boolean isValid = res.getBoolean("isValid");
          if(null != pCallback){
            pCallback.handleSuccess(isValid);
          }

        }

        @Override
        public void fail(FHResponse pResponse) {
          FHLog.w(LOG_TAG, pResponse.getRawResponse());
          if(null != pCallback){
            pCallback.handleError(pResponse);
          }
        }
      }, pUseSync);
    } catch (Exception e){
      FHLog.e(LOG_TAG, e.getMessage(), e);
      throw e;
    }
  }

  public static interface Callback {

    public void handleSuccess(boolean isValid);

    public void handleError(FHResponse pRes);
  }
}
