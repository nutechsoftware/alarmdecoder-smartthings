/**
 *  AlarmDecoder Network Appliance
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
 */

/**
 * global support
 */
import groovy.transform.Field
@Field APPNAMESPACE = "alarmdecoder"

/**
 * Parsing support
 */
import groovy.json.JsonSlurper;
import groovy.util.XmlParser;

/**
 * System Settings
 */
// The max number of zone faults we can show on this page.
// To adjust requires manual adding of the tiles into preferences.
// FIXME: refactor remove static count.
@Field MAX_ZONE_FAULT_TILES = 12

/**
 * preferences
 */
preferences {
  section() {
    input(
      name: "api_key",
      type: "password",
      title: "API Key",
      description: "The key to access the REST API",
      required: true)
    input(
      name: "user_code",
      type: "password",
      title: "Alarm Code",
      description: "The user code for the panel",
      required: true)
    input(
      name: "panel_type",
      type: "enum",
      title: "Panel Type",
      description: "Type of panel [ADEMCO,DSC]",
      options: ["ADEMCO", "DSC"],
      defaultValue: "ADEMCO",
      required: true)
  }
}

/**
 * metadata
 */
metadata {
  definition(
    name: "AlarmDecoder network appliance",
    namespace: APPNAMESPACE, author: "Nu Tech Software Solutions, Inc.") {

    // capabilities
    capability "Refresh"

    // attributes
    attribute(
      "panel_state",
      "enum",
      [
        "armed",
        "armed_stay",
        "armed_stay_exit",
        "disarmed",
        "alarming",
        "fire",
        "ready",
        "notready"
      ]
    )
    attribute "armed", "enum", ["armed", "disarmed"]
    attribute "panic_state", "string"
    attribute "zoneStatus1", "string"
    attribute "zoneStatus2", "string"
    attribute "zoneStatus3", "string"
    attribute "zoneStatus4", "string"
    attribute "zoneStatus5", "string"
    attribute "zoneStatus6", "string"
    attribute "zoneStatus7", "string"
    attribute "zoneStatus8", "string"
    attribute "zoneStatus9", "string"
    attribute "zoneStatus10", "string"
    attribute "zoneStatus11", "string"
    attribute "zoneStatus12", "string"

    // commands
    command "disarm"
    command "arm_stay"
    command "exit"
    command "arm_away"
    command "fire"
    command "fire1"
    command "fire2"
    command "panic"
    command "panic1"
    command "panic2"
    command "aux"
    command "aux1"
    command "aux2"
    command "chime"
    command "bypass", ["number"]
    command "bypassN", ["string"]
    command "bypass1"
    command "bypass2"
    command "bypass3"
    command "bypass4"
    command "bypass5"
    command "bypass6"
    command "bypass7"
    command "bypass8"
    command "bypass9"
    command "bypass10"
    command "bypass11"
    command "bypass12"
  }

  simulator {
    // TODO: define status and reply messages here
  }

  // tile definitions
  tiles(scale: 2) {

    // top alarm status box
    multiAttributeTile(
      name: "status",
      type: "generic",
      width: 6, height: 4) {
      tileAttribute(
        "device.panel_state",
        key: "PRIMARY_CONTROL") {
        attributeState(
          "armed",
          label: 'Armed',
          icon: "st.security.alarm.on",
          backgroundColor: "#ffa81e")
        attributeState(
          "armed_exit",
          label: 'Armed (exit-now)',
          icon: "st.nest.nest-away",
          backgroundColor:
          "#ffa81e")
        attributeState(
          "armed_stay",
          label: 'Armed (stay)',
          icon: "st.security.alarm.on",
          backgroundColor: "#ffa81e")
        attributeState(
          "armed_stay_exit",
          label: 'Armed (exit-now)',
          icon: "st.nest.nest-away",
          backgroundColor: "#ffa81e")
        attributeState(
          "disarmed",
          label: 'Disarmed',
          icon: "st.security.alarm.off",
          backgroundColor: "#79b821",
          defaultState: true)
        attributeState(
          "alarming",
          label: 'Alarming!',
          icon: "st.home.home2",
          backgroundColor: "#ff4000")
        attributeState(
          "fire",
          label: 'Fire!',
          icon: "st.contact.contact.closed",
          backgroundColor: "#ff0000")
        attributeState(
          "ready",
          label: 'Ready',
          icon: "st.security.alarm.off",
          backgroundColor: "#79b821")
        attributeState(
          "notready",
          label: 'Not Ready',
          icon: "st.security.alarm.off",
          backgroundColor: "#e86d13")
      }
    }

    // arm/disarm button #1
    standardTile(
      "arm_disarm",
      "device.panel_state",
      inactiveLabel: false,
      width: 2, height: 2) {
      state(
        "armed",
        action: "disarm",
        icon: "st.security.alarm.off",
        label: "DISARM")
      state(
        "armed_exit",
        action: "disarm",
        icon: "st.security.alarm.off",
        label: "DISARM")
      state(
        "armed_stay",
        action: "disarm",
        icon: "st.security.alarm.off",
        label: "DISARM")
      state(
        "armed_stay_exit",
        action: "disarm",
        icon: "st.security.alarm.off",
        label: "DISARM")
      state(
        "disarmed",
        action: "arm_away",
        icon: "st.security.alarm.on",
        label: "AWAY")
      state(
        "alarming",
        action: "disarm",
        icon: "st.security.alarm.off",
        label: "DISARM")
      state(
        "fire",
        action: "disarm",
        icon: "st.security.alarm.off",
        label: "DISARM")
      state(
        "ready",
        action: "arm_away",
        icon: "st.security.alarm.on",
        label: "AWAY")
      state(
        "notready",
        action: "disarm",
        icon: "st.security.alarm.off",
        label: "DISARM")
    }

    // arm/disarm button #2
    standardTile(
      "stay_disarm",
      "device.panel_state",
      inactiveLabel: false,
      width: 2, height: 2) {
      state(
        "armed",
        action: "disarm",
        icon: "st.security.alarm.off",
        label: "DISARM")
      state(
        "armed_exit",
        action: "disarm",
        icon: "st.security.alarm.off",
        label: "DISARM")
      state(
        "armed_stay",
        action: "exit",
        icon: "st.nest.nest-away",
        label: "EXIT")
      state(
        "armed_stay_exit",
        action: "disarm",
        icon: "st.security.alarm.off",
        label: "DISARM")
      state(
        "disarmed",
        action: "arm_stay",
        icon: "st.Home.home4",
        label: "STAY")
      state(
        "alarming",
        action: "disarm",
        icon: "st.security.alarm.off",
        label: "DISARM")
      state(
        "fire",
        action: "disarm",
        icon: "st.security.alarm.off",
        label: "DISARM")
      state(
        "ready",
        action: "arm_stay",
        icon: "st.security.alarm.on",
        label: "STAY")
      state(
        "notready",
        action: "disarm",
        icon: "st.security.alarm.off",
        label: "DISARM")
    }

    // panic buttons Touch 3 times to trigger an alarm.
    // panic police
    standardTile(
      "panic",
      "device.panic_state",
      inactiveLabel: false,
      width: 1, height: 1) {
      state(
        "default",
        icon: "http://www.alarmdecoder.com/st/ad2-police.png",
        label: "PANIC",
        nextState: "panic1",
        action: "panic1")
      state(
        "panic1",
        icon: "http://www.alarmdecoder.com/st/ad2-police.png",
        label: "PANIC",
        nextState: "panic2",
        action: "panic2",
        backgroundColor: "#ffa81e")
      state(
        "panic2",
        icon: "http://www.alarmdecoder.com/st/ad2-police.png",
        label: "PANIC", nextState: "default",
        action: "panic",
        backgroundColor: "#ff4000"
      )
    }

    // panic fire
    standardTile(
      "fire",
      "device.fire_state",
      inactiveLabel: false,
      width: 1, height: 1) {
      state(
        "default",
        icon: "http://www.alarmdecoder.com/st/ad2-fire.png",
        label: "FIRE",
        nextState: "fire1",
        action: "fire1")
      state(
        "fire1",
        icon: "http://www.alarmdecoder.com/st/ad2-fire.png",
        label: "FIRE",
        nextState: "fire2",
        action: "fire2",
        backgroundColor: "#ffa81e")
      state(
        "fire2",
        icon: "http://www.alarmdecoder.com/st/ad2-fire.png",
        label: "FIRE",
        nextState: "default",
        action: "fire",
        backgroundColor: "#ff4000")
    }

    // panic medical
    standardTile(
      "aux",
      "device.aux_state",
      inactiveLabel: false,
      width: 1, height: 1) {
      state(
        "default",
        icon: "http://www.alarmdecoder.com/st/ad2-aux.png",
        label: "AUX",
        nextState: "aux1",
        action: "aux1")
      state(
        "aux1",
        icon: "http://www.alarmdecoder.com/st/ad2-aux.png",
        label: "AUX",
        nextState: "aux2",
        action: "aux2",
        backgroundColor: "#ffa81e")
      state(
        "aux2",
        icon: "http://www.alarmdecoder.com/st/ad2-aux.png",
        label: "AUX",
        nextState: "default",
        action: "aux",
        backgroundColor: "#ff4000")
    }

    // zone fault tiles show a list of faulted zones where pressing will bypass.
    // #1
    valueTile(
      "zoneStatus1",
      "device.zoneStatus1",
      inactiveLabel: false,
      width: 1, height: 1) {
      state "default",
        icon: "",
        label: '${currentValue}',
        action: "bypass1",
        nextState: "default",
        backgroundColors: [
          [value: 0, color: "#ffffff"],
          [value: 1, color: "#ff0000"],
          [value: 99, color: "#ff0000"]
        ]
    }
    // #2
    valueTile(
      "zoneStatus2",
      "device.zoneStatus2",
      inactiveLabel: false,
      width: 1, height: 1) {
      state "default",
        icon: "",
        label: '${currentValue}',
        action: "bypass2",
        nextState: "default",
        backgroundColors: [
          [value: 0, color: "#ffffff"],
          [value: 1, color: "#ff0000"],
          [value: 99, color: "#ff0000"]
        ]
    }
    // #3
    valueTile(
      "zoneStatus3",
      "device.zoneStatus3",
      inactiveLabel: false,
      width: 1, height: 1) {
      state "default",
        icon: "",
        label: '${currentValue}',
        action: "bypass3",
        nextState: "default",
        backgroundColors: [
          [value: 0, color: "#ffffff"],
          [value: 1, color: "#ff0000"],
          [value: 99, color: "#ff0000"]
        ]
    }
    // #4
    valueTile(
      "zoneStatus4",
      "device.zoneStatus4",
      inactiveLabel: false,
      width: 1, height: 1) {
      state "default",
        icon: "",
        label: '${currentValue}',
        action: "bypass4",
        nextState: "default",
        backgroundColors: [
          [value: 0, color: "#ffffff"],
          [value: 1, color: "#ff0000"],
          [value: 99, color: "#ff0000"]
        ]
    }
    // #5
    valueTile(
      "zoneStatus5",
      "device.zoneStatus5",
      inactiveLabel: false,
      width: 1, height: 1) {
      state "default",
        icon: "",
        label: '${currentValue}',
        action: "bypass5",
        nextState: "default",
        backgroundColors: [
          [value: 0, color: "#ffffff"],
          [value: 1, color: "#ff0000"],
          [value: 99, color: "#ff0000"]
        ]
    }
    // #6
    valueTile(
      "zoneStatus6",
      "device.zoneStatus6",
      inactiveLabel: false,
      width: 1, height: 1) {
      state "default",
        icon: "",
        label: '${currentValue}',
        action: "bypass6",
        nextState: "default",
        backgroundColors: [
          [value: 0, color: "#ffffff"],
          [value: 1, color: "#ff0000"],
          [value: 99, color: "#ff0000"]
        ]
    }
    // #7
    valueTile(
      "zoneStatus7",
      "device.zoneStatus7",
      inactiveLabel: false,
      width: 1, height: 1) {
      state "default",
        icon: "",
        label: '${currentValue}',
        action: "bypass7",
        nextState: "default",
        backgroundColors: [
          [value: 0, color: "#ffffff"],
          [value: 1, color: "#ff0000"],
          [value: 99, color: "#ff0000"]
        ]
    }
    // #8
    valueTile(
      "zoneStatus8",
      "device.zoneStatus8",
      inactiveLabel: false,
      width: 1, height: 1) {
      state "default",
        icon: "",
        label: '${currentValue}',
        action: "bypass8",
        nextState: "default",
        backgroundColors: [
          [value: 0, color: "#ffffff"],
          [value: 1, color: "#ff0000"],
          [value: 99, color: "#ff0000"]
        ]
    }
    // #9
    valueTile(
      "zoneStatus9",
      "device.zoneStatus9",
      inactiveLabel: false,
      width: 1, height: 1) {
      state "default",
        icon: "",
        label: '${currentValue}',
        action: "bypass9",
        nextState: "default",
        backgroundColors: [
          [value: 0, color: "#ffffff"],
          [value: 1, color: "#ff0000"],
          [value: 99, color: "#ff0000"]
        ]
    }
    // #10
    valueTile(
      "zoneStatus10",
      "device.zoneStatus10",
      inactiveLabel: false,
      width: 1, height: 1) {
      state "default",
        icon: "",
        label: '${currentValue}',
        action: "bypass10",
        nextState: "default",
        backgroundColors: [
          [value: 0, color: "#ffffff"],
          [value: 1, color: "#ff0000"],
          [value: 99, color: "#ff0000"]
        ]
    }
    // #11
    valueTile(
      "zoneStatus11",
      "device.zoneStatus11",
      inactiveLabel: false,
      width: 1, height: 1) {
      state "default",
        icon: "",
        label: '${currentValue}',
        action: "bypass11",
        nextState: "default",
        backgroundColors: [
          [value: 0, color: "#ffffff"],
          [value: 1, color: "#ff0000"],
          [value: 99, color: "#ff0000"]
        ]
    }
    // #12
    valueTile(
      "zoneStatus12",
      "device.zoneStatus12",
      inactiveLabel: false,
      width: 1, height: 1) {
      state "default",
        icon: "",
        label: '${currentValue}',
        action: "bypass12",
        nextState: "default",
        backgroundColors: [
          [value: 0, color: "#ffffff"],
          [value: 1, color: "#ff0000"],
          [value: 99, color: "#ff0000"]
        ]
    }

    // refresh button (PULL results from server)
    // FIXME: Not need as things improve. Make smaller still useful for testing.
    standardTile(
      "refresh",
      "device.refresh",
      inactiveLabel: false,
      decoration: "flat",
      width: 1, height: 1) {
      state "default",
        action: "refresh.refresh",
        icon: "st.secondary.refresh",
        label: "refresh"
    }

    // main page layout and order
    main(["status"])
    details(
      ["status",
        "arm_disarm",
        "stay_disarm",
        "panic",
        "fire",
        "aux",
        "refresh",
        "zoneStatus1",
        "zoneStatus2",
        "zoneStatus3",
        "zoneStatus4",
        "zoneStatus5",
        "zoneStatus6",
        "zoneStatus7",
        "zoneStatus8",
        "zoneStatus9",
        "zoneStatus10",
        "zoneStatus11",
        "zoneStatus12"
      ])
  }
}


