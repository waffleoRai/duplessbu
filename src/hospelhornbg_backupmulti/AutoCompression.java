package hospelhornbg_backupmulti;

import waffleoRai_Utils.FileBuffer;

public class AutoCompression {
	
	public static final int AUTOCOMPEXT_NO = 0;
	public static final int AUTOCOMPEXT_YES = 1;
	public static final int AUTOCOMPEXT_IF_REPETITIVE = 2;
	
	public static final String[] AUTOCOMP_EXTS = {"wav", "wave", "sf2", "sf3", "dls",
												  "txt", "text", "in", "out", "err",
												  "java", "h", "hpp", "hxx", "c", "cpp", "cxx",
												  "hh", "cc", "cs", "sh", "bat", "bsh", "js",
												  "json", "py", "r", "xml", "md", "html", "htm",
												  "yml", "yaml", "veg", "veg.bak", "exe", "dll",
												  "iso", "gcm"};
	
	public static long compressionThreshold(){
		return BackupProgramFiles.getCompressionThreshold();
	}
	
	public static int isAutocompExt(String ext){
		if(ext == null || ext.isEmpty()) return AUTOCOMPEXT_NO;
		
		ext = ext.toLowerCase();
		for(String e : AUTOCOMP_EXTS){
			if(e.equals(ext)) return AUTOCOMPEXT_YES;
		}
		
		return AUTOCOMPEXT_NO;
	}
	
	public static boolean autocompressFile(String path){
		long thresh = BackupProgramFiles.getCompressionThreshold();
		if(thresh < 0L) return false;
		long fsz = FileBuffer.fileSize(path);
		if(fsz < 1L) return false;
		if(fsz >= thresh){
			//Check extension.
			path = path.toLowerCase();
			for(String e : AUTOCOMP_EXTS){
				if(path.endsWith("."+e)) return true;
			}
		}
		
		return false;
	}

	//Score file repetition (byte frequencies) to see if there's a lot of easily compressable padding
	public static int getByteDistScoreThreshold(){
		//TODO
		return 0;
	}
	
	public static int scoreByteDist(String filepath){
		//TODO
		return 0;
	}
	
}
