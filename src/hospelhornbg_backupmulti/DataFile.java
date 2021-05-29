package hospelhornbg_backupmulti;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import waffleoRai_Files.tree.FileNode;
import waffleoRai_Utils.FileBuffer;

public class DataFile implements Comparable<DataFile>{
	
	//Record in the main GUID index
	
	/*----- Constants -----*/
	
	public static final long GIGABYTE = 0x40000000;
	public static final long MEGABYTE = 0x100000;
	public static final long KILOBYTE = 0x400;
	
	/*----- Instance Variables -----*/
	
	private boolean is_compressed;
	private long guid;
	private long timestamp_raw;
	private ZonedDateTime timestamp;
	private long file_size;
	private byte[] hash;
	private Map<Integer, List<DeviceFile>> device_files;
	
	/*----- Inner Classes -----*/
	
	public static class DeviceFile{
		public short drive_id;
		
		//Different ways to link
		public int fs_offset;
		public FileNode node;
		public String dev_path;
		
		protected DeviceFile(){}
		
		public DeviceFile(short drive, String path){
			drive_id = drive; dev_path = path;
		}
		
	}

	/*----- Construction/Parsing -----*/
	
	private DataFile(){
		device_files = new TreeMap<Integer, List<DeviceFile>>();
	}
	
	public static DataFile generateDataFileRecord(String sourcepath) throws IOException{
		File f = new File(sourcepath);
		if(!f.isFile()) throw new IOException("File \"" + sourcepath + "\" does not exist!");
		
		DataFile rec = new DataFile();
		rec.file_size = f.length();
		rec.timestamp_raw = f.lastModified();
		rec.timestamp = ZonedDateTime.ofInstant(Instant.ofEpochMilli(rec.timestamp_raw), ZoneId.systemDefault());
		
		//Hash :|
		try {
			rec.hash = FileBuffer.getFileHash(BackupProgramFiles.HASH_ALGO, sourcepath);
		} 
		catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			throw new IOException("File \"" + sourcepath + "\" hash failed");
		}
		
		rec.guid = 0L;
		for(int i = 0; i < 8; i++){
			rec.guid = rec.guid << 8;
			rec.guid |= (Byte.toUnsignedLong(rec.hash[i]));
		}
		
		//Determine whether to compress
		if(rec.file_size >= AutoCompression.compressionThreshold()){
			int dot = sourcepath.lastIndexOf('.');
			if(dot >= 0){
				int autoc = AutoCompression.isAutocompExt(sourcepath.substring(dot+1));
				if(autoc == AutoCompression.AUTOCOMPEXT_YES) rec.is_compressed = true;
				else if (autoc == AutoCompression.AUTOCOMPEXT_IF_REPETITIVE){
					int rthresh = AutoCompression.getByteDistScoreThreshold();
					int rscore = AutoCompression.scoreByteDist(sourcepath);
					rec.is_compressed = (rscore >= rthresh);
				}
			}
		}
		
		//Note file path
		List<DeviceFile> dlist = new LinkedList<DeviceFile>();
		dlist.add(new DeviceFile(BackupProgramFiles.currentDriveID(), sourcepath));
		rec.device_files.put(BackupProgramFiles.currentDevice().getID(), dlist);
		
		return rec;
	}
	
	public static DataFile generateDataFileRecord(Path sourcepath){
		return generateDataFileRecord(sourcepath.toAbsolutePath());
	}
	
	public static DataFile parseDataFileRecord(FileBuffer dat, long stoff) throws IOException{
		if(dat == null) throw new IOException("Provided data ref is null");
		DataFile rec = new DataFile();
		
		dat.setEndian(false);
		dat.setCurrentPosition(stoff);
		
		int flags = Short.toUnsignedInt(dat.nextShort());
		if((flags & 0x8000) != 0) rec.is_compressed = true;
		dat.nextInt(); //Skip size
		rec.guid = dat.nextLong();
		rec.timestamp_raw = dat.nextLong();
		rec.timestamp = ZonedDateTime.ofInstant(Instant.ofEpochMilli(rec.timestamp_raw), ZoneId.systemDefault());
		rec.file_size = dat.nextLong();
		
		long cpos = dat.getCurrentPosition();
		rec.hash = dat.getBytes(cpos, cpos + 20);
		dat.skipBytes(20);
		
		int dev_count = Short.toUnsignedInt(dat.nextShort());
		for(int i = 0; i < dev_count; i++){
			int duid = dat.nextInt();
			int fcount = Short.toUnsignedInt(dat.nextShort());
			
			List<DeviceFile> flist = new LinkedList<DeviceFile>();
			for(int j = 0; j < fcount; j++){
				DeviceFile f = new DeviceFile();
				flist.add(f);
				f.drive_id = dat.nextShort();
				f.fs_offset = dat.nextInt();
			}
			
			rec.device_files.put(duid, flist);
		}
		
		return rec;
	}
	
	/*----- Serialization -----*/
	
	public int getSerializedRecordSize(){
		int ct = 2 + 4 + 24 + 20 + 2; //Common fields
		int devcount = device_files.size();
		int fcount = countLinkedDeviceFiles();
		
		//6 bytes for every device, an 6 bytes for every file
		ct += 6 * devcount;
		ct += 6 * fcount;
		
		return ct;
	}
	
	/*----- Getters -----*/
	
	public int countLinkedDeviceFiles(){
		//TODO
		return 0;
	}
	
	/*----- Display -----*/
	
	public String decSizeString(){

		if(file_size >= GIGABYTE){
			double sz = (double)file_size/(double)GIGABYTE;
			return String.format("%.2f GiB", sz);
		}
		else if(file_size >= MEGABYTE){
			double sz = (double)file_size/(double)MEGABYTE;
			return String.format("%.2f MiB", sz);
		}
		else if(file_size >= KILOBYTE){
			double sz = (double)file_size/(double)KILOBYTE;
			return String.format("%.2f KiB", sz);
		}
		
		return file_size + " bytes";
	}
	
	public String toString(){
		StringBuilder sb = new StringBuilder(256);
		sb.append(Long.toHexString(guid));
		sb.append(" ");
		sb.append(timestamp.getYear());
		sb.append("-");
		sb.append(String.format("%02d", timestamp.getMonthValue()));
		sb.append("-");
		sb.append(String.format("%02d", timestamp.getDayOfMonth()));
		sb.append(" ");
		sb.append(decSizeString());
		
		return sb.toString();
	}
	
	/*----- Setters -----*/
	
	/*----- Compare -----*/
	
	public int hashCode(){
		return (int)guid;
	}
	
	public boolean equals(Object other){
		if(other == null) return false;
		if(!(other instanceof DataFile)) return false;
		return this.guid == ((DataFile)other).guid;
	}

	public int compareTo(DataFile o) {
		if(o == null) return -1;
		if(this.guid > o.guid) return 1;
		if(this.guid < o.guid) return -1;
		return 0;
	}
	
}
