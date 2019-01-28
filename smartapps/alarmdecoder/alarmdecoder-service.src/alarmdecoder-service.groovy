/**
 *  AlarmDecoder Service Manager
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
 *
 * Version 1.0.0 - Scott Petersen - Initial design and release
 * Version 2.0.0 - Sean Mathews <coder@f34r.com> - Changed to use UPNP Push API in AD2 web app
 * Version 2.0.1 - Sean Mathews <coder@f34r.com> - Adding CID device management support.
 * Version 2.0.2 - Sean Mathews <coder@f34r.com> - Fixed app 20 second max timeout. AddZone is now async, added more zones.
 * Version 2.0.3 - Sean Mathews <coder@f34r.com> - Improved/fixed issues with previous app 20 timeout after more testing.
 */

/*
 * global support
 */
import groovy.transform.Field
import groovy.json.JsonSlurperClassic

/*
 * Turn on verbose debugging
 */
@Field debug = false

/*
 * Device label name settings
 */
@Field lname = "AlarmDecoder"
@Field sname = "AD2"
@Field adApiKey = "UPDATE_ME"
 
definition(
    name: "AlarmDecoder service",
    namespace: "alarmdecoder",
    author: "Nu Tech Software Solutions, Inc.",
    description: "AlarmDecoder (Service Manager)",
    category: "My Apps",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    singleInstance: true) { }

preferences {
    page(name: "page_main", title: titles("page_main"), install: false, uninstall: false, refreshInterval: 5)
    page(name: "page_discover_devices", title: titles("page_discover_devices"), content: "page_discover_devices", install: true, uninstall: false)
    page(name: "page_remove_all", title: titles("page_remove_all"), content: "page_remove_all", install: false, uninstall: false)
    page(name: "page_cid_management", title: titles("page_cid_management"), content: "page_cid_management", install: false, uninstall: false)
    page(name: "page_add_new_cid", title: titles("page_add_new_cid"), content: "page_add_new_cid", install: false, uninstall: false)
    page(name: "page_add_new_cid_confirm", title: titles("page_add_new_cid_confirm", buildcidlabel()), content: "page_add_new_cid_confirm", install: false, uninstall: false)
    page(name: "page_remove_selected_cid", title: titles("page_remove_selected_cid"), content: "page_remove_selected_cid", install: false, uninstall: false)
}

/*
 * Localization strings
 */
 
// string table for titles
def titles(String name, Object... args) {
  def page_titles = [
    "page_main": "${lname} setup and management",
    "page_discover_devices": "Install ${lname} service",
    "page_remove_all": "Remove all ${lname} devices",
    "confirm_remove_all": "Confirm remove all",
    "page_cid_management": "Contact ID device management",
    "page_add_new_cid": "Add new CID virtual switch",
    "page_add_new_cid_confirm": "Add new CID switch : %s",
    "page_remove_selected_cid": "Remove selected virtual switches",
    "href_refresh_devices": "Send UPNP discovery",
    "section_cid_value": "Build CID Value(USER/ZONE) : %s",
    "section_cid_partition": "Select the CID partition",
    "section_cid_names": "Device Name and Label",
    "section_build_cid": "Build CID Number :",
    "input_cid_name": "Enter the new device name or blank for auto",
    "input_cid_label": "Enter the new device label or blank for auto",    
    "info_page_remove_selected_cid": "Attempted to remove selected devices. This may fail if the device is in use. If it fails review the log and manually remove the usage. Press back to continue.",
    "info_add_new_cid_confirm": "Attempted to add new CID device. This may fail if the device is in use. If it fails review the log. Press back to continue.",
    "info_remove_all_a": "Removed all child devices.",
    "info_remove_all_b": "This will attempt to remove all child devices. This may fail if the device is in use. If it fails review the log and manually remove the usage. Press back to continue.",
    "input_cid_devices": "Remove installed CID virutal switches",
    "input_cid_number": "Select the CID number for this device",
    "input_cid_value": "Zero PAD 3 digit User,Zone or simple regex pattern ex. '001' or '...'",
    "input_cid_partition": "Enter the partition or 0 for system",
    "input_cid_number_raw": "Enter CID # or simple regex pattern",
    "input_selected_devices": "Select device(s) (%s found)",
    "defaultSensorToClosed": "Default zone sensors to closed?",
    "shmIntegration": "Integrate with Smart Home Monitor?",
    "shmChangeSHMStatus": "Automatically change Smart Home Monitor status when armed or disarmed?",
  ]
  if (args)
      return String.format(page_titles[name], args)
  else
      return page_titles[name]
}

// string table for descriptions
def descriptions(name, Object... args) {
  def element_descriptions = [
    "href_discover_devices": "Tap to discover and install your ${lname} Appliance",
    "href_remove_all": "Tap to remove all ${lname} virtual devices",
    "href_cid_management": "Tap to manage CID virtual switches",
    "input_cid_devices": "Tap to select",
    "input_cid_number": "Tap to select",
    "href_refresh_devices": "Tap to select",
    "href_remove_selected_cid": "Tap to remove selected virtual switches",
    "href_add_new_cid": "Tap to add new CID switch",
    "href_add_new_cid_confirm": "Tap to confirm and add"
  ]
  if (args)
      return String.format(element_descriptions[name],args)
  else
      return element_descriptions[name]
}

/**
 * Allow remote device to force the HUB to request an
 * update from the AlarmDecoder.
 *
 * Just a few steps :( but it works.
 * AD2 -> ST-CLOUD -> ST-HUB -> AD2 -> ST-HUB -> ST-CLOUD
 */
mappings {
    path("/update") {
        action: [
            GET: "webserviceUpdate"
        ]
    }
}

/**
 * Section note on saving and < icons
 */
def section_no_save_note() {section("Please note") { paragraph "Do not use the \"<\" or the \"Save\" buttons on this page."}}

/**
 * our main page dynamicly generated to control page based upon logic.
 */
