/**
 *  AlarmDecoder Service Manager
 *
 *  Copyright 2016-2019 Nu Tech Software Solutions, Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not
 *  use this file except in compliance with the License. You may obtain a copy
 *  of the License at:
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 *  WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 *  License for the specific language governing permissions and limitations
 *  under the License.
 *
 *
 * V1.0.0 - Scott Petersen - Initial design and release 2015/12/10 - 2017/04/20
 * V2.0.0 - Sean Mathews <coder@f34r.com> - 2018/05/21
 *    Changed to use UPNP Push API in AD2 web app.
 * V2.0.1 - Sean Mathews <coder@f34r.com> - 2018/05/21
 *    Adding CID device management support.
 * V2.0.2 - Sean Mathews <coder@f34r.com> - 2019/01/02
 *    Fixed app 20 second max timeout. AddZone is now async, added more zones.
 * V2.0.3 - Sean Mathews <coder@f34r.com> - 2019/01/11
 *    Improved/fixed issues with previous app 20 timeout after more testing.
 * V2.0.4 - Sean Mathews <coder@f34r.com> - 2019/02/19
 *    Support multiple instances of service by changing unique ID message
 *    filters by MAC.
 * V2.0.5 - Sean Mathews <coder@f34r.com> - 2019/03/06
 *    Add switch to create Disarm button.
 * V2.0.6 - Sean Mathews <coder@f34r.com> - 2019/03/28
 *    Compatibility between ST and HT. Still requires some manual code edits but
 *    it will be minimal.
 * V2.0.7 - Sean Mathews <coder@f34r.com> - 2019/05/05
 *    Add RFX virtual device management to track Ademco 5800 wireless or VPLEX
 *    sensors directly.
 * V2.0.8 - Sean Mathews <coder@f34r.com> - 2019/05/17
 *    Added Exit button. New flag from AD2 state to detect exit state.
 *    Added rebuild devices button.
 * V2.0.9 - Sean Mathews <coder@f34r.com> - 2019/05/17
 *    Split some devices from a single combined Momentary to a Momentary and a
 *    Contact for easy access by other systems.
 * V2.1.0 - Sean Mathews <coder@f34r.com> - 2019/08/02
 *    Modified virtual RFX devices to allow inverting signal and capabilities
 *    detection. Set the RFX device type to AlarmDecoder virtual smoke and it
 *    will report as a smoke detector. Modified sending of events creating a
 *    wrapper to invert and adjust to the device type.
 * V2.1.1 - Sean Mathews <coder@f34r.com> - 2019/09/21
 *    Add screen to re-link a local AlarmDecoder WEBAPP if the IP/MAC address
 *    change. Refactoring cleanup localization. Refactoring long lines.
 * V2.2.0 - Sean Mathews <coder@f34r.com> - 2019/09/20 - 2019/09/30
 *    A major re-factor and cleanup of the code. Improved UI. Ability to
 *    reconnect the AlarmDecoder if the IP or MAC change using the App UI.
 *    Refactor how zones are mapped to devices. Now each virtual device can
 *    be assigned a zone number in preferences.
 *
 */

/**
 * global support
 */
import groovy.transform.Field
@Field APPNAMESPACE = "alarmdecoder"
@Field SSDPTERM = "urn:schemas-upnp-org:device:AlarmDecoder:1"

/**
 * System Settings
 */
@Field NOCREATEDEV = false

/**
 * Install Notes:
 * Modify code in getHubAction below to adjust
 * between SmartThings and Hubitat
 */

/**
 * Device label name settings
 * To run more than once service load this code as a new SmartApp
 * and change the idname to something unique. For easy use with Echo
 * you can set the '''sname''' to something easy to say such as 'Security'
 * then you can say 'Computer - Security Arm Stay On'
 */
@Field lname = "AlarmDecoder"
@Field sname = "Security"
@Field guiname = "${lname} UI"
@Field idname = ""

/**
 * CID table
 * List of some of the CID #'s and descriptions.
 * 000 will trigger a manual input of the CID number.
 */
@Field cid_numbers = ["0": "000 - Other / Custom",
  "10?": "100-102 - ALL Medical alarms",
  "11?": "110-118 - ALL Fire alarms",
  "12?": "120-126 - ALL Panic alarms",
  "13?": "130-139 - ALL Burglar alarms",
  "14?": "140-149 - ALL General alarms",
  "1[5-6]?": "150-169 - ALL 24 HOUR AUX alarms",
  "154": "154 - Water Leakage",
  "158": "158 - High Temp",
  "162": "162 - Carbon Monoxide Detected",
  "301": "301 - AC Loss",
  "3??": "3?? - All System Troubles",
  "401": "401 - Arm AWAY OPEN/CLOSE",
  "441": "441 - Arm STAY OPEN/CLOSE",
  "4[0,4]1": "4[0,4]1 - Arm Stay or Away OPEN/CLOSE"
]

/**
 * Get the HubAction class specific to
 * the HUB type being used.
 *
 * NOTE:
 *   Remove comments on code for the HUB type being used.
 */
def getHubAction(action, method = null) {

  // SmartThings specific classes here
  // Comment out the next 2 lines if we are using Hubitat
  //if (!method) method = physicalgraph.device.Protocol.LAN
  //def ha = new physicalgraph.device.HubAction(action, method)

  // Hubitat specific classes here
  // Comment out the next line if we are using SmartThings
  if (!method) method = hubitat.device.Protocol.LAN
  def ha = new hubitat.device.HubAction(action, method)

  return ha
}

/**
 * Service definition and preferences
 */
definition(
  name: "AlarmDecoder service dev",
  namespace: APPNAMESPACE,
  author: "Nu Tech Software Solutions, Inc.",
  description: "AlarmDecoder (Service Manager)",
  category: "My Apps",
  iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
  iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
  iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
  singleInstance: true) {}

preferences {
  page(
    name: "page_main",
    install: false,
    uninstall: false)
  page(
    name: "page_discover",
    content: "page_discover",
    install: true,
    uninstall: false)
  page(
    name: "page_remove_all",
    content: "page_remove_all",
    install: false,
    uninstall: false)
  page(
    name: "page_rebuild_all",
    content: "page_rebuild_all",
    install: false,
    uninstall: false)
  page(name: "page_cid_management",
    content: "page_cid_management",
    install: false,
    uninstall: false)
  page(name: "page_add_new_cid",
    content: "page_add_new_cid",
    install: false,
    uninstall: false)
  page(
    name: "page_add_new_cid_confirm",
    content: "page_add_new_cid_confirm",
    install: false,
    uninstall: false)
  page(
    name: "page_remove_selected_cid",
    content: "page_remove_selected_cid",
    install: false,
    uninstall: false)
  page(
    name: "page_rfx_management",
    content: "page_rfx_management",
    install: false,
    uninstall: false,
    refreshInterval: 5)
  page(
    name: "page_add_new_rfx",
    content: "page_add_new_rfx",
    install: false,
    uninstall: false)
  page(
    name: "page_add_new_rfx_confirm",
    content: "page_add_new_rfx_confirm",
    install: false,
    uninstall: false)
  page(
    name: "page_remove_selected_rfx",
    content: "page_remove_selected_rfx",
    install: false,
    uninstall: false)
  page(
    name: "page_relink_update",
    content: "page_relink_update",
    install: false,
    uninstall: false)
  page(
    name: "page_select_device",
    content: "page_select_device",
    install: false,
    uninstall: false,
    refreshInterval: 5)
}

/*
 * Localization strings
 */
def getText(String name, Object... args) {
  def en_strings = [

    // misc or used multiple places
    "home": "home",
    "home_screen": "Press the < arrow above two times to return home.",
    "no_save_note": "Do not use the \"Save\" button on this page.",
    "save_note": "Press \"Save\" buttons on this page to install the service devices.",
	"done_note": "Press \"Done\" button on this page to install the service devices.",
    "input_selected_devices_title": "Select device (%s found).",
    "section_monitor_integration": "Monitor integration",
    "section_zone_sensor_settings": "Zone Sensors",
    "section_mon_integration": "Monitor Integration",
    "tap_here": "[ * TAP HERE * ]",
    "press_back_note": "Press the < arrow above to return to the previous page.",

    //  Main Page
    "page_main_title": "Setup And Management",
    "page_main_device_found": "${lname} service found.\nSelect from management options below.",
	"debug_logging": "Enable debug logging",
	"trace_logging": "Enable trace logging",

    // Discover/Install
    "page_discover_title": "Install Service",
    "page_discover_desc": "Tap to discover and install your ${lname} Appliance.",
    "page_discover_section_selected_device": "Selected device info: %s",
	"section_sensor_counts": "Sensor Configuration",
	"contact_sensor_count": "How many contact (window/door) sensors should be created?",
	"smoke_detector_count": "How many smoke detectors should be created?",
	"co_detector_count": "How many carbon monoxide detectors should be created?",
	"motion_sensor_count": "How many motion sensors should be created?",
	"shock_sensor_count": "How many shock (glass break) sensors should be created?",
	"create_disarm_switch": "Create a switch that can be used to disarm the alarm (note this can be a security risk if this is exposed to things like Google Home or Alexa)",

    // Service Settings
    "monIntegrationSHM": "Integrate with Smart Home Monitor?",
    "monIntegrationHSM": "Integrate with Hubitat Safety Monitor?",
    "monChangeStatusSHM": "Automatically change Smart Home Monitor status when armed or disarmed?",
    "monChangeStatusHSM": "Automatically change Hubitat Safety Monitor status when armed or disarmed?",
    "defaultSensorToClosed": "Default zone sensors to closed?",

    // CID Management
    "page_cid_management_title": "Contact ID Device Management",
    "page_cid_management_desc": "Tap to manage virtual devices.",
    "input_cid_devices_title": "Remove installed CID virutal devices.",
    "input_cid_devices_desc": "Tap to select.",

    //// Add new CID
    "page_add_new_cid_title": "Add New Contact ID Virtual Switch",
    "page_add_new_cid_desc": "Tap to add new CID switch",
    "section_build_cid": "CODE mask: %s",
    "input_cid_number_title": "Select the CID number for this device",
    "input_cid_number_desc": "Tap to select",
    "input_cid_number_raw_title": "Enter raw Contact ID CODE or simple regex pattern",
    "section_cid_value": "USER# or ZONE# mask : %s",
    "input_cid_value_title": "Zero padded 3 digit User# or Zone# or simple regex pattern ex. '001' or '???'",
    "section_cid_partition": "Partition mask: %s",
    "input_cid_partition_title": "Enter the partition. Use 0 for system and ? for any.",
    "section_cid_name": "Device Name",
    "input_cid_name_title": "Enter the new device name or blank for auto",
    "section_cid_label": "Device Label",
    "input_cid_label_title": "Enter the new device label or blank for auto",

    ////// Add new confirm
    "page_add_new_cid_confirm_title": "Add new CID switch : %s",
    "add_new_cid_confirm_info": "Attempted to add new CID device. This may fail if the device is in use. If it fails review the log. This screen is probably no longer valid so you may get errors on the app until you exit these pages. Press back to continue.",
    "href_add_new_cid_confirm_desc": "Tap to confirm and add",

    ////// Remove CID
    "page_remove_selected_cid_title": "Remove selected virtual CID devices",
    "page_remove_selected_cid_desc": "Tap to remove selected virtual CID device",
    "page_remove_selected_cid_info": "Attempted to remove selected devices. This may fail if the device is in use. If it fails review the log and manually remove all devices and remove the service from the location. Press back to continue.",

    // RFX Management
    "page_rfx_management_title": "RFX Device Management",
    "page_rfx_management_desc": "Tap to manage virtual devices.",
    "input_rfx_devices_title": "Selected RFX devices to remove.",
    "input_rfx_devices_desc": "Tap to select.",
    "page_remove_selected_rfx_title": "Remove selected RFX devices.",
    "page_add_new_rfx_title": "Add New RFX Virtual Device",
    "page_add_new_rfx_desc": "To add new RFX virtual device\n%s",
    "page_add_new_rfx_confirm": "Tap to confirm and add.",

    "page_remove_selected_rfx": "Remove Selected Virtual Device.",
    "section_build_rfx": "Build Device Name :",
    "input_rfx_name": "Enter the new device name or blank for auto.",
    "input_rfx_label": "Enter the new device label or blank for auto.",
    "input_rfx_sn": "Enter Serial # or simple REGEX pattern.",
    "input_rfx_supv": "Supervision bit: Enter 1 to watch or ? to ignore.",
    "input_rfx_bat": "Battery: Enter 1 to monitor or ? to ignore.",
    "input_rfx_loop0": "Loop 0: Enter 1 to monitor or ? to ignore.",
    "input_rfx_loop1": "Loop 1: Enter 1 to monitor or ? to ignore.",
    "input_rfx_loop2": "Loop 2: Enter 1 to monitor or ? to ignore.",
    "input_rfx_loop3": "Loop 3: Enter 1 to monitor or ? to ignore.",

    // Select Device discovery
    "page_select_device_title": "Select ${lname} Appliance",
    "page_select_device_desc": "To select the local ${lname} Appliance this service will connect to. Be sure the WEBAPP UPNP notification service is enabled.",

    // Update AD2 IP/MAC relink
    "info_confirm_relink_update": "To update the ${lname} service to link to the selected device at (%s).",
    "page_relink_update_title": "Update Service Settings",
    "page_relink_update_desc": "This page updates settings or re-link the ${lname} Appliance if the IP or MAC address change.\nTap to select.",
    "page_relink_section_active_device": "Linked device info: %s",
    "page_relink_section_selected_device": "Selected device info: %s",
    "info_relink_update_done": "Attempted to re-link the selected devices. This may fail. If it fails review the log and provide feedback.",


    // Rebuild All
    "page_rebuild_all_title": "Rebuild Virtual Devices",
    "page_rebuild_all_desc": "This page will find and repair missing virtual devices.\nTap to select.",
    "confirm_rebuild_all": "Tap to confirm and rebuild all.",
    "info_rebuild_all_done": "Rebuild done. Press back arrow to return to home screen.",
    "info_rebuild_all_confirm": "This will attempt to rebuild all child devices. Monitor the logs for any errors. Press back to return to the main page.",

    // Remove All
    "page_remove_all_title": "Uninstall ${lname} Service",
    "page_remove_all_desc": "This page is for removing and uninstalling the ${lname} service.\nTap to select.",
    "remove_all_href_confirm": "[ * TAP HERE * ]",
    "info_remove_all_done": "Removed all child devices. Press back to return to the main page.",
    "info_remove_all_confirm": "This will attempt to remove all child devices. This may fail if the device is in use. If it fails review the log and manually remove the usage.\nTap to confirm.",


    // End
    "": ""
  ]
  if (args) {
    try {
      return String.format(en_strings[name], args)
    } catch (Exception ex) {
      log.error("***SZT***:${name}")
      return "**SZT***:err:${name}"
    }
  } else {
    return en_strings[name]
  }
}