/*** Event Handlers ***/

/**
 * installed()
 *
 * Called when the device page reports an install event.
 */
def installed() {
  // FIXME: refactor remove for() with fixed #
  for (def i = 1; i <= 12; i++)
    sendEvent(name: "zoneStatus${i}", value: "", displayed: false)
}

/**
 * uninstalled()
 *
 * Called when the device page reports an uninstall event.
 */
def uninstalled() {
  log.trace "--- handler.uninstalled"
}

/**
 * update()
 *
 * Called when the device settings are udpated.
 */
def updated() {
  log.trace "--- handler.updated"

  state.faulted_zones = []

  // Raw panel state values
  state.panel_ready = true
  state.panel_armed = false
  state.panel_armed_stay = false
  state.panel_exit = false
  state.panel_fire_detected = false
  state.panel_alarming = false
  state.panel_panicked = false
  state.panel_on_battery = true
  state.panel_powered = true
  state.panel_chime = true
  state.panel_perimeter_only = false
  state.panel_entry_delay_off = false

  // Calculated alarm state ENUM
  state.panel_state = "disarmed"
  state.alarm_status = "off"
  state.armed = false

  // internal state vars
  state.panic_started = null;
  state.fire_started = null;
  state.aux_started = null;

  // FIXME: refactor remove for() with fixed #
  for (def i = 1; i <= 12; i++)
    sendEvent(name: "zoneStatus${i}", value: "", displayed: false)

  // subscribe if settings change
  unschedule()
  // subscribe right now
  subscribeNotifications()
  // resub every 5min
  runEvery5Minutes(subscribeNotifications)
}

