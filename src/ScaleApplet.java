import java.applet.Applet;
import java.awt.Container;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Label;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.swing.JOptionPane;

import ch.ntb.usb.Device;
import ch.ntb.usb.LibusbJava;
import ch.ntb.usb.USB;
import ch.ntb.usb.USBException;
import ch.ntb.usb.Usb_Bus;
import ch.ntb.usb.Usb_Device;

import org.apache.commons.io.FileUtils;

public class ScaleApplet extends Applet{
	private static final long serialVersionUID = 1L;
//	private Label m_label = new Label("version 11.8.11"); // MM.DD.YY - Date of last update
	private int interface_num = 0;
	
	private Label view = new Label();
	private String unit[];
	private double weight;
	private int unitNumber;
	private String message;
	private short vid;
	private short pid;
	private int config_value;
	private String stringVID = "";
    private String stringPID = "";
	private boolean error;
	private boolean reconnect;
	private byte[] readData;
	private Device dev;
	private String url;
	private String arch = "";
	private boolean try_install;
	private boolean force_install;
	private boolean connectionClosed = false;
	private int connectionAttempts = 0;
	private int maxConnectionAttempts = 10;
	
	public ScaleApplet(){
		
	}

	/**
	 * Initialize ScaleApplet and connect to scale
	 */
	public void init() {
		unit = new String[] {"mg", "g", "kg", "carat", "tael", "gr", "dwt", "t", "tn", "ozt", "oz", "lb"};
		weight = 0;
		unitNumber = 11; // 11 for lb
		message = "Not connected to scale";
		error = true;
		reconnect = false;
		try_install = true;
		force_install = false;
		// data read from the device
		readData = new byte[6];
		url = getParameter("url");
		String temp = getParameter("install");
		if (temp != null && (temp.equals("install") || temp.equals("force"))){
			force_install = true;
			System.out.println("Scale drivers install has been forced.");
		}
		else if (temp != null && temp.equals("no")){
			try_install = false;
		}
		
	    setup(null);
	    get_scale();
	    
		//if (getParameter("debug").equals("Yes"))
		//	add(m_label);
		// Try really, really hard to connect
		try {
			dev = USB.getDevice(vid, pid);
			
			dev.open(config_value, interface_num, -1);
			// Give it some time to open
			int waitTries=0;
			while (!dev.isOpen() && waitTries < 10)
			{
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {}
				waitTries++;
			}
			System.out.println("Device opened after "+(waitTries*100)+"ms");
			
			dev.controlMsg(ch.ntb.usb.USB.REQ_TYPE_DIR_DEVICE_TO_HOST, ch.ntb.usb.USB.REQ_GET_STATUS, 1, 0, readData, readData.length, 2000, true);
			System.out.println("Using interface: "+dev.getInterface());
			error = false;
		} catch (USBException e) {
			System.out.println("Error opening Device: "+e.getMessage());
			try {
				dev.close();
			} catch (USBException e3) {}
			int alt = dev.getAltinterface();
			if (alt == -1){
				alt = 0;
				try {
					Thread.sleep(100);
				} catch (InterruptedException e1) {}
			}
			try{
				//get_scale();
				//dev = USB.getDevice(vid, pid);
				dev.setResetOnFirstOpen(false, 5);
				dev.open(config_value, alt, -1);
				dev.controlMsg(ch.ntb.usb.USB.REQ_TYPE_DIR_DEVICE_TO_HOST, ch.ntb.usb.USB.REQ_GET_STATUS, 1, 0, readData, readData.length, 2000, true);
				System.out.println("Using alternate interface: "+alt);
			} catch (USBException e2) {
				System.out.println("Error weight: "+weight);
				if (!communicate()){
					try {
						dev.close();
					} catch (USBException e3) {}
					System.out.println(e2.getMessage());
					//filter_install();
					try{
						get_scale();
						dev = USB.getDevice(vid, pid);
						dev.open(config_value, interface_num, -1);
						dev.controlMsg(ch.ntb.usb.USB.REQ_TYPE_DIR_DEVICE_TO_HOST, ch.ntb.usb.USB.REQ_GET_STATUS, 1, 0, readData, readData.length, 2000, true);
						System.out.println("Using interface: "+dev.getInterface());
					}catch (Exception e1){
						error = true;
						reconnect = true;
						message = "Couldn't connect to scale";
						view.setText(message);
						//showMessage(message);
					}
				}
				else{
					System.out.println("Second connection attempt successful");
				}
			}
		}
		//add(view);
		communicate();
		System.out.println("First communication attempted");
	}
	
