This repository provides support for the AlarmDecoder webapp inside of the SmartThings home automation platform.

## Requirements

* AlarmDecoder webapp 0.8.2+
* SmartThings Hub

## Features

* Arm, disarm, or panic your alarm system from within SmartThings.
* Provides virtual sensors that can be married to zones on your panel to allow automation based on zones faulting and restoring.
* Provides virtual momentary switches with indicators for arming away, stay and toggling chime mode with each switch indicating the current state. Alexa and other systems are able to activate these switches to allow a wide array of alarm panel control possibilities.
* Provide virtual contact sensors for "Ready" and "Alarm Bell" indications on the alarm panel.
* Provides a virtual "AD2 Smoke Alarm" that can be integrated with SHM or other systems to control state during a fire event such as turning all lights on.
* Provides the ability to create virtual switches that can be tied to specific Contact ID report codes from the alarm panel. As an example a zone setup as a Carbon Monoxide alarm can be directly tied to a virtual switch that will OPEN in the event it triggers using Contact ID 162.
* Smart Home Monitor integration.  
One-way - Arm or disarm your panel when the Smart Home Monitor status is changed.  
Two-way - Change Smart Home Monitor's status when your panel is armed or disarmed.  

## Virtual devices

* AlarmDecoder(AD2)  
Description: Main service device provides a simple user interface to manage the alarm.  

* AD2 Alarm Bell  
Capabilities:  Contact sensor  
Description: An indicator to show the panel bell state  
States: close = off, open = sounding  

* AD2 Chime  
Capabilities:  Momentary  
Description: indicator to show the Chime state.
-- Action **'push'** will toggle the chime state.  
States: [on, off]  

* AD2 Ready  
Capabilities: Contact Sensor  
Description: An indicator to show the panel ready to arm state.  
States: [open, close = Ready]  

* AD2 Bypass  
Capabilities: Contact Sensor  
Description: An indicator to show if the panel has a bypassed zone.  
States: [open = Zone(s) Bypassed, close]  

* AD2 Smoke Alarm  
Capabilities: smokeDetector  
Description: An indicator to show the panel fire state.  
States: [clear, detected]  

* AD2 Stay  
Capabilities:  Momentary  
Description: indicator to show the arm Stay state
-- Action **'push'** will send the arm Stay command to the panel  
States: [on, off]  

* AD2 Away  
Capabilities:  Momentary  
Description: indicator to show the arm Away state
-- Action **'push'** will send the arm Away command to the panel  
States: [on, off]  

* AD2 Panic Alarm  
Capabilities:  Momentary  
Description: Action **'push'** will send the Panic Alarm command to the panel  
States: No indication of alarm type  

* AD2 Aux Alarm  
Capabilities:  Momentary  
Description: Action **'push'** will send the AUX Alarm command to the panel  
States: No indication of alarm type  

* AD2 Fire Alarm  
Capabilities:  Momentary  
Description: Action **'push'** will send the Fire Alarm command to the panel  
States: No indication of alarm type  

* AD2 Zone Sensor #N  
Capabilities: Contact Sensor  
Description: An indicator to show the zone state  
States: [open , close] * reversible in parent device settings  

* AD2 CID-***AAA***-***B***-***CCC***  
Capabilities: Momentary  
Description: Indicates the state of the given Contact ID report state. The action **'push'** will restore to closed state. ***AAA*** is the Contact ID number ***B*** is the partition and ***CCC*** is the zone or user code to match with '...' matching all. Ex. CID-401-012 will monitor user 012 arming/disarming. Supports regex in the deviceNetworkId to allow to create devices that can trigger on multiple CID messages such as ***"CID-4[0,4]]1-1-..."*** will monitor all users for arming/disarming away or stay on partition 1.  
States: [on, off]  

## Setup

