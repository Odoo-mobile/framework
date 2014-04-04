.. OpenERP Mobile documentation master file, created by
   sphinx-quickstart on Tue Mar 25 14:15:37 2014.
   You can adapt this file completely to your liking, but it should at least
   contain the root `toctree` directive.

Introduction
============

OpenERP android framework is an open source, object oriented framework works with OpenERP JSON-RPC API Connector library.
 
This framework provides all supporting class files required to build a module for android client.
 
This framework is designed to fulfill requirement of android application based on any modules (addons or app) in the OpenERP.
 
It provides basic menu configuration, service providers and build in ORM for managing local database (SQLite)
 
Model is a database part. In this framework each module has its own separate model (database helper) file to manage their related data and requested by particular module at runtime.
 
View is a user interface. In this framework each UI is created separately for each module.
 
Controller is the framework itself. It will handle the user actions such as menu.

Basic directory structure of OpenERP Android framework:
-------------------------------------------------------
:: 

    openerp-mobile
    └── src
        ├── com
        │   └── openerp                 // Framework loader
        │       ├── base                // Base modules
        │       ├── config              // Module + Sync Config
        │       ├── orm                 // Application ORM Package
        │       ├── support             // Supporting classes
        │       ├── util                // Utility classes
        │       ├── auth                // Account authenticator
        │       └── addons              // All Modules (addons)
        │            └── idea           // Sample Idea Module
        │                ├── services   // Idea module services (optional)
        │                ├── widgets    // Idea module widgets (optional)
        │                └── providers
        │                    └── idea   // Idea module providers (optional)
        │                
        ├── libs                        // External Support libraries
        └── res                         // All UI Resources
            ├── drawable                // Application icons and images
            ├── layout                  // Application module UIs
            ├── menu                    // Application module menus
            ├── values                  // Application static String, attr, integers, styles...
            └── xml                     // Application provider and preference xmls
