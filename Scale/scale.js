/**
 * @author William Wynn (CIO Technologies)
 */
var applet;
var timeout;
var unit;
var element;
var after;
var lastWeight;

window.onbeforeunload = function(){
	if (applet){
		closeConnection();
	}
}

function scaleSetup(u, e, a){
	unit = u;
	element = e;
	after = a;
}

function closeConnection(){
	console.log('Attempting to close scale connection');
	applet.closeConnection();
}

function waituntilok(u, e, a) {
	applet = document.getElementById('scaleApplet');
	try{
		if (applet){
			scaleSetup(u, e, a);
			if (applet.isActive()){
				getWeight();
			}
			else{
				setTimeout(waituntilok(), 1000);
			}
		}
	}catch(e){
		// Will get here is applet is blocked or doesn't load
    }
}

// Polling interval in milliseconds
function startWeight(interval){
	if (!interval){
		interval = 500;
	}
	getWeight();
	timeout = setTimeout(function() { startWeight(interval); }, interval);
}

function stopWeight(){
	clearTimeout(timeout);
}

function getWeight(){
	var weight = 0;
	var u = null;
	if (unit != null && unit != '')
		weight = applet.getWeight(unit);
	else{
		weight = applet.getWeight();
		u = applet.getUnit();
	}
	var message = "";
	if (weight == -1){
		message = applet.getMessage();
		weight = 0;
	}
	weight = roundNumber(weight, 2);
	if (weight != lastWeight){
		lastWeight = weight;
		document.getElementById(element).value = weight;
		if (unit == null || unit == '')
			document.getElementById('unit').innerHTML = u;
		if (after != null && after != '')
			window[after]();
	}
	document.getElementById('message').innerHTML = message;
	return true;
}

/**
 * Round number to d decimal places
 * @param {int} num Number to round
 * @param {int} d Number of decimal places to round to
 */
function roundNumber(num, d) {
	if (d == 0)
		return Math.round(num);
	return Math.round(num*Math.pow(10,d))/Math.pow(10,d);
}