def page_main() {

    // make sure we are listening to all network subscriptions
    initSubscriptions()

    // send out a UPNP broadcast discovery
    discover_alarmdecoder()

    // see if we are already installed
    def foundMsg = ""
    def children = getChildDevices()
    if (children) foundMsg = "**** AlarmDecoder already installed ${children.size()}****"

    dynamicPage(name: "page_main") {
        if (!children) {
            section("") {
                href(name: "href_discover", required: false, page: "page_discover_devices", title: titles("page_discover_devices"), description: descriptions("href_discover_devices"))
            }
        } else {
            section("") {
                paragraph(foundMsg)
            }
            section("") {
                href(name: "href_remove_all", required: false, page: "page_remove_all", title: titles("page_remove_all"), description: descriptions("href_remove_all"))
            }        
            section("") {
                href(name: "href_cid_management", required: false, page: "page_cid_management", title: titles("page_cid_management"), description: descriptions("href_cid_management"))
            }
        }
    }
}

/**
 * Page page_cid_management generator.
 */
def page_cid_management() {
    // TODO: Find a way to clear our current values on loading page
    return dynamicPage(name: "page_cid_management") {
        def found_devices = []
        getAllChildDevices().each { device ->
            if (device.deviceNetworkId.contains(":CID-"))
            {
                found_devices << device.deviceNetworkId.split(":")[2].trim()
            }
        }
        section("") {
            if (found_devices.size()) {
                input "input_cid_devices", "enum", required: false, multiple: true, options: found_devices, title: titles("input_cid_devices"), description: descriptions("input_cid_devices"), submitOnChange: true
                if (input_cid_devices) {
                    href(name: "href_remove_selected_cid", required: false, page: "page_remove_selected_cid", title: titles("page_remove_selected_cid"), description: descriptions("href_remove_selected_cid"), submitOnChange: true)
                }
            }
        }
        section("") {
            href(name: "href_add_new_cid", required: false, page: "page_add_new_cid", title: titles("page_add_new_cid"), description: descriptions("href_add_new_cid"))
        }
        section_no_save_note()
    }
}

/**
 * Page page_remove_selected_cid generator
 */
def page_remove_selected_cid() {
    def errors = []
    getAllChildDevices().each { device ->
        if (device.deviceNetworkId.contains(":CID-"))
        {
            // Only remove the one that matches our list
            def device_name = device.deviceNetworkId.split(":")[2].trim()
            def d = input_cid_devices.find{ it == device_name }
            if (d)
            {
                log.trace("removing CID device ${device.deviceNetworkId}")
                try {
                    deleteChildDevice(device.deviceNetworkId)
                    input_cid_devices.remove(device_name)
                    errors << "Success removing " + device_name                    
                }
                catch (e) { 
                    log.error "There was an error (${e}) when trying to delete the child device"
                    errors << "Error removing " + device_name
                }
            }
        }
    }
    return dynamicPage(name: "page_remove_selected_cid") {
        section("") {
            paragraph titles("info_page_remove_selected_cid")
            errors.each { error -> 
                paragraph(error)
            }
        }
    }
}

/**
 * Page page_add_new_cid generator
 */
def page_add_new_cid() {
    // list of some of the CID #'s and descriptions.
    // 000 will trigger a manual input of the CID number.
    def cid_numbers = [  "0": "000 - Other / Custom",
                       "101": "101 - Pendant Transmitter",
                       "110": "110 - Fire",
                       "150": "150 - 24 HOUR (AUXILIARY)",
                       "154": "154 - Water Leakage",
                       "158": "158 - High Temp",
                       "162": "162 - Carbon Monoxide Detected",
                       "401": "401 - Arm AWAY OPEN/CLOSE",
                       "441": "441 - Arm STAY OPEN/CLOSE",
                       "4[0,4]1": "4[0,4]1 - Arm Stay or Away OPEN/CLOSE"]
    
    return dynamicPage(name: "page_add_new_cid") {
        // show pre defined CID number templates to select from
        section("") {
            paragraph titles("section_build_cid", buildcid())
            input "input_cid_number", "enum", required: true, submitOnChange: true, multiple: false, title: titles("input_cid_number"), description: descriptions("input_cid_number"), options: cid_numbers 
        }
        // if a CID entry is selected then check the value if it is "0" to show raw input section
        if (input_cid_number) {
            if (input_cid_number == "0") {
                section {
                    input(name: "input_cid_number_raw", type: "text", required: true, defaultValue: 110, submitOnChange: true, title: titles("input_cid_number_raw"))
                }
            }
            section {
                paragraph titles("section_cid_value", buildcidvalue())
                input(name: "input_cid_value", type: "text", required: true, defaultValue: 001,submitOnChange: true, title: titles("input_cid_value"))
            }
            section {
                paragraph titles("section_cid_partition")
                input(name: "input_cid_partition", type: "number", required: false, defaultValue: 1, submitOnChange: true, title: titles("input_cid_partition"))
            }
            section {
                paragraph titles("section_cid_names")
                input(name: "input_cid_name", type: "text", required: false, submitOnChange: true, title: titles("input_cid_name"))
                input(name: "input_cid_label", type: "text", required: false, submitOnChange: true, title: titles("input_cid_label"))
            }
            // If input_cid_number or input_cid_number_raw have a value
            if ( (input_cid_number && (input_cid_number != "0")) || (input_cid_number_raw)) {
                section(""){ href(name: "href_add_new_cid_confirm", required: false, page: "page_add_new_cid_confirm", title: titles("page_add_new_cid_confirm", buildcidlabel()+"("+buildcidnetworkid()+")"), description: descriptions("href_add_new_cid_confirm"))}
            }
            section_no_save_note()
        }
    }
}

/**
 * Helper to build a final CID value from inputs
 */
def buildcid() {
    def cidnum = ""
    if (input_cid_number == "0") {
        cidnum = input_cid_number_raw
    } else {
        cidnum = input_cid_number
    }
    return cidnum
}

def buildcidname() {
    if (input_cid_name) {
        return "CID-" + input_cid_name
    } else {
        return buildcidnetworkid()
    }
}

def buildcidlabel() {
    if (input_cid_label) {
        return "CID-" + input_cid_label
    } else {
        return buildcidnetworkid()
    }
}

def buildcidnetworkid() {
    // get the CID value
    def newcid =  buildcid()

    def cv = buildcidvalue()
    def pt = input_cid_partition
    return "CID-${newcid}-${pt}-${cv}"
}

def buildcidvalue() {
    def cidval = input_cid_value
    return cidval
}

/**
 * Page page_add_new_cid_confirm generator.
 */
