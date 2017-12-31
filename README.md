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

Navigate to [https://graph.api.smartthings.com](https://graph.api.smartthings.com) in your browser and login to your account.

### Install device handlers (via github integration)
1. Click on **My Device Handlers**
2. Click **Settings** (top of page)
3. Click **Add New Repository** (bottom of dialog)
4. Enter `nutechsoftware` as the **owner**
5. Enter `alarmdecoder-smartthings` as the **name**
6. Enter `master` as the **branch**
7. Click **Save**
8. Click **Update From Repo** (top of page)
9. Check the boxes `network appliance` and `virtual contact sensor`
10. Check **Publish** (bottom of dialog)
11. Click **Execute Update**
  
### Install SmartApp (via github integration)
1. Click on **My SmartApps**
2. Click **Update From Repo** (top of page)
3. Check box for `alarmdecoder service`
4. Check **Publish** (bottom of dialog)
5. Click **Execute Update**
6. Click **alarmdecoder : alarmdecoder service** in the list of installed apps
7. Click the **App Settings** button at the top.
8. Scroll to the bottom of the page and click **OAuth**
9. Click **Enable OAuth in Smart App**
10. Click **Update** (bottom of page)
11 Select your location on the right and press **Set Location**.  (Click the **Simulator** if you don't see these options)
12. Click the **Discover** button.  You'll probably have to hit refresh a couple of times to get your device to show up.  If it doesn't show up make sure you're running an up-to-date version of the webapp.
13. Click **Select Devices** and select your AlarmDecoder.
14. Click **Install**
15. After install there will be two boxes at the bottom titled 'API Token' and 'API Endpoint'.  Record these so you can input them into the webapp for full integration.

### Configure AlarmDecoder device
1. Open up the SmartThings app **on your phone**
2. Tap **My Home** and select the **Things** tab
3. Select the **AlarmDecoder** device
4. Tap the gear icon and select **Edit Device**
5. Enter the API key you generated from [https://alarmdecoder.local/api/](https://alarmdecoder.local/api/)
6. Enter the alarm code you'd like to use to arm/disarm your panel.
7. Select your panel type.
8. Zone sensors may be configured to open and close themselves when a zone is faulted.  For example, specifying zone 7 for Zonetracker Sensor #1 would trip that sensor whenever zone 7 is faulted.

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
