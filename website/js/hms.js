function send(params){
	send(params, null);
}
function send(params, handleResult){
	if (window.XMLHttpRequest) {
		xmlHttp = new XMLHttpRequest();
	} else {
		xmlHttp = new ActiveXObject("Microsoft.XMLHTTP");
	}
	xmlHttp.onreadystatechange = function() {
		if (xmlHttp.readyState == 4) {
			if(xmlHttp.status == 200 || xmlHttp.status == 204) {
				if (handleResult != null && xmlHttp.status == 200){
					handleResult(xmlHttp.responseText);					
				}
			} else {
				Materialize.toast('Errorcode: ' + xmlHttp.status + " executing " + params, 3000, 'rounded');
			}
		}
	};
	xmlHttp.open("POST", "/submit", true);
	xmlHttp.send(params);
}
function switchToMode(mode){
	send("mode=" + mode);
}
function shutdown(device){
	send("shutdown=" + device);
}
function powerup(device){
	send("powerup=" + device);
}
function addAlarm(time){
	send("addalarmat=" + time)
}
function removeAlarm(time){
	send("removealarmat=" + time)
}
function silenceAlarms(){
	send("silencealarms=true");
}
function updateRuleActivation(name, doActivate){
	if (doActivate){
		send("enablerule=" + name);
	} else {
		send("disablerule=" + name);
	}
}
function setPCVolumeTo(value){
	send("setpcvolume=" + value);
}
function startProgramOnPC(name){
	send("startprogram=" + name + "&on=pc");
}
function loadLog(handleResult){
	send("loadlog=true", handleResult);
}
function pressKeyOnLight(key){
	send("presskey=" + key + "&ondevice=light");
}
function pressKeyOnSub(key){
	send("presskey=" + key + "&ondevice=sub");
}
function pressPowerOnKeyOnRadio(key){
	send("radiochannel=" + key + "&powerstate=" + true);
}
function pressPowerOffKeyOnRadio(key){
	send("radiochannel=" + key + "&powerstate=" + false);
}
function startVisualizer(){
	send("startvisualizer=true");
}
function startAmbilight(){
	send("startambilight=true");
}
function stopFeatures(){
	send("stopfeatures=true");
}