/**
 * parse()
 *
 * Core event parse routine for Device Handler
 *
 * Expect:
 *  Response to SUBSCRIBE requests to the AlarmDecoder.
 *
 * Test from the AlarmDecoder Appliance:
 *   curl
 *
 */
def parse(String description) {
  if (parent.debug)
    log.debug("--- parse: em: ${em}, description: '${description}'")

  // Create our events array we will append into.
  def events = []

  // Parse the event string.
  def em = parent.parseEventMessage(description)

  // Is this a message from the active registerd AlarmDecoder?
  // FIXME: Add some security test.
  if (em.mac != getDataValue("mac")) {
    if (parent.debug)
      log.info("--- parse: skipping event incorrect mac address: ${em.mac}")
    return events
  } else {
    if (parent.debug)
      log.info("--- parse: event match mac: ${em.mac}")
  }

  // If we initiate a connection it will get an id.
  // Just grab the last for loging.
  def rID = ((em.requestId?.length() > 12) ?
    em.requestId.substring(em.requestId.length() - 12) : "000000000000")

  // We need headers for a valid request.
  if (!em.headers) {
    if (parent.debug)
      log.info("--- parse: skipping event missing headers from ${em.mac}")
    return events
  }

  log.info("--- parse: mac: ${event?.mac} requestId: ${rID}")

  // The body may be empty.
  def bodyString = (em.body) ? em.body : ""

  // Verbose debug details.
  if (parent.debug) {
    log.debug("--- parse: type: ${em.contenttype} " +
      "rid: ${em.requestId}, headers: ${em.headers}")
    log.debug("--- parse: rid: ${rID}, body: ${em.body}")
  }

  def type = em.contenttype

  // Based upon content type process the content.
  if (type?.contains("json") && bodyString.length()) {
    parse_json(bodyString).each {
      e-> events << e
    }
  } else if (type?.contains("xml") && bodyString.length()) {
    parse_xml(bodyString).each {
      e-> events << e
    }
  } else {
    if (parent.debug) log.debug("--- parse: unknown type: ${type}")
  }

  if (parent.debug)
    log.debug("--- parse: results rid:${rID}, events: ${events}")

  return events
}