def page_add_new_cid_confirm() {
    def errors = []
    // get the CID value
    def newcidlabel =  buildcidlabel()
    def newcidname = buildcidname()
    def newcidnetworkid = buildcidnetworkid()
    def cv = input_cid_value
    def pt = input_cid_partition.toInteger()
    
    // Add virtual CID switch if it does not exist.
    def d = getChildDevice("${state.ip}:${state.port}:${newcidlabel}")
    if (!d)
    {
        def nd = addChildDevice("alarmdecoder", "AlarmDecoder action button indicator", "${state.ip}:${state.port}:${newcidnetworkid}", state.hub,
                                [name: "${state.ip}:${state.port}:${newcidname}", label: "${sname} ${newcidlabel}", completedSetup: true])
        nd.sendEvent(name: "switch", value: "off", isStateChange: true, displayed: false)
        errors << "Success adding ${newcidlabel}"
    } else {
        errors << "Error adding ${newcidlabel}: Exists" 
    }

    return dynamicPage(name: "page_add_new_cid_confirm") {
        section("") {
            paragraph titles("info_add_new_cid_confirm")
            errors.each { error -> 
                paragraph(error)
            }            
        }
    }
}

/**
 * Page page_remove_all generator.
 */
def page_remove_all(params) {
    def message = ""

    return dynamicPage(name: "page_remove_all") {
        if (params?.confirm) {
            uninstalled()
            message = titles("info_remove_all_a")
        } else {
            section("") {
                href(name: "href_confirm_remove_all_devices", title: titles("confirm_remove_all"), description: descriptions("href_refresh_devices"), required: false, page: "page_remove_all", params: [confirm: true])
            }
            message = titles("info_remove_all_b")
        }
        section("") {
            paragraph message
        }
    }
}

/**
 * Page page_discover_devices generator. Called periodically to refresh content of the page.
 */
def page_discover_devices() {

    // send out UPNP discovery messages and watch for responses
    discover_alarmdecoder()

    // build list of currently known AlarmDecoder parent devices
    def found_devices = [:]
    def options = state.devices.each { k, v ->
        if (debug) log.debug "page_discover_devices: ${v}"
        def ip = convertHexToIP(v.ip)
        found_devices["${v.ip}:${v.port}"] = "AlarmDecoder @ ${ip}"
    }

    // How many do we have?
    def numFound = found_devices.size() ?: 0

    return dynamicPage(name: "page_discover_devices") {
        section("Devices") {
            input "input_selected_devices", "enum", required: true, title: titles("input_selected_devices",numFound), multiple: true, options: found_devices
            // Allow user to force a new UPNP discovery message
            href(name: "href_refresh_devices", title: titles("href_refresh_devices"), description: descriptions("href_refresh_devices"), required: false, page: "page_discover_devices")
        }
        section("Smart Home Monitor Integration") {
            input(name: "shmIntegration", type: "bool", defaultValue: true, title: titles("shmIntegration"))
            input(name: "shmChangeSHMStatus", type: "bool", defaultValue: true, title: titles("shmChangeSHMStatus"))
        }
        section("Zone Sensors") {
            input(name: "defaultSensorToClosed", type: "bool", defaultValue: true, title: titles("defaultSensorToClosed"))
        }
    }
}

/*** Pre-Defined callbacks ***/

/**
 *  installed()
 */
def installed() {
    log.trace "installed"
    if (debug) log.debug "Installed with settings: ${settings}"

    // initialize everything
    initialize()
}

/**
 * updated()
 */
def updated() {
    log.trace "updated"
    if (debug) log.debug "Updated with settings: ${settings}"

    // re initialize everything
    initialize()
}

/**
 * uninstalled()
 */
def uninstalled() {
    log.trace "uninstalled"

    // disable all scheduling and subscriptions
    unschedule()

    // remove all the devices and children
    def devices = getAllChildDevices()
    devices.each {
        try {
            log.debug "deleting child device: ${it.deviceNetworkId}"
            deleteChildDevice(it.deviceNetworkId)
        }
        catch(Exception e) {
            log.trace("exception while uninstalling: ${e}")
        }
    }
}

/**
 * initialize called upon update and at startup
 *   Add subscriptions and schdules
 *   Create our default state
 *
 */
def initialize() {
    log.trace "initialize"

    // unsubscribe from everything
    unsubscribe()

    // remove all schedules
    unschedule()

    // Create our default state values
    state.lastSHMStatus = null
    state.lastAlarmDecoderStatus = null

    // Network and SHM subscriptions
    initSubscriptions()

    // if a device in the GUI is selected then add it.
    if (input_selected_devices) {
        log.trace "device in gui selected"
        addExistingDevices()
    }

    // Device handler -> service subscriptions
    configureDeviceSubscriptions()

    // keep us subscribed to notifications
    getAllChildDevices().each { device ->
        // Only refresh the main device that has a panel_state
        def device_type = device.getTypeName()
        if (device_type == "AlarmDecoder network appliance")
        {
            if (debug) log.debug("initialize: Found device refresh subscription.")
            device.subscribeNotifications()
        }
    }
}

/*** Handlers ***/

/**
 * locationHandler(evt)
 * Local network messages sent to TCP port 39500 and UPNP UDP port 1900 will be captured here.
 *
 * Test from the AlarmDecoder Appliance:
 *   curl -H "Content-Type: application/json" -X POST -d ‘{"message":"Hi, this is a test from AlarmDecoder network device"}’ http://YOUR.HUB.IP.ADDRESS:39500
 */
