/**
 *  AlarmDecoder Service Manager
 *
 *  Copyright 2016-2019 Nu Tech Software Solutions, Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 *
 * Version 1.0.0 - Scott Petersen - Initial design and release
 * Version 2.0.0 - Sean Mathews <coder@f34r.com> - Changed to use UPNP Push API in AD2 web app
 * Version 2.0.1 - Sean Mathews <coder@f34r.com> - Adding CID device management support.
 * Version 2.0.2 - Sean Mathews <coder@f34r.com> - Fixed app 20 second max timeout. AddZone is now async, added more zones.
 * Version 2.0.3 - Sean Mathews <coder@f34r.com> - Improved/fixed issues with previous app 20 timeout after more testing.
 * Version 2.0.4 - Sean Mathews <coder@f34r.com> - Support multiple instances of service by changing unique ID message filters by MAC.
 * Version 2.0.5 - Sean Mathews <coder@f34r.com> - Add switch to create Disarm button.
 * Version 2.0.6 - Sean Mathews <coder@f34r.com> - Compatiblity between ST and HT. Still requires some manual code edits but it will be minimal.
 * Version 2.0.7 - Sean Mathews <coder@f34r.com> - Add RFX virtual device management to track Ademco 5800 wireless or VPLEX sensors directly.
 * Version 2.0.8 - Sean Mathews <coder@f34r.com> - Added Exit button. New flag from AD2 state to detect exit state. Added rebuild devices button.
 * Version 2.0.9 - Sean Mathews <coder@f34r.com> - Split some devices from a single combined Momentary to a Momentary and a Contact for
 *                                                 easy access by other systems.
 * Version 2.1.0 - Sean Mathews <coder@f34r.com> - Modified virtual RFX devices to allow inverting signal and capabilities detection.
 *                                                 Set the RFX device type to AlarmDecoder virtual smoke and it will report as a smoke detector.
 *                                                 Modified sending of events creating a wrapper to invert and adjust to the device type.
 *
 */

/*
 * global support
 */
import groovy.transform.Field
@Field APPNAMESPACE = "alarmdecoder"

/*
 * System Settings
 */
@Field debug = false
@Field max_sensors = 20
@Field nocreatedev = false
@Field create_disarm = true
// You will also need to comment out and comment code in sendVerfy and sendDiscover below.

/*
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

/*
 * sendDiscover sends a discovery message to the HUB.
 * Leave comments out for the HUB type being used.
 */
def sendDiscover() {
    // Request HUB send out a UpNp broadcast discovery messages on the local network
    def haobj
    // Comment out the next line if we are using Hubitat
    if (isSmartThings()) {
        // Comment out the next line if we are using Hubitat
        haobj = new physicalgraph.device.HubAction("lan discovery urn:schemas-upnp-org:device:AlarmDecoder:1", physicalgraph.device.Protocol.LAN)
    }
    else if (isHubitat()) {
        // Comment out the next line if we are using SmartThings
        //haobj = new hubitat.device.HubAction("lan discovery urn:schemas-upnp-org:device:AlarmDecoder:1", hubitat.device.Protocol.LAN)
    }
	sendHubCommand(haobj)
}

/*
 * sendVerify sends a message to the HUB.
 * Leave comments out for the HUB type being used.
 */
def sendVerify(deviceNetworkID, ssdpPath) {
  String ip = getHostAddressFromDNI(deviceNetworkId)
  if (debug) log.debug("verifyAlarmDecoder: $deviceNetworkId ssdpPath: ${ssdpPath} ip: ${ip}")

  if(isSmartThings()) {
      // Comment out the next line if we are using Hubitat
      def result = new physicalgraph.device.HubAction([method: "GET", path: ssdpPath, headers: [Host: ip, Accept: "*/*"]], deviceNetworkId)
  }
  else if (isHubitat()) {
      // Comment out the next line if we are using SmartThings
      //def result = new hubitat.device.HubAction([method: "GET", path: ssdpPath, headers: [Host: ip, Accept: "*/*"]], deviceNetworkId)
  }
  sendHubCommand(result)
}

definition(
    name: "AlarmDecoder service${idname}",
    namespace: APPNAMESPACE,
    author: "Nu Tech Software Solutions, Inc.",
    description: "AlarmDecoder (Service Manager)",
    category: "My Apps",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    singleInstance: true) { }

preferences {
    page(name: "page_main", title: titles("page_main"), install: false, uninstall: false, refreshInterval: 5)
    page(name: "page_discover_devices", title: titles("page_discover_devices"), content: "page_discover_devices", install: true, uninstall: false)
    page(name: "page_remove_all", title: titles("page_remove_all"), content: "page_remove_all", install: false, uninstall: false)
    page(name: "page_rebuild_all", title: titles("page_rebuild_all"), content: "page_rebuild_all", install: false, uninstall: false)
    page(name: "page_cid_management", title: titles("page_cid_management"), content: "page_cid_management", install: false, uninstall: false)
    page(name: "page_add_new_cid", title: titles("page_add_new_cid"), content: "page_add_new_cid", install: false, uninstall: false)
    page(name: "page_add_new_cid_confirm", title: titles("page_add_new_cid_confirm", buildcidlabel()), content: "page_add_new_cid_confirm", install: false, uninstall: false)
    page(name: "page_remove_selected_cid", title: titles("page_remove_selected_cid"), content: "page_remove_selected_cid", install: false, uninstall: false)
    page(name: "page_rfx_management", title: titles("page_rfx_management"), content: "page_rfx_management", install: false, uninstall: false)
    page(name: "page_add_new_rfx", title: titles("page_add_new_rfx"), content: "page_add_new_rfx", install: false, uninstall: false)
    page(name: "page_add_new_rfx_confirm", title: titles("page_add_new_rfx_confirm", buildcidlabel()), content: "page_add_new_rfx_confirm", install: false, uninstall: false)
    page(name: "page_remove_selected_rfx", title: titles("page_remove_selected_rfx"), content: "page_remove_selected_rfx", install: false, uninstall: false)
}

/*
 * Localization strings
 */

