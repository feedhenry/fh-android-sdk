# FeedHenry Android SDK

[![Travis](https://img.shields.io/travis/feedhenry/fh-android-sdk/master.svg)](http://travis-ci.org/feedhenry/fh-android-sdk)
[![Coveralls](https://img.shields.io/coveralls/feedhenry/fh-android-sdk/master.svg)](https://coveralls.io/github/feedhenry/fh-android-sdk)
[![License](https://img.shields.io/badge/-Apache%202.0-blue.svg)](http://www.apache.org/licenses/LICENSE-2.0)
[![Maven Central](https://img.shields.io/maven-central/v/com.feedhenry/fh-android-sdk.svg)](http://search.maven.org/#search%7Cga%7C1%7Cfh-android-sdk)
[![Javadocs](http://www.javadoc.io/badge/com.feedhenry/fh-android-sdk.svg?color=blue)](http://www.javadoc.io/doc/com.feedhenry/fh-android-sdk)

This SDK should provide you with all you'll need to start developing cloud-connected apps with the FeedHenry platform. The SDK provides access to cloud action calls, app authentication and authorization.

|                 | Project Info                                                 |
| --------------- | ------------------------------------------------------------ |
| License:        | Apache License, Version 2.0                                  |
| Build:          | Gradle                                                       |
| Documentation:  | http://www.javadoc.io/doc/com.feedhenry/fh-android-sdk       |
| Issue tracker:  | https://issues.jboss.org/browse/FH                           |
| Mailing lists:  | [feedhenry-dev](http://feedhenry-dev.2363497.n4.nabble.com/) |

## Building

```shell
./gradlew build
```

## Usage

There are two supported ways of developing apps using Feedhenry for Android: Android Studio and Maven.

### Android Studio

Add to your application's `build.gradle` file

```groovy
dependencies {
  compile 'com.feedhenry:fh-android-sdk:3.3.1'
}
```

### Maven

Include the following dependencies in your project's `pom.xml`

```xml
<dependency>
  <groupId>com.feedhenry</groupId>
  <artifactId>fh-android-sdk</artifactId>
  <version>3.3.1</version>
  <type>aar</type>
</dependency>
```

## Documentation

For more details about that please consult our [JavaDoc](http://www.javadoc.io/doc/com.feedhenry/fh-android-sdk).

## Demo apps

Take a look in our demo apps

* [Blank](https://github.com/feedhenry-templates/blank-android-gradle)
* [Helloworld](https://github.com/feedhenry-templates/helloworld-android-gradle)
* [Push Starter](https://github.com/feedhenry-templates/pushstarter-android-app)
* [Sync](https://github.com/feedhenry-templates/sync-android-app)
* [SAML](https://github.com/feedhenry-templates/saml-android-app)
* [OAuth2](https://github.com/feedhenry-templates/oauth-android-app)

## Development

If you would like to help develop Feedhenry you can join our [developer's mailing list](http://feedhenry-dev.2363497.n4.nabble.com/), join #feedhenry on Freenode, or shout at us on Twitter [@feedhenry](https://twitter.com/feedhenry).

## Questions?

Join our [developer's mailing list](http://feedhenry-dev.2363497.n4.nabble.com/) for any questions or shoot a question on Stackoverflow using [feedhenry tag](http://stackoverflow.com/questions/tagged/feedhenry)! We really hope you enjoy app development with Feedhenry!

## Found a bug?

If you found a bug please create a ticket for us on [Jira](https://issues.jboss.org/browse/FH) with some steps to reproduce it.