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
		int pdiroff = data.intFromFile(cpos); cpos += 4;
		int vcount = data.intFromFile(cpos); cpos += 4;
		SerializedString ss = data.readVariableLengthString("UTF8", cpos, BinFieldSize.WORD, 2);
		cpos += ss.getSizeOnDisk();
		String nameraw = ss.getString();
		
		VersionedFileNode fn = new VersionedFileNode(parent, nameraw);
		fn.setSourcePath(data.getPath());
		fn.setOffset(offset);
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
	
	public static DirectoryNode readDirRecord(DirectoryNode parent, FileBuffer data, long offset){
		//Return the record size in the long scratch field
		long cpos = offset + 2;
		int poff = data.intFromFile(cpos); cpos += 4;
		int ccount = data.intFromFile(cpos); cpos += 4;
		int coff = data.intFromFile(cpos); cpos += 4;
		
		SerializedString ss = data.readVariableLengthString("UTF8", cpos, BinFieldSize.WORD, 2);
		cpos += ss.getSizeOnDisk();
		String nameraw = ss.getString();
		
		//DirectoryNode dn = new DirectoryNode(parent, nameraw);
		DirectoryNode dn = new LoadableDirectoryNode(parent, nameraw);
		dn.setSourcePath(data.getPath());
		dn.setScratchLong(cpos - offset);
		dn.setOffset(Integer.toUnsignedLong(coff));
		dn.setLength(ccount);
		dn.setScratchValue(poff);
		
		dn.setMetadataValue(PARENT_OFF_KEY, Integer.toHexString(poff));
		
		return dn;
	}
	
	public static FileBuffer serializeNode(VersionedFileNode fn){
		if(fn == null) return null;
		String name = fn.getFileName();
		int vcount = fn.subfileCount();
		int alloc = 13 + (name.length() << 2) + (vcount << 3);
		FileBuffer buff = new FileBuffer(alloc, false);
		
		int flags = 0;
		if(vcount > 0) flags |= 0x4000;
		buff.addToFile((short)flags);
		DirectoryNode parent = fn.getParent();
		if(parent == null) buff.addToFile(0);
		else buff.addToFile((int)parent.getOffset());
		buff.addToFile(vcount);
		buff.addVariableLengthString("UTF8", name, BinFieldSize.WORD, 2);
		long[] ids = fn.getSubfiles();
		for(long l : ids) buff.addToFile(l);
		
		return buff;
	}

}
