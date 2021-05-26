package hospelhornbg_backupmulti;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.InetAddress;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import waffleoRai_Reflection.ReflectionUtils;
import waffleoRai_Utils.FileBuffer;

/*Update Record
 * 
 * 2021.05.24 | 1.0.0
 * 
 */

/**
 * Non-static class serving as a manager for the backup environment.
 * @author Blythe Hospelhorn
 * @version 1.0.0
 * @since May 24, 2021
 */
public class BackupManager {
	
	public static final long COMP_THRESH_DEFO = 0x1000L;
	
	public static final String INIKEY_HASHALL = "HASHALL";
	public static final String INIKEY_COMPTHRESH = "COMPTHRESH";
	
	private String root_dir;
	private boolean hash_all;
	private long comp_thresh;
	
	//For now, not including randomness check
	
	private DeviceRecord localHost;
	private List<DeviceRecord> devices;

	/**
	 * Construct a <code>BackupManager</code> using the directory
	 * the <code>BackupManager</code> class was loaded from as the backup
	 * root directory.
	 * @throws IOException If the class path could not be detected, or the settings
	 * file could not be read.
	 * @since 1.0.0
	 */
	public BackupManager() throws IOException {
		try{
			Path mypath = ReflectionUtils.getLoadedClassFileSource(BackupManager.class);
			mypath = mypath.getParent();
			root_dir = mypath.toAbsolutePath().toString();
		}
		catch(Exception x){
			x.printStackTrace();
			throw new IOException("Path BackupManager class was loaded from could not be determined.");
		}
		loadSettings();
		loadDevices();
	}
	
	/**
	 * Construct a <code>BackupManager</code> using a provided
	 * root dir path as a <code>String</code>.
	 * @param dir_path Backup utility root directory. Backup data will be loaded/read from
	 * this directory.
	 * @throws IOException If there is an error reading the settings file.
	 * @since 1.0.0
	 */
	public BackupManager(String dir_path) throws IOException {
		root_dir = dir_path;
		loadSettings();
		loadDevices();
	}
	
	private void loadSettings() throws IOException{
		//Look for settings file.
		String path = root_dir + File.separator + BackupProgramFiles.FN_SETTINGS_BUDRIVE;
		hash_all = false;
		comp_thresh = COMP_THRESH_DEFO;
		if(FileBuffer.fileExists(path)){
			Map<String, String> map = new HashMap<String, String>();
			BufferedReader br = new BufferedReader(new FileReader(path));
			String line = null;
			while((line = br.readLine()) != null){
				if(line.isEmpty()) continue;
				String[] fields = line.split("=");
				String key = fields[0];
				String val = "";
				if(fields.length > 1) val = fields[1];
				map.put(key, val);
			}
			br.close();
			
			String val = map.get(INIKEY_HASHALL).toLowerCase();
			if(val != null){
				if(val.equals("true")) hash_all = true;
			}
			
			val = map.get(INIKEY_COMPTHRESH);
			if(val != null){
				try{
					comp_thresh = Long.parseUnsignedLong(val, 16);
				}
				catch(NumberFormatException ex){}
			}
		}
	}
	
	private void loadDevices() throws IOException{
		// /_index/devs.bin
		//Open file (to determine how many devices and size of arraylist)
		char sep = File.separatorChar;
		String tpath = root_dir + sep + BackupProgramFiles.DN_INDEX + sep + BackupProgramFiles.FN_INDEX_DEVTBL;
		
		if(FileBuffer.fileExists(tpath)){
			//Detect the local host
			String localname = null;
			try{localname = InetAddress.getLocalHost().getHostName();}
			catch(Exception ex){ex.printStackTrace();}
			if(localname != null) localname = localname.toLowerCase();
			
			//Allocate ArrayList
			FileBuffer file = FileBuffer.createBuffer(tpath, true);
			file.setCurrentPosition(0L);
			int ver = file.nextInt();
			int rcount = Short.toUnsignedInt(file.nextShort());
			devices = new ArrayList<DeviceRecord>(rcount+2);
			
			//Read the rest of the file
			for(int i = 0; i < rcount; i++){
				DeviceRecord dev = DeviceRecord.readRecordFrom(file, ver);
				if(dev.getDisplayName().equals(localname)){
					//Set as local host too.
					localHost = dev;
				}
				devices.add(dev);
			}
			
		}
		else{
			//Just alloc array list.
			//Local host detection should be called manually.
			devices = new ArrayList<DeviceRecord>();
		}
	}
	
