/**
 *  Copyright 2016 Ryan Finnie
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
metadata {
	definition (name: "Z-Wave Repeater", namespace: "rfinnie", author: "Ryan Finnie") {
		capability "Refresh"

		fingerprint mfr:"0246", prod:"0001", deviceJoinName: "Iris Smart Plug Z-Wave Repeater"
	}

	tiles(scale: 2) {
		standardTile("refresh", "device.refresh", inactiveLabel: false, decoration: "flat", width: 6, height: 4) {
			state "default", label:"", action:"refresh.refresh", icon:"st.secondary.refresh"
		}
		main "refresh"
		details(["refresh"])
	}
}

def parse(String description) {
	log.debug "description is $description"
	def result = []
	if (description.startsWith("Err")) {
		result = createEvent(descriptionText:description, isStateChange:true)
	} else {
		def cmd = zwave.parse(description, [0x20: 1, 0x84: 1, 0x98: 1, 0x56: 1, 0x60: 3])
		if (cmd) {
			result += zwaveEvent(cmd)
		}
	}
	return result
}

def zwaveEvent(physicalgraph.zwave.commands.versionv1.VersionReport cmd) {
	def fw = "${cmd.applicationVersion}.${cmd.applicationSubVersion}"
	def protocol = "${cmd.zWaveProtocolVersion}.${cmd.zWaveProtocolSubVersion}"
	def library = "${cmd.zWaveLibraryType}"
	log.debug "Firmware: $fw - Protocol: $protocol - Library: $library"
	updateDataValue("fw", fw)
}

def zwaveEvent(physicalgraph.zwave.commands.manufacturerspecificv2.ManufacturerSpecificReport cmd) {
	def msr = String.format("%04X-%04X-%04X", cmd.manufacturerId, cmd.productTypeId, cmd.productId)
	log.debug "MSR: $msr"
	updateDataValue("MSR", msr)
}

def ping() {
	refresh()
}

def refresh() {
	delayBetween([
		zwave.versionV1.versionGet().format(),
		zwave.manufacturerSpecificV2.manufacturerSpecificGet().format()
	], 500)
}
