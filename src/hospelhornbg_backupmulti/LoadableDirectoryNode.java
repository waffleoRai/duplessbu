package hospelhornbg_backupmulti;

import java.io.IOException;
import java.util.Enumeration;

import javax.swing.tree.TreeNode;

import waffleoRai_Files.tree.DirectoryNode;
import waffleoRai_Files.tree.FileNode;
import waffleoRai_Utils.FileBuffer;

public class LoadableDirectoryNode extends DirectoryNode{

	private boolean isLoaded;
	private long child_offset;
	private long parent_offset;
	
	public LoadableDirectoryNode(DirectoryNode parent, String name) {
		super(parent, name);
		isLoaded = false;
	}
	
	public long getChildrenOffset(){return child_offset;}
	public long getParentOffset(){return parent_offset;}
	public void setChildrenOffset(long off){child_offset = off;}
	public void setParentOffset(long off){parent_offset = off;}
	
	public boolean childrenLoaded(){
		return isLoaded;
	}
	
	public void loadChildren(){
		if(isLoaded) return;
		int ccount = (int)super.getLength();
		
		String binpath = super.getSourcePath();
		//long cpos = super.getOffset();
		long cpos = child_offset;
		
		//Open
		try{
			FileBuffer buff = FileBuffer.createBuffer(binpath, false);
			
			for(int i = 0; i < ccount; i++){
				int flags = Short.toUnsignedInt(buff.shortFromFile(cpos));
				if((flags & 0x8000) != 0){
					//Directory
					DirectoryNode dn  = DriveFileSystem.readDirRecord(this, buff, cpos);
					cpos += dn.getScratchLong();
				}
				else{
					//File
					FileNode fn = DriveFileSystem.readFileRecord(this, buff, cpos);
					cpos += fn.getScratchLong();
				}
			}	
		}
		catch(IOException x){
			x.printStackTrace();
		}
		
		isLoaded = true;
	}
	
	public void unloadChildren(){
		if(!isLoaded) return;
		super.clearChildren();
		isLoaded = false;
	}

	public TreeNode getChildAt(int childIndex) {
		loadChildren(); 
		return super.getChildAt(childIndex);
	}

	public int getChildCount() {
		loadChildren(); 
		return super.getChildCount();
	}

	public int getIndex(TreeNode node) {
		loadChildren(); 
		return super.getIndex(node);
	}

	public boolean isLeaf() {
		loadChildren(); 
		return super.isLeaf();
	}

	public Enumeration<TreeNode> children() {
		loadChildren(); 
		return super.children();
	}

}
