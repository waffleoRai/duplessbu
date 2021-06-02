package hospelhornbg_backupmulti;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import waffleoRai_Utils.BinFieldSize;
import waffleoRai_Utils.FileBuffer;
import waffleoRai_Utils.MultiFileBuffer;
import waffleoRai_Utils.SerializedString;

public class DeviceRecord {
	
	public static final int SERIAL_VERSION = 1;
	
	/*----- Instance Variables -----*/
	
	private int dev_id;
	private String dev_name;
	private int os_enum;
	private List<DriveRecord> drives;
	
	/*----- Inner Classes -----*/
	
	public static class DriveRecord{
		public short ID;
		public String name;
		public String device_path;
		public String blacklist_path;
	}
	
	/*----- Construction -----*/
	
	public DeviceRecord(int id, int drive_count){
		dev_id = id;
		dev_name = "DEVICE_" + Integer.toHexString(dev_id);
		drive_count = Math.max(drive_count, 1);
		drives = new ArrayList<DriveRecord>(drive_count+1);
	}
	
	public static DeviceRecord readRecordFrom(FileBuffer file, int version){
		//Skip record size
		file.skipBytes(4);
		int id = file.nextInt();
		int flags = Short.toUnsignedInt(file.nextShort());
		SerializedString ss = file.readVariableLengthString("UTF8", file.getCurrentPosition(), BinFieldSize.WORD, 2);
		String name = ss.getString();
		file.skipBytes(ss.getSizeOnDisk());
		int dcount = Short.toUnsignedInt(file.nextShort());
		
		DeviceRecord rec = new DeviceRecord(id, dcount);
		rec.dev_name = name;
		rec.os_enum = (flags & 0xF);
		
		for(int i = 0; i < dcount; i++){
			DriveRecord drive = new DriveRecord();
			drive.ID = file.nextShort();
			ss = file.readVariableLengthString("UTF8", file.getCurrentPosition(), BinFieldSize.WORD, 2);
			drive.name = ss.getString();
			file.skipBytes(ss.getSizeOnDisk());
			ss = file.readVariableLengthString("UTF8", file.getCurrentPosition(), BinFieldSize.WORD, 2);
			drive.device_path = ss.getString();
			file.skipBytes(ss.getSizeOnDisk());
			
			drive.blacklist_path = "/" + BackupProgramFiles.DN_INDEX + 
					"/" + BackupProgramFiles.DN_INDEX_FS + 
					"/" + rec.dev_name + "/blacklist.txt";
		}
		
		return rec;
	}
	
	public static DeviceRecord readRecordFrom(FileBuffer file, long pos, int version){
		long oldpos = file.getCurrentPosition();
		file.setCurrentPosition(pos);
		DeviceRecord rec = readRecordFrom(file, version);
		file.setCurrentPosition(oldpos);
		return rec;
	}
	
	/*----- Getters -----*/
	
	public int getID(){return dev_id;}
	
	public String getDisplayName(){return this.dev_name;}
	
	public int getOSEnum(){return os_enum;}
	
	public List<DriveRecord> getDrives(){
		if(drives == null || drives.isEmpty()) new LinkedList<DriveRecord>();
		List<DriveRecord> list = new ArrayList<DriveRecord>(drives.size());
		list.addAll(drives);
		return list;
	}
	
	/*----- Setters -----*/
	
	public void setDisplayName(String str){this.dev_name = str;}
	
	public void addDrive(String name, String dev_path){
		DriveRecord rec = new DriveRecord();
		rec.name = name;
		rec.device_path = dev_path;
		rec.ID = (short)(new Random().nextInt());
		
		drives.add(rec);
	}

	/*----- Serialize -----*/
	
	public FileBuffer serializeMe(){
		//Do the drives first
		int drsize = 0;
		int dcount = drives.size();
		List<FileBuffer> ddat = new LinkedList<FileBuffer>();
		
		for(DriveRecord dr : drives){
			if(dr.device_path == null) continue;
			if(dr.name == null){
				dr.name = "DRIVE_" + Integer.toHexString(dr.ID);
			}
			int cap = 2 + 6 + (dr.name.length() << 2) + (dr.device_path.length() << 2);
			FileBuffer buff = new FileBuffer(cap, true);
			buff.addToFile(dr.ID);
			buff.addVariableLengthString("UTF8", dr.name, BinFieldSize.WORD, 2);
			buff.addVariableLengthString("UTF8", dr.device_path, BinFieldSize.WORD, 2);
			drsize += buff.getFileSize();
			ddat.add(buff);
		}
		
		dcount = ddat.size();
		
		FileBuffer devmain = new FileBuffer(12 + (dev_name.length() << 2) + 3, true);
		devmain.addToFile(0); //Replace with block size at end.
		devmain.addToFile(dev_id);
		int flags = os_enum & 0xF;
		devmain.addToFile((short)flags);
		devmain.addVariableLengthString("UTF8", dev_name, BinFieldSize.WORD, 2);
		devmain.addToFile((short)dcount);
		drsize += (devmain.getFileSize() - 4);
		devmain.replaceInt(drsize, 0L);
		
		MultiFileBuffer out = new MultiFileBuffer(dcount+1);
		out.addToFile(devmain);
		for(FileBuffer buff : ddat) out.addToFile(buff);
		
		return out;
	}
	
	/*----- Misc -----*/
	
	public String toString(){
		StringBuilder sb = new StringBuilder(1024);
		sb.append(dev_name);
		switch(os_enum){
		case BackupProgramFiles.OSENUM_WIN: 
			sb.append(" [WINDOWS]");
			break;
		case BackupProgramFiles.OSENUM_LINUX: 
			sb.append(" [LINUX]");
			break;
		case BackupProgramFiles.OSENUM_MACOS: 
			sb.append(" [MACOS]");
			break;
		}
		return sb.toString();
	}
	
}
