metadata {
	definition (name: "Securifi Key Fob", namespace: "tierneykev", author: "Kevin Tierney") {
    
    capability "Configuration"
    capability "Refresh"
    capability "Button"
    
    
    attribute "button2","ENUM",["released","pressed"]
    attribute "button3","ENUM",["released","pressed"]  
	attribute "numButtons", "NUMBER"
    
    fingerprint profileId: "0104", deviceId: "0401", inClusters: "0000,0003,0500", outClusters: "0003,0501"
    }
    

	 tiles {

        standardTile("button1", "device.button", width: 1, height: 1) {
            state("released", label:'${name}', icon:"st.button.button.released", backgroundColor:"#ffa81e")
            state("pressed", label:'${name}', icon:"st.button.button.pressed", backgroundColor:"#79b821")
        }

        standardTile("button2", "device.button2", width: 1, height: 1) {
            state("released", label:'${name}', icon:"st.button.button.released", backgroundColor:"#ffa81e")
            state("pressed", label:'${name}', icon:"st.button.button.pressed", backgroundColor:"#79b821")
        }        

        standardTile("button3", "device.button3", width: 1, height: 1) {
            state("released", label:'${name}', icon:"st.button.button.released", backgroundColor:"#ffa81e")
            state("pressed", label:'${name}', icon:"st.button.button.pressed", backgroundColor:"#79b821")
        }
     	valueTile("numButtons", "device.numButtons", decoration: "flat") {
        	state "numButtons", label:'Fob ${currentValue} Buttons'
    	}
 

    main (["button3", "button2", "button1", "numButtons"])
    details (["button3", "button2", "button1", "numButtons" ])
    }
}




def parse(String description) {	       	        
    
    if (description?.startsWith('enroll request')) {
        
        List cmds = enrollResponse()
        log.debug "enroll response: ${cmds}"
        def result = cmds?.collect { new physicalgraph.device.HubAction(it) }
        
        return result    
    } else if (description?.startsWith('catchall:')) {
        def msg = zigbee.parse(description)
		log.debug "Parsed messages is: ${msg}"
        buttonPush(msg.data[0])
    } else {
    	log.debug "parse description: $description"
    }
    sendEvent(name: "numButtons", value: 3, displayed: false)
    
}

def buttonPush(button){
    def name = null
    if (button == 0) {
        name = "1"
        def currentST = device.currentState("button")?.value
        log.debug "Unlock button Pushed"           
    } else if (button == 2) {
        name = "2"
        def currentST = device.currentState("button2")?.value
        log.debug "Home button pushed"        
    } else if (button == 3) {
        name = "3"
        def currentST = device.currentState("button3")?.value
        log.debug "Lock Button pushed"         
    } 

	def result = createEvent(name: "button", value: "pushed", data: [buttonNumber: name], descriptionText: "$device.displayName button $name was pushed", isStateChange: true)
    log.debug "Parse returned ${result?.descriptionText}"
    return result
}


def enrollResponse() {
    log.debug "Sending enroll response"
    [            
    "raw 0x500 {01 23 00 00 00}", "delay 200",
    "send 0x${device.deviceNetworkId} 0x08 1"        
    ]
}



def configure(){
    log.debug "Config Called"
    sendEvent(name: "numButtons", value: 3, displayed: false)
    def configCmds = [
    "zcl global write 0x500 0x10 0xf0 {${device.zigbeeId}}", "delay 200",
    "send 0x${device.deviceNetworkId} 0x08 1", "delay 1500",
    "zdo bind 0x${device.deviceNetworkId} 0x08 0x01 0x0501 {${device.zigbeeId}} {}", "delay 500",
    "zdo bind 0x${device.deviceNetworkId} 0x08 1 1 {${device.zigbeeId}} {}"
    ]
    return configCmds
}
