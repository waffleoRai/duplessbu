package hospelhornbg_backupmulti;

import java.util.List;

public class DeviceRecord {
	
	/*----- Instance Variables -----*/
	
	private int dev_id;
	private String dev_name;
	private List<DriveRecord> drives;
	
	/*----- Inner Classes -----*/
	
	public static class DriveRecord{
		public short ID;
		public String name;
		public String device_path;
		public String blacklist_path;
	}
	
	/*----- Construction -----*/
	
	/*----- Getters -----*/
	
	public int getID(){return dev_id;}
	
	/*----- Setters -----*/

}
