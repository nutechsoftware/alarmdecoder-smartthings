This repository provides support for the AlarmDecoder webapp inside of the SmartThings home automation platform.  In the future it will also support the Z-Wave version of our product.

## Requirements

* AlarmDecoder webapp 0.6.2+
* SmartThings Hub

## Capabilities

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
8. Click Publish -> For Me
9. Select your Location on the right and press Set Location.
10. Click the Discover button.  You'll probably have to hit refresh a couple of times to get your device to show up.  If it doesn't show up make sure you're running an up-to-date version of the webapp.
11. Click Select Devices and select your AlarmDecoder.
12. Click Install
13. Open up the SmartThings app on your phone
14. Click My Home and select the Things tab
15. Select the AlarmDecoder
16. Click the gear icon and select Edit Device
17. Input the API key you generated from [https://alarmdecoder.local/api/](https://alarmdecoder.local/api/)
18. Input the alarm code you'd like to use to arm/disarm your panel.
19. Select your panel type.
20. Zone sensors may be configured to open and close themselves when a zone is faulted.  For example, specifying zone 7 for Zonetracker Sensor #1 would trip that sensor whenever zone 7 is faulted.

## Known Issues

* DSC: DSC support has not been tested.  DSC does not currently have the ability to PANIC.
* ADEMCO: Disarming the panel after an ALARM may be difficult due to requiring a double-disarm and the button states don't quite work that way.  Current workaround is to try to arm, which will let you disarm again.
* Due to platform limitations we're restricted to a one-minute refresh interval, which is a little slow.
