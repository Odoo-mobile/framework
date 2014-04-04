.. OpenERP Mobile documentation master file, created by
   sphinx-quickstart on Tue Mar 25 14:15:37 2014.
   You can adapt this file completely to your liking, but it should at least
   contain the root `toctree` directive.

Downloading and Installing
==================================

Get the Android SDK
-------------------

The Android SDK provides you the API libraries and developer tools necessary to build, test, and debug apps for Android.
 
It is recommend to download the ADT Bundle to quickly start developing apps. It includes the essential Android SDK components and a version of the Eclipse IDE with built-in ADT (Android Developer Tools) to streamline your Android app development.

Download ADT Bundle http://developer.android.com/sdk/index.html

Installing the Eclipse Plugin
-----------------------------

Android offers a custom plugin for the Eclipse IDE, called Android Development Tools (ADT). This plugin provides a powerful, integrated environment in which to develop Android apps. It extends the capabilities of Eclipse to let you quickly set up new Android projects, build an app UI, debug your app, and export signed (or unsigned) app packages (APKs) for distribution.

If you need to install Eclipse, you can download it from eclipse.org/downloads/.

.. note:: 
	If you prefer to work in a different IDE, you do not need to install Eclipse or ADT. Instead, you can directly use the SDK tools to build and debug your application.
	
Download the ADT Plugin
-----------------------

* Start Eclipse, then select Help > Install New Software.
* Click Add, in the top-right corner.
* In the Add Repository dialog that appears, enter "ADT Plugin" for the Name and the following URL for the Location: https://dl-ssl.google.com/android/eclipse/
* Click OK.If you have trouble acquiring the plugin, try using "http" in the Location URL, instead of "https" (https is preferred for security reasons).
* In the Available Software dialog, select the checkbox next to Developer Tools and click Next.
* In the next window, you'll see a list of the tools to be downloaded. Click Next.
* Read and accept the license agreements, then click Finish. If you get a security warning saying that the authenticity or validity of the software can't be established, click OK.
* When the installation completes, restart Eclipse.

Configure the ADT Plugin
------------------------

* Once Eclipse restarts, you must specify the location of your Android SDK directory:
* In the "Welcome to Android Development" window that appears, select Use existing SDKs.
* Browse and select the location of the Android SDK directory you recently downloaded and unpacked.
* Click Next.

Your Eclipse IDE is now set up to develop Android apps, but you need to add the latest SDK platform tools and an Android platform to your environment.
 
**References :**
http://developer.android.com/sdk/index.html