	public void closeConnection(){
		try {
			dev.close();
			System.out.println("Connection to scale closed");
			connectionClosed = true;
		} catch (USBException e) {
			System.out.println("Couldn't close connection");
		}
	}
	
	/**
	 * Attempt to reconnect to same scale after losing connection.
	 * Tries same VID and PID as before.
	 */
	public void reconnect() {
		if (!connectionClosed && connectionAttempts < maxConnectionAttempts){
			// Attempt reconnect
			System.out.println("Attempting reconnection to scale. Attempt "+connectionAttempts);
			try {
				dev.reset();
			} catch (USBException e3) {}
			try{
				try{
					if (dev.isOpen())
						dev.close();
				} catch (Exception ee){
					System.out.println("Couldn't close device");
				}
				
				dev.open(config_value, interface_num, -1);
				dev.controlMsg(ch.ntb.usb.USB.REQ_TYPE_DIR_DEVICE_TO_HOST, ch.ntb.usb.USB.REQ_GET_STATUS, 1, 0, readData, readData.length, 2000, false);
				System.out.println("Using interface: "+dev.getInterface());
			}catch(USBException e){
				System.out.println("Error reconnecting to scale: "+e.getMessage());
				int alt = dev.getAltinterface();
				if (alt == -1){
					alt = 0;
					try {
						Thread.sleep(100);
					} catch (InterruptedException e1) {}
				}
				try{
					dev = USB.getDevice(vid, pid);
					dev.open(config_value, alt, -1);
					dev.controlMsg(ch.ntb.usb.USB.REQ_TYPE_DIR_DEVICE_TO_HOST, ch.ntb.usb.USB.REQ_GET_STATUS, 1, 0, readData, readData.length, 2000, false);
					System.out.println("Using alternate interface "+alt);
				} catch (USBException e2) {
					System.out.println("Error reconnecting to scale: "+e2.getMessage());
				}
			}
			connectionAttempts++;
		}
		else{
			System.out.println("Max reconnection attempts already made. Could not connect.");
		}
		
	}

	/**
	 * Paint canvas
	 * 
	 * @param g Graphics object
	 */
	public void paint(Graphics g) {
	}
	