/**
 * Allow remote device to force the HUB to request an
 * update from the AlarmDecoder.
 *
 * Just a few steps :( but it works.
 * AD2 -> ST-CLOUD -> ST-HUB -> AD2 -> ST-HUB -> ST-CLOUD
 */
mappings {
  path("/update") {
    action: [
      GET: "webserviceUpdate"
    ]
  }
}


/*** Pages/UI ***/

/**
 * Misc helper sections
 */
def section_save_note() {
	if (isSmartThings()) {
		section(getText("save_note")) {}
	}
	else {
		section(getText("done_note")) {}
	}
}

def section_no_save_note() {
  if (isSmartThings()) {
	section(getText("no_save_note")) {}
  }
  else {
	section { paragraph "<script>\$('button[name=\"_action_previous\"]').hide()</script>" }
  }
}

def section_home() {
  section(getText("home")) {
    href(
      name: "href_home",
      title: getText("tap_here"),
      required: false,
      description: getText("home_screen"),
      page: "page_main"
    )
  }
}

def section_back_note() {
	if (isSmartThings()) {
		section(getText("press_back_note")) {}
	}
}

/**
 * The main service page
 */
def page_main() {

  // make sure we are listening to all network subscriptions
  initSubscriptions()

  // send out a UPNP broadcast discovery
  discover_alarmdecoder()

  // see if we are already installed
  def foundMsg = ""
  def children = getChildDevices()
  if (children) foundMsg = getText("page_main_device_found")

  dynamicPage(name: "page_main", title: getText("page_main_title")) {
    if (!children) {
      // Not installed show discovery page to complete the install.
      section("") {
        href(
          name: "href_discover",
          title: getText("page_discover_title"),
          required: false,
          description: getText("page_discover_desc"),
          page: "page_discover"
        )
      }
    } else {
      section(foundMsg) {
        href(
          name: "href_cid_management",
          title: getText("page_cid_management_title"),
          required: false,
          description: getText("page_cid_management_desc"),
          page: "page_cid_management"
        )
      }
      section("") {
        href(
          name: "href_rfx_management",
          title: getText("page_rfx_management_title"),
          required: false,
          description: getText("page_rfx_management_desc"),
          page: "page_rfx_management"
        )
      }
      section("") {
        href(
          name: "href_relink_update",
          title: getText("page_relink_update_title"),
          required: false,
          description: getText("page_relink_update_desc"),
          page: "page_relink_update"
        )
      }
      section("") {
        href(
          name: "href_rebuild_all",
          title: getText("page_rebuild_all_title"),
          required: false,
          description: getText("page_rebuild_all_desc"),
          page: "page_rebuild_all"
        )
      }
      section("") {
        href(
          name: "href_remove_all",
          title: getText("page_remove_all_title"),
          required: false,
          description: getText("page_remove_all_desc"),
          page: "page_remove_all"
        )
      }
	  section("") {
		input("debugOutput", "bool", title: getText("debug_logging"), submitOnChange: true)
		input("traceOutput", "bool", title: getText("trace_logging"), submitOnChange: true)
	  }
    }
  }
}

/**
 * Page page_cid_management generator.
 */
def page_cid_management() {
  // TODO: Find a way to clear our current values on loading page
  return\
  dynamicPage(
    name: "page_cid_management",
    title: getText("page_cid_management_title")
  ) {
    def found_devices = []
    getAllChildDevices().each {
      device->
        if (device.deviceNetworkId.contains(":CID-")) {
          found_devices << getDeviceNamePart(device)
        }
    }
    section_no_save_note()
    section {
      if (found_devices.size()) {
        input(
          name: "input_cid_devices",
          type: "enum",
          required: false,
          multiple: true,
          options: found_devices,
          title: getText("input_cid_devices_title"),
          description: getText("input_cid_devices_desc"),
          submitOnChange: true
        )
        if (input_cid_devices) {
          href(
            name: "href_remove_selected_cid",
            required: false,
            page: "page_remove_selected_cid",
            title: getText("page_remove_selected_cid_title"),
            description: getText("page_remove_selected_cid_desc")
          )
        }
      }
    }
    section {
      href(
        name: "href_add_new_cid",
        required: false,
        page: "page_add_new_cid",
        title: getText("page_add_new_cid_title"),
        description: getText("page_add_new_cid_desc")
      )
    }
    section_back_note()
  }
}

/**
 * Page page_remove_selected_cid generator
 */
def page_remove_selected_cid() {
  def errors = []
  getAllChildDevices().each {
    device->
      if (device.deviceNetworkId.contains(":CID-")) {
        // Only remove the one that matches our list
        def device_name = getDeviceNamePart(device)
        def d = input_cid_devices.find {
          it == device_name
        }
        if (d) {
          logTrace("removing CID device ${device.deviceNetworkId}")
          try {
            deleteChildDevice(device.deviceNetworkId)
            input_cid_devices.remove(device_name)
            errors << "Success removing " + device_name
          } catch (e) {
            log.error("There was an error (${e}) when trying " +
              "to delete the child device")
            errors << "Error removing " + device_name
          }
        }
      }
  }

  return\
  dynamicPage(
    name: "page_remove_selected_cid",
    title: getText("page_remove_selected_cid_title")
  ) {
    section_no_save_note()
    section {
      paragraph getText("page_remove_selected_cid_info")
      errors.each {
        error->
          paragraph(error)
      }
    }
    section_back_note()
  }
}


/**
 * Page page_add_new_cid generator
 */
