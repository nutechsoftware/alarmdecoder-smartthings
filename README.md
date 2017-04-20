This repository provides support for the AlarmDecoder webapp inside of the SmartThings home automation platform.

## Requirements

* AlarmDecoder webapp 0.7.6+
* SmartThings Hub

## Features

* Arm, disarm, or panic your alarm system from within SmartThings.
* Provides virtual sensors that can be married to zones on your panel to allow automation based on zones faulting and restoring.
* Smart Home Monitor integration
** One-way - Arm or disarm your panel when the Smart Home Monitor status is changed.
** Two-way - Change Smart Home Monitor's status when your panel is armed or disarmed.

## DeviceType Capabilities

* Switch - Used to represent the panel in an ARMED STAY state.  Triggering switch.on() will arm the panel using the stored user code.  switch.off() will disarm the panel.  This is input only at the moment.
* Lock - Used to represent the panel in an ARMED AWAY state.  Triggering lock.lock() will arm the panel and lock.unlock() will disarm the panel.  This is the primary capability for watching for armed/disarmed events.
* Alarm - Used to indicate an alarming state and also may trigger a panic on the panel with alarm.both().
* SmokeDetector - Used to indicate if a FIRE was detected on the panel.
* Virtual Zone Sensors - The service manager smartapp creates 8 virtual contact sensors which are used to provide triggers when configured zones are tripped on the alarm panel.

## Setup

1. Navigate to [https://graph.api.smartthings.com](https://graph.api.smartthings.com) in your browser and login to your account.
2. Click on My Device Handlers
  1. Click on Create New Device Handler
  2. Click the From Code tab
  3. Paste the contents of virtual_contact_sensor.groovy into the box and press Create.
  4. Click Publish -> For Me
3. Repeat the process with device_type.groovy.
4. Click on My SmartApps
5. Click New SmartApp
6. Click the From Code tab
7. Paste the contents of service_manager.groovy into the box and press Create.
8. Click the 'App Settings' button at the top.
9. Scroll to the bottom of the page and click 'OAuth'
10. Click 'Enable OAuth in Smart App'
11. Click Publish -> For Me
12. Select your Location on the right and press Set Location.  (Click the Simulator button if you don't see these options)
13. Click the Discover button.  You'll probably have to hit refresh a couple of times to get your device to show up.  If it doesn't show up make sure you're running an up-to-date version of the webapp.
14. Click Select Devices and select your AlarmDecoder.
15. Change other options on this screen if needed.
16. Click Install
17. After install there will be two boxes at the bottom titled 'API Token' and 'API Endpoint'.  Record these so you can input them into the webapp for full integration.
18. Open up the SmartThings app on your phone
19. Click My Home and select the Things tab
20. Select the AlarmDecoder
21. Click the gear icon and select Edit Device
22. Input the API key you generated from [https://alarmdecoder.local/api/](https://alarmdecoder.local/api/)
23. Input the alarm code you'd like to use to arm/disarm your panel.
24. Select your panel type.
25. Zone sensors may be configured to open and close themselves when a zone is faulted.  For example, specifying zone 7 for Zonetracker Sensor #1 would trip that sensor whenever zone 7 is faulted.

## Enabling SmartThings Integration in the Webapp
1. Log into your AlarmDecoder webapp.
2. Click Settings
3. Click Notifications
4. Click the New Notification button
5. Set the Notification Type to 'SmartThings Integration'
6. Enter a description
7. Click SmartThings settings
8. Enter the 'API Endpoint' you recorded during install into the 'URL' box.
9. Enter the 'API Token' you recorded during install into the 'Token' box.
10. Press Next
11. Press Save

## Known Issues

* DSC: Extra zones will show up in the zone list.
* ADEMCO: Disarming the panel after an ALARM may be difficult due to requiring a double-disarm and the button states don't quite work that way.  Current workaround is to try to arm, which will let you disarm again.