	/**
	 * Look for a compatable USB device and the the VID and PID
	 * 
	 * @return Success on finding a scale
	 */
	private boolean get_scale(){
		try{
			LibusbJava.usb_init();
		}catch (UnsatisfiedLinkError e){
			System.out.println("Path: "+System.getProperty("java.library.path"));
			System.out.println("Library error: "+e.getMessage());
			int pos_32 = e.getMessage().indexOf("32-bit");
			int pos_64 = e.getMessage().indexOf("64-bit");
			// If error message is what we expect
			if (pos_32 > -1 && pos_64 > -1){
				// "Can't load 32-bit .dll on 64-bit platform" error message 
				if (pos_32 < pos_64)
					setup("64");
				else
					setup("32");
			}
			// Otherwise try the other library
			else{
				if (arch.equals("32"))
					setup("64");
				else
					setup("32");
			}
			try{
				LibusbJava.usb_init();
			}catch (Exception e2){
				showMessage("Can't install scale drivers on this architecture.\n\nReason: "+e2.getMessage(), "Error connecting to scale!");
				System.exit(0);
			}
		}
	    LibusbJava.usb_find_busses();
	    LibusbJava.usb_find_devices();
	    Usb_Bus bus = LibusbJava.usb_get_busses();
	    Usb_Device device = bus.getDevices();
	    
	    if (device != null){
	    	String temp = device.toString();
	    	config_value = device.getConfig()[0].getBConfigurationValue();
	    	System.out.println("Configuration value: "+config_value);
	    	stringVID = temp.substring(temp.indexOf("0x")+2, temp.lastIndexOf("-"));
	    	if (stringVID.length() < 4){ // Prepend any missing zeros
	    		for (int i=0; i<stringVID.length(); i++){
	    			stringVID = "0"+stringVID;
	    		}
	    	}
	    	
	    	stringPID = temp.substring(temp.lastIndexOf("0x")+2);
	    	if (stringPID.length() < 4){ // Prepend any missing zeros
	    		for (int i=0; i<stringPID.length(); i++){
	    			stringPID = "0"+stringPID;
	    		}
	    	}
	    	System.out.println("0x"+stringVID+" - 0x"+stringPID);
			vid = (short) Integer.parseInt(stringVID, 16);
			pid = (short) Integer.parseInt(stringPID, 16);
	    	return true;
	    }
	    else
	    	return false;
	}
	
	/**
	 * Setup drivers for Scale applet
	 */
	private void setup(String forceArch){
		String sys32 = System.getenv("WINDIR") + "\\system32\\";
		if (forceArch != null){
			arch = forceArch;
			System.out.print("Forced ");
		}
		else if (arch.equals("") && System.getenv("WINDIR") != null){
			arch = getArchitecture();
			if(arch.contains((CharSequence)"64")){
				arch = "64";
			}
			else{
				arch = "32";
			}
			System.out.print("Detected ");
		}
		if(System.getenv("WINDIR") == null){
			showMessage("Looks like you're on Linux or Mac.\nThe CIO Remote Scale "
					+"application only supports Windows at this time.", "Error");
			System.exit(0);
		}
		System.out.println(arch+" bit OS");
		String tempDir = System.getProperty("java.io.tmpdir");
		if (tempDir.charAt(tempDir.length()-1) != '/' && tempDir.charAt(tempDir.length()-1) != '\\'){
			tempDir += File.separator;
		}
		
		String UAC = "CIORemote_uac-"+generateString(new Random(System.currentTimeMillis()), 4)+".exe";
		String Installer = "CIORemote_ins-"+generateString(new Random(System.currentTimeMillis()), 4)+".exe";

		System.out.println("Checking for Drivers");
		File file1 = new File(sys32 + "LibusbJava.dll");
		File fileUAC = new File(tempDir + UAC);
		File fileInstall = new File(tempDir + Installer);
		if ((!file1.exists() && try_install) || force_install){
			try {
				System.out.println("Drivers Missing. Copying over files.");
				FileUtils.copyURLToFile(new URL(url+"/drivers/usb/uac-launch.exe"), fileUAC);
				FileUtils.copyURLToFile(new URL(url+"/drivers/usb/libusb-"+arch+".exe"), fileInstall);
				//new ProcessBuilder(tempDir + "libusb.exe").start();
				final File uacLaunch = new File(tempDir + UAC);
			/*
				File workingDir;

				try {
					workingDir = new File(sys32);
				} catch ( Exception e ) {
					workingDir = new File(".");
				}
			*/
				if (forceArch != null){
					Object[] options = {"Install Scale", "Cancel"};
					String message = "CIO Remote will now install the required scale drivers.\nAdministrator privileges are required!\n\n"
							+"If you are running XP, you must un-select the box that says:\n"
							+"\"Protect my computer and data from unauthorized program activity\"\n\n"
							+"The installer will launch when you close this window.\n\n"
							+"When asked to save a file, save it in your home directory and then select \"Install Now\"";
					int choice = JOptionPane.showOptionDialog(this, message, "CIO Remote - Installing Drivers", JOptionPane.YES_NO_OPTION,
							JOptionPane.INFORMATION_MESSAGE, null, options, options[0]);
					if (choice != 0){
						System.out.println("Install canceled");
						return;
					}
				}
				
				try {
					// Build the command list to be given to the ProcessBuilder
					final List<String> cmdArgs = new ArrayList<String>();
					cmdArgs.add(uacLaunch.getAbsolutePath());
					//cmdArgs.add(workingDir.getAbsolutePath());
					cmdArgs.add(tempDir + Installer);

					// Create a process, and start it.
					final ProcessBuilder p = new ProcessBuilder(cmdArgs);
					p.directory(new File("."));
					System.out.println("Starting Install process");
					Process proc = p.start();
					 //Wait to get exit value
			        try {
			            proc.waitFor();
			            System.out.println("Install finished: "+proc.exitValue());
			        } catch (InterruptedException e) {
			        	showMessage("You must reload this page after installing drivers", "Error");
			        }
				} catch (Throwable t) {
					showMessage("Couldn't install drivers.\n\n"+t.getMessage(), "Error");
				}
			//	FileUtils.copyURLToFile(new URL(url+"/drivers/usb/"+arch+"/LibusbJava.dll"), file1);
			} catch (Exception e) {
				showMessage("Couldn't install drivers.\n\n"+e.getMessage(), "Error");
			}
		}
		else if(!file1.exists() && !try_install){
			System.out.println("Drivers missing, but install was skipped by request.");
		}
		else{
			System.out.println("Drivers already installed");
		}
	}
	
