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

        attribute "urn", "string"
        attribute "apikey", "string"

        command "disarm"
    }

    simulator {
        // TODO: define status and reply messages here
    }

    tiles {
        // TODO: define your main and details tiles here
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

    log.trace "event: ${event}"

    // HTTP
    if (event?.body && event?.headers) {
        def slurper = new JsonSlurper()
        String bodyText = new String(event.body.decodeBase64())
        def result = slurper.parseText(bodyText)

        if (result.panel_armed == true) {
            events << set_away(true)
        }
        else {
            events << set_away(false)
        }

        log.trace "result=${result}"
    }

    return events
}

/*** Capabilities ***/

// Switch - STAY
def set_stay(value) {
    log.trace "set_stay(${value})"

    def event_value = "off"
    if (value)
        event_value = "on"

    return createEvent(name: "switch", value: event_value)
}

// Lock - AWAY
def set_away(value) {
    log.trace "set_away(${value})"

    def event_value = "unlocked"
    if (value)
        event_value = "locked"

    return createEvent(name: "lock", value: event_value)
}

// Alarm - PANIC
def set_alarming(value) {
    log.trace "set_alarming(${value})"

    def event_value = "off"
    if (value)
        event_value = "both"

    return createEvent(name: "alarm", value: event_value)
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