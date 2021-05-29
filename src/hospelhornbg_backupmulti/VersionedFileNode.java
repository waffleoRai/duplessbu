package hospelhornbg_backupmulti;

import java.util.ArrayList;

import waffleoRai_Files.tree.DirectoryNode;
import waffleoRai_Files.tree.FileNode;

public class VersionedFileNode extends FileNode{
	
	private ArrayList<Long> subfiles;

	public VersionedFileNode(DirectoryNode parent, String name) {
		super(parent, name);
	}
	
	public void allocSubfileList(int size){subfiles = new ArrayList<Long>(size);}
	
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
	
	public long[] getSubfiles(){
		if(subfiles == null || subfiles.isEmpty()) return null;
		long[] out = new long[subfiles.size()];
		
		int i = 0;
		for(Long id : subfiles) out[i++] = id;
		
		return out;
	}

}
