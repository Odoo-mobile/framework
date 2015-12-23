DevicePermissionHelper Developer Guide
======================================

DevicePermissionHelper is helper class for getting permission from user in API23+ android devices. 

**Constructors**

```java
DevicePermissionHelper(OdooCompatActivity activity);
DevicePermissionHelper(BaseFragment fragment);
```

Takes valid **OdooCompatActivity** or **BaseFragment** 

OdooCompatActivity contains required callbacks for permission model and extends **AppCompatActivity**

Check application has permission
---------------------------------

**hasPermission()**

**Syntax:**

```java
boolean hasPermission(String permission);
```

Returns `true` if permission granted, `false` otherwise


**requestToGrantPermission()**

**Syntax:**

```java
void requestToGrantPermission(PermissionGrantListener callback, String permission);
```

Takes permission grant listener and permission

**PermissionGrantListener** will contains 3 callback methods :

    - onPermissionGranted()
    - onPermissionDenied()
    - onPermissionRationale()
    
Example: 

```java

DevicePermissionHelper devicePermissionHelper = new DevicePermissionHelper(activity); 

if (devicePermissionHelper.hasPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

    // Do your stuff, Permission already granted.

}else{
     devicePermissionHelper.requestToGrantPermission(new DevicePermissionHelper
                    .PermissionGrantListener() {
                @Override
                public void onPermissionGranted() {
                    // Do Your stuff, permission granted by user.
                }

                @Override
                public void onPermissionDenied() {
                   // Oops ! User denied to grant you permission.
                }

                @Override
                public void onPermissionRationale() {
                    // Oh ! User trying to use feature without granting permission.
                    // Tell him to grant permission to use these feature.
                }
            }, Manifest.permission.WRITE_EXTERNAL_STORAGE);
}

```
