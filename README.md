# TOC

-   [Introduction](#introduction)
-   [Requirements](#requirements)
-   [Features](#features)
-   [Setup](#setup)  
    -- [Install device handlers (via github integration)](#install-device-handlers--via-github-integration-)  
    -- [Install SmartApp (via github integration)](#install-smartapp--via-github-integration-)  
    -- [Obtain an API key from the AlarmDecoder webapp](#obtain-an-api-key-from-the-alarmdecoder-webapp)  
    -- [Enabling SmartThings/Hubitat UPNP/SSDP Integration in the Webapp](#enabling-smartthings-hubitat-upnp-ssdp-integration-in-the-webapp)  
    -- [Configure AlarmDecoder device](#configure-alarmdecoder-device)  
    -- [Configure Contact ID switches](#configure-contact-id-switches)  
    -- [Configure RFX switches for Ademco 58XX sensors](#configure-rfx-switches-for-ademco-58xx-sensors)  
-   [Additional Info](#additional-info)  
    -- [Provided virtual devices](#provided-virtual-devices)  
    -- [Alarm Decoder Device Handlers](#alarm-decoder-device-handlers)  
    -- [Known Issues](#known-issues)  

# Introduction

This project provides support for the AlarmDecoder webapp UPNP/SSDP integration with SmartThings or Hubitat home automation platforms.

# Requirements

The AlarmDecoder webapp and hub use UDP broadcast packets for discovery. The hub and webapp must be located on the same layer 2 network.

-   AlarmDecoder webapp 0.8.3+   
-   SmartThings or Hubitat hub  

# Features

-   Arm, disarm, toggle chime, or panic your alarm system from within SmartThings/Hubitat.
-   Provides virtual sensors that can be tied to zones on your panel to allow automation based on zones faulting and restoring.
-   Provides virtual momentary switches with indicators for arming away, stay and toggling chime mode with each switch indicating the current state. Alexa<sup>[#warn](#warn)</sup> and other systems are able to activate these switches to allow a wide array of alarm panel control possibilities.
-   Provide virtual contact sensors for "Ready" and "Alarm Bell" indications on the alarm panel.
-   Provides a virtual "Smoke Alarm" that can be integrated with SHM or other systems to control state during a fire event such as turning all lights on.
-   Provides the ability to create virtual switches that can be tied to specific Contact ID report codes from the alarm panel. As an example a zone setup as a Carbon Monoxide alarm can be directly tied to a virtual switch that will OPEN in the event it triggers using Contact ID 162.
-   Provides the ability to create virtual switches that can be tied to specific 5800 RF devices. Typical use case is using a 5800micra sensor to monitor open/close of secure areas but not require panel zone programming to use. Just record its serial number and tie it to a virtual device using the serial number only.
-   Smart Home Monitor / Home Security Module integration.  
    One-way - Arm or disarm your panel when the Smart Home Monitor status is changed.  
    Two-way - Change Smart Home Monitor's status when your panel is armed or disarmed.  
-   Change virtual device handlers in the graph pages to change device capabilities and the system will adjust event types to match the device. Change a Zone Sensor to a Virtual Smoke Alarm and it will report 'clear' or 'detected'. This allows changing of device types to match what is needed for the task.  

<a name="warn">#warn</a>: Alexa and other systems may be confused by the device name to send ON or OFF action too. This can be done using fuzzy AI logic than can cause the system to send the event to a Panic or Alarm action button triggering an alarm. Thoughtful naming of devices as well as restricting access is advised before allowing these external systems to access the virtual buttons.</sup>  

# Setup

Navigate to <https://graph.api.smartthings.com> in your browser and login to your account.

## Install device handlers (via github integration)

-   Be sure a Hub is associated to the Location you are installing the service into.

1.  Click on **My Device Handlers**
2.  Click **Settings** (top of page)
3.  Click **Add New Repository** (bottom of dialog)
4.  Enter `nutechsoftware` as the **owner**
5.  Enter `alarmdecoder-smartthings` as the **name**
6.  Enter `master` as the **branch**
7.  Click **Save**
8.  Click **Update From Repo** (top of page)
9.  Check the boxes  
    -   [x] `AlarmDecoder network appliance`  
    -   [x] `AlarmDecoder virtual contact sensor`  
    -   [x] `AlarmDecoder virtual smoke alarm`  
    -   [x] `AlarmDecoder action button indicator`  
    -   [x] `AlarmDecoder status indicator`  
    -   [x] `AlarmDecoder virtual shock sensor`  
    -   [x] `AlarmDecoder virtual motion detector`  
    -   [x] `AlarmDecoder virtual carbon monoxide detector`  
10. Check **Publish** (bottom of dialog)
11. Click **Execute Update**

## Install SmartApp (via github integration)

1.  Click on **My SmartApps**
2.  Click **Update From Repo** (top of page)
3.  Check box for `alarmdecoder service`
4.  Check **Publish** (bottom of dialog)
5.  Click **Execute Update**
6.  Adjust **@Field** settings as needed at the top of the **AlarmDecoder service** code and as well as any other noted changes needed for Hubitat or SmartThings in the header and **Publish** if changes are made.
7.  Select the `alarmdecoder: AlarmDecoder service` smart app and then select your location on the right and press **Set Location**.  (Click the **Simulator** if you don't see these options)
8.  Click the **Discover** button.  You may have to hit refresh to get your device to show up.  If it doesn't show up make sure you're running an up-to-date version of the webapp and that it is on the same netowrk as your SmartThings HUB.
9.  Click **Select Devices** and select your AlarmDecoder.
10. Click **Install**

-   notes  
      a. This will generate new devices under **My Devices**  
      b. If you **Uninstall** from **AlarmDecoder service** screen it will attempt to automatically remove all sub devices if they are not in use by SHM or other rules.  
      c. You can remove blocking child items from the **My Devices** -> **Show Device** screen by selecting the **In Use By** item and deleting it.  

## Obtain an API key from the AlarmDecoder webapp

1.  Navigate to the API section of webapp on your local network: (<https://alarmdecoder.local/api/>)
2.  Click **Manage API keys**
3.  Click **Generate** for the desired webapp user (eg. `admin`)

## Enabling SmartThings/Hubitat UPNP/SSDP Integration in the Webapp

1.  Log into your AlarmDecoder webapp.
2.  Click Settings
3.  Click Notifications
4.  Click the New Notification button
5.  Set the Notification Type to 'UPNP Push'
6.  Enter a description ex 'UPNP PUSH'
7.  Press Next
8.  Press Save

-   notes  
      a.  If the AlarmDecoder Web App restarts it will loose subscriptions. It may take 5 minutes to restore PUSH notification.  
      b.  Updating the **AlarmDecoder UI** device settings on the phone app or web-based IDE will force a new subscription.  

## Configure AlarmDecoder device

Using the SmartThings app **on your phone**
1. Open up the SmartThings app **on your phone**  
2. Tap **My Home** and select the **Things** tab  
3. Select the **AlarmDecoder UI** device  
4. Tap the gear icon to edit the device  
5. Enter the API key you generated from the AlarmDecoder webapp  
6. Enter the alarm code you'd like to use to arm/disarm your panel.  
7. Select your panel type.  
8. Zone sensors may be configured to open and close themselves when a zone is faulted.  For example, specifying zone 7 for Zonetracker Sensor #1 would trip that sensor whenever zone 7 is faulted.  
9. Use **graph.api.smartthings.com** and modify the device type in the device editor. If the application needs the device to be a Smoke Alarm then change its device type to **AlarmDecoder virtual smoke alarm** in the device editor.  
10. Using the devices preferences(gear) update the zone numbers and invert option for the **Zone Sensors**.  

Using **graph.api.smartthings.com**
1. Login to your SmartThings graph web-based IDE.  
2. Select **My Devices**  
3. Select the  **AlarmDecoder(AD2)** device for your HUBs location.  
4. Click Preferences(**edit**) link.  
5. Enter the Rest API key you generated from the AlarmDecoder webapp  
6. Enter the alarm code you'd like to use to arm/disarm your panel.  
7. In the Panel Type - Type of panel enter **ADEMCO** or **DSC** depending on the panel type.  
8. Change the **Device Type** of the new **Zone Sensors** as desired by editing the device. If the application needs the device to be a Smoke Alarm then change its device type to **AlarmDecoder virtual smoke alarm** in the device editor.  
9. Using the preferences page update the zone numbers and invert option for the **Zone Sensors**.

## Configure Contact ID switches

**CID** or **Contact ID** is a prevalent and respected format for communications between alarms and the systems at alarm monitoring agencies that they report to.

If the Ademco panel has an existing Internet or cellular communicator then the AD2 is able to capture these messages and PUSH them to the home automation system as virtual switches. For DSC Power Series panels this feature is a Zero configuration feature(it just works). With Ademco panels a communicator is required or you can enable LRR messaging in your panel programming and enable LRR Emulation on the AlarmDecoder. In both panels it may be necessary to Enable / Disable reporting for some types of events.

1.  Open up the SmartThings app **on your phone**
2.  Tap **My Home** and select the **Things** tab
3.  Select the **AlarmDecoder(AD2)** device
4.  Select the **SmartApps** tab
5.  Select the **AlarmDecoder service**
6.  Select **Contact ID device management**
7.  Select **Add new CID virtual switch**
8.  Select the CID number pattern  
    -- 000 - Other / Custom  
    -- 100-102 - ALL Medical alarms  
    -- 110-118 - ALL Fire alarms  
    -- 120-126 - ALL Panic alarms  
    -- 130-139 - ALL Burglar alarms  
    -- 140-149 - ALL General alarms  
    -- 150-169 - ALL 24 HOUR AUX alarms  
    -- 154 - Water Leakage  
    -- 158 - High Temp  
    -- 162 - Carbon Monoxide Detected  
    -- 301 - AC Loss  
    -- 3?? - All System Troubles  
    -- 401 - Arm AWAY OPEN/CLOSE  
    -- 441 - Arm STAY OPEN/CLOSE  
    -- 4[0,4]1 - Arm Stay or Away OPEN/CLOSE  
9.  Enter the User or ZONE to match or ??? to match all.
10. Enter the partition to match 0(system), 1, 2 or ? to match all.
11. select **Add new CID virtual switch**
12. The switch will be created and you can see it under **My Devices**
13. Change the **Device Type** of the new **CID virtual switch** as desired by editing the device. If the application needs the device to be a Smoke Alarm then change its device type to **AlarmDecoder virtual smoke alarm** in the device editor.

## Configure RFX switches for Ademco 58XX sensors

All 5800 sensors within range of the 5800 receiver are able to be monitored for events.  All that is needed is the serial number. It is not necessary to program the 5800 device into the panel for this to work.

1.  Open up the SmartThings app **on your phone**
2.  Tap **My Home** and select the **Things** tab
3.  Select the **AlarmDecoder(AD2)** device
4.  Select the **SmartApps** tab
5.  Select the **AlarmDecoder service**
6.  Select **RFX device management**
7.  Select **Add new RFX virtual switch**
8.  Enter the 7 digit serial number including leading 0's ex 0123020
9.  Enter **?**, **0** or **1** for each loop or attribute to watch for events. ? will ignore the attribute and a value will match for it.
10. select **Add new RFX switch**
11. The switch will be created and you can see it under **My Devices**
12. Change the **Device Type** of the new **RFX virtual switch** as desired by editing the device. If the application needs the device to be a Smoke Alarm then change its device type to **AlarmDecoder virtual smoke alarm** in the device editor.

# Additional Info

## Provided virtual devices

  -   AlarmDecoder UI  
     Description: Main service device provides a simple user interface to manage the alarm.  
  -   Security<sup>[#1](#vdevicenames)</sup> Alarm Bell  
     Network Mask: \*:alarmBell  
     Description: An indicator to show the panel bell state and button to clear.  
     Default Handler:  **AlarmDecoder action button indicator**<sup>[#5](#flexible_handlers)</sup>  
     -- capabilities [Momentary, Switch]  
     -- actions [push: manually set state to off and clear]  
     -- states [on(Alarming), off]  
     -- preferences \[ invert:[true, false], zone: N/A]  
  -   Security<sup>[#1](#vdevicenames)</sup> Alarm Bell Status  
     Network Mask: \*:alarmBellStatus  
     Description: An indicator to show the panel bell state.  
     Default Handler: **AlarmDecoder status indicator**<sup>[#5](#flexible_handlers)</sup>  
     -- capabilities  [Contact sensor]  
     -- states [open(Alarming), close]  
     -- preferences \[ invert:[true, false], zone: N/A]  
  -   Security<sup>[#1](#vdevicenames)</sup> Chime  
     Network Mask: \*:chimeMode  
     Description: indicator to show the Chime state and button to toggle.  
     Default Handler:  **AlarmDecoder action button indicator**<sup>[#5](#flexible_handlers)</sup>  
      -- capabilities [Momentary, Switch]  
      -- actions [push: toggle chime mode]  
      -- states [on(Enabled), off]  
      -- preferences \[ invert:[true, false], zone: N/A]  
  -   Security<sup>[#1](#vdevicenames)</sup> Chime Status  
    Network Mask: \*:chimeModeStatus  
    Description: An indicator to show the chime state.  
    Default Handler: **AlarmDecoder status indicator**<sup>[#5](#flexible_handlers)</sup>  
     -- capabilities  [Contact sensor]  
     -- states [open(Enabled), close]  
     -- preferences \[ invert:[true, false], zone: N/A]  
  -   Security<sup>[#1](#vdevicenames)</sup> Ready Status  
    Network Mask: \*:readyStatus  
    Description: An indicator to show the panel ready to arm state.  
    Default Handler: **AlarmDecoder status indicator**<sup>[#5](#flexible_handlers)</sup>  
     -- capabilities  [Contact sensor]  
     -- states [open(READY), close]  
     -- preferences \[ invert:[true, false], zone: N/A]  
  -   Security<sup>[#1](#vdevicenames)</sup> Bypass Status  
    Network Mask: \*:bypassStatus  
    Description: An indicator to show if the panel has a bypassed zone.  
    Default Handler: **AlarmDecoder status indicator**<sup>[#5](#flexible_handlers)</sup>  
     -- capabilities  [Contact sensor]  
     -- states [open(Zone(s) bypassed), close]  
     -- preferences \[ invert:[true, false], zone: N/A]  
  -   Security<sup>[#1](#vdevicenames)</sup> Entry Delay Off state  
    Network Mask: \*:entryDelayOffStatus  
    Description: An indicator to show if the panel has exit delay off set.  
    Default Handler: **AlarmDecoder status indicator**<sup>[#5](#flexible_handlers)</sup>  
     -- capabilities  [Contact sensor]  
     -- states [open(Exit Delay OFF), close]  
     -- preferences \[ invert:[true, false], zone: N/A]  
  -   Security<sup>[#1](#vdevicenames)</sup> Perimeter Only state  
    Network Mask: \*:perimeterOnlyStatus  
    Description: An indicator to show if the panel has perimeter only set.  
    Default Handler: **AlarmDecoder status indicator**<sup>[#5](#flexible_handlers)</sup>  
     -- capabilities  [Contact sensor]  
     -- states [open(Perimeter Only ON), close]  
     -- preferences \[ invert:[true, false], zone: N/A]  
  -   Security<sup>[#1](#vdevicenames)</sup> Smoke Alarm  
    Network Mask: \*:smokeAlarm  
    Description: An indicator to show the panel fire state.  
    Default Handler: **AlarmDecoder virtual smoke alarm**<sup>[#5](#flexible_handlers)</sup>  
     -- capabilities  [Smoke Detector]  
     -- states [clear, detected]  
     -- preferences \[ invert:[true, false], zone: N/A]  
  -   Security<sup>[#1](#vdevicenames)</sup> Disarm  
    Network Mask: \*:disarm  
    Description: indicator to show the arm state and button to disarm.  
    Default Handler:  **AlarmDecoder action button indicator**<sup>[#5](#flexible_handlers)</sup>  
     -- capabilities [Momentary, Switch]  
     -- actions [push: disarm panel]  
     -- states [on(Armed), off]  
     -- preferences \[ invert:[true, false], zone: N/A]  
  -   Security<sup>[#1](#vdevicenames)</sup> Stay  
    Network Mask: \*:armStay  
    Description: indicator to show the arm stay state and button to arm stay.  
    Default Handler:  **AlarmDecoder action button indicator**<sup>[#5](#flexible_handlers)</sup>  
     -- capabilities [Momentary, Switch]  
     -- actions [push: arm stay]  
     -- states [on(Armed Stay), off]  
     -- preferences \[ invert:[true, false], zone: N/A]  
  -   Security<sup>[#1](#vdevicenames)</sup> Stay Status  
    Network Mask: \*:armStayStatus  
    Description: An indicator to show if the panel is armed in stay mode.  
    Default Handler: **AlarmDecoder status indicator**<sup>[#5](#flexible_handlers)</sup>  
     -- capabilities  [Contact sensor]  
     -- states [open(Armed Stay), close]  
     -- preferences \[ invert:[true, false], zone: N/A]  
  -   Security<sup>[#1](#vdevicenames)</sup> Away  
    Network Mask: \*:armAway  
    Description: indicator to show the arm away state and button to arm away.  
    Default Handler:  **AlarmDecoder action button indicator**<sup>[#5](#flexible_handlers)</sup>  
     -- capabilities [Momentary, Switch]  
     -- actions [push: arm away]  
     -- states [on(Armed Away), off]  
     -- preferences \[ invert:[true, false], zone: N/A]  
  -   Security<sup>[#1](#vdevicenames)</sup> Away Status  
    Network Mask: \*:armAwayStatus  
    Description: An indicator to show if the panel is armed in stay mode.  
    Default Handler: **AlarmDecoder status indicator**<sup>[#5](#flexible_handlers)</sup>  
     -- capabilities  [Contact sensor]  
     -- states [open(Armed Away), close]  
     -- preferences \[ invert:[true, false], zone: N/A]  
  -   Security<sup>[#1](#vdevicenames)</sup> Exit  
    Network Mask: \*:exit  
    Description: indicator to show the exit state.  
    Default Handler:  **AlarmDecoder action button indicator**<sup>[#5](#flexible_handlers)</sup>  
     -- capabilities [Momentary, Switch]  
     -- actions [push: request exit]  
     -- states [on(Exit now active), off]  
     -- preferences \[ invert:[true, false], zone: N/A]  
  -   Security<sup>[#1](#vdevicenames)</sup> Exit Status  
    Network Mask: \*:exitStatus  
    Description: An indicator to show if the panel exit mode is active.  
    Default Handler: **AlarmDecoder status indicator**<sup>[#5](#flexible_handlers)</sup>  
     -- capabilities  [Contact sensor]  
     -- states [open(Exit now active), close]  
     -- preferences \[ invert:[true, false], zone: N/A]  
  -   Security<sup>[#1](#vdevicenames)</sup> Panic Alarm  
    Network Mask: \*:alarmPanic  
    Description: Button to send Panic Alarm signal to the panel.  
    Default Handler:  **AlarmDecoder action button indicator**<sup>[#5](#flexible_handlers)</sup>  
     -- capabilities [Momentary, Switch]  
     -- actions [push: request panic]  
     -- states N/A<sup>[#1](#notsupported)</sup>
     \-- preferences \[ invert:[true, false], zone: N/A]  
  -   Security<sup>[#1](#vdevicenames)</sup> AUX(Medical) Alarm  
    Network Mask: \*:alarmAUX  
    Description: Button to send AUX(Medical) Alarm signal to the panel.  
    Default Handler:  **AlarmDecoder action button indicator**<sup>[#5](#flexible_handlers)</sup>  
     -- capabilities [Momentary, Switch]  
     -- actions [push: request aux alarm]  
     -- states N/A<sup>[#1](#notsupported)</sup>
     \-- preferences \[ invert:[true, false], zone: N/A]  
  -   Security<sup>[#1](#vdevicenames)</sup> Fire Alarm  
    Network Mask: \*:alarmFire  
    Description: An indicator to show the panel fire state and button to clear.  
    Default Handler:  **AlarmDecoder action button indicator**<sup>[#5](#flexible_handlers)</sup>  
     -- capabilities [Momentary, Switch]  
     -- actions [push: manually set state to off and clear]  
     -- states [on(Alarming), off]  
     -- preferences \[ invert:[true, false], zone: N/A]  
  -   Security<sup>[#1](#vdevicenames)</sup> Fire Alarm Status  
    Network Mask: \*:alarmFireStatus  
    Description: An indicator to show the panel fire alarm state.  
    Default Handler: **AlarmDecoder status indicator**<sup>[#5](#flexible_handlers)</sup>  
     -- capabilities  [Contact sensor]  
     -- states [open(Alarming), close]  
     -- preferences \[ invert:[true, false], zone: N/A]  
  -   Security<sup>[#1](#vdevicenames)</sup> Zone Sensor #N<sup>[#4](#zonenumbers)</sup>  
    Network Mask: \*:switch[#N]  
    Description: An indicator to show the zone state.  
    Default Handler: **AlarmDecoder virtual contact sensor**<sup>[#5](#flexible_handlers)</sup>  
     -- capabilities  [Contact sensor]  
     -- states [open(Alarming), close]  
     -- preferences \[ invert:[true, false], zone: Numeric zone associated with this device.]  
  -   CID-**_AAA_**-**_B_**-**_CCC_**<sup>[#2](#cidmask)</sup>  
    Network Mask: \*:CID-AAAA-B-CCC  
    Description: Indicates the state of the given Contact ID report state.  
    Default Handler: **AlarmDecoder action button indicator**<sup>[#5](#flexible_handlers)</sup>  
     -- capabilities  [Contact sensor]  
     -- actions [push: manually set state to off and clear]  
     -- states [on(Active), off]  
     -- preferences \[ invert:[true, false], zone: N/A]  
  -   RFX-AAAAAA-B-C-D-E-F-G<sup>[#3](#rfxmask)</sup>
    Network Mask: \*:RFX-AAAAAAA-B-C-D-E-F-G  
    Description: Indicates the state of the given RFX sensor.  
    Default Handler: **AlarmDecoder action button indicator**<sup>[#5](#flexible_handlers)</sup>  
     -- capabilities  [Contact sensor]  
     -- actions [push: manually set state to off and clear]  
     -- states [on(Active), off]  
     -- preferences \[ invert:[true, false], zone: N/A]  

<a name="vdevicenames">#1</a>: The device name prefix is configurable during install by changing @Field lname at the top of the **_AlarmDecoder Service_** code. The name can be changed after installing but the address must not be modified.  

<a name="cidmask">#2</a>: **_AAA_** is the Contact ID number **_B_** is the partition and **_CCC_** is the zone or user code to match with '???' matching all. Ex. CID-401-012 will monitor user 012 arming/disarming. Supports regex in the deviceNetworkId to allow to create devices that can trigger on multiple CID messages such as **_"CID-4[0,4]]1-1-???"_** will monitor all users for arming/disarming away or stay on partition 1.  

<a name="rfxmask">#3</a>:AAAAAA is the RF Serial Number B is battery status, C is supervisor event(ignore with ?), D is loop0(ignore with ?), E is loop1(ignore with ?), F is loop2(ignore with ?) and E is loop3(ignore with ?). Ex. RFX-123456-?-?-1-?-?-? will monitor 5800 RF sensor with serial number 123456 and loop1 for changes.  

<a name="zonenumbers">#4</a>: The number assigned initially to each device zone name is sequential and arbitrary. The actual zone tracked for each device is configured in the **AlarmDecoder UI** device settings page.  So 'Security Zone Sensor #1' could actually be zone 20.  Rename these as needed.

<a name="flexible_handlers">#5</a>: Each AlarmDecoder Virtual device receives a default type when it is created. This Device Type or Device Handler can be changed using the device editor. [Several example device types](#devicetypes) are provided and more can be created using the examples as reference.  

<a name="devicetypes"></a>

## Alarm Decoder Device Handlers

The Device Type or Device Handler for a given virtual device can be changed at any time. If the application requires a Smoke Detector and does not support Contact Sensors simply change the Type in the device editor. AlarmDecoder virtual devices will receive a translated message from on/off to a message appropriate to the devices type and capabilities.  

-   AlarmDecoder virtual contact sensor  
    capabilities [Contact Sensor]  
-   AlarmDecoder action button indicator  
    capabilities [Momentary, Switch]  
-   AlarmDecoder status indicator  
    capabilities [Contact Sensor]  
-   AlarmDecoder virtual carbon monoxide detector  
    capabilities [Carbon Monoxide Detector]  
-   AlarmDecoder virtual shock sensor  
    capabilities [Shock Sensor]  
-   AlarmDecoder virtual motion sensor  
    capabilities [Motion Sensor]  
-   AlarmDecoder virtual smoke alarm  
    capabilities [Smoke Detector]

## Known Issues

-   DSC:  
    a. Extra zones will show up in the zone list.  
    b. Arming STAY shows AWAY until after EXIT state.  
-   ADEMCO:  
    a. As with a regular keypad it is necessary to disarm a second time after an alarm to restore to Ready state. The Disarm button stays enabled when the panel is Not Ready.  
    b. Fire state take 30 seconds to clear after being cleared on the panel.  
-   All panels:  
    a. not updating when the panel arms disarms etc.  
    Subscription may have been lost during restart of web app.  
    The AlarmDecoder SmartThings device will renew its subscription every 5 minutes.  
    To force a renwal update the Settings such as the API KEY in the App or Device graph page.  
