package com.feedhenry.sdk.api;

import java.util.Locale;

import org.apache.http.Header;
import org.json.fh.JSONObject;

import android.content.Context;

import com.feedhenry.sdk.CloudProps;
import com.feedhenry.sdk.FHActCallback;
import com.feedhenry.sdk.FHHttpClient;
import com.feedhenry.sdk.FHRemote;
import com.feedhenry.sdk.utils.FHLog;

public class FHCloudRequest  extends FHRemote {
	
  public enum Methods {
    GET, POST, PUT, DELETE;

    public boolean equals(Methods pThat){
      return this.toString().equals(pThat.toString());
    }
    
    public static Methods parse(String pMethod) throws Exception{
      if(pMethod.toUpperCase(Locale.ENGLISH).equals(GET.toString())){
        return GET;
      } else if(pMethod.toUpperCase(Locale.ENGLISH).equals(POST)) {
        return POST;
      } else if(pMethod.toUpperCase(Locale.ENGLISH).equals(PUT)){
        return PUT;
      } else if(pMethod.toUpperCase(Locale.ENGLISH).equals(DELETE)){
        return DELETE;
      } else {
        throw new Exception("Unsupported HTTP method:" + pMethod.toUpperCase(Locale.ENGLISH));
      }
    }
  };
  
	protected static final String LOG_TAG = "com.feedhenry.sdk.api.FHCloudRequest";
	
	private CloudProps mCloudProps;
	private String mPath = "";
	private Methods mMethod = Methods.GET;
	private Header[] mHeaders = null;
	private JSONObject mArgs = new JSONObject();

	public FHCloudRequest(Context context, CloudProps pCloudProps) {
		super(context, pCloudProps.getAppProperties());
		mCloudProps = pCloudProps;
	}
	
	public void setPath(String pPath){
	  mPath = pPath;
	}
	
	public void setMethod(Methods pMethod){
	  mMethod = pMethod;
	}
	
	public void setHeaders(Header[] pHeaders) {
	  mHeaders = pHeaders;
	}
	
	public void setRequestArgs(JSONObject pArgs){
	  mArgs = pArgs;
	}

	@Override
	protected String getPath() {
		return mPath;
	}

	@Override
	protected JSONObject getRequestArgs() {
		return mArgs;
	}

  @Override
  public void executeAsync(FHActCallback pCallback) throws Exception {
    try {
      switch (mMethod) {
      case GET:
        FHHttpClient.get(getURL(), buildHeaders(mHeaders), mArgs, pCallback);
        break;
      case PUT:
        FHHttpClient.put(getURL(), buildHeaders(mHeaders), mArgs, pCallback);
        break;
      case POST:
        FHHttpClient.post(getURL(), buildHeaders(mHeaders), mArgs, pCallback);
        break;
      case DELETE:
        FHHttpClient.delete(getURL(), buildHeaders(mHeaders), mArgs, pCallback);
        break;
      default:
        break;
      }
    } catch (Exception e) {
      FHLog.e(LOG_TAG, e.getMessage(), e);
      throw e;
    }
  }
  
  private String getURL(){
    String host = mCloudProps.getCloudHost();
    return host + (getPath().startsWith("/")? getPath(): ("/" + getPath()));
  }

}
