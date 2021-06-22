package hospelhornbg_backupmulti.gui;

import javax.swing.JFrame;
import java.awt.GridBagLayout;
import javax.swing.JPanel;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JTabbedPane;
import javax.swing.JScrollPane;
import javax.swing.border.BevelBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import hospelhornbg_backupmulti.BackupManager;
import hospelhornbg_backupmulti.DataFile;
import hospelhornbg_backupmulti.DataFile.DeviceFile;
import hospelhornbg_backupmulti.DeviceRecord;
import hospelhornbg_backupmulti.VersionedFileNode;
import hospelhornbg_backupmulti.gui.progress_dialogs.BackupProgressDialog;
import hospelhornbg_backupmulti.gui.progress_dialogs.IndefProgressDialog;
import waffleoRai_Files.tree.DirectoryNode;
import waffleoRai_Files.tree.FileNode;

import javax.swing.JTree;
import javax.swing.SwingWorker;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JTextField;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JTextPane;

public class BUExForm extends JFrame{

	/*----- Constants -----*/
	
	private static final long serialVersionUID = -8925831783995294784L;
	
	public static final int MIN_WIDTH = 430;
	public static final int MIN_HEIGHT = 330;
	
	/*----- Instance Variables -----*/
	
	private DirectoryNode mtree;
	
	private JTextField txtSearch; //Search all files in backup system by name
	private JList<String> lstSearch; //File name search results
	private JTree tree; //Tree view of backup files
	
	private JList<DataFile> lstFiles; //List of files associated with node (like, various versions)
	private JTextPane txtFileInfo; //Info on specific file/version
	
	private BackupManager manager;
	private Map<String, Collection<Long>> search_results;
	
	private TreePath tree_last_clicked;
	//private String list_last_clicked;
	
	/*----- Initialization -----*/
	
	public BUExForm(BackupManager bumng) {
		manager = bumng;
		this.setMinimumSize(new Dimension(MIN_WIDTH, MIN_HEIGHT));
		this.setPreferredSize(new Dimension(MIN_WIDTH, MIN_HEIGHT));
		
		search_results = new HashMap<String, Collection<Long>>();
		
		initGUI();
		loadManagerView();
	}

