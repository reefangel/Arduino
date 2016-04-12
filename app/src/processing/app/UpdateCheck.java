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

import org.apache.commons.compress.utils.IOUtils;
import processing.app.legacy.PApplet;

import javax.swing.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Random;
import java.io.BufferedReader; 
import java.io.FileReader; 
import java.io.IOException; 
import java.util.ArrayList; 
import java.util.List; 

import static processing.app.I18n.tr;


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
//<<<<<<< HEAD
//	Base base;
//
//	static final long ONE_DAY = 24 * 60 * 60 * 1000;
//
//	JPanel panel = new JPanel();
//	JProgressBar progressBar;
//	JDialog dialog;
//	
//	public UpdateCheck(Base base) {
//		Thread thread = new Thread(this);
//		this.base = base;
//		thread.start();
//	}
//
//
//	public void run() {
//		try {
//			String lastString = Preferences.get("update.last");
//			long now = System.currentTimeMillis();
//			if (lastString != null) {
//				long when = Long.parseLong(lastString);
//				if (now - when < ONE_DAY) {
//					// don't annoy the shit outta people
//					return;
//				}
//			}
//			Preferences.set("update.last", String.valueOf(now));
//
//			String prompt =
//					_("A new version of the Reef Angel Libraries is available.\nYour current version is " + MyVer() + "\n" +
//							"Newer version is " + readVer() + "\n\n" +
//							"Would you like to update now?");
//
//			if (base.activeEditor != null) {
//				if (!readVer().equals(MyVer())) {
//					Object[] options = { _("Yes"), _("No") };
//					int result = JOptionPane.showOptionDialog(base.activeEditor,
//							prompt,
//							_("Update"),
//							JOptionPane.YES_NO_OPTION,
//							JOptionPane.QUESTION_MESSAGE,
//							null,
//							options,
//							options[0]);
//					if (result == JOptionPane.YES_OPTION) {
//						if (base.activeEditor != null) {
//							panel.add(new JLabel ("Progress:"));
//							progressBar = new JProgressBar(0, 100);
//							progressBar.setValue(0);
//							progressBar.setStringPainted(true);	
//							panel.add(progressBar);
//							Thread t = new Thread(new DownloadFile());
//							t.start();
//							JOptionPane pane = new JOptionPane(panel);
//							pane.setOptions(new Object[] {"Cancel"});
//							dialog = pane.createDialog(base.activeEditor,"Reef Angel Update Tool"); 
//							dialog.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE); 
//							dialog.setVisible(true);
//
//						}	
//					}
//				}
//			}
//		} catch (Exception e) {
//			//e.printStackTrace();
//			//System.err.println("Error while trying to check for an update.");
//		}
//	}
//
//
//	protected String readVer() throws Exception {
//		// generate a random id in case none exists yet
//		Random r = new Random();
//		long id = r.nextLong();
//
//		String idString = Preferences.get("update.id");
//		if (idString != null) {
//			id = Long.parseLong(idString);
//		} else {
//			Preferences.set("update.id", String.valueOf(id));
//		}
//
//		String info;
//		info = URLEncoder.encode(id + "\t" +
//				PApplet.nf(Base.REVISION, 4) + "\t" +
//				System.getProperty("java.version") + "\t" +
//				System.getProperty("java.vendor") + "\t" +
//				System.getProperty("os.name") + "\t" +
//				System.getProperty("os.version") + "\t" +
//				System.getProperty("os.arch"), "UTF-8");
//
//		String filename = "https://raw.github.com/reefangel/Libraries/master/ReefAngel/ReefAngel.h?" + info;
//		URL url = new URL(filename);
//		InputStream stream = url.openStream();
//		InputStreamReader isr = new InputStreamReader(stream);
//		BufferedReader reader = new BufferedReader(isr);
//
//		String strLine;
//		String strversion="";
//		while ((strLine = reader.readLine()) != null)   {
//			if (strLine.indexOf("ReefAngel_Version ")>0)
//			{
//				strversion=strLine.substring(strLine.indexOf("ReefAngel_Version ")+19);
//				strversion=strversion.replace("\"", "");
//			}
//		}
//		return strversion;
//	}
//
//	protected String MyVer() throws Exception {
//		FileInputStream fstream = new FileInputStream(Base.getSketchbookFolder().getPath() + "/libraries/ReefAngel/ReefAngel.h");
//		DataInputStream in = new DataInputStream(fstream);
//		BufferedReader br = new BufferedReader(new InputStreamReader(in));
//		String strLine;
//		String strversion="";
//		while ((strLine = br.readLine()) != null)   {
//			if (strLine.indexOf("ReefAngel_Version ")>0)
//			{
//				strversion=strLine.substring(strLine.indexOf("ReefAngel_Version ")+19);
//				strversion=strversion.replace("\"", "");
//			}
//		}
//		in.close();
//		return strversion;
//	}
//	
//	class Filename {
//		private String fullPath;
//		private char pathSeparator, extensionSeparator;
//
//		public Filename(String str, char sep, char ext) {
//			fullPath = str;
//			pathSeparator = sep;
//			extensionSeparator = ext;
//		}
//
//		public String extension() {
//			int dot = fullPath.lastIndexOf(extensionSeparator);
//			return fullPath.substring(dot + 1);
//		}
//
//		public String filename() { // gets filename without extension
//			int dot = fullPath.lastIndexOf(extensionSeparator);
//			int sep = fullPath.lastIndexOf(pathSeparator);
//			return fullPath.substring(sep + 1, dot);
//		}
//
//		public String path() {
//			int sep = fullPath.lastIndexOf(pathSeparator);
//			return fullPath.substring(0, sep);
//		}
//	}
//
//	class DownloadFile implements Runnable
//	{
//		String[] SourceFiles=new String[0];
//		
//		public void run(){
//			try
//			{
//				copyURLtoFile("http://www.reefangel.com/update/files/update/source.txt",Base.getSketchbookFolder().getPath() + "/update/source.txt");
//				FileReader fileReader = new FileReader(Base.getSketchbookFolder().getPath() + "/update/source.txt"); 
//				BufferedReader bufferedReader = new BufferedReader(fileReader); 
//				List<String> lines = new ArrayList<String>(); 
//				String line = null; 
//				while ((line = bufferedReader.readLine()) != null) { 
//					lines.add(line); 
//				} 
//				bufferedReader.close(); 
//				SourceFiles=lines.toArray(new String[lines.size()]); 
//			}
//			catch (IOException e1) {
//				e1.printStackTrace();
//			} 	
//
//	        for (int a=0;a<SourceFiles.length;a++)
//	        {
//	        	String s=SourceFiles[a].replace("http://www.reefangel.com/update/files", "").replace("https://raw.github.com/reefangel/libraries/master", "/libraries");
//	        	System.out.println("Updating " + s);
//	        	copyURLtoFile(SourceFiles[a],Base.getSketchbookFolder().getPath() + s);
//	        	progressBar.setValue(a);
//	        }
//	        dialog.dispose();
//			try {
//				JOptionPane.showMessageDialog(null,"Your Reef Angel Libraries have been updated to version " + MyVer() , "Reef Angel Update Tool", JOptionPane.INFORMATION_MESSAGE);
//			} catch (Exception e) {
//				//e.printStackTrace();
//				//System.err.println("Error while trying to check for an update.");
//			}
//		}
//		
//		private void copyURLtoFile (String origin, String dest){
//			URL originURL = null;
//			
//			Filename fn = new Filename(dest, '/', '.');
////			System.out.println(fn.path());
//			File f=new File(fn.path());
//			if (!f.exists())
//			{
//				System.out.println("Does not exist");
//				f.mkdir();
//			}
//			try {
//				originURL = new URL(origin);
//			} catch (MalformedURLException e1) {
//				// TODO Auto-generated catch block
//				e1.printStackTrace();
//			} 
//			ReadableByteChannel rbc = null;
//			try {
//				rbc = Channels.newChannel(originURL.openStream());
//			} catch (IOException e2) {
//				// TODO Auto-generated catch block
//				e2.printStackTrace();
//			} 
//			FileOutputStream fos = null;
//			try {
//				fos = new FileOutputStream(dest);
//			} catch (FileNotFoundException e1) {
//				// TODO Auto-generated catch block
//				e1.printStackTrace();
//			} 
//			try {
//				fos.getChannel().transferFrom(rbc, 0, 1 << 24);
//				fos.close();
//			} catch (IOException e1) {
//				// TODO Auto-generated catch block
//				e1.printStackTrace();
//			} 	
//		}		
//	}
//=======
  Base base;
  String downloadURL = tr("http://www.arduino.cc/latest.txt");

  static final long ONE_DAY = 24 * 60 * 60 * 1000;


  public UpdateCheck(Base base) {
    Thread thread = new Thread(this);
    this.base = base;
    thread.start();
  }


  public void run() {
    //System.out.println("checking for updates...");

    // generate a random id in case none exists yet
    Random r = new Random();
    long id = r.nextLong();

    String idString = PreferencesData.get("update.id");
    if (idString != null) {
      id = Long.parseLong(idString);
    } else {
      PreferencesData.set("update.id", String.valueOf(id));
    }

    try {
      String info;
      info = URLEncoder.encode(id + "\t" +
                        PApplet.nf(BaseNoGui.REVISION, 4) + "\t" +
                        System.getProperty("java.version") + "\t" +
                        System.getProperty("java.vendor") + "\t" +
                        System.getProperty("os.name") + "\t" +
                        System.getProperty("os.version") + "\t" +
                        System.getProperty("os.arch"), "UTF-8");
      
      int latest = readInt(downloadURL + "?" + info);

      String lastString = PreferencesData.get("update.last");
      long now = System.currentTimeMillis();
      if (lastString != null) {
        long when = Long.parseLong(lastString);
        if (now - when < ONE_DAY) {
          // don't annoy the shit outta people
          return;
        }
      }
      PreferencesData.set("update.last", String.valueOf(now));

      String prompt =
        tr("A new version of Arduino is available,\n" +
          "would you like to visit the Arduino download page?");
        
      if (base.activeEditor != null) {
        if (latest > BaseNoGui.REVISION) {
          Object[] options = { tr("Yes"), tr("No") };
          int result = JOptionPane.showOptionDialog(base.activeEditor,
                                                    prompt,
                                                    tr("Update"),
                                                    JOptionPane.YES_NO_OPTION,
                                                    JOptionPane.QUESTION_MESSAGE,
                                                    null,
                                                    options,
                                                    options[0]);
          if (result == JOptionPane.YES_OPTION) {
            Base.openURL(tr("http://www.arduino.cc/en/Main/Software"));
          }
        }
      }
    } catch (Exception e) {
      //e.printStackTrace();
      //System.err.println("Error while trying to check for an update.");
    }
  }


  protected int readInt(String filename) throws IOException {
    URL url = new URL(filename);
    BufferedReader reader = null;
    try {
      reader = new BufferedReader(new InputStreamReader(url.openStream()));
      return Integer.parseInt(reader.readLine());
    } finally {
      IOUtils.closeQuietly(reader);
    }
  }
}
