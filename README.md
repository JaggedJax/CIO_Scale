Java applet for USB scale integration with a web application
============================

CIO Scale Java applet written by: William S. Wynn (CIO Technologies)

CIO Scale is licensed under the MIT X11 License. Libraries included herein are unmodified, listed below, and are covered by their respective license.

* libusb-win32 wrapper (Java): http://libusbjava.sourceforge.net
* libusb-win32: http://sourceforge.net/apps/trac/libusb-win32
* Apache Commons-IO: https://commons.apache.org/proper/commons-io/

ScaleAppletSigned.jar takes two parameters at runtime:

* url - Base url of page. eg: https://cioremotedemo.ciotech.com
* install - Ignored unless this param's value is also 'install'. If so, this forces the installer to run even if a valid scale is detected.

The /src folder contains ScaleApplet.java which is the only custom Java code you will need for the integration.
	
If you import this base folder (not just src) into Eclipse as an existing project, it _Should_ be able to build ScaleApplet.java. Otherwise you might have to link the libraries properly yourself.

I normally build ScaleApplet.java and then open ScaleAppletUnsigned.jar as a zip file and just copy in the new ScaleApplet.class file instead of creating the entire jar.

## Drivers
The applet expects to find libusb-32.exe and libusb-64.exe in /BASE_URL/drivers/usb of the webserver. This can be changed in the java code.
Those two files are the custom driver installers.

folder /drivers/usb contents:

* libusb-32.exe & libusb-64.exe installers
* inf-wizard.exe
	- GUI installer for scale special drivers
	- This program is included in and launched by the installers listed above
* install-filter-win32.exe & install-filter-win64.exe
	- These are the filter drivers that can be used to talk to a usb scale w/o installing the special drivers. I haven't played with this much.
	- Instructions for use are at: http://sourceforge.net/apps/trac/libusb-win32/wiki#Installation
	- The downside of this is you could have to install every time the computer is restarted. The upside is it can be automated more, I think.
* uac-launch.exe
	- A program I modified to launch any program and ask for admin privalages.
	- Parameters passed are the parameters for the file to launch.
	- The program is compiled with Microsoft Visual C++ 2010 Express
	- Project source included at /uac

>uac-launch.exe \<File to launch\> \<Parameters\>

Folders /lib32 and /lib64 contain the raw files that libusb-32.exe and libusb-64.exe actually contain.
The corresponding .iip files are Clickteam Install Creator Pro files used to create/modify the exe installers. They use the /lib32 and /lib64 folders to build the installers.

## Create jar from scratch
    jar cvf jarFile.jar inputfile1.class inputfile2.class folder1 folder2
    
Make sure to add the following line to the jar's manifest or Java will display an extra warning message

    Trusted-Library: true

## Signing
You must use a signed jar file. An unsigned file will not load.

* In command prompt go to folder containing unsigned jar
* Use your own jdk directory in the commands below of course.

* Create Store: (If an error about this alias is thrown, make any new -alias here (Doesn't matter what) and also change it in the next command)

    keytool -genkey -alias signJar -keystore compstore -keypass mypass -dname "CN=William Wynn, OU=CIO Remote, O=CIO Technologies Inc., L=Santa Barbara, ST=CA, C=US" -storepass SuperSecretPassword"

* Sign Jar:

    jarsigner -keystore compstore -storepass SuperSecretPassword -keypass mypass -signedjar ScaleAppletSigned.jar ScaleAppletUnsigned.jar signJar

## Using the Applet
Example code to include in page:

    <script type="text/javascript" src="javascript/scale.js"></script> <!-- File included in project folder -->
    <!-- Must call waituntilok() in body onload(). Can optionally pass setup parameters of: (desired units, element to update, function to call after update) -->
    <body onload="waituntilok('oz', 'weight_oz', 'PostageUpdate');">
    <!-- Alternate parameters below will automatically pause scale and optionally call your own JS function when this happens. -->
    <!--  <body onload="waituntilok('oz', 'weight_oz', 'PostageUpdate', true, 'someOptionalFunc');"> -->
    
    <!-- if applet is stored at <https://cioremotedemo.ciotech.com/java/ScaleAppletSigned.jar> -->
    <object type="application/x-java-applet" name="scaleApplet" id="scaleApplet" width="200" height="40">
        <param name="Codebase" value="java/">
        <param name="legacy_lifecycle" value="true">
        <param name="cache_option" value="Plugin">
        <param name="cache_archive" value="ScaleAppletSigned.jar">
        <param name="code" value="ScaleApplet.class">
        <param name="url" value="https://cioremotedemo.ciotech.com">
        <param name="install" value="">
    </object>
    
    <!-- Call getWeight() from scale.js to pull weight from scale and put it in desired element ID  -->
    <input type="button" onclick="getWeight(); return false;" value="Get Weight">
    
    <! -- We'll put it in this form field -->
    <input type="text" name="weight_oz" id="weight_oz" class="textbox" onchange="PostageUpdate();" size="5" value="0" maxlength="6" />Ounces

The legacy_lifecycle option will keep the applet paused and the JVM running even while not on the page. This makes future runs of the applet instant with no JVM startup or scale re-connection needed. If the applet is truely ended, the destroy() function will close the scale connection automatically.

The file install_scale.php included in this folder is a good sample file that will force a scale install, and show the basics of how to use the applet.

The connection code is really messy right now and I'll try and clean it up when I have time.

## TODO

* Fix 32/64 bit issues
* Automate driver installation more
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
