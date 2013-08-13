/**
 * @author williamw
 */
var scale_applet;
var scale_timeout;
var scale_unit;
var scale_element;
var scale_after;
var scale_lastWeight;

/**
window.onunload = function(){
	if (scale_applet){
		closeConnection();
	}
}
*/

function scaleSetup(u, e, a){
	scale_unit = u;
	scale_element = e;
	scale_after = a;
}

function closeConnection(){
	//console.log('Attempting to close scale connection');
	if(scale_applet){
		scale_applet.closeConnection();
	}
}

function waituntilok(u, e, a) {
	scale_applet = document.getElementById('scaleApplet');
	try{
		if (scale_applet){
			scaleSetup(u, e, a);
			if (scale_applet.isActive()){
				getWeight();
			}
			else{
				setTimeout(waituntilok, 100);
			}
		}
	}catch(e){
		// Will get here if applet is blocked or doesn't load
    }
}

// Polling interval in milliseconds
function startWeight(interval){
	if (!interval){
		interval = 200;
	}
	getWeight();
	scale_timeout = setTimeout(function() { startWeight(interval); }, interval);
}

function stopWeight(){
	clearTimeout(scale_timeout);
}

function getWeight(){
	if(scale_applet){
		var weight_box = document.getElementById(scale_element);
		var weight = 0;
		var u = null;
		if (scale_unit != null && scale_unit != ''){
			weight = scale_applet.getWeight(scale_unit);
		}
		else{
			weight = scale_applet.getWeight();
			u = scale_applet.getUnit();
		}
		var message = "";
		if (weight == -1){
			message = scale_applet.getMessage();
			weight = 0;
		}
		weight = roundNumber(weight, 2);
		cur_weight = weight_box
			? roundNumber(weight_box.value, 2)
			: weight;
		
		if (weight_box && weight != scale_lastWeight || weight != cur_weight){
			scale_lastWeight = weight;
			weight_box.value = weight;
			if (scale_unit == null || scale_unit == ''){
				document.getElementById('unit').innerHTML = u;
			}
			if (scale_after != null && scale_after != ''){
				window[scale_after]();
			}
		}
		document.getElementById('message').innerHTML = message;
	}
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