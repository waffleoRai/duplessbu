package hospelhornbg_backupmulti.gui;

import java.awt.Frame;

import javax.swing.JDialog;
import javax.swing.JFileChooser;

import java.awt.GridBagLayout;
import javax.swing.JPanel;
import java.awt.GridBagConstraints;
import javax.swing.JScrollPane;
import java.awt.Insets;
import javax.swing.JTextArea;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.border.BevelBorder;

import java.awt.Dimension;
import java.awt.Font;

import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.awt.event.ActionEvent;
import javax.swing.border.EtchedBorder;

import hospelhornbg_backupmulti.BackupManager;
import hospelhornbg_backupmulti.BackupProgramFiles;
import hospelhornbg_backupmulti.DataFile;
import hospelhornbg_backupmulti.DeviceRecord;

import javax.swing.JCheckBox;
import javax.swing.JTextField;
import javax.swing.JComboBox;
import javax.swing.SwingConstants;

public class SettingsDialog extends JDialog{

	private static final long serialVersionUID = 3309377134181509163L;
	
	public static final int MIN_WIDTH = 450;
	public static final int MIN_HEIGHT = 420;
	
	public static final int CMBX_IDX_Byte = 0;
	public static final int CMBX_IDX_KB = 1;
	public static final int CMBX_IDX_MB = 2;
	public static final int CMBX_IDX_GB = 3;
	
	private String last_browse_path;
	
	private JTextArea txtDeviceInfo;
	private JList<String> lstBlacklist;
	private JCheckBox cbComp;
	private JTextField txtComp;
	private JComboBox<String> cmbxComp;
	private JCheckBox cbHashAll;
	
	private BackupManager manager;
	private Set<String> blacklist;
	
	public SettingsDialog(Frame parent, BackupManager bumng){
		super(parent, true);
		blacklist = new HashSet<String>();
		
		setLocationRelativeTo(parent);
		manager = bumng;
		initGUI();
		populateCombobox();
		updateGUI();
	}
	
