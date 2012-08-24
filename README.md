# FeedHenry Android SDK

This SDK should provide you with all you'll need to start developing cloud-connected apps with the FeedHenry platform. The SDK provides access to cloud action calls, app authentication and authorization. 

### Build

To build the SDK, you first need to install the Android SDK from http://developer.android.com/sdk/installing/index.html.

Then you can clone the repo, go to the *sdk* directory, create a new file called local.properties file based on the local.properties.dist file, change the value of *sdk.dir* property to be the path to your local Android SDK directory.

Then run the ant task:

```
ant

```

This will compile the source code and genrate a jar file in the *dist* directory

### Update Java Docs

We use Github Pages to serve Java Docs. To update the docs, you should do the following:

* Clone the repo
* Checkout master branch, update Java Docs comments in the code
* Go to *sdk* directory, run *ant doc* command to re-generate all the docs
* Commit the changes
* Checkout *gh-pages* branch, rebase it to master branch
* Push the chagnes for both branches

### Usage

See [FH Android SDK Guide](http://docs.feedhenry.com/v2/sdk_android.html)

### Example

The *example* directory contains an example to demostrate how to use all the android native APIs. You can import it into Eclipse and run it on the emulator or device.
	
### Links
* [FeedHenry Documentation](http://docs.feedhenry.com)