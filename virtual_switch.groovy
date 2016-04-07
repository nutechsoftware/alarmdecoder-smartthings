/* virtual switch */

/* Based off of https://github.com/jwsf/device-type.arduino-8-way-relay/blob/master/VirtualSwitch.groovy */
metadata {
    definition (name: "VirtualSwitch", namespace: "alarmdecoder", author: "badgermanus@gmail.com") {
        capability "Switch"
        capability "Momentary"
        capability "Refresh"
    }

    // simulator metadata
    simulator {
        status "on":  "command: 2003, payload: FF"
        status "off": "command: 2003, payload: 00"

        // reply messages
        reply "2001FF,delay 100,2502": "command: 2503, payload: FF"
        reply "200100,delay 100,2502": "command: 2503, payload: 00"
    }

    // tile definitions
    tiles {
        standardTile("switch", "device.switch", width: 2, height: 2, canChangeIcon: true) {
            state "on", label: '${name}', action: "switch.off", icon: "st.switches.switch.on", backgroundColor: "#79b821"
            state "off", label: '${name}', action: "switch.on", icon: "st.switches.switch.off", backgroundColor: "#ffffff"
        }
        standardTile("push", "device.switch", inactiveLabel: false, decoration: "flat") {
            state "default", label:'Push', action:"momentary.push", icon: "st.secondary.refresh-icon"
        }
        standardTile("refresh", "device.switch", inactiveLabel: false, decoration: "flat") {
            state "default", label:'Refresh', action:"device.refresh", icon: "st.secondary.refresh-icon"
        }

        main "switch"
        details(["switch","push","refresh"])
    }
}


// handle commands
def on() {
    log.debug "On"
    sendEvent (name: "switch", value: "on", isStateChange:true)
}

def off() {
    log.debug "Off"
    sendEvent (name: "switch", value: "off", isStateChange:true)
}

def push() {
    log.debug "Push"
    sendEvent(name: "momentary", value: "push", isStateChange:true)
}