package hospelhornbg_backupmulti;

import java.util.ArrayList;

import waffleoRai_Files.tree.DirectoryNode;
import waffleoRai_Files.tree.FileNode;

public class VersionedFileNode extends FileNode{
	
	private ArrayList<Long> subfiles;
	private long parent_offset;

	public VersionedFileNode(DirectoryNode parent, String name) {
		super(parent, name);
	}
	
	public void allocSubfileList(int size){subfiles = new ArrayList<Long>(size);}
	
	public long getParentOffset(){return parent_offset;}
	public void setParentOffset(long off){parent_offset = off;}
	
	public void addSubfile(long uid){
		if(subfiles == null) allocSubfileList(8);
		subfiles.add(uid);
	}
	
	public void clearSubfiles(){
		subfiles.clear();
	}
	
	public int subfileCount(){
		if(subfiles == null) return 0;
		return subfiles.size();
	}
	
	public boolean hasSubfile(long uid){
		if(subfiles == null) return false;
		for(Long id : subfiles){
			if(id == uid) return true;
		}
		return false;
	}
	
	public long[] getSubfiles(){
		if(subfiles == null || subfiles.isEmpty()) return null;
		long[] out = new long[subfiles.size()];
		
		int i = 0;
		for(Long id : subfiles) out[i++] = id;
		
		return out;
	}

}
