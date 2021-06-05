package hospelhornbg_backupmulti;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import hospelhornbg_backupmulti.DataFile.DeviceFile;
import hospelhornbg_backupmulti.DeviceRecord.DriveRecord;
import waffleoRai_Files.tree.DirectoryNode;
import waffleoRai_Files.tree.FileNode;
import waffleoRai_Reflection.ReflectionUtils;
import waffleoRai_Utils.BinFieldSize;
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
	
	private DataIndex idx_dat;
	private NameIndex idx_name;
	
	//Backup state
	private short drive_id;
	private OutputStream fs_out;
	private long fs_out_pos;
	private Map<String, String> pathsubs;
	private List<String> blacklist;
	
	private Map<Integer, Integer> fsoffrepl;

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
		loadIndices();
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
		loadIndices();
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
	
	private void loadIndices() throws IOException{
		//TODO
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
	public String getHostBlacklistRelativePath(){
		if(localHost == null) return null;
		StringBuilder sb = new StringBuilder(4096);
		sb.append("/"); sb.append(BackupProgramFiles.DN_INDEX);
		sb.append("/"); sb.append(BackupProgramFiles.DN_INDEX_FS);
		sb.append("/"); sb.append(localHost.getDisplayName());
		sb.append("/"); sb.append(BackupProgramFiles.FN_INDEX_DEVBL);
		return sb.toString();
	}
	
	/**
	 * 
	 * @return
	 * @since 1.0.0
	 */
	public String getHostBlacklistAbsolutePath(){
		return toHostFSPath(getHostBlacklistRelativePath());
	}
	
	/**
	 * 
	 * @return
	 * @since 1.0.0
	 */
	public String toHostFSPath(String relPath){
		if(relPath == null) return null;
		return getRootDirPath() + File.separator + relPath.replace('/', File.separatorChar);
	}
	
	/**
	 * 
	 * @param guid
	 * @return
	 * @since 1.0.0
	 */
	public String getDataFileRelativePath(long guid){
		long b0 = (guid >>> 56) & 0xFF;
		StringBuilder sb = new StringBuilder(64);
		sb.append("./");
		sb.append(BackupProgramFiles.DN_DATA);
		sb.append("/");
		sb.append(String.format("%02x", b0));
		sb.append("/");
		sb.append(String.format("%016x", guid));
		return sb.toString();
	}
	
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
	 * 
	 * @return
	 * @since 1.0.0
	 */
	public List<String> defaultBlacklist(){
		List<String> list = new LinkedList<String>();
		if(this.localHost != null){
			int os = localHost.getOSEnum();
			DirectoryStream<Path> dstr = null;
			switch(os){
			case BackupProgramFiles.OSENUM_WIN:
				//Anything on OS root that isn't Users
				try{
					dstr = Files.newDirectoryStream(Paths.get("C:"));
					for(Path p : dstr){
						String apath = p.toAbsolutePath().toString();
						if(!apath.endsWith("Users")) list.add(apath);
					}
					dstr.close();
				}
				catch(Exception ex){ex.printStackTrace();}
				break;
			}
		}
				
		Collections.sort(list);
		return list;
	}
	
	/**
	 * 
	 * @return
	 * @since 1.0.0
	 */
	public List<String> getHostBlacklist() throws IOException{
		List<String> list = new LinkedList<String>();
		String path = getHostBlacklistAbsolutePath();
		if(path == null || path.isEmpty()) return list;
		
		if(!FileBuffer.fileExists(path)){
			//return default list
			return defaultBlacklist();
		}
		BufferedReader br = new BufferedReader(new FileReader(path));
		String line = null;
		while((line = br.readLine()) != null){
			if(line.isEmpty()) continue;
			list.add(line);
		}
		br.close();
		Collections.sort(list);
		return list;
	}
	
	/**
	 * 
	 * @return
	 * @since 1.0.0
	 */
	public void setHostBlacklist(Collection<String> list) throws IOException{
		if(list == null) return;
		String path = getHostBlacklistAbsolutePath();
		BufferedWriter bw = new BufferedWriter(new FileWriter(path));
		for(String s : list){
			bw.write(s + "\n");
		}
		bw.close();
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

	private boolean backupFile(DirectoryNode pnode, String tpath, String true_path, String alias_path, boolean parent_exists, BackupListener observer) throws IOException{
		
		//Check if file exists (ie. is not blacklisted and file exists on source drive)
		boolean iexist = parent_exists && FileBuffer.fileExists(true_path) && !blacklist.contains(alias_path);
		
		//Get previous record (if exists)
		String filename = alias_path.substring(alias_path.lastIndexOf(File.separatorChar) + 1);
		VersionedFileNode prevrec = null;
		if(pnode != null){
			FileNode fn = pnode.getNodeAt("./" + filename);
			if(fn != null){
				if(fn instanceof VersionedFileNode){
					prevrec = (VersionedFileNode)fn;
				}
			}
		}
		
		//Update fs offset in data index record
		if(prevrec != null){
			long[] ids = prevrec.getSubfiles();
			for(long id : ids){
				DataFile df = idx_dat.getDataRecord(id);
				DeviceFile vf = df.getDeviceFileFor(localHost.getID(), drive_id, prevrec.getOffset());
				vf.fs_offset = (int)fs_out_pos;
			}
		}
		
		//If doesn't exist, just copy the fs record that is there and return.
		if(!iexist){
			if(prevrec != null){
				FileBuffer fsdat = DriveFileSystem.serializeNode(prevrec);
				fsdat.writeToStream(fs_out);
				fs_out_pos += fsdat.getFileSize();
			}
			return true;
		}
		
		//Check if should hash regardless if new
		DataFile dat = null;
		long match_uid = 0L;
		if(hash_all){
			//Hash the file
			//This may take a while
			byte[] hash = BackupProgramFiles.getFileHash(true_path, observer);
			
			//Check to see if identical file already exists
			long guid = BackupProgramFiles.hash2UID(hash);
			if(idx_dat.recordExists(guid)){
				//If so, check if match is another version of this file
				//If not, set match_uid
				if(prevrec == null) match_uid = guid;
				else{
					if(!prevrec.hasSubfile(guid)) match_uid = guid;
				}
			}
			else{
				//If not, set dat
				dat = DataFile.generateDataFileRecord(true_path, alias_path, hash);
				//idx_dat.addDataRecord(dat);
				
				//Copy data to backup drive
				String dstpath = toHostFSPath(getDataFileRelativePath(guid));
				BackupProgramFiles.copyToBackup(true_path, dstpath, dat.isCompressed(), observer);
			}
		}
		else{
			//Determine most recent previous version
			DataFile lastver = null;
			if(prevrec != null){
				long[] prevvers = prevrec.getSubfiles();
				long latest = 0L;
				for(long ver : prevvers){
					DataFile prev = idx_dat.getDataRecord(ver);
					if(prev == null) continue;
					long ts = prev.getRawTimestamp();
					if(ts > latest){
						latest = ts;
						//lastver_guid = prev.getGUID();
						lastver = prev;
					}
				}
			}
			
			//Compare last modified date.
			boolean isnew = true;
			if(lastver != null){
				File f = new File(true_path);
				long ts = f.lastModified();	
				if(lastver.getRawTimestamp() == ts) isnew = false;
			}
			
			if(isnew){
				//If new version of file, copy to backup drive while hashing
				boolean deflate = AutoCompression.autocompressFile(true_path);
				byte[] hash = BackupProgramFiles.hashAndCopy(true_path, tpath, deflate, observer);
				long guid = BackupProgramFiles.hash2UID(hash);
				
				//See if identical file already exists.
				if(idx_dat.recordExists(guid)){
					//If so, delete temp copy, set match_uid
					match_uid = guid;
					Files.deleteIfExists(Paths.get(tpath));
				}
				else{
					//If not, move temp copy to where expected
					//Generate record and set to dat
					String dstpath = toHostFSPath(getDataFileRelativePath(guid));
					Files.move(Paths.get(tpath), Paths.get(dstpath));
					dat = DataFile.generateDataFileRecord(true_path, alias_path, hash);
				}
			}
			
		}
		
		if(dat == null){
			if(match_uid == 0L) {
				if(prevrec != null){
					//Nothing new. Just copy old record.
					FileBuffer fsdat = DriveFileSystem.serializeNode(prevrec);
					fsdat.writeToStream(fs_out);
					fs_out_pos += fsdat.getFileSize();	
				}
				return true;
			} 
			
			//Matched to a file in a different location
			//Update fs record and write to output
			FileBuffer fsdat = null;
			if(prevrec != null){
				prevrec.addSubfile(match_uid);
				fsdat = DriveFileSystem.serializeNode(prevrec);
			}
			else{
				VersionedFileNode vfn = new VersionedFileNode(pnode, filename);
				fsdat = DriveFileSystem.serializeNode(vfn);
			}
			fsdat.writeToStream(fs_out);
			
			//Update GUID index record (new device file)
			DataFile df = idx_dat.getDataRecord(match_uid);
			DeviceFile vf = df.addDeviceFile(localHost.getID(), drive_id, alias_path);
			vf.fs_offset = (int)fs_out_pos;
			fs_out_pos += fsdat.getFileSize();
			
			//Update Name index
			idx_name.addMapping(filename, match_uid);
			
			return true;
		}
		
		//New data
		//Update fs record and write to output
		VersionedFileNode vfn = new VersionedFileNode(pnode, filename);
		FileBuffer fsdat = DriveFileSystem.serializeNode(vfn);
		fsdat.writeToStream(fs_out);
		
		//Update GUID index with DataFile record
		//Get device record and update fs_offset...
		idx_dat.addDataRecord(dat);
		DeviceFile vf = dat.getDeviceFileFor(localHost.getID(), drive_id, prevrec.getOffset());
		vf.fs_offset = (int)fs_out_pos;
		
		fs_out_pos += fsdat.getFileSize();
		
		//Update Name index
		idx_name.addMapping(filename, dat.getGUID());
		
		return true;
	}
	
	private boolean backupDirectory(DirectoryNode mynode, String true_path, String alias_path, boolean parent_exists, BackupListener observer) throws IOException{
		//Blacklist must be recursive!!!! If parent does not "exist", nothing below it does!
		
		//Aliased only if alias_path is non-null
		boolean aliased = (alias_path != null);
		if(!aliased) alias_path = true_path;
		boolean iexist = parent_exists && !blacklist.contains(alias_path) && FileBuffer.directoryExists(true_path);
		
		//Scan for contents of directory
		List<String> subdirs = new LinkedList<String>();
		List<String> files = new LinkedList<String>();
		Set<String> cnames = new HashSet<String>();
		if(iexist){
			DirectoryStream<Path> dstr = Files.newDirectoryStream(Paths.get(true_path));
			for(Path p : dstr){
				if(Files.isDirectory(p))subdirs.add(p.getFileName().toString().toLowerCase());
				else files.add(p.getFileName().toString().toLowerCase());
				cnames.add(p.getFileName().toString().toLowerCase());
			}
			dstr.close();	
		}
		
		//Scan for old children in fs record but not on device (anymore)
		if(mynode != null){
			List<FileNode> children = mynode.getChildren();
			for(FileNode fn : children){
				String name = fn.getFileName().toLowerCase();
				if(!cnames.contains(name)){
					cnames.add(name);
					if(fn.isDirectory()) subdirs.add(name);
					else files.add(name);
				}
			}
		}
		
		//Files
		String tpath = getRootDirPath() + File.separator + BackupProgramFiles.DN_DATA + File.separator + "data.tmp";
		for(String fname : files){
			String fpath_a = alias_path + File.separator + fname;
			String fpath_t = true_path + File.separator + fname;
			//Does file have overriding alias?
			if(pathsubs.containsKey(fpath_a)){fpath_t = pathsubs.get(fpath_a);}
			if(!backupFile(mynode, tpath, fpath_t, fpath_a, iexist, observer)) return false;
		}
		
		//Subdirectories
		//First cycle through and write the incomplete fs records (with placeholders for offsets)
		//Don't forget to update the directory offsets in the nodes for correct parent offsets
		//Don't forget blacklist
		//Map<String, Long> doffmap = new HashMap<String, Long>();
		final long DR_FIELD_OFF = 10L;
		for(String dname : subdirs){
			//Alias
			String dpath_a = alias_path + File.separator + dname;
			String dpath_t = dpath_a;
			boolean dn_alias = false;
			//See if it has its own alias
			if(pathsubs.containsKey(dpath_a)){
				dpath_t = pathsubs.get(dpath_a);
				dn_alias = true;
			}
			//If not, see if parent is aliased
			if(!dn_alias && aliased){
				dpath_t = true_path + File.separator + dname;
				dn_alias = true;
			}
			
			//Exists?
			boolean dn_exist = iexist && FileBuffer.directoryExists(dpath_t) && !blacklist.contains(dpath_a);
			
			//Get node
			DirectoryNode sdnode = null;
			if(mynode != null){
				FileNode fn = mynode.getNodeAt("./" + dname);
				if(fn != null && fn instanceof DirectoryNode){
					sdnode = (DirectoryNode)fn;
					//Force load if loadable dir node
					sdnode.getChildCount();
				}
			}
			if(sdnode == null){
				//make one
				sdnode = new DirectoryNode(mynode, dname);
			}
			
			//Count number of (non-blacklisted) children
			//This INCLUDES those in old fs record, but not on device any more
			Set<String> childnames = new HashSet<String>();
			if(sdnode != null){
				List<FileNode> children = sdnode.getChildren();
				for(FileNode fn : children) childnames.add(fn.getFileName().toLowerCase());
			}
			if(dn_exist){
				DirectoryStream<Path> dstr = Files.newDirectoryStream(Paths.get(dpath_t));
				for(Path p : dstr){
					String mypath = p.toAbsolutePath().toString();
					if(dn_alias) mypath = mypath.replace(dpath_t, dpath_a);
					if(blacklist.contains(mypath)) continue;
					childnames.add(p.getFileName().toString().toLowerCase());
				}
				dstr.close();	
			}
			
			//Update offset in sdnode
			sdnode.setOffset(fs_out_pos);
			
			//Write fs record, saving offset of contents offset field (for later overwrite)
			int rsz = 14 + 3 + (dname.length() << 2);
			FileBuffer dirrec = new FileBuffer(rsz, false);
			dirrec.addToFile((short)0x8000);
			int poff = 0;
			if(mynode != null) poff = (int)mynode.getOffset();
			dirrec.addToFile(poff);
			dirrec.addToFile(childnames.size());
			dirrec.addToFile(0xFFFFFFFF); //PLACEHOLDER
			dirrec.addVariableLengthString("UTF8", dname, BinFieldSize.WORD, 2);
			
			dirrec.writeToStream(fs_out);
			fs_out_pos += dirrec.getFileSize();
		}
		
		//Then call this function for the subdirs
		//Don't forget aliases and blacklisting
		for(String dname : subdirs){
			
			//Alias
			String dpath_a = alias_path + File.separator + dname;
			String dpath_t = dpath_a;
			boolean dn_alias = false;
			//See if it has its own alias
			if(pathsubs.containsKey(dpath_a)){
				dpath_t = pathsubs.get(dpath_a);
				dn_alias = true;
			}
			//If not, see if parent is aliased
			if(!dn_alias && aliased){
				dpath_t = true_path + File.separator + dname;
				dn_alias = true;
			}
			
			
			//Get node
			DirectoryNode sdnode = null;
			if(mynode != null){
				FileNode fn = mynode.getNodeAt("./" + dname);
				if(fn != null && fn instanceof DirectoryNode){
					sdnode = (DirectoryNode)fn;
				}
			}
			
			fsoffrepl.put((int)(sdnode.getOffset() + DR_FIELD_OFF), (int)fs_out_pos);
			if(!dn_alias) dpath_a = null;
			if(!backupDirectory(sdnode, dpath_t, dpath_a, iexist, observer)) return false;
		}
		
		return true;
	}
	
	/**
	 * 
	 * @param subpaths
	 * @param observer
	 * @return
	 * @throws IOException 
	 * @since 1.0.0
	 */
	public boolean runBackup(Map<String, String> subpaths, BackupListener observer) throws IOException{
		if(localHost == null) return false;
		
		pathsubs = subpaths;
		if(pathsubs == null) pathsubs = new HashMap<String,String>();
		blacklist = getHostBlacklist();
		fsoffrepl = new HashMap<Integer, Integer>();
		
		//Get the fs dir path.
		String idxfs_dir = this.getRootDirPath() + File.separator
				+ BackupProgramFiles.DN_INDEX + File.separator
				+ BackupProgramFiles.DN_INDEX_FS + File.separator
				+ localHost.getDisplayName();
		
		List<DriveRecord> drives = localHost.getDrives();
		boolean allokay = true;
		for(DriveRecord drive : drives){
			
			//Is drive itself blacklisted? If so, skip.
			if(blacklist.contains(drive.device_path)) continue;
			drive_id = drive.ID;
			
			//Open old fs (if exists)
			String fsbin_path = idxfs_dir + File.separator + drive.name + ".bin";
			String fstpath = fsbin_path + ".tmp";
			DirectoryNode driveroot = null;
			if(FileBuffer.fileExists(fsbin_path)){
				FileBuffer fsbin = FileBuffer.createBuffer(fsbin_path, false);
				int rootchildren = fsbin.intFromFile(4L);
				driveroot = new LoadableDirectoryNode(null, drive.name);
				driveroot.setOffset(8L);
				driveroot.setLength(rootchildren);
				driveroot.getChildCount(); //Force load
			}
			else{
				driveroot = new DirectoryNode(null, drive.name);
				driveroot.setOffset(0L);
			}
			
			//Initialize
			fsoffrepl.clear();
			fs_out_pos = 0L;
			fs_out = new BufferedOutputStream(new FileOutputStream(fstpath));
			
			//Do root
			boolean b = backupDirectory(driveroot, drive.device_path, drive.device_path, true, observer);
			allokay = allokay && b;
			
			//Close fs output stream
			fs_out.close();
			
			//Copy & modify fs 
			LinkedList<Integer> ol = new LinkedList<Integer>();
			ol.addAll(fsoffrepl.keySet());
			Collections.sort(ol);
			long sz = FileBuffer.fileSize(fstpath);
			InputStream fsi = new BufferedInputStream(new FileInputStream(fstpath));
			OutputStream fso = new BufferedOutputStream(new FileOutputStream(fsbin_path));
			
			long cpos = 0;
			long nxtoff = sz;
			if(!ol.isEmpty()) nxtoff = (long)ol.pop();
			while(cpos < sz){
				if(cpos == nxtoff){
					//Replace the next four bytes with the mapped value.
					fsi.read(new byte[4]);
					int val = fsoffrepl.get(nxtoff);
					for(int i = 0; i < 4; i++){
						fso.write((byte)(val & 0xFF));
						val = val >>> 8;
					}
				}
				else{
					//Copy byte.
					fso.write(fsi.read());
					cpos++;
				}
			}
			
			fso.close();
			fsi.close();
			Files.delete(Paths.get(fstpath));
		}
		
		return allokay;
	}
	
}
