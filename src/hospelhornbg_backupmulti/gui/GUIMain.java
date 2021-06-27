/**
 * 
 */
package hospelhornbg_backupmulti.gui;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.SwingUtilities;

import hospelhornbg_backupmulti.BackupProgramFiles;
import hospelhornbg_backupmulti.DeviceRecord;
import waffleoRai_GUITools.GUITools;

/**
 * @author Blythe
 *
 */
public class GUIMain {

	private static void launchMainGUI(){
		BUExForm mygui = new BUExForm(BackupProgramFiles.getActiveManager());
    	mygui.setLocation(GUITools.getScreenCenteringCoordinates(mygui));
    	mygui.addWindowListener(new WindowAdapter(){
    		public void windowClosing(WindowEvent e) {
    			BackupProgramFiles.shutDownManager();
    			System.exit(0);
			}
    	});
    	mygui.pack();
    	mygui.setVisible(true);
	}
	
	private static void deviceSelectPopup(){
		DeviceSelectWindow dsw = new DeviceSelectWindow(BackupProgramFiles.getActiveManager());
		dsw.addWindowListener(new WindowAdapter(){
    		public void windowClosed(WindowEvent e) {
    			//System.err.println("dsw window closed event heard!");
    			if(!dsw.getCloseSelection()){
    				System.err.println("Cancelled, exiting.");
        			System.exit(2);
        		}
        		DeviceRecord dev = dsw.getSelectedDevice();
        		BackupProgramFiles.getActiveManager().setCurrentHost(dev);
        		launchMainGUI();
			}
    	});
		dsw.setLocation(GUITools.getScreenCenteringCoordinates(dsw));
		dsw.pack();
		
		dsw.setVisible(true);
	}
	
	public static void main(String[] args) {

		//Parse arguments. Arg0 should be dir.
		//If it's null, it tries to use the JAR dir
		String budir = null;
		if(args != null && args.length > 0){
			budir = args[0];
		}
		if(budir != null){
			System.err.println("Backup directory requested: " + budir);
		}

		//Load device list
		//Determine which device this is
		try{
			BackupProgramFiles.loadManagerFrom(budir);
		}
		catch(Exception ex){
			System.err.println("Manager could not be loaded. See stack trace.");
			ex.printStackTrace();
			System.exit(1);
		}
		
		//Init GUI thread
		//If device was not recognized, pop up a dialog asking if user wants
		//	new device or to update existing one
		
		SwingUtilities.invokeLater(new Runnable() 
        {
            public void run() 
            {
            	//First, popup to match device if it was not found.
            	if(BackupProgramFiles.currentDevice() == null){
            		deviceSelectPopup();
            	}
            	else launchMainGUI();
            }
        });
		
	}

}