	/**
	 * Generate a device record for the system currently running the tool. The
	 * name in the record will be the device's network name. 
	 * This method then tries to detect the OS. If Windows, it will try to scan for
	 * all root drives as well.
	 * <br>This method also sets the static field referencing the current local host
	 * to the newly generated record.
	 * @return New <code>DeviceRecord</code> containing information on the host
	 * system. If the host system has no network name or there is another problem
	 * generating a record, this method will return <code>null</code>.
	 * @since 1.0.0
	 */
	public DeviceRecord genRecordForLocalHost(){
		String localname = null;
		try{localname = InetAddress.getLocalHost().getHostName();}
		catch(Exception ex){ex.printStackTrace(); return null;}
		localname = localname.toLowerCase();
		
		//Detect OS.
		String osname = System.getProperty("os.name");
		int os = BackupProgramFiles.OSENUM_UNK;
		if(osname != null){
			osname = osname.toLowerCase();
			if(osname.startsWith("win")) os = BackupProgramFiles.OSENUM_WIN;
			else if (osname.contains("linux")) os = BackupProgramFiles.OSENUM_LINUX;
			else if (osname.contains("osx")) os = BackupProgramFiles.OSENUM_MACOS;
		}
		
		if(localname == null){
			Random rand = new Random();
			if(osname == null) osname = "UNK_OS";
			osname = osname.toUpperCase().replace(" ", "_");
			localname = osname + "_" + Long.toHexString(rand.nextLong());
		}
		
		//Gen ID and device record
		DeviceRecord dev = new DeviceRecord(localname.hashCode(), 8);
		dev.setDisplayName(localname);
		
		//Detect drives.
		if(os == BackupProgramFiles.OSENUM_WIN){
			//Just gonna go thru the alphabet.........
			for(char c = 'A'; c <= 'Z'; c++){
				String dpath = c + ":";
				if(FileBuffer.directoryExists(dpath)){
					dev.addDrive(Character.toString(c), dpath);
				}
			}
		}
		else{
			//Just "/"
			dev.addDrive("root", "/");
		}
		
		devices.add(dev);
		localHost = dev;
		return dev;
	}
	
	/**
	 * Get a <code>String</code> representing the path (on local system) to
	 * backup root directory.
	 * @return Backup root directory path.
	 * @since 1.0.0
	 */
	public String getRootDirPath(){return root_dir;}
	
	/**
	 * Get a flag representing whether this <code>BackupManager</code> is
	 * set to run a hash of <i>every</i> file that is processed during backup
	 * regardless of its last date modified.
	 * @return <code>True</code> if this manager is set to hash every file. <code>False</code>
	 * if it is set to only hash new files.
	 * @since 1.0.0
	 */
	public boolean getHashAll(){return hash_all;}
	
	/**
	 * Get the currently set compression threshold for this manager. The compression
	 * threshold is the size over which a file of a known uncompressed file format
	 * will be subject to deflation if auto-compression is turned on.
	 * @return Long integer representing the maximum size of a file before triggering
	 * auto-compression. If -1, this threshold is not set.
	 * @since 1.0.0
	 */
	public long getCompThreshold(){return comp_thresh;}
	
	/**
	 * Get the <code>DeviceRecord</code> for the current host, if one has been previously
	 * generated and the host has been properly detected.
	 * @return <code>DeviceRecord</code> representing device currently running the
	 * program, or <code>null</code> if unknown.
	 * @since 1.0.0
	 */
	public DeviceRecord getCurrentHost(){return localHost;}
	
	/**
	 * 
	 * @return
	 * @since 1.0.0
	 */
	public List<DeviceRecord> getAllDevices(){
		List<DeviceRecord> list = new ArrayList<DeviceRecord>(devices.size()+1);
		list.addAll(devices);
		return list;
	}
	
	/**
	 * Set the root path to a <code>String</code> formatted in such a way the local
	 * file system recognizes. This is the root path used for the backup directory.
	 * @param path Path to set as root.
	 * @since 1.0.0
	 */
	public void setRootDirPath(String path){root_dir = path;}
	
	/**
	 * Set flag telling this manager whether all files should be hash checked when
	 * backed up.
	 * @param flag <code>true</code> to set full hash check, <code>false</code>
	 * to unset.
	 * @since 1.0.0
	 */
	public void setHashAll(boolean flag){hash_all = flag;}
	
	/**
	 * Set the compression threshold for this manager. The compression
	 * threshold is the size over which a file of a known uncompressed file format
	 * will be subject to deflation if auto-compression is turned on.
	 * @param value Value to set compression threshold to. If negative, auto-compression
	 * will be turned off.
	 * @since 1.0.0
	 */
	public void setCompThreshold(long value){comp_thresh = value;}

	/**
	 * 
	 * @param device
	 * @since 1.0.0
	 */
	public void setCurrentHost(DeviceRecord device){
		localHost = device;
	}
	
	/**
	 * Write out the settings to a text file in the root directory to 
	 * save the current environment settings.
	 * @throws IOException If there is an error writing to the current root directory.
	 * @since 1.0.0
	 */
	public void saveSettings() throws IOException{
		String path = root_dir + File.separator + BackupProgramFiles.FN_SETTINGS_BUDRIVE;
		BufferedWriter bw = new BufferedWriter(new FileWriter(path));
		bw.write(INIKEY_HASHALL + "=" + hash_all + "\n");
		bw.write(INIKEY_COMPTHRESH + "=" + Long.toHexString(comp_thresh) + "\n");
		bw.close();
	}
	
}