Navigate to [https://graph.api.smartthings.com](https://graph.api.smartthings.com) in your browser and login to your account.

### Install device handlers (via github integration)
1. Click on **My Device Handlers**
2. Click **Settings** (top of page)
3. Click **Add New Repository** (bottom of dialog)
4. Enter `yhax` as the **owner**
5. Enter `alarmdecoder-smartthings` as the **name**
6. Enter `zone.updates` as the **branch**
7. Click **Save**
8. Click **Update From Repo** (top of page)
9. Check the boxes 
   * `AlarmDecoder network appliance`
   * `AlarmDecoder virtual contact sensor`
   * `AlarmDecoder virtual smoke alarm`
   * `AlarmDecoder action button indicator`
   * `AlarmDecoder status indicator`
10. Check **Publish** (bottom of dialog)
11. Click **Execute Update**

### Generate device handler file from the AlarmDecoder webapp
1. Navigate to the advanced section of the webapp on your local network: ([https://alarmdecoder.local/settings/advanced/](https://alarmdecoder.local/settings/advanced/))
2. Click **Generate Device Handler** (bottom of page)
3. Open the file the generated file that was created in step 2 (alarmdecoder-network-appliance.groovy)
4. Copy the entire file
5. Navigate to [https://graph.api.smartthings.com](https://graph.api.smartthings.com) in your browser and login to your account.
6. Click on **My Device Handlers**
7. Click on **alarmdecoder : AlarmDecoder network appliance**
8. Remove all the code from the editor, and paste the code from step 4.
9. Click **Save**
10. Click **Publish** 

### Install SmartApp (via github integration)
1. Click on **My SmartApps**
2. Click **Update From Repo** (top of page)
3. Check box for `alarmdecoder service`
4. Check **Publish** (bottom of dialog)
5. Click **Execute Update**
6. Select the `alarmdecoder: AlarmDecoder service` smart app.
7. On line 39, `UPDATE_ME` with your AlarmDecoder API key, which you can find in the AlarmDecoder webapp - [https://alarmdecoder.local/api/keys](https://alarmdecoder.local/api/keys)) - If you don't already have one skip to the next section for details how to generate one, and then come back to this step.
8. Click **Save**
9. Click **Publish** 
10. Select your location on the right and press **Set Location**.  (Click the **Simulator** if you don't see these options)
11. Click the **Discover** button.  You may have to hit refresh to get your device to show up.  If it doesn't show up make sure you're running an up-to-date version of the webapp and that it is on the same netowrk as your SmartThings HUB.
12. Click **Select Devices** and select your AlarmDecoder.
13. Click **Install**
    * Notes
        1. This will generate new devices under **My Devices**
        2. If you **Uninstall** from **AlarmDecoder service** screen it will attempt to automatically remove all sub devices if they are not in use by SHM or other rules.
        3. You can remove blocking child items from the **My Devices** -> **Show Device** screen by selecting the **In Use By** item and deleting it.

### Obtain an API key from the AlarmDecoder webapp
1. Navigate to the API section of webapp on your local network: ([https://alarmdecoder.local/api/](https://alarmdecoder.local/api/))
2. Click **Manage API keys**
3. Click **Generate** for the desired webapp user (eg. `admin`)

### Configure AlarmDecoder device
* Using the SmartThings app **on your phone**
    1. Open up the SmartThings app **on your phone**
    2. Tap **My Home** and select the **Things** tab
    3. Select the **AlarmDecoder** device
    4. Tap the gear icon to edit the device
    5. Enter the API key you generated from the AlarmDecoder webapp
    6. Enter the alarm code you'd like to use to arm/disarm your panel.
    7. Select your panel type.
    8. Zone sensors may be configured to open and close themselves when a zone is faulted.  For example, specifying zone 7 for Zonetracker Sensor #1 would trip that sensor whenever zone 7 is faulted.
* Using **graph.api.smartthings.com**
    1. Login to your SmartThings graph web-based IDE.
    2. Select **My Devices**
    3. Select the  **AlarmDecoder(AD2)** device for your HUBs location.
    4. Click Preferences(**edit**) link.
    5. Enter the Rest API key you generated from the AlarmDecoder webapp
    6. Enter the alarm code you'd like to use to arm/disarm your panel.
    7. In the Panel Type - Type of panel enter **ADEMCO** or **DSC** depending on the panel type.

### Configure Contact ID switches
* Using the SmartThings app **on your phone**
    1. Open up the SmartThings app **on your phone**
    2. Tap **My Home** and select the **Things** tab
    3. Select the **AlarmDecoder(AD2)** device
    4. Select the **SmartApps** tab
    5. Select the **AlarmDecoder service**
    6. Select **Contact ID device management**
    7. Select **Add new CID virtual switch**
    8. Select the CID number or select **000 - Other / Custom**
    9. select **Add new CID virtual switch**
    10. The switch will be created and you can see it under **My Devices**


## Enabling SmartThings Integration in the Webapp
1. Log into your AlarmDecoder webapp.
2. Click Settings
3. Click Notifications
4. Click the New Notification button
5. Set the Notification Type to 'UPNP Push'
6. Enter a description ex 'UPNP PUSH'
7. Press Next
8. Press Save
    * notes
        1. If the AlarmDecoder Web App restarts it will loose subscriptions. It may take 5 minutes to restore PUSH notification.
        2. Updating the **AlarmDecoder** device settings on the phone app or web-based IDE will force a new subscription.

## Known Issues

* DSC: Extra zones will show up in the zone list.
* ADEMCO: As with a regular keypad it is necessary to disarm a second time after an alarm to restore to Ready state. The Disarm button stays enabled when the panel is Not Ready.
* Status is not updating when the panel arms disarms etc.
    * Subscription may have been lost during restart of web app.
    * The AlarmDecoder SmartThings device will renew its subscription every 5 minutes.
    * To force a renwal update the Settings such as the API KEY in the App or Device graph page.
