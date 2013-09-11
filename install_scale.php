<html>
<head>
	<title>Installing Scale</title>
	<script type="text/javascript" src="scale.js"></script>
</head>
<body onload="waituntilok(null, 'weight', null, 500);" title="Installing Scale">
	<?php
		function base_url(){
			$protocol = $_SERVER['HTTPS'] ? "https" : "http";
			return $protocol . "://" . $_SERVER['HTTP_HOST'];
		}
	?>

<object type="application/x-java-applet" name="scaleApplet" id="scaleApplet" width="200" height="40">
	<param name="Codebase" value="java/">
	<param name="archive" value="ScaleAppletSigned.jar">
	<param name="code" value="ScaleApplet.class">
	<param name="url" value="<?php echo base_url(); ?>">
	<param name="install" value="install">
</object>
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