def locationHandler(evt) {
    if (debug) log.debug "locationHandler"

    def description = evt.description
    def hub = evt?.hubId

    // many events but we only want PUSH notifications and they have all the data in evt.description
    if (!description)
     return

    if (debug)
      log.debug "locationHandler: description: ${evt.description} name: ${evt.name} value: ${evt.value} data: ${evt.data}"

    def parsedEvent = ["hub":hub]
    try {
        parsedEvent << parseEventMessage(description)
    }
    catch(Exception e) {
        log.error("exception in parseEventMessage: evt: ${evt.name}: ${evt.value} : ${evt.data}")
        return
    }

    if (debug) log.debug("locationHandler parsedEvent: ${parsedEvent}")

    // UPNP LAN EVENTS on UDP port 1900 from 'AlarmDecoder:1' devices only
    if (parsedEvent?.ssdpTerm?.contains("urn:schemas-upnp-org:device:AlarmDecoder:1")) {

        // make sure our state.devices is initialized. return discard.
        def alarmdecoders = getDevices()

        // add the device to state.devices if it does not exist yet
        if (!(alarmdecoders."${parsedEvent.ssdpUSN.toString()}")) {
            if (debug) log.debug "locationHandler: Adding device: ${parsedEvent.ssdpUSN}"
            alarmdecoders << ["${parsedEvent.ssdpUSN.toString()}": parsedEvent]
        } else
        { // It exists so update if needed
            // grab the device object based upon ur ssdpUSN
            if (debug) log.debug  "alarmdecoders ${alarmdecoders}"
            def d = alarmdecoders."${parsedEvent.ssdpUSN.toString()}"

            if (debug) log.debug "locationHandler: checking for device changed values on device=${d}"

            // Did the DNI change? if so update it.
            if (d.ip != parsedEvent.ip || d.port != parsedEvent.port) {
                // update the state.devices DNI
                d.ip = parsedEvent.ip
                d.port = parsedEvent.port

                if (debug) log.debug "locationHandler: device DNI changed values!"

                // Update device by its MAC address if the DNI changes
                def children = getChildDevices()
                children.each {
                    if (it.getDeviceDataByName("mac") == parsedEvent.mac) {
                        it.setDeviceNetworkId((parsedEvent.ip + ":" + parsedEvent.port))
                        if (debug) log.debug "Set new network id: " + parsedEvent.ip + ":" + parsedEvent.port
                    }
                }
            }

            // TODO: if the ssdpPath changes we need to fetch a new one
            if (d.ssdpPath != parsedEvent.ssdpPath) {
                // update the ssdpPath
                d.ssdpPath = parsedEvent.ssdpPath
                if (debug) log.debug "locationHandler: device ssdpPath changed values. need to fetch new description.xml."

                // send out reqeusts for xml description for anyone not verified yet
                // FIXME: verifyAlarmDecoders()
            }
        }
    } else if (parsedEvent?.headers && parsedEvent?.body)
    { // HTTP EVENTS on TCP port 39500 RESPONSES
      // for some reason they hit here and in the parse() in the device?
        def headerString = new String(parsedEvent.headers.decodeBase64())
        def bodyString = new String(parsedEvent.body.decodeBase64())
        def type = (headerString =~ /Content-Type:.*/) ? (headerString =~ /Content-Type:.*/)[0] : null

        if (debug) log.debug ("locationHandler HTTP event type:${type} body:${bodyString} headers:${headerString}")

        // XML PUSH data
        if (type?.contains("xml"))
        {
            getAllChildDevices().each { device ->
                // Only refresh the main device that has a panel_state
                def device_type = device.getTypeName()
                if (device_type == "AlarmDecoder network appliance")
                {
                    if (debug) log.debug ("push_update_alarmdecoders: Found device sending pushed data.")
                    device.parse_xml(headerString, bodyString).each { e-> device.sendEvent(e) }
                }
            }
        }
    }
}

/**
 * Handle remote web requests to the ST cloud services for http://somegraph/update
 */
def webserviceUpdate()
{
    log.trace "webserviceUpdate"
    refresh_alarmdecoders()
    return [status: "OK"]
}

/**
 * Handle our child device action button events
 * sets Contact attributes of the alarmdecoder smoke device
 */
def actionButton(id) {
    if (debug) log.debug("actionButton: desc=${id}")

    // grab our primary AlarmDecoder device object
    def d = getChildDevice("${state.ip}:${state.port}")
    if (!d) {
        log.error("actionButton: Could not find primary device '${state.ip}:${state.port}'.")
        return
    }

    /* FIXME: Need a pin code or some way to trust the request.
    if (id.contains(":disarm")) {
        d.disarm()
    } */

    if (id.contains(":armAway")) {
        d.arm_away()
    }
    if (id.contains(":armStay")) {
        d.arm_stay()
    }
    if (id.contains(":chimeMode")) {
        d.chime()
    }
    if (id.contains(":alarmPanic")) {
        d.panic()
    }
    if (id.contains(":alarmAUX")) {
        d.aux()
    }
    if (id.contains(":alarmFire")) {
        d.fire()
    }
    if (id.contains(":CID-")) {
        def cd = getChildDevice("${id}")
        if (!cd) {
            log.error("actionButton: Could not CID device '${id}'.")
            return
        }
        cd.sendEvent(name: "switch", value: "off", isStateChange: true, filtered: true)
    }
}

/**
 * send event to SmokeAlarm device to set state
 */
def smokeSet(evt) {
    if (debug) log.debug("smokeSet: desc=${evt.value}")

    def d = getChildDevices().find { it.deviceNetworkId.contains(":SmokeAlarm") }
    if (!d)
    {
        log.error("smokeSet: Could not find 'SmokeAlarm' device.")
        return
    }
    d.sendEvent(name: "smoke", value: evt.value, isStateChange: true, filtered: true)
}

/**
 * send event to armAway device to set state
 */
def armAwaySet(evt) {
    if (debug) log.debug("armAwaySet ${evt.value}")
    def d = getChildDevice("${state.ip}:${state.port}:armAway")
    if (!d) {
        log.error("armAwaySet: Could not find 'armAway' device.")
        return
    }
    d.sendEvent(name: "switch", value: evt.value, isStateChange: true, filtered: true)
}

/**
 * send event to armStay device to set state
 */
def armStaySet(evt) {
    if (debug) log.debug("armStaySet ${evt.value}")
    def d = getChildDevice("${state.ip}:${state.port}:armStay")
    if (!d) {
        log.error("armStaySet: Could not find 'armStay' device.")
        return
    }
    d.sendEvent(name: "switch", value: evt.value, isStateChange: true, filtered: true)
}

/**
 * send event to alarmbell indicator device to set state
 */
def alarmBellSet(evt) {
    if (debug)
      log.debug("alarmBellSet ${evt.value}")
    def d = getChildDevice("${state.ip}:${state.port}:alarmBellIndicator")
    if (!d) {
        log.error("alarmBellSet: Could not find 'alarmBellIndicator' device.")
        return
    }
    d.sendEvent(name: "contact", value: evt.value, isStateChange: true, filtered: true)
}

/**
 * send event to chime indicator device to set state
 */
def chimeSet(evt) {
    if (debug) log.debug("chimeSet ${evt.value}")
    def d = getChildDevice("${state.ip}:${state.port}:chimeMode")
    if (!d) {
        log.error("chimeSet: Could not find device 'chimeMode'")
        return
    }
    d.sendEvent(name: "switch", value: evt.value, isStateChange: true, filtered: true)
}

