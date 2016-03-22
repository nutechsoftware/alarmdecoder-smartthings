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

preferences {
    input("api_key", "text", title: "API Key", description: "The key to access the REST API")
    input("user_code", "text", title: "Alarm Code", description: "The user code for the panel")
}

metadata {
    definition (name: "AlarmDecoder Network Appliance", namespace: "alarmdecoder", author: "Scott Petersen") {
        capability "Refresh"
        capability "Switch"             // STAY
        capability "Lock"               // AWAY
        capability "Alarm"              // PANIC
        capability "smokeDetector"      // FIRE

        attribute "urn", "string"
        attribute "panel_state", "enum", ["armed", "armed_stay", "disarmed", "alarming", "fire"]

        command "disarm"
        command "arm_stay"
        command "arm_away"
        command "panic"

        command "teststuff" // TEMP
    }

    simulator {
        // TODO: define status and reply messages here
    }

    tiles(scale: 2) {
        multiAttributeTile(name: "status", type: "generic", width: 6, height: 4) {
            tileAttribute("device.panel_state", key: "PRIMARY_CONTROL") {
                attributeState "armed", label: 'Armed', icon: "st.contact.contact.closed", backgroundColor: "#ffa81e"
                attributeState "armed_stay", label: 'Armed (stay)', icon: "st.contact.contact.closed", backgroundColor: "#ffa81e"
                attributeState "disarmed", label: 'Disarmed', icon: "st.contact.contact.open", backgroundColor: "#79b821"
                attributeState "alarming", label: 'Alarming!', icon: "st.contact.contact.open", backgroundColor: "#ff4000"
                attributeState "fire", label: 'Fire!', icon: "st.contact.contact.closed", backgroundColor: "#ff0000"
            }
        }

        standardTile("refresh", "device.refresh", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
            state "default", action:"refresh.refresh", icon:"st.secondary.refresh"
        }

        standardTile("arm_away", "device.arm_away", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
            state "default", action:"lock.lock", icon:"st.locks.lock.locked"
        }

        standardTile("arm_stay", "device.arm_stay", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
            state "default", action:"switch.on", icon:"st.locks.lock.locked"
        }

        standardTile("disarm", "device.disarm", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
            state "default", action:"lock.unlock", icon:"st.locks.lock.unlocked"
        }

        standardTile("teststuff", "device.teststuff", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
            state "default", action:"teststuff", icon:"st.contact.contact.closed"
        }

        main(["status"])
        details(["status", "refresh", "arm_away", "arm_stay", "disarm", "teststuff"])
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
        // TODO: Make this not sucky.
        set_fire(result.panel_fire_detected).each { e -> events << e }
        set_alarming(result.panel_alarming).each { e -> events << e }
        set_away(result.panel_armed).each { e -> events << e }
    }

    log.trace "resulting events=${events}"

    return events
}

/*** Capabilities ***/

// NOTE: Do I really need these?  Should they make calls to the API to arm/disarm?

def on() {
    log.trace("on()")

    return delayBetween([
        arm_stay(),
        refresh()
    ], 2000)
}

def off() {
    log.trace("off()")

    return delayBetween([
        disarm(),
        refresh()
    ], 2000)
}

def strobe() {
    log.trace("strobe() - do nothing")
}

def siren() {
    log.trace("siren() - do nothing")
}

def both() {
    log.trace("both() - panic")

    return delayBetween([
        panic(),
        refresh()
    ], 2000)
}

def lock() {
    log.trace("lock()")

    return delayBetween([
        arm_away(),
        refresh()
    ], 2000)
}

def unlock() {
    log.trace("unlock()")

    return delayBetween([
        disarm(),
        refresh()
    ], 2000)
}

// Switch - STAY
// NOTE: This stuff doesn't work since we don't have a way to tell if we're armed stay or not from the alarmdecoder API.
def set_stay(value) {
    if (state.stay == value)
        return

    log.trace "set_stay(${value})"
    state.stay = value

    def events = []

    def event_value = "off"
    if (value) {
        event_value = "on"
        events << createEvent(name: "panel_state", value: "armed_stay")
    }
    else
        events << createEvent(name: "panel_state", value: "disarmed")

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
        events << createEvent(name: "panel_state", value: "armed")
    }
    else
        events << createEvent(name: "panel_state", value: "disarmed")

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
        events << createEvent(name: "panel_state", value: "alarming")
    }
    else {
        if (state.away)
            events << createEvent(name: "panel_state", value: "armed")
        else
            events << createEvent(name: "panel_state", value: "disarmed")
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
        events << createEvent(name: "panel_state", value: "fire")
    }
    else {
        if (state.away)
            events << createEvent(name: "panel_state", value: "armed")
        else
            events << createEvent(name: "panel_state", value: "disarmed")
    }

    events << createEvent(name: "smoke", value: event_value)

    return events
}

/*** Commands ***/

def refresh() {
    log.debug "Executing 'refresh'"

    def urn = device.currentValue("urn")
    def apikey = _get_api_key()

    return hub_http_get(urn, "/api/v1/alarmdecoder?apikey=${apikey}")
}

def disarm() {
    def user_code = _get_user_code()

    return send_keys("${user_code}1")
}

def arm_away() {
    def user_code = _get_user_code()

    return send_keys("${user_code}2")
}

def arm_stay() {
    def user_code = _get_user_code()

    return send_keys("${user_code}3")
}

def panic() {
    // TODO: This doesn't work in any of the ways i've tried it.  Probably some limitation in groovy or json.  Going to need a panic api route.
    def panic_key = sprintf("%c%c%c", 0x01, 0x01, 0x01)

    return send_keys("${panic_key}")
}

def teststuff() {

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

def send_keys(keys) {
    def urn = device.currentValue("urn")
    def apikey = _get_api_key()

    return hub_http_post(urn, "/api/v1/alarmdecoder/send?apikey=${apikey}", """{ "keys": "${keys}" }""")  // TODO: Change key based on panel type.
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
    log.trace "hub_http_post: host=${host}, path=${path}"

    def httpRequest = [
        method:     "POST",
        path:       path,
        headers:    [ HOST: host, "Content-Type": "application/json" ],
        body:       body
    ]

    return new physicalgraph.device.HubAction(httpRequest, "${host}")
}

def _get_user_code() {
    def user_code = settings.user_code

    // TEMP
    if (user_code == null)
        user_code = "4112"

    return user_code
}

def _get_api_key() {
    def api_key = settings.api_key

    // TEMP
    if (api_key == null)
        api_key = 5

    return api_key
}
