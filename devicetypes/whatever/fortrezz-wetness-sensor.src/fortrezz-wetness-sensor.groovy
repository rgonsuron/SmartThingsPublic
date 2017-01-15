/**
 *  Fortrezz Moisture Temp Sensor2
 * COMMAND CLASS: SensorMultilevelReport(precision: 0, scale: 0, scaledSensorValue: 20, sensorType: 1, sensorValue: [20], size: 1)
 */

metadata {
	// Automatically generated. Make future change here.
	definition (name: "Fortrezz Wetness Sensor", namespace: "whatever", author: "whoever") {
		capability "Water Sensor"
		capability "Sensor"
		capability "Battery"
        capability "Temperature Measurement"

		fingerprint deviceId: "0x2001", inClusters: "0x30,0x9C,0x9D,0x85,0x80,0x72,0x31,0x84,0x86"
		fingerprint deviceId: "0x2101", inClusters: "0x71,0x70,0x85,0x80,0x72,0x31,0x84,0x86"
	}

	simulator {
		status "dry": "command: 7105, payload: 00 00 00 FF 05 FE 00 00"
		status "wet": "command: 7105, payload: 00 FF 00 FF 05 02 00 00"
		status "overheated": "command: 7105, payload: 00 00 00 FF 04 02 00 00"
		status "freezing": "command: 7105, payload: 00 00 00 FF 04 05 00 00"
		status "normal": "command: 7105, payload: 00 00 00 FF 04 FE 00 00"
		for (int i = 0; i <= 100; i += 20) {
			status "battery ${i}%": new physicalgraph.zwave.Zwave().batteryV1.batteryReport(batteryLevel: i).incomingMessage()
		}
	}
	tiles {
		standardTile("water", "device.water", width: 2, height: 2) {
			state "dry", icon:"st.alarm.water.dry", backgroundColor:"#ffffff"
			state "wet", icon:"st.alarm.water.wet", backgroundColor:"#53a7c0"
		}
		standardTile("temperature", "device.temperature", width: 2, height: 2) {
			state "normal", icon:"st.alarm.temperature.normal", backgroundColor:"#ffffff"
			state "freezing", icon:"st.alarm.temperature.freeze", backgroundColor:"#53a7c0"
			state "overheated", icon:"st.alarm.temperature.overheat", backgroundColor:"#F80000"
		}
        standardTile("refresh", "device.switch", inactiveLabel: false, decoration: "flat") {
			state "default", label:'', action:"refresh.refresh", icon:"st.secondary.refresh"
		}
        //standardTile("R2", "device.switch", inactiveLabel: false, decoration: "flat") {
		//	state "default", label:'', action:"refresh.refresh", icon:"st.secondary.refresh"
		//}

		valueTile("battery", "device.battery", decoration: "flat", inactiveLabel: false) {
			state "battery", label:'${currentValue}% battery', unit:""/*, backgroundColors:[
				[value: 5, color: "#BC2323"],
				[value: 10, color: "#D04E00"],
				[value: 15, color: "#F1D801"],
				[value: 16, color: "#FFFFFF"]
			]*/
		}
       	valueTile("temp", "device.temp", inactiveLabel: false) {
			state "temp", label:'${currentValue}°'
		}            
		// main (["temperature", "water", "temp"])
        main (["temp"])
		details(["water", "temperature", "battery", "refresh", "temp"])
	}
}

def refresh() {
    //zwave.wakeUpV2.wakeUpIntervalSet(seconds:4 * 3600, nodeid:zwaveHubNodeId).format()
    log.debug "Requesting temperature"
	delayBetween([
        zwave.sensormultilevelv1.SensorMultilevelGet().format()
	])
    //log.debug "Sent wakeup config command for ${zwaveHubNodeId}"
    log.debug "Requested temperature for ${zwaveHubNodeId}"
}