/**
 * send event to bypass indicator device to set state
 */
def bypassSet(evt) {
    if (debug) log.debug("bypassSet ${evt.value}")
    def d = getChildDevice("${state.ip}:${state.port}:bypass")
    if (!d) {
        log.error("bypassSet: Could not find device 'bypass'")
        return
    }
    d.sendEvent(name: "contact", value: evt.value, isStateChange: true, filtered: true)
}

/**
 * send event to ready indicator device to set state
 */
def readySet(evt) {
    if (debug) log.debug("readySet ${evt.value}")
    def d = getChildDevice("${state.ip}:${state.port}:readyIndicator")
    if (!d) {
        log.error("readySet: Could not find 'readyIndicator' device.")
        return
    }
    d.sendEvent(name: "contact", value: evt.value, isStateChange: true, filtered: true)
}

/**
 * send CID event to the correct device if one exists
 * evt.value example !LRR:001,1,CID_1406,ff
 */
def cidSet(evt) {
    log.info("cidSet ${evt.value}")

    // get our CID state and number
    def parts = evt.value.split(',')

    // 1 digit QUALIFIER 1 = Event or Open, 3 = Restore or Close   
    def cidstate = (parts[2][-4..-4] == "1") ? "on" : "off"

    // 3 digit CID number
    def cidnum = parts[2][-3..-1]

    // the CID report value. Zone # or User # or ...
    def cidvalue = parts[0].split(':')[1]

    // the partition # with 0 being system
    def partition =  parts[1].toInteger()

    if (debug) log.debug("cidSet num:${cidnum} part: ${partition} state:${cidstate} val:${cidvalue}")

    def sent = false
    def rawmsg = evt.value
    def device_name = "CID-${cidnum}-${partition}-${cidvalue}"
    def children = getChildDevices()
    children.each {
        if (it.deviceNetworkId.contains(":CID-")) {
            def match = it.deviceNetworkId.split(":")[2].trim()
            if (device_name =~ /${match}/) {
                if (debug) log.error("cidSet device: ${device_name} matches ${match} sendng state ${cidstate}")
                it.sendEvent(name: "switch", value: cidstate, isStateChange: true, filtered: true)
                sent = true
            } else {
                if (debug) log.error("cidSet device: ${device_name} no match ${match}")
            }
        }
    }

    if (!sent) {
        log.error("cidSet: Could not find 'CID-${cidnum}-${partition}-${cidvalue}|XXX' device.")
        return
    }
}

/**
 * Handle Device Command addZone()
 * add a zone during post install to keep it async
 */
def addZone(evt) {

    def i = evt.value
    log.info("App Event: addZone ${i}")
    
    try {        
        def zone_switch = addChildDevice("alarmdecoder", "AlarmDecoder virtual contact sensor", "${evt.data}", state.hub, [name: "${evt.data}", label: "${sname} Zone Sensor #${i}", completedSetup: true])    
        def sensorValue = "open"
        if (settings.defaultSensorToClosed == true) {
            sensorValue = "closed"
        }

        // Set default contact state.
        zone_switch.sendEvent(name: "contact", value: sensorValue, isStateChange: true, displayed: false)
    } catch (e) { 
        log.error "There was an error (${e}) when trying to addZone ${i}"
    }
}


/**
 * Handle Device Command zoneOn()
 * sets Contact attributes of the alarmdecoder device to open/closed
 */
def zoneOn(evt) {
    if (debug) log.debug("zoneOn: desc=${evt.value}")

    def d = getChildDevices().find { it.deviceNetworkId.endsWith("switch${evt.value}") }
    if (d)
    {
        def sensorValue = "closed"
        if (settings.defaultSensorToClosed == true)
            sensorValue = "open"

        d.sendEvent(name: "contact", value: sensorValue, isStateChange: true, filtered: true)
    }
}

/**
 * Handle Device Command zoneOff()
 * sets Contact attributes of the alarmdecoder device to open/closed
 */
def zoneOff(evt) {
    if (debug) log.debug("zoneOff: desc=${evt.value}")

    def d = getChildDevices().find { it.deviceNetworkId.endsWith("switch${evt.value}") }
    if (d)
    {
        def sensorValue = "open"
        if (settings.defaultSensorToClosed == true)
            sensorValue = "closed"

        d.sendEvent(name: "contact", value: sensorValue, isStateChange: true, filtered: true)
    }
}

/**
 * Handle Smart Home Monitor App alarmSystemStatus events and update the UI of the App.
 *
 */
def shmAlarmHandler(evt) {
    if (settings.shmIntegration == false)
        return

    if (debug) log.debug("shmAlarmHandler -- ${evt.value}, lastSHMStatus ${state.lastSHMStatus}, lastAlarmDecoderStatus ${state.lastAlarmDecoderStatus}")

    if (state.lastSHMStatus != evt.value && evt.value != state.lastAlarmDecoderStatus)
    {
        getAllChildDevices().each { device ->
            if (!device.deviceNetworkId.contains(":switch"))
            {
                if (evt.value == "away")
                    device.arm_away()
                else if (evt.value == "stay")
                    device.arm_stay()
                else if (evt.value == "off")
                    device.disarm()
                else
                    log.debug "Unknown SHM alarm value: ${evt.value}"
            }
        }
    }

    state.lastSHMStatus = evt.value
}

/**
 * Handle Alarm events from the AlarmDecoder and
 * send them back to the Smart Home Monitor API to update the
 * status of the alarm panel
 */
def alarmdecoderAlarmHandler(evt) {
    if (settings.shmIntegration == false || settings.shmChangeSHMStatus == false)
        return

    if (debug) log.trace("alarmdecoderAlarmHandler: ${evt.value}")

    if (state.lastAlarmDecoderStatus != evt.value && evt.value != state.lastSHMStatus) {
        if (debug) log.debug("alarmdecoderAlarmHandler: sendLocationEvent")
        sendLocationEvent(name: "alarmSystemStatus", value: evt.value)
    }

    state.lastAlarmDecoderStatus = evt.value
}

/*** Utility ***/

/**
 * Enable primary network and system subscriptions
 */
def initSubscriptions() {
    // subscribe to the Smart Home Manager api for alarm status events
    if (debug) log.debug("initSubscriptions: Subscribe to handlers")
    subscribe(location, "alarmSystemStatus", shmAlarmHandler)

    // subscribe to add zone handler
    subscribe(app, addZone)

    /* subscribe to local LAN messages to this HUB on TCP port 39500 and UPNP UDP port 1900 */
    subscribe(location, null, locationHandler, [filterEvents: false])
}

