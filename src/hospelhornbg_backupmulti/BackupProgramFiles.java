package hospelhornbg_backupmulti;

import java.io.IOException;

public class BackupProgramFiles {
	
	public static final String HASH_ALGO = "SHA-1";
	
	public static final String DN_INDEX = "_index";
	public static final String DN_DATA = "_data";
	public static final String DN_INDEX_FS = "_fs";
	
	public static final String FN_SETTINGS_BUDRIVE = "settings.ini";
	public static final String FN_INDEX_DEVTBL = "devs.bin";
	public static final String FN_INDEX_DEVBL = "blacklist.txt";
	
	public static final int OSENUM_UNK = 0;
	public static final int OSENUM_WIN = 1;
	public static final int OSENUM_LINUX = 2;
	public static final int OSENUM_MACOS = 3;
	
	private static BackupManager current_manager = null;
	
	public static BackupManager loadManagerFrom(String dir_path) throws IOException{
		//If path null, calls the constructor overload that checks from reflection
		if(dir_path == null || dir_path.isEmpty()) current_manager = new BackupManager();
		else current_manager = new BackupManager(dir_path);
		return current_manager;
	}
	
	public static BackupManager getActiveManager(){return current_manager;}
	
	public static String getSysDefaultSettingsDirectory(){
		//TODO
		return null;
	}
	
	public static String getBackupBaseDirectory(){
		//Current directory to backup root directory
		/*If not set manually (in main() arguments), it will try to
		 * detect the directory the BackupProgramFiles class was loaded from
		 * (ie. the directory containing the JAR)
		 * */
		if(current_manager == null) return null;
		return current_manager.getRootDirPath();
	}
	
	public static boolean alwaysCheckHash(){
		if(current_manager == null) return false;
		return current_manager.getHashAll();
	}
	
	public static DeviceRecord currentDevice(){
		if(current_manager == null) return null;
		return current_manager.getCurrentHost();
	}

	public static short currentDriveID(){
		//TODO
		return 0;
	}
	
	public static void shutDownManager(){
		//TODO
	}
	
}