	/**
	 * Install filter driver if we still can not connect to the scale
	 */
	private void filter_install(){
		System.out.println("Failed to connect to scale. Installing filter driver.");
		if (arch.equals("")){
			arch = getArchitecture();
			if(arch.contains((CharSequence)"64"))
				arch = "64";
			else
				arch = "32";
			System.out.print("Detected ");
		}
		System.out.println(arch+" bit OS");
		String tempDir = System.getProperty("java.io.tmpdir");
		String UAC = "CIORemote_uac-"+generateString(new Random(System.currentTimeMillis()), 4)+".exe";
		String filter = "CIORemote_filter-"+generateString(new Random(System.currentTimeMillis()), 4)+".exe";
		File fileUAC = new File(tempDir + UAC);
		File fileFilter = new File(tempDir + filter);
		
		showMessage("Failed connecting to scale. Trying alternate drivers.", "CIO Remote - Installing Drivers");
		try {
			FileUtils.copyURLToFile(new URL(url+"/drivers/usb/uac-launch.exe"), fileUAC);
			FileUtils.copyURLToFile(new URL(url+"/drivers/usb/install-filter-win"+arch+".exe"), fileFilter);
			final File uacLaunch = new File(tempDir + UAC);
			
			// Build the command list to be given to the ProcessBuilder
			final List<String> cmdArgs = new ArrayList<String>();
			cmdArgs.add(uacLaunch.getAbsolutePath());
			cmdArgs.add(tempDir + filter);
			cmdArgs.add("-i \"--device=USB\\VID_"+vid+"&PID_"+pid+"\"");

			// Create a process, and start it.
			final ProcessBuilder p = new ProcessBuilder(cmdArgs);
			p.directory(new File("."));
			System.out.println("Starting Install process");
			Process proc = p.start();
			 //Wait to get exit value
	        try {
	            proc.waitFor();
	            System.out.println("Done: "+proc.exitValue());
	        } catch (InterruptedException e) {
	        	showMessage("You must reload this page after installing drivers", "Error");
	        }
			
		} catch (Exception e) {
			showMessage("Couldn't install drivers.\n\n"+e.getMessage(), "Error");
		}
	}
	
