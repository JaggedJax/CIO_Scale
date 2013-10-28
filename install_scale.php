<html>
<head>
	<title>Installing Scale</title>
	<script type="text/javascript" src="http://www.java.com/js/deployJava.js"></script>
	<script type="text/javascript" src="scale.js"></script>
</head>
<body onload="waituntilok(null, 'weight', null, 500);" title="Installing Scale">
	<?php
		function base_url(){
			$protocol = $_SERVER['HTTPS'] ? "https" : "http";
			return $protocol . "://" . $_SERVER['HTTP_HOST'];
		}
	?>
<script>
	var attributes = {
		name:'scaleApplet', id:'scaleApplet',
		codebase:'java/', code:'ScaleApplet',
		cache_archive:'ScaleAppletSigned.jar',
		cache_option:'Plugin',
		initial_focus:false,
		width:200, height:40 };
	var parameters = { url:'<?php echo base_url(); ?>', install:'install' } ;
	var version = '1.7' ;
	deployJava.runApplet(attributes, parameters, version);
</script>
<br>
<form id="testform" name="testform">
	<input type="text" name="weight" id="weight" size="6" value="0" /><span id="unit"></span>
	<br>
	<span id="message"></span>
</form>
<br>
<button id="getWeight" onclick="getWeight()">Get Weight</button>
</body>
</html>
