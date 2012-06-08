/* -*- mode: java; c-basic-offset: 2; indent-tabs-mode: nil -*- */

/*
  Part of the Processing project - http://processing.org

  Copyright (c) 2005-06 Ben Fry and Casey Reas

  This program is free software; you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation; either version 2 of the License, or
  (at your option) any later version.

  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.

  You should have received a copy of the GNU General Public License
  along with this program; if not, write to the Free Software Foundation,
  Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package processing.app;

import java.io.*;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Random;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.WindowConstants;


import processing.core.PApplet;
import static processing.app.I18n._;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
/**
 * Threaded class to check for updates in the background.
 * <P>
 * This is the class that handles the mind control and stuff for
 * spying on our users and stealing their personal information.
 * A random ID number is generated for each user, and hits the server
 * to check for updates. Also included is the operating system and
 * its version and the version of Java being used to run Processing.
 * <P>
 * The ID number also helps provide us a general idea of how many
 * people are using Processing, which helps us when writing grant
 * proposals and that kind of thing so that we can keep Processing free.
 */
public class UpdateCheck implements Runnable {
	Base base;

	static final long ONE_DAY = 24 * 60 * 60 * 1000;

	JPanel panel = new JPanel();
	JProgressBar progressBar;
	JDialog dialog;
	
	public UpdateCheck(Base base) {
		Thread thread = new Thread(this);
		this.base = base;
		thread.start();
	}