/*** Capabilities ***/

def refresh() {
  log.trace("--- handler.refresh")

  def urn = getDataValue("urn")
  def apikey = _get_api_key()

  return hub_http_get(urn, "/api/v1/alarmdecoder?apikey=${apikey}")
}

/*** Commands ***/
/**
 * disarm()
 * Sends a disarm command to the panel
 * TODO: Add security
 */
def disarm() {
  log.trace("--- disarm")

  def user_code = _get_user_code()
  def keys = ""

  if (settings.panel_type == "ADEMCO")
    keys = "${user_code}1"
  else if (settings.panel_type == "DSC")
    keys = "${user_code}"
  else
    log.warn("--- disarm: unknown panel_type.")

  return send_keys(keys)
}

/**
 * exit()
 * Sends a exit command to the panel
 * TODO: Add security
 */
def exit() {
  log.trace("--- exit")

  def user_code = _get_user_code()
  def keys = ""

  if (settings.panel_type == "ADEMCO")
    keys = "*"
  else if (settings.panel_type == "DSC")
    keys = "S8"
  else
    log.warn("--- exit: unknown panel_type.")

  return send_keys(keys)
}

/**
 * arm_away()
 * Sends an arm away command to the panel
 */
def arm_away() {
  log.trace("--- arm_away")

  def user_code = _get_user_code()
  def keys = ""

  if (settings.panel_type == "ADEMCO")
    keys = "${user_code}2"
  else if (settings.panel_type == "DSC")
    keys = "<S5>"
  else
    log.warn("--- arm_away: unknown panel_type.")

  return send_keys(keys)
}

/**
 * arm_stay()
 * Sends an arm stay command to the panel
 */
def arm_stay() {
  log.trace("--- arm_stay")

  def user_code = _get_user_code()
  def keys = ""

  if (settings.panel_type == "ADEMCO")
    keys = "${user_code}3"
  else if (settings.panel_type == "DSC")
    keys = "<S4>"
  else
    log.warn("--- arm_stay: unknown panel_type.")

  return send_keys(keys)
}

/**
 * fire()
 * Sends an fire alarm command to the panel
 */
def fire() {
  log.trace("--- fire")
  state.fire_started = null
  def keys = "<S1>"
  return send_keys(keys)
}

def fire1() {
  state.fire_started = new Date().time

  runIn(10, checkFire);

  log.trace("Fire stage 1: ${state.fire_started}")
}

def fire2() {
  state.fire_started = new Date().time

  runIn(10, checkFire);

  log.trace("Fire stage 2: ${state.fire_started}")
}