// string table for titles
def titles(String name, Object... args) {
  def page_titles = [
    /*  Main */
    "page_main": "${lname} setup and management",

    /* Discover */
    "page_discover_devices": "Install ${lname} service",
    "input_selected_devices": "Select device(s) (%s found)",
    "monIntegration": "Integrate with Smart Home Monitor?",
    "monChangeStatus": "Automatically change Monitor(SHM|HSM) status when armed or disarmed?",
    "defaultSensorToClosed": "Default zone sensors to closed?",

    /* Remove All */
    "page_remove_all": "Remove all ${lname} devices",
    "confirm_remove_all": "Confirm remove all",
    "href_refresh_devices": "Send UPNP discovery",
    "info_remove_all_a": "Removed all child devices.",
    "info_remove_all_b": "This will attempt to remove all child devices. This may fail if the device is in use. If it fails review the log and manually remove the usage. Press back to continue.",

    /* Virtual Device Management */
    "page_vid_management": "AD2 Virtual device management",
    "page_rebuild_all": "Rebuild virtual devices",
    "confirm_rebuild_all": "Confirm rebuild all",
    "info_rebuild_all_a": "Rebuilt all child devices.",
    "info_rebuild_all_b": "This will attempt to rebuild all child devices. Review the log and manually remove the usage. Press back to continue.",

    /* CID Management */
    "page_cid_management": "Contact ID device management",
    "page_add_new_cid": "Add new CID virtual switch",
    "page_add_new_cid_confirm": "Add new CID switch : %s",
    "page_remove_selected_cid": "Remove selected virtual switches",
    "info_page_remove_selected_cid": "Attempted to remove selected devices. This may fail if the device is in use. If it fails review the log and manually remove the usage. Press back to continue.",
    "info_add_new_cid_confirm": "Attempted to add new CID device. This may fail if the device is in use. If it fails review the log. Press back to continue.",
    "section_cid_value": "Build CID Value(USER/ZONE) : %s",
    "section_cid_partition": "Select the CID partition",
    "section_cid_names": "Device Name and Label",
    "section_build_cid": "Build Device Name :",
    "input_cid_name": "Enter the new device name or blank for auto",
    "input_cid_label": "Enter the new device label or blank for auto",
    "input_cid_devices": "Remove installed CID virutal switches",
    "input_cid_number": "Select the CID number for this device",
    "input_cid_value": "Zero PAD 3 digit User,Zone or simple regex pattern ex. '001' or '???'",
    "input_cid_partition": "Enter the partition or 0 for system",
    "input_cid_number_raw": "Enter CID # or simple regex pattern",

    /* RFX Management */
    "page_rfx_management": "RFX device management",
    "page_add_new_rfx": "Add new RFX virtual switch",
    "page_add_new_rfx_confirm": "Add new RFX switch : %s",
    "page_remove_selected_rfx": "Remove selected virtual switches",
    "section_build_rfx": "Build Device Name :",
    "input_rfx_name": "Enter the new device name or blank for auto",
    "input_rfx_label": "Enter the new device label or blank for auto",
    "input_rfx_sn": "Enter Serial # or simple regex pattern",
    "input_rfx_supv": "Supervision bit: Enter 1 to watch or ? to ignore",
    "input_rfx_bat": "Battery: Enter 1 to monitor or ? to ignore",
    "input_rfx_loop0": "Loop 0: Enter 1 to monitor or ? to ignore",
    "input_rfx_loop1": "Loop 1: Enter 1 to monitor or ? to ignore",
    "input_rfx_loop2": "Loop 2: Enter 1 to monitor or ? to ignore",
    "input_rfx_loop3": "Loop 3: Enter 1 to monitor or ? to ignore",
  ]
  if (args)
      return String.format(page_titles[name], args)
  else
      return page_titles[name]
}

