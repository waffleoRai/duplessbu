package hospelhornbg_backupmulti;

import java.io.IOException;
import java.util.LinkedList;

import hospelhornbg_backupmulti.DeviceRecord.DriveRecord;
import waffleoRai_Files.tree.DirectoryNode;
import waffleoRai_Utils.BinFieldSize;
import waffleoRai_Utils.FileBuffer;
import waffleoRai_Utils.SerializedString;

public class DriveFileSystem {
	
	public static final String FVER_KEY = "FVER";
	public static final String PARENT_OFF_KEY = "PARDIR_OFF";
	public static final String RECORD_OFF_KEY = "REC_OFF";
	public static final String DATAGUID_KEY = "DATAGUID";
	
	private String drive_name;
	private String drive_path;
	private String fs_path;
	private boolean is_win;
	
	public DriveFileSystem(DriveRecord drive, String fs_bin_path, boolean is_windows){
		drive_name = drive.name;
		drive_path = drive.device_path;
		fs_path = fs_bin_path;
		is_win = is_windows;
	}
	
	public String getDriveName(){return drive_name;}
	public String getDrivePath(){return drive_path;}
	
	public String resolveRelativeFSPath(long rec_offset){
		try{
			FileBuffer file = FileBuffer.createBuffer(fs_path, false);
			LinkedList<String> stack = new LinkedList<String>();
			int sballoc = 0;
			long off = rec_offset;
			
			while(off > 0L){
				int flags = Short.toUnsignedInt(file.shortFromFile(off));
				if((flags & 0x8000) != 0){
					//Dir
					LoadableDirectoryNode dn = readDirRecord(null, file, off);
					sballoc += dn.getFileName().length() + 1;
					stack.push(dn.getFileName());
					off = dn.getParentOffset();
				}
				else{
					VersionedFileNode fn = readFileRecord(null, file, off);
					sballoc += fn.getFileName().length() + 1;
					stack.push(fn.getFileName());
					off = fn.getParentOffset();
				}
			}
			
			StringBuilder sb = new StringBuilder(sballoc + 5);
			while(!stack.isEmpty()){
				sb.append('/');
				sb.append(stack.pop());
			}
			
			return sb.toString();
		}
		catch(Exception ex){
			ex.printStackTrace();
			return null;
		}
	}
	
	public String resolveDeviceAbsoluteFSPath(long rec_offset){
		String raw = resolveRelativeFSPath(rec_offset);
		if(raw == null) return null;
		if(!is_win) return raw;

		return drive_path + raw.replace('/', '\\');
	}
	
	public LoadableDirectoryNode loadRoot() throws IOException{
		LoadableDirectoryNode dn = new LoadableDirectoryNode(null, "root");
		dn.setOffset(0);
		dn.setParentOffset(0);
		dn.setChildrenOffset(8L);
		
		//Get child count
		FileBuffer file = FileBuffer.createBuffer(fs_path, false);
		dn.setLength(Integer.toUnsignedLong(file.intFromFile(4L)));
		
		return dn;
	}
	
 	public static VersionedFileNode readFileRecord(DirectoryNode parent, FileBuffer data, long offset){
		//Return the record size in the long scratch field
		long cpos = offset;
		int flags = Short.toUnsignedInt(data.shortFromFile(cpos)); cpos+=2;
		if((flags & 0x8000) != 0) return null; //It's a directory...
		int pdiroff = data.intFromFile(cpos); cpos += 4;
		int vcount = 1;
		if((flags & 0x4000) != 0) vcount = data.intFromFile(cpos); cpos += 4;
		SerializedString ss = data.readVariableLengthString("UTF8", cpos, BinFieldSize.WORD, 2);
		cpos += ss.getSizeOnDisk();
		String nameraw = ss.getString();
		
		VersionedFileNode fn = new VersionedFileNode(parent, nameraw);
		fn.setSourcePath(data.getPath());
		fn.setOffset(offset);
		fn.setParentOffset(pdiroff);
		//fn.setMetadataValue(FVER_KEY, Short.toString(version));
		fn.setMetadataValue(PARENT_OFF_KEY, Integer.toHexString(pdiroff));
		fn.setMetadataValue(DATAGUID_KEY, Integer.toHexString(pdiroff));
		
		fn.allocSubfileList(vcount);
		for(int i = 0; i < vcount; i++){
			fn.addSubfile(data.longFromFile(cpos));
			cpos += 8;
		}
		fn.setScratchLong(cpos-offset);

		return fn;
	}
	
	public static LoadableDirectoryNode readDirRecord(DirectoryNode parent, FileBuffer data, long offset){
		//Return the record size in the long scratch field
		long cpos = offset + 2;
		int poff = data.intFromFile(cpos); cpos += 4;
		int ccount = data.intFromFile(cpos); cpos += 4;
		int coff = data.intFromFile(cpos); cpos += 4;
		
		SerializedString ss = data.readVariableLengthString("UTF8", cpos, BinFieldSize.WORD, 2);
		cpos += ss.getSizeOnDisk();
		String nameraw = ss.getString();
		
		//DirectoryNode dn = new DirectoryNode(parent, nameraw);
		LoadableDirectoryNode dn = new LoadableDirectoryNode(parent, nameraw);
		dn.setSourcePath(data.getPath());
		dn.setScratchLong(cpos - offset);
		//dn.setOffset(Integer.toUnsignedLong(coff));
		dn.setChildrenOffset(Integer.toUnsignedLong(coff));
		dn.setParentOffset(Integer.toUnsignedLong(poff));
		dn.setOffset(offset);
		dn.setLength(ccount);
		dn.setScratchValue(poff);
		
		dn.setMetadataValue(PARENT_OFF_KEY, Integer.toHexString(poff));
		dn.setMetadataValue(RECORD_OFF_KEY, Long.toHexString(offset));
		
		return dn;
	}
	
	public static FileBuffer serializeNode(VersionedFileNode fn){
		if(fn == null) return null;
		String name = fn.getFileName();
		int vcount = fn.subfileCount();
		int alloc = 13 + (name.length() << 2) + (vcount << 3);
		FileBuffer buff = new FileBuffer(alloc, false);
		
		int flags = 0;
		if(vcount > 1) flags |= 0x4000;
		buff.addToFile((short)flags);
		/*DirectoryNode parent = fn.getParent();
		if(parent == null) buff.addToFile(0);
		else buff.addToFile((int)parent.getOffset());*/
		buff.addToFile((int)fn.getParentOffset());
		if(vcount > 1) buff.addToFile(vcount);
		buff.addVariableLengthString("UTF8", name, BinFieldSize.WORD, 2);
		long[] ids = fn.getSubfiles();
		for(long l : ids) buff.addToFile(l);
		
		return buff;
	}

}
