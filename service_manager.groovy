/**
 *  AlarmDecoder Service Manager
 *
 *  Copyright 2015 Nu Tech Software Solutions, Inc.
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
definition(
    name: "AlarmDecoder (Service Manager)",
    namespace: "alarmdecoder",
    author: "Nu Tech Software Solutions, Inc.",
    description: "AlarmDecoder (Service Manager)",
    category: "My Apps",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png") {
    /*appSetting "ipAddress"*/}

preferences {
    page(name: "main", title: "Discover your AlarmDecoder", install: true, uninstall: true) {
        section("") {
            href(name: "discover", title: "Discover", required: false, page: "discover_devices", description: "Tap to discover")
        }
    }
    page(name: "discover_devices", title: "Discovery started..", content: "discover_devices", refreshTimeout: 5)
}

/*** Handlers ***/

def installed() {
    log.debug "Installed with settings: ${settings}"

    initialize()
}

def updated() {
    log.debug "Updated with settings: ${settings}"

    unschedule()
    initialize()
}

def uninstalled() {
    log.trace "uninstalled"

    def devices = getChildDevices()
    devices.each {
        try {
            deleteChildDevice(it.deviceNetworkId)            
        }
        catch(Exception e) {
            
        }
    }
}

def initialize() {
    log.trace "initialize"

    unsubscribe()
    state.subscribed = false

    unschedule()

    if (selectedDevices) {
        addExistingDevices()
    }

    scheduleRefresh()
}

def locationHandler(evt) {
    log.trace "locationHandler"
    
    def description = evt.description
    def hub = evt?.hubId
    
    log.trace "locationHandler: description=${description}"
    
    def parsedEvent = parseEventMessage(description)
    parsedEvent << ["hub":hub]

    // LAN EVENTS
    if (parsedEvent?.ssdpTerm?.contains("urn:schemas-upnp-org:device:AlarmDecoder:1")) {
        getDevices()
        
        if (!(devices."${parsedEvent.ssdpUSN.toString()}")) {
            log.trace "locationHandler: Adding device: ${parsedEvent.ssdpUSN}"

            devices << ["${parsedEvent.ssdpUSN.toString()}": parsedEvent]
        }
    }

    // HTTP EVENTS
    if (parsedEvent?.body && parsedEvent?.headers) {
        log.trace "locationHandler: headers=${new String(parsedEvent.headers.decodeBase64())}"
        log.trace "locationHandler: body=${new String(parsedEvent.body.decodeBase64())}"
    }
}

def discover_devices() {
    int refreshInterval = 5
    int refreshCount = !state.refreshCount ? 0 : state.refreshCount as int
    state.refreshCount = refreshCount += 1

    def found_devices = [:]
    def options = state.devices.each { k, v ->
        log.trace "discover_devices: ${v}"
        def ip = convertHexToIP(v.ip)
        found_devices["${k}"] = "AlarmDecoder @ ${ip}"
    }

    def numFound = found_devices.size() ?: 0

    if (!state.subscribed) {
        log.trace "discover_devices: subscribe to location"

        subscribe(location, null, locationHandler, [filterEvents: false])
        state.subscribed = true
    }
    
    discover_alarmdecoder()
    verify_devices()

    return dynamicPage(name: "discover_devices", title: "Discovery started..", nextPage: "", refreshInterval: refreshInterval, install: true, uninstall: true) {
        section("Please wait.") {
            input "selectedDevices", "enum", required: false, title: "Select device(s) (${numFound} found)", multiple: true, options: found_devices
            // TEMP: REMOVE THIS
            href(name: "refreshDevices", title: "Refresh", required: false, page: "discover_devices")
        }
    }
}

def verify_devices() {
/*   
    def path = parsedEvent.ssdpPath
    path -= "http://"

    state.device_path = path

    //hub_http_get(path, "/api/v1/alarmdecoder?apikey=5")
*/
}


/*** Commands ***/



/*** Utility ***/

def scheduleRefresh() {
    def minutes = 1

    def cron = "0 0/${minutes} * * * ?"
    schedule(cron, refreshHandler)
}

def refreshHandler() {
    log.trace "refreshHandler"

    //discover_alarmdecoder()

    refresh_alarmdecoders()
}

def discover_alarmdecoder() {
    log.trace "discover_alarmdecoder"

    if (!state.subscribed) {
        log.trace "discover_alarmdecoder: subscribing!"
        subscribe(location, null, locationHandler, [filterEvents: false])
        state.subscribed = true
    }

    sendHubCommand(new physicalgraph.device.HubAction("lan discovery urn:schemas-upnp-org:device:AlarmDecoder:1", physicalgraph.device.Protocol.LAN))
}

def getDevices() {
    if(!state.devices) {
        state.devices = [:]
    }
    
    state.devices
}

def refresh_alarmdecoders() {
    log.trace("refresh_alarmdecoders-")
    getAllChildDevices().each { device ->
        log.trace("refresh_alarmdecoders: ${device}")

        device.refresh()
    }
}

def addExistingDevices() {
    log.trace "addExistingDevices: ${selectedDevices}"

    def selected_devices = selectedDevices
    if (selected_devices instanceof java.lang.String) {
        selected_devices = [selected_devices]
    }

    selected_devices.each { dni ->
        def d = getChildDevice(dni)
        if (!d) {
            def newDevice = devices.find { k, v -> k == dni }
            if (newDevice) {
                d = addChildDevice("alarmdecoder", "AlarmDecoder Network Appliance", dni, newDevice?.value.hub)

                // Set the device network ID so that hubactions get sent to the device parser.
                def ip = newDevice.value.ip
                def port = newDevice.value.port
                d.deviceNetworkId = "${ip}:${port}"

                // Set URN and APIKey on the child device
                def urn = newDevice.value.ssdpPath
                urn -= "http://"

                d.sendEvent(name: 'urn', value: urn)
                d.sendEvent(name: 'apikey', value: 5)

                // Force a status refresh
                d.refresh()
            }
        }
    }
}

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

    sendHubCommand(new physicalgraph.device.HubAction(httpRequest, "${host}"))
}

def http_get(params) {
    log.trace "http_get: ${params}"

    httpGet(params) { resp ->
        return resp.data
    }
}

private String convertHexToIP(hex) {
    [convertHexToInt(hex[0..1]),convertHexToInt(hex[2..3]),convertHexToInt(hex[4..5]),convertHexToInt(hex[6..7])].join(".")
}

private Integer convertHexToInt(hex) {
    Integer.parseInt(hex,16)
}

def do_stuff() {
    log.trace "doing stuff!!!!"
}