package hospelhornbg_backupmulti;

import waffleoRai_Files.tree.DirectoryNode;
import waffleoRai_Files.tree.FileNode;
import waffleoRai_Utils.BinFieldSize;
import waffleoRai_Utils.FileBuffer;
import waffleoRai_Utils.SerializedString;

public class DriveFileSystem {
	
	public static final String FVER_KEY = "FVER";
	public static final String PARENT_OFF_KEY = "PARDIR_OFF";
	public static final String DATAGUID_KEY = "DATAGUID";
	
	public static FileNode readFileRecord(DirectoryNode parent, FileBuffer data, long offset){
		//Return the record size in the long scratch field
		//Skip flags
		long cpos = offset + 2;
		short version = data.shortFromFile(cpos); cpos += 2;
		int pdiroff = data.intFromFile(cpos); cpos += 4;
		long guid = data.longFromFile(cpos); cpos += 8;
		SerializedString ss = data.readVariableLengthString("UTF8", cpos, BinFieldSize.WORD, 2);
		cpos += ss.getSizeOnDisk();
		String nameraw = ss.getString();
		
		IDSettableFileNode fn = new IDSettableFileNode(parent, nameraw + "@" + Long.toHexString(guid));
		fn.setSourcePath(data.getPath());
		fn.setScratchLong(cpos-offset);
		fn.setOffset(Integer.toUnsignedLong(pdiroff));
		fn.setMetadataValue(FVER_KEY, Short.toString(version));
		fn.setMetadataValue(PARENT_OFF_KEY, Integer.toHexString(pdiroff));
		fn.setMetadataValue(DATAGUID_KEY, Integer.toHexString(pdiroff));
		fn.setUID(guid);
		
		return fn;
	}
	
	public static DirectoryNode readDirRecord(DirectoryNode parent, FileBuffer data, long offset){
		//Return the record size in the long scratch field
		long cpos = offset + 2;
		int ccount = data.intFromFile(cpos); cpos += 4;
		int coff = data.intFromFile(cpos); cpos += 4;
		int poff = data.intFromFile(cpos); cpos += 4;
		
		SerializedString ss = data.readVariableLengthString("UTF8", cpos, BinFieldSize.WORD, 2);
		cpos += ss.getSizeOnDisk();
		String nameraw = ss.getString();
		
		DirectoryNode dn = new DirectoryNode(parent, nameraw);
		dn.setSourcePath(data.getPath());
		dn.setScratchLong(cpos - offset);
		dn.setOffset(Integer.toUnsignedLong(coff));
		dn.setLength(ccount);
		dn.setScratchValue(poff);
		
		dn.setMetadataValue(PARENT_OFF_KEY, Integer.toHexString(poff));
		
		return dn;
	}

}
