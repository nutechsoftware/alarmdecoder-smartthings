/**
 *  Virtual Motion Detector for alarm panel zones
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

/*
 * global support
 */
import groovy.transform.Field
@Field APPNAMESPACE = "alarmdecoder"

metadata {
  definition(
    name: "AlarmDecoder virtual motion detector",
    namespace: APPNAMESPACE,
    author: "Nu Tech Software Solutions, Inc.") {
    capability "Motion Sensor"
	capability "Tamper Alert"
	attribute "low_battery", "bool"
	attribute "last_checkin", "number"
  }

  // tile definitions
  tiles(scale: 2) {
    multiAttributeTile(
      name: "motion",
      type: "generic",
      width: 6, height: 4) {
      tileAttribute(
        "device.motion",
        key: "PRIMARY_CONTROL") {
        attributeState(
          "active",
          label: 'motion',
          icon: "st.motion.motion.active",
          backgroundColor: "#53a7c0")
        attributeState(
          "inactive",
          label: 'no motion',
          icon: "st.motion.motion.inactive",
          backgroundColor: "#ffffff")
      }
    }
    main "motion"
    details "motion"
  }

  preferences {
    input(
      name: "invert",
      type: "bool",
      title: "Invert signal [true,false]",
      description: "Invert signal [true,false]." +
      " Changes ON/OFF,OPEN/CLOSE,DETECTED/CLEAR",
      required: false)
    input(
      name: "zone",
      type: "number",
      title: "Zone Number",
      description: "Zone # required for zone events.",
      required: false)
    input(
      name: "serial", type:
      "string", title: "Serial Number",
      description: "The serial number of an RF device.",
      required: false)
	if (serial != null) {
      input(
        name: "zoneLoop", type:
        "number", title: "Zone Loop", 
		range: "1..4",
        description: "The loop to use for zone open/close.",
        required: true)
      input(
        name: "tamperLoop", type:
        "number", title: "Tamper Loop",
		range: "1..4",
        description: "The loop to use to detect tamper.",
		defaultValue: 4,
        required: false)	  
	}	  
  }
}

/**
 * installed()/updated()
 *
 * It is not possible for a service to access preferences directly so
 * update device data value to allow access from parent
 * using getDeviceDataByName getDataValue
 * FIXME: diff ^ docs not clear.
 *
 */
def installed() {
  updateDataValue("invert", invert.toString())
  updateDataValue("zone", zone.toString())
  updateDataValue("serial", serial)
  updateDataValue("zoneLoop", zoneLoop.toString())
  updateDataValue("tamperLoop", tamperLoop.toString())
}

def updated() {
  updateDataValue("invert", invert.toString())
  updateDataValue("zone", zone.toString())
  updateDataValue("serial", serial)
  updateDataValue("zoneLoop", zoneLoop.toString())
  updateDataValue("tamperLoop", tamperLoop.toString())
}

// FIXME: what?
def parse(String description) {
  if (description != "updated") {
    if (parent.debug)
      log.info "parse returned:${description}"
    def pair = description.split(":")
    createEvent(name: pair[0].trim(), value: pair[1].trim())
  }
}

def active() {
  sendEvent(name: "motion", value: "active")
}

def inactive() {
  sendEvent(name: "motion", value: "inactive")
}