	private void initGUI(){
		setMinimumSize(new Dimension(MIN_WIDTH, MIN_HEIGHT));
		setPreferredSize(new Dimension(MIN_WIDTH, MIN_HEIGHT));
		
		setTitle("Settings");
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{0, 0};
		gridBagLayout.rowHeights = new int[]{0, 0, 0, 0};
		gridBagLayout.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{1.0, 1.0, 0.0, Double.MIN_VALUE};
		getContentPane().setLayout(gridBagLayout);
		
		txtDeviceInfo = new JTextArea();
		GridBagConstraints gbc_txtDeviceInfo = new GridBagConstraints();
		gbc_txtDeviceInfo.insets = new Insets(0, 0, 5, 0);
		gbc_txtDeviceInfo.fill = GridBagConstraints.BOTH;
		gbc_txtDeviceInfo.gridx = 0;
		gbc_txtDeviceInfo.gridy = 0;
		getContentPane().add(txtDeviceInfo, gbc_txtDeviceInfo);
		
		JPanel pnlBlacklist = new JPanel();
		pnlBlacklist.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		GridBagConstraints gbc_pnlBlacklist = new GridBagConstraints();
		gbc_pnlBlacklist.insets = new Insets(0, 0, 5, 0);
		gbc_pnlBlacklist.fill = GridBagConstraints.BOTH;
		gbc_pnlBlacklist.gridx = 0;
		gbc_pnlBlacklist.gridy = 1;
		getContentPane().add(pnlBlacklist, gbc_pnlBlacklist);
		GridBagLayout gbl_pnlBlacklist = new GridBagLayout();
		gbl_pnlBlacklist.columnWidths = new int[]{0, 0};
		gbl_pnlBlacklist.rowHeights = new int[]{0, 0, 0, 0};
		gbl_pnlBlacklist.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gbl_pnlBlacklist.rowWeights = new double[]{1.0, 1.0, 1.0, Double.MIN_VALUE};
		pnlBlacklist.setLayout(gbl_pnlBlacklist);
		
		JPanel pnlBlacklistLabel = new JPanel();
		GridBagConstraints gbc_pnlBlacklistLabel = new GridBagConstraints();
		gbc_pnlBlacklistLabel.insets = new Insets(0, 0, 5, 0);
		gbc_pnlBlacklistLabel.fill = GridBagConstraints.BOTH;
		gbc_pnlBlacklistLabel.gridx = 0;
		gbc_pnlBlacklistLabel.gridy = 0;
		pnlBlacklist.add(pnlBlacklistLabel, gbc_pnlBlacklistLabel);
		GridBagLayout gbl_pnlBlacklistLabel = new GridBagLayout();
		gbl_pnlBlacklistLabel.columnWidths = new int[]{0, 0};
		gbl_pnlBlacklistLabel.rowHeights = new int[]{0, 0};
		gbl_pnlBlacklistLabel.columnWeights = new double[]{0.0, Double.MIN_VALUE};
		gbl_pnlBlacklistLabel.rowWeights = new double[]{1.0, Double.MIN_VALUE};
		pnlBlacklistLabel.setLayout(gbl_pnlBlacklistLabel);
		
		JLabel lblBlacklist = new JLabel("Blacklist:");
		lblBlacklist.setFont(new Font("Tahoma", Font.BOLD, 12));
		GridBagConstraints gbc_lblBlacklist = new GridBagConstraints();
		gbc_lblBlacklist.insets = new Insets(0, 5, 0, 0);
		gbc_lblBlacklist.gridx = 0;
		gbc_lblBlacklist.gridy = 0;
		pnlBlacklistLabel.add(lblBlacklist, gbc_lblBlacklist);
		
		JScrollPane spBlacklist = new JScrollPane();
		spBlacklist.setViewportBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		GridBagConstraints gbc_spBlacklist = new GridBagConstraints();
		gbc_spBlacklist.insets = new Insets(0, 0, 5, 0);
		gbc_spBlacklist.fill = GridBagConstraints.BOTH;
		gbc_spBlacklist.gridx = 0;
		gbc_spBlacklist.gridy = 1;
		pnlBlacklist.add(spBlacklist, gbc_spBlacklist);
		
		lstBlacklist = new JList<String>();
		spBlacklist.setViewportView(lstBlacklist);
		
		JPanel pnlBlacklistControls = new JPanel();
		GridBagConstraints gbc_pnlBlacklistControls = new GridBagConstraints();
		gbc_pnlBlacklistControls.fill = GridBagConstraints.BOTH;
		gbc_pnlBlacklistControls.gridx = 0;
		gbc_pnlBlacklistControls.gridy = 2;
		pnlBlacklist.add(pnlBlacklistControls, gbc_pnlBlacklistControls);
		GridBagLayout gbl_pnlBlacklistControls = new GridBagLayout();
		gbl_pnlBlacklistControls.columnWidths = new int[]{0, 0, 0, 0, 0, 0};
		gbl_pnlBlacklistControls.rowHeights = new int[]{0, 0};
		gbl_pnlBlacklistControls.columnWeights = new double[]{0.0, 0.0, 1.0, 0.0, 0.0, Double.MIN_VALUE};
		gbl_pnlBlacklistControls.rowWeights = new double[]{1.0, Double.MIN_VALUE};
		pnlBlacklistControls.setLayout(gbl_pnlBlacklistControls);
		
		JButton btnNew = new JButton("New");
		GridBagConstraints gbc_btnNew = new GridBagConstraints();
		gbc_btnNew.insets = new Insets(0, 5, 0, 5);
		gbc_btnNew.gridx = 0;
		gbc_btnNew.gridy = 0;
		pnlBlacklistControls.add(btnNew, gbc_btnNew);
		btnNew.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				action_onNew();
			}
		});
		
		JButton btnDelete = new JButton("Delete");
		btnDelete.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				action_onDelete();
			}
		});
		GridBagConstraints gbc_btnDelete = new GridBagConstraints();
		gbc_btnDelete.insets = new Insets(0, 0, 0, 5);
		gbc_btnDelete.gridx = 1;
		gbc_btnDelete.gridy = 0;
		pnlBlacklistControls.add(btnDelete, gbc_btnDelete);
		
		JButton btnReset = new JButton("Reset");
		GridBagConstraints gbc_btnReset = new GridBagConstraints();
		gbc_btnReset.insets = new Insets(0, 0, 0, 5);
		gbc_btnReset.gridx = 3;
		gbc_btnReset.gridy = 0;
		pnlBlacklistControls.add(btnReset, gbc_btnReset);
		btnReset.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				action_onReset();
			}
		});
		
		JButton btnClear = new JButton("Clear");
		GridBagConstraints gbc_btnClear = new GridBagConstraints();
		gbc_btnClear.insets = new Insets(0, 0, 0, 5);
		gbc_btnClear.gridx = 4;
		gbc_btnClear.gridy = 0;
		pnlBlacklistControls.add(btnClear, gbc_btnClear);
		btnClear.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				action_onClear();
			}
		});
		
		JPanel pnlMiscSettings = new JPanel();
		GridBagConstraints gbc_pnlMiscSettings = new GridBagConstraints();
		gbc_pnlMiscSettings.fill = GridBagConstraints.BOTH;
		gbc_pnlMiscSettings.gridx = 0;
		gbc_pnlMiscSettings.gridy = 2;
		getContentPane().add(pnlMiscSettings, gbc_pnlMiscSettings);
		GridBagLayout gbl_pnlMiscSettings = new GridBagLayout();
		gbl_pnlMiscSettings.columnWidths = new int[]{0, 0};
		gbl_pnlMiscSettings.rowHeights = new int[]{0, 0, 0};
		gbl_pnlMiscSettings.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gbl_pnlMiscSettings.rowWeights = new double[]{0.0, 0.0, Double.MIN_VALUE};
		pnlMiscSettings.setLayout(gbl_pnlMiscSettings);
		
		JPanel pnlMisc = new JPanel();
		GridBagConstraints gbc_pnlMisc = new GridBagConstraints();
		gbc_pnlMisc.insets = new Insets(0, 0, 5, 0);
		gbc_pnlMisc.fill = GridBagConstraints.BOTH;
		gbc_pnlMisc.gridx = 0;
		gbc_pnlMisc.gridy = 0;
		pnlMiscSettings.add(pnlMisc, gbc_pnlMisc);
		GridBagLayout gbl_pnlMisc = new GridBagLayout();
		gbl_pnlMisc.columnWidths = new int[]{0, 0, 0, 0, 60, 0, 0};
		gbl_pnlMisc.rowHeights = new int[]{0, 0, 0};
		gbl_pnlMisc.columnWeights = new double[]{0.0, 0.0, 0.0, 2.0, 0.0, 1.0, Double.MIN_VALUE};
		gbl_pnlMisc.rowWeights = new double[]{0.0, 0.0, Double.MIN_VALUE};
		pnlMisc.setLayout(gbl_pnlMisc);
		
		cbComp = new JCheckBox("Auto-Compression");
		cbComp.setHorizontalAlignment(SwingConstants.LEFT);
		GridBagConstraints gbc_cbComp = new GridBagConstraints();
		gbc_cbComp.anchor = GridBagConstraints.WEST;
		gbc_cbComp.insets = new Insets(0, 5, 5, 5);
		gbc_cbComp.gridx = 0;
		gbc_cbComp.gridy = 0;
		pnlMisc.add(cbComp, gbc_cbComp);
		cbComp.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				action_onCheckAutocomp();
			}
		});
		
		JLabel lblOver = new JLabel("Over");
		GridBagConstraints gbc_lblOver = new GridBagConstraints();
		gbc_lblOver.anchor = GridBagConstraints.EAST;
		gbc_lblOver.insets = new Insets(0, 0, 5, 5);
		gbc_lblOver.gridx = 2;
		gbc_lblOver.gridy = 0;
		pnlMisc.add(lblOver, gbc_lblOver);
		
		txtComp = new JTextField();
		GridBagConstraints gbc_txtComp = new GridBagConstraints();
		gbc_txtComp.insets = new Insets(0, 0, 5, 5);
		gbc_txtComp.fill = GridBagConstraints.HORIZONTAL;
		gbc_txtComp.gridx = 3;
		gbc_txtComp.gridy = 0;
		pnlMisc.add(txtComp, gbc_txtComp);
		txtComp.setColumns(10);
		
		cmbxComp = new JComboBox<String>();
		GridBagConstraints gbc_cmbxComp = new GridBagConstraints();
		gbc_cmbxComp.insets = new Insets(0, 0, 5, 5);
		gbc_cmbxComp.fill = GridBagConstraints.HORIZONTAL;
		gbc_cmbxComp.gridx = 4;
		gbc_cmbxComp.gridy = 0;
		pnlMisc.add(cmbxComp, gbc_cmbxComp);
		
		cbHashAll = new JCheckBox("Hash Check All Files");
		cbHashAll.setHorizontalAlignment(SwingConstants.LEFT);
		GridBagConstraints gbc_cbHashAll = new GridBagConstraints();
		gbc_cbHashAll.anchor = GridBagConstraints.WEST;
		gbc_cbHashAll.insets = new Insets(0, 5, 0, 5);
		gbc_cbHashAll.gridx = 0;
		gbc_cbHashAll.gridy = 1;
		pnlMisc.add(cbHashAll, gbc_cbHashAll);
		
		JPanel pnlClose = new JPanel();
		GridBagConstraints gbc_pnlClose = new GridBagConstraints();
		gbc_pnlClose.fill = GridBagConstraints.BOTH;
		gbc_pnlClose.gridx = 0;
		gbc_pnlClose.gridy = 1;
		pnlMiscSettings.add(pnlClose, gbc_pnlClose);
		GridBagLayout gbl_pnlClose = new GridBagLayout();
		gbl_pnlClose.columnWidths = new int[]{0, 0, 0, 0};
		gbl_pnlClose.rowHeights = new int[]{0, 0};
		gbl_pnlClose.columnWeights = new double[]{1.0, 0.0, 0.0, Double.MIN_VALUE};
		gbl_pnlClose.rowWeights = new double[]{0.0, Double.MIN_VALUE};
		pnlClose.setLayout(gbl_pnlClose);
		
		JButton btnOkay = new JButton("Okay");
		GridBagConstraints gbc_btnOkay = new GridBagConstraints();
		gbc_btnOkay.insets = new Insets(0, 0, 5, 5);
		gbc_btnOkay.gridx = 1;
		gbc_btnOkay.gridy = 0;
		pnlClose.add(btnOkay, gbc_btnOkay);
		btnOkay.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				action_onOkay();
			}
		});
		
		JButton btnCancel = new JButton("Cancel");
		GridBagConstraints gbc_btnCancel = new GridBagConstraints();
		gbc_btnCancel.insets = new Insets(0, 0, 5, 5);
		gbc_btnCancel.gridx = 2;
		gbc_btnCancel.gridy = 0;
		pnlClose.add(btnCancel, gbc_btnCancel);
		btnCancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				action_onCancel();
			}
		});
	}

	private void populateCombobox(){
		DefaultComboBoxModel<String> mdl = new DefaultComboBoxModel<String>();
		mdl.addElement("bytes");
		mdl.addElement("kB");
		mdl.addElement("MB");
		mdl.addElement("GB");
		cmbxComp.setModel(mdl);
	}
	
	private void updateGUI(){
		//Device info in pane
		StringBuilder sb = new StringBuilder(4096);
		if(manager == null) sb.append("<No manager loaded>");
		else{
			DeviceRecord rec = manager.getCurrentHost();
			sb.append("Device Name: " + rec.getDisplayName() + "\n");
			sb.append("OS Family: ");
			switch(rec.getOSEnum()){
			case BackupProgramFiles.OSENUM_WIN: sb.append("Windows"); break;
			case BackupProgramFiles.OSENUM_MACOS: sb.append("MacOS/OSX"); break;
			case BackupProgramFiles.OSENUM_LINUX: sb.append("Linux"); break;
			default: sb.append("(Unknown)"); break;
			}
			sb.append("\n");
			sb.append("Device ID: 0x" + String.format("%08x", rec.getID())+  "\n");
		}
		txtDeviceInfo.setText(sb.toString());
		txtDeviceInfo.repaint();
		
		//Load blacklist
		DefaultListModel<String> mdl = new DefaultListModel<String>();
		if(manager == null){
			blacklist = null;
		}
		else{
			try{
				List<String> blist = manager.getHostBlacklist();
				blacklist.clear(); blacklist.addAll(blist);
				for(String s : blist) mdl.addElement(s);
			}
			catch(IOException ex){
				JOptionPane.showMessageDialog(this, "Error: Blacklist could not be loaded!", 
						"I/O Error", JOptionPane.ERROR_MESSAGE);
			}
		}
		lstBlacklist.setModel(mdl);
		lstBlacklist.setSelectedIndex(-1);
		lstBlacklist.repaint();
		
		//Update checkbox settings
		if(manager != null){
			long cthresh = manager.getCompThreshold();
			if(cthresh < 0L){
				cbComp.setSelected(false);
				txtComp.setText("0");
				cmbxComp.setSelectedIndex(0);
			}
			else{
				cbComp.setSelected(true);
				double amt = 0.0;
				if(cthresh >= DataFile.GIGABYTE){
					cmbxComp.setSelectedIndex(CMBX_IDX_GB);
					amt = (double)cthresh/(double)DataFile.GIGABYTE;
				}
				else if(cthresh >= DataFile.MEGABYTE){
					cmbxComp.setSelectedIndex(CMBX_IDX_MB);
					amt = (double)cthresh/(double)DataFile.MEGABYTE;
				}
				else if(cthresh >= DataFile.KILOBYTE){
					cmbxComp.setSelectedIndex(CMBX_IDX_KB);
					amt = (double)cthresh/(double)DataFile.KILOBYTE;
				}
				else{
					cmbxComp.setSelectedIndex(CMBX_IDX_Byte);
					amt = (double)cthresh;
				}
				txtComp.setText(Double.toString(amt));
			}
			cbHashAll.setSelected(manager.getHashAll());
		}
		else{
			txtComp.setText("0");
			cmbxComp.setSelectedIndex(0);
			cbComp.setSelected(false);
			cbHashAll.setSelected(false);
		}
		action_onCheckAutocomp();
		cbHashAll.repaint();

	}
	
	protected void closeMe(){
		setVisible(false);
		dispose();
	}
	
	private void action_onNew(){
		JFileChooser fc = new JFileChooser(last_browse_path);
		fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
		int res = fc.showOpenDialog(this);
		
		if(res == JFileChooser.APPROVE_OPTION){
			File[] files = fc.getSelectedFiles();
			if(files == null || files.length < 1) return;
			for(File f : files){
				blacklist.add(f.getAbsolutePath());
			}
			
			last_browse_path = files[0].getAbsolutePath();
			List<String> list = new ArrayList<String>(blacklist.size()+1);
			list.addAll(blacklist);
			Collections.sort(list);
			
			DefaultListModel<String> mdl = new DefaultListModel<String>();
			for(String s : list) mdl.addElement(s);
			lstBlacklist.setModel(mdl);
			lstBlacklist.repaint();
		}
	}
	
	private void action_onDelete(){
		//Get selected list items
		List<String> slist = lstBlacklist.getSelectedValuesList();
		if(slist == null || slist.isEmpty()) return;
		
		for(String s : slist){
			blacklist.remove(s);
		}
		
		List<String> list = new ArrayList<String>(blacklist.size()+1);
		list.addAll(blacklist);
		Collections.sort(list);
		
		DefaultListModel<String> mdl = new DefaultListModel<String>();
		for(String s : list) mdl.addElement(s);
		lstBlacklist.setModel(mdl);
		lstBlacklist.repaint();
	}
	
	private void action_onReset(){
		List<String> list = manager.defaultBlacklist();
		Collections.sort(list);
		blacklist.clear();
		blacklist.addAll(list);
		
		DefaultListModel<String> mdl = new DefaultListModel<String>();
		for(String s : list) mdl.addElement(s);
		lstBlacklist.setModel(mdl);
		lstBlacklist.repaint();
	}
	
	private void action_onClear(){
		blacklist.clear();
		lstBlacklist.setModel(new DefaultListModel<String>());
		lstBlacklist.repaint();
	}
	
	private void action_onOkay(){
		//Save to manager and close dialog
		if(manager == null){
			action_onCancel();
			return;
		}
		
		//Checkbox stuff
		manager.setHashAll(cbHashAll.isSelected());
		if(!cbComp.isSelected()) manager.setCompThreshold(-1L);
		else{
			//Try to parse text field
			double val = -1.0;
			try{val = Double.parseDouble(txtComp.getText());}
			catch(NumberFormatException ex){
				JOptionPane.showMessageDialog(this, "Number format exception", 
						"Invalid compression threshold value: \"" + txtComp.getText() + "\"", 
						JOptionPane.WARNING_MESSAGE);
				return;
			}
			
			if(val >= 0){
				//Get scaling factor.
				switch(cmbxComp.getSelectedIndex()){
				case CMBX_IDX_KB: 
					val *= (double)DataFile.KILOBYTE;
					break;
				case CMBX_IDX_MB: 
					val *= (double)DataFile.MEGABYTE;
					break;
				case CMBX_IDX_GB:
					val *= (double)DataFile.GIGABYTE;
					break;
				}
				manager.setCompThreshold(Math.round(val));
			}
			else{
				JOptionPane.showMessageDialog(this, "Number format exception", 
						"Invalid compression threshold value: \"" + txtComp.getText() + "\"", 
						JOptionPane.WARNING_MESSAGE);
				return;
			}
		}
		
		//Blacklist
		try{manager.setHostBlacklist(blacklist);}
		catch(IOException ex){
			ex.printStackTrace();
			JOptionPane.showMessageDialog(this, "I/O Error", 
					"Blacklist failed to update (could not write file)", 
					JOptionPane.WARNING_MESSAGE);
		}
		
		//Close
		setVisible(false);
		dispose();
	}
	
	private void action_onCancel(){
		//Save nothing and close dialog.
		setVisible(false);
		dispose();
	}
	
	private void action_onCheckAutocomp(){
		//Enables and disables the comp fields.
		txtComp.setEnabled(cbComp.isSelected()); txtComp.repaint();
		cmbxComp.setEnabled(cbComp.isSelected()); cmbxComp.repaint();
	}
	
}
