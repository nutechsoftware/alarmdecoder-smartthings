/**
 *  AlarmDecoder Network Appliance
 *
 *  Copyright 2015 Scott Petersen
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
 */
import groovy.json.JsonSlurper;

metadata {
    definition (name: "AlarmDecoder Network Appliance", namespace: "alarmdecoder", author: "Scott Petersen") {
        capability "Refresh"
        capability "Switch"
        capability "Lock"
        capability "Alarm"
        capability "smokeDetector"

        attribute "urn", "string"
        attribute "apikey", "string"

        attribute "panel_state", "enum", ["armed", "disarmed", "alarming", "fire"]

        command "disarm"
    }

    simulator {
        // TODO: define status and reply messages here
    }

    tiles(scale: 2) {
        multiAttributeTile(name: "status", type: "generic", width: 6, height: 4) {
            tileAttribute("device.panel_state", key: "PRIMARY_CONTROL") {
                attributeState "armed", label: '${name}', icon: "st.contact.contact.open", backgroundColor: "#ffa81e"
                attributeState "disarmed", label: '${name}', icon: "st.contact.contact.closed", backgroundColor: "#79b821"
                attributeState "alarming", label: '${name}', icon: "st.contact.contact.closed", backgroundColor: "#79b821"
                attributeState "fire", label: '${name}', icon: "st.contact.contact.closed", backgroundColor: "#79b821"
            }
        }

        standardTile("refresh", "device.refresh", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
            state "default", action:"refresh.refresh", icon:"st.secondary.refresh"
        }

        main(["status"])
        details(["status", "refresh"])
    }
}

/*** Handlers ***/

def updated() {
    log.trace "Updated"
}

def uninstalled() {
    log.trace "Uninstalled"
}

// parse events into attributes
def parse(String description) {
    log.debug "Parsing '${description}'"

    def events = []
    def event = parseEventMessage(description)

    log.trace "new event: ${event}"

    // HTTP
    if (event?.body && event?.headers) {
        def slurper = new JsonSlurper()
        String bodyText = new String(event.body.decodeBase64())
        def result = slurper.parseText(bodyText)

        log.trace "http result=${result}"

        // TODO: Fix null entries in events?
        set_away(result.panel_armed).each { e -> events << e }
        set_fire(result.panel_fire_detected).each { e -> events << e }
        set_alarming(result.panel_alarming).each { e -> events << e }
    }

    log.trace "resulting events=${events}"

    return events
}

/*** Capabilities ***/

// NOTE: Do I really need these?  Should they make calls to the API to arm/disarm?

def on() {
    log.trace("on()")
    set_stay(true)
}

def off() {
    log.trace("off()")
    set_stay(false)
    set_alarming(false)
}

def strobe() {
    log.trace("strobe() - do nothing")
}

def siren() {
    log.trace("siren() - do nothing")
}

def both() {
    log.trace("both() - panic")

    // TODO: panic here.
    set_alarming(true)
}

def lock() {
    log.trace("lock()")
    set_away(true)
}

def unlock() {
    log.trace("unlock()")
    set_away(false)
}

// Switch - STAY
def set_stay(value) {
    if (state.stay == value)
        return

    log.trace "set_stay(${value})"
    state.stay = value

    def events = []

    def event_value = "off"
    if (value) {
        event_value = "on"
        events << device.createEvent(name: "ad_status", value: "armed")
    }
    else
        events << device.createEvent(name: "ad_status", value: "disarmed")

    events << createEvent(name: "switch", value: event_value)

    return events
}

// Lock - AWAY
def set_away(value) {
    if (state.away == value)
        return

    log.trace "set_away(${value})"
    state.away = value

    def events = []

    def event_value = "unlocked"
    if (value) {
        event_value = "locked"
        events << device.createEvent(name: "ad_status", value: "armed")
    }
    else
        events << createEvent(name: "ad_status", value: "disarmed")

    events << createEvent(name: "lock", value: event_value)

    return events
}

// Alarm
def set_alarming(value) {
    if (state.alarming == value)
        return

    log.trace "set_alarming(${value})"
    state.alarming = value

    def events = []

    def event_value = "off"
    if (value) {
        event_value = "both"
        events << device.createEvent(name: "ad_status", value: "alarming")
    }

    events << createEvent(name: "alarm", value: event_value)

    return events
}

// smokeDetector - FIRE
def set_fire(value) {
    if (state.fire == value)
        return

    log.trace "set_fire(${value})"
    state.fire = value

    def events = []

    def event_value = "clear"
    if (value) {
        event_value = "detected"
        events << device.createEvent(name: "ad_status", value: "fire")
    }

    events << createEvent(name: "smoke", value: event_value)

    return events
}

/*** Commands ***/

def refresh() {
    log.debug "Executing 'refresh'"

    def urn = device.currentValue("urn")
    def apikey = device.currentValue("apikey")

    return hub_http_get(urn, "/api/v1/alarmdecoder?apikey=${apikey}")
}

def disarm() {
    def urn = device.currentValue("urn")
    def apikey = device.currentValue("apikey")

    return hub_http_post(urn, "/api/v1/alarmdecoder/send?apikey=${apikey}", """{ "keys": "41122" }""")
}

def arm_away() {
    def urn = device.currentValue("urn")
    def apikey = device.currentValue("apikey")

    return hub_http_get(urn, "/api/v1/alarmdecoder?apikey=${apikey}")
}

/*** Utility ***/

private def parseEventMessage(String description) {
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

def hub_http_get(host, path) {
    log.trace "hub_http_get: host=${host}, path=${path}"

    def httpRequest = [
        method:     "GET",
        path:       path,
        headers:    [ HOST: host ]
    ]

    return new physicalgraph.device.HubAction(httpRequest, "${host}")
}

def hub_http_post(host, path, body) {
    log.trace "hub_http_get: host=${host}, path=${path}"

    def httpRequest = [
        method:     "POST",
        path:       path,
        headers:    [ HOST: host, "Content-Type": "application/json" ],
        body:       body
    ]

    return new physicalgraph.device.HubAction(httpRequest, "${host}")
}