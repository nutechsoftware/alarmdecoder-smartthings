/**
 *  Virtual Contact Sensor for alarm panel zones
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

import groovy.transform.Field
@Field APPNAMESPACE = "alarmdecoder"

metadata {
    definition (name: "AlarmDecoder virtual motion sensor", namespace: APPNAMESPACE, author: "scott@nutech.com") {
        capability "Motion Sensor"
    }

    // tile definitions

	tiles(scale: 2) {
		multiAttributeTile(name:"motion", type: "generic", width: 6, height: 4){
			tileAttribute ("device.motion", key: "PRIMARY_CONTROL") {
				attributeState "active", label:'motion', icon:"st.motion.motion.active", backgroundColor:"#53a7c0"
				attributeState "inactive", label:'no motion', icon:"st.motion.motion.inactive", backgroundColor:"#ffffff"
			}
		}
		main "motion"
		details "motion"
	}
    
        preferences {
        input name: "invert", type: "bool", title: "Invert", description: "Invert signal ON is OFF/OPEN is CLOSE/DETECTED is CLEAR", required: false
    }
}

def parse(String description) {
    if (description != "updated"){
    	log.info "parse returned:${description}"
		def pair = description.split(":")
		createEvent(name: pair[0].trim(), value: pair[1].trim())
      }
}

def active() {
	sendEvent(name: "motion", value: "active")
}

def inactive() {
    sendEvent(name: "motion", value: "inactive")
}

def installed() {
    updateDataValue("invert", invert.toString())
}

def updated() {
    updateDataValue("invert", invert.toString())
}