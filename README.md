# FeedHenry Android SDK

This SDK should provide you with all you'll need to start developing cloud-connected apps with the FeedHenry platform. The SDK provides access to cloud action calls, app authentication and authorization. 

## Build

### Prereqs

* [Java 6](http://www.oracle.com/technetwork/java/javase/downloads/index.html)
* [Maven 3.1.1](http://maven.apache.org/)
* Latest [Android SDK](https://developer.android.com/sdk/index.html) and [Platform version](http://developer.android.com/tools/revisions/platforms.html)
* Latest [Maven Android SDK Deployer](https://github.com/mosabua/maven-android-sdk-deployer)
* Set ```ANDROID_HOME``` environment variable

### Building

This library is built as aar project using Maven, but Google does not ship all the required libraries to Maven Central. You must locally deploy them using the [maven-android-sdk-deployer](https://github.com/mosabua/maven-android-sdk-deployer).

```
git clone git://github.com/mosabua/maven-android-sdk-deployer.git
cd $PWD/maven-android-sdk-deployer/platforms/android-21
mvn install -N --quiet
```

Now let's build the library

```
mvn clean package
```

This will compile the source code and genrate an aar and a jar  file in the _target_ directory

## Usage

See [FH Android SDK Guide](http://docs.feedhenry.com/v2/sdk_android.html)

## Example

The _example_ directory contains an example to demostrate how to use all the android native APIs. You can import it into Eclipse and run it on the emulator or device.
	
## Links

* [FeedHenry Documentation](http://docs.feedhenry.com)