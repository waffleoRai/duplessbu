package hospelhornbg_backupmulti;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.util.zip.DeflaterOutputStream;

import waffleoRai_Utils.FileBuffer;

public class BackupProgramFiles {
	
	public static final String HASH_ALGO = "SHA-1";
	
	public static final String DN_INDEX = "_index";
	public static final String DN_DATA = "_data";
	public static final String DN_INDEX_FS = "_fs";
	
	public static final String FN_SETTINGS_BUDRIVE = "settings.ini";
	public static final String FN_INDEX_DEVTBL = "devs.bin";
	public static final String FN_INDEX_DEVBL = "blacklist.txt";
	
	public static final int OSENUM_UNK = 0;
	public static final int OSENUM_WIN = 1;
	public static final int OSENUM_LINUX = 2;
	public static final int OSENUM_MACOS = 3;
	
	private static BackupManager current_manager = null;
	
	public static BackupManager loadManagerFrom(String dir_path) throws IOException{
		//If path null, calls the constructor overload that checks from reflection
		if(dir_path == null || dir_path.isEmpty()) current_manager = new BackupManager();
		else current_manager = new BackupManager(dir_path);
		return current_manager;
	}
	
	public static BackupManager getActiveManager(){return current_manager;}
	
	public static String getSysDefaultSettingsDirectory(){
		//TODO
		return null;
	}
	
	public static String getBackupBaseDirectory(){
		//Current directory to backup root directory
		/*If not set manually (in main() arguments), it will try to
		 * detect the directory the BackupProgramFiles class was loaded from
		 * (ie. the directory containing the JAR)
		 * */
		if(current_manager == null) return null;
		return current_manager.getRootDirPath();
	}
	
	public static boolean alwaysCheckHash(){
		if(current_manager == null) return false;
		return current_manager.getHashAll();
	}
	
	public static long getCompressionThreshold(){
		//TODO
		return 0L;
	}
	
	public static DeviceRecord currentDevice(){
		if(current_manager == null) return null;
		return current_manager.getCurrentHost();
	}

	public static short currentDriveID(){
		//TODO
		return 0;
	}
	
	public static long hash2UID(byte[] hash){
		if(hash == null || hash.length < 8) return 0L;
		long uid = 0L;
		for(int i = 0; i < 8; i++){
			uid <<= 8;
			uid |= Byte.toUnsignedLong(hash[i]);
		}
		return uid;
	}
	
	public static byte[] getFileHash(String path, BackupListener observer){

		try{
			long size = FileBuffer.fileSize(path);
			if(observer != null) observer.setHashCopySize(size);
			BufferedInputStream bis = new BufferedInputStream(new FileInputStream(path));
			MessageDigest sha = MessageDigest.getInstance(HASH_ALGO);
			
			long ct = 0;
			byte[] buff = new byte[0x100];
			while(ct < size){
				int read = bis.read(buff);
				sha.update(buff, 0, read);
				ct += size;
				if(observer != null) observer.updateHashCopyProgress(ct);
			}

			bis.close();
			byte[] hash = sha.digest();
			return hash;
		}
		catch(Exception x){
			x.printStackTrace();
			return null;
		}
	}
	
	public static byte[] hashAndCopy(String srcpath, String dstpath, boolean deflate, BackupListener observer){
		try{
			OutputStream os = null;
			BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(dstpath));
			if(deflate){
				os = new DeflaterOutputStream(bos);
			}
			else os = bos;
			
			long size = FileBuffer.fileSize(srcpath);
			if(observer != null) observer.setHashCopySize(size);
			BufferedInputStream bis = new BufferedInputStream(new FileInputStream(srcpath));
			MessageDigest sha = MessageDigest.getInstance(HASH_ALGO);
			
			long ct = 0;
			byte[] buff = new byte[0x100];
			while(ct < size){
				int read = bis.read(buff);
				sha.update(buff, 0, read);
				os.write(buff, 0, read);
				ct += size;
				if(observer != null) observer.updateHashCopyProgress(ct);
			}

			os.close();
			bis.close();
			byte[] hash = sha.digest();
			return hash;
		}
		catch(Exception x){
			x.printStackTrace();
			return null;
		}
	}
	
	public static void copyToBackup(String srcpath, String dstpath, boolean deflate, BackupListener observer){
		//TODO
	}
	
	public static void shutDownManager(){
		//TODO
	}
	
}
