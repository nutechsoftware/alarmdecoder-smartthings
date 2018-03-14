/**
 *  AlarmDecoder Service Manager
 *
 *  Copyright 2016-2018 Nu Tech Software Solutions, Inc.
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
    name: "alarmdecoder service",
    namespace: "alarmdecoder",
    author: "Nu Tech Software Solutions, Inc.",
    description: "AlarmDecoder (Service Manager)",
    category: "My Apps",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    singleInstance: true) { }

preferences {
    page name: "main"
    page(name: "discover_devices", title: "Discovery started..", content: "discover_devices", refreshTimeout: 5)
}

/**
 * our main page dynamicly generated so we can do some code as it is shown.
 */
def main() {

    // make sure we are listening to all network subscriptions
    initSubscriptions()

    // send out a UPNP broadcast discovery
    discover_alarmdecoder()

    dynamicPage(name: "main", title: "Discover your AlarmDecoder", install: true, uninstall: true) {
        section("") {
            href(name: "discover", title: "Discover", required: false, page: "discover_devices", description: "Tap to discover")
        }
    }
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
 * Page discover_devices generator. Called periodically to refresh content of the page.
 */
def discover_devices() {
    int refreshInterval = 5
    int refreshCount = !state.refreshCount ? 0 : state.refreshCount as int
    state.refreshCount = refreshCount += 1

    // send out UPNP discovery messages and watch for responses
    discover_alarmdecoder()

    // build list of currently known AlarmDecoder parent devices
    def found_devices = [:]
    def options = state.devices.each { k, v ->
        log.trace "discover_devices: ${v}"
        def ip = convertHexToIP(v.ip)
        found_devices["${v.ip}:${v.port}"] = "AlarmDecoder @ ${ip}"
    }

    // How many do we have?
    def numFound = found_devices.size() ?: 0


    return dynamicPage(name: "discover_devices", title: "Setup", nextPage: "", refreshInterval: refreshInterval, install: true, uninstall: true) {
        section("Devices") {
            input "selectedDevices", "enum", required: true, title: "Select device(s) (${numFound} found)", multiple: true, options: found_devices
            // Allow user to force a new UPNP discovery message
            href(name: "refreshDevices", title: "Refresh", required: false, page: "discover_devices")
        }
        section("Smart Home Monitor Integration") {
            input(name: "shmIntegration", type: "bool", defaultValue: true, title: "Integrate with Smart Home Monitor?")
            input(name: "shmChangeSHMStatus", type: "bool", defaultValue: true, title: "Automatically change Smart Home Monitor status when armed or disarmed?")
        }
        section("Zone Sensors") {
            input(name: "defaultSensorToClosed", type: "bool", defaultValue: true, title: "Default zone sensors to closed?")
        }
    }
}

/*** Pre-Defined callbacks ***/

/**
 *  installed()
 */
def installed() {
    log.debug "Installed with settings: ${settings}"

    // initialize everything
    initialize()
}

/**
 * updated()
 */
def updated() {
    log.debug "Updated with settings: ${settings}"

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
    state.lastSHMStatus = null
    state.lastAlarmDecoderStatus = null

    initSubscriptions()

    if (selectedDevices) {
        addExistingDevices()
        configureDevices()
    }

    scheduleRefresh()
}


/*** Handlers ***/

/**
 * locationHandler(evt)
 * Local network messages sent to port 39500 will be captured here.
 *
 * Test from the AlarmDecoder Appliance:
 *   curl -H "Content-Type: application/json" -X POST -d ‘{"message":"Hi, this is a test from AlarmDecoder network device"}’ http://YOUR.HUB.IP.ADDRESS:39500
 */
def locationHandler(evt) {
    log.trace "locationHandler"

    def description = evt.description
    def hub = evt?.hubId

    log.trace "locationHandler: description=${description}"

    def parsedEvent = parseEventMessage(description)
    parsedEvent << ["hub":hub]

    // UPNP LAN EVENTS on port 1900 from 'AlarmDecoder:1' devices only
    if (parsedEvent?.ssdpTerm?.contains("urn:schemas-upnp-org:device:AlarmDecoder:1")) {

        // make sure our state.devices is initialized. return discard.
        getDevices()

        if (!(state.devices."${parsedEvent.ssdpUSN.toString()}")) {
            log.trace "locationHandler: Adding device: ${parsedEvent.ssdpUSN}"

            devices << ["${parsedEvent.ssdpUSN.toString()}": parsedEvent]
        }
        else {
            def d = state.devices."${parsedEvent.ssdpUSN.toString()}"
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

/**
 * Handle cron refresh event
 */
def refreshHandler() {
    log.trace "refreshHandler"

    refresh_alarmdecoders()
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
 * Handle Device Command zoneOn()
 */
def zoneOn(evt) {
    log.trace("zoneOn: desc=${evt.value}")

    def d = getChildDevices().find { it.deviceNetworkId.contains("switch${evt.value}") }
    if (d)
    {
        def sensorValue = "closed"
        if (settings.defaultSensorToClosed == true)
            sensorValue = "open"

        d.sendEvent(name: "contact", value: sensorValue, isStateChange: true, filtered: true)
    }
}

/**
 * Handle Device Command zoneOff()
 */
def zoneOff(evt) {
    log.trace("zoneOff: desc=${evt.value}")

    def d = getChildDevices().find { it.deviceNetworkId.contains("switch${evt.value}") }
    if (d)
    {
        def sensorValue = "open"
        if (settings.defaultSensorToClosed == true)
            sensorValue = "closed"

        d.sendEvent(name: "contact", value: sensorValue, isStateChange: true, filtered: true)
    }
}

/**
 * Handle Smart Home Monitor App alarmSystemStatus events and update the UI of the App.
 *
 */
def shmAlarmHandler(evt) {
    if (settings.shmIntegration == false)
        return

    log.trace("shmAlarmHandler -- ${evt.value}")

    if (state.lastSHMStatus != evt.value && evt.value != state.lastAlarmDecoderStatus)
    {
        getAllChildDevices().each { device ->
            if (!device.deviceNetworkId.contains(":switch"))
            {
                if (evt.value == "away")
                    device.lock()
                else if (evt.value == "stay")
                    device.on()
                else if (evt.value == "off")
                    device.off()
                else
                    log.debug "Unknown SHM alarm value: ${evt.value}"
            }
        }
    }

    state.lastSHMStatus = evt.value
}

/**
 * Handle Alarm events from the AlarmDecoder and
 * send them back to the Smart Home Monitor API to update the
 * status of the alarm panel
 */
def alarmdecoderAlarmHandler(evt) {
    if (settings.shmIntegration == false || settings.shmChangeSHMStatus == false)
        return

    log.trace("alarmdecoderAlarmHandler: ${evt.value}")

    if (state.lastAlarmDecoderStatus != evt.value && evt.value != state.lastSHMStatus)
        sendLocationEvent(name: "alarmSystemStatus", value: evt.value)

    state.lastAlarmDecoderStatus = evt.value
}

/*** Utility ***/

/**
 * Enable primary network and system subscriptions
 */
def initSubscriptions() {
    // subscribe to the Smart Home Manager api for alarm status events
    log.trace "initialize: subscribe to SHM alarmSystemStatus API messages"
    subscribe(location, "alarmSystemStatus", shmAlarmHandler)

    /* subscribe to local LAN messages to this HUB on port 39500 */
    log.trace "initialize: subscribe to locations local LAN messages"
    subscribe(location, null, locationHandler, [filterEvents: false])

}

/**
 * Called by discover_devices page periodically
 */
def discover_alarmdecoder() {
    log.trace "discover_alarmdecoder"

    // Request HUB send out a UpNp broadcast discovery messages on the local network
    sendHubCommand(new physicalgraph.device.HubAction("lan discovery urn:schemas-upnp-org:device:AlarmDecoder:1", physicalgraph.device.Protocol.LAN))
}

/**
 * create cron schedule and call back handlers
 */
def scheduleRefresh() {
    def minutes = 1

    def cron = "0 0/${minutes} * * * ?"
    schedule(cron, refreshHandler)
}

/**
 * Call refresh() on the AlarmDecoder parent device object.
 * This will force the HUB send a REST API request to the AlarmDecoder Network Appliance.
 * and get back the current status of the AlarmDecoder.
 */
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

/**
 * return the list of known devices and initialize the list if needed.
 */
def getDevices() {
    if(!state.devices) {
        state.devices = [:]
    }

    state.devices
}

/**
 * Add devices selected in the GUI if new.
 */
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
            def newDevice = state.devices.find { /*k, v -> k == dni*/ k, v -> dni == "${v.ip}:${v.port}" }
            log.trace("addExistingDevices, devices.find=${newDevice}")

            if (newDevice) {
                // Set the device network ID so that hubactions get sent to the device parser.
                state.ip = newDevice.value.ip
                state.port = newDevice.value.port
                state.hub = newDevice.value.hub

                // Set URN for the child device
                state.urn = convertHexToIP(state.ip) + ":" + convertHexToInt(state.port)
                log.trace("AlarmDecoder webapp api endpoint('${state.urn}')")

                // Create device and subscribe to it's zone-on/off events.
                d = addChildDevice("alarmdecoder", "network appliance", "${state.ip}:${state.port}", newDevice?.value.hub, [name: "${state.ip}:${state.port}", label: "AlarmDecoder", completedSetup: true, data:[urn: state.urn]])
            }
        }
    }
}

/**
 * Configure the root device(s) to listen to events etc
 */
private def configureDevices() {
    def device = getChildDevice("${state.ip}:${state.port}")
    if (!device) {
        log.trace("configureDevices: Could not find primary device.")
        return
    }

    subscribe(device, "zone-on", zoneOn, [filterEvents: false])
    subscribe(device, "zone-off", zoneOff, [filterEvents: false])
    subscribe(device, "alarmStatus", alarmdecoderAlarmHandler, [filterEvents: false])

    // Add virtual zone contact sensors.
    for (def i = 0; i < 8; i++)
    {
        def newSwitch = state.devices.find { k, v -> k == "${state.ip}:${state.port}:switch${i+1}" }
        if (!newSwitch)
        {
            def zone_switch = addChildDevice("alarmdecoder", "virtual contact sensor", "${state.ip}:${state.port}:switch${i+1}", state.hub, [name: "${state.ip}:${state.port}:switch${i+1}", label: "AlarmDecoder Zone Sensor #${i+1}", completedSetup: true])

            def sensorValue = "open"
            if (settings.defaultSensorToClosed == true)
                sensorValue = "closed"

            // Set default contact state.
            zone_switch.sendEvent(name: "contact", value: sensorValue, isStateChange: true, displayed: false)
        }
    }
}

/**
 * Parse local network messages.
 *
 * May be to port 1900 for UPNP message or to port 39500
 * for local network to hub push messages.
 *
 */
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
