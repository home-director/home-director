/**
 *  HTTP Updater
 *
 *  Copyright 2016 Daniel Forys
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
    name: "HTTP Updater",
    namespace: "DanForys",
    author: "Daniel Forys",
    description: "Send push updates for your devices out to an HTTP service of your chosing",
    category: "",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    oauth: true)


preferences {
    page(name: "copyConfig")
}

//When adding device groups, need to add here
def copyConfig() {
    if (!state.accessToken) {
        createAccessToken()
    }
    dynamicPage(name: "copyConfig", title: "Config", install:true, uninstall:true) {
        section("Select devices to include in the /devices API call") {
            paragraph "Version 0.4.1"
            input "deviceList", "capability.refresh", title: "Most Devices", multiple: true, required: false
            input "sensorList", "capability.sensor", title: "Sensor Devices", multiple: true, required: false
            input "switchList", "capability.switch", title: "All Switches", multiple: true, required: false
            paragraph "Devices Selected: ${deviceList ? deviceList?.size() : 0}\nSensors Selected: ${sensorList ? sensorList?.size() : 0}\nSwitches Selected: ${switchList ? switchList?.size() : 0}"
        }
        section("Home Director URL to send updates to") {
        	input "directorUrl", "text", title: "Director URL", required: true
        }
        section() {
            paragraph "View this SmartApp's configuration to use it in other places."
            href url:"${apiServerUrl("/api/smartapps/installations/${app.id}/config?access_token=${state.accessToken}")}", style:"embedded", required:false, title:"Config", description:"Tap, select, copy, then click \"Done\""
        }

        section() {
        	paragraph "View the JSON generated from the installed devices."
            href url:"${apiServerUrl("/api/smartapps/installations/${app.id}/devices?access_token=${state.accessToken}")}", style:"embedded", required:false, title:"Device Results", description:"View accessories JSON"
        }
        section() {
        	paragraph "Enter the name you would like shown in the smart app list"
        	label title:"SmartApp Label (optional)", required: false
        }
    }
}

def authError() {
    [error: "Permission denied"]
}

def renderConfig() {
    def configJson = new groovy.json.JsonOutput().toJson([
        description: "JSON API",
        platforms: [
            [
                platform: "SmartThings",
                name: "SmartThings",
                app_url: apiServerUrl("/api/smartapps/installations/"),
                app_id: app.id,
                access_token:  state.accessToken
            ]
        ],
    ])

    def configString = new groovy.json.JsonOutput().prettyPrint(configJson)
    render contentType: "text/plain", data: configString
}

def installed() {
	log.debug "Installed with settings: ${settings}"

	initialize()
}

def updated() {
	log.debug "Updated with settings: ${settings}"

	unsubscribe()
	initialize()
}

def initialize() {
	if(!state.accessToken) {
         createAccessToken()
    }
	registerAll()
}

def registerAll() {
	//This has to be done at startup because it takes too long for a normal command.
	log.debug "Registering All Events"
    state.devchanges = []
	registerChangeHandler(deviceList)
	registerChangeHandler(sensorList)
	registerChangeHandler(switchList)
}

def registerChangeHandler(myList) {
	myList.each { myDevice ->
		def theAtts = myDevice.supportedAttributes
		theAtts.each {att ->
		    subscribe(myDevice, att.name, changeHandler)
    	log.debug "Registering ${myDevice.displayName}.${att.name}"
		}
	}
}

def changeHandler(evt) {
	//Only add to the state's devchanges if the endpoint has renewed in the last 10 minutes.
    if (state.subscriptionRenewed>(now()-(1000*60*10))) {
  		if (evt.isStateChange()) {
			state.devchanges << [device: evt.deviceId, attribute: evt.name, value: evt.value, date: evt.date]
      }
    } else if (state.subscriptionRenewed>0) { //Otherwise, clear it
    	log.debug "Endpoint Subscription Expired. No longer storing changes for devices."
        state.devchanges=[]
        state.subscriptionRenewed=0
    }

    def params = [
        uri: directorUrl,
        body: [[device: evt.deviceId, attribute: evt.name, value: evt.value, date: evt.date]]
    ]

    try {
        httpPostJson(params) { resp ->
            resp.headers.each {
                log.debug "${it.name} : ${it.value}"
            }
            log.debug "response contentType: ${resp.    contentType}"
        }
    } catch (e) {
        log.debug "something went wrong: $e"
    }
}

def getAllData() {
	def deviceData =
    [	location: renderLocation(),
        deviceList: renderDevices() ]
    def deviceJson = new groovy.json.JsonOutput().toJson(deviceData)
    render contentType: "application/json", data: deviceJson
}

def renderDevices() {
    def deviceData = []
        deviceList.each {
        	deviceData << [name: it.displayName, deviceid: it.id, capabilities: deviceCapabilityList(it), commands: deviceCommandList(it), attributes: deviceAttributeList(it)]
	}
        sensorList.each  {
        	deviceData << [name: it.displayName, deviceid: it.id, capabilities: deviceCapabilityList(it), commands: deviceCommandList(it), attributes: deviceAttributeList(it)]
	}
        switchList.each  {
        	deviceData << [name: it.displayName, deviceid: it.id, capabilities: deviceCapabilityList(it), commands: deviceCommandList(it), attributes: deviceAttributeList(it)]
	}
    return deviceData
}

def renderLocation() {
  	[
    	latitude: location.latitude,
    	longitude: location.longitude,
    	mode: location.mode,
    	name: location.name,
    	temperature_scale: location.temperatureScale,
    	zip_code: location.zipCode
  	]
}



def deviceCapabilityList(device) {
  	def i=0
  	device.capabilities.collectEntries { capability->
    	[
      		(capability.name):1
    	]
  	}
}

def deviceCommandList(device) {
  	def i=0
  	device.supportedCommands.collectEntries { command->
    	[
      		(command.name): (command.arguments)
    	]
  	}
}

def deviceAttributeList(device) {
  	device.supportedAttributes.collectEntries { attribute->
    	try {
      		[
        		(attribute.name): device.currentValue(attribute.name)
      		]
    	} catch(e) {
      		[
        		(attribute.name): null
      		]
    	}
  	}
}


mappings {
    if (!params.access_token || (params.access_token && params.access_token != state.accessToken)) {
        path("/devices")                        { action: [GET: "authError"] }
        path("/config")                         { action: [GET: "authError"] }
    } else {
        path("/devices")                        { action: [GET: "getAllData"] }
        path("/config")                         { action: [GET: "renderConfig"]  }
    }
}
