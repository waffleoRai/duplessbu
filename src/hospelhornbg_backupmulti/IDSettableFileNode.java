package hospelhornbg_backupmulti;

import waffleoRai_Files.tree.DirectoryNode;
import waffleoRai_Files.tree.FileNode;

public class IDSettableFileNode extends FileNode{

	public IDSettableFileNode(DirectoryNode parent, String name) {
		super(parent, name);
	}
	
	public void setUID(long value){
		super.setGUID(value);
	}

}