def parse(String description) {

	def parsedZwEvent = zwave.parse(description, [0x30: 1, 0x71: 2, 0x84: 1, 0x31: 2])
	def zwEvent = zwaveEvent(parsedZwEvent)
	def result = []

	log.debug "Parser description ${description}"


	result << createEvent( zwEvent )

	if( parsedZwEvent.CMD == "8407" ) {
        if (!state.lastbatt || (new Date().time) - state.lastbatt > 24*60*60*1000) {
                result << response(zwave.batteryV1.batteryGet())
                result << response("delay 1200")  // leave time for device to respond to batteryGet
        }
		result << response(zwave.wakeUpV1.wakeUpNoMoreInformation())
        //batt
        /*
		def lastStatus = device.currentState("battery")
		def ageInMinutes = lastStatus ? (new Date().time - lastStatus.date.time)/60000 : 600
		log.debug "Battery status was last checked ${ageInMinutes} minutes ago"

		if (ageInMinutes >= 600) {
			log.debug "Battery status is outdated, requesting battery report"
			result << new physicalgraph.device.HubAction(zwave.batteryV1.batteryGet().format())
            //?device.currentState("battery") = new Date().time;
		}
        
		result << new physicalgraph.device.HubAction(zwave.wakeUpV1.wakeUpNoMoreInformation().format())
        */
	}

	log.debug "Parse returned ${result}"
	return result
}
def zwaveEvent(physicalgraph.zwave.commands.sensormultilevelv2.SensorMultilevelReport cmd)
{
	def map = [:]
	switch (cmd.sensorType) {
		case 1:
			// temperature
			def cmdScale = cmd.scale == 1 ? "F" : "C"
			map.value = convertTemperatureIfNeeded(cmd.scaledSensorValue, cmdScale, cmd.precision)
            map.unit = getTemperatureScale()
			map.name = "temp"
			break;
			log.debug "Got temperature: $map.value"
	}
	map
}

def zwaveEvent(physicalgraph.zwave.commands.wakeupv1.WakeUpNotification cmd)
{
	[descriptionText: "${device.displayName} woke up", isStateChange: false]
}

def zwaveEvent(physicalgraph.zwave.commands.sensorbinaryv1.SensorBinaryReport cmd)
{
	def map = [:]
	map.name = "water"
	map.value = cmd.sensorValue ? "wet" : "dry"
	map.descriptionText = "${device.displayName} is ${map.value}"
	map
}

def zwaveEvent(physicalgraph.zwave.commands.batteryv1.BatteryReport cmd) {
	def map = [:]
	if(cmd.batteryLevel == 0xFF) {
		map.name = "battery"
		map.value = 1
		map.descriptionText = "${device.displayName} has a low battery"
		map.displayed = true
	} else {
		map.name = "battery"
		map.value = cmd.batteryLevel > 0 ? cmd.batteryLevel.toString() : 1
		map.unit = "%"
		map.displayed = false
	}
    //batt
	state.lastbatt = new Date().time

	map
}

def zwaveEvent(physicalgraph.zwave.commands.alarmv2.AlarmReport cmd)
{
	def map = [:]
	if (cmd.zwaveAlarmType == physicalgraph.zwave.commands.alarmv2.AlarmReport.ZWAVE_ALARM_TYPE_WATER) {
		map.name = "water"
		map.value = cmd.alarmLevel ? "wet" : "dry"
		map.descriptionText = "${device.displayName} is ${map.value}"
	}
	if(cmd.zwaveAlarmType ==  physicalgraph.zwave.commands.alarmv2.AlarmReport.ZWAVE_ALARM_TYPE_HEAT) {
		map.name = "temperature"
		if(cmd.zwaveAlarmEvent == 1) { map.value = "overheated"}
		if(cmd.zwaveAlarmEvent == 2) { map.value = "overheated"}
		if(cmd.zwaveAlarmEvent == 3) { map.value = "changing temperature rapidly"}
		if(cmd.zwaveAlarmEvent == 4) { map.value = "changing temperature rapidly"}
		if(cmd.zwaveAlarmEvent == 5) { map.value = "freezing"}
		if(cmd.zwaveAlarmEvent == 6) { map.value = "freezing"}
		if(cmd.zwaveAlarmEvent == 254) { map.value = "normal"}
		map.descriptionText = "${device.displayName} is ${map.value}"
	}

	map
}

def zwaveEvent(physicalgraph.zwave.Command cmd)
{
	log.debug "COMMAND CLASS: $cmd"
}