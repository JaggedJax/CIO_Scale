/**
 * @author williamw
 */
var scale_applet;
var scale_timeout;
var scale_unit;
var scale_element;
var scale_after;
var scale_freeze_func;
var scale_lastWeight;
var scale_auto_freeze;
var scale_stable_count;
var scale_disable_timeout;
var scale_freeze_after_num_dups;

window.onunload = function(){
	if (scale_applet){
		closeConnection();
	}
}

function scaleSetup(u, e, a, autofreeze, freeze_func){
	scale_unit = u;
	scale_element = e;
	scale_after = a;
	scale_freeze_after_num_dups = 2;
	// Auto-freeze options
	scale_auto_freeze = (autofreeze === true) ? true : false;
	scale_freeze_func = (freeze_func) ? freeze_func : null;
	scale_stable_count = 0;
	scale_disable_timeout = false;
}

function closeConnection(){
	//console.log('Attempting to close scale connection');
	if(scale_applet){
		scale_applet.closeConnection();
	}
}

/**
 * Wait until scale applet loaded then set up scale connection
 * @param {string} u Unit of measure to return. One of: "mg", "g", "kg", "carat", "tael", "gr", "dwt", "t", "tn", "ozt", "oz", "lb"
 * @param {string} e ID of input field where weight will be put
 * @param {string} a Name of function to call whenever a weight is returned
 * @param {int} i Polling interval in milliseconds. Use 0 to disable automatic polling.
 * @param {boolean} autofreeze Should we tell your application to stop requesting weights after the scale stabilizes?
 * @param {string} freeze_func Optional function to call if autofreeze is set to true
 * @returns {undefined}
 */
function waituntilok(u, e, a, i, autofreeze, freeze_func) {
	var ready = false;
	scale_applet = document.getElementById('scaleApplet');
	try{
		if (scale_applet){
			scaleSetup(u, e, a, autofreeze, freeze_func);
			if (scale_applet.isActive()){
				ready = true;
				//console.log('Scale is ready!');
				if (i && i > 0){
					startWeight(i);
				}
				else{
					getWeight();
				}
			}
		}
		if(!ready){
			//console.log('Scale not ready');
			setTimeout(waituntilok, 100);
		}
	}catch(e){
		//console.log('Error waiting for scale: '+e.message);
		// Will get here if applet is blocked or doesn't load
    }
}

// Polling interval in milliseconds
function startWeight(interval){
	if (!interval){
		interval = 200;
	}
	getWeight();
	if(scale_disable_timeout === true){
		scale_disable_timeout = false;
		scale_stable_count = 0;
	}
	else{
		scale_timeout = setTimeout(function() { startWeight(interval); }, interval);
	}
}

function stopWeight(){
	scale_disable_timeout = true;
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
		if(scale_auto_freeze){
			if(weight > 0 && scale_lastWeight === weight){
				scale_stable_count++;
			}
			else{
				scale_stable_count = 0;
			}
			if(scale_stable_count == scale_freeze_after_num_dups){
				stopWeight();
				if(scale_freeze_func){
					window[scale_freeze_func]();
				}
			}
		}
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