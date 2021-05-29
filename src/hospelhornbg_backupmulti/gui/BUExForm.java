package hospelhornbg_backupmulti.gui;

import javax.swing.JFrame;
import java.awt.GridBagLayout;
import javax.swing.JPanel;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import javax.swing.JTabbedPane;
import javax.swing.JScrollPane;
import javax.swing.border.BevelBorder;

import hospelhornbg_backupmulti.BackupManager;
import hospelhornbg_backupmulti.DataFile;

import javax.swing.JTree;
import javax.swing.JButton;
import javax.swing.JTextField;
import javax.swing.JList;
import javax.swing.JTextPane;

public class BUExForm extends JFrame{

	/*----- Constants -----*/
	
	private static final long serialVersionUID = -8925831783995294784L;
	
	public static final int MIN_WIDTH = 430;
	public static final int MIN_HEIGHT = 330;
	
	/*----- Instance Variables -----*/
	
	private JTextField txtSearch; //Search all files in backup system by name
	private JList<String> lstSearch; //File name search results
	private JTree tree; //Tree view of backup files
	
	private JList<DataFile> lstFiles; //List of files associated with node (like, various versions)
	private JTextPane txtFileInfo; //Info on specific file/version
	
	private BackupManager manager;
	
	/*----- Initialization -----*/
	
	public BUExForm(BackupManager bumng) {
		manager = bumng;
		this.setMinimumSize(new Dimension(MIN_WIDTH, MIN_HEIGHT));
		this.setPreferredSize(new Dimension(MIN_WIDTH, MIN_HEIGHT));
		
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
		
		JButton btnRun = new JButton("Run Backup");
		GridBagConstraints gbc_btnRun = new GridBagConstraints();
		gbc_btnRun.gridx = 3;
		gbc_btnRun.gridy = 0;
		pnlButtons.add(btnRun, gbc_btnRun);
	}
	
	/*----- GUI View -----*/
	
	private void loadManagerView(){
		//TODO
		//Loads the tree view etc.
	}
	
	/*----- Actions -----*/
	
	protected void action_onSettings(){
		//TODO
	}
	
	protected void action_onExtract(){
		//TODO
	}
	
	protected void action_onRunBackup(){
		//TODO
	}
	
	protected void action_onTabChange(){
		//TODO
	}
	
	protected void action_onSearchGo(){
		//TODO
	}
	
	protected void action_onViewTreeNode(){
		//TODO
	}
	
	protected void action_onViewSearchNode(){
		//TODO
		//Viewing a search result
	}
	
	protected void action_onViewListSelection(){
		//TODO
		//Viewing an item from the selection result in the details text box
	}
	
}
