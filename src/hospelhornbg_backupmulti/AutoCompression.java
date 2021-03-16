package hospelhornbg_backupmulti;

public class AutoCompression {
	
	public static final int AUTOCOMPEXT_NO = 0;
	public static final int AUTOCOMPEXT_YES = 1;
	public static final int AUTOCOMPEXT_IF_REPETITIVE = 2;
	
	public static long compressionThreshold(){
		//TODO
		return 0;
	}
	
	public static int isAutocompExt(String ext){
		//TODO
		return 0;
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