def checkFire() {
  log.trace("checkFire");
  if (state.fire_started != null && new Date().time - state.fire_started >= 5) {
    sendEvent(name: "fire_state", value: "default", isStateChange: true);
    log.trace("clearing fire");
  }
}

/**
 * panic()
 * Sends an panic alarm command to the panel
 */
def panic() {
  log.trace("--- panic")
  state.panic_started = null
  def keys = "<S2>"
  return send_keys(keys)
}

def panic1() {
  state.panic_started = new Date().time

  runIn(10, checkPanic);

  log.trace("Panic stage 1: ${state.panic_started}")
}

def panic2() {
  state.panic_started = new Date().time

  runIn(10, checkPanic);

  log.trace("Panic stage 2: ${state.panic_started}")
}

def checkPanic() {
  log.trace("checkPanic");
  if (state.panic_started != null &&
    new Date().time - state.panic_started >= 5) {
    sendEvent(name: "panic_state", value: "default", isStateChange: true);
    log.trace("clearing panic");
  }
}

/**
 * aux()
 * Sends an aux alarm command to the panel
 */
def aux() {
  log.trace("--- aux")
  state.aux_started = null
  def keys = "<S3>"
  return send_keys(keys)
}

def aux1() {
  state.aux_started = new Date().time

  runIn(10, checkAux);

  log.trace("Aux stage 1: ${state.aux_started}")
}

def aux2() {
  state.aux_started = new Date().time

  runIn(10, checkAux);

  log.trace("Aux stage 2: ${state.aux_started}")
}

def checkAux() {
  log.trace("checkAux");
  if (state.aux_started != null && new Date().time - state.aux_started >= 5) {
    sendEvent(name: "aux_state", value: "default", isStateChange: true);
    log.trace("clearing aux");
  }
}

/**
 * bypassX()
 *
 */
def bypass1(evt) { bypassN(1) }
def bypass2(evt) { bypassN(2) }
def bypass3(evt) { bypassN(3) }
def bypass4(evt) { bypassN(4) }
def bypass5(evt) { bypassN(5) }
def bypass6(evt) { bypassN(6) }
def bypass7(evt) { bypassN(7) }
def bypass8(evt) { bypassN(8) }
def bypass9(evt) { bypassN(9) }
def bypass10(evt) { bypassN(10) }
def bypass11(evt) { bypassN(11) }
def bypass12(evt) { bypassN(12) }

/**
 * bypassN()
 * Bypass the zone indicated on this tile
 */
def bypassN(szValue) {
  def zone = device.currentValue("zoneStatus${szValue}")
  bypass(zone)
}

/**
 * bypass()
 * Send a bypass command to the panel for a zone number.
 */
def bypass(zone) {
  log.trace("--- bypass ${zone}")

  // if no zone then skip
  if (!zone.toInteger())
    return;

  def user_code = _get_user_code()
  def keys = ""

  if (settings.panel_type == "ADEMCO")
    keys = "${user_code}6" + zone.toString().padLeft(2, "0") + "*"
  else if (settings.panel_type == "DSC")
    keys = "*1" + zone.toString().padLeft(2, "0") + "#"
  else
    log.warn("--- bypass: unknown panel_type.")

  return send_keys(keys)
}

/**
 * chime()
 * Sends a chime command to the panel
 */
def chime() {
  log.trace("--- chime")
  def user_code = _get_user_code()
  def keys = ""

  if (settings.panel_type == "ADEMCO")
    keys = "${user_code}9*"
  else if (settings.panel_type == "DSC")
    keys = "<S6>"
  else
    log.warn("--- chime: unknown panel_type.")

  return send_keys(keys)
}

/*** Business Logic ***/

/**
 * update_state(data)
 * process a state change event object from xml/json parser
 */
