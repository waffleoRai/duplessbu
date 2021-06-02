package hospelhornbg_backupmulti;

public interface BackupListener {

	public void setHashCopySize(long size);
	public void updateHashCopyProgress(long bytesProcessed);
	
}
