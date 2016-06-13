/*
	zigbee window shade device handler
*/
metadata {
	definition (name: "Window Shade Shield", namespace: "rgonsuron", author: "rgonsuron") {
		// capability "Actuator"
        capability "Garage Door Control"
		capability "Switch"
		// capability "Sensor"
        // capability "Window Shade"
        attribute "currentSwitch", "string"
	}

	// Simulator metadata
	simulator {
		status "open":  "catchall: 0104 0000 01 01 0040 00 0A21 00 00 0000 0A 00 0A6F6E"
		status "close": "catchall: 0104 0000 01 01 0040 00 0A21 00 00 0000 0A 00 0A6F6666"

		// reply messages
		reply "raw 0x0 { 00 00 0a 0a 6f 6e }": "catchall: 0104 0000 01 01 0040 00 0A21 00 00 0000 0A 00 0A6F6E"
		reply "raw 0x0 { 00 00 0a 0a 6f 66 66 }": "catchall: 0104 0000 01 01 0040 00 0A21 00 00 0000 0A 00 0A6F6666"
	}

	// UI tile definitions
	tiles {
		standardTile("windowShade", "device.windowShade", width: 2, height: 2, canChangeIcon: true, canChangeBackground: true) {
//			state "on", label: '${name}', action: "switch.off", icon: "st.doors.garage.garage-open", backgroundColor: "#79b821"
//			state "off", label: '${name}', action: "switch.on", icon: "st.doors.garage.garage-closed", backgroundColor: "#ffffff"
			state "open", label: '${name}', action: "switch.off", icon: "st.doors.garage.garage-open", backgroundColor: "#79b821"
			state "opening", label: '${name}', icon: "st.doors.garage.garage-opening", backgroundColor: "#b9b821"
			state "close", label: '${name}', action: "switch.on", icon: "st.doors.garage.garage-closed", backgroundColor: "#ffffff"
			state "closing", label: '${name}', icon: "st.doors.garage.garage-closing", backgroundColor: "#b9b821"
		}
        
		main "windowShade"
		details "windowShade"
	}
}

// Parse incoming device messages to generate events
def parse(String description) {
	def value = zigbee.parse(description)?.text
	def name = value in ["on","off","closing","opening","open","close","status"] ? "windowShade" : null
    log.debug "Window shade data received ${name} : ${value}"
	def result = createEvent(name: name, value: value)
	log.debug "Window shade parse returned ${result?.descriptionText}"
	return result
}

// Commands sent to the device
def on() {
	log.debug("sent open command to device");
	zigbee.smartShield(text: "open").format()
}

def off() {
	log.debug("sent close command to device");
	zigbee.smartShield(text: "close").format()
}

def status() {
	log.debug("sent status request to device")
    zigbee.smartShield(text: "status").format()
}
