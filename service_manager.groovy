/**
 *  AlarmDecoder Service Manager
 *
 *  Copyright 2016 Nu Tech Software Solutions, Inc.
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
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    singleInstance: true) { }

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

    // HACK: Work around SmartThings wonky uninstall.  They claim unsubscribe is uncessary, 
    //       but it is, as is the runIn() since everything is asynchronous.  Otherwise events
    //       don't get correctly unbound and the devices can't be deleted because they're in use.
    unschedule()
    unsubscribe()
    runIn(300, do_uninstall)
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
        else {
            def d = devices."${parsedEvent.ssdpUSN.toString()}"
            boolean deviceChangedValues = false

            log.trace "locationHandler: device already exists.. checking for changed values"

            if (d.ip != parsedEvent.ip || d.port != parsedEvent.port) {
                d.ip = parsedEvent.ip
                d.port = parsedEvent.port
                deviceChangedValues = true

                log.trace "locationHandler: device changed values!"
            }

            if (deviceChangedValues) {
                def children = getChildDevices()
                children.each {
                    if (it.getDeviceDataByName("mac") == parsedEvent.mac) {
                        it.setDeviceNetworkId((parsedEvent.ip + ":" + parsedEvent.port))
                        log.trace "Set new network id: " + parsedEvent.ip + ":" + parsedEvent.port
                    }
                }
            }
        }
    }

    // HTTP EVENTS
    if (parsedEvent?.body && parsedEvent?.headers) {
        log.trace "locationHandler: headers=${new String(parsedEvent.headers.decodeBase64())}"
        log.trace "locationHandler: body=${new String(parsedEvent.body.decodeBase64())}"
    }
}

def refreshHandler() {
    log.trace "refreshHandler"

    refresh_alarmdecoders()
}

/*** Commands ***/

def zoneOn(evt) {
    log.trace("zoneOn: desc=${evt.value}")
    
    def d = getChildDevices().find { it.deviceNetworkId.contains("switch${evt.value}") }
    if (d)
        d.sendEvent(name: "contact", value: "closed", isStateChange: true, filtered: true)
}

def zoneOff(evt) {
    log.trace("zoneOff: desc=${evt.value}")
    
    def d = getChildDevices().find { it.deviceNetworkId.contains("switch${evt.value}") }
    if (d)
        d.sendEvent(name: "contact", value: "open", isStateChange: true, filtered: true)
}

/*** Utility ***/

def discover_devices() {
    int refreshInterval = 5
    int refreshCount = !state.refreshCount ? 0 : state.refreshCount as int
    state.refreshCount = refreshCount += 1

    def found_devices = [:]
    def options = state.devices.each { k, v ->
        log.trace "discover_devices: ${v}"
        def ip = convertHexToIP(v.ip)
        found_devices["${v.ip}:${v.port}"] = "AlarmDecoder @ ${ip}"
    }

    def numFound = found_devices.size() ?: 0

    if (!state.subscribed) {
        log.trace "discover_devices: subscribe to location"

        subscribe(location, null, locationHandler, [filterEvents: false])
        state.subscribed = true
    }
    
    discover_alarmdecoder()

    return dynamicPage(name: "discover_devices", title: "Discovery started..", nextPage: "", refreshInterval: refreshInterval, install: true, uninstall: true) {
        section("Please wait.") {
            input "selectedDevices", "enum", required: false, title: "Select device(s) (${numFound} found)", multiple: true, options: found_devices
            // TEMP: REMOVE THIS
            href(name: "refreshDevices", title: "Refresh", required: false, page: "discover_devices")
        }
    }
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

def do_uninstall() {
    def devices = getChildDevices()

    devices.each {
        try {
            log.trace "deleting child device: ${it.deviceNetworkId}"
            deleteChildDevice(it.deviceNetworkId)
        }
        catch(Exception e) {
            log.trace("exception while uninstalling: ${e}")
        }
    }
}

def scheduleRefresh() {
    def minutes = 1

    def cron = "0 0/${minutes} * * * ?"
    schedule(cron, refreshHandler)
}

def refresh_alarmdecoders() {
    log.trace("refresh_alarmdecoders-")
    getAllChildDevices().each { device ->
        // Only refresh the main device.
        if (!device.deviceNetworkId.contains(":switch"))
        {
            log.trace("refresh_alarmdecoders: ${device}")
            device.refresh()
        }
    }
}

def getDevices() {
    if(!state.devices) {
        state.devices = [:]
    }
    
    state.devices
}

def addExistingDevices() {
    log.trace "addExistingDevices: ${selectedDevices}"

    def selected_devices = selectedDevices
    if (selected_devices instanceof java.lang.String) {
        selected_devices = [selected_devices]
    }

    selected_devices.each { dni ->
        def d = getChildDevice(dni)
        log.trace("addExistingDevices, getChildDevice(${dni})")
        if (!d) {
            log.trace("devices=${devices}")
            def newDevice = devices.find { /*k, v -> k == dni*/ k, v -> dni == "${v.ip}:${v.port}" }
            log.trace("addExistingDevices, devices.find=${newDevice}")
            if (newDevice) {
                // Set the device network ID so that hubactions get sent to the device parser.
                def ip = newDevice.value.ip
                def port = newDevice.value.port

                // Create device and subscribe to it's zone-on/off events.
                d = addChildDevice("alarmdecoder", "AlarmDecoder Network Appliance", "${ip}:${port}", newDevice?.value.hub, [name: "${ip}:${port}", label: "AlarmDecoder", completedSetup: true])

                // Set URN and APIKey on the child device
                def urn = newDevice.value.ssdpPath
                urn -= "http://"

                d.sendEvent(name: 'urn', value: urn, displayed: false)

                subscribe(d, "zone-on", zoneOn, [filterEvents: false])
                subscribe(d, "zone-off", zoneOff, [filterEvents: false])

                // Add virtual zone contact sensors.
                for (def i = 0; i < 8; i++)
                {
                    def newSwitch = devices.find { k, v -> k == "${ip}:${port}:switch${i+1}" }
                    if (!newSwitch)
                    {
                        def zone_switch = addChildDevice("alarmdecoder", "VirtualContactSensor", "${ip}:${port}:switch${i+1}", newDevice.value.hub, [name: "${ip}:${port}:switch${i+1}", label: "AlarmDecoder Zone Sensor #${i+1}", completedSetup: true])

                        // Default contact to closed.
                        zone_switch.sendEvent(name: "contact", value: "open", isStateChange: true, displayed: false);
                    }
                }
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

private String convertHexToIP(hex) {
    [convertHexToInt(hex[0..1]),convertHexToInt(hex[2..3]),convertHexToInt(hex[4..5]),convertHexToInt(hex[6..7])].join(".")
}

private Integer convertHexToInt(hex) {
    Integer.parseInt(hex,16)
}