def update_state(data) {
  log.trace("--- update_state")
  def forceguiUpdate = false
  def skipstate = false
  def events = []

  /*
   * WARNING: we can get the Ready and Armed state events out of
   * order thanks to async io and the fact that both events occure
   * from a single AD2 message.Handle this gracefully to avoid client
   * update glitches.
   *
   * [10000001000100003A0-],008,[f702...],"****DISARMED****  Ready to Arm  "
   * [00100301000100003A0-],008,[f702...],"ARMED ***STAY***You may exit now"
   * [10000101000100003A0-],008,[f702...],"****DISARMED****  Ready to Arm  "
   */

  // Event Type 14 CID send raw data upstream if we find one
  if (data.eventid == 14) {
    events << createEvent(
      name: "cid-set",
      value: data.rawmessage,
      displayed: true,
      isStateChange: true)

    // do not trust state for these messages they could be out of sync.
    skipstate = true;
  }

  // Event Type 17 RFX send eventmessage upstream if we find one
  if (data.eventid == 17) {
    events << createEvent(
      name: "rfx-set",
      value: data.eventmessage,
      displayed: true,
      isStateChange: true)
    // do not trust state for these messages they could be out of sync.
    skipstate = true;
  }

  // Event Type 18 EXP/REL send eventmessage upstream if we find one
  if (data.eventid == 18) {
    events << createEvent(
      name: "exp-set",
      value: data.eventmessage,
      displayed: true,
      isStateChange: true)
    // do not trust state for these messages they could be out of sync.
    skipstate = true;
  }

  // Event Type 19 AUI send eventmessage upstream if we find one
  if (data.eventid == 19) {
    events << createEvent(
      name: "aui-set",
      value: data.eventmessage,
      displayed: true,
      isStateChange: true)
    // do not trust state for these messages they could be out of sync.
    skipstate = true;
  }

  // Event Type 5 Bypass
  if (data.eventid == 5) {
    events << createEvent(
      name: "bypass-set",
      value: (data.panel_bypassed ? "on" : "off"),
      displayed: true,
      isStateChange: true)
    // do not trust state for these messages they could be out of sync.
    skipstate = true;
  }

  // Event Type 16 Chime
  if (data.eventid == 16) {
    events << createEvent(
      name: "chime-set",
      value: (data.panel_chime ? "on" : "off"),
      displayed: true,
      isStateChange: true)
    // do not trust state for these messages they could be out of sync.
    skipstate = true;
  }

  // SKIP if needed
  if (skipstate != true) {
    // Get our armed state from our new state data
    def armed = data.panel_armed ||
      (data.panel_armed_stay != null && data.panel_armed_stay == true)

    def panel_state = (data.panel_ready ? "ready" : "notready")

    // Update our ready status virtual device
    if (forceguiUpdate || data.panel_ready != state.panel_ready)
      events << createEvent(
        name: "ready-set",
        value: (data.panel_ready ? "on" : "off"),
        displayed: true,
        isStateChange: true)

    // If armed update internal UI state to exit mode and armed state
    if (armed) {
      if (data.panel_exit) {
        if (data.panel_armed_stay) {
          panel_state = "armed_stay_exit"
        } else {
          panel_state = "armed_exit"
        }
      } else {
        panel_state = (data.panel_armed_stay ? "armed_stay" : "armed")
      }
    }

    // FORCE ARMED if ALARMING to be sure MONITOR gets it as it will
    // not show alarms if not armed :(
    if (data.panel_alarming) {
      panel_state = "alarming"
    }

    // NOTE: Fire overrides alarm since it's definitely more serious.
    if (data.panel_fire_detected) {
      panel_state = "fire"
    }

    // Update our Smoke Sensor virtual device that MONITOR or
    // others the current state.
    if (forceguiUpdate || data.panel_fire_detected != state.panel_fire_detected)
      events << createEvent(
        name: "smoke-set",
        value: (data.panel_fire_detected ? "detected" : "clear"),
        displayed: true,
        isStateChange: true)

    // If armed STAY changes data.panel_armed_stay
    if (forceguiUpdate || data.panel_armed_stay != state.panel_armed_stay) {
      if (data.panel_armed_stay) {
        events << createEvent(
          name: "arm-stay-set",
          value: "on",
          displayed: true,
          isStateChange: true)
        events << createEvent(
          name: "arm-away-set",
          value: "off",
          displayed: true,
          isStateChange: true)
      }
    }

    // If the panel ARMED state changes
    if (forceguiUpdate || armed != state.armed) {
      if (!armed) {
        events << createEvent(
          name: "arm-away-set",
          value: "off",
          displayed: true,
          isStateChange: true)
        events << createEvent(
          name: "arm-stay-set",
          value: "off",
          displayed: true,
          isStateChange: true)
        events << createEvent(
          name: "disarm-set",
          value: "off",
          displayed: true,
          isStateChange: true)
      } else {
        // If armed AWAY changes data.panel_armed_away
        if (!data.panel_armed_stay)
          events << createEvent(
            name: "arm-away-set",
            value: "on",
            displayed: true,
            isStateChange: true)
        events << createEvent(
          name: "disarm-set",
          value: "on",
          displayed: true,
          isStateChange: true)
      }
    }

    // If Update exit state
    if (forceguiUpdate || data.panel_exit != state.panel_exit) {
      events << createEvent(
        name: "exit-set",
        value: (data.panel_exit ? "on" : "off"),
        displayed: true,
        isStateChange: true)
    }

    // If Update perimeter only device
    if (forceguiUpdate ||
      data.panel_perimeter_only != state.panel_perimeter_only) {
      events << createEvent(
        name: "perimeter-only-set",
        value: (data.panel_perimeter_only ? "on" : "off"),
        displayed: true,
        isStateChange: true)
    }

    // If Update entry delay off device
    if (forceguiUpdate ||
      data.panel_entry_delay_off != state.panel_entry_delay_off) {
      events << createEvent(
        name: "entry-delay-off-set",
        value: (data.panel_entry_delay_off ? "on" : "off"),
        displayed: true,
        isStateChange: true)
    }

    // set our panel_state
    if (forceguiUpdate || panel_state != state.panel_state) {
      log.trace("--- update_state: new state **** ${panel_state} ****")
      events << createEvent(
        name: "panel_state",
        value: panel_state,
        displayed: true,
        isStateChange: true)
    }

    // build our alarm_status value
    def alarm_status = "off"
    if (armed) {
      alarm_status = "away"
      if (data.panel_armed_stay == true)
        alarm_status = "stay"
    }

    // Create an event to notify Smart Home Monitor in our service.
    // "enum", ["off", "stay", "away"]
    if (forceguiUpdate || alarm_status != state.alarm_status)
      events << createEvent(
        name: "alarmStatus",
        value: alarm_status,
        displayed: true,
        isStateChange: true)

    // Update our alarming switch so MONITORs know we are in an alarm state.
    //  In alarm close contact.
    // "enum", ["open", "closed"]
    if (forceguiUpdate || data.panel_alarming != state.panel_alarming)
      events << createEvent(
        name: "alarmbell-set",
        value: (data.panel_alarming ? "on" : "off"),
        displayed: true,
        isStateChange: true)

    // will only add events for zones that change state.
    def zone_events = build_zone_events(data)
    events = events.plus(zone_events)

    // Update our saved state
    /// Calculated state enum
    state.panel_state = panel_state
    state.armed = armed

    /// raw panel state bits
    state.panel_ready = data.panel_ready
    state.panel_armed = data.panel_armed
    state.panel_armed_stay = data.panel_armed_stay
    state.panel_exit = data.panel_exit
    state.panel_fire_detected = data.panel_fire_detected
    state.panel_alarming = data.panel_alarming
    state.alarm_status = alarm_status
    state.panel_powered = data.panel_powered
    state.panel_on_battery = data.panel_on_battery
    state.panel_ready = data.panel_ready
    state.panel_chime = data.chime
    state.panel_perimeter_only = data.panel_perimeter_only
    state.panel_entry_delay_off = data.panel_entry_delay_off
  }
  return events
}