	public void run() {
		
		try {
			String lastString = Preferences.get("update.last");
			long now = System.currentTimeMillis();
			if (lastString != null) {
				long when = Long.parseLong(lastString);
				if (now - when < ONE_DAY) {
					// don't annoy the shit outta people
					return;
				}
			}
			Preferences.set("update.last", String.valueOf(now));

			String prompt =
					_("A new version of the Reef Angel Libraries is available.\nYour current version is " + readVer() + "\n" +
							"Newer version is " + MyVer() + "\n\n" +
							"Would you like to update now?");

			if (base.activeEditor != null) {
				if (!readVer().equals(MyVer())) {
					Object[] options = { _("Yes"), _("No") };
					int result = JOptionPane.showOptionDialog(base.activeEditor,
							prompt,
							_("Update"),
							JOptionPane.YES_NO_OPTION,
							JOptionPane.QUESTION_MESSAGE,
							null,
							options,
							options[0]);
					if (result == JOptionPane.YES_OPTION) {
						if (base.activeEditor != null) {
							panel.add(new JLabel ("Progress:"));
							progressBar = new JProgressBar(0, 100);
							progressBar.setValue(0);
							progressBar.setStringPainted(true);	
							panel.add(progressBar);
							Thread t = new Thread(new DownloadFile());
							t.start();
							JOptionPane pane = new JOptionPane(panel);
							pane.setOptions(new Object[] {"Cancel"});
							dialog = pane.createDialog(base.activeEditor,"Reef Angel Update Tool"); 
							dialog.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE); 
							dialog.setVisible(true);

						}	
					}
				}
			}
		} catch (Exception e) {
			//e.printStackTrace();
			//System.err.println("Error while trying to check for an update.");
		}
	}


	protected String readVer() throws Exception {
		// generate a random id in case none exists yet
		Random r = new Random();
		long id = r.nextLong();

		String idString = Preferences.get("update.id");
		if (idString != null) {
			id = Long.parseLong(idString);
		} else {
			Preferences.set("update.id", String.valueOf(id));
		}

		String info;
		info = URLEncoder.encode(id + "\t" +
				PApplet.nf(Base.REVISION, 4) + "\t" +
				System.getProperty("java.version") + "\t" +
				System.getProperty("java.vendor") + "\t" +
				System.getProperty("os.name") + "\t" +
				System.getProperty("os.version") + "\t" +
				System.getProperty("os.arch"), "UTF-8");

		String filename = "https://raw.github.com/reefangel/Libraries/master/ReefAngel/ReefAngel.h?" + info;
		URL url = new URL(filename);
		InputStream stream = url.openStream();
		InputStreamReader isr = new InputStreamReader(stream);
		BufferedReader reader = new BufferedReader(isr);

		String strLine;
		String strversion="";
		while ((strLine = reader.readLine()) != null)   {
			if (strLine.indexOf("ReefAngel_Version ")>0)
			{
				strversion=strLine.substring(strLine.indexOf("ReefAngel_Version ")+19);
				strversion=strversion.replace("\"", "");
			}
		}
		return strversion;
	}

	protected String MyVer() throws Exception {
		FileInputStream fstream = new FileInputStream(Base.getSketchbookFolder().getPath() + "/libraries/ReefAngel/ReefAngel.h");
		DataInputStream in = new DataInputStream(fstream);
		BufferedReader br = new BufferedReader(new InputStreamReader(in));
		String strLine;
		String strversion="";
		while ((strLine = br.readLine()) != null)   {
			if (strLine.indexOf("ReefAngel_Version ")>0)
			{
				strversion=strLine.substring(strLine.indexOf("ReefAngel_Version ")+19);
				strversion=strversion.replace("\"", "");
			}
		}
		in.close();
		return strversion;
	}
	
	class Filename {
		private String fullPath;
		private char pathSeparator, extensionSeparator;

		public Filename(String str, char sep, char ext) {
			fullPath = str;
			pathSeparator = sep;
			extensionSeparator = ext;
		}

		public String extension() {
			int dot = fullPath.lastIndexOf(extensionSeparator);
			return fullPath.substring(dot + 1);
		}

		public String filename() { // gets filename without extension
			int dot = fullPath.lastIndexOf(extensionSeparator);
			int sep = fullPath.lastIndexOf(pathSeparator);
			return fullPath.substring(sep + 1, dot);
		}

		public String path() {
			int sep = fullPath.lastIndexOf(pathSeparator);
			return fullPath.substring(0, sep);
		}
	}

	class DownloadFile implements Runnable
	{
		String[] SourceFiles=new String[0];
		String[] DestFiles=new String[0];
		
		public void run(){
	        AddFile("https://raw.github.com/reefangel/libraries/master/DS1307RTC/DS1307RTC.cpp",Base.getSketchbookFolder().getPath() + "/libraries/DS1307RTC/DS1307RTC.cpp");
	        AddFile("https://raw.github.com/reefangel/libraries/master/DS1307RTC/DS1307RTC.h",Base.getSketchbookFolder().getPath() + "/libraries/DS1307RTC/DS1307RTC.h");
	        AddFile("https://raw.github.com/reefangel/libraries/master/DS1307RTC/keywords.txt",Base.getSketchbookFolder().getPath() + "/libraries/DS1307RTC/keywords.txt");
	        AddFile("https://raw.github.com/reefangel/libraries/master/DS1307RTC/readme.txt",Base.getSketchbookFolder().getPath() + "/libraries/DS1307RTC/readme.txt");
	        AddFile("https://raw.github.com/reefangel/libraries/master/NokiaLCD/NokiaLCD.cpp",Base.getSketchbookFolder().getPath() + "/libraries/NokiaLCD/NokiaLCD.cpp");
	        AddFile("https://raw.github.com/reefangel/libraries/master/NokiaLCD/NokiaLCD.h",Base.getSketchbookFolder().getPath() + "/libraries/NokiaLCD/NokiaLCD.h");
	        AddFile("https://raw.github.com/reefangel/libraries/master/NokiaLCD/README",Base.getSketchbookFolder().getPath() + "/libraries/NokiaLCD/README");
	        AddFile("https://raw.github.com/reefangel/libraries/master/OneWire/OneWire.cpp",Base.getSketchbookFolder().getPath() + "/libraries/OneWire/OneWire.cpp");
	        AddFile("https://raw.github.com/reefangel/libraries/master/OneWire/OneWire.h",Base.getSketchbookFolder().getPath() + "/libraries/OneWire/OneWire.h");
	        AddFile("https://raw.github.com/reefangel/libraries/master/OneWire/keywords.txt",Base.getSketchbookFolder().getPath() + "/libraries/OneWire/keywords.txt");
	        AddFile("https://raw.github.com/reefangel/libraries/master/Phillips6610LCD/Phillips6610LCD.cpp",Base.getSketchbookFolder().getPath() + "/libraries/Phillips6610LCD/Phillips6610LCD.cpp");
	        AddFile("https://raw.github.com/reefangel/libraries/master/Phillips6610LCD/Phillips6610LCD.h",Base.getSketchbookFolder().getPath() + "/libraries/Phillips6610LCD/Phillips6610LCD.h");
	        AddFile("https://raw.github.com/reefangel/libraries/master/Phillips6610LCD/Phillips6610LCD.o",Base.getSketchbookFolder().getPath() + "/libraries/Phillips6610LCD/Phillips6610LCD.o");
	        AddFile("https://raw.github.com/reefangel/libraries/master/Phillips6610LCDInv/Phillips6610LCDInv.cpp",Base.getSketchbookFolder().getPath() + "/libraries/Phillips6610LCDInv/Phillips6610LCDInv.cpp");
	        AddFile("https://raw.github.com/reefangel/libraries/master/Phillips6610LCDInv/Phillips6610LCDInv.h",Base.getSketchbookFolder().getPath() + "/libraries/Phillips6610LCDInv/Phillips6610LCDInv.h");
	        AddFile("https://raw.github.com/reefangel/libraries/master/Time/Time.cpp",Base.getSketchbookFolder().getPath() + "/libraries/Time/Time.cpp");
	        AddFile("https://raw.github.com/reefangel/libraries/master/Time/Time.h",Base.getSketchbookFolder().getPath() + "/libraries/Time/Time.h");
	        AddFile("https://raw.github.com/reefangel/libraries/master/Time/DateStrings.cpp",Base.getSketchbookFolder().getPath() + "/libraries/Time/DateStrings.cpp");
	        AddFile("https://raw.github.com/reefangel/libraries/master/Time/keywords.txt",Base.getSketchbookFolder().getPath() + "/libraries/Time/keywords.txt");
	        AddFile("https://raw.github.com/reefangel/libraries/master/Time/Readme.txt",Base.getSketchbookFolder().getPath() + "/libraries/Time/Readme.txt");
	        AddFile("https://raw.github.com/reefangel/libraries/master/TimeAlarms/TimeAlarms.cpp",Base.getSketchbookFolder().getPath() + "/libraries/TimeAlarms/TimeAlarms.cpp");
	        AddFile("https://raw.github.com/reefangel/libraries/master/TimeAlarms/TimeAlarms.h",Base.getSketchbookFolder().getPath() + "/libraries/TimeAlarms/TimeAlarms.h");
	        AddFile("https://raw.github.com/reefangel/libraries/master/TimeAlarms/keywords.txt",Base.getSketchbookFolder().getPath() + "/libraries/TimeAlarms/keywords.txt");
	        AddFile("https://raw.github.com/reefangel/libraries/master/TimeAlarms/readme.txt",Base.getSketchbookFolder().getPath() + "/libraries/TimeAlarms/readme.txt");
	        AddFile("https://raw.github.com/reefangel/libraries/master/README.md",Base.getSketchbookFolder().getPath() + "/libraries/README.md");
	        AddFile("https://raw.github.com/reefangel/libraries/master/AI/AI.cpp",Base.getSketchbookFolder().getPath() + "/libraries/AI/AI.cpp");
	        AddFile("https://raw.github.com/reefangel/libraries/master/AI/AI.h",Base.getSketchbookFolder().getPath() + "/libraries/AI/AI.h");
	        AddFile("https://raw.github.com/reefangel/libraries/master/Globals/Globals.cpp",Base.getSketchbookFolder().getPath() + "/libraries/Globals/Globals.cpp");
	        AddFile("https://raw.github.com/reefangel/libraries/master/Globals/Globals.h",Base.getSketchbookFolder().getPath() + "/libraries/Globals/Globals.h");
	        AddFile("https://raw.github.com/reefangel/libraries/master/InternalEEPROM/InternalEEPROM.cpp",Base.getSketchbookFolder().getPath() + "/libraries/InternalEEPROM/InternalEEPROM.cpp");
	        AddFile("https://raw.github.com/reefangel/libraries/master/InternalEEPROM/InternalEEPROM.h",Base.getSketchbookFolder().getPath() + "/libraries/InternalEEPROM/InternalEEPROM.h");
	        AddFile("https://raw.github.com/reefangel/libraries/master/IO/IO.cpp",Base.getSketchbookFolder().getPath() + "/libraries/IO/IO.cpp");
	        AddFile("https://raw.github.com/reefangel/libraries/master/IO/IO.h",Base.getSketchbookFolder().getPath() + "/libraries/IO/IO.h");
	        AddFile("https://raw.github.com/reefangel/libraries/master/LED/LED.cpp",Base.getSketchbookFolder().getPath() + "/libraries/LED/LED.cpp");
	        AddFile("https://raw.github.com/reefangel/libraries/master/LED/LED.h",Base.getSketchbookFolder().getPath() + "/libraries/LED/LED.h");
	        AddFile("https://raw.github.com/reefangel/libraries/master/Memory/Memory.cpp",Base.getSketchbookFolder().getPath() + "/libraries/Memory/Memory.cpp");
	        AddFile("https://raw.github.com/reefangel/libraries/master/Memory/Memory.h",Base.getSketchbookFolder().getPath() + "/libraries/Memory/Memory.h");
	        AddFile("https://raw.github.com/reefangel/libraries/master/ORP/ORP.cpp",Base.getSketchbookFolder().getPath() + "/libraries/ORP/ORP.cpp");
	        AddFile("https://raw.github.com/reefangel/libraries/master/ORP/ORP.h",Base.getSketchbookFolder().getPath() + "/libraries/ORP/ORP.h");
	        AddFile("https://raw.github.com/reefangel/libraries/master/RA_ATO/RA_ATO.cpp",Base.getSketchbookFolder().getPath() + "/libraries/RA_ATO/RA_ATO.cpp");
	        AddFile("https://raw.github.com/reefangel/libraries/master/RA_ATO/RA_ATO.h",Base.getSketchbookFolder().getPath() + "/libraries/RA_ATO/RA_ATO.h");
	        AddFile("https://raw.github.com/reefangel/libraries/master/RA_Colors/RA_Colors.h",Base.getSketchbookFolder().getPath() + "/libraries/RA_Colors/RA_Colors.h");
	        AddFile("https://raw.github.com/reefangel/libraries/master/RA_CustomColors/RA_CustomColors.h",Base.getSketchbookFolder().getPath() + "/libraries/RA_CustomColors/RA_CustomColors.h");
	        AddFile("https://raw.github.com/reefangel/libraries/master/RA_Joystick/RA_Joystick.cpp",Base.getSketchbookFolder().getPath() + "/libraries/RA_Joystick/RA_Joystick.cpp");
	        AddFile("https://raw.github.com/reefangel/libraries/master/RA_Joystick/RA_Joystick.h",Base.getSketchbookFolder().getPath() + "/libraries/RA_Joystick/RA_Joystick.h");
	        AddFile("https://raw.github.com/reefangel/libraries/master/RA_NokiaLCD/README",Base.getSketchbookFolder().getPath() + "/libraries/RA_NokiaLCD/README");
	        AddFile("https://raw.github.com/reefangel/libraries/master/RA_NokiaLCD/RA_NokiaLCD.cpp",Base.getSketchbookFolder().getPath() + "/libraries/RA_NokiaLCD/RA_NokiaLCD.cpp");
	        AddFile("https://raw.github.com/reefangel/libraries/master/RA_NokiaLCD/RA_NokiaLCD.h",Base.getSketchbookFolder().getPath() + "/libraries/RA_NokiaLCD/RA_NokiaLCD.h");
	        AddFile("https://raw.github.com/reefangel/libraries/master/RA_PWM/RA_PWM.cpp",Base.getSketchbookFolder().getPath() + "/libraries/RA_PWM/RA_PWM.cpp");
	        AddFile("https://raw.github.com/reefangel/libraries/master/RA_PWM/RA_PWM.h",Base.getSketchbookFolder().getPath() + "/libraries/RA_PWM/RA_PWM.h");
	        AddFile("https://raw.github.com/reefangel/libraries/master/RA_TempSensor/RA_TempSensor.cpp",Base.getSketchbookFolder().getPath() + "/libraries/RA_TempSensor/RA_TempSensor.cpp");
	        AddFile("https://raw.github.com/reefangel/libraries/master/RA_TempSensor/RA_TempSensor.h",Base.getSketchbookFolder().getPath() + "/libraries/RA_TempSensor/RA_TempSensor.h");
	        AddFile("https://raw.github.com/reefangel/libraries/master/RA_Wifi/RA_Wifi.cpp",Base.getSketchbookFolder().getPath() + "/libraries/RA_Wifi/RA_Wifi.cpp");
	        AddFile("https://raw.github.com/reefangel/libraries/master/RA_Wifi/RA_Wifi.h",Base.getSketchbookFolder().getPath() + "/libraries/RA_Wifi/RA_Wifi.h");
	        AddFile("https://raw.github.com/reefangel/libraries/master/ReefAngel/ReefAngel.cpp",Base.getSketchbookFolder().getPath() + "/libraries/ReefAngel/ReefAngel.cpp");
	        AddFile("https://raw.github.com/reefangel/libraries/master/ReefAngel/ReefAngel.h",Base.getSketchbookFolder().getPath() + "/libraries/ReefAngel/ReefAngel.h");
	        AddFile("https://raw.github.com/reefangel/libraries/master/ReefAngel/README",Base.getSketchbookFolder().getPath() + "/libraries/ReefAngel/README");
	        AddFile("https://raw.github.com/reefangel/libraries/master/ReefAngel/keywords.txt",Base.getSketchbookFolder().getPath() + "/libraries/ReefAngel/keywords.txt");
	        AddFile("https://raw.github.com/reefangel/libraries/master/ReefAngel_Features/ReefAngel_Features.h",Base.getSketchbookFolder().getPath() + "/libraries/ReefAngel_Features/ReefAngel_Features.h");
	        AddFile("https://raw.github.com/reefangel/libraries/master/ReefTouch/Arial12.h",Base.getSketchbookFolder().getPath() + "/libraries/ReefTouch/Arial12.h");
	        AddFile("https://raw.github.com/reefangel/libraries/master/ReefTouch/ArialBold20.h",Base.getSketchbookFolder().getPath() + "/libraries/ReefTouch/ArialBold20.h");
	        AddFile("https://raw.github.com/reefangel/libraries/master/ReefTouch/ArialBold24.h",Base.getSketchbookFolder().getPath() + "/libraries/ReefTouch/ArialBold24.h");
	        AddFile("https://raw.github.com/reefangel/libraries/master/ReefTouch/f8x8.h",Base.getSketchbookFolder().getPath() + "/libraries/ReefTouch/f8x8.h");
	        AddFile("https://raw.github.com/reefangel/libraries/master/ReefTouch/f12x12.h",Base.getSketchbookFolder().getPath() + "/libraries/ReefTouch/f12x12.h");
	        AddFile("https://raw.github.com/reefangel/libraries/master/ReefTouch/f15x22.h",Base.getSketchbookFolder().getPath() + "/libraries/ReefTouch/f15x22.h");
	        AddFile("https://raw.github.com/reefangel/libraries/master/ReefTouch/Fat32.cpp",Base.getSketchbookFolder().getPath() + "/libraries/ReefTouch/Fat32.cpp");
	        AddFile("https://raw.github.com/reefangel/libraries/master/ReefTouch/Fat32.h",Base.getSketchbookFolder().getPath() + "/libraries/ReefTouch/Fat32.h");
	        AddFile("https://raw.github.com/reefangel/libraries/master/ReefTouch/Green.h",Base.getSketchbookFolder().getPath() + "/libraries/ReefTouch/Green.h");
	        AddFile("https://raw.github.com/reefangel/libraries/master/ReefTouch/mmc.cpp",Base.getSketchbookFolder().getPath() + "/libraries/ReefTouch/mmc.cpp");
	        AddFile("https://raw.github.com/reefangel/libraries/master/ReefTouch/mmc.h",Base.getSketchbookFolder().getPath() + "/libraries/ReefTouch/mmc.h");
	        AddFile("https://raw.github.com/reefangel/libraries/master/ReefTouch/Red.h",Base.getSketchbookFolder().getPath() + "/libraries/ReefTouch/Red.h");
	        AddFile("https://raw.github.com/reefangel/libraries/master/ReefTouch/ReefAngel_Logo.h",Base.getSketchbookFolder().getPath() + "/libraries/ReefTouch/ReefAngel_Logo.h");
	        AddFile("https://raw.github.com/reefangel/libraries/master/ReefTouch/ReefTouch.cpp",Base.getSketchbookFolder().getPath() + "/libraries/ReefTouch/ReefTouch.cpp");
	        AddFile("https://raw.github.com/reefangel/libraries/master/ReefTouch/ReefTouch.h",Base.getSketchbookFolder().getPath() + "/libraries/ReefTouch/ReefTouch.h");
	        AddFile("https://raw.github.com/reefangel/libraries/master/ReefTouch/SDFile.cpp",Base.getSketchbookFolder().getPath() + "/libraries/ReefTouch/SDFile.cpp");
	        AddFile("https://raw.github.com/reefangel/libraries/master/ReefTouch/SDFile.h",Base.getSketchbookFolder().getPath() + "/libraries/ReefTouch/SDFile.h");
	        AddFile("https://raw.github.com/reefangel/libraries/master/ReefTouch/TestImage.h",Base.getSketchbookFolder().getPath() + "/libraries/ReefTouch/TestImage.h");
	        AddFile("https://raw.github.com/reefangel/libraries/master/Relay/Relay.cpp",Base.getSketchbookFolder().getPath() + "/libraries/Relay/Relay.cpp");
	        AddFile("https://raw.github.com/reefangel/libraries/master/Relay/Relay.h",Base.getSketchbookFolder().getPath() + "/libraries/Relay/Relay.h");
	        AddFile("https://raw.github.com/reefangel/libraries/master/RF/RF.cpp",Base.getSketchbookFolder().getPath() + "/libraries/RF/RF.cpp");
	        AddFile("https://raw.github.com/reefangel/libraries/master/RF/RF.h",Base.getSketchbookFolder().getPath() + "/libraries/RF/RF.h");
	        AddFile("https://raw.github.com/reefangel/libraries/master/RT_PWM/RT_PWM.cpp",Base.getSketchbookFolder().getPath() + "/libraries/RT_PWM/RT_PWM.cpp");
	        AddFile("https://raw.github.com/reefangel/libraries/master/RT_PWM/RT_PWM.h",Base.getSketchbookFolder().getPath() + "/libraries/RT_PWM/RT_PWM.h");
	        AddFile("https://raw.github.com/reefangel/libraries/master/Salinity/Salinity.cpp",Base.getSketchbookFolder().getPath() + "/libraries/Salinity/Salinity.cpp");
	        AddFile("https://raw.github.com/reefangel/libraries/master/Salinity/Salinity.h",Base.getSketchbookFolder().getPath() + "/libraries/Salinity/Salinity.h");
	        AddFile("https://raw.github.com/reefangel/libraries/master/Timer/Timer.cpp",Base.getSketchbookFolder().getPath() + "/libraries/Timer/Timer.cpp");
	        AddFile("https://raw.github.com/reefangel/libraries/master/Timer/Timer.h",Base.getSketchbookFolder().getPath() + "/libraries/Timer/Timer.h");
	        AddFile("http://www.reefangel.com/update/files/RA_Preloaded/RA_Preloaded.ino",Base.getSketchbookFolder().getPath() + "/RA_Preloaded/RA_Preloaded.ino");
	        AddFile("http://www.reefangel.com/update/files/tools/RALibsVer/tool/RALibsVer.jar",Base.getSketchbookFolder().getPath() + "/tools/RALibsVer/tool/RALibsVer.jar");
	        AddFile("http://www.reefangel.com/update/files/tools/RestorePreloaded/tool/RestorePreloaded.jar",Base.getSketchbookFolder().getPath() + "/tools/RestorePreloaded/tool/RestorePreloaded.jar");
	        AddFile("http://www.reefangel.com/update/files/tools/RestorePreloaded/data/connection.png",Base.getSketchbookFolder().getPath() + "/tools/RestorePreloaded/data/connection.png");
	        AddFile("http://www.reefangel.com/update/files/tools/RestorePreloaded/data/ra_small.png",Base.getSketchbookFolder().getPath() + "/tools/RestorePreloaded/data/ra_small.png");
	        AddFile("http://www.reefangel.com/update/files/tools/Wizard/tool/Wizard.jar",Base.getSketchbookFolder().getPath() + "/tools/Wizard/tool/Wizard.jar");
	        AddFile("http://www.reefangel.com/update/files/tools/Wizard/data/AI.png",Base.getSketchbookFolder().getPath() + "/tools/Wizard/data/AI.png");
	        AddFile("http://www.reefangel.com/update/files/tools/Wizard/data/connection.png",Base.getSketchbookFolder().getPath() + "/tools/Wizard/data/connection.png");
	        AddFile("http://www.reefangel.com/update/files/tools/Wizard/data/moon.png",Base.getSketchbookFolder().getPath() + "/tools/Wizard/data/moon.png");
	        AddFile("http://www.reefangel.com/update/files/tools/Wizard/data/parabola.png",Base.getSketchbookFolder().getPath() + "/tools/Wizard/data/parabola.png");
	        AddFile("http://www.reefangel.com/update/files/tools/Wizard/data/parabolasettings.png",Base.getSketchbookFolder().getPath() + "/tools/Wizard/data/parabolasettings.png");
	        AddFile("http://www.reefangel.com/update/files/tools/Wizard/data/ra_ato.png",Base.getSketchbookFolder().getPath() + "/tools/Wizard/data/ra_ato.png");
	        AddFile("http://www.reefangel.com/update/files/tools/Wizard/data/ra_dimming.png",Base.getSketchbookFolder().getPath() + "/tools/Wizard/data/ra_dimming.png");
	        AddFile("http://www.reefangel.com/update/files/tools/Wizard/data/ra_small.png",Base.getSketchbookFolder().getPath() + "/tools/Wizard/data/ra_small.png");
	        AddFile("http://www.reefangel.com/update/files/tools/Wizard/data/Radion.png",Base.getSketchbookFolder().getPath() + "/tools/Wizard/data/Radion.png");
	        AddFile("http://www.reefangel.com/update/files/tools/Wizard/data/relay_small.png",Base.getSketchbookFolder().getPath() + "/tools/Wizard/data/relay_small.png");
	        AddFile("http://www.reefangel.com/update/files/tools/Wizard/data/slope.png",Base.getSketchbookFolder().getPath() + "/tools/Wizard/data/slope.png");
	        AddFile("http://www.reefangel.com/update/files/tools/Wizard/data/slopesettings.png",Base.getSketchbookFolder().getPath() + "/tools/Wizard/data/slopesettings.png");

	        for (int a=0;a<SourceFiles.length;a++)
	        {
	        	copyURLtoFile(SourceFiles[a],DestFiles[a]);
	        	progressBar.setValue(a);
	        }
	        dialog.dispose();
			try {
				JOptionPane.showMessageDialog(null,"Your Reef Angel Libraries have been updated to version " + MyVer() , "Reef Angel Update Tool", JOptionPane.INFORMATION_MESSAGE);
			} catch (Exception e) {
				//e.printStackTrace();
				//System.err.println("Error while trying to check for an update.");
			}
		}
		
		public void AddFile(String source, String dest)
		{
			SourceFiles = push(SourceFiles,source);
			DestFiles = push(DestFiles,dest);
			progressBar.setMaximum(SourceFiles.length);
//			progressBar.repaint();
		}
		
		private String[] push(String[] array, String push) { 
		    String[] longer = new String[array.length + 1]; 
		    System.arraycopy(array, 0, longer, 0, array.length); 
		    longer[array.length] = push; 
		    return longer; 
		} 
		private void copyURLtoFile (String origin, String dest){
			URL originURL = null;
			
			Filename fn = new Filename(dest, '/', '.');
//			System.out.println(fn.path());
			File f=new File(fn.path());
			if (!f.exists())
			{
				System.out.println("Does not exist");
				f.mkdir();
			}
			try {
				originURL = new URL(origin);
			} catch (MalformedURLException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} 
			ReadableByteChannel rbc = null;
			try {
				rbc = Channels.newChannel(originURL.openStream());
			} catch (IOException e2) {
				// TODO Auto-generated catch block
				e2.printStackTrace();
			} 
			FileOutputStream fos = null;
			try {
				fos = new FileOutputStream(dest);
			} catch (FileNotFoundException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} 
			try {
				fos.getChannel().transferFrom(rbc, 0, 1 << 24);
				fos.close();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} 	
		}		
	}
}
