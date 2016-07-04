/**
*/
metadata {
	definition (name: "Window Shade Shield", namespace: "rgonsuron", author: "rgonsuron") {
		capability "Actuator"
		capability "Switch"
	}

	simulator {
	}

	// UI tile definitions
	tiles {
		standardTile("switch", "device.switch", width: 2, height: 2, canChangeIcon: true, canChangeBackground: true) {
			// state "on", label: '${name}', action: "switch.off", icon: "st.switches.switch.on", backgroundColor: "#79b821"
			// state "off", label: '${name}', action: "switch.on", icon: "st.switches.switch.off", backgroundColor: "#ffffff"
			state "on", label: 'opened', action: "switch.off", icon: "st.doors.garage.garage-open", backgroundColor: "#79b821"
			state "off", label: 'closed', action: "switch.on", icon: "st.doors.garage.garage-closed", backgroundColor: "#ffffff"
            state "opening", label: '${name}', icon: "st.doors.garage.garage-opening", backgroundColor: "#b9b821"
            state "closing", label: '${name}', icon: "st.doors.garage.garage-closing", backgroundColor: "#b9b821"
		}

		main "switch"
		details "switch"
	}
}

// Parse incoming device messages to generate events
def parse(String description) {
	def value = zigbee.parse(description)?.text
	def name = value in ["on","off","opening","closing"] ? "switch" : null
	def result = createEvent(name: name, value: value)
	log.debug "Parse returned ${result?.descriptionText}"
	return result
}

// Commands sent to the device
def on() {
	zigbee.smartShield(text: "on").format()
}

def off() {
	zigbee.smartShield(text: "off").format()
}

