/**
 *  AlarmDecoder Service Manager
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
definition(
    name: "AlarmDecoder Service Manager",
    namespace: "alarmdecoder",
    author: "Scott Petersen",
    description: "AlarmDecoder Service Manager",
    category: "My Apps",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png") {
    /*appSetting "ipAddress"*/}


preferences {
    //page(name: "doDiscovery", title:"Discovery", content: "doDiscovery", refreshTimeout: 5)
}

/*
def selectDevices() {
    log.trace "selectDevices()"
    // get SessionID
    atomicState.sessionID = atomicState.sessionID ?: initSession()["SessionID"]
    
    // get device list
    def devices = [:]
    
    deviceList(atomicState.sessionID)["DeviceList"].each {
        def roomName = it["GroupName"] == "harman" ? "" : "@"+it["GroupName"]
        devices.put(it["DeviceID"], it["DeviceName"] + roomName)
    }
    
    // have user choose devices to work with
    dynamicPage(name: "selectDevices", title: "Select Your Devices", uninstall: true, install:true) {
        section {
            paragraph "Tap below to see the list of HK Speakers available in your network and select the ones you want to connect to SmartThings."
            input(name: "speakers", type: "enum", title: "Speakers", description: "Tap to choose", required: true, options: devices, multiple:true)
        }
    }
}
*/

def installed() {
    log.debug "Installed with settings: ${settings}"
    initialize()
    
    //
}

def updated() {
    log.debug "Updated with settings: ${settings}"
    initialize()
}

def uninstalled() {
    log.trace "uninstalled()"
}

def initialize() {
    log.trace "initialize()"
    
    if (!state.subscribe) {
        log.trace "subscribe to location"
        subscribe(location, null, locationHandler, [filterEvents: false])
        state.subscribe = true
    }
    
    sendHubCommand(new physicalgraph.device.HubAction("lan discovery urn:schemas-upnp-org:device:AlarmDecoder:1", physicalgraph.device.Protocol.LAN))
}



def locationHandler(evt) {
    log.trace "locationHandler()"
    
    def description = evt.description
    def hub = evt?.hubId
    
    def parsedEvent = parseEventMessage(description)
    parsedEvent << ["hub":hub]
    
    if (parsedEvent?.ssdpTerm?.contains("urn:schemas-upnp-org:device:AlarmDecoder:1")) {
        getDevices()
        if (!(devices."${parsedEvent.ssdpUSN.toString()}")) {
            devices << ["${parsedEvent.ssdpUSN.toString()}":parsedEvent]
        }
    }
}

def getDevices() {
    if(!state.devices) {
        state.devices = [:]
    }
    
    state.devices
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

def doDiscovery() {
    
}


// Handle commands




/**************************
 * REST UTILITY FUNCTIONS *
 **************************/

// All REST utility functions simply return the HTTP response data
/*
def initSession() {
    log.trace "initSession()"
    def params = [
        uri: appSettings.ipAddress,
        path: "/v1/init_session"
    ]
    get(params)
}
*/

def get(params) {
    log.trace "get(${params})"
    httpGet(params) { resp ->
        return resp.data
    }
}