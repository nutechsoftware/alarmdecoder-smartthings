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

// parse events into attributes
def parse(String description) {
    log.debug "Parsing '${description}'"

    def event = parseEventMessage(description)
    if (event?.apikey) {
        state.apikey = event.apikey
    }
    if (event?.urn) {
        state.urn = event.urn
    }

    log.trace "event: ${event}"
}

def updated() {
    log.trace "Updated"
    /*if (!state.subscribed) {
        subscribe(location, null, locationHandler, [filterEvents: false])
        state.subscribed = true
    }*/
}

def uninstalled() {
    log.trace "Uninstalled"
    //unsubscribe()

    state.subscribed = false
}

// handle commands
def refresh() {
    log.debug "Executing 'refresh'"
    // TODO: handle 'refresh' command

    def urn = device.currentValue("urn")
    def apikey = device.currentValue("apikey")

    return hub_http_get(urn, "/api/v1/alarmdecoder?apikey=${apikey}")
}

// Switch - STAY
def on() {
    log.trace "on()"
}

def off() {
    log.trace "off()"
}

// Lock - AWAY
def lock() {
    log.trace "lock()"
}

def unlock() {
    log.trace "unlock()"
}

// Alarm - PANIC
def both() {
    log.trace "both()"


}

def arm_away() {
    def urn = device.currentValue("urn")
    def apikey = device.currentValue("apikey")

    hub_http_get(urn, "/api/v1/alarmdecoder?apikey=${apikey}")
}

def disarm() {
    def urn = device.currentValue("urn")
    def apikey = device.currentValue("apikey")

    hub_http_post(urn, "/api/v1/alarmdecoder/send?apikey=${apikey}", """{ "keys": "41122" }""")
}

// TODO: Do I need to define the rest of the Alarm commands?


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
    log.trace "hub_http_get: host=${host}, path=${path} 2"

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

    sendHubCommand(new physicalgraph.device.HubAction(httpRequest, "${host}"))
}