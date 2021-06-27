package hospelhornbg_backupmulti;

import hospelhornbg_backupmulti.gui.GUIMain;

public class TestMain {

	public static void main(String[] args) {
		//Just calls the GUIMain with a specific dir argument
		String targdir = "C:\\Users\\Blythe\\dev\\butest\\backup";
		GUIMain.main(new String[]{targdir});
	}

}
