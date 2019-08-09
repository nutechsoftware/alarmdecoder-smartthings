/**
 *  Virtual Smoke Alarm for alarm panel
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

/*
 * global support
 */
import groovy.transform.Field
@Field APPNAMESPACE = "alarmdecoder"

metadata {
    definition (name: "AlarmDecoder virtual smoke alarm", namespace: APPNAMESPACE, author: "Nu Tech Software Solutions, Inc.") {
        capability "Smoke Detector"
    }

    // tile definitions
    tiles {
        standardTile("sensor", "device.smoke", width: 2, height: 2, canChangeIcon: true) {
            state "clear", label: '${name}', icon: "st.alarm.smoke.clear", backgroundColor: "#79b821"
            state "detected", label: '${name}', icon: "st.alarm.smoke.smoke", backgroundColor: "#e86d13"
        }
        main "sensor"
        details "sensor"
    }

    // preferences
    preferences {
        input name: "invert", type: "bool", title: "Invert", description: "Invert signal ON is OFF/OPEN is CLOSE/DETECTED is CLEAR", required: false
    }
}

def installed() {
    updateDataValue("invert", invert.toString())
}

def updated() {
    updateDataValue("invert", invert.toString())
}