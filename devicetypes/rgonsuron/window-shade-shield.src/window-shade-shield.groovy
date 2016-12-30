/**
*/
metadata {
	definition (name: "Window Shade Shield", namespace: "rgonsuron", author: "rgonsuron") {
		capability "Actuator"
		capability "Switch"
        
        command "bump1open"
        command "bump1close"
        command "bump2open"
        command "bump2close"
        command "bump3open"
        command "bump3close"
	}

	simulator {
	}

	// UI tile definitions
	tiles(scale: 2) {
		standardTile("switch", "device.switch", width: 6, height: 4, canChangeIcon: true, canChangeBackground: true) {
			state "on", label: 'opened', action: "switch.off", icon: "st.doors.garage.garage-open", backgroundColor: "#79b821"
			state "off", label: 'closed', action: "switch.on", icon: "st.doors.garage.garage-closed", backgroundColor: "#ffffff"
            state "opening", label: '${name}', icon: "st.doors.garage.garage-opening", backgroundColor: "#b9b821"
            state "closing", label: '${name}', icon: "st.doors.garage.garage-closing", backgroundColor: "#b9b821"
		}
		standardTile("bump1open", "device.switch", width: 2, height: 2) {
            state "", label: "Open1", action: "bump1open", icon: "st.switches.switch.on", backgroundColor: "#a9b881"
        }
		standardTile("bump2open", "device.switch", width: 2, height: 2) {
            state "", label: "Open2", action: "bump2open", icon: "st.switches.switch.on", backgroundColor: "#a9b881"
        }
		standardTile("bump3open", "device.switch", width: 2, height: 2) {
            state "", label: "Open3", action: "bump3open", icon: "st.switches.switch.on", backgroundColor: "#a9b881"
        }
		standardTile("bump1close", "device.switch", width: 2, height: 2) {
            state "", label: "Close1", action: "bump1close", icon: "st.switches.switch.on", backgroundColor: "#c9b8a1"  
		}
		standardTile("bump2close", "device.switch", width: 2, height: 2) {
            state "", label: "Close2", action: "bump2close", icon: "st.switches.switch.on", backgroundColor: "#c9b8a1"  
		}
		standardTile("bump3close", "device.switch", width: 2, height: 2) {
            state "", label: "Close3", action: "bump3close", icon: "st.switches.switch.on", backgroundColor: "#c9b8a1"  
		}

		main ("switch")
		details (["switch", "bump1open", "bump2open", "bump3open", "bump1close", "bump2close", "bump3close"])
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
def on() {	zigbee.smartShield(text: "on").format() }
def off() {	zigbee.smartShield(text: "off").format() }

def bump1open() { zigbee.smartShield(text: "bump1open").format() }
def bump1close() { zigbee.smartShield(text: "bump1close").format() }

def bump2open() { zigbee.smartShield(text: "bump2open").format() }
def bump2close() { zigbee.smartShield(text: "bump2close").format() }

def bump3open() { zigbee.smartShield(text: "bump3open").format() }
def bump3close() { zigbee.smartShield(text: "bump3close").format() }
