Java applet for USB scale integration with a web application
============================

CIO Scale Java applet written by: William S. Wynn (CIO Technologies)

Thanks to the following for their help and input:
* Joe Carr

CIO Scale is licensed under the MIT X11 License. Libraries included herein are unmodified, listed below, and are covered by their respective license.

* libusb-win32 wrapper (Java): http://libusbjava.sourceforge.net
* Zadig: http://zadig.akeo.ie/
* Apache Commons-IO: https://commons.apache.org/proper/commons-io/
* libusb-win32 (No longer used - Replaced by Zadig): http://sourceforge.net/apps/trac/libusb-win32

ScaleAppletSigned.jar takes two parameters at runtime:

* url - Base url of page. eg: https://cioremotedemo.ciotech.com
* install - Use one of the following:
 * install or force - Either of these will trigger the installer, even if a valid scale is detected.
 * no - This will never launch the installer, even if no scale is detected.
 * If you leave the value empty or don't pass it at all, the installer will launch only if no scale is detected. This is generally the best method.

The /src folder contains ScaleApplet.java which is the only custom Java code you will need for the integration.
	
If you import this base folder (not just src) into Eclipse as an existing project, it _Should_ be able to build ScaleApplet.java. Otherwise you might have to link the libraries properly yourself. Better instructions to come soon.

I normally build ScaleApplet.java and then open ScaleAppletUnsigned.jar as a zip file and just copy in the new ScaleApplet.class file instead of creating the entire jar.

## Drivers
The applet expects to find libusb-32.exe and libusb-64.exe in /BASE_URL/drivers/usb of the webserver. This can be changed in the java code.
Those two files are the custom driver installers.

folder /drivers/usb contents:

* libusb-32.exe & libusb-64.exe installers
* zadig_xp_vXXX.exe
	- GUI installer for scale special drivers.
	- This program is included in and launched by the installers listed above.
	- XP version is included for wider support.
* inf-wizard.exe
	- No longer used. Replaced by Zadig.
* install-filter-win32.exe & install-filter-win64.exe
	- These are the filter drivers that can be used to talk to a usb scale w/o installing the special drivers. I haven't played with this much.
	- Instructions for use are at: http://sourceforge.net/apps/trac/libusb-win32/wiki#Installation
	- The downside of this is you might have to install every time the computer is restarted. The upside is it can be automated more and doesn't hog the default scale drivers, I think.
* uac-launch.exe
	- A program I modified to launch any program and ask for admin privalages.
	- Parameters passed are the parameters for the file to launch.
	- The program is compiled with Microsoft Visual C++ 2010 Express
	- Project source included at /uac

>uac-launch.exe \<File to launch\> \<Parameters\>

Folder /libusb contains the raw files that libusb-32.exe and libusb-64.exe actually contain. The .iip files are Clickteam Install Creator Pro files used to create/modify the exe installers. They use the /libusb folder to build the installers.

## Create jar from scratch
    jar cvf jarFile.jar inputfile1.class inputfile2.class folder1 folder2

Building requires plugin.jar and deploy.jar in the build path. Usually found at: JAVA_HOME/jre/lib

Make sure to add the following line to the jar's manifest or Java will display an extra warning message

    Trusted-Library: true

## Using the Applet
Example code to include in page:

    <script type="text/javascript" src="javascript/scale.js"></script> <!-- File included in project folder -->
	<script type="text/javascript" src="http://www.java.com/js/deployJava.js"></script>
    <!-- Must call scaleSetup() in body onload(). Can optionally pass setup parameters of: (desired units, element ID to update, function to call after update, Polling interval in milliseconds) -->
    <body onload="scaleSetup('oz', 'weight_oz', 'PostageUpdate', 200);">
    <!-- Alternate parameters below will automatically pause scale when it's stable and optionally call your own JS function when this happens. -->
    <!--  <body onload="scaleSetup('oz', 'weight_oz', 'PostageUpdate', 200, true, 'someOptionalFunc');"> -->
    
    <!-- Example if applet was stored at <https://cioremotedemo.ciotech.com/java/ScaleAppletSigned.jar> -->
	<script>
		var attributes = {
			name:'scaleApplet', id:'scaleApplet',
			codebase:'java/', code:'ScaleApplet',
			cache_archive:'ScaleAppletSigned.jar',
			cache_option:'Plugin',
			initial_focus:false,
			width:200, height:40 };
		var parameters = { url:'https://cioremotedemo.ciotech.com' } ;
		var version = '1.7' ;
		deployJava.runApplet(attributes, parameters, version);
	</script>
    
    <!-- Call getWeight() from scale.js to manually pull weight from scale and put it in desired element ID  -->
    <input type="button" onclick="getWeight(); return false;" value="Get Weight">
    
    <! -- We'll put it in this input form field -->
    <input type="text" name="weight_oz" id="weight_oz" class="textbox" onchange="PostageUpdate();" size="5" value="0" maxlength="6" />Ounces

Once the applet has loaded, it will call the JS function cioScaleReady() which is included in scale.js or you may write your own. This may not work if testing from within Eclipse. I have to build it and test in a real browser for the callback to work.

The legacy_lifecycle option will keep the applet paused and the JVM running even while not on the page. This makes future runs of the applet instant with no JVM startup or scale re-connection needed. If the applet is truely ended, the destroy() function will close the scale connection automatically.

The file install_scale.php included in this folder is a good sample file that will force a scale install, and show the basics of how to use the applet.

The connection code is really messy right now and I'll try and clean it up when I have time.

## Signing
You must use a signed jar file. An unsigned file will not load.
Better signing instructions will come soon.

* In command prompt go to folder containing unsigned jar
* Use your own jdk directory in the commands below of course.

* Create Store: (If an error about this alias is thrown, make any new -alias here (Doesn't matter what) and also change it in the next command)

    keytool -genkey -alias signJar -keystore compstore -keypass mypass -dname "CN=William Wynn, OU=CIO Remote, O=CIO Technologies Inc., L=Santa Barbara, ST=CA, C=US" -storepass SuperSecretPassword"

* Sign Jar:

    jarsigner -keystore compstore -storepass SuperSecretPassword -keypass mypass -signedjar ScaleAppletSigned.jar ScaleAppletUnsigned.jar signJar

## TODO

* Only show users the possible scales during install instead of all USB devices.
* Look into TCP/IP scale interfaces. Example: http://us.mt.com/dam/mt_ext_files/Editorial/Generic/8/Ethernet_interface_option_for_Excellence_bal_BA_Editorial-Generic_1116310641541_files/excellence-ethernet-ba-e-11780579a.pdf

## License

The following license covers files in the src/ directory as well as scale.js. All other software is linked under its existing license and is unmodified.

The MIT License (MIT)

Copyright (c) 2013 CIO Technologies Inc.

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