/*** Utility ***/

/**
 * parse_json(String body)
 *
 * Parse json response data from a PULL request made to the
 * AlarmDecoder REST api into an event object and
 * send to update_state().
 */
def parse_json(String body) {
  def events = []

  try {
    def slurper = new JsonSlurper()
    def result = slurper.parseText(body)

    // Build our events list from our current state
    update_state(result).each {
      e-> events << e
    }

    if (parent.debug) log.debug("parse_json in:****** ${resultMap}")
    if (parent.debug) log.debug("parse_json out:****** ${events}")

  } catch (Exception e) {
    log.error("parse_json: Exception ${e}")
  }

  return events
}

/**
 * parse_xml(String body)
 *
 * Parse xml data from a UPNP PUSH message from the AlarmDecoder
 * push notification service into an event object and
 * send to update_state().
 */
def parse_xml(String body) {
  def events = []

  try {
    def xmlResult = new XmlSlurper().parseText(body)

    def resultMap = [: ]
    resultMap['eventid'] =
      xmlResult.property.eventid.toInteger()
    resultMap['eventdesc'] =
      xmlResult.property.eventdesc.text()
    resultMap['eventmessage'] =
      xmlResult.property.eventmessage.text()
    resultMap['rawmessage'] =
      xmlResult.property.rawmessage.text()
    resultMap['last_message_received'] =
      xmlResult.property.panelstate.last_message_received.text()
    resultMap['panel_alarming'] =
      xmlResult.property.panelstate.panel_alarming.toBoolean()
    resultMap['panel_armed'] =
      xmlResult.property.panelstate.panel_armed.toBoolean()
    resultMap['panel_armed_stay'] =
      xmlResult.property.panelstate.panel_armed_stay.toBoolean()
    resultMap['panel_exit'] =
      xmlResult.property.panelstate.panel_exit.toBoolean()
    resultMap['panel_bypassed'] =
      xmlResult.property.panelstate.panel_bypassed.toBoolean()
    resultMap['panel_fire_detected'] =
      xmlResult.property.panelstate.panel_fire_detected.toBoolean()
    resultMap['panel_on_battery'] =
      xmlResult.property.panelstate.panel_on_battery.toBoolean()
    resultMap['panel_panicked'] =
      xmlResult.property.panelstate.panel_panicked.toBoolean()
    resultMap['panel_powered'] =
      xmlResult.property.panelstate.panel_powered.toBoolean()
    resultMap['panel_ready'] =
      xmlResult.property.panelstate.panel_ready.toBoolean()
    resultMap['panel_entry_delay_off'] =
      xmlResult.property.panelstate.panel_entry_delay_off.toBoolean()
    resultMap['panel_perimeter_only'] =
      xmlResult.property.panelstate.panel_perimeter_only.toBoolean()
    resultMap['panel_type'] =
      xmlResult.property.panelstate.panel_type.text()
    resultMap['panel_chime'] =
      xmlResult.property.panelstate.panel_chime.toBoolean()

    // build list of faulted zones unpack xml
    // only update zone list on zone change events
    // <eventmessage>
    //   <![CDATA[Zone <unnamed> (9) has been faulted.]]>
    // </eventmessage>
    if (xmlResult.property.eventmessage.text().startsWith('Zone ')) {
      def zones = []
      xmlResult.property.panelstate.panel_zones_faulted.z.each {
        e-> zones << e.toInteger()
      }
      resultMap['panel_zones_faulted'] = zones
    }

    // unpack the relay xml
    def relays = []
    xmlResult.property.panelstate.panel_relay_status.r.each {
      e->
        relays << ['address': e.a, 'channel': e.c, 'value': e.v]
    }
    resultMap['panel_relay_status'] = relays

    // Build our events list from our current state
    update_state(resultMap).each {
      e-> events << e
    }

    if (parent.debug) log.debug("parse_xml in:****** ${resultMap}")
    if (parent.debug) log.debug("parse_xml out:****** ${events}")

  } catch (Exception e) {
    log.error("parse_xml: Exception ${e}")
  }

  return events
}

/**
 * subscribeNotifications()
 *
 * Send a SUBSCRIBE request to the AlarmDecoder UPNP notification service.
 * To receive event PUSH notifications.
 *
 * Note:
 *  Be sure to enable the AlarmDecoder UPNP notification service.
 *
 * FIXME: Need to get this from the eventSubURL in the
 * ssdpPath: /static/device_description.xml
 */
