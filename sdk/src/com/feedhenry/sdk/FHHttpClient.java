package com.feedhenry.sdk;

import android.util.Log;
import org.apache.http.entity.StringEntity;
import org.json.JSONArray;
import org.json.JSONObject;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;

/**
 * The static HTTP client for use solely by the FeedHenry SDK, implemented as
 * outlined at http://loopj.com/android-async-http/
 */
class FHHttpClient {
	private static AsyncHttpClient mClient = new AsyncHttpClient();
	static {
		mClient.setUserAgent("fh_android_sdk");
	}

	// We only need to declare a POST function; the FeedHenry act calls make no
	// use of GET.
	public static void post(String url, JSONObject params,
			FHActCallback callback) throws Exception {
		
		StringEntity entity;
		if (params != null) {
			entity = new StringEntity(params.toString(), "UTF-8");
		} else {
			entity = new StringEntity(new JSONObject().toString());
		}
		
		mClient.post(null, url, entity, "application/json",
				new FHJsonHttpResponseHandler(callback));
	}

	static class FHJsonHttpResponseHandler extends JsonHttpResponseHandler {

		private FHActCallback callback = null;

		public FHJsonHttpResponseHandler(FHActCallback pCallback) {
			super();
			callback = pCallback;
		}

		@Override
		public void onSuccess(JSONArray pRes) {
			Log.d(FH.LOG_TAG, "Got response : " + pRes.toString());
			if (null != callback) {
				FHResponse fhres = new FHResponse(null, pRes, null, null);
				callback.success(fhres);
			}
		}

		public void onSuccess(JSONObject pRes) {
			Log.d(FH.LOG_TAG, "Got response : " + pRes.toString());
			if (null != callback) {
				FHResponse fhres = new FHResponse(pRes, null, null, null);
				callback.success(fhres);
			}
		}

		public void onFailure(Throwable e, String content) {
			Log.e(FH.LOG_TAG, e.getMessage(), e);
			if (null != callback) {
				FHResponse fhres = new FHResponse(null, null, e, content);
				callback.fail(fhres);
			}
		}
	}
}
