# FeedHenry Android SDK [![Build Status](https://travis-ci.org/feedhenry/fh-android-sdk.png)](https://travis-ci.org/feedhenry/fh-android-sdk)


This SDK should provide you with all you'll need to start developing cloud-connected apps with the FeedHenry platform. The SDK provides access to cloud action calls, app authentication and authorization. 

## Build

### Prereqs

* [Java 6](http://www.oracle.com/technetwork/java/javase/downloads/index.html)
* [Maven 3.1.1](http://maven.apache.org/)
* Latest [Android SDK](https://developer.android.com/sdk/index.html) and [Platform version](http://developer.android.com/tools/revisions/platforms.html)
* Latest [Maven Android SDK Deployer](https://github.com/mosabua/maven-android-sdk-deployer)
* Set ```ANDROID_HOME``` environment variable

### Building

This library is built as an aar project using Maven, but Google does not ship all the required libraries to Maven Central. You must locally deploy them using the [maven-android-sdk-deployer](https://github.com/mosabua/maven-android-sdk-deployer).

Run the following commands outside the FeedHenry Android SDK directory to avoid polluting the repo:

```
git clone git://github.com/mosabua/maven-android-sdk-deployer.git
cd $PWD/maven-android-sdk-deployer/platforms/android-21
mvn install -N --quiet
cd -
cd $PWD/maven-android-sdk-deployer/repositories/google-m2repository
mvn install -N --quiet
```

Now let's build the library.

In the base repo directory:

```
cd $PWD/fh-android-sdk
mvn clean package
```

This will compile the source code, generating an aar and a jar file in the _target_ directory.

## Deploy

Until we don't have it released in Maven Central we are hosting it in a [Github repo](https://github.com/feedhenry/fh-android-sdk-repository)

To deploy it, you need to follow the instructions below:

For create snapshots

```shell
mvn -DaltDeploymentRepository=snapshot-repo:default::file:[your repo dir]/snapshots clean deploy
```

For create releases

```shell
mvn -DaltDeploymentRepository=releases-repo:default::file:[your repo dir]/releases clean deploy
```

After that, commit it in the [fh-android-sdk-repository](https://github.com/feedhenry/fh-android-sdk-repository) and send a PR on it

## Usage

Add our Github host repo in your `gradle.build` project

```
allprojects {
	repositories {
   	jcenter()
		maven { url "https://github.com/danielpassos/fh-android-sdk-repository/raw/master/releases" }
	}
}
```

Add the FH Android `aar` library in your `build.gradle` project

```
dependencies {
	compile 'com.feedhenry:fh-android-sdk:2.3.1'
}
```

For more detail about our API, See [FH Android SDK Guide](http://docs.feedhenry.com/v3/dev_tools/sdks/android.html).

## Example

The _example_ directory contains an example to demonstrate how to use all the Android native APIs. You can import it into Eclipse and run it on an emulator or device.

If building in Android Studio, you will need to manually add the jar as a library, as well as the [Android Asynchronous Http Client](http://loopj.com/android-async-http/).
	
## Links

* [FeedHenry Documentation](http://docs.feedhenry.com)