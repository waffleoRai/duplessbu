package hospelhornbg_backupmulti;

public class BackupProgramFiles {
	
	public static final String HASH_ALGO = "SHA-1";
	
	public static String getSysDefaultSettingsDirectory(){
		//TODO
		return null;
	}
	
	public static String getBackupBaseDirectory(){
		//TODO
		//Current directory to backup root directory
		/*If not set manually (in main() arguments), it will try to
		 * detect the directory the BackupProgramFiles class was loaded from
		 * (ie. the directory containing the JAR)
		 * */
		return null;
	}
	
	public static boolean alwaysCheckHash(){
		//TODO
		return false;
	}
	
	public static DeviceRecord currentDevice(){
		//TODO
		return null;
	}

	public static short currentDriveID(){
		//TODO
		return 0;
	}
}
