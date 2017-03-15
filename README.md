# FeedHenry Android SDK

[![Travis](https://img.shields.io/travis/feedhenry/fh-android-sdk.svg)](http://travis-ci.org/feedhenry/fh-android-sdk)
[![Coveralls](https://img.shields.io/coveralls/feedhenry/fh-android-sdk/master.svg)](https://coveralls.io/github/feedhenry/fh-android-sdk)
[![License](https://img.shields.io/badge/-Apache%202.0-blue.svg)](https://opensource.org/s/Apache-2.0)
[![Maven Central](https://img.shields.io/maven-central/v/com.feedhenry/fh-android-sdk.svg)](http://search.maven.org/#search%7Cga%7C1%7Cfh-android-sdk)
[![Javadocs](http://www.javadoc.io/badge/com.feedhenry/fh-android-sdk.svg?color=blue)](http://www.javadoc.io/doc/com.feedhenry/fh-android-sdk)

This SDK should provide you with all you'll need to start developing cloud-connected apps with the FeedHenry platform. The SDK provides access to cloud action calls, app authentication and authorization. 

## Build

### Prereqs

* [Java 7](http://www.oracle.com/technetwork/java/javase/downloads/index.html)
* [Maven 3.1.1](http://maven.apache.org/)
* Latest [Android SDK](https://developer.android.com/sdk/index.html) and [Platform version](http://developer.android.com/tools/revisions/platforms.html)
* Latest [Maven Android SDK Deployer](https://github.com/mosabua/maven-android-sdk-deployer)
* Set ```ANDROID_HOME``` environment variable

### Building

This library is built as an aar project using Maven, but Google does not ship all the required libraries to Maven Central. You must locally deploy them using the [maven-android-sdk-deployer](https://github.com/mosabua/maven-android-sdk-deployer).

Run the following commands outside the FeedHenry Android SDK directory to avoid polluting the repo:

```
git clone git://github.com/mosabua/maven-android-sdk-deployer.git
cd $PWD/maven-android-sdk-deployer/platforms/android-23
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

## Usage

### Gradle Projects (Recommended)

Add the FH Android `aar` library in your `build.gradle` project

```
dependencies {
	compile 'com.feedhenry:fh-android-sdk:3.1.0'
}
```

For more detail about our API, See [FH Android SDK Guide](http://docs.feedhenry.com/v3/dev_tools/sdks/android.html).

### Ant Projects

To use the Android SDK in Ant-based Android projects, the following files need to be added as the dependencies:

* The fh-android-sdk jar file (e.g. [3.1.0 Jar File](https://repository.jboss.org/nexus/service/local/repositories/releases/content/com/feedhenry/fh-android-sdk/3.1.0/))
* All the jar files in the [deps](./deps) directory


## Example

The _example_ directory contains an example to demonstrate how to use all the Android native APIs. You can import it into Eclipse and run it on an emulator or device.

If building in Android Studio, you will need to manually add the jar as a library, as well as the [Android Asynchronous Http Client](http://loopj.com/android-async-http/).
	
## Links

* [FeedHenry Documentation](http://docs.feedhenry.com)