	/**
	 * Get the OS architecture and not the JVN architecture
	 * 
	 * @return Architecture type
	 */
	private String getArchitecture() {
		//String arch = System.getenv("PROCESSOR_ARCHITEW6432");
		//String arch = System.getProperty("sun.arch.data.model");
		String arch = System.getProperty("os.arch");
		//System.out.println("TEST ARCH: "+arch);
		if(arch != null && !arch.isEmpty()) {
			return arch;
		}
		return System.getenv("PROCESSOR_ARCHITECTURE");
	}

	
	/**
	 * Tell applet to read data from scale
	 */
	public boolean communicate(){
		boolean res = true;
		reconnect = false;
		if (!connectionClosed){
			for (int i=0; i<4; i++){
				try{
					dev.readInterrupt(0x81, readData, readData.length, 500, false);
					res = true;
				} catch (USBException e) {
					error = true;
					weight = -1;
					if (connectionAttempts < maxConnectionAttempts){
						message = "Lost connection to scale, reconnect and try again.";
						reconnect = true;
					}
					else{
						message = "Could not reconnect to scale. Please restart application.";
					}
					view.setText(message);
					//showMessage(message);
					res = false;
					System.out.println("Error: "+e.getMessage());
				}
				// Format data from the device
				readScale(readData);
				view.setText("Weight: "+weight+" "+unit[unitNumber]);
				try {
					Thread.sleep(10);
				} catch (InterruptedException e) {}
			}
			if (weight != -1){
				System.out.println("Weight: "+weight+" "+unit[unitNumber]);
			}
			else{
				System.out.println(message);
			}
			if (reconnect){
				reconnect();
			}
		}
		return res;
	}
	
	/**
	 * Tell applet to read data from scale forever
	 */
	public void loopApplet(){
		while (true){
			reconnect = false;
			try{
				dev.readInterrupt(0x81, readData, readData.length, 2000, false);
			} catch (USBException e) {
				error = true;
				reconnect = true;
				message = "Lost connection to scale";
				view.setText(message);
				//showMessage(message);
				System.out.println(e.getMessage());
			}
			// Format data from the device
			readScale(readData);
			view.setText("Weight: "+weight+" "+unit[unitNumber]);
			if (weight != -1)
				System.out.println("Weight: "+weight+" "+unit[unitNumber]);
			else
				System.out.println(message);
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {}
		}
	}
	
	/**
	 * Get Weight
	 * @return The last weight the scale showed in its native units. -1 if error. Call getMessage() to get error.
	 */
	public double getWeight(){
		communicate();
		if (!error)
			return weight;
		else
			return -1;
	}
	
	/**
	 * Get the weight in a specific unit. -1 if error. Call getMessage() to get error.
	 * @param toUnit
	 * @return The last weight the scale showed in desired units. -1 if error. Call getMessage() to get error.
	 */
	public double getWeight(String toUnit){
		communicate();
		// Return if already correct weight
		if (toUnit.equals(unit[unitNumber]))
			return weight;
		
		double newWeight = weight;
		if (!error){
			// Convert to lbs
				if (unit[unitNumber].equals("lb")) newWeight = weight;
				else if (unit[unitNumber].equals("mg")) newWeight = weight*(2.2046*Math.pow(10, -6));
				else if (unit[unitNumber].equals("g")) newWeight = weight*(2.2046*Math.pow(10, -3));
				else if (unit[unitNumber].equals("kg")) newWeight = weight*2.2046;
				else if (unit[unitNumber].equals("oz")) newWeight = weight*0.0625;
				else if (unit[unitNumber].equals("carat")) newWeight = weight*(4.4092*Math.pow(10, -4));
				else if (unit[unitNumber].equals("tael")) newWeight = weight*0.11023;
				else if (unit[unitNumber].equals("gr")) newWeight = weight*(1.429*Math.pow(10, -4));
				else if (unit[unitNumber].equals("dwt")) newWeight = weight*(3.4286*Math.pow(10, -3));
				else if (unit[unitNumber].equals("t")) newWeight = weight*2204.623;
				else if (unit[unitNumber].equals("tn")) newWeight = weight*2000;
				else if (unit[unitNumber].equals("ozt")) newWeight = weight*0.06857;
			// Convert from lbs to desired unit
				if (toUnit.equals("lb")){}
				else if (toUnit.equals("mg")) newWeight = newWeight*453592;
				else if (toUnit.equals("g")) newWeight = newWeight*453.6;
				else if (toUnit.equals("kg")) newWeight = newWeight*0.4536;
				else if (toUnit.equals("oz")) newWeight = newWeight*16;
				else if (toUnit.equals("carat")) newWeight = newWeight*2268;
				else if (toUnit.equals("tael")) newWeight = newWeight*9.072;
				else if (toUnit.equals("gr")) newWeight = newWeight*7000;
				else if (toUnit.equals("dwt")) newWeight = newWeight*291.7;
				else if (toUnit.equals("t")) newWeight = newWeight*(4.536*Math.pow(10, -4));
				else if (toUnit.equals("tn")) newWeight = newWeight/2000;
				else if (toUnit.equals("ozt")) newWeight = newWeight*14.58;
			return newWeight;
		}
		else
			return -1;
	}
	
