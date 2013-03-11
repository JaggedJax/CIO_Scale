<html>
<head>
	<title>Installing Scale</title>
	<script type="text/javascript" src="scale.js"></script>
</head>
<body onload="waituntilok(null, 'weight');" title="Installing Scale">
	<?php
		function base_url(){
			$protocol = $_SERVER['HTTPS'] ? "https" : "http";
			return $protocol . "://" . $_SERVER['HTTP_HOST'];
		}
	?>
	
<applet archive="ScaleAppletSigned.jar" code="ScaleApplet.class" name="scaleApplet" id="scaleApplet" width="200" height="40" >
	<!-- <param name="codebase" value="ch.ntb.usb-0.5.9.jar"> -->
	<param name="url" value="<?php echo base_url(); ?>">
	<param name="install" value="install">
</applet>
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
