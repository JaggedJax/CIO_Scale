/**
 * @author williamw
 */
var timeout;
var unit;
var element;
var after;

function scaleSetup(u, e, a){
	unit = u;
	element = e;
	after = a;
}

function waituntilok(u, e, a) {
	scaleSetup(u, e, a);
	if (document.getElementById('scaleApplet').isActive())
		getWeight();
	else
		settimeout(waituntilok(), 1000)
}

function startWeight(){
	getWeight();
	timeout = setTimeout(startWeight(), 2000);
}

function stopWeight(){
	clearTimeout(timeout);
}

function getWeight(){
	var weight = 0;
	var u = null;
	if (unit != null && unit != '')
		weight = document.getElementById('scaleApplet').getWeight(unit);
	else{
		weight = document.getElementById('scaleApplet').getWeight();
		u = document.scaleApplet.getUnit();
	}
	var message = "";
	if (weight == -1){
		message = document.getElementById('scaleApplet').getMessage();
		weight = 0;
	}
	document.getElementById(element).value = roundNumber(weight, 2);
	if (unit == null || unit == '')
		document.getElementById('unit').innerHTML = u;
	document.getElementById('message').innerHTML = message;
	if (after != null && after != '')
		window[after]();
	return true;
}

/**
 * 
 * @param {int} num Number to round
 * @param {int} d Number of decimal places to round to
 */
function roundNumber(num, d) {
	if (d == 0)
		return Math.round(num);
	return Math.round(num*Math.pow(10,d))/Math.pow(10,d);
}