	/**
	 * Get the units scale is measuring in.
	 * @return
	 */
	public String getUnit(){
		return unit[unitNumber];
	}
	
	/**
	 * Get the latest message from the scale.
	 * @return Latest messsage. Empty string if none.
	 */
	public String getMessage(){
		return message;
	}
	
	/**
	 * Close scale connection before applet exits.
	 */
	public void destroy(){
		closeConnection();
		super.destroy();
	}

	private void readScale(byte[] data) {
		if (data[0] == 0x03){ // Is Data Report
			switch (data[1]){
				case 0x01:
					error = true;
					message = "Scale Fault!";
					break;
				case 0x03: // Scale sliding, wait until it settles
					error = true;
					message = "Scale in motion";
					break;
				case 0x05:
					error = true;
					message = "Weight less than zero!";
					break;
				case 0x06:
					error = true;
					message = "Over weight limit!";
					break;
				case 0x07:
					error = true;
					message = "Recalibration Required!";
					break;
				case 0x08:
					error = true;
					message = "Re-zeroing Required!";
					break;
				case 0x02: // Report zero weight
					error = false;
					message = "";
					weight = 0;
					unitNumber = Integer.parseInt(Integer.toHexString(data[2] & 0xff), 16)-1;
					break;
				case 0x04: // Report Weight
					error = false;
					message = "";
					weight = ((Integer.parseInt(Integer.toHexString(data[4] & 0xff), 16)) +
						((Integer.parseInt(Integer.toHexString(data[5] & 0xff), 16)) << 8)) *
						Math.pow(10, (Integer.parseInt(Integer.toString(data[3]))));
					unitNumber = Integer.parseInt(Integer.toHexString(data[2] & 0xff), 16)-1;
					break;
			}
		}
	}
	
	private static String generateString(Random rng, int length)
	{
		String characters = "1234567890abcdefghijklmnopqrstuzwxyzABCDEFGHIJKLMNOPQRSTUZWXYZ";
	    char[] text = new char[length];
	    for (int i = 0; i < length; i++)
	    {
	        text[i] = characters.charAt(rng.nextInt(characters.length()));
	    }
	    return new String(text);
	}
	
	private Frame getParentFrame() {
		Container c = this;
		while (c != null) {
			if (c instanceof Frame)
				return (Frame) c;
			c = c.getParent();
		}
		return (Frame) null;
	}

	private void showMessage(String message, String title) {
		System.out.println(message);
		Frame frame = getParentFrame();
		JOptionPane.showMessageDialog(frame, message, title,
				JOptionPane.WARNING_MESSAGE);
	}
}
