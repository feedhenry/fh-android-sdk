# FeedHenry Android SDK (Alpha)

This SDK should provide you with all you'll need to start developing cloud-connected apps with the FeedHenry platform. The SDK provides access to cloud action calls.

### Build

To build the SDK, you first need to install the Android SDK from http://developer.android.com/sdk/installing/index.html.

Then you can clone the repo, go to the *sdk* directory, create a new file called local.properties file based on the local.properties.dist file, change the value of *sdk.dir* property to be the path to your local Android SDK directory.

Then run the ant task:

```ant```

This will compile the source code and genrate a jar file in the *dist* directory

### Usage

To use the Android SDK with your app, you'll need to do the following:

* Add fh-android-sdk.jar file to your application's build path in Eclipse
* Create a file called *fh.properties* in your application's *assets* directory. This file should contain the following properties:
  * **apiurl** - this is the base SDK URL, by default this is *http://apps.feedhenry.com- change this if your app lives on another domain.
  * **domain** - the domain is a shortened version of the apiurl - it's name which proceeds *.feedhenry.com* (e.g. the domain for http://**apps**.feedhenry.com is **apps**)
  * **app** & **inst** - these is the app's identifiers (**app** being an app's unique ID, an **inst** being an identifier for a particular version of an app). These can be obtained by logging into the Studio, opening your app and pressing **CTRL+ALT+G**
  * With these configured, you can now make Cloud action calls with the FeedHenry Android SDK. Examples of Cloud calls are included in the SDK, as well as below. 

### Examples

```
public class AndroidExampleApp extends ListActivity {

  private static final String TAG = "FHAndroidSDKExample";
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    final ProgressDialog diglog = ProgressDialog.show(this, "Loading", "Please wait...");
    FH.initializeFH(this);
    final ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,R.layout.list_event);
    getListView().setAdapter(adapter);
    try{
      FHActRequest request = FH.buildActRequest("getEventsByLocation", new JSONObject().put("longi", "-7.12").put("lati", "52.25"));
      request.executeAsync(new FHActCallback() {
        
        @Override
        public void success(FHResponse res) {
          JSONArray resObj = res.getArray();
          try{
            Log.d(TAG, resObj.toString(2));
            for(int i=0;i<resObj.length();i++){
              JSONObject event = resObj.getJSONObject(i);
              adapter.add(event.getString("title"));
            }
            diglog.dismiss();
          } catch(Exception e){
            Log.e(TAG, e.getMessage(), e);
          }
        }
        
        @Override
        public void fail(FHResponse res) {
          Log.e(TAG, res.getErrorMessage(), res.getError());
        }
      });
    } catch(Exception e){
      Log.e(TAG, e.getMessage(), e);
    }
  }
}

```
	
### Links
* [FeedHenry Documentation](http://docs.feedhenry.com)