/**
 * Called by page_discover_devices page periodically
 */
def discover_alarmdecoder() {
    if (debug) log.debug("discover_alarmdecoder")

    // Request HUB send out a UpNp broadcast discovery messages on the local network
    sendHubCommand(new physicalgraph.device.HubAction("lan discovery urn:schemas-upnp-org:device:AlarmDecoder:1", physicalgraph.device.Protocol.LAN))
}

/**
 * Call refresh() on the AlarmDecoder parent device object.
 * This will force the HUB to send a REST API request to the AlarmDecoder Network Appliance.
 * and get back the current status of the AlarmDecoder.
 */
def refresh_alarmdecoders() {
    if (debug) log.debug("refresh_alarmdecoders")

    getAllChildDevices().each { device ->
        // Only refresh the main device that has a panel_state
        def device_type = device.getTypeName()
        if (device_type == "AlarmDecoder network appliance")
        {
            def apikey = device._get_api_key()
            if (apikey) {
                device.refresh()
            } else {
                log.error("refresh_alarmdecoders no API KEY for: ${device} @ ${device.getDataValue("urn")}")
            }
        }
    }
}

/**
 * return the list of known devices and initialize the list if needed.
 *
 * FIXME: SM20180315:
 *        This uses the ssdpUSN as the key when we also use DNI
 *        Why not just use DNI all over or ssdpUSN. Keep it consistent.
 *        We get ssdpUSN from our UPNP discovery messages on port 1900
 *        and then we get DNI messages from our GET requests to the
 *        alarmdecoder web services on port 5000. We can also get DNI
 *        from Notification events we subscribe to when the AlarmDecoder
 *        sends us requests on port 39500. Easy way is to use DNI as we get
 *        it every time from all requests. Downside is we can not have more
 *        than one AlarmDecoder per IP:PORT. This seems ok to me for now.
 *
 *
 *  state.devices structure
 *  [
 *      uuid:0c510e98-8ce0-11e7-81a5-XXXXXXXXXXXXXX:
 *      [
 *          port:1388,
 *          ssdpUSN:uuid:0c510e98-8ce0-11e7-81a5-XXXXXXXXXXXXXX,
 *          devicetype:04,
 *          mac:XXXXXXXXXX02,
 *          hub:936de0be-1cb7-4185-9ac9-XXXXXXXXXXXXXX,
 *          ssdpPath:http://XXX.XXX.XXX.XXX:5000,
 *          ssdpTerm:urn:schemas-upnp-org:device:AlarmDecoder:1,
 *          ip:XXXXXXX2
 *      ],
 *      uuid:592952ba-77b0-11e7-b0c7-XXXXXXXXXXXXXX:
 *      [
 *          port:1388,
 *          ssdpUSN:uuid:592952ba-77b0-11e7-b0c7-XXXXXXXXXXXXXX,
 *          devicetype:04,
 *          mac:XXXXXXXXXX01,
 *          hub:936de0be-1cb7-4185-9ac9-XXXXXXXXXXXXXX,
 *          ssdpPath:/static/device_description.xml,
 *          ssdpTerm:urn:schemas-upnp-org:device:AlarmDecoder:1,
 *          ip:XXXXXXX1
 *      ]
 *  ]
 *
 */
def getDevices() {
    if (!state.devices) {
        state.devices = [:]
    }
    return state.devices
}

/**
 * Add devices selected in the GUI if new.
 */