def subscribeNotifications() {
  if (parent.debug)
    log.trace "--- subscribeNotifications: ${getDataValue("urn")}"

  // Get our HUBs address details for callbacks.
  def address = parent.getHubURN()

  // Build the SUBSCRIBE request.
  def obj = [
    method: "SUBSCRIBE",
    path: "/api/v1/alarmdecoder/event?apikey=${_get_api_key()}",
    headers: [
      HOST: getDataValue("urn"),
      CALLBACK: "<http://${address}/notify${callbackPath}>",
      NT: "upnp:event",
      TIMEOUT: "Second-28800"
    ]
  ]

  // Build the HubAction object.
  def ha = parent.getHubAction(obj, address)

  // Tag the HubAction message. It will return in the event handler parse().
  // FIXME: Add some security test.
  if (parent.isSmartThings())
    ha.requestId = "SUBSCRIBE"

  sendHubCommand(ha)
}


/**
 * build_zone_events(data)
 *
 * Parse the zone events and return a list of events
 * for each zone filtered to report only events with
 * a changes in state.
 */
private def build_zone_events(data) {
  def events = []

  // TODO: probably remove this.
  if (state.faulted_zones == null)
    state.faulted_zones = []

  // If we have no tag then do nothing.
  def current_faults = data.panel_zones_faulted
  if (current_faults == null)
    return events

  def number_of_zones_faulted = current_faults.size()

  def new_faults = current_faults.minus(state.faulted_zones)
  def cleared_faults = state.faulted_zones.minus(current_faults)

  if (parent.debug) {
    log.trace("Current faulted zones: ${current_faults}")
    log.trace("New faults: ${new_faults}")
    log.trace("Cleared faults: ${cleared_faults}")
  }

  // Trigger switches for newly faulted zones.
  for (def i = 0; i < new_faults.size(); i++) {
    if (parent.debug) log.trace("Setting switch ${new_faults[i]}")
    def switch_events = update_zone_switch(new_faults[i], true)
    events = events.plus(switch_events)
  }

  // Reset switches for cleared zones.
  for (def i = 0; i < cleared_faults.size(); i++) {
    if (parent.debug) log.trace("Clearing switch ${cleared_faults[i]}")
    def switch_events = update_zone_switch(cleared_faults[i], false)
    events = events.plus(switch_events)
  }

  // FIXME: refactor to remove MAX_ZONE_FAULT_TILES
  // Populate AlarmDecoder UI zone tiles
  for (def i = 1; i <= MAX_ZONE_FAULT_TILES; i++) {
    if (number_of_zones_faulted > 0 && i <= number_of_zones_faulted) {
      def d = (device.currentValue("zoneStatus${i}") ?: "0")
      if (d.toInteger() != current_faults[i - 1])
        events << createEvent(
          name: "zoneStatus${i}",
          value: current_faults[i - 1],
          displayed: true)
    } else {
      if (device.currentValue("zoneStatus${i}") != null)
        events << createEvent(
          name: "zoneStatus${i}",
          value: "",
          displayed: true)
    }
  }

  state.faulted_zones = current_faults

  return events
}

/**
 * update_zone_switch(zone, faulted)
 *
 * Send a zone update event to the AlarmDecoder service for processing.
 * This sends the raw zone number and it is up to the service to route
 * this event to the correct virtual switch.
 *
 */
private def update_zone_switch(zone, faulted) {
  def events = []

  if (faulted) {
    events << createEvent(
      name: "zone-on",
      value: zone,
      isStateChange: true,
      displayed: false
    )
  } else {
    events << createEvent(
      name: "zone-off",
      value: zone,
      isStateChange: true,
      displayed: false
    )
  }

  return events
}

/**
 * send_keys(keys)
 *
 * Build a GET request HubAction object to call the
 * AlarmDecoder REST api send command.
 */
def send_keys(String keys) {
  if (parent.debug)
    log.trace("--- send_keys: keys=${keys}")
  else
    log.trace("--- send_keys")

  def urn = getDataValue("urn")
  def apikey = _get_api_key()

  return hub_http_post(
    urn,
    "/api/v1/alarmdecoder/send?apikey=${apikey}",
    """{ "keys": "${keys}" }"""
  )
}

/**
 * hub_http_get()
 *
 * Build a GET request HubAction object.
 */
def hub_http_get(host, path) {
  if (parent.debug)
    log.trace "--- hub_http_get: host=${host}, path=${path}"

  def httpRequest = [
    method: "GET",
    path: path,
    headers: [HOST: host]
  ]

  return parent.getHubAction(httpRequest, host)
}

/**
 * hub_http_post()
 *
 * Build a POST request HubAction object.
 */
def hub_http_post(host, path, body) {
  if (parent.debug)
    log.trace "--- hub_http_post: host=${host}, path=${path} body=${body}"

  def httpRequest = [
    method: "POST",
    path: path,
    headers: [HOST: host, "Content-Type": "application/json"],
    body: body
  ]

  return parent.getHubAction(httpRequest, host)
}

/**
 * _get_user_code()
 *
 * Internal routine to return the current user_code setting
 * for the connected AlarmDecoder WEBAPP
 */
def _get_user_code() {
  return settings.user_code
}

/**
 * _get_api_key()
 *
 * Internal routine to return the current REST api key setting
 * for the connected AlarmDecoder WEBAPP
 */
def _get_api_key() {
  return settings.api_key
}

/**
 * getStateValue(key)
 *
 * helper parent access to state.
 */
def getStateValue(key) {
  return state[key]
}