def page_add_new_cid() {

  return\
  dynamicPage(
    name: "page_add_new_cid",
    title: getText("page_add_new_cid_title")
  ) {
    section_no_save_note()
    // show pre defined CID number templates to select from
    section(getText("section_build_cid", buildcid())) {
      input(
        name: "input_cid_number",
        type: "enum",
        required: true,
        multiple: false,
        options: cid_numbers,
        title: getText("input_cid_number_title"),
        description: getText("input_cid_number_desc"),
        submitOnChange: true
      )
    }
    // if a CID entry is selected then check the value if it is "0"
    // to show raw input section
    if (input_cid_number) {
      if (input_cid_number == "0") {
        section {
          input(
            name: "input_cid_number_raw",
            type: "text",
            required: true,
            title: getText("input_cid_number_raw_title"),
            defaultValue: 110,
            submitOnChange: true
          )
        }
      }
      section(getText("section_cid_value", buildcidvalue())) {
        input(
          name: "input_cid_value",
          type: "text",
          required: true,
          title: getText("input_cid_value_title"),
          defaultValue: "???",
          submitOnChange: true
        )
      }
      section(getText("section_cid_partition", input_cid_partition)) {
        input(
          name: "input_cid_partition",
          type: "enum",
          required: true,
          defaultValue: 1,
          options: ['?', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9'],
          submitOnChange: true,
          title: getText("input_cid_partition_title")
        )
      }
      section(getText("section_cid_name")) {
        input(
          name: "input_cid_name",
          type: "text",
          required: false,
          defaultValue: '',
          submitOnChange: true,
          title: getText("input_cid_name_title")
        )
      }
      section(getText("section_cid_label")) {
        input(
          name: "input_cid_label",
          type: "text",
          required: false,
          defaultValue: '',
          submitOnChange: true,
          title: getText("input_cid_label_title")
        )
      }
      // If input_cid_number or input_cid_number_raw have a value
      if ((input_cid_number && (input_cid_number != "0")) ||
        (input_cid_number_raw)) {
        section("") {
          href(
            name: "href_add_new_cid_confirm",
            required: false,
            page: "page_add_new_cid_confirm",
            title: getText("page_add_new_cid_confirm_title",
              buildcidlabel() + "(" + buildcidnetworkid() + ")"),
            description: getText("href_add_new_cid_confirm_desc")
          )
        }
      }
      section_back_note()
    }
  }
}

/**
 * page_add_new_cid helpers
 */
def buildcid() {
  def cidnum = ""
  if (input_cid_number == "0") {
    cidnum = input_cid_number_raw
  } else {
    cidnum = input_cid_number
  }
  return cidnum
}

def buildcidname() {
  if (input_cid_name) {
    return "CID-" + input_cid_name
  } else {
    return buildcidnetworkid()
  }
}

def buildcidlabel() {
  if (input_cid_label) {
    return "CID-" + input_cid_label
  } else {
    return buildcidnetworkid()
  }
}

def buildcidnetworkid() {
  // get the CID value
  def newcid = buildcid()

  def cv = buildcidvalue()
  def pt = input_cid_partition
  return "CID-${newcid}-${pt}-${cv}"
}

def buildcidvalue() {
  def cidval = input_cid_value
  return cidval
}

/**
 * Page page_add_new_cid_confirm generator.
 */
def page_add_new_cid_confirm() {
  def errors = []
  // get the CID value
  def newcidlabel = buildcidlabel()
  def newcidname = buildcidname()
  def newcidnetworkid = buildcidnetworkid()
  def cv = input_cid_value
  def pt = input_cid_partition

  // Add virtual CID switch if it does not exist.
  def d = getChildDevice("${getDeviceKey()}:${newcidlabel}")
  if (!d) {
    def nd = \
      addChildDevice(
        APPNAMESPACE,
        "AlarmDecoder action button indicator",
        "${getDeviceKey()}:${newcidnetworkid}",
        state.hubId,
        [
          name: "${getDeviceKey()}:${newcidname}",
          label: "${sname} ${newcidlabel}",
          completedSetup: true
        ]
      )
    nd.sendEvent(
      name: "switch",
      value: "off",
      isStateChange: true,
      displayed: false
    )
    errors << "Success adding ${newcidlabel}"
  } else {
    errors << "Error adding ${newcidlabel}: Exists"
  }

  return\
  dynamicPage(
    name: "page_add_new_cid_confirm",
    title: buildcidlabel()
  ) {
    section_no_save_note()
    section(getText("add_new_cid_confirm_info")) {
      errors.each {
        error->
          paragraph(error)
      }
    }
    section_back_note()
  }
}

/**
 * Page page_rfx_management generator.
 */
def page_rfx_management() {
  return\
  dynamicPage(
    name: "page_rfx_management",
    title: getText("page_rfx_management_title")
  ) {
    def found_devices = []
    getAllChildDevices().each {
      device->
        if (device.deviceNetworkId.contains(":RFX-")) {
          found_devices << \
            getDeviceNamePart(device)
        }
    }
    section_no_save_note()
    section("") {
      if (found_devices.size()) {
        input(
          name: "input_rfx_devices",
          type: "enum",
          required: false,
          multiple: true,
          options: found_devices,
          title: getText("input_rfx_devices_title"),
          description: getText("input_rfx_devices_desc"),
          submitOnChange: true
        )
        if (input_rfx_devices) {
          href(
            name: "href_remove_selected_rfx",
            required: false,
            page: "page_remove_selected_rfx",
            title: getText("page_remove_selected_rfx_title"),
            description: getText("href_remove_selected_rfx_desc")
          )
        }
      }
    }
    section("") {
      href(
        name: "href_add_new_rfx",
        required: false,
        page: "page_add_new_rfx",
        title: getText("tap_here"),
        description: getText("page_add_new_rfx_desc", "")
      )
    }
    section_back_note()
  }
}

/**
 * Page page_remove_selected_rfx generator
 */
def page_remove_selected_rfx() {
  def errors = []
  getAllChildDevices().each {
    device->
      if (device.deviceNetworkId.contains(":RFX-")) {
        // Only remove the one that matches our list
        def device_name = getDeviceNamePart(device)
        def d = input_rfx_devices.find {
          it == device_name
        }
        if (d) {
          logTrace("removing RFX device ${device.deviceNetworkId}")
          try {
            deleteChildDevice(device.deviceNetworkId)
            input_rfx_devices.remove(device_name)
            errors << "Success removing " + device_name
          } catch (e) {
            log.error "There was an error (${e}) when trying" +
              " to delete the child device"
            errors << "Error removing " + device_name
          }
        }
      }
  }

  return dynamicPage(
    name: "page_remove_selected_rfx",
    title: getText("page_remove_selected_rfx_title")
  ) {
    section_no_save_note()
    section(getText("info_page_remove_selected_rfx")) {
      errors.each {
        error->
          paragraph(error)
      }
    }
    section_back_note()
  }
}

/**
 * Page page_add_new_rfx generator
 */
def page_add_new_rfx() {

  return\
  dynamicPage(
    name: "page_add_new_rfx",
    title: getText("page_add_new_rfx_title")
  ) {
    section_no_save_note()
    section(getText("section_build_rfx")) {
      paragraph getText("section_rfx_names")
      input(
        name: "input_rfx_label",
        type: "text",
        required: false,
        defaultValue: '',
        submitOnChange: true,
        title: getText("input_rfx_label")
      )
      input(
        name: "input_rfx_name",
        type: "text",
        required: false,
        defaultValue: '',
        submitOnChange: true,
        title: getText("input_rfx_name")
      )
    }
    section {
      input(
        name: "input_rfx_sn",
        type: "text",
        required: true,
        defaultValue: '000000',
        submitOnChange: true,
        title: getText("input_rfx_sn")
      )
      input(
        name: "input_rfx_bat",
        type: "text",
        required: true,
        defaultValue: '?',
        submitOnChange: true,
        title: getText("input_rfx_bat")
      )
      input(
        name: "input_rfx_supv",
        type: "text",
        required: true,
        defaultValue: '?',
        submitOnChange: true,
        title: getText("input_rfx_supv")
      )
      input(
        name: "input_rfx_loop0",
        type: "text",
        required: true,
        defaultValue: '1',
        submitOnChange: true,
        title: getText("input_rfx_loop0")
      )
      input(
        name: "input_rfx_loop1",
        type: "text",
        required: true,
        defaultValue: '?',
        submitOnChange: true,
        title: getText("input_rfx_loop1")
      )
      input(
        name: "input_rfx_loop2",
        type: "text",
        required: true,
        defaultValue: '?',
        submitOnChange: true,
        title: getText("input_rfx_loop2")
      )
      input(
        name: "input_rfx_loop3",
        type: "text",
        required: true,
        defaultValue: '?',
        submitOnChange: true,
        title: getText("input_rfx_loop3")
      )
    }
    section {
      href(
        name: "href_add_new_rfx_confirm",
        required: false,
        page: "page_add_new_rfx_confirm",
        title: getText("tap_here"),
        description: getText("page_add_new_rfx_desc",
          "${buildrfxlabel()} (${buildrfxnetworkid()})")
      )
    }
    section_back_note()
  }
}

/**
 * page_add_new_rfx helpers
 */
def buildrfx() {
  return input_rfx_sn
}

def buildrfxname() {
  if (input_rfx_name) {
    return "RFX-" + input_rfx_name
  } else {
    return buildrfxnetworkid()
  }
}

def buildrfxlabel() {
  if (input_rfx_label) {
    return "RFX-" + input_rfx_label
  } else {
    return buildrfxnetworkid()
  }
}

def buildrfxnetworkid() {
  // get the RFX value
  def newrfx = buildrfx()

  def cv = buildrfxvalue()
  return "RFX-${newrfx}-${cv}"
}

def buildrfxvalue() {
  def rfxval = "${input_rfx_bat}-${input_rfx_supv}-${input_rfx_loop0}-" +
    "${input_rfx_loop1}-${input_rfx_loop2}-${input_rfx_loop3}"
  return rfxval
}

/**
 * Page page_add_new_rfx_confirm generator.
 */
def page_add_new_rfx_confirm() {
  def errors = []
  // get the RFX value
  def newrfxlabel = buildrfxlabel()
  def newrfxname = buildrfxname()
  def newrfxnetworkid = buildrfxnetworkid()
  def cv = input_rfx_value

  // Add virtual RFX switch if it does not exist.
  def d = getChildDevice("${getDeviceKey()}:${newrfxnetworkid}")
  if (!d) {
    def nd = addChildDevice(
      APPNAMESPACE,
      "AlarmDecoder action button indicator",
      "${getDeviceKey()}:${newrfxnetworkid}",
      state.hubId,
      [
        name: "${getDeviceKey()}:${newrfxname}",
        label: "${sname} ${newrfxlabel}",
        completedSetup: true
      ]
    )
    nd.sendEvent(
      name: "switch",
      value: "off",
      isStateChange: true,
      displayed: false
    )
    errors << "Success adding ${newrfxlabel}"
  } else {
    errors << "Error adding ${newrfxlabel}: Exists"
  }

  return\
  dynamicPage(
    name: "page_add_new_rfx_confirm",
    title: buildrfxlabel()
  ) {
    section_no_save_note()
    section("") {
      paragraph getText("info_add_new_rfx_confirm")
      errors.each {
        error->
          paragraph(error)
      }
    }
    section_back_note()
  }
}

/**
 * Page page_rebuild_all generator.
 */
def page_rebuild_all(params) {
  def message = ""

  return\
  dynamicPage(
    name: "page_rebuild_all",
    title: getText("page_rebuild_all_title")
  ) {
    section_no_save_note()
    if (params?.confirm) {
      // Call rebuild device function here
      addExistingDevices()
      message = getText("info_rebuild_all_done")
    } else {
      section("") {
        href(
          name: "href_confirm_rebuild_all_devices",
          title: getText("confirm_rebuild_all"),
          description: getText("href_rebuild_devices"),
          required: false,
          page: "page_rebuild_all",
          params: [confirm: true]
        )

      }
	  		section(getText("section_sensor_counts")) {	  
			  input(name: "inputContactSensorCount", type: "number", defaultValue: 20, required: true, title: getText("contact_sensor_count"))
			  input(name: "inputSmokeDetector", type: "number", defaultValue: 2, required: true, title: getText("smoke_detector_count"))
			  input(name: "inputCODetector", type: "number", defaultValue: 2, required: true, title: getText("co_detector_count"))
			  input(name: "inputMotionDetector", type: "number", defaultValue: 2, required: true, title: getText("motion_sensor_count"))
			  input(name: "inputShockSensor", type: "number", defaultValue: 1, required: true, title: getText("shock_sensor_count"))
			  input(name: "inputCreateDisarm", type: "bool", defaultValue: true, title: getText("create_disarm_switch"))
		  }
      message = getText("info_rebuild_all_confirm")
    }
    section("") {
      paragraph message
    }
    section_back_note()
  }
}

/**
 * Page page_remove_all generator.
 */
def page_remove_all(params) {
  def message = ""

  return\
  dynamicPage(
    name: "page_remove_all",
    title: getText("page_remove_all_title")
  ) {
    section_no_save_note()
    if (params?.confirm) {
      uninstalled()
      message = getText("info_remove_all_done")
    } else {
      section("") {
        href(
          name: "href_confirm_remove_all_devices",
          title: getText("remove_all_href_confirm"),
          description: getText("info_remove_all_confirm"),
          required: false,
          page: "page_remove_all",
          params: [confirm: true]
        )
      }
    }
    section("") {
      paragraph message
    }
    section_back_note()
  }
}

/**
 * Page page_select_device generator
 *   reloaded every N seconds to refresh list
 */
def page_select_device() {
  // send out UPNP discovery messages and watch for responses
  discover_alarmdecoder()

  // build list of currently known AlarmDecoder parent devices
  def found_devices = [: ]
  def options = getDevices().each { k, v ->
    logDebug "page_select: ${v}"
    def ip = convertHexToIP(v.ip)
    found_devices["${v.ip}:${v.port}"] = "AlarmDecoder @ ${ip}"
  }

  // How many do we have?
  def numFound = found_devices.size() ?: 0
	def install = true
	def nextPage = ""
	 if (isHubitat()) {
		install = false
		nextPage = "page_discover"
	}
	
  return\
  dynamicPage(
    name: "page_select_device",
    title: getText("page_select_device_title"),
	install: install,
	nextPage: nextPage
  ) {
	if (isSmartThings()) {
		section_no_save_note()
	}
    section("Discovered devices: (scanning)") {
      input(
        name: "input_selected_devices",
        type: "enum",
        required: false,
        title: getText("input_selected_devices_title", numFound),
        multiple: false,
        submitOnChange: true,
        options: found_devices
      )
    }
    section_back_note()
  }
}

/**
 * Page page_discover generator.
 */
def page_discover() {

  // send out UPNP discovery messages and watch for responses
  discover_alarmdecoder()

  // build list of currently known AlarmDecoder parent devices
  def found_devices = [: ]
  logDebug "devices ${getDevices()}"
  def options = getDevices().each { k, v ->
    logDebug "page_discover: ${v}"
    def ip = convertHexToIP(v.ip)
    found_devices["${v.ip}:${v.port}"] = "AlarmDecoder @ ${ip}"
  }

  // How many do we have?
  def numFound = found_devices.size() ?: 0

  // Load strings for the correct platform
  def monitor_suffix = ""
  if (isSmartThings())
    monitor_suffix = "SHM"
  else if (isHubitat())
    monitor_suffix = "HSM"

  return\
  dynamicPage(
    name: "page_discover",
    title: getText("page_discover_title")
  ) {
    def section_select_device_heading = ""

    if (input_selected_devices) {
      // Find the discovered device with a matching
      // dni XXXXXXXX:XXXX in input_selected_devices
      def d = \
        getDevices().find { k, v -> "${v.ip}:${v.port}" ==
          "${input_selected_devices}"
        }

      def dni = getDeviceKey(d?.value?.ip, d?.value?.port)
      def urn = getHostAddressFromDNI(input_selected_devices)
      def ssdpPath = d?.value?.ssdpPath
      def mac = d ?.value?.mac
      def uuid = d?.value?.ssdpUSN

      section_select_device_heading =
        getText("page_discover_section_selected_device",
          "\ndni: ${dni}\nurn: ${urn}\nssdpPath: ${ssdpPath}\n" +
          "mac: ${mac}\nusn: ${uuid}")
      section_save_note()
    }
    section(section_select_device_heading) {
      href(
        name: "href_confirm_discover_update",
        title: getText("tap_here"),
        description: getText("page_select_device_desc"),
        required: false,
        page: "page_select_device"
      )
    }
	if (input_selected_devices) {
		section(getText("section_sensor_counts")) {	  
			  input(name: "inputContactSensorCount", type: "number", defaultValue: 20, required: true, title: getText("contact_sensor_count"))
			  input(name: "inputSmokeDetector", type: "number", defaultValue: 2, required: true, title: getText("smoke_detector_count"))
			  input(name: "inputCODetector", type: "number", defaultValue: 2, required: true, title: getText("co_detector_count"))
			  input(name: "inputMotionDetector", type: "number", defaultValue: 2, required: true, title: getText("motion_sensor_count"))
			  input(name: "inputShockSensor", type: "number", defaultValue: 1, required: true, title: getText("shock_sensor_count"))
			  input(name: "inputCreateDisarm", type: "bool", defaultValue: true, title: getText("create_disarm_switch"))
		  }
		section(getText("section_mon_integration")) {
		  input(
			name: "monIntegration",
			type: "bool",
			defaultValue: true,
			title: getText("monIntegration${monitor_suffix}")
		  )
		  input(
			name: "monChangeStatus",
			type: "bool",
			defaultValue: true,
			title: getText("monChangeStatus${monitor_suffix}")
		  )
		}
		section(getText("section_zone_sensor_settings")) {
		  input(
			name: "defaultSensorToClosed",
			type: "bool",
			defaultValue: true,
			title: getText("defaultSensorToClosed")
		  )
		}
	}

    section_back_note()
  }
}

/**
 * Page page_relink_update generator.
 */
def page_relink_update(params) {
  def message = ""
  def errors = []

  // Load strings for the correct platform
  def monitor_suffix = ""
  if (isSmartThings())
    monitor_suffix = "SHM"
  else if (isHubitat())
    monitor_suffix = "HSM"

  return\
  dynamicPage(
    name: "page_relink_update",
    title: getText("page_relink_update_title")
  ) {
    section_no_save_note()

    if (params?.confirm) {
      message = getText("info_relink_update_done")
      // re-init subs just in case they were lost in the cloud.
      initSubscriptions()

      // current device key before we change it
      def dkey = getDeviceKey()

      // new data
      def dni = params?.dni
      def urn = params?.urn
      def ssdpPath = params?.ssdpPath
      def mac = params?.mac
      def uuid = params?.uuid

      // FIXME: need to refactor this.
      // now update our static state
      state.ip = dni.split(":").first()
      state.port = dni.split(":").last()

      // Set URN for the child device
      state.urn = urn

      try {
        // Update device by its MAC address if the DNI changes
        def children = getChildDevices()
        children.each {
          def suffix = ""
          // The primary device has no suffix ":armedAway" etc.
          if (it.deviceNetworkId != dkey) {
            suffix = ":${it.deviceNetworkId.split(":").last().trim()}"
          } else {
            // must be the parent lets udpate the data for it.
            it.updateDataValue("mac", mac)
            it.updateDataValue("urn", urn)
            it.updateDataValue("ssdpUSN", uuid)
            it.updateDataValue("ssdpPath", ssdpPath)
          }
          it.setDeviceNetworkId("${dni}${suffix}")

          // FIXME: We also need to update the Name and Label but how?
          // what else?
        }

        errors << "Success updating ${dkey} to ${dni}"
      } catch (e) {
        log.error("There was an error (${e}) when trying " +
          "to relink device ${dkey} to ${dni}")
        errors << "Error relinking ${dkey} to ${dni}"
      }
    } else {

      // get the active device for info display
      def d = getChildDevice(getDeviceKey())
      if (!d) {
        log.warn("page_relink_update: Could not find primary" +
          " device for '${getDeviceKey()}'.")
        return
      }

      // Build heading vars for current active device.
      def dni = getDeviceKey()
      def urn = d.getDeviceDataByName("urn")
      def ssdpPath = d.getDeviceDataByName("ssdpPath")
      def mac = d.getDeviceDataByName("mac")
      def uuid = d.getDeviceDataByName("ssdpUSN")

      section(getText("page_relink_section_active_device",
        "\ndni: ${dni}\nurn: ${urn}\nssdpPath: ${ssdpPath}\n" +
        "mac: ${mac}\nusn: ${uuid}")) {
        href(
          name: "href_select_device_relink_update",
          title: getText("tap_here"),
          description: getText("page_select_device_desc"),
          required: false,
          page: "page_select_device"
        )
      }

      // Find the discovered device with a matching
      // dni XXXXXXXX:XXXX in input_selected_devices
      d = \
        getDevices().find { k, v ->
          "${v.ip}:${v.port}" == "${input_selected_devices}"
        }

      if (d) {
        // Build heading vars for UI selected device.

        dni = getDeviceKey(d.value.ip, d.value.port)
        urn = getHostAddressFromDNI(input_selected_devices)
        ssdpPath = d.value.ssdpPath
        mac = d.value.mac
        uuid = d.value.ssdpUSN

        section(getText("page_relink_section_selected_device",
          "\ndni: ${dni}\nurn: ${urn}\nssdpPath: ${ssdpPath}\n" +
          "mac: ${mac}\nusn: ${uuid}")) {
          href(
            name: "href_confirm_relink_update",
            title: getText("tap_here"),
            description: getText("info_confirm_relink_update", "${urn}"),
            required: false,
            page: "page_relink_update",
            params: [
              confirm: true,
              dni: dni,
              urn: urn,
              ssdpPath: ssdpPath,
              mac: mac,
              uuid: uuid
            ]
          )
        }
      }

      section(getText("section_monitor_integration")) {
        input(
          name: "monIntegration",
          type: "bool",
          defaultValue: true,
          submitOnChange: true,
          title: getText("monIntegration${monitor_suffix}")
        )
        input(
          name: "monChangeStatus",
          type: "bool",
          defaultValue: true,
          submitOnChange: true,
          title: getText("monChangeStatus${monitor_suffix}")
        )
      }
      section(getText("section_zone_sensor_settings")) {
        input(
          name: "defaultSensorToClosed",
          type: "bool",
          defaultValue: true,
          submitOnChange: true,
          title: getText("defaultSensorToClosed")
        )
      }
    }
    section("") {
      paragraph message
      errors.each {
        error->
          paragraph(error)
      }
    }

    section_back_note()
  }
}

/*** Standard service callbacks ***/

/**
 *  installed()
 */
def installed() {
  logTrace "installed"
  logDebug "Installed with settings: ${settings}"

  // initialize everything
  initialize()
}

/**
 * updated()
 */
def updated() {
  logTrace "updated"
  logDebug "Updated with settings: ${settings}"

  // re initialize everything
  initialize()
}

/**
 * uninstalled()
 */
def uninstalled() {
  logTrace "uninstalled"

  // disable all scheduling and subscriptions
  unschedule()

  // remove all the devices and children
  def devices = getAllChildDevices()
  devices.each {
    try {
      logDebug "deleting child device: ${it.deviceNetworkId}"
      deleteChildDevice(it.deviceNetworkId)
    } catch (Exception e) {
      log.error("exception while uninstalling: ${e}")
    }
  }
}

/**
 * initialize called upon update and at startup
 *   Add subscriptions and schdules
 *   Create our default state
 */
def initialize() {
  logTrace "initialize"

  // unsubscribe from everything
  unsubscribe()

  // remove all schedules
  unschedule()

  // Create our default state values
  state.lastMONStatus = null
  state.lastAlarmDecoderStatus = null

  // Network and Monitor subscriptions
  initSubscriptions()

  // if a device in the GUI is selected then add it.
  if (input_selected_devices) {
    addExistingDevices()
  }

  // Device handler -> service subscriptions
  configureDeviceSubscriptions()

  // keep us subscribed to notifications
  getAllChildDevices().each {
    device->
      // Only refresh the main device that has a panel_state
      def device_type = device.getTypeName()
    if (device_type == "AlarmDecoder network appliance") {
      logDebug("initialize: Found device refresh subscription.")
      device.subscribeNotifications()
    }
  }
}

/*** Event handlers ***/

/**
 * locationHandler(evt)
 * Local SSDP/UPNP network messages sent on UDP port 1900
 * and NOTIFICATIONS sent to the hub.localSrvPortTCP
 * will be captured here. The messages will then if valid be parsed
 * into a parsedEvent Map.
 *
 * Test from the AlarmDecoder Appliance:
 *   curl
 *
 */
def locationHandler(evt) {
  logTrace "locationHandler: name: '${evt.name}'"

  // only process events with a description.
  if (!evt.description) {
    logDebug("locationHandler: skipping event missing 'description'")
    return
  }

  // Parse message into parsedEvent map
  def parsedEvent = parseEventMessage(evt.description)

  // UPNP LAN EVENTS on UDP port 1900 from 'AlarmDecoder:1' devices only
  //// parse and update state.devices Map
  if (parsedEvent.ssdpTerm?.contains(SSDPTERM)) {
    def ct = now()

    logDebug "locationHandler: received ssdpTerm match."

    // Pre fill parsed event object with hubId the event was from.
    parsedEvent << ["hubId": evt?.hubId]

    // Add a timestamp for garbage collection
    parsedEvent << ["ts": now()]

    // get our ssdp discovery results array.
    def alarmdecoders = getDevices()

    // Look at all entries and remove expired ones
    def garbage = []
    alarmdecoders.each { k, v ->
      if (v.ts) {
        // Expire if not seen for 5minutes
        if ((ct - v.ts) / 1000 > (5 * 60)) {
          log.warn("locationHandler: removing expired ssdp discovery: " +
            "mac:${v.mac} ip:${v.ip}")
          garbage << k
        } else {
          if (debug)
            log.warn("locationHandler: ${k} discovery last " +
              "seen: ${(ct-v.ts)/1000}s ago mac:${v.mac} ip:${v.ip})")
        }
      } else {
        // no ts so set one
        log.warn("locationHandler: ts not found for " +
          "mac:${v.mac} ip:${v.ip} adding.")
        v.ts = now()
      }
    }
    // finally remove them
    garbage.each { v ->
        alarmdecoders.remove(v)
    }

    // add/update the device in state.devices with local discovered devices.
    alarmdecoders << ["${parsedEvent.ssdpUSN.toString()}": parsedEvent]

    logDebug("locationHandler: alarmdecoders found: ${alarmdecoders}")

  } else {

    // Content type already parsed here.
    def type = parsedEvent.contenttype

    logDebug("locationHandler: HTTP request type:${type} " +
      "body:${parsedEvent?.body} headers:${parsedEvent?.headers}")

    // XML PUSH data
    if (type?.contains("xml")) {
      // get our primary device the Alarmdecoder UI.
      def d = getChildDevice("${getDeviceKey()}")
      if (d) {
        if (d.getDeviceDataByName("mac") == parsedEvent.mac) {
          logDebug("push_update_alarmdecoders: Found device parse xml data.")

          d.parse_xml(parsedEvent?.body).each {
            e-> d.sendEvent(e)
          }

          return
        }
      }
    }

    // Unkonwn silently ignore
    logDebug("locationHandler: ignoring unknown message from " +
      "name:${evt.name} parsedEvent: ${parsedEvent}")
  }
}

/**
 * Handle remote web requests for http://somegraph/update
 */
def webserviceUpdate() {
  logTrace "webserviceUpdate"
  refresh_alarmdecoders()
  return [status: "OK"]
}

/**
 * Handle our child device action button events
 * sets Contact attributes of the alarmdecoder smoke device
 */
def actionButton(id) {

  // grab our primary AlarmDecoder device object
  def d = getChildDevice("${getDeviceKey()}")
  logDebug("actionButton: desc=${id} dev=${d}")

  if (!d) {
    log.error("actionButton: Could not find primary dev. '${getDeviceKey()}'.")
    return
  }

  /* FIXME: Need a pin code or some way to trust the request. */
  if (inputCreateDisarm) {
    if (id.contains(":disarm")) {
      d.disarm()
    }
  }
  if (id.contains(":exit")) {
    d.exit()
  }
  if (id.contains(":armAway")) {
    d.arm_away()
  }
  if (id.contains(":armStay")) {
    d.arm_stay()
  }
  if (id.contains(":chimeMode")) {
    d.chime()
  }
  if (id.contains(":alarmPanic")) {
    d.panic()
  }
  if (id.contains(":alarmAUX")) {
    d.aux()
  }
  if (id.contains(":alarmFire")) {
    d.fire()
  }
  // Turn off alarm bell if pushed
  if (id.contains(":alarmBell")) {
    def cd = getChildDevice("${id}")
    if (!cd) {
      log.info("actionButton: Could not clear '${id}'.")
      return
    }
    _sendEventTranslate(cd, "off")
  }
  if (id.contains(":CID-")) {
    def cd = getChildDevice("${id}")
    if (!cd) {
      log.info("actionButton: Could not clear device '${id}'.")
      return
    }
    _sendEventTranslate(cd, "off")
  }
}

/**
 * send event to smokeAlarm device to set state [detected, clear]
 */
def smokeSet(evt) {
  logDebug("smokeSet: desc=${evt.value}")

  def d = getChildDevices().find {
    it.deviceNetworkId.contains(":smokeAlarm")
  }

  if (!d) {
    log.info("smokeSet: Could not find 'SmokeAlarm' device.")
    return
  }
  _sendEventTranslate(d, (evt.value == "detected" ? "on" : "off"))

  d = getChildDevice("${getDeviceKey()}:alarmFireStatus")
  if (!d) {
    log.info("smokeSet: Could not find 'alarmFireStatus' device.")
  } else {
    _sendEventTranslate(d, (evt.value == "detected" ? "on" : "off"))
  }
}

/**
 * send event to armAway device to set state
 */
def armAwaySet(evt) {
  logDebug("armAwaySet ${evt.value}")
  def d = getChildDevice("${getDeviceKey()}:armAway")
  if (!d) {
    log.info("armAwaySet: Could not find 'armAway' device.")
  } else {
    _sendEventTranslate(d, evt.value)
  }

  d = getChildDevice("${getDeviceKey()}:armAwayStatus")
  if (!d) {
    log.info("armAwaySet: Could not find 'armAwayStatus' device.")
  } else {
    _sendEventTranslate(d, evt.value)
  }
}

/**
 * send event to armStay device to set state
 */
def armStaySet(evt) {
  logDebug("armStaySet ${evt.value}")
  def d = getChildDevice("${getDeviceKey()}:armStay")
  if (!d) {
    log.info("armStaySet: Could not find 'armStay' device.")
  } else {
    d.sendEvent(
      name: "switch",
      value: evt.value,
      isStateChange: true,
      filtered: true
    )
  }

  d = getChildDevice("${getDeviceKey()}:armStayStatus")
  if (!d) {
    log.info("armStaySet: Could not find 'armStayStatus' device.")
  } else {
    _sendEventTranslate(d, evt.value)
  }
}

/**
 * send event to armNight device to set state
 */
def armNightSet(evt) {
  logDebug("armStaySet ${evt.value}")
  def d = getChildDevice("${getDeviceKey()}:armNight")
  if (!d) {
    log.info("armNightSet: Could not find 'armNight' device.")
  } else {
    d.sendEvent(
      name: "switch",
      value: evt.value,
      isStateChange: true,
      filtered: true
    )
  }

  d = getChildDevice("${getDeviceKey()}:armNightStatus")
  if (!d) {
    log.info("armNightSet: Could not find 'armNightStatus' device.")
  } else {
    _sendEventTranslate(d, evt.value)
  }
}

/**
 * send event to alarmbell indicator device to set state
 */
def alarmBellSet(evt) {
  logDebug("alarmBellSet ${evt.value}")
  def d = getChildDevice("${getDeviceKey()}:alarmBell")
  if (!d) {
    log.info("alarmBellSet: Could not find 'alarmBell' device.")
  } else {
    _sendEventTranslate(d, evt.value)
  }

  d = getChildDevice("${getDeviceKey()}:alarmBellStatus")
  if (!d) {
    log.info("alarmBellSet: Could not find device 'alarmBellStatus'")
  } else {
    _sendEventTranslate(d, evt.value)
  }
}

/**
 * send event to chime indicator device to set state
 */
def chimeSet(evt) {
  logDebug("chimeSet ${evt.value}")
  def d = getChildDevice("${getDeviceKey()}:chimeMode")
  if (!d) {
    log.info("chimeSet: Could not find device 'chimeMode'")
  } else {
    _sendEventTranslate(d, evt.value)
  }

  d = getChildDevice("${getDeviceKey()}:chimeModeStatus")
  if (!d) {
    log.info("chimeSet: Could not find device 'chimeModeStatus'")
  } else {
    _sendEventTranslate(d, evt.value)
  }
}

/**
 * send event to exit indicator device to set state
 */
def exitSet(evt) {
  logDebug("exitSet ${evt.value}")
  def d = getChildDevice("${getDeviceKey()}:exit")
  if (!d) {
    log.info("exitSet: Could not find device 'exit'")
  } else {
    _sendEventTranslate(d, evt.value)
  }

  d = getChildDevice("${getDeviceKey()}:exitStatus")
  if (!d) {
    log.info("exitSet: Could not find device 'exitStatus'")
  } else {
    _sendEventTranslate(d, evt.value)
  }
}

/**
 * send event to perimeter only indicator device to set state
 */
def perimeterOnlySet(evt) {
  logDebug("perimeterOnlySet ${evt.value}")
  def d = getChildDevice("${getDeviceKey()}:perimeterOnlyStatus")
  if (!d) {
    log.info("perimeterOnlySet: Could not find device 'perimeterOnly'")
    return
  }
  _sendEventTranslate(d, evt.value)
}

/**
 * send event to entry delay off indicator device to set state
 */
def entryDelayOffSet(evt) {
  logDebug("entryDelayOffSet ${evt.value}")
  def d = getChildDevice("${getDeviceKey()}:entryDelayOffStatus")
  if (!d) {
    log.info("entryDelayOffSet: Could not find device 'entryDelayOff'")
    return
  }
  _sendEventTranslate(d, evt.value)
}


/**
 * send event to bypass status device to set state
 */
def bypassSet(evt) {
  logDebug("bypassSet ${evt.value}")
  def d = getChildDevice("${getDeviceKey()}:bypassStatus")
  if (!d) {
    log.info("bypassSet: Could not find device 'bypassStatus'")
    return
  }
  _sendEventTranslate(d, evt.value)
}

/**
 * send event to ready status device to set state
 */
def readySet(evt) {
  logDebug("readySet ${evt.value}")
  def d = getChildDevice("${getDeviceKey()}:readyStatus")
  if (!d) {
    log.info("readySet: Could not find 'readyStatus' device.")
    return
  }
  _sendEventTranslate(d, evt.value)
}

/**
 * send event to disarm status device to set state
 */
def disarmSet(evt) {
  logDebug("disarmSet ${evt.value}")
  def d = getChildDevice("${getDeviceKey()}:disarm")
  if (!d) {
    log.info("disarmSet: Could not find 'disarm' device.")
  } else {
    _sendEventTranslate(d, evt.value)
  }
}

/**
 * send CID event to the correct device if one exists
 * evt.value example !LRR:001,1,CID_1406,ff
 */
def cidSet(evt) {
  log.info("cidSet ${evt.value}")

  // get our CID state and number
  def parts = evt.value.split(',')

  // 1 digit QUALIFIER 1 = Event or Open, 3 = Restore or Close
  def cidstate = (parts[2][-4.. - 4] == "1") ? "on" : "off"

  // 3 digit CID number
  def cidnum = parts[2][-3.. - 1]

  // the CID report value. Zone # or User # or ...
  def cidvalue = parts[0].split(':')[1]

  // the partition # with 0 being system
  def partition = parts[1].toInteger()

  logDebug("cidSet num:${cidnum} part: ${partition} " +
    "state:${cidstate} val:${cidvalue}")

  def sent = false
  def rawmsg = evt.value
  def device_name = "CID-${cidnum}-${partition}-${cidvalue}"
  def children = getChildDevices()
  children.each {
    if (it.deviceNetworkId.contains(":CID-")) {

      def match = getDeviceNamePart(it)

      // replace ? with . non regex
      match = match.replace("?", ".")

      if (device_name =~ /${match}/) {
        logDebug("cidSet device: ${device_name} matches ${match} " +
          "sending state ${cidstate}")

        _sendEventTranslate(it, cidstate)

        sent = true
      } else {
        logDebug("cidSet device: ${device_name} no match ${match}")
      }
    }
  }

  if (!sent) {
    log.error("cidSet: Could not find " +
      "'CID-${cidnum}-${partition}-${cidvalue}|XXX' device.")
    return
  }
}

/**
 * send RFX event to the correct device if one exists
 * evt.value example raw !RFX:123123,a0
 * eventmessage 123123:0:0:1:1:0:0
 * 01020304:1388:RFX-123123-?-?-1-?-?-?
 */
def rfxSet(evt) {
 // log.info("rfxSet ${evt.value}")

  // get our RFX state and number
  def parts = evt.value.split(':')

  def sn = parts[0]
  def bat = parts[1]
  def supv = parts[2]
  def loop0 = parts[3]
  def loop1 = parts[4]
  def loop2 = parts[5]
  def loop3 = parts[6]

  if (debug)
    log.info("rfxSet sn:${sn} bat: ${bat} sukpv:${supv} loop0:${loop0} " +
      "loop1:${loop1} loop2:${loop2} loop3:${loop3}")

  def sent = false

  def device_name = "RFX-${sn}-${bat}-${supv}-" +
    "${loop0}-${loop1}-${loop2}-${loop3}"

  def d = getChildDevices().findAll {
    it.deviceNetworkId.contains(":switch") &&
      it.getDataValue("serial") == sn
  }
      
  if (d) {
	d.each {
	  def zoneLoop = it.getDataValue("zoneLoop")
	  def tamperLoop = it.getDataValue("tamperLoop")
	  
	  if (zoneLoop != null && zoneLoop != "null") {
	    if ((zoneLoop == "1" && loop0 == "1") || (zoneLoop == "2" && loop1 == "1") || (zoneLoop == "3" && loop2 == "1") || (zoneLoop == "4" && loop3 == "1")) {
		  _sendEventTranslate(it, ("on"), false)
		} else {
		  _sendEventTranslate(it, ("off"), false)
		}
	  }
	  
	  if (tamperLoop != null && tamperLoop != "null") {
        if ((tamperLoop == "1" && loop0 == "1") || (tamperLoop == "2" && loop1 == "1") || (tamperLoop == "3" && loop2 == "1") || (tamperLoop == "4" && loop3 == "1")) {
		  it.sendEvent(
            name: "tamper",
            value: "detected"
          )
		} else {
		  it.sendEvent(
            name: "tamper",
            value: "clear"
          )
		}
	  }
	}
        
    d.each {
      it.sendEvent(
        name: "low_battery",
        value: bat == "1"
      )
    }
        
    if (supv == "1") {
      def last_checkin = now()
      d.each {
        it.sendEvent(
          name: "last_checkin",
          value: last_checkin
        )
      }
    }
  } 
    
  def children = getChildDevices()
  children.each {
    if (it.deviceNetworkId.contains(":RFX-")) {

      def sp = getDeviceNamePart(it).split("-")

      def match = sp[0] + "-" + sp[1] + "-*"
      if (device_name =~ /${match}/) {
        def tot = 0
        if (sp[2] == "1" && bat == "1") {
          tot++
        }
        if (sp[3] == "1" && supv == "1") {
          tot++
        }
        if (sp[4] == "1" && loop0 == "1") {
          tot++
        }
        if (sp[5] == "1" && loop1 == "1") {
          tot++
        }
        if (sp[6] == "1" && loop2 == "1") {
          tot++
        }
        if (sp[7] == "1" && loop3 == "1") {
          tot++
        }

        _sendEventTranslate(it, (tot > 0 ? "on" : "off"))

        if (debug)
          log.info("rfxSet device: ${device_name} matches ${match} " +
            "sending state ${tot}")

        sent = true

      } 
    }
  }    

  if (!sent) {
    //log.warn("rfxSet: Could not find '${device_name}|XXX' device.")
    return
  }
}

/**
 * Handle Device Command addZone()
 * add a zone during post install to keep it async
 */
def addZone(evt) {

  def i = evt.value
  log.info("App Event: addZone ${i}")

  // do not create devices if testing. Real PITA to delete them
  // every time. ST needs to add a way to delete multiple devices at once.
  if (NOCREATEDEV) {
    log.warn "addZone: NOCREATEDEV enabled skipping ${evt.data.id}."
    return
  }

  def d = getChildDevice("${evt.data.id}")
  if (d) {
    log.warn "addZone: Already found zone ${i} device ${evt.data.id} skipping."
    return
  }

  try {
	def zone_switch = null
	if (evt.data.type == "contact") {
		zone_switch = \
		  addChildDevice(
			APPNAMESPACE,
			"AlarmDecoder virtual contact sensor",
			"${evt.data.id}",
			state.hubId,
			[
			  name: "${evt.data.id}",
			  label: "${sname} Zone Sensor #${i}",
			  completedSetup: true
			]
		  )
	}
	else if (evt.data.type == "smoke") {
		zone_switch = \
		  addChildDevice(
			APPNAMESPACE,
			"AlarmDecoder virtual smoke alarm",
			"${evt.data.id}",
			state.hubId,
			[
			  name: "${evt.data.id}",
			  label: "${sname} Zone Sensor #${i}",
			  completedSetup: true
			]
		  )	
	}
	else if (evt.data.type == "co") {
		zone_switch = \
		  addChildDevice(
			APPNAMESPACE,
			"AlarmDecoder virtual carbon monoxide detector",
			"${evt.data.id}",
			state.hubId,
			[
			  name: "${evt.data.id}",
			  label: "${sname} Zone Sensor #${i}",
			  completedSetup: true
			]
		  )	
	}
	else if (evt.data.type == "shock") {
		zone_switch = \
		  addChildDevice(
			APPNAMESPACE,
			"AlarmDecoder virtual shock sensor",
			"${evt.data.id}",
			state.hubId,
			[
			  name: "${evt.data.id}",
			  label: "${sname} Zone Sensor #${i}",
			  completedSetup: true
			]
		  )	
	}	
	else if (evt.data.type == "motion") {
		zone_switch = \
		  addChildDevice(
			APPNAMESPACE,
			"AlarmDecoder virtual motion detector",
			"${evt.data.id}",
			state.hubId,
			[
			  name: "${evt.data.id}",
			  label: "${sname} Zone Sensor #${i}",
			  completedSetup: true
			]
		  )	
	}
	def sensorValue = "on"
	if (settings.defaultSensorToClosed == true) {
	  sensorValue = "off"
	}
    // Set default contact state.
    _sendEventTranslate(zone_switch, sensorValue)
  } catch (e) {
    log.error "There was an error (${e}) when trying to addZone ${i}"
  }
}


/**
 * Handle Device Command zoneOn()
 * sets Contact attributes of the alarmdecoder device to open/closed
 */
def zoneOn(evt) {
  logDebug("zoneOn: desc=${evt.value}")

  // Find all :switch devices with a matching zone the event.
  def d = getChildDevices().findAll {
    it.deviceNetworkId.contains(":switch") &&
      it.getDataValue("zone") == evt.value && it.getDataValue("serial") == null
  }

  if (d) {
    // Send the event to all devices that had a matching zone value.
    d.each {
      _sendEventTranslate(it, ("on"))
    }
  } else {
    logDebug("zoneOn: Virtual device with zone #${evt.value} not found.")
  }
}

/**
 * Handle Device Command zoneOff()
 * sets Contact attributes of the alarmdecoder device to open/closed
 */
def zoneOff(evt) {
  logDebug("zoneOff: desc=${evt.value}")

  def d = getChildDevices().findAll {
    it.deviceNetworkId.contains(":switch") &&
      it.getDataValue("zone") == evt.value && it.getDataValue("serial") == null
  }

  if (d) {
    // Send the event to all devices that had a matching zone value.
    d.each {
      _sendEventTranslate(it, ("off"))
    }
  } else {
    logDebug("zoneOff: Virtual device with zone #${evt.value} not found.")
  }
}

/**
 * Handle SmartThings Smart Home Monitor(SHM) or Hubitat Safety Monitor (HSM)
 * events and update the UI of the App.
 */
def monitorAlarmHandler(evt) {
  if (settings.monIntegration == false)
    return


  //if (state.lastMONStatus != evt.value) {
    logDebug("monitorAlarmHandler -- update lastMONStatus " +
      "to ${evt.value} from ${state.lastMONStatus}")

    // Update last known MON state
    state.lastMONStatus = evt.value

    getAllChildDevices().each {
      device->
        // Only refresh the main device that has a panel_state
        def device_type = device.getTypeName()
      if (device_type == "AlarmDecoder network appliance") {
        logDebug("monitorAlarmHandler DEBUG-- ${device.deviceNetworkId}")

        /* SmartThings */
        if (isSmartThings()) {
          if (evt.value == "away" || evt.value == "armAway") {
            // do not send if already in that state.
            if (!device.getStateValue("panel_armed") &&
              !device.getStateValue("panel_armed_stay")) {
              device.arm_away()
            } else {
              logTrace "monitorAlarmHandler -- no send arm_away already set"
            }
          } else if (evt.value == "stay" || evt.value == "armHome") {
            // do not send if already in that state.
            if (!device.getStateValue("panel_armed") &&
              !device.getStateValue("panel_armed_stay")) {
              device.arm_stay()
            } else {
              logTrace "monitorAlarmHandler -- no send arm_stay already set"
            }
          } else if (evt.value == "off" || evt.value == "disarm") {
            // do not send if already in that state.
            if (device.getStateValue("panel_armed") ||
              device.getStateValue("panel_armed_stay")) {
              device.disarm()
            } else {
              logTrace "monitorAlarmHandler -- no send disarm already set"
            }
          } else
            logDebug "Unknown SHM alarm value: ${evt.value}"
        }
        /* Hubitat */
        else if (isHubitat()) {
          if (evt.value == "armedAway") {
            // do not send if already in that state.
            if (!device.getStateValue("panel_armed") &&
              !device.getStateValue("panel_armed_stay")) {
              device.arm_away()
            } else {
              logTrace "monitorAlarmHandler -- no send arm_away already set"
            }
          } else if (evt.value == "armedHome") {
            // do not send if already in that state.
            if (!device.getStateValue("panel_armed") &&
              !device.getStateValue("panel_armed_stay")) {
              device.arm_stay()
            } else {
              logTrace "monitorAlarmHandler -- no send arm_stay already set"
            }
          } else if (evt.value == "armedNight") {
			// do not send if already in that state.
            if (!device.getStateValue("panel_armed") &&
              !device.getStateValue("panel_armed_night")) {
              device.arm_night()
            } else {
              logTrace "monitorAlarmHandler -- no send arm_night already set"
            }
		  } else if (evt.value == "disarmed") {
            // do not send if already in that state.
            if (device.getStateValue("panel_armed") ||
              device.getStateValue("panel_armed_stay")) {
              device.disarm()
            } else {
              logTrace "monitorAlarmHandler -- no send disarm already " +
                "set ${device.getStateValue('panel_armed')} " +
                "${device.getStateValue('panel_armed_stay')}"
            }
          } else
            logDebug "Unknown HSM alarm value: ${evt.value}"
        }
      }
    //}
  }
}

/**
 * Handle Alarm events from the AlarmDecoder and
 * send them back to the the Monitor API to update the
 * status of the alarm panel
 */
def alarmdecoderAlarmHandler(evt) {
  if (settings.monIntegration == false || settings.monChangeStatus == false)
    return

  logDebug("alarmdecoderAlarmHandler -- update lastAlarmDecoderStatus " +
    "to ${evt.value} from ${state.lastAlarmDecoderStatus}")

  state.lastAlarmDecoderStatus = evt.value

  if (isSmartThings()) {
    /* no traslation needed for [stay,away,off] but night has to be translated to stay */
    logDebug("alarmdecoderAlarmHandler alarmSystemStatus ${evt.value}")

    msg = evt.value
	
    if (evt.value == "night")
      msg = "stay"
    // Update last known MON state
    state.lastMONStatus = msg

    sendLocationEvent(name: "alarmSystemStatus", value: msg)
  } else if (isHubitat()) {
    /* translate to HSM */
    msg = ""
    nstate = ""
    if (evt.value == "stay") {
      msg = "armHome"
      nstate = "armedHome" // prevent loop
    }
    if (evt.value == "away") {
      msg = "armAway"
      nstate = "armedAway" // prevent loop
    }
	if (evt.value == "night") {
      msg = "armNight"
      nstate = "armedNight" // prevent loop
	}
    if (evt.value == "off") {
      msg = "disarm"
      nstate = "disarmed" // prevent loop
    }

    logDebug("alarmdecoderAlarmHandler: hsmSetArm ${msg} " +
      "last ${state.lastMONStatus} new ${nstate}")

    // Update last known MON state
    state.lastMONStatus = nstate

    // Notify external MON of the change
    sendLocationEvent(name: "hsmSetArm", value: msg)
  } else {
    log.warn("alarmdecoderAlarmHandler: monttype?  evt:value: ${evt.value} " +
      " lastAlarmDecoderStatus: ${state.lastAlarmDecoderStatus}")
  }
}

/*** Utility/Misc ***/

/*
 * determines if the app is running under SmartThings
 */
def isSmartThings() {
  return physicalgraph?.device?.HubAction;
}

/*
 * determines if the app is running under Hubitat
 */
def isHubitat() {
  return hubitat?.device?.HubAction;
}

/**
 * Enable primary network and system subscriptions
 */
def initSubscriptions() {
  // subscribe to the Smart Home Manager api for alarm status events
  logDebug("initSubscriptions: Subscribe to handlers")

  if (isSmartThings()) {
    subscribe(location, "alarmSystemStatus", monitorAlarmHandler)
  } else if (isHubitat()) {
    subscribe(location, "hsmStatus", monitorAlarmHandler)
  }

  // subscribe to add zone handler
  subscribe(app, addZone)

  // subscribe to local LAN messages to this HUB on TCP port 39500 and
  // UPNP UDP port 1900
  subscribe(location, null, locationHandler, [filterEvents: false])
}

/**
 * Called by page_discover page periodically
 * sends a UPNP discovery message from the HUB
 * to the local network
 */
def discover_alarmdecoder() {
  logDebug("discover_alarmdecoder")
  def haobj =
    getHubAction("lan discovery ${SSDPTERM}}")

  sendHubCommand(haobj)
}

/*
 * sendVerify sends a message to the HUB.
 */
def sendVerify(DNI, ssdpPath) {

  String ip = getHostAddressFromDNI(DNI)

  logDebug("verifyAlarmDecoder: ${DNI} ssdpPath: ${ssdpPath} ip: ${ip}")

  def haobj =
    getHubAction(
      [method: "GET", path: ssdpPath, headers: [Host: ip, Accept: "*/*"]],
      DNI
    )

  sendHubCommand(haobj)
}

/**
 * Call refresh() on the AlarmDecoder parent device object.
 * This will force the HUB to send a REST API request to the AlarmDecoder
 * Network Appliance. and get back the current status of the AlarmDecoder.
 */
def refresh_alarmdecoders() {
  logDebug("refresh_alarmdecoders")

  // just because it seems to get lost.
  initSubscriptions()

  getAllChildDevices().each {
    device->
      // Only refresh the main device that has a panel_state
      def device_type = device.getTypeName()
    if (device_type == "AlarmDecoder network appliance") {
      def apikey = device._get_api_key()
      if (apikey) {
        device.refresh()
      } else {
        log.error("refresh_alarmdecoders no API KEY for: " +
          "${device} @ ${device.getDataValue("urn")}")
      }
    }
  }
}

/**
 * return the list of known devices and initialize the list if needed.
 *
 * FIXME: SM20180315:
 *        This uses the ssdpUSN as the key when we also use DNI
 *        Why not just use DNI all over or ssdpUSN. Keep it consistent.
 *        We get ssdpUSN from our UPNP discovery messages on port 1900
 *        and then we get DNI messages from our GET requests to the
 *        alarmdecoder web services on port 5000. We can also get DNI
 *        from Notification events we subscribe to when the AlarmDecoder
 *        sends us requests on port 39500. Easy way is to use DNI as we get
 *        it every time from all requests. Downside is we can not have more
 *        than one AlarmDecoder per IP:PORT. This seems ok to me for now.
 *
 *
 *  state.devices structure
 *  [
 *      uuid:0c510e98-8ce0-11e7-81a5-XXXXXXXXXXXXXX:
 *      [
 *          port:1388,
 *          ssdpUSN:uuid:0c510e98-8ce0-11e7-81a5-XXXXXXXXXXXXXX,
 *          devicetype:04,
 *          mac:XXXXXXXXXX02,
 *          hub:936de0be-1cb7-4185-9ac9-XXXXXXXXXXXXXX,
 *          ssdpPath:http://XXX.XXX.XXX.XXX:5000,
 *          ssdpTerm:urn:schemas-upnp-org:device:AlarmDecoder:1,
 *          ip:XXXXXXX2
 *      ],
 *      uuid:592952ba-77b0-11e7-b0c7-XXXXXXXXXXXXXX:
 *      [
 *          port:1388,
 *          ssdpUSN:uuid:592952ba-77b0-11e7-b0c7-XXXXXXXXXXXXXX,
 *          devicetype:04,
 *          mac:XXXXXXXXXX01,
 *          hub:936de0be-1cb7-4185-9ac9-XXXXXXXXXXXXXX,
 *          ssdpPath:/static/device_description.xml,
 *          ssdpTerm:urn:schemas-upnp-org:device:AlarmDecoder:1,
 *          ip:XXXXXXX1
 *      ]
 *  ]
 *
 */
def getDevices() {
  if (!state.devices) {
    state.devices = [: ]
  }
  return state.devices
}

/**
 * Add all devices if triggered by the "Setup And Management" pages.
 */
def addExistingDevices() {
  logDebug("addExistingDevices: ${input_selected_devices}")

  // resubscribe just in case it was lost
  configureDeviceSubscriptions()

  //FIXME: Why? Maybe this returns [] or "" if multi select.
  def selected_devices = input_selected_devices
  if (selected_devices instanceof java.lang.String) {
    selected_devices = [selected_devices]
  } else {
    logDebug("addExistingDevices: FIXME not input_selected_devices not String")
  }

  selected_devices.each {
    dni->
      logDebug("addExistingDevices, getChildDevice(${dni})")

    def d = getChildDevice(dni)

    if (!d) {
      // Find the discovered device with a matching dni XXXXXXXX:XXXX
      def newDevice = \
        getDevices().find { k, v -> "${v.ip}:${v.port}" == dni
        }

      logDebug("addExistingDevices, devices.find=${newDevice}")

      if (newDevice) {
        // FIXME: Save DNI details for filtering
        // This needs to be reviewed.
        // We have this data already so why put into a state var
        state.ip = newDevice.value.ip
        state.port = newDevice.value.port
        state.hubId = newDevice.value.hubId

        // Set URN for the child device
        state.urn = convertHexToIP(state.ip) + ":" +
          convertHexToInt(state.port)

        logDebug("AlarmDecoder webapp urn ('${state.urn}') " +
          "hub ('${state.hubId}')")

        try {
          // Create device adding the URN to its data object
          d = addChildDevice(APPNAMESPACE,
            "AlarmDecoder network appliance",
            "${getDeviceKey()}",
            state.hubId,
            [
              name: "${getDeviceKey()}",
              label: "${guiname}",
              completedSetup: true,
              /* data associated with this AlarmDecoder */
              data: [
                // save mac address to update if IP / PORT change
                mac: newDevice.value.mac,
                ssdpUSN: newDevice.value.ssdpUSN,
                urn: state.urn,
                ssdpPath: newDevice.value.ssdpPath
              ]
            ]
          )

          // Set default device state to notready.
          d.sendEvent(
            name: "panel_state",
            value: "notready",
            isStateChange: true,
            displayed: true
          )
        } catch (e) {
          log.info "Error creating device root device ${guiname}"
        }
      }
    }

	def deviceIdx = 0
    // Add zone contact sensors if they do not exist.
    // asynchronous to avoid timeout. Apps can only run for 20 seconds or
    // it will be killed.
    for (def i = 0; i < inputContactSensorCount; i++) {
      logDebug("Adding virtual zone sensor ${deviceIdx}")
      // SmartThings we do out of band with callback
      if (isSmartThings()) {
        sendEvent(
          name: "addZone",
          value: "${deviceIdx+1}",
          data: [id:"${getDeviceKey()}:switch${deviceIdx+1}",type:"contact"]
        )
      }
      // Callbacks to local events seem to not work on HT
      else if (isHubitat()) {
        if (debug)
          log.warn("NOTE: Hubitat calling addZone directly")
        def evt = [value: "${deviceIdx+1}", data: [id:"${getDeviceKey()}:switch${deviceIdx+1}",type: "contact"]]
        addZone(evt)
      }
	  deviceIdx++
    }
    for (def i = 0; i < inputCODetector; i++) {
      logDebug("Adding virtual zone sensor ${deviceIdx}")
      // SmartThings we do out of band with callback
      if (isSmartThings()) {
        sendEvent(
          name: "addZone",
          value: "${deviceIdx+1}",
          data: [id:"${getDeviceKey()}:switch${deviceIdx+1}",type:"co"]
        )
      }
      // Callbacks to local events seem to not work on HT
      else if (isHubitat()) {
        if (debug)
          log.warn("NOTE: Hubitat calling addZone directly")
        def evt = [value: "${deviceIdx+1}", data: [id:"${getDeviceKey()}:switch${deviceIdx+1}",type: "co"]]
        addZone(evt)
      }
	  deviceIdx++
    }
    for (def i = 0; i < inputMotionDetector; i++) {
      logDebug("Adding virtual zone sensor ${deviceIdx}")
      // SmartThings we do out of band with callback
      if (isSmartThings()) {
        sendEvent(
          name: "addZone",
          value: "${deviceIdx+1}",
          data: [id:"${getDeviceKey()}:switch${deviceIdx+1}",type:"motion"]
        )
      }
      // Callbacks to local events seem to not work on HT
      else if (isHubitat()) {
        if (debug)
          log.warn("NOTE: Hubitat calling addZone directly")
        def evt = [value: "${deviceIdx+1}", data: [id:"${getDeviceKey()}:switch${deviceIdx+1}",type: "motion"]]
        addZone(evt)
      }
	  deviceIdx++
    }
    for (def i = 0; i < inputShockSensor; i++) {
      logDebug("Adding virtual zone sensor ${deviceIdx}")
      // SmartThings we do out of band with callback
      if (isSmartThings()) {
        sendEvent(
          name: "addZone",
          value: "${deviceIdx+1}",
          data: [id:"${getDeviceKey()}:switch${deviceIdx+1}",type:"shock"]
        )
      }
      // Callbacks to local events seem to not work on HT
      else if (isHubitat()) {
        if (debug)
          log.warn("NOTE: Hubitat calling addZone directly")
        def evt = [value: "${deviceIdx+1}", data: [id:"${getDeviceKey()}:switch${deviceIdx+1}",type: "shock"]]
        addZone(evt)
      }
	  deviceIdx++
    }
    for (def i = 0; i < inputSmokeDetector; i++) {
      logDebug("Adding virtual zone sensor ${deviceIdx}")
      // SmartThings we do out of band with callback
      if (isSmartThings()) {
        sendEvent(
          name: "addZone",
          value: "${deviceIdx+1}",
          data: [id:"${getDeviceKey()}:switch${deviceIdx+1}",type:"smoke"]
        )
      }
      // Callbacks to local events seem to not work on HT
      else if (isHubitat()) {
        if (debug)
          log.warn("NOTE: Hubitat calling addZone directly")
        def evt = [value: "${deviceIdx+1}", data: [id:"${getDeviceKey()}:switch${deviceIdx+1}",type: "smoke"]]
        addZone(evt)
      }
	  deviceIdx++
    }

    // do not create devices if testing. Real PITA to delete them
    // every time. ST needs to add a way to delete multiple devices at once.
    if (!NOCREATEDEV) {
      // Add Smoke Alarm sensors if it does not exist.
      def cd = \
        getChildDevice("${getDeviceKey()}:smokeAlarm")
      if (!cd) {
        try {
          def nd = \
            addChildDevice(
              APPNAMESPACE,
              "AlarmDecoder virtual smoke alarm",
              "${getDeviceKey()}:smokeAlarm",
              state.hubId,
              [
                name: "${getDeviceKey()}:smokeAlarm",
                label: "${sname} Smoke Alarm",
                completedSetup: true
              ]
            )
          nd.sendEvent(
            name: "smoke",
            value: "clear",
            isStateChange: true,
            displayed: false
          )
        } catch (e) {
          log.info "Error creating device: smokeAlarm"
        }
      } else {
        log.warn "addExistingDevices: Already found device " +
          "${getDeviceKey()}:smokeAlarm skipping."
      }

      // Add Arm Stay switch/indicator combo if it does not exist.
      addAD2VirtualDevices("armStay", "Stay", false, true, true)

      // Add Arm Away switch/indicator combo if it does not exist.
      addAD2VirtualDevices("armAway", "Away", false, true, true)

      // Add Arm Night switch/indicator combo if it does not exist.
      addAD2VirtualDevices("armNight", "Night", false, true, true)

      // Add Exit switch/indicator combo if it does not exist.
      addAD2VirtualDevices("exit", "Exit", false, true, true)

      // Add Chime Mode toggle switch/indicator combo if does not exist.
      addAD2VirtualDevices("chimeMode", "Chime", false, true, true)

      // Add Bypass status contact if it does not exist.
      addAD2VirtualDevices("bypass", "Bypass", false, false, true)

      // Add Ready status contact if it does not exist.
      addAD2VirtualDevices("ready", "Ready", false, false, true)

      // Add perimeter only status contact if it does not exist.
      addAD2VirtualDevices("perimeterOnly",
        "Perimeter Only", false, false, true)

      // Add entry delay off status contact if it does not exist.
      addAD2VirtualDevices("entryDelayOff",
        "Entry Delay Off", false, false, true)

      // Add virtual Alarm Bell switch/indicator combo if does not exist.
      addAD2VirtualDevices("alarmBell",
        "Alarm Bell", false, true, true)

      // Add FIRE Alarm switch/indicator combo if it does not exist.
      addAD2VirtualDevices("alarmFire", "Fire Alarm", false, true, true)

      // Add Panic Alarm switch/indicator combo if it does not exist.
      addAD2VirtualDevices("alarmPanic",
        "Panic Alarm", false, true, false)

      // Add AUX Alarm switch/indicator combo if it does not exist.
      addAD2VirtualDevices("alarmAUX", "AUX Alarm", false, true, false)

      // Add Disarm button if it does not exist.
      if (inputCreateDisarm) {
        addAD2VirtualDevices("disarm", "Disarm", false, true, false)
      }
    } else {
      log.warn "addExistingDevices: NOCREATEDEV enabled skip device creation."
    }

  }
}


/**
 * Add Virtual button and contact
 */
def addAD2VirtualDevices(name, label, initstate, createButton, createContact) {

  if (createButton) {
    // Add switch/indicator combo if it does not exist.
    def cd = \
      getChildDevice("${getDeviceKey()}:${name}")

    if (!cd) {
      try {
        def nd = \
          addChildDevice(
            APPNAMESPACE,
            "AlarmDecoder action button indicator",
            "${getDeviceKey()}:${name}",
            state.hubId,
            [
              name: "${getDeviceKey()}:${name}",
              label: "${sname} ${label}",
              completedSetup: true
            ]
          )
        nd.sendEvent(
          name: "switch",
          value: (initstate ? "on" : "off"),
          isStateChange: true,
          displayed: false
        )
      } catch (e) {
        log.info "Error creating device: ${name}"
      }
    } else {
      log.warn "addAD2VirtualDevices: Already found device " +
        "${getDeviceKey()}:${name} skipping."
      return
    }
  }

  if (createContact) {
    // Add contact status contact if it does not exit.
    def cd = \
      getChildDevice("${getDeviceKey()}:${name}Status")

    if (!cd) {
      try {
        def nd = \
          addChildDevice(
            APPNAMESPACE,
            "AlarmDecoder status indicator",
            "${getDeviceKey()}:${name}Status",
            state.hubId,
            [
              name: "${getDeviceKey()}:${name}Status",
              label: "${sname} ${label} Status",
              completedSetup: true
            ]
          )
        nd.sendEvent(
          name: "contact",
          value: (initstate ? "closed" : "open"),
          isStateChange: true,
          displayed: false
        )
      } catch (e) {
        log.info "Error creating device: ${getDeviceKey()}:${name}Status"
      }
    } else {
      log.warn "addAD2VirtualDevices: Already found device " +
        "${getDeviceKey()}:${name}Status skipping."
      return
    }
  }
}

/**
 * Configure subscriptions the virtual devices will send too.
 */
private def configureDeviceSubscriptions() {
  logDebug("configureDeviceSubscriptions")
  def device = getChildDevice("${getDeviceKey()}")
  if (!device) {
    log.error("configureDeviceSubscriptions: Could not find primary" +
      " device for '${getDeviceKey()}'.")
    return
  }

  /* Handle events sent from the AlarmDecoder network appliance device
   * to update virtual zones when they change.
   */
  subscribe(device, "zone-on", zoneOn, [filterEvents: false])
  subscribe(device, "zone-off", zoneOff, [filterEvents: false])

  // Subscribe to our own alarm status events from our primary device
  subscribe(device, "alarmStatus", alarmdecoderAlarmHandler,
    [filterEvents: false])

  // subscrib to smoke-set handler for updates
  subscribe(device, "smoke-set", smokeSet, [filterEvents: false])

  // subscribe to arm-away handler
  subscribe(device, "arm-away-set", armAwaySet, [filterEvents: false])

  // subscribe to arm-stay handler
  subscribe(device, "arm-stay-set", armStaySet, [filterEvents: false])
  
  // subscribe to arm-night handler
  subscribe(device, "arm-night-set", armNightSet, [filterEvents: false])

  // subscribe to chime handler
  subscribe(device, "chime-set", chimeSet, [filterEvents: false])

  // subscribe to exit handler
  subscribe(device, "exit-set", exitSet, [filterEvents: false])

  // subscribe to perimeter-only-set handler
  subscribe(device, "perimeter-only-set", perimeterOnlySet,
    [filterEvents: false])

  // subscribe to entry-deley-off-set handler
  subscribe(device, "entry-delay-off-set", entryDelayOffSet,
    [filterEvents: false])

  // subscribe to bypass handler
  subscribe(device, "bypass-set", bypassSet, [filterEvents: false])

  // subscribe to alarm bell handler
  subscribe(device, "alarmbell-set", alarmBellSet, [filterEvents: false])

  // subscribe to ready handler
  subscribe(device, "ready-set", readySet, [filterEvents: false])

  // subscribe to disarm handler
  subscribe(device, "disarm-set", disarmSet, [filterEvents: false])

  // subscribe to CID handler
  subscribe(device, "cid-set", cidSet, [filterEvents: false])

  // subscribe to RFX handler
  subscribe(device, "rfx-set", rfxSet, [filterEvents: false])

}

/**
 * Parse local network messages to a parsedEvent object.
 *
 * May be to UDP port 1900 for UPNP message or to TCP port 39500
 * for local network to hub push messages.
 *
 *  parsedEvent structure
 *  [
 *      devicetype:??,
 *      mac:XXXXXXXXXX02,
 *      networkAddress:??,
 *      deviceAddress:??,
 *      ssdpPath: "",
 *      ssdpUSN: "",
 *      ssdpTerm: "",
 *      headers: "The raw headers already base64 decoded",
 *      contenttype: "The parsed content type",
 *      body: "The raw body already base64 decoded."
 *  ]
 *
 */
private def parseEventMessage(String message) {

  logDebug "parseEventMessage: $message"

  def event = [: ]
  try {
    def parts = message.split(',')
    parts.each {
      part->
        part = part.trim()
      if (part.startsWith('devicetype:')) {
        def valueString = part.split(":")[1].trim()
        event.devicetype = valueString
      } else if (part.startsWith('mac:')) {
        def valueString = part.split(":")[1].trim()
        if (valueString) {
          event.mac = valueString
        }
      } else if (part.startsWith('requestId:')) {
        // If we made the request we will get the requestId of the host we
        // contacted. If we did not provide one in HubAction() then it will be
        // auto generated ex. c089d06f-ba3c-4baa-a1a4-950b9ffd372a
        part -= "requestId:"
        def valueString = part.trim()
        if (valueString) {
          event.requestId = valueString
        }
      } else if (part.startsWith('ip:')) {
        // If we made the request we will get the IP of the host we contacted.
        part -= "ip:"
        def valueString = part.trim()
        if (valueString) {
          event.ip = valueString
        }
      } else if (part.startsWith('port:')) {
        // If we made the request we will get the PORT of the host we contacted.
        part -= "port:"
        def valueString = part.trim()
        if (valueString) {
          event.port = valueString
        }
      } else if (part.startsWith('networkAddress:')) {
        def valueString = part.split(":")[1].trim()
        if (valueString) {
          event.ip = valueString
        }
      } else if (part.startsWith('deviceAddress:')) {
        def valueString = part.split(":")[1].trim()
        if (valueString) {
          event.port = valueString
        }
      } else if (part.startsWith('ssdpPath:')) {
        part -= "ssdpPath:"
        def valueString = part.trim()
        if (valueString) {
          event.ssdpPath = valueString
        }
      } else if (part.startsWith('ssdpUSN:')) {
        part -= "ssdpUSN:"
        def valueString = part.trim()
        if (valueString) {
          event.ssdpUSN = valueString
        }
      } else if (part.startsWith('ssdpTerm:')) {
        part -= "ssdpTerm:"
        def valueString = part.trim()
        if (valueString) {
          event.ssdpTerm = valueString
        }
      } else if (part.startsWith('headers:')) {
        part -= "headers:"
        def valueString = part.trim()
        if (valueString) {
          if (parse_headers) {
            /*
            // Testing parsing full headers
            def headers = [:]
            def str = new String(valueString.decodeBase64())
            str.eachLine { line, lineNumber ->
                if (lineNumber == 0) {
                    headers.status = line
                    return
                }
                headers << stringToMap(line)
            }
            event.headers = headers
            */
          } else {
            // decode the headers.
            event.headers = new String(valueString.decodeBase64())
            // extract the content type.
            event.contenttype =
              (event.headers =~ /Content-Type:.*/) ?
              (event.headers =~ /Content-Type:.*/)[0] : null
          }
        }
      } else if (part.startsWith('body:')) {
        part -= "body:"
        def valueString = part.trim()
        if (valueString) {
          event.body = new String(valueString.decodeBase64())
        }
      }
    }
  } catch (Exception e) {
    log.error("exception ${e} in parseEventMessage parsing: ${message}")
  }

  // return the parsedEvent
  return event
}

/**
 * Send a request for the description.xml For every known AlarmDecoder
 * we have discovered that is not verified.
 */
def verifyAlarmDecoders() {
  def devices = getDevices().findAll {
    it?.value?.verified != true
  }

  if (devices) {
    log.warn "verifyAlarmDecoders: UNVERIFIED Decoders!: $devices"
  }

  devices.each {
    if (it?.value?.ssdpPath?.contains("xml")) {
      verifyAlarmDecoder(
        (it?.value?.ip + ":" + it?.value?.port),
        it?.value?.ssdpPath
      )
    } else {
      log.warn("verifyAlarmDecoders: invalid ssdpPath not an xml file")
    }
  }
}

/**
 * Send a GET request from HUB to the AlarmDecoder for its descrption.xml file
 */
def verifyAlarmDecoder(String DNI, String ssdpPath) {
  sendVerify(DNI, ssdpPath)
}

/**
 * Convert from internal format networkAddress:C0A8016F to a real IP address
 * string ex. 192.168.1.111
 */
private String convertHexToIP(hex) {
  [convertHexToInt(hex[0..1]),
    convertHexToInt(hex[2..3]),
    convertHexToInt(hex[4..5]),
    convertHexToInt(hex[6..7])
  ].join(".")
}

/**
 * Convert from ip to internal format C0A8016F
 * FIXME: Needs more groovy.
 */
private String convertIPToHex(ip) {
  def parts = ip.split(".")
  parts[0] = Integer.toHexString(parts[0])
  parts[1] = Integer.toHexString(parts[1])
  parts[2] = Integer.toHexString(parts[2])
  parts[3] = Integer.toHexString(parts[3])
  return parts.join("")
}

/**
 * convert hex encoded string to integer
 */
private Integer convertHexToInt(hex) {
  Integer.parseInt(hex, 16)
}

/**
 * return a device key appropriate for the platform
 */
private String getDeviceKey() {
  def key = ""
  if (isSmartThings())
    key = "${state.ip}:${state.port}"
  else if (isHubitat())
    key = "a${state.ip}"

  return key
}

private String getDeviceKey(ip, port) {
  def key = ""
  if (isSmartThings())
    key = "${ip}:${port}"
  else if (isHubitat())
    key = "${ip}"

  return key
}

/**
 * return this hubs URN:XXX.XXX.XXX.XXX:YYYY
 * X = IP
 * Y = Port
 */
private String getHubURN() {
  def urn = null
  if (isSmartThings()) {
    def hub = location.hubs[0]
    def ip = hub.localIP
    def port = hub.localSrvPortTCP
    urn = "${ip}:${port}"
  } else if (isHubitat()) {
    def hub = location.hubs[0]
    def ip = hub.getDataValue("localIP")
    def port = hub.getDataValue("localSrvPortTCP")
    urn = "${ip}:${port}"
  }
  return urn
}

/**
 * build a URI host address of the AlarmDecoder web appliance for web requests.
 *  ex. AABBCCDD:XXXX -> 192.168.1.1:5000
 */
private getHostAddressFromDNI(d) {
  def ip = ""
  def port = ""
  if (d) {
    def parts = d.split(":")
    if (parts.size() == 2) {
      ip = convertHexToIP(parts[0])
      port = convertHexToInt(parts[1])
    }
  }
  return ip + ":" + port
}

/**
 * return a device network name rightmost data Z
 * SmartThings AABBCCDD:XXXX:ZZZZZZZZZ
 * Hubitat AABBCCDD:ZZZZZZZZZ
 */
private getDeviceNamePart(d) {
  def result = ""
  if (isSmartThings()) {
    result = d.deviceNetworkId.split(":")[2].trim()
  } else if (isHubitat()) {
    result = d.deviceNetworkId.split(":")[1].trim()
  }
  return result
}

/**
 * Send a state change to an AD2 virtual device adjusting it to
 * the devices actual capabilities and inverting if preferred.
 *
 * ad2d: The device handle.
 * state: [on, off]
 * Capabilities: [Switch]
 *   Default off = off, on(Alerting) = on
 * Capabilities: [Contact Sensor]
 *   Default close = off, open(Alerting) = on
 * Capabilities: [Smoke Detector]
 *   Default clear = off, detected(Alerting) = on
 *
 */
def _sendEventTranslate(ad2d, state, stateChange = true) {

  // Grab the devices preferences for inverting
  def invert = (ad2d.device.getDataValue("invert") == "true" ? true : false)

  // send a switch event if its a [Switch]
  // Default off = Off, on(Alerting) = On
  if (ad2d.hasCapability("Switch")) {
    // convert: Any matches is ON(true) no match is OFF(false)
    def sval = ((state == "on") ? true : false)

    // invert: If device has invert attribute then invert signal
    sval = (invert ? !sval : sval)

    // send 'switch' event
    ad2d.sendEvent(
      name: "switch",
      value: (sval ? "on" : "off"),
      isStateChange: stateChange,
      filtered: true
    )
  }

  // send a 'contact' event if its a [Contact Sensor]
  // Default close = Off, open(Alerting) = On
  if (ad2d.hasCapability("Contact Sensor")) {
    // convert: Any matches is ON(true) no match is OFF(false)
    def sval = ((state == "on") ? true : false)

    // invert: If device has invert attribute then invert signal
    sval = (invert ? !sval : sval)

    // send switch event
    ad2d.sendEvent(
      name: "contact",
      value: (sval ? "open" : "closed"),
      isStateChange: stateChange,
      filtered: true
    )
  }

  // send a 'motion' event if its a [Motion Sensor]
  // Default inactive = Off, active(Alerting) = On
  if (ad2d.hasCapability("Motion Sensor")) {
    // convert: Any matches is ON(true) no match is OFF(false)
    def sval = ((state == "on") ? true : false)

    // invert: If device has invert attribute then invert signal
    sval = (invert ? !sval : sval)

    // send switch event
    ad2d.sendEvent(
      name: "motion",
      value: (sval ? "active" : "inactive"),
      isStateChange: stateChange,
      filtered: true
    )
  }

  // send a 'shock' event if its a [Shock Sensor]
  // Default clear = Off, detected(Alerting) = On
  if (ad2d.hasCapability("Shock Sensor")) {
    // convert: Any matches is ON(true) no match is OFF(false)
    def sval = ((state == "on") ? true : false)

    // invert: If device has invert attribute then invert signal
    sval = (invert ? !sval : sval)

    // send switch event
    ad2d.sendEvent(
      name: "shock",
      value: (sval ? "detected" : "clear"),
      isStateChange: stateChange,
      filtered: true
    )
  }

  // send a 'carbonMonoxide' event if its a [Carbon Monoxide Detector]
  // Default clear = Off, detected(Alerting) = On
  if (ad2d.hasCapability("Carbon Monoxide Detector")) {
    // convert: Any matches is ON(true) no match is OFF(false)
    def sval = ((state == "on") ? true : false)

    // invert: If device has invert attribute then invert signal
    sval = (invert ? !sval : sval)

    // send switch event
    ad2d.sendEvent(
      name: "carbonMonoxide",
      value: (sval ? "detected" : "clear"),
      isStateChange: stateChange,
      filtered: true
    )
  }

  // send a 'smoke' event if its a [Smoke Detector]
  // Default clear = Off, detected(Alerting) = On
  if (ad2d.hasCapability("Smoke Detector")) {
    // convert: Any matches is ON(true) no match is OFF(false)
    def sval = ((state == "on") ? true : false)

    // invert: If device has invert attribute then invert signal
    sval = (invert ? !sval : sval)

    // send switch event
    ad2d.sendEvent(
      name: "smoke",
      value: (sval ? "detected" : "clear"),
      isStateChange: stateChange,
      filtered: true
    )
  }
}

def logDebug(msg) {
  if (settings?.debugOutput) {
    log.debug msg
  }
}

def logTrace(msg) {
  if (settings?.traceOutput) {
    log.trace msg
  }
}