def addExistingDevices() {
    if (debug) log.debug("addExistingDevices: ${input_selected_devices}")

    def selected_devices = input_selected_devices
    if (selected_devices instanceof java.lang.String) {
        selected_devices = [selected_devices]
    }

    selected_devices.each { dni ->
        def d = getChildDevice(dni)
        if (debug) log.debug("addExistingDevices, getChildDevice(${dni})")
        if (!d) {

            // Find the device with a matching dni XXXXXXXX:XXXX
            def newDevice = state.devices.find { /*k, v -> k == dni*/ k, v -> dni == "${v.ip}:${v.port}" }
            if (debug) log.debug("addExistingDevices, devices.find=${newDevice}")

            if (newDevice) {
                // Set the device network ID so that hubactions get sent to the device parser.
                state.ip = newDevice.value.ip
                state.port = newDevice.value.port
                state.hub = newDevice.value.hub

                // Set URN for the child device
                state.urn = convertHexToIP(state.ip) + ":" + convertHexToInt(state.port)
                if (debug) log.debug("AlarmDecoder webapp api endpoint('${state.urn}')")

                // Create device adding the URN to its data object
                d = addChildDevice("alarmdecoder",
                                   "AlarmDecoder network appliance",
                                   "${state.ip}:${state.port}",
                                   newDevice?.value.hub,
                                   [
                                       name: "${state.ip}:${state.port}",
                                       label: "${lname}(${sname})",
                                       completedSetup: true,
                                       /* data associated with this AlarmDecoder */
                                       data:[
                                                // save mac address to update if IP / PORT change
                                                mac: newDevice.value.mac,
                                                ssdpUSN: newDevice.value.ssdpUSN,
                                                urn: state.urn,
                                                ssdpPath: newDevice.value.ssdpPath
                                            ]
                                   ])

                // Set default device state to notready.
                d.sendEvent(name: "panel_state", value: "notready", isStateChange: true, displayed: true)

            }
            
            // Add virtual zone contact sensors if they do not exist.
            def mainDevice = getChildDevice("${state.ip}:${state.port}")
            if (adApiKey == "UPDATE_ME"){
                log.error("AD API Key needs to be updated in alarmdecoder-service.groovy from ST website.")
            }
            else if (mainDevice)
            {       
                if (debug) log.debug ">>> mainDevice: ${mainDevice}"

                def result = new physicalgraph.device.HubAction(
                    method: "GET",
                    path: "/api/v1/zones?apikey=${adApiKey}",
                    headers: [
                            "HOST" : "${mainDevice.getDataValue("urn")}",
                            "Content-Type": "application/json"],
                            null,
                            [callback: parseZoneData]
                )
                sendHubCommand(result);
            }
            else
                log.error("Unable to find main device - zone setup incomplete.")

            // Add virtual Smoke Alarm sensors if it does not exist.
            def cd = state.devices.find { k, v -> k == "${state.ip}:${state.port}:SmokeAlarm" }
            if (!cd)
            {
                def nd = addChildDevice("alarmdecoder", "AlarmDecoder virtual smoke alarm", "${state.ip}:${state.port}:SmokeAlarm", state.hub,
                [name: "${state.ip}:${state.port}:smokeAlarm", label: "${sname} Smoke Alarm", completedSetup: true])
                nd.sendEvent(name: "smoke", value: "clear", isStateChange: true, displayed: false)
            }

            // Add virtual Arm Stay switch if it does not exist.
            cd = state.devices.find { k, v -> k == "${state.ip}:${state.port}:armStay" }
            if (!cd)
            {
                def nd = addChildDevice("alarmdecoder", "AlarmDecoder action button indicator", "${state.ip}:${state.port}:armStay", state.hub,
                [name: "${state.ip}:${state.port}:armStay", label: "${sname} Stay", completedSetup: true])
                nd.sendEvent(name: "switch", value: "off", isStateChange: true, displayed: false)
            }

            // Add virtual Arm Away switch if it does not exist.
            cd = state.devices.find { k, v -> k == "${state.ip}:${state.port}:armAway" }
            if (!cd)
            {
                def nd = addChildDevice("alarmdecoder", "AlarmDecoder action button indicator", "${state.ip}:${state.port}:armAway", state.hub,
                [name: "${state.ip}:${state.port}:armAway", label: "${sname} Away", completedSetup: true])
                nd.sendEvent(name: "switch", value: "off", isStateChange: true, displayed: false)
            }

            // Add virtual Chime switch if it does not exist.
            cd = state.devices.find { k, v -> k == "${state.ip}:${state.port}:chimeMode" }
            if (!cd)
            {
                def nd = addChildDevice("alarmdecoder", "AlarmDecoder action button indicator", "${state.ip}:${state.port}:chimeMode", state.hub,
                [name: "${state.ip}:${state.port}:chimeMode", label: "${sname} Chime", completedSetup: true])
                nd.sendEvent(name: "switch", value: "off", isStateChange: true, displayed: false)
            }

            // Add virtual Bypass switch if it does not exist.
            cd = state.devices.find { k, v -> k == "${state.ip}:${state.port}:bypass" }
            if (!cd)
            {
                def nd = addChildDevice("alarmdecoder", "AlarmDecoder status indicator", "${state.ip}:${state.port}:bypass", state.hub,
                [name: "${state.ip}:${state.port}:bypass", label: "${sname} Bypass", completedSetup: true])
                nd.sendEvent(name: "contact", value: "close", isStateChange: true, displayed: false)
            }

            // Add virtual Ready contact if it does not exist.
            cd = state.devices.find { k, v -> k == "${state.ip}:${state.port}:readyIndicator" }
            if (!cd)
            {
                def nd = addChildDevice("alarmdecoder", "AlarmDecoder status indicator", "${state.ip}:${state.port}:readyIndicator", state.hub,
                [name: "${state.ip}:${state.port}:readyIndicator", label: "${sname} Ready", completedSetup: true])
                nd.sendEvent(name: "contact", value: "close", isStateChange: true, displayed: false)
            }

            // Add virtual Alarm Bell contact if it does not exist.
            cd = state.devices.find { k, v -> k == "${state.ip}:${state.port}:alarmBell" }
            if (!cd)
            {
                def nd = addChildDevice("alarmdecoder", "AlarmDecoder status indicator", "${state.ip}:${state.port}:alarmBellIndicator", state.hub,
                [name: "${state.ip}:${state.port}:alarmBellIndicator", label: "${sname} Alarm Bell", completedSetup: true])
                nd.sendEvent(name: "contact", value: "close", isStateChange: true, displayed: false)
            }

            // Add FIRE alarm button if it does not exist.
            cd = state.devices.find { k, v -> k == "${state.ip}:${state.port}:alarmFire" }
            if (!cd)
            {
                def nd = addChildDevice("alarmdecoder", "AlarmDecoder action button indicator", "${state.ip}:${state.port}:alarmFire", state.hub,
                [name: "${state.ip}:${state.port}:alarmFire", label: "${sname} Fire Alarm", completedSetup: true])
                nd.sendEvent(name: "switch", value: "close", isStateChange: true, displayed: false)
            }

            // Add Panic alarm button if it does not exist.
            cd = state.devices.find { k, v -> k == "${state.ip}:${state.port}:alarmPanic" }
            if (!cd)
            {
                def nd = addChildDevice("alarmdecoder", "AlarmDecoder action button indicator", "${state.ip}:${state.port}:alarmPanic", state.hub,
                [name: "${state.ip}:${state.port}:alarmPanic", label: "${sname} Panic Alarm", completedSetup: true])
                nd.sendEvent(name: "switch", value: "close", isStateChange: true, displayed: false)
            }

            // Add AUX alarm button if it does not exist.
            cd = state.devices.find { k, v -> k == "${state.ip}:${state.port}:alarmAUX" }
            if (!cd)
            {
                def nd = addChildDevice("alarmdecoder", "AlarmDecoder action button indicator", "${state.ip}:${state.port}:alarmAUX", state.hub,
                [name: "${state.ip}:${state.port}:alarmAUX", label: "${sname} AUX Alarm", completedSetup: true])
                nd.sendEvent(name: "switch", value: "close", isStateChange: true, displayed: false)
            }
        }
    }
}

/*
* This gets the response from the AD API for get zones. It adds switches based on
their st_id (smartthings id). If the st_id is null, you need to log into the AD webapp
go into settings-> advanced and click generate device handler.
*/
def parseZoneData(physicalgraph.device.HubResponse hubResponse) {
    def json = null 
    try {
        json = new groovy.json.JsonSlurperClassic().parseText(hubResponse.body)
    }
    catch (e) {
        log.error(e)
    }

    if (json)
    {
        json.zones.each
        {
            z ->
                if(debug) log.debug("Zone (${z.zone_id}) ST ID: ${z.st_id}")

                if (z.st_id != null && z.st_id > 0){
                    def existingSwitch = state.devices.find { k, v -> k == "${state.ip}:${state.port}:switch${z.st_id}" }

                    if (!existingSwitch)
                    {
                        if(debug) log.debug("Adding ${sname} Zone Sensor #${z.zone_id}")

                        def zoneName =  "${sname} Zone Sensor #${z.zone_id}"
                        if (z.name?.trim()){
                            zoneName = z.name
                        }

                        def zone_switch = addChildDevice("alarmdecoder", "AlarmDecoder virtual contact sensor", "${state.ip}:${state.port}:switch${z.st_id}", state.hub, [name: "${state.ip}:${state.port}:switch${z.st_id}", label: "${zoneName}", completedSetup: true])

                        def sensorValue = "open"
                        if (settings.defaultSensorToClosed == true)
                            sensorValue = "closed"

                        // Set default contact state.
                        zone_switch.sendEvent(name: "contact", value: sensorValue, isStateChange: true, displayed: false)
                    }
                }
        }
    }
}

