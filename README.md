ZBiM Android SDK Documentation
=======

## Table of Contents
---
1. [Overview](#Overview)
2. [Requirements](#Requirements)
3. [Project Setup](#ProjectSetup)
4. [Quick Start](#QuickStart)
5. [Initialization](#Initialization)
6. [User Tags & Content Tailoring](#UserTagsContentTailoring)
7. [Content Structure](#ContentStructure)
8. [Content HUB UI & Navigation Model](#ContentHUBNavigationModel)
9. [Security](#Security)
10. [Custom URL Schemes Support](#CustomURLSchemesSupport)
11. [Configuration](#Configuration)
12. [Customizations](#Customizations)
13. [Reacting ZBIM SDK State Changes](#ZBIMSDKStateChanges)
14. [Facilitating An Adaptive Application UI](#AdaptiveApplicationUI)
15. [Logging](#Logging)
16. [Metrics](#Metrics)
17. [Local Notifications](#LocalNotifications)

<a id="Overview"/>
## Overview

This document provides an overview of ZBi for Marketers (ZBiM) SDK and describes the way it can be integrated into and interacted with by a client application.

The ZBiM SDK provides the ability to deliver tailored content inside a native application, where the content is managed directly by the marketing team responsible for the application. The goal is to bridge the gap between content creation and content delivery in an intuitive, transparent, and scalable way while allowing for personalization based on analyzing users' behaviors without involving the application development team or IT beyond the initial SDK integration.

<a id="Requirements"/>
## Requirements

ZBiM SDK requires:
* Android Studio 1.0+ or Intellij 14+
* Minimum API Target of 14

**NOTE**: Running the SDK in a version older than API 14 will cause an UnsupportedOperationException.

<a id="ProjectSetup"/>
## Project Setup

This document assumes that you have already created an Android project before in Android Studio or Intellij and are familiar with the Gradle build system.

For more information on downloading, installing, and getting started with Android development see the official Android developer documentation at: http://developer.android.com/index.html.

The following steps are used to reference the ZBiM SDK project in an application target:

**NOTE**: For the beta release, the AAR files that you need to integrate ZBiM SDK into your app are not yet part of a public Maven repository. Instead these are checked in alongside the sample code (under the libs folder) and referenced in the gradle config.

1. In the build.gradle for the application add the following to the list of repositories:<br/>
```gradle
flatDir {
   dirs 'libs'
}
```

2. In the build.gradle for the application add the following to the list of dependencies:<br/> 
```gradle
dependencies {
   compile(name:'zbim-sdk-android-1.2.0.41.RELEASE-20150626.204703-1', ext:'aar')
   compile(name:'xwalk_core_library', ext:'aar')
   compile 'com.google.code.gson:gson:2.3'
   compile 'com.google.android.gms:play-services:6.5.87'
   compile 'com.amazonaws:aws-android-sdk-sqs:2.2.1'
   compile 'org.apache.httpcomponents:httpmime:4.1'
   compile 'oauth.signpost:signpost-core:1.2.1.2'
   compile 'org.twitter4j:twitter4j-core:3.0.5'
   compile 'com.squareup.okhttp:okhttp:2.3.0'
   compile 'com.arcao:slf4j-timber:1.5'
   compile 'com.android.support:multidex:1.0.0'
}
```

<a id="QuickStart"/>
## Quick Start
Here's the minimum set of steps required to get a Content Hub up and running:

1. Save the Zumobi provided credentials into your assets folder.
  * zbimconfig.properties
  * pubkey.der
2. Initialize the ZBiM SDK.
  1. Inside your your main activity class add the following:<br/>
     ```java
      // Initialize ZBiM
      ZBiM mZBiM = ZBiM.getInstance(this);

      // Check for a current user and if none exists, create one.
      String userId = mZBiM.getActiveUser();
      if (userId == null) {
          userId = mZBiM.generateDefaultUserID();
          try {
              mZBiM.createUser(userId, null);
              mZBiM.setActiveUser(userId);
          }
          catch (ZBiMStateException e) {
              // Failed setting active user.
          }
      }
     ```

  2. Inside the Activity for launching the ContentHub, create a button with an onClick handler like the following:<br/>
     ```java
     try {
         mZBiM.launchContentHubActivity();
     }
     catch (ZBiMStateException ex) {
        // Failed opening content hub
     }
     ```

<a id="Initialization"/>
## SDK Initialization

### Initialize
Accessing the following method instantiates the singleton and sets up the ZBiM SDK. This is done once per application session and it is recommended that it is done as early as possible:<br/>
```java
ZBiM.getInstance(this);
```

**NOTE** All calls to the SDK, go through the singleton.

### New User Creation

The first time the SDK is initialized, the client application must create a user ID and set it as the active user. This user ID is used for tracking the user's activity while interacting with the Content Hub experience for metrics reporting and content tailoring purposes. The user ID is used in an anonymous fashion and the SDK has a built-in mechanism (described below) to generate a new user ID for this purpose if you do not have or cannot use an existing user ID.

If the client application allows multiple users to be logged in over time, a different user ID can be registered with the SDK and associated with each application user, enabling the SDK to preserve tailoring data in case a previously registered user comes back. An important thing to note is that while there can be multiple user IDs registered with the SDK, only one of them can be active at any point in time.

To create a new user, call:
```java
public void createUser(String userID, String[] tags)
```

If the application already has the concept of a user ID, e.g. the application requires the user to login to access services provided by the application, the application can pass that value (username or assigned unique identifier) as the ZBiM user ID.

It is important to note that it is the application's responsibility to ensure that no personally identifiable information is passed to ZBiM SDK. The username provided will be used as-is and no attempt at obfuscating the value will be made by the ZBiM SDK.

Alternatively the application can call the following method provided by the ZBiM SDK to have a new user ID generated on its behalf:
```java
public String generateDefaultUserID()
```

Once the user ID has been created, the application must call the following method in order to instruct the SDK to start using the newly created user ID:
```java
public void setActiveUser(String userID)
```

The client application can check if there is an active user already set by calling:
```java
public String getActiveUser()
```

<a id="UserTagsContentTailoring"/>
## User Tags & Content Tailoring

When creating a new user, the application has the option to pass a set of tags that sets up the user's profile when the user launches the application for the first time. This is to provide a hint to the ZBiM SDK on how it should initially customize the experience for that particular user. Such tags can initially be derived from user data that is already available to the marketer (e.g. "sports fan" or "high income"), but may change over time as the user interacts with the Content Hub experience. Tags are central to delivering tailored content to the user, however, providing these to the SDK is optional and can be skipped, in which case the ZBiM SDK will not start with a customized experience for the user.

The application also has the option to query ZBiM SDK for the set of tags accumulated until that point by calling:
```java
public ArrayList<String> getTagsForUser(String userID)
```

An important requirement for ZBiM SDK to function properly is that client code and content source must use the exact same set of tags. If there is a divergence, then the ZBiM SDK will likely not select the most appropriately tailored content and in certain case may not show any content at all.


<a id="ContentStructure"/>
## Content Structure
When content feeds are presented in-app to a user via the ZBiM SDK, we call this area of the application a Content Hub. In some cases, a brand might have multiple Content Hubs, which are expressed in one or more individual applications.

Content is organized logically in a hierarchy. The Content Hub loads data from a content source. A content source can have one or more hubs. A hub can have one or more channels and each channel can have one or more articles.

At each level of the hierarchy there is a set of tags associated with the corresponding item, e.g. hub, channel or article. When the time comes for the SDK to decide which hub to show or how to priority-order articles inside a channel these tags are compared against the set of tags associated with the current active user.

<a id="ContentHUBNavigationModel"/>
## Content HUB UI & Navigation Model

Content is presented to the user inside a Content Hub, which can be in the form of a modal view, taking over the entire screen, or nested inside a parent view, provided by the client application. It is up to the application to decide which mode is more appropriate in the context of what it is trying to achieve.

To display the Content Hub in it's own Activity, use:
```java
public void launchContentHubActivity()
```
or
```java
public void launchContentHubActivity(HashMap<String, String> tags)
```

To launch the Content Hub to a specific item; a channel or article/video, use:
```java
public void launchContentHubActivity(String strUri)
```

The tags array parameter is used to allow overriding the current set of tags associated with each user. This allows the client application to explicitly select which Content Hub to display, assuming there is more than one, without explicitly having to provide a URL address for it (doing so will require hardcoding the address in code, but the corresponding value can change inside the content source at any time). In the case where nil is passed for the tags parameter, the Content Hub will use tags associated with current active user to determine what content to show.

To present it as a Fragment, use:
```java
public ContentHubFragment getContentHubFragment()
```

The ContentHubFragment exposes several methods:
```java
public void goBack()
```
```java
public void canGoBack()
```
```java
public void goForward()
```
```java
public void canGoForward()
```
```java
public void setOnScrollListener(OnScrollListener listener)
```

ZBiM SDK keeps all navigation internal to the Content Hub except for the following cases: <br/>
1. Videos are played full-screen in a separate native Activity view. <br/>
2. Clicking on external links will prompt the user to leave the application and be navigated to the default browser. <br/>
3. Clicking on a link which implements a custom scheme, e.g. zbim_sample_app://URI, will launch an Intent for the Application to handle.

Content Hub to talk back to the application, notifying the application of an action that it needs to handle.

Only one Content Hub can be shown by the SDK at any point in time. Multiple side-by-side hubs or stack of hubs displayed on top of each other are not supported.

The default Content Hub view has a predefined user interface (close, back buttons, ZBiM logo). In case the application wants to customize the Content Hub UI so it fits more seamlessly with the rest of the UI, the application should open the ContentHub as a Fragment utilizing the ContentHubFragment. It is then the application's responsibility to show the corresponding navigation buttons and connect them to the methods exposed by the ContentHubFragment class.

Progress updates, e.g. indicating that new content is being downloaded, will be displayed inside the Content Hub UI. If content cannot be presented for any reason, a message will be displayed inside the Content Hub UI, informing users that an error occurred. Currently there is no provision to be able to customize either of those UIs, however the ZBiM SDK does expose notifications that the application can subscribe to, notifying it of error or progress update events taking place. For more details on the latter, please see the section on State.

<a id="Security"/>
## Security

Zumobi realizes the sensitive nature of serving content within the framework of an existing application and the various degrees to which a client application can be affected by a malicious or otherwise inappropriate content being served.

We take security very seriously and have included features that give an application's administrators direct control over the usage of the SDK with the application and the ability for the application itself to reject content that has been altered by an unauthorized party.

A basic premise of the core architecture of ZBiM is that web technologies expose a large number of possible vectors for attack, interception, and content tampering. From social engineering to technical issues that reveal vulnerabilities of the underlying application code, there are numerous ways a client
application can be impacted. Rather than attempting to address every attack vector separately, we took a more robust approach by creating a secured walled-garden content experience that is delivered by a system that secures each step of the content's journey until it reaches the native application.

In addition to the above, ZBiM also provides the option to remotely shut down your specific implementation of the Content Hub, which prevents the hub from appearing within your application. This gives the administrator the immediate ability to remove the Content Hubs from your application in the event that some inappropriate or unauthorized content was discovered.

All metadata needed to verify the integrity of the content is exchanged over a secured network connection (TLS). The metadata is then used to verify that the content itself has not been tampered with. For these additional checks to work the application bundle needs to contain a Zumobi-provided file that contains a public key specific to each application. The name of the file is pubkey.der and it must be carried as an asset by your application apk.

<a id="CustomURLSchemesSupport"/>
## Custom URL Schemes Support & Whitelisting

The approach descibed in the previous section works well in most cases, however the ZBiM SDK provides additional capabilities to enable deeper integration with the host application. One capability is the SDK's ability to utilize custom URL schemes. This enables the SDK to communicate with the host application for the purpose of performing actions not supported directly by the SDK itself. For example, the Content Hub may display an item available for purchase, but only the application can perform the checkout action. If the user taps on the purchase button within the Content Hub, the ZBiM SDK will generate a custom URL request for the host application to handle the checkout functionality.

By default, any URL request that cannot be resolved internally by ZBiM SDK will be flagged and rejected by the security checks operating while Content Hub is being interacted with. To get around that restriction the application's developer has the option to whitelist either a specific URL or a URL scheme, which will allow selected requests to go through. To whitelist a specific URL the application must call:

A URL scheme can be whitelisted by calling:
```java
public void whitelistScheme(String url)
```

When a whitelisted scheme is encountered an Intent is created and published so that the application can handle it.
* Example: <br/> 
  In AndroidManifest.xml
```xml
  <activity
    android:name=".ProductActivity">
      <intent-filter>
          <action android:name="android.intent.action.VIEW"/>
          <category android:name="android.intent.category.DEFAULT"/>
          <data android:scheme="myAppShowProduct"/>
      </intent-filter>
  </activity>
```
  In Code:
```java
  // Whitelist the scheme
 ZBiM.getInstance(this).whitelistScheme("myAppShowProduct");
```
 The sent Intent:
```java
 Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_DEFAULT);
        sendIntent.setData(Uri.parse(<URL>));
```

<a id="Configuration"/>
## Configuration

Configuration data must be in a zbimconfig.properties file, which is carried as an asset by the application apk. The configuration will be given to the SDK implementor. No changes will be needed in the file. For reference, these are the values to be aware of:

1. dbServiceUrl – a Zumobi-provided URL to content download service
2. appID – a Zumobi-provided ID needed for metrics reporting
3. metricsReportingQueueId – a Zumobi-provided ID also needed for metrics
reporting

<a id="Customizations"/>
## Customizations

There are several main aspects in which the ZBiM SDK's look and behavior can be customized.

#### Navigation Chrome

The Content Hub's default navigation chrome can be omitted entirely in favor of an application-provided one. This is achieved by nesting the Content Hub into a parent view and was already discussed in the Content Hub UI & Navigation Model section.

#### Color Schemes

The Content Hub UI supports two different color schemes - dark (default) and light - allowing the application to choose which one fits better with the existing look and feel. The color schemes selection affects the Content Hub's navigation chrome as well as the built-in progress reporting and error messaging. Supported values for color schemes are: ZBiMColorScheme.Dark and ZBiMColorScheme.Light.

To query the current color scheme, call:
```java
public ZBiMColorScheme getColorScheme()
```

To set the active color scheme, call:
```java
public void setColorScheme(ZBiMColorScheme colorScheme)
```

To set the background color of the ContentHub, call:
```java
public void setContentHubBackgroundColor(int color) 
```

#### Logging
The ZBiM SDK's logging can be fully adjusted to suit the application developer's needs. For more details see the Logging section later in this document.

#### Download Modes
There is quite a bit of flexibility in the download modes. These are controlled by the server and can be changed at anytime by the marketer. The modes are split into two parts: first database download and subsequent database downloads.

First Database download modes are:
* Immediately -- The database is immediately downloaded.
* OnDemand -- 2 possibilities
  * The download will be at the opening of the ContentHub
  * If the subsequent download of database is on a recurring schedule, that interval may come before opening the Content Hub, in which case, the first database download will be at that time

Subsequent Database download modes are
* OnDemand, but no stale content. In essence, when checking if there is a new DB when opening the Content Hub, download the new content and show it.
* OnDemand, but stale content is ok. In essence, when checking if there is a new DB when opening the Content Hub, show the previous downloaded content while the new content is retrieved. Show the new content on the next ContentHub opening.
* Scheduled. Content is downloaded on a periodic basis and is not due to ContentHub activity.

#### Advertising ID
The SDK allows the application to determine what constitutes a meaningful advertiser ID and pass the value back to the SDK to be used for reporting purposes. To take advantage of that customization option, the application must set it:
```java
public void setAdvertiserID(String id)
```

<a id="ZBIMSDKStateChanges"/>
## Reacting to ZBIM SDK State Changes

ZBiM SDK provides limited access to internal state-related information that host application can use to adjust UI to improve the user experience. This section describes publicly accessible state information and provides examples of how it can be used.

#### Methods
The following four methods allow access to a subset of ZBiM SDK internal states.

```java
public boolean isDownloadingContent()
```
  This indicates whether content source related metadata or content source itself is being currently downloaded.


```java
public boolean isDisplayingContentHub()
```
* This indicates if Content Hub is currently being displayed. It provides no information regarding what the hub is showing, i.e. actual content, progress indicator or error status


```java
public boolean isReady()
```
* This indicates whether SDK is ready to show content. Implies that a valid content has been already downloaded and validated and is ready to be presented.


```java
public boolean isDisabled()
```
* This indicates whether SDK has been shutdown remotely (typically as a security
precaution)


Please note that more than one of the above can return true simultaneously, e.g. content might be downloading while the Content Hub is being presented, but there's previously downloaded valid content, so the ZBiM SDK is in a "ready" state.

#### SDK State Change Notifications

For each of the above-mentioned states there is an IntentFilter that client code can subscribe to if it needs to know when the corresponding state changes.

The IntentFilter names are:
* ZBiM.DOWNLOADING_CONTENT_CHANGED_ACTION
* ZBiM.DISPLAYING_CONTENT_HUB_CHANGED_ACTION
* ZBiM.READY_STATE_CHANGED_ACTION
* ZBiM.DISABLED_STATE_CHANGED_ACTION

One example of how the application can take advantage of the above is by showing or hiding the entry point (e.g. a button) to the Content Hub. If the ZBiM SDK has been remotely disabled, we know the application will not download and display a Content Hub for the remainder of the application session. In such a scenario, the entry point can be hidden altogether until any security concerns have been resolved to provide the user with a more seamless app experience until the Content Hub is again accessible.

<a id="AdaptiveApplicationUI"/>
## Facilitating an Adaptive Application UI

In addition to state-related information, the ZBiM SDK can notify host application when certain events of interest take place. This allows the application to adapt its UI in order to provide a more seamless ZBiM SDK integration.

#### Download State Changes
IntentFilter name:
* ZBiM.DOWNLOAD_PROGRESS_CHANGED_ACTION

IntentFilter Extra Keys:
* ZBiM.DOWNLOAD_PROGRESS_CHANGED_ACTION_EXTRA

When the progress of the database download changes, the SDK sends a LocalBroadcast with this information. The application can register as a receiver for the LocalBroadcast.
The Intent will have an Extra, ZBiM.DOWNLOAD_PROGRESS_CHANGED_ACTION_EXTRA, for the progress. The progress is an Integer from 0-100. The event will be fired every time when there's a notable change in download progress.

#### Content State Changes
IntentFilter name:
* ZBiM.CONTENT_TYPE_CHANGED_ACTION

IntentFilter Extra Keys:
* ZBiM.CONTENT_TYPE_CHANGED_TYPE_ACTION_EXTRA
* ZBiM.CONTENT_TYPE_CHANGED_URL_ACTION_EXTRA
* ZBiM.CONTENT_TYPE_CHANGED_TITLE_ACTION_EXTRA

The application can subscribe for the ZBiMContentTypeChanged notification, which gets triggered every time the Content Hub navigates to a new piece of content. The set of resource types indicating what content is currently loaded are:
* ZBiMResourceTypeHub
* ZBiMResourceTypeChannel
* ZBiMResourceTypeArticle

By monitoring the resource type of the currently loaded content item the application can implement, for example, conditional logic to disable the hiding and showing animation for the navigation chrome when user is reading an article and re-enable it when user navigates back to channel or hub views. Again, a sample implementation of the above-mentioned scenario can be found in the ZBiM Sample App's source code.

#### Scroll State Changes
```java
public void setOnScrollListener(OnScrollListener listener)
```

OnScrollListener Interface has three methods:
```java
public void onScrollStarting(int horizontalScrollPosition, int verticalScrollPosition);
```

```java
public void onScroll(int horizontalScrollPosition, int verticalScrollPosition);
```

```java
public void onScrollEnding(int horizontalScrollPosition, int verticalScrollPosition);
```

This is targeted primarily at the ContentHub Fragment mode, where the Content Hub is presented inside a parent view. By getting access to the raw scrolling events, the parent view can implement behaviors such as hiding and showing the navigation chrome depending on the scroll direction. A sample implementation can be found as part of the ZBiM Sample Application's source code.


<a id="Logging"/>
## Logging

Logging is designed to be pretty flexible, allowing the client application to decide what gets logged and how.

The client application can set the logging delegate by calling:

```java
public void setLoggingDelegate(ZBiMLogging loggingDelegate)
```

ZBiMLogging Interface has three methods:
```java
public void error(String message, String recoverySuggestion)
```

```java
public void warning(String message, String recoverySuggestion);
```

```java
public void info(String message);
```

<a id="Metrics"/>
## Metrics

The ZBiM SDK has an onboard metrics collection system that enables marketers to measure the performance of the various pieces of content and the interaction history of the user against the Content Hub for segmentation purposes. The SDK will periodically upload collected metrics to a cloud-based reporting system over a secured connection for marketing analysis. This secured, on-device metrics system is designed to work over mobile networks and is resilient against network interruptions or if the user puts his/her device into "airplane" mode.
For metrics collection to work properly, the client application must have the correct reporting queue ID in the zbimconfig.plist file. This value is assigned by Zumobi.

<a id="LocalNotifications"/>
## Local Notifications

ZBiM SDK supports scheduling of local notifications on behalf of the client application. These notifications come from the database and are scheduled to show up sometime in the future. Metadata describing when a local notification is to be scheduled, as well as what its message and action should be are provided via the ZBiM Portal. The SDK handles all scheduling and showing of these notifications.



[diagram1]: diagram1.png
[diagram2]: diagram2.png

