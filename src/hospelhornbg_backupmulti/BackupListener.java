package hospelhornbg_backupmulti;

public interface BackupListener {

	public void setHashCopySize(long size);
	public void updateHashCopyProgress(long bytesProcessed);
	public void setCopySize(long size);
	public void updateCopyProgress(long bytesProcessed);
	public void setTotalEstimatedFiles(int count);
	public void incrementProcessedFileCount();
	public void onStartFileProcessing(String file_path);
	public void onStartDirectoryProcessing(String dir_path);
	
	public void onCancelRequest();
	public void onCancelAcknowledge();
	public void onFinishSuccess();
	public void onFinishFailure();
	
}