/**
 * Configure subscriptions the virtual devices will send too.
 */
private def configureDeviceSubscriptions() {
    if (debug) log.debug("configureDeviceSubscriptions")
    def device = getChildDevice("${state.ip}:${state.port}")
    if (!device) {
        log.error("configureDeviceSubscriptions: Could not find primary device.")
        return
    }

    /* Handle events sent from the AlarmDecoder network appliance device
     * to update virtual zones when they change.
     */
    subscribe(device, "zone-on", zoneOn, [filterEvents: false])
    subscribe(device, "zone-off", zoneOff, [filterEvents: false])

    /* Subscribe to Smart Home Monitor(SHM) alarmStatus events
     */
    subscribe(device, "alarmStatus", alarmdecoderAlarmHandler, [filterEvents: false])

    // subscrib to smoke-set handler for updates
    subscribe(device, "smoke-set", smokeSet, [filterEvents: false])

    // subscribe to arm-away handler
    subscribe(device, "arm-away-set", armAwaySet, [filterEvents: false])

    // subscribe to arm-stay handler
    subscribe(device, "arm-stay-set", armStaySet, [filterEvents: false])

    // subscribe to chime handler
    subscribe(device, "chime-set", chimeSet, [filterEvents: false])

    // subscribe to bypass handler
    subscribe(device, "bypass-set", bypassSet, [filterEvents: false])

    // subscribe to alarm bell handler
    subscribe(device, "alarmbell-set", alarmBellSet, [filterEvents: false])

    // subscribe to ready handler
    subscribe(device, "ready-set", readySet, [filterEvents: false])

    // subscribe to CID handler
    subscribe(device, "cid-set", cidSet, [filterEvents: false])

}

/**
 * Parse local network messages.
 *
 * May be to UDP port 1900 for UPNP message or to TCP port 39500
 * for local network to hub push messages.
 *
 */
private def parseEventMessage(String description) {
    if (debug)
      log.debug "parseEventMessage: $description"
    def event = [:]
    def parts = description.split(',')
    parts.each { part ->
        part = part.trim()        
        if (part.startsWith('devicetype:')) {
            def valueString = part.split(":")[1].trim()
            event.devicetype = valueString
        }
        else if (part.startsWith('mac:')) {
            def valueString = part.split(":")[1].trim()
            if (valueString) {
                event.mac = valueString
            }
        }
        else if (part.startsWith('networkAddress:')) {
            def valueString = part.split(":")[1].trim()
            if (valueString) {
                event.ip = valueString
            }
        }
        else if (part.startsWith('deviceAddress:')) {
            def valueString = part.split(":")[1].trim()
            if (valueString) {
                event.port = valueString
            }
        }
        else if (part.startsWith('ssdpPath:')) {
            part -= "ssdpPath:"
            def valueString = part.trim()
            if (valueString) {
                event.ssdpPath = valueString
            }
        }
        else if (part.startsWith('ssdpUSN:')) {
            part -= "ssdpUSN:"
            def valueString = part.trim()
            if (valueString) {
                event.ssdpUSN = valueString
            }
        }
        else if (part.startsWith('ssdpTerm:')) {
            part -= "ssdpTerm:"
            def valueString = part.trim()
            if (valueString) {
                event.ssdpTerm = valueString
            }
        }
        else if (part.startsWith('headers')) {
            part -= "headers:"
            def valueString = part.trim()
            if (valueString) {
                event.headers = valueString
            }
        }
        else if (part.startsWith('body')) {
            part -= "body:"
            def valueString = part.trim()
            if (valueString) {
                event.body = valueString
            }
        }
    }

    event
}

/**
 * Send a request for the description.xml For every known AlarmDecoder
 * we have discovered that is not verified.
 */
def verifyAlarmDecoders() {
    def devices = getDevices().findAll { it?.value?.verified != true }

  if (devices) {
        log.warn "verifyAlarmDecoders: UNVERIFIED Decoders!: $devices"
  }

  devices.each {
        if (it?.value?.ssdpPath?.contains("xml")) {
            verifyAlarmDecoder((it?.value?.ip + ":" + it?.value?.port), it?.value?.ssdpPath)
        } else {
            log.warn("verifyAlarmDecoders: invalid ssdpPath not an xml file")
        }
    }
}

/**
 * Send a GET request from the HUB to the AlarmDecoder for its descrption.xml file
 */
def verifyAlarmDecoder(String deviceNetworkId, String ssdpPath) {
  String ip = getHostAddressFromDNI(deviceNetworkId)

  if (debug) log.debug("verifyAlarmDecoder: $deviceNetworkId ssdpPath: ${ssdpPath} ip: ${ip}")

  def result = new physicalgraph.device.HubAction([
  method: "GET",
        path: ssdpPath,
        headers: [Host: ip, Accept: "*/*"]],
        deviceNetworkId
  )
  sendHubCommand(result)
}

/**
 * Convert from internal format networkAddress:C0A8016F to a real IP address string 192.168.1.111
 */
private String convertHexToIP(hex) {
    [convertHexToInt(hex[0..1]),convertHexToInt(hex[2..3]),convertHexToInt(hex[4..5]),convertHexToInt(hex[6..7])].join(".")
}

/**
 * convert hex encoded string to integer
 */
private Integer convertHexToInt(hex) {
    Integer.parseInt(hex,16)
}

/**
 * build a URI host address of the AlarmDecoder web appliance for web requests.
 *  ex. XXX.XXX.XXX.XXX:XXXXX -> 192.168.1.1:5000
 */
private getHostAddressFromDNI(d) {
  def parts = d.split(":")
  def ip = convertHexToIP(parts[0])
  def port = convertHexToInt(parts[1])
  return ip + ":" + port
}
