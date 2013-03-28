/* 
 * Java libusb wrapper
 * Copyright (c) 2005-2006 Andreas Schläpfer <spandi at users.sourceforge.net>
 *
 * http://libusbjava.sourceforge.net
 * This library is covered by the LGPL, read LGPL.txt for details.
 */
//package ch.ntb.usb.scale;

import java.text.DecimalFormat;

import ch.ntb.usb.Device;
import ch.ntb.usb.USB;
import ch.ntb.usb.USBException;

/**
 * Class to read a scale and output the weight, unit, and status.
 * 
 * @author William Wynn
 * 
 */
public class ReadScale {

	private static void parseData(byte[] data) {
		if (data[0] == 0x03){ // Is Data Report
			String unit[] = new String[] {"mg", "g", "kg", "carats", "taels", "gr", "dwt", "t", "tn", "ozt", "oz", "lb"};
			System.out.print("                                          " + (char)13); // Ugly fix for pretty formatting
			double weight = 0;
			switch (data[1]){
				case 0x01:
					System.out.print("Scale Fault!" + (char)13);
					System.out.print("");
					break;
				case 0x03: // Scale sliding, wait until it settles
					System.out.print("Scale in motion" + (char)13);
					System.out.print("");
					break;
				case 0x05:
					System.out.print("Weight less than zero!" + (char)13);
					System.out.print("");
					break;
				case 0x06:
					System.out.print("Over weight limit!" + (char)13);
					System.out.print("");
					break;
				case 0x07:
					System.out.print("Recalibration Required!" + (char)13);
					System.out.print("");
					break;
				case 0x08:
					System.out.print("Re-zeroing Required!" + (char)13);
					System.out.print("");
					break;
				case 0x02: // Report zero weight
					System.out.print("Weight: 0 " +
							unit[Integer.parseInt(Integer.toHexString(data[2] & 0xff), 16)-1] + (char)13);
					System.out.print("");
					break;
				case 0x04: // Report Weight
					DecimalFormat df = new DecimalFormat("#.##");
					weight = ((Integer.parseInt(Integer.toHexString(data[4] & 0xff), 16)) +
							((Integer.parseInt(Integer.toHexString(data[5] & 0xff), 16)) << 8)) *
							Math.pow(10, (Integer.parseInt(Integer.toString(data[3]))));
					System.out.print("Weight: " + df.format(weight) + " " +
							unit[Integer.parseInt(Integer.toHexString(data[2] & 0xff), 16)-1] + (char)13);
					System.out.print("");
					break;
			}
		}
	}

	public static void main(String[] args) {
		if (args.length != 2){
			System.out.println("To run: java ReadScale <vid> <pid>\nvid and pid should not contain the HEX indicator: 0x");
			System.exit(0);
		}
		short vid = (short) Integer.parseInt(args[0], 16);
		short pid = (short) Integer.parseInt(args[1], 16);
		// get a device instance with vendor id and product id
		Device dev = USB.getDevice(vid, pid); // Metler Toledo - 0x0EB8, 0xF000
		//Device dev = USB.getDevice((short) 0x1446, (short) 0x6A76); // Stamps.com
		try {
			byte[] readData = new byte[7];

			// open the device with configuration 1, interface 0 and without alt interface
			// this will initialise Libusb
			dev.open(1, 0, -1);
			dev.controlMsg(ch.ntb.usb.USB.REQ_TYPE_DIR_DEVICE_TO_HOST, ch.ntb.usb.USB.REQ_GET_STATUS, 1, 0, readData, readData.length, 2000, false);
			
			// read data from the device
			int j = 1;
			while (j == 1){ // Because eclipse doesn't like: while(true)
				dev.readInterrupt(0x81, readData, readData.length, 2000, false);
				// parse data from the device
				parseData(readData);
				try {
					Thread.sleep(50);
				} catch (InterruptedException e) {}
			}
			// close the device
			dev.close();
		} catch (USBException e) {
			System.out.println("Couldn't connect to scale");
			e.printStackTrace();
		}
	}
}