	private void initGUI(){
		setTitle("Multi Backup");
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{0, 0, 0};
		gridBagLayout.rowHeights = new int[]{0, 25, 0};
		gridBagLayout.columnWeights = new double[]{1.0, 1.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{1.0, 0.0, Double.MIN_VALUE};
		getContentPane().setLayout(gridBagLayout);
		
		JTabbedPane tabPane = new JTabbedPane(JTabbedPane.TOP);
		GridBagConstraints gbc_tabPane = new GridBagConstraints();
		gbc_tabPane.insets = new Insets(0, 0, 5, 5);
		gbc_tabPane.fill = GridBagConstraints.BOTH;
		gbc_tabPane.gridx = 0;
		gbc_tabPane.gridy = 0;
		getContentPane().add(tabPane, gbc_tabPane);
		
		JPanel pnlTree = new JPanel();
		tabPane.addTab("Browse", null, pnlTree, null);
		GridBagLayout gbl_pnlTree = new GridBagLayout();
		gbl_pnlTree.columnWidths = new int[]{0, 0};
		gbl_pnlTree.rowHeights = new int[]{0, 0};
		gbl_pnlTree.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gbl_pnlTree.rowWeights = new double[]{1.0, Double.MIN_VALUE};
		pnlTree.setLayout(gbl_pnlTree);
		
		JScrollPane spTree = new JScrollPane();
		spTree.setViewportBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		GridBagConstraints gbc_spTree = new GridBagConstraints();
		gbc_spTree.insets = new Insets(5, 5, 5, 5);
		gbc_spTree.fill = GridBagConstraints.BOTH;
		gbc_spTree.gridx = 0;
		gbc_spTree.gridy = 0;
		pnlTree.add(spTree, gbc_spTree);
		
		tree = new JTree();
		spTree.setViewportView(tree);
		tree.addMouseListener(new MouseAdapter(){
			public void mousePressed(MouseEvent e){
				if(e.getButton() == MouseEvent.BUTTON1){
					TreePath sel = tree.getSelectionPath();
					if(tree_last_clicked != null && sel != null){
						if(tree_last_clicked.equals(sel)){
							action_onViewTreeNode();
						}
					}
					tree_last_clicked = sel;
				}
			}
		});
		
		JPanel pnlSearch = new JPanel();
		tabPane.addTab("Search", null, pnlSearch, null);
		GridBagLayout gbl_pnlSearch = new GridBagLayout();
		gbl_pnlSearch.columnWidths = new int[]{0, 0};
		gbl_pnlSearch.rowHeights = new int[]{25, 0, 0};
		gbl_pnlSearch.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gbl_pnlSearch.rowWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
		pnlSearch.setLayout(gbl_pnlSearch);
		
		JPanel pnlSearchbox = new JPanel();
		GridBagConstraints gbc_pnlSearchbox = new GridBagConstraints();
		gbc_pnlSearchbox.insets = new Insets(0, 0, 5, 0);
		gbc_pnlSearchbox.fill = GridBagConstraints.BOTH;
		gbc_pnlSearchbox.gridx = 0;
		gbc_pnlSearchbox.gridy = 0;
		pnlSearch.add(pnlSearchbox, gbc_pnlSearchbox);
		GridBagLayout gbl_pnlSearchbox = new GridBagLayout();
		gbl_pnlSearchbox.columnWidths = new int[]{0, 0, 0};
		gbl_pnlSearchbox.rowHeights = new int[]{0, 0};
		gbl_pnlSearchbox.columnWeights = new double[]{1.0, 0.0, Double.MIN_VALUE};
		gbl_pnlSearchbox.rowWeights = new double[]{0.0, Double.MIN_VALUE};
		pnlSearchbox.setLayout(gbl_pnlSearchbox);
		
		txtSearch = new JTextField();
		GridBagConstraints gbc_txtSearch = new GridBagConstraints();
		gbc_txtSearch.insets = new Insets(5, 5, 5, 5);
		gbc_txtSearch.fill = GridBagConstraints.HORIZONTAL;
		gbc_txtSearch.gridx = 0;
		gbc_txtSearch.gridy = 0;
		pnlSearchbox.add(txtSearch, gbc_txtSearch);
		txtSearch.setColumns(10);
		
		JButton btnGo = new JButton("Go");
		GridBagConstraints gbc_btnGo = new GridBagConstraints();
		gbc_btnGo.insets = new Insets(5, 0, 5, 5);
		gbc_btnGo.fill = GridBagConstraints.BOTH;
		gbc_btnGo.gridx = 1;
		gbc_btnGo.gridy = 0;
		pnlSearchbox.add(btnGo, gbc_btnGo);
		btnGo.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				action_onSearchGo();
			}
		});
		
		JScrollPane spSearchList = new JScrollPane();
		spSearchList.setViewportBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		GridBagConstraints gbc_spSearchList = new GridBagConstraints();
		gbc_spSearchList.insets = new Insets(0, 5, 5, 5);
		gbc_spSearchList.fill = GridBagConstraints.BOTH;
		gbc_spSearchList.gridx = 0;
		gbc_spSearchList.gridy = 1;
		pnlSearch.add(spSearchList, gbc_spSearchList);
		
		lstSearch = new JList<String>();
		spSearchList.setViewportView(lstSearch);
		lstSearch.addListSelectionListener(new ListSelectionListener(){
			@Override
			public void valueChanged(ListSelectionEvent e) {
				action_onViewSearchNode();
			}
		});
		
		JPanel pnlFile = new JPanel();
		GridBagConstraints gbc_pnlFile = new GridBagConstraints();
		gbc_pnlFile.insets = new Insets(0, 0, 5, 0);
		gbc_pnlFile.fill = GridBagConstraints.BOTH;
		gbc_pnlFile.gridx = 1;
		gbc_pnlFile.gridy = 0;
		getContentPane().add(pnlFile, gbc_pnlFile);
		GridBagLayout gbl_pnlFile = new GridBagLayout();
		gbl_pnlFile.columnWidths = new int[]{0, 0, 0, 0};
		gbl_pnlFile.rowHeights = new int[]{0, 0, 0, 0};
		gbl_pnlFile.columnWeights = new double[]{1.0, 0.0, 0.0, Double.MIN_VALUE};
		gbl_pnlFile.rowWeights = new double[]{1.0, 1.0, 0.0, Double.MIN_VALUE};
		pnlFile.setLayout(gbl_pnlFile);
		
		JScrollPane spFileList = new JScrollPane();
		spFileList.setViewportBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		GridBagConstraints gbc_spFileList = new GridBagConstraints();
		gbc_spFileList.gridwidth = 3;
		gbc_spFileList.insets = new Insets(5, 5, 5, 5);
		gbc_spFileList.fill = GridBagConstraints.BOTH;
		gbc_spFileList.gridx = 0;
		gbc_spFileList.gridy = 0;
		pnlFile.add(spFileList, gbc_spFileList);
		
		lstFiles = new JList<DataFile>();
		spFileList.setViewportView(lstFiles);
		lstFiles.addListSelectionListener(new ListSelectionListener(){
			@Override
			public void valueChanged(ListSelectionEvent e) {
				action_onViewListSelection();
			}
		});
		
		JScrollPane spFileInfo = new JScrollPane();
		GridBagConstraints gbc_spFileInfo = new GridBagConstraints();
		gbc_spFileInfo.gridwidth = 3;
		gbc_spFileInfo.insets = new Insets(5, 5, 5, 5);
		gbc_spFileInfo.fill = GridBagConstraints.BOTH;
		gbc_spFileInfo.gridx = 0;
		gbc_spFileInfo.gridy = 1;
		pnlFile.add(spFileInfo, gbc_spFileInfo);
		
		txtFileInfo = new JTextPane();
		txtFileInfo.setEditable(false);
		spFileInfo.setViewportView(txtFileInfo);
		
		JButton btnExtract = new JButton("Extract...");
		GridBagConstraints gbc_btnExtract = new GridBagConstraints();
		gbc_btnExtract.insets = new Insets(5, 0, 0, 5);
		gbc_btnExtract.gridx = 2;
		gbc_btnExtract.gridy = 2;
		pnlFile.add(btnExtract, gbc_btnExtract);
		btnExtract.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				action_onExtract();
			}
		});
		
		JPanel pnlButtons = new JPanel();
		GridBagConstraints gbc_pnlButtons = new GridBagConstraints();
		gbc_pnlButtons.gridwidth = 2;
		gbc_pnlButtons.insets = new Insets(0, 5, 5, 5);
		gbc_pnlButtons.fill = GridBagConstraints.BOTH;
		gbc_pnlButtons.gridx = 0;
		gbc_pnlButtons.gridy = 1;
		getContentPane().add(pnlButtons, gbc_pnlButtons);
		GridBagLayout gbl_pnlButtons = new GridBagLayout();
		gbl_pnlButtons.columnWidths = new int[]{0, 0, 0, 0, 0};
		gbl_pnlButtons.rowHeights = new int[]{0, 0};
		gbl_pnlButtons.columnWeights = new double[]{0.0, 0.0, 1.0, 0.0, Double.MIN_VALUE};
		gbl_pnlButtons.rowWeights = new double[]{0.0, Double.MIN_VALUE};
		pnlButtons.setLayout(gbl_pnlButtons);
		
		JButton btnSettings = new JButton("Settings...");
		GridBagConstraints gbc_btnSettings = new GridBagConstraints();
		gbc_btnSettings.insets = new Insets(0, 0, 0, 5);
		gbc_btnSettings.gridx = 0;
		gbc_btnSettings.gridy = 0;
		pnlButtons.add(btnSettings, gbc_btnSettings);
		btnSettings.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				action_onSettings();
			}
		});
		
		JButton btnRun = new JButton("Run Backup");
		GridBagConstraints gbc_btnRun = new GridBagConstraints();
		gbc_btnRun.gridx = 3;
		gbc_btnRun.gridy = 0;
		pnlButtons.add(btnRun, gbc_btnRun);
		btnRun.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				action_onRunBackup();
			}
		});
	}
	
	/*----- GUI View -----*/
	
	private void loadManagerView(){
		//Loads the tree view etc.
		//Clear search
		txtSearch.setText(""); txtSearch.repaint();
		clearSearchList();
		
		//Load tree
		if(manager == null) tree.setModel(new DefaultTreeModel(new DirectoryNode(null, "backup")));
		else{
			try{
				mtree = manager.getBackupTree();
				DefaultTreeModel tree_mdl = new DefaultTreeModel(mtree);
				tree.setModel(tree_mdl);
			}
			catch(IOException ex){
				showError("I/O Error: Backup file system tree could not be loaded!");
				tree.setModel(new DefaultTreeModel(new DirectoryNode(null, "backup")));
			}		
		}
		tree.repaint();
		
		//Clear other panels
		txtFileInfo.setText(""); txtFileInfo.repaint();
		lstFiles.setModel(new DefaultListModel<DataFile>());
		lstFiles.repaint();
	
	}
	
	private void clearSearchList(){
		search_results.clear();
		lstSearch.setModel(new DefaultListModel<String>());
		lstSearch.repaint();
	}
	
	private void loadDatafileInfoText(){
		DataFile df = getSelectedDataFile();
		if(df == null){
			txtFileInfo.setText("");
			txtFileInfo.repaint();
			return;
		}
		StringBuilder sb = new StringBuilder(4096);
		
		sb.append(String.format("%016x\n", df.getGUID()));
		sb.append("Last Modified: ");
		sb.append(df.getTimestamp().format(DateTimeFormatter.RFC_1123_DATE_TIME));
		sb.append("\n");
		sb.append("Deflated: ");
		sb.append(df.isCompressed()); sb.append("\n");
		sb.append("Size: 0x"); sb.append(Long.toHexString(df.getFileSize()));
		sb.append(" ("); sb.append(df.decSizeString()); sb.append(")\n");
		sb.append("SHA1: "); sb.append(df.getHashString()); sb.append("\n");
		sb.append("--- Associated Files ---\n");
		
		List<Integer> devices = df.getAssociatedDevices();
		for(Integer devid : devices){
			sb.append(">");
			DeviceRecord dr = manager.getDevice(devid);
			if(dr == null){
				sb.append("\n"); continue;
			}
			sb.append(dr.getDisplayName()); sb.append("\n");
			List<DeviceFile> dvlist = df.getDeviceFiles(devid);
			for(DeviceFile dv : dvlist){
				String p = manager.resolveDeviceFilePath(dv);
				sb.append("\t"); sb.append(p); sb.append("\n");
			}
		}
		
		txtFileInfo.setText(sb.toString());
		txtFileInfo.repaint();
	}
	
	/*----- Get -----*/
	
	public DataFile getSelectedDataFile(){
		return lstFiles.getSelectedValue();
	}
	
	/*----- Actions -----*/
	
	protected static String treePath2String(TreePath tp){
		if(tp == null) return "";
		Object[] opath = tp.getPath();
		StringBuilder sb = new StringBuilder(1024);
		for(Object o : opath){
			if(o instanceof FileNode){
				FileNode fn = (FileNode)o;
				sb.append("/");
				sb.append(fn.getFileName());
			}
		}
		return sb.toString();
	}
	
	protected void action_onSettings(){
		SettingsDialog dia = new SettingsDialog(this, manager);
		dia.setLocationRelativeTo(this);
		dia.setVisible(true);
	}
	
	protected void action_onExtract(){
		if(manager == null){
			showError("No backup manager loaded!");
			return;
		}
		
		//Get selected data file.
		DataFile df = getSelectedDataFile();
		
		//If none, show message and return.
		if(df == null){
			showError("No file selected for extraction!");
			return;
		}
		
		//Get a good candidate device path for the selected data file
		int devid = manager.getCurrentHost().getID();
		List<DeviceFile> devfiles = df.getDeviceFiles(devid);
		String spath = null;
		int sepcount = Integer.MAX_VALUE;
		for(DeviceFile r : devfiles){
			String path = manager.resolveDeviceFilePath(r);
			if(path != null){
				int seps = path.split(File.separator).length - 1;
				if(seps < sepcount){
					sepcount = seps;
					spath = path;
				}
			}
		}
		
		//Open JFileChooser with most recent device path for data file
		JFileChooser fc = new JFileChooser(spath);
		fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
		int res = fc.showSaveDialog(this);
		if(res != JFileChooser.APPROVE_OPTION) return;
		String p = fc.getSelectedFile().getAbsolutePath();
		
		//If user approves, spawn background task and copy back to requested path
		IndefProgressDialog waitdia = new IndefProgressDialog(this, "Extracting File");
		waitdia.setPrimaryString("Please wait");
		waitdia.setSecondaryString("Extracting to " + p);
		SwingWorker<Void, Void> task = new SwingWorker<Void, Void>(){

			protected Void doInBackground() throws Exception {
				try{
					if(manager.extractDataFile(df, p)) showNotice("Extraction was successful!");
					else showError("Data extraction failed.");
				}
				catch (Exception e){
					e.printStackTrace();
					showError("ERROR: Exception occurred during extraction.\n"
							+ "See stderr for details.");
				}
				return null;
			}
			
			public void done(){
				waitdia.closeMe();
			}
			
		};
		task.execute();
		waitdia.setVisible(true);
		
	}
	
	protected void action_onRunBackup(){
		//Launch backup start dialog
		BackupStartDialog stdia = new BackupStartDialog(this);
		stdia.setVisible(true);
		
		if(!stdia.getCloseSelection()) return; //Cancelled
		Map<String, String> submap = stdia.getSubstitutionPaths();
		//if(submap == null) submap = new HashMap<String, String>();
		//Start dialog disposes itself, so we can move on...
		
		BackupProgressDialog progdia = new BackupProgressDialog(this, manager);
		SwingWorker<Void, Void> task = new SwingWorker<Void, Void>(){

			protected Void doInBackground() throws Exception {
				try{
					manager.runBackup(submap, progdia);
				}
				catch (Exception e){
					e.printStackTrace();
					showError("");
				}
				return null;
			}
			
			public void done(){
				progdia.setVisible(false);
				progdia.dispose();
			}
			
		};
		task.execute();
		progdia.setVisible(true);
		
	}
	
	protected void action_onTabChange(){
		//TODO
	}
	
	protected void action_onSearchGo(){
		if(manager == null){
			showError("No backup manager loaded!");
			return;
		}
		
		//Get the text in the search box
		String query = txtSearch.getText();
		search_results.clear();
		
		IndefProgressDialog waitdia = new IndefProgressDialog(this, "Searching for files");
		waitdia.setPrimaryString("Search in progress");
		waitdia.setSecondaryString("Querying \"" + query + "\"");
		SwingWorker<Void, Void> task = new SwingWorker<Void, Void>(){

			protected Void doInBackground() throws Exception {
				try{
					Map<String, Collection<Long>> map = manager.searchByName(query, false, false);
					if (map != null) search_results = map;
				}
				catch (Exception e){
					e.printStackTrace();
					showError("ERROR: Exception occurred during search.\n"
							+ "See stderr for details.");
				}
				return null;
			}
			
			public void done(){
				waitdia.closeMe();
			}
			
		};
		task.execute();
		waitdia.setVisible(true);
	}
	
	protected void action_onViewTreeNode(){
		if(manager == null){
			showError("No backup manager loaded!");
			return;
		}
		
		lstFiles.setModel(new DefaultListModel<DataFile>());
		txtFileInfo.setText("");
		lstFiles.repaint(); txtFileInfo.repaint();
		
		IndefProgressDialog waitdia = new IndefProgressDialog(this, "Loading node");
		waitdia.setPrimaryString("Please wait");
		waitdia.setSecondaryString("Loading node data");
		SwingWorker<Void, Void> task = new SwingWorker<Void, Void>(){

			protected Void doInBackground() throws Exception {
				try{
					TreePath tp = tree.getSelectionPath();
					if(tp == null) return null;
					String relpath = treePath2String(tp);
					
					//Get the fs node from the manager...
					FileNode fn = mtree.getNodeAt(relpath);
					if(fn == null) return null;
					if(!(fn instanceof VersionedFileNode))return null;
					VersionedFileNode vfn = (VersionedFileNode)fn;
					
					//Load all associated DataFiles into the preview list (or clear if none)
					long[] ids = vfn.getSubfiles();
					DefaultListModel<DataFile> mdl = new DefaultListModel<DataFile>();
					for(long id : ids){
						DataFile df = manager.getDataRecord(id);
						if(df != null) mdl.addElement(df);
					}
					
					//Select first DataFile in list and load info into pane (or clear if N/A)
					lstFiles.setModel(mdl);
					lstFiles.setSelectedIndex(0);
					action_onViewListSelection();
				}
				catch (Exception e){
					e.printStackTrace();
					showError("ERROR: Node information could not be loaded.\n"
							+ "See stderr for details.");
				}
				return null;
			}
			
			public void done(){
				waitdia.closeMe();
			}
			
		};
		task.execute();
		waitdia.setVisible(true);
	}
	
	protected void action_onViewSearchNode(){
		//Viewing a search result
		
		if(manager == null){
			showError("No backup manager loaded!");
			return;
		}
		
		lstFiles.setModel(new DefaultListModel<DataFile>());
		txtFileInfo.setText("");
		lstFiles.repaint(); txtFileInfo.repaint();
		
		IndefProgressDialog waitdia = new IndefProgressDialog(this, "Loading search result");
		waitdia.setPrimaryString("Please wait");
		waitdia.setSecondaryString("Loading node data");
		SwingWorker<Void, Void> task = new SwingWorker<Void, Void>(){

			protected Void doInBackground() throws Exception {
				try{
					String query = lstSearch.getSelectedValue();
					if(query == null) return null;
					query = query.toLowerCase();
					Collection<Long> ids = search_results.get(query);
					if(ids == null || ids.isEmpty()) return null;

					DefaultListModel<DataFile> mdl = new DefaultListModel<DataFile>();
					for(long id : ids){
						DataFile df = manager.getDataRecord(id);
						if(df != null) mdl.addElement(df);
					}
					
					//Select first DataFile in list and load info into pane (or clear if N/A)
					lstFiles.setModel(mdl);
					lstFiles.setSelectedIndex(0);
					action_onViewListSelection();
				}
				catch (Exception e){
					e.printStackTrace();
					showError("ERROR: Node information could not be loaded.\n"
							+ "See stderr for details.");
				}
				return null;
			}
			
			public void done(){
				waitdia.closeMe();
			}
			
		};
		task.execute();
		waitdia.setVisible(true);
	}
	
	protected void action_onViewListSelection(){
		//Viewing an item from the selection result in the details text box
		loadDatafileInfoText();
	}
	
	/*----- Messages -----*/
	
	public void showNotice(String message){
		JOptionPane.showMessageDialog(this, message, "Notice", JOptionPane.INFORMATION_MESSAGE);
	}
	
	public void showWarning(String message){
		JOptionPane.showMessageDialog(this, message, "Warning", JOptionPane.WARNING_MESSAGE);
	}
	
	public void showError(String message){
		JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
	}
	
}
