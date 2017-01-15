/* my zwave momentary
	based on z-wave switch generic
	basic switch handler which flashes the status on/off again to indicate a momentary action
    the monoprice z-wave curtain module (11992) closes the "stop" contact for 1 second after receiving either
    a on OR off command
*/
metadata {
	definition (name: "Monoprice z-wave curtain module", namespace: "smartthings", author: "SmartThings") {
		capability "Actuator"
 		capability "Switch"
        capability "Valve"
		capability "Polling"
		capability "Refresh"
		capability "Sensor"
		capability "Health Check"

		fingerprint mfr:"0063", prod:"4952", deviceJoinName: "Z-Wave Wall Switch"
		fingerprint mfr:"0063", prod:"5257", deviceJoinName: "Z-Wave Wall Switch"
		fingerprint mfr:"0063", prod:"5052", deviceJoinName: "Z-Wave Plug-In Switch"
		fingerprint mfr:"0113", prod:"5257", deviceJoinName: "Z-Wave Wall Switch"
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
	// attributeState "off", label: '${name}', action: "switch.on", icon: "st.switches.switch.off", backgroundColor: "#79b821"
	tiles(scale: 2) {
		multiAttributeTile(name:"switch", type: "lighting", width: 6, height: 4, canChangeIcon: true){
			tileAttribute ("device.switch", key: "PRIMARY_CONTROL") {
				attributeState "on", label: '${name}', action: "switch.off", icon: "st.switches.switch.on", backgroundColor: "#79b821"
				attributeState "off", label: '${name}', action: "switch.on", icon: "st.switches.switch.off", backgroundColor: "#ffffff"
			}
		}
		standardTile("valve", "device.switch", width: 6, height: 4, canChangeIcon: true){
			state "on", label: 'operating', action: "valve.close", icon: "st.doors.garage.garage-open", backgroundColor: "#79b821"
			state "off", label: 'idle', action: "valve.open", icon: "st.doors.garage.garage-closed", backgroundColor: "#ffffff"
		}

		standardTile("refresh", "device.switch", width: 2, height: 2, inactiveLabel: false, decoration: "flat") {
			state "default", label:'', action:"refresh.refresh", icon:"st.secondary.refresh"
		}

		main "valve"
		details(["valve","refresh"])
	}
}

def updated(){
		// Device-Watch simply pings if no device events received for 32min(checkInterval)
		sendEvent(name: "checkInterval", value: 2 * 15 * 60 + 2 * 60, displayed: false, data: [protocol: "zwave", hubHardwareId: device.hub.hardwareID])
}

def parse(String description) {
	def result = null
	def cmd = zwave.parse(description, [0x20: 1, 0x70: 1])
	if (cmd) {
        result = createEvent(zwaveEvent(cmd))
        runIn(20, switchoff)
	}
	if (result?.name == 'hail' && hubFirmwareLessThan("000.011.00602")) {
		result = [result, response(zwave.basicV1.basicGet())]
		log.debug "Was hailed: requesting state update"
	} else {
		log.debug "Parse returned ${result?.descriptionText}"
	}
	return result
}
def switchoff() {
	sendEvent(name: "switch", value: "off")
}

def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicReport cmd) {
	// [name: "switch", value: cmd.value ? "on" : "off", type: "physical"]
    [name: "switch", value: "on", type: "physical"]
}

def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicSet cmd) {
	// [name: "switch", value: cmd.value ? "on" : "off", type: "physical"]
    [name: "switch", value: "on", type: "physical"]
}

def zwaveEvent(physicalgraph.zwave.commands.switchbinaryv1.SwitchBinaryReport cmd) {
	// [name: "switch", value: cmd.value ? "on" : "off", type: "digital"]
    [name: "switch", value: "on", type: "digital"]
}

def zwaveEvent(physicalgraph.zwave.commands.manufacturerspecificv2.ManufacturerSpecificReport cmd) {
	log.debug "manufacturerId:   ${cmd.manufacturerId}"
	log.debug "manufacturerName: ${cmd.manufacturerName}"
	log.debug "productId:        ${cmd.productId}"
	log.debug "productTypeId:    ${cmd.productTypeId}"
	def msr = String.format("%04X-%04X-%04X", cmd.manufacturerId, cmd.productTypeId, cmd.productId)
	updateDataValue("MSR", msr)
	updateDataValue("manufacturer", cmd.manufacturerName)
	createEvent([descriptionText: "$device.displayName MSR: $msr", isStateChange: false])
}

def zwaveEvent(physicalgraph.zwave.Command cmd) {
	// Handles all Z-Wave commands we aren't interested in
	//	[:]
    // This will capture any commands not handled by other instances of zwaveEvent
    // and is recommended for development so you can see every command the device sends
    return createEvent(descriptionText: "${device.displayName}: ${cmd}")
}

def on() {
	delayBetween([
		zwave.basicV1.basicSet(value: 0xFF).format(),
		zwave.switchBinaryV1.switchBinaryGet().format()
	])
}

def off() {
	delayBetween([
		zwave.basicV1.basicSet(value: 0x00).format(),
		zwave.switchBinaryV1.switchBinaryGet().format()
	])
}

def open() {
	delayBetween([
		zwave.basicV1.basicSet(value: 0xFF).format(),
		zwave.switchBinaryV1.switchBinaryGet().format()
	])
}

def close() {
	delayBetween([
		zwave.basicV1.basicSet(value: 0x00).format(),
		zwave.switchBinaryV1.switchBinaryGet().format()
	])
}

def poll() {
	delayBetween([
		zwave.switchBinaryV1.switchBinaryGet().format(),
		zwave.manufacturerSpecificV1.manufacturerSpecificGet().format()
	])
}

/** PING is used by Device-Watch in attempt to reach the Device **/
def ping() {
		refresh()
}

def refresh() {
	delayBetween([
		zwave.switchBinaryV1.switchBinaryGet().format(),
		zwave.manufacturerSpecificV1.manufacturerSpecificGet().format()
	])
}

def invertSwitch(invert=true) {
	if (invert) {
		zwave.configurationV1.configurationSet(configurationValue: [1], parameterNumber: 4, size: 1).format()
	}
	else {
		zwave.configurationV1.configurationSet(configurationValue: [0], parameterNumber: 4, size: 1).format()
	}
}