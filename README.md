# FeedHenry Android SDK (Alpha)

This SDK should provide you with all you'll need to start developing cloud-connected apps with the FeedHenry platform. The SDK provides access to cloud action calls, app authentication and authorization. 

### Build

To build the SDK, you first need to install the Android SDK from http://developer.android.com/sdk/installing/index.html.

Then you can clone the repo, go to the *sdk* directory, create a new file called local.properties file based on the local.properties.dist file, change the value of *sdk.dir* property to be the path to your local Android SDK directory.

Then run the ant task:

```
ant

```

This will compile the source code and genrate a jar file in the *dist* directory

### Usage

#### Start with a new project

* The *FHStarterProject* directory contains a new empty project for you to start with. The SDK is included in the project and setup. You just need to update the fh.properties files with your app's configurations

#### Existing project

To use the Android SDK with your existing app, you'll need to do the following:

* Add fh-&lt;version&gt;.jar file to your application's libs directory. The ADT tool should automatically add the jar file to the project's build path.
* Create a file called *fh.properties* in your application's *assets* directory. This file should contain the following properties:
  * **host** - &lt;the app's host name&gt;
  * **appID** - &lt;id of the app&gt;
  * **appKey** - &lt;the api key of the app&gt;
  * **mode** - &lt;should be dev or prod&gt;

* Add internet permissions in the application's AndroidManifest.xml file
  
With these configured, you can now make Cloud action calls with the FeedHenry Android SDK. 

### Example

The *example* directory contains an example to demostrate how to use all the android native APIs. You can import it into Eclipse and run it on the emulator or device.
	
### Links
* [FeedHenry Documentation](http://docs.feedhenry.com)