// string table for descriptions
def descriptions(name, Object... args) {
  def element_descriptions = [
    "href_refresh_devices": "Tap to select",
    "href_discover_devices": "Tap to discover and install your ${lname} Appliance",
    "href_rebuild_all": "Tap to rebuild all ${lname} virtual devices",
    "href_remove_all": "Tap to remove all ${lname} virtual devices",
    "href_cid_management": "Tap to manage CID virtual switches",
    "href_remove_selected_cid": "Tap to remove selected virtual switches",
    "href_add_new_cid": "Tap to add new CID switch",
    "href_add_new_cid_confirm": "Tap to confirm and add",
    "input_cid_devices": "Tap to select",
    "input_cid_number": "Tap to select",
    "href_rfx_management": "Tap to manage RFX virtual switches",
    "href_remove_selected_rfx": "Tap to remove selected virtual switches",
    "href_add_new_rfx": "Tap to add new RFX switch",
    "href_add_new_rfx_confirm": "Tap to confirm and add",
    "input_rfx_devices": "Tap to select",
    "input_rfx_number": "Tap to select",
  ]
  if (args)
      return String.format(element_descriptions[name],args)
  else
      return element_descriptions[name]
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

/**
 * Section note on saving and < icons
 */
def section_no_save_note() {section("Please note") { paragraph "Do not use the \"Save\" buttons on this page. Use the \"<\" to exit or go back."}}

/**
 * our main page dynamicly generated to control page based upon logic.
 */
def page_main() {

    // make sure we are listening to all network subscriptions
    initSubscriptions()

    // send out a UPNP broadcast discovery
    discover_alarmdecoder()

    // see if we are already installed
    def foundMsg = ""
    def children = getChildDevices()
    if (children) foundMsg = "**** AlarmDecoder already installed ${children.size()}****"

    dynamicPage(name: "page_main") {
        if (!children) {
            section("") {
                href(name: "href_discover", required: false, page: "page_discover_devices", title: titles("page_discover_devices"), description: descriptions("href_discover_devices"))
            }
        } else {
            section("") {
                paragraph(foundMsg)
            }
            section("") {
                href(name: "href_rebuild_all", required: false, page: "page_rebuild_all", title: titles("page_rebuild_all"), description: descriptions("href_rebuild_all"))
            }
            section("") {
                href(name: "href_remove_all", required: false, page: "page_remove_all", title: titles("page_remove_all"), description: descriptions("href_remove_all"))
            }
            section("") {
                href(name: "href_cid_management", required: false, page: "page_cid_management", title: titles("page_cid_management"), description: descriptions("href_cid_management"))
            }
            section("") {
                href(name: "href_rfx_management", required: false, page: "page_rfx_management", title: titles("page_rfx_management"), description: descriptions("href_rfx_management"))
            }
        }
    }
}

/**
 * Page page_cid_management generator.
 */
def page_cid_management() {
    // TODO: Find a way to clear our current values on loading page
    return dynamicPage(name: "page_cid_management") {
        def found_devices = []
        getAllChildDevices().each { device ->
            if (device.deviceNetworkId.contains(":CID-"))
            {
                found_devices << device.deviceNetworkId.split(":")[2].trim()
            }
        }
        section("") {
            if (found_devices.size()) {
                input "input_cid_devices", "enum", required: false, multiple: true, options: found_devices, title: titles("input_cid_devices"), description: descriptions("input_cid_devices"), submitOnChange: true
                if (input_cid_devices) {
                    href(name: "href_remove_selected_cid", required: false, page: "page_remove_selected_cid", title: titles("page_remove_selected_cid"), description: descriptions("href_remove_selected_cid"), submitOnChange: true)
                }
            }
        }
        section("") {
            href(name: "href_add_new_cid", required: false, page: "page_add_new_cid", title: titles("page_add_new_cid"), description: descriptions("href_add_new_cid"))
        }
        section_no_save_note()
    }
}

/**
 * Page page_remove_selected_cid generator
 */
def page_remove_selected_cid() {
    def errors = []
    getAllChildDevices().each { device ->
        if (device.deviceNetworkId.contains(":CID-"))
        {
            // Only remove the one that matches our list
            def device_name = device.deviceNetworkId.split(":")[2].trim()
            def d = input_cid_devices.find{ it == device_name }
            if (d)
            {
                log.trace("removing CID device ${device.deviceNetworkId}")
                try {
                    deleteChildDevice(device.deviceNetworkId)
                    input_cid_devices.remove(device_name)
                    errors << "Success removing " + device_name
                }
                catch (e) {
                    log.error "There was an error (${e}) when trying to delete the child device"
                    errors << "Error removing " + device_name
                }
            }
        }
    }
    return dynamicPage(name: "page_remove_selected_cid") {
        section("") {
            paragraph titles("info_page_remove_selected_cid")
            errors.each { error ->
                paragraph(error)
            }
        }
    }
}

/**
 * Page page_add_new_cid generator
 */
def page_add_new_cid() {
    // list of some of the CID #'s and descriptions.
    // 000 will trigger a manual input of the CID number.
    def cid_numbers = [  "0": "000 - Other / Custom",
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
                       "4[0,4]1": "4[0,4]1 - Arm Stay or Away OPEN/CLOSE"]

    return dynamicPage(name: "page_add_new_cid") {
        // show pre defined CID number templates to select from
        section("") {
            paragraph titles("section_build_cid", buildcid())
            input "input_cid_number", "enum", required: true, submitOnChange: true, multiple: false, title: titles("input_cid_number"), description: descriptions("input_cid_number"), options: cid_numbers
        }
        // if a CID entry is selected then check the value if it is "0" to show raw input section
        if (input_cid_number) {
            if (input_cid_number == "0") {
                section {
                    input(name: "input_cid_number_raw", type: "text", required: true, defaultValue: 110, submitOnChange: true, title: titles("input_cid_number_raw"))
                }
            }
            section {
                paragraph titles("section_cid_value", buildcidvalue())
                input(name: "input_cid_value", type: "text", required: true, defaultValue: "???",submitOnChange: true, title: titles("input_cid_value"))
            }
            section {
                paragraph titles("section_cid_partition")
                input(name: "input_cid_partition", type: "number", required: false, defaultValue: 1, submitOnChange: true, title: titles("input_cid_partition"))
            }
            section {
                paragraph titles("section_cid_names")
                input(name: "input_cid_name", type: "text", required: false, defaultValue: '', submitOnChange: true, title: titles("input_cid_name"))
                input(name: "input_cid_label", type: "text", required: false, defaultValue: '', submitOnChange: true, title: titles("input_cid_label"))
            }
            // If input_cid_number or input_cid_number_raw have a value
            if ( (input_cid_number && (input_cid_number != "0")) || (input_cid_number_raw)) {
                section(""){ href(name: "href_add_new_cid_confirm", required: false, page: "page_add_new_cid_confirm", title: titles("page_add_new_cid_confirm", buildcidlabel()+"("+buildcidnetworkid()+")"), description: descriptions("href_add_new_cid_confirm"))}
            }
            section_no_save_note()
        }
    }
}

/**
 * Helper to build a final CID value from inputs
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
    def newcid =  buildcid()

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
    def newcidlabel =  buildcidlabel()
    def newcidname = buildcidname()
    def newcidnetworkid = buildcidnetworkid()
    def cv = input_cid_value
    def pt = input_cid_partition.toInteger()

    // Add virtual CID switch if it does not exist.
    def d = getChildDevice("${getDeviceKey()}:${newcidlabel}")
    if (!d)
    {
        def nd = addChildDevice(APPNAMESPACE, "AlarmDecoder action button indicator", "${getDeviceKey()}:${newcidnetworkid}", state.hub,
                                [name: "${getDeviceKey()}:${newcidname}", label: "${sname} ${newcidlabel}", completedSetup: true])
        nd.sendEvent(name: "switch", value: "off", isStateChange: true, displayed: false)
        errors << "Success adding ${newcidlabel}"
    } else {
        errors << "Error adding ${newcidlabel}: Exists"
    }

    return dynamicPage(name: "page_add_new_cid_confirm") {
        section("") {
            paragraph titles("info_add_new_cid_confirm")
            errors.each { error ->
                paragraph(error)
            }
        }
        section_no_save_note()
    }
}

/**
 * Page page_rfx_management generator.
 */
def page_rfx_management() {
    // TODO: Find a way to clear our current values on loading page
    return dynamicPage(name: "page_rfx_management") {
        def found_devices = []
        getAllChildDevices().each { device ->
            if (device.deviceNetworkId.contains(":RFX-"))
            {
                found_devices << device.deviceNetworkId.split(":")[2].trim()
            }
        }
        section("") {
            if (found_devices.size()) {
                input "input_rfx_devices", "enum", required: false, multiple: true, options: found_devices, title: titles("input_rfx_devices"), description: descriptions("input_rfx_devices"), submitOnChange: true
                if (input_rfx_devices) {
                    href(name: "href_remove_selected_rfx", required: false, page: "page_remove_selected_rfx", title: titles("page_remove_selected_rfx"), description: descriptions("href_remove_selected_rfx"), submitOnChange: true)
                }
            }
        }
        section("") {
            href(name: "href_add_new_rfx", required: false, page: "page_add_new_rfx", title: titles("page_add_new_rfx"), description: descriptions("href_add_new_rfx"))
        }
        section_no_save_note()
    }
}

/**
 * Page page_remove_selected_rfx generator
 */
def page_remove_selected_rfx() {
    def errors = []
    getAllChildDevices().each { device ->
        if (device.deviceNetworkId.contains(":RFX-"))
        {
            // Only remove the one that matches our list
            def device_name = device.deviceNetworkId.split(":")[2].trim()
            def d = input_rfx_devices.find{ it == device_name }
            if (d)
            {
                log.trace("removing RFX device ${device.deviceNetworkId}")
                try {
                    deleteChildDevice(device.deviceNetworkId)
                    input_rfx_devices.remove(device_name)
                    errors << "Success removing " + device_name
                }
                catch (e) {
                    log.error "There was an error (${e}) when trying to delete the child device"
                    errors << "Error removing " + device_name
                }
            }
        }
    }
    return dynamicPage(name: "page_remove_selected_rfx") {
        section("") {
            paragraph titles("info_page_remove_selected_rfx")
            errors.each { error ->
                paragraph(error)
            }
        }
        section_no_save_note()
    }
}

/**
 * Page page_add_new_rfx generator
 */
def page_add_new_rfx() {

    return dynamicPage(name: "page_add_new_rfx") {
        section("") {
            paragraph titles("section_build_rfx")
        }
        section {
            paragraph titles("section_rfx_names")
            input(name: "input_rfx_name", type: "text", required: false, defaultValue: '', submitOnChange: true, title: titles("input_rfx_name"))
            input(name: "input_rfx_label", type: "text", required: false, defaultValue: '', submitOnChange: true, title: titles("input_rfx_label"))
        }
        section {
            input(name: "input_rfx_sn", type: "text", required: true, defaultValue: '000000', submitOnChange: true, title: titles("input_rfx_sn"))
            input(name: "input_rfx_bat", type: "text", required: true, defaultValue: '?', submitOnChange: true, title: titles("input_rfx_bat"))
            input(name: "input_rfx_supv", type: "text", required: true, defaultValue: '?', submitOnChange: true, title: titles("input_rfx_supv"))
            input(name: "input_rfx_loop0", type: "text", required: true, defaultValue: '1', submitOnChange: true, title: titles("input_rfx_loop0"))
            input(name: "input_rfx_loop1", type: "text", required: true, defaultValue: '?', submitOnChange: true, title: titles("input_rfx_loop1"))
            input(name: "input_rfx_loop2", type: "text", required: true, defaultValue: '?', submitOnChange: true, title: titles("input_rfx_loop2"))
            input(name: "input_rfx_loop3", type: "text", required: true, defaultValue: '?', submitOnChange: true, title: titles("input_rfx_loop3"))
        }
        section(""){ href(name: "href_add_new_rfx_confirm", required: false, page: "page_add_new_rfx_confirm", title: titles("page_add_new_rfx_confirm", buildrfxlabel()+"("+buildrfxnetworkid()+")"), description: descriptions("href_add_new_rfx_confirm"))}
        section_no_save_note()
    }
}

/**
 * Helper to build a final RFX value from inputs
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
    def newrfx =  buildrfx()

    def cv = buildrfxvalue()
    return "RFX-${newrfx}-${cv}"
}

def buildrfxvalue() {
    def rfxval = "${input_rfx_bat}-${input_rfx_supv}-${input_rfx_loop0}-${input_rfx_loop1}-${input_rfx_loop2}-${input_rfx_loop3}"
    return rfxval
}

/**
 * Page page_add_new_rfx_confirm generator.
 */
def page_add_new_rfx_confirm() {
    def errors = []
    // get the RFX value
    def newrfxlabel =  buildrfxlabel()
    def newrfxname = buildrfxname()
    def newrfxnetworkid = buildrfxnetworkid()
    def cv = input_rfx_value

    // Add virtual RFX switch if it does not exist.
    def d = getChildDevice("${getDeviceKey()}:${newrfxlabel}")
    if (!d)
    {
        def nd = addChildDevice(APPNAMESPACE, "AlarmDecoder action button indicator", "${getDeviceKey()}:${newrfxnetworkid}", state.hub,
                                [name: "${getDeviceKey()}:${newrfxname}", label: "${sname} ${newrfxlabel}", completedSetup: true])
        nd.sendEvent(name: "switch", value: "off", isStateChange: true, displayed: false)
        errors << "Success adding ${newrfxlabel}"
    } else {
        errors << "Error adding ${newrfxlabel}: Exists"
    }

    return dynamicPage(name: "page_add_new_rfx_confirm") {
        section("") {
            paragraph titles("info_add_new_rfx_confirm")
            errors.each { error ->
                paragraph(error)
            }
        }
        section_no_save_note()
    }
}

/**
 * Page page_rebuild_all generator.
 */
def page_rebuild_all(params) {
    def message = ""

    return dynamicPage(name: "page_rebuild_all") {
        if (params?.confirm) {
            // Call rebuild device function here
            addExistingDevices()
            message = titles("info_rebuild_all_a")
        } else {
            section("") {
                href(name: "href_confirm_rebuild_all_devices", title: titles("confirm_rebuild_all"), description: descriptions("href_rebuild_devices"), required: false, page: "page_rebuild_all", params: [confirm: true])
            }
            message = titles("info_rebuild_all_b")
        }
        section("") {
            paragraph message
        }
    }
}

/**
 * Page page_remove_all generator.
 */
def page_remove_all(params) {
    def message = ""

    return dynamicPage(name: "page_remove_all") {
        if (params?.confirm) {
            uninstalled()
            message = titles("info_remove_all_a")
        } else {
            section("") {
                href(name: "href_confirm_remove_all_devices", title: titles("confirm_remove_all"), description: descriptions("href_refresh_devices"), required: false, page: "page_remove_all", params: [confirm: true])
            }
            message = titles("info_remove_all_b")
        }
        section("") {
            paragraph message
        }
    }
}

/**
 * Page page_discover_devices generator. Called periodically to refresh content of the page.
 */
def page_discover_devices() {

    // send out UPNP discovery messages and watch for responses
    discover_alarmdecoder()

    // build list of currently known AlarmDecoder parent devices
    def found_devices = [:]
    def options = state.devices.each { k, v ->
        if (debug) log.debug "page_discover_devices: ${v}"
        def ip = convertHexToIP(v.ip)
        found_devices["${v.ip}:${v.port}"] = "AlarmDecoder @ ${ip}"
    }

    // How many do we have?
    def numFound = found_devices.size() ?: 0

    return dynamicPage(name: "page_discover_devices") {
        section("Devices") {
            input "input_selected_devices", "enum", required: true, title: titles("input_selected_devices",numFound), multiple: true, options: found_devices
            // Allow user to force a new UPNP discovery message
            href(name: "href_refresh_devices", title: titles("href_refresh_devices"), description: descriptions("href_refresh_devices"), required: false, page: "page_discover_devices")
        }
        section("Smart Home Monitor Integration") {
            input(name: "monIntegration", type: "bool", defaultValue: true, title: titles("monIntegration"))
            input(name: "monChangeStatus", type: "bool", defaultValue: true, title: titles("monChangeStatus"))
        }
        section("Zone Sensors") {
            input(name: "defaultSensorToClosed", type: "bool", defaultValue: true, title: titles("defaultSensorToClosed"))
        }
    }
}

/*** Pre-Defined callbacks ***/

/**
 *  installed()
 */
def installed() {
    log.trace "installed"
    if (debug) log.debug "Installed with settings: ${settings}"

    // initialize everything
    initialize()
}

/**
 * updated()
 */
def updated() {
    log.trace "updated"
    if (debug) log.debug "Updated with settings: ${settings}"

    // re initialize everything
    initialize()
}

/**
 * uninstalled()
 */
def uninstalled() {
    log.trace "uninstalled"

    // disable all scheduling and subscriptions
    unschedule()

    // remove all the devices and children
    def devices = getAllChildDevices()
    devices.each {
        try {
            log.debug "deleting child device: ${it.deviceNetworkId}"
            deleteChildDevice(it.deviceNetworkId)
        }
        catch(Exception e) {
            log.trace("exception while uninstalling: ${e}")
        }
    }
}

/**
 * initialize called upon update and at startup
 *   Add subscriptions and schdules
 *   Create our default state
 *
 */
def initialize() {
    log.trace "initialize"

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
    getAllChildDevices().each { device ->
        // Only refresh the main device that has a panel_state
        def device_type = device.getTypeName()
        if (device_type == "AlarmDecoder network appliance")
        {
            if (debug) log.debug("initialize: Found device refresh subscription.")
            device.subscribeNotifications()
        }
    }
}

/*** Handlers ***/

/**
 * locationHandler(evt)
 * Local network messages sent to TCP port 39500 and UPNP UDP port 1900 will be captured here.
 *
 * Test from the AlarmDecoder Appliance:
 *   curl -H "Content-Type: application/json" -X POST -d ‘{"message":"Hi, this is a test from AlarmDecoder network device"}’ http://YOUR.HUB.IP.ADDRESS:39500
 */
def locationHandler(evt) {
    if (debug) log.debug "locationHandler ${evt.name}: ${evt.value}"

    def description = evt.description
    def hub = evt?.hubId

    // many events but we only want PUSH notifications and they have all the data in evt.description
    if (!description)
     return

    if (debug) log.debug "locationHandler: description: ${evt.description} name: ${evt.name} value: ${evt.value} data: ${evt.data}"

    def parsedEvent = ["hub":hub]
    try {
        parsedEvent << parseEventMessage(description)
    }
    catch(Exception e) {
        log.error("exception in parseEventMessage: evt: ${evt.name}: ${evt.value} : ${evt.data}")
        return
    }

    if (debug) log.debug("locationHandler mac: ${parsedEvent.mac} parsedEvent: ${parsedEvent}")

    // UPNP LAN EVENTS on UDP port 1900 from 'AlarmDecoder:1' devices only
    if (parsedEvent?.ssdpTerm?.contains("urn:schemas-upnp-org:device:AlarmDecoder:1")) {

        // make sure our state.devices is initialized. return discard.
        def alarmdecoders = getDevices()

        // add the device to state.devices if it does not exist yet
        if (!(alarmdecoders."${parsedEvent.ssdpUSN.toString()}")) {
            if (debug) log.debug "locationHandler: Adding device: ${parsedEvent.ssdpUSN}"
            alarmdecoders << ["${parsedEvent.ssdpUSN.toString()}": parsedEvent]
        } else
        { // It exists so update if needed
            // grab the device object based upon ur ssdpUSN
            if (debug) log.debug  "alarmdecoders ${alarmdecoders}"
            def d = alarmdecoders."${parsedEvent.ssdpUSN.toString()}"

            if (debug) log.debug "locationHandler: checking for device changed values on device=${d}"

            // Did the DNI change? if so update it.
            if (d.ip != parsedEvent.ip || d.port != parsedEvent.port) {
                // update the state.devices DNI
                d.ip = parsedEvent.ip
                d.port = parsedEvent.port

                if (debug) log.debug "locationHandler: device DNI changed values!"

                // Update device by its MAC address if the DNI changes
                def children = getChildDevices()
                children.each {
                    if (it.getDeviceDataByName("mac") == parsedEvent.mac) {
                        it.setDeviceNetworkId((parsedEvent.ip + ":" + parsedEvent.port))
                        if (debug) log.debug "Set new network id: " + parsedEvent.ip + ":" + parsedEvent.port
                    }
                }
            }

            // TODO: if the ssdpPath changes we need to fetch a new one
            if (d.ssdpPath != parsedEvent.ssdpPath) {
                // update the ssdpPath
                d.ssdpPath = parsedEvent.ssdpPath
                if (debug) log.debug "locationHandler: device ssdpPath changed values. need to fetch new description.xml."

                // send out reqeusts for xml description for anyone not verified yet
                // FIXME: verifyAlarmDecoders()
            }
        }
    } else if (parsedEvent?.headers && parsedEvent?.body)
    { // HTTP EVENTS on TCP port 39500 RESPONSES
      // for some reason they hit here and in the parse() in the device?
        def headerString = new String(parsedEvent.headers.decodeBase64())
        def bodyString = new String(parsedEvent.body.decodeBase64())
        def type = (headerString =~ /Content-Type:.*/) ? (headerString =~ /Content-Type:.*/)[0] : null

        if (debug) log.debug ("locationHandler HTTP event type:${type} body:${bodyString} headers:${headerString}")

        // XML PUSH data
        if (type?.contains("xml"))
        {
            getAllChildDevices().each { device ->
                // Only refresh the main device that has a panel_state
                def device_type = device.getTypeName()
                // only accept messages that are from a network appliance and that match our MAC address
                if (device_type == "AlarmDecoder network appliance" && device.getDeviceDataByName("mac") == parsedEvent.mac)
                {
                    if (debug) log.debug ("push_update_alarmdecoders: Found device sending pushed data.")
                    device.parse_xml(headerString, bodyString).each { e-> device.sendEvent(e) }
                }
            }
        }
    }
}

/**
 * Handle remote web requests to the ST cloud services for http://somegraph/update
 */
def webserviceUpdate()
{
    log.trace "webserviceUpdate"
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
	if (debug) log.debug("actionButton: desc=${id} dev=${d}")

    if (!d) {
        log.error("actionButton: Could not find primary device '${getDeviceKey()}'.")
        return
    }

    /* FIXME: Need a pin code or some way to trust the request. */
    if(create_disarm) {
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
    if (debug) log.debug("smokeSet: desc=${evt.value}")

    def d = getChildDevices().find { it.deviceNetworkId.contains(":smokeAlarm") }
    if (!d)
    {
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
    if (debug) log.debug("armAwaySet ${evt.value}")
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
    if (debug) log.debug("armStaySet ${evt.value}")
    def d = getChildDevice("${getDeviceKey()}:armStay")
    if (!d) {
        log.info("armStaySet: Could not find 'armStay' device.")
    } else {
        d.sendEvent(name: "switch", value: evt.value, isStateChange: true, filtered: true)
    }

    d = getChildDevice("${getDeviceKey()}:armStayStatus")
    if (!d) {
        log.info("armStaySet: Could not find 'armStayStatus' device.")
    } else {
        _sendEventTranslate(d, evt.value)
    }
}

/**
 * send event to alarmbell indicator device to set state
 */
def alarmBellSet(evt) {
    if (debug) log.debug("alarmBellSet ${evt.value}")
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
    /* if (debug)*/ log.debug("chimeSet ${evt.value}")
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
    if (debug) log.debug("exitSet ${evt.value}")
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
    if (debug) log.debug("perimeterOnlySet ${evt.value}")
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
    if (debug) log.debug("entryDelayOffSet ${evt.value}")
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
    if (debug) log.debug("bypassSet ${evt.value}")
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
    if (debug) log.debug("readySet ${evt.value}")
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
    if (debug) log.debug("disarmSet ${evt.value}")
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
    def cidstate = (parts[2][-4..-4] == "1") ? "on" : "off"

    // 3 digit CID number
    def cidnum = parts[2][-3..-1]

    // the CID report value. Zone # or User # or ...
    def cidvalue = parts[0].split(':')[1]

    // the partition # with 0 being system
    def partition =  parts[1].toInteger()

    if (debug) log.debug("cidSet num:${cidnum} part: ${partition} state:${cidstate} val:${cidvalue}")

    def sent = false
    def rawmsg = evt.value
    def device_name = "CID-${cidnum}-${partition}-${cidvalue}"
    def children = getChildDevices()
    children.each {
        if (it.deviceNetworkId.contains(":CID-")) {
            def match = it.deviceNetworkId.split(":")[2].trim()

            // replace ? with . non regex
            match = match.replace("?",".")

            if (device_name =~ /${match}/) {
                if (debug) log.debug("cidSet device: ${device_name} matches ${match} sendng state ${cidstate}")
                _sendEventTranslate(it, cidstate)
                sent = true
            } else {
                if (debug) log.debug("cidSet device: ${device_name} no match ${match}")
            }
        }
    }

    if (!sent) {
        log.error("cidSet: Could not find 'CID-${cidnum}-${partition}-${cidvalue}|XXX' device.")
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
    log.info("rfxSet ${evt.value}")

    // get our RFX state and number
    def parts = evt.value.split(':')

    def sn = parts[0]
    def bat = parts[1]
    def supv = parts[2]
    def loop0 = parts[3]
    def loop1 = parts[4]
    def loop2 = parts[5]
    def loop3 = parts[6]

    if (debug) log.info("rfxSet sn:${sn} bat: ${bat} sukpv:${supv} loop0:${loop0} loop1:${loop1} loop2:${loop2} loop3:${loop3}")

    def sent = false

    def device_name = "RFX-${sn}-${bat}-${supv}-${loop0}-${loop1}-${loop2}-${loop3}"

    def children = getChildDevices()
    children.each {
        if (it.deviceNetworkId.contains(":RFX-")) {

            // Network mask differes from ST to HT
            def sp = ""
            if (isSmartThings())
                sp = it.deviceNetworkId.split(":")[2].trim().split("-")
            else if (isHubitat())
                sp = it.deviceNetworkId.split(":")[1].trim().split("-")


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

				_sendEventTranslate(it, (tot>0 ? "on" : "off"))

                if (debug) log.info("rfxSet device: ${device_name} matches ${match} sendng state ${tot}")

                sent = true

            } else {
                if (debug) log.info("rfxSet device: ${device_name} no match ${match}")
            }
        }
    }

    if (!sent && debug) {
        log.error("rfxSet: Could not find '${device_name}|XXX' device.")
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

    try {
        def zone_switch = addChildDevice(APPNAMESPACE, "AlarmDecoder virtual contact sensor", "${evt.data}", state.hub, [name: "${evt.data}", label: "${sname} Zone Sensor #${i}", completedSetup: true])
        def sensorValue = "open"
        if (settings.defaultSensorToClosed == true) {
            sensorValue = "closed"
        }

        // Set default contact state.
        _sendEventTranslate(zone_switch, (sensorValue == "open" ? "on" : "off"))
    } catch (e) {
        log.error "There was an error (${e}) when trying to addZone ${i}"
    }
}


/**
 * Handle Device Command zoneOn()
 * sets Contact attributes of the alarmdecoder device to open/closed
 */
def zoneOn(evt) {
    if (debug) log.debug("zoneOn: desc=${evt.value}")

    def d = getChildDevices().find { it.deviceNetworkId.endsWith("switch${evt.value}") }
    if (d)
    {
        def sensorValue = "closed"
        if (settings.defaultSensorToClosed == true)
            sensorValue = "open"

        _sendEventTranslate(d, (sensorValue == "open" ? "on" : "off"))
    }
}

/**
 * Handle Device Command zoneOff()
 * sets Contact attributes of the alarmdecoder device to open/closed
 */
def zoneOff(evt) {
    if (debug) log.debug("zoneOff: desc=${evt.value}")

    def d = getChildDevices().find { it.deviceNetworkId.endsWith("switch${evt.value}") }
    if (d)
    {
        def sensorValue = "open"
        if (settings.defaultSensorToClosed == true)
            sensorValue = "closed"

        _sendEventTranslate(d, (sensorValue == "open" ? "on" : "off"))
    }
}

/**
 * Handle SmartThings Smart Home Monitor(SHM) or Hubitat Safety Monitor (HSM)  events and update the UI of the App.
 *
 */
def monitorAlarmHandler(evt) {
    if (settings.monIntegration == false)
        return

    if (debug) log.debug("monitorAlarmHandler -- ${evt.value}, lastMONStatus ${state.lastMONStatus}, lastAlarmDecoderStatus ${state.lastAlarmDecoderStatus}")

    if (state.lastMONStatus != evt.value)
    {
        getAllChildDevices().each { device ->
            // Only refresh the main device that has a panel_state
            def device_type = device.getTypeName()
            if (device_type == "AlarmDecoder network appliance")
            {
                if (debug) log.debug("monitorAlarmHandler DEBUG-- ${device.deviceNetworkId}")
                /* SmartThings */
                if (isSmartThings()) {
                    if (evt.value == "away" || evt.value == "armAway") {
                        // do not send if already in that state.
                        if(!device.getStateValue("panel_armed") && !device.getStateValue("panel_armed_stay")) {
                            device.arm_away()
                        } else {
                            log.trace "monitorAlarmHandler -- no send arm_away already set"
                        }
                    }
                    else if (evt.value == "stay" || evt.value == "armHome") {
                        // do not send if already in that state.
                        if(!device.getStateValue("panel_armed") && !device.getStateValue("panel_armed_stay")) {
                            device.arm_stay()
                        } else {
                            log.trace "monitorAlarmHandler -- no send arm_stay already set"
                        }
                    }
                    else if (evt.value == "off" || evt.value == "disarm") {
                        // do not send if already in that state.
                        if(device.getStateValue("panel_armed") || device.getStateValue("panel_armed_stay")) {
                            device.disarm()
                        } else {
                            log.trace "monitorAlarmHandler -- no send disarm already set"
                        }
                    } else
                        log.debug "Unknown SHM alarm value: ${evt.value}"
                }
                /* Hubitat */
                else if (isHubitat()) {
                    if (evt.value == "armedAway") {
                        // do not send if already in that state.
                        if(!device.getStateValue("panel_armed") && !device.getStateValue("panel_armed_stay")) {
                            device.arm_away()
                        } else {
                            log.trace "monitorAlarmHandler -- no send arm_away already set"
                        }
                    }
                    else if (evt.value == "armedHome") {
                        // do not send if already in that state.
                        if(!device.getStateValue("panel_armed") && !device.getStateValue("panel_armed_stay")) {
                            device.arm_stay()
                        } else {
                            log.trace "monitorAlarmHandler -- no send arm_stay already set"
                        }
                    }
                    else if (evt.value == "disarmed") {
                        // do not send if already in that state.
                        if(device.getStateValue("panel_armed") || device.getStateValue("panel_armed_stay")) {
                            device.disarm()
                        } else {
                            log.trace "monitorAlarmHandler -- no send disarm already set ${device.getStateValue('panel_armed')} ${device.getStateValue('panel_armed_stay')}"
                        }
                    }
                    else
                        log.debug "Unknown HSM alarm value: ${evt.value}"
                }
            }
        }
    }
    // Track for async processing
    state.lastMONStatus = evt.value
}

/**
 * Handle Alarm events from the AlarmDecoder and
 * send them back to the the Monitor API to update the
 * status of the alarm panel
 */
def alarmdecoderAlarmHandler(evt) {
    if (settings.monIntegration == false || settings.monChangeStatus == false)
        return

    if (debug) log.debug("alarmdecoderAlarmHandler -- ${evt.value}, lastMONStatus ${state.lastMONStatus}, lastAlarmDecoderStatus ${state.lastAlarmDecoderStatus}")

    if (state.lastAlarmDecoderStatus != evt.value) {
        if(isSmartThings()) {
            /* no traslation needed already [stay,away,off] */
            if (debug) log.debug("alarmdecoderAlarmHandler: alarmSystemStatus ${evt.value}")
            sendLocationEvent(name: "alarmSystemStatus", value: evt.value)
        }
        else if (isHubitat()) {
            /* translate to HSM */
            msg = ""
            if (evt.value == "stay") {
                msg = "armHome"
                state.lastMONStatus = "armedHome" // prevent loop
            }
            if (evt.value == "away") {
                msg = "armAway"
                state.lastMONStatus = "armedAway" // prevent loop
            }
            if (evt.value == "off") {
                msg = "disarm"
                state.lastMONStatus = "disarmed" // prevent loop
            }
            if (debug) log.debug("alarmdecoderAlarmHandler: hsmSetArm ${msg}")
            // Notify external MON of the change
            sendLocationEvent(name: "hsmSetArm", value: msg)
        }
    }

    state.lastAlarmDecoderStatus = evt.value
}

/*** Utility ***/

/**
 * Enable primary network and system subscriptions
 */
def initSubscriptions() {
    // subscribe to the Smart Home Manager api for alarm status events
    if (debug) log.debug("initSubscriptions: Subscribe to handlers")

    if (isSmartThings()) {
        subscribe(location, "alarmSystemStatus", monitorAlarmHandler)
    }
    else if (isHubitat()) {
        subscribe(location, "hsmStatus", monitorAlarmHandler)
    }

    // subscribe to add zone handler
    subscribe(app, addZone)

    /* subscribe to local LAN messages to this HUB on TCP port 39500 and UPNP UDP port 1900 */
    subscribe(location, null, locationHandler, [filterEvents: false])
}

/**
 * Called by page_discover_devices page periodically
 */
def discover_alarmdecoder() {
    if (debug) log.debug("discover_alarmdecoder")
    sendDiscover()
}

/**
 * Call refresh() on the AlarmDecoder parent device object.
 * This will force the HUB to send a REST API request to the AlarmDecoder Network Appliance.
 * and get back the current status of the AlarmDecoder.
 */
def refresh_alarmdecoders() {
    if (debug) log.debug("refresh_alarmdecoders")

    getAllChildDevices().each { device ->
        // Only refresh the main device that has a panel_state
        def device_type = device.getTypeName()
        if (device_type == "AlarmDecoder network appliance")
        {
            def apikey = device._get_api_key()
            if (apikey) {
                device.refresh()
            } else {
                log.error("refresh_alarmdecoders no API KEY for: ${device} @ ${device.getDataValue("urn")}")
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
        state.devices = [:]
    }
    return state.devices
}

/**
 * Add devices selected in the GUI if new.
 */
def addExistingDevices() {
    if (debug) log.debug("addExistingDevices: ${input_selected_devices}")

	// resubscribe! Its a hack if one adds more subscriptions and does not want to rebuild everything.
    configureDeviceSubscriptions()

    def selected_devices = input_selected_devices
    if (selected_devices instanceof java.lang.String) {
        selected_devices = [selected_devices]
    }

    selected_devices.each { dni ->
        def d = getChildDevice(dni)
        if (debug) log.debug("addExistingDevices, getChildDevice(${dni})")
        if (!d) {

            // Find the device with a matching dni XXXXXXXX:XXXX
            def newDevice = state.devices.find { /*k, v -> k == dni*/ k, v -> dni == "${v.ip}:${v.port}" }
            if (debug) log.debug("addExistingDevices, devices.find=${newDevice}")

            if (newDevice) {
                // Set the device network ID so that hubactions get sent to the device parser.
                state.ip = newDevice.value.ip
                state.port = newDevice.value.port
                state.hub = newDevice.value.hub

                // Set URN for the child device
                state.urn = convertHexToIP(state.ip) + ":" + convertHexToInt(state.port)
                if (debug) log.debug("AlarmDecoder webapp api endpoint('${state.urn}')")

                try {

                    // Create device adding the URN to its data object
                    d = addChildDevice(APPNAMESPACE,
                                       "AlarmDecoder network appliance",
                                       "${getDeviceKey()}",
                                       newDevice?.value.hub,
                                       [
                                           name: "${getDeviceKey()}",
                                           label: "${guiname}",
                                           completedSetup: true,
                                           /* data associated with this AlarmDecoder */
                                           data:[
                                                    // save mac address to update if IP / PORT change
                                                    mac: newDevice.value.mac,
                                                    ssdpUSN: newDevice.value.ssdpUSN,
                                                    urn: state.urn,
                                                    ssdpPath: newDevice.value.ssdpPath
                                                ]
                                       ])

                    // Set default device state to notready.

                    d.sendEvent(name: "panel_state", value: "notready", isStateChange: true, displayed: true)
                } catch (e) {
                    log.info "Error creating device root device ${guiname}"
                }
            }
        }

        // Add zone contact sensors if they do not exist.
        // asynchronous to avoid timeout. Apps can only run for 20 seconds or it will be killed.
        for (def i = 0; i < max_sensors; i++)
        {
            if (debug) log.debug("Adding virtual zone sensor ${i}")
            // SmartThings we do out of band with callback
            if (isSmartThings()) {
                sendEvent(name: "addZone", value: "${i+1}", data: "${getDeviceKey()}:switch${i+1}")
            }
            // Callbacks to local events seem to not work on HT
            else if (isHubitat()) {
                def evt = [value: "${i+1}", data: "${state.ip}:switch${i+1}"]
                addZone(evt)
            }
        }

        // Add Smoke Alarm sensors if it does not exist.
        def cd = state.devices.find { k, v -> k == "${getDeviceKey()}:smokeAlarm" }
        if (!cd)
        {
            try {
                def nd = addChildDevice(APPNAMESPACE, "AlarmDecoder virtual smoke alarm", "${getDeviceKey()}:smokeAlarm", state.hub,
                                        [name: "${getDeviceKey()}:smokeAlarm", label: "${sname} Smoke Alarm", completedSetup: true])
                nd.sendEvent(name: "smoke", value: "clear", isStateChange: true, displayed: false)
            } catch (e) {
                log.info "Error creating device: smokeAlarm"
            }
        }

        // do not create devices if testing. Real PITA to delete them every time. ST needs to add a way to delete multiple devices at once.
        if (!nocreatedev)
        {
            // Add Arm Stay switch/indicator combo if it does not exist.
            addAD2VirtualDevices("armStay", "Stay", false, true, true)

            // Add Arm Away switch/indicator combo if it does not exist.
            addAD2VirtualDevices("armAway", "Away", false, true, true)

            // Add Exit switch/indicator combo if it does not exist.
            addAD2VirtualDevices("exit", "Exit", false, true, true)

            // Add Chime Mode toggle switch/indicator combo if it does not exist.
            addAD2VirtualDevices("chimeMode", "Chime", false, true, true)

            // Add Bypass status contact if it does not exist.
            addAD2VirtualDevices("bypass", "Bypass", false, false, true)

            // Add Ready status contact if it does not exist.
            addAD2VirtualDevices("ready", "Ready", false, false, true)

            // Add perimeter only status contact if it does not exist.
            addAD2VirtualDevices("perimeterOnly", "Perimeter Only", false, false, true)

            // Add entry delay off status contact if it does not exist.
            addAD2VirtualDevices("entryDelayOff", "Entry Delay Off", false, false, true)

            // Add virtual Alarm Bell switch/indicator combo if it does not exist.
            addAD2VirtualDevices("alarmBell", "Alarm Bell", false, true, true)

            // Add FIRE Alarm switch/indicator combo if it does not exist.
            addAD2VirtualDevices("alarmFire", "Fire Alarm", false, true, true)

            // Add Panic Alarm switch/indicator combo if it does not exist.
            addAD2VirtualDevices("alarmPanic", "Panic Alarm", false, true, false)

            // Add AUX Alarm switch/indicator combo if it does not exist.
            addAD2VirtualDevices("alarmAUX", "AUX Alarm", false, true, false)

            // Add Disarm button if it does not exist.
            if(create_disarm) {
                addAD2VirtualDevices("disarm", "Disarm", false, true, false)
            }
        }
    }
}


/**
 * Add Virtual button and contact
 */
def addAD2VirtualDevices(name, label, initstate, createButton, createContact) {

    if (createButton) {
        // Add switch/indicator combo if it does not exist.
        def cd = state.devices.find { k, v -> k == "${getDeviceKey()}:${name}" }
        if (!cd)
        {
            try {
                def nd = addChildDevice(APPNAMESPACE, "AlarmDecoder action button indicator", "${getDeviceKey()}:${name}", state.hub,
                                        [name: "${getDeviceKey()}:${name}", label: "${sname} ${label}", completedSetup: true])
                nd.sendEvent(name: "switch", value: (initstate ? "on" : "off") , isStateChange: true, displayed: false)
            } catch (e) {
                log.info "Error creating device: ${name}"
            }
        }
    }

    if (createContact) {
        // Add contact status contact if it does not exit.
        def cd = state.devices.find { k, v -> k == "${getDeviceKey()}:${name}Status" }
        if (!cd)
        {
            try {
                def nd = addChildDevice(APPNAMESPACE, "AlarmDecoder status indicator", "${getDeviceKey()}:${name}Status", state.hub,
                                        [name: "${getDeviceKey()}:${name}Status", label: "${sname} ${label} Status", completedSetup: true])
                nd.sendEvent(name: "contact", value: (initstate ? "closed" : "open"), isStateChange: true, displayed: false)
            } catch (e) {
                log.info "Error creating device: ${name}Status"
            }
        }
    }
}

/**
 * Configure subscriptions the virtual devices will send too.
 */
private def configureDeviceSubscriptions() {
    if (debug) log.debug("configureDeviceSubscriptions")
    def device = getChildDevice("${getDeviceKey()}")
    if (!device) {
        log.error("configureDeviceSubscriptions: Could not find primary device.")
        return
    }

    /* Handle events sent from the AlarmDecoder network appliance device
     * to update virtual zones when they change.
     */
    subscribe(device, "zone-on", zoneOn, [filterEvents: false])
    subscribe(device, "zone-off", zoneOff, [filterEvents: false])

    // Subscribe to our own alarm status events from our primary device
    subscribe(device, "alarmStatus", alarmdecoderAlarmHandler, [filterEvents: false])

    // subscrib to smoke-set handler for updates
    subscribe(device, "smoke-set", smokeSet, [filterEvents: false])

    // subscribe to arm-away handler
    subscribe(device, "arm-away-set", armAwaySet, [filterEvents: false])

    // subscribe to arm-stay handler
    subscribe(device, "arm-stay-set", armStaySet, [filterEvents: false])

    // subscribe to chime handler
    subscribe(device, "chime-set", chimeSet, [filterEvents: false])

    // subscribe to exit handler
    subscribe(device, "exit-set", exitSet, [filterEvents: false])

    // subscribe to perimeter-only-set handler
    subscribe(device, "perimeter-only-set", perimeterOnlySet, [filterEvents: false])

    // subscribe to entry-deley-off-set handler
    subscribe(device, "entry-delay-off-set", entryDelayOffSet, [filterEvents: false])

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
 * Parse local network messages.
 *
 * May be to UDP port 1900 for UPNP message or to TCP port 39500
 * for local network to hub push messages.
 *
 */
private def parseEventMessage(String description) {
    if (debug)
      log.debug "parseEventMessage: $description"
    def event = [:]
    def parts = description.split(',')
    parts.each { part ->
        part = part.trim()
        if (part.startsWith('devicetype:')) {
            def valueString = part.split(":")[1].trim()
            event.devicetype = valueString
        }
        else if (part.startsWith('mac:')) {
            def valueString = part.split(":")[1].trim()
            if (valueString) {
                event.mac = valueString
            }
        }
        else if (part.startsWith('networkAddress:')) {
            def valueString = part.split(":")[1].trim()
            if (valueString) {
                event.ip = valueString
            }
        }
        else if (part.startsWith('deviceAddress:')) {
            def valueString = part.split(":")[1].trim()
            if (valueString) {
                event.port = valueString
            }
        }
        else if (part.startsWith('ssdpPath:')) {
            part -= "ssdpPath:"
            def valueString = part.trim()
            if (valueString) {
                event.ssdpPath = valueString
            }
        }
        else if (part.startsWith('ssdpUSN:')) {
            part -= "ssdpUSN:"
            def valueString = part.trim()
            if (valueString) {
                event.ssdpUSN = valueString
            }
        }
        else if (part.startsWith('ssdpTerm:')) {
            part -= "ssdpTerm:"
            def valueString = part.trim()
            if (valueString) {
                event.ssdpTerm = valueString
            }
        }
        else if (part.startsWith('headers')) {
            part -= "headers:"
            def valueString = part.trim()
            if (valueString) {
                event.headers = valueString
            }
        }
        else if (part.startsWith('body')) {
            part -= "body:"
            def valueString = part.trim()
            if (valueString) {
                event.body = valueString
            }
        }
    }

    event
}

/**
 * Send a request for the description.xml For every known AlarmDecoder
 * we have discovered that is not verified.
 */
def verifyAlarmDecoders() {
    def devices = getDevices().findAll { it?.value?.verified != true }

  if (devices) {
        log.warn "verifyAlarmDecoders: UNVERIFIED Decoders!: $devices"
  }

  devices.each {
        if (it?.value?.ssdpPath?.contains("xml")) {
            verifyAlarmDecoder((it?.value?.ip + ":" + it?.value?.port), it?.value?.ssdpPath)
        } else {
            log.warn("verifyAlarmDecoders: invalid ssdpPath not an xml file")
        }
    }
}

/**
 * Send a GET request from the HUB to the AlarmDecoder for its descrption.xml file
 */
def verifyAlarmDecoder(String deviceNetworkId, String ssdpPath) {
  sendVerify(deviceNetworkID, ssdpPath)
}

/**
 * Convert from internal format networkAddress:C0A8016F to a real IP address string 192.168.1.111
 */
private String convertHexToIP(hex) {
    [convertHexToInt(hex[0..1]),convertHexToInt(hex[2..3]),convertHexToInt(hex[4..5]),convertHexToInt(hex[6..7])].join(".")
}

/**
 * convert hex encoded string to integer
 */
private Integer convertHexToInt(hex) {
    Integer.parseInt(hex,16)
}

/**
 *
 */
private String getDeviceKey() {
    def key = ""
    if (isSmartThings())
        key = "${state.ip}:${state.port}"
    else if (isHubitat())
        key = "${state.ip}"

    return key
}

/**
 * build a URI host address of the AlarmDecoder web appliance for web requests.
 *  ex. XXX.XXX.XXX.XXX:XXXXX -> 192.168.1.1:5000
 */
private getHostAddressFromDNI(d) {
  def parts = d.split(":")
  def ip = convertHexToIP(parts[0])
  def port = convertHexToInt(parts[1])
  return ip + ":" + port
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
def _sendEventTranslate(ad2d, state) {

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
        ad2d.sendEvent(name: "switch", value: (sval ? "on" : "off"), isStateChange: true, filtered: true)
    }

    // send a 'contact' event if its a [Contact Sensor]
    // Default close = Off, open(Alerting) = On
    if (ad2d.hasCapability("Contact Sensor")) {
        // convert: Any matches is ON(true) no match is OFF(false)
        def sval = ((state == "on") ? true : false)

        // invert: If device has invert attribute then invert signal
        sval = (invert ? !sval : sval)

        // send switch event
        ad2d.sendEvent(name: "contact", value: (sval ? "open" : "closed") , isStateChange: true, filtered: true)
    }

    // send a 'motion' event if its a [Motion Sensor]
    // Default inactive = Off, active(Alerting) = On
    if (ad2d.hasCapability("Motion Sensor")) {
        // convert: Any matches is ON(true) no match is OFF(false)
        def sval = ((state == "on") ? true : false)

        // invert: If device has invert attribute then invert signal
        sval = (invert ? !sval : sval)

        // send switch event
        ad2d.sendEvent(name: "motion", value: (sval ? "active" : "inactive") , isStateChange: true, filtered: true)
    }

    // send a 'shock' event if its a [Shock Sensor]
    // Default clear = Off, detected(Alerting) = On
    if (ad2d.hasCapability("Shock Sensor")) {
        // convert: Any matches is ON(true) no match is OFF(false)
        def sval = ((state == "on") ? true : false)

        // invert: If device has invert attribute then invert signal
        sval = (invert ? !sval : sval)

        // send switch event
        ad2d.sendEvent(name: "shock", value: (sval ? "detected" : "clear") , isStateChange: true, filtered: true)
    }

	// send a 'carbonMonoxide' event if its a [Carbon Monoxide Detector]
    // Default clear = Off, detected(Alerting) = On
    if (ad2d.hasCapability("Carbon Monoxide Detector")) {
        // convert: Any matches is ON(true) no match is OFF(false)
        def sval = ((state == "on") ? true : false)

        // invert: If device has invert attribute then invert signal
        sval = (invert ? !sval : sval)

        // send switch event
        ad2d.sendEvent(name: "carbonMonoxide", value: (sval ? "detected" : "clear") , isStateChange: true, filtered: true)
    }

    // send a 'smoke' event if its a [Smoke Detector]
    // Default clear = Off, detected(Alerting) = On
    if (ad2d.hasCapability("Smoke Detector")) {
        // convert: Any matches is ON(true) no match is OFF(false)
        def sval = ((state == "on") ? true : false)

        // invert: If device has invert attribute then invert signal
        sval = (invert ? !sval : sval)

        // send switch event
        ad2d.sendEvent(name: "smoke", value: (sval ? "detected" : "clear") , isStateChange: true, filtered: true)
    }
}
