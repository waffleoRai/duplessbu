package hospelhornbg_backupmulti.gui;

import java.awt.Frame;

import javax.swing.JDialog;
import javax.swing.JFileChooser;

import java.awt.GridBagLayout;
import javax.swing.JPanel;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.border.EtchedBorder;
import javax.swing.JLabel;

import java.awt.Dimension;
import java.awt.Font;
import javax.swing.JScrollPane;
import javax.swing.border.BevelBorder;
import javax.swing.JList;

public class BackupStartDialog extends JDialog{

	//I wanna add an option to sub in directories for other directories
	//(Like to sync in older backups on other drives)

	private static final long serialVersionUID = 494112460420988625L;
	
	public static final int MIN_WIDTH = 350;
	public static final int MIN_HEIGHT = 200;
	
	private JList<Subpath> lstPathSubs;
	private Map<String, String> submap;
	
	private boolean okay;
	
	private static class Subpath{
		public String from;
		public String to;
		
		public Subpath(String s1, String s2){
			from = s1; to = s2;
		}
		
		public String toString(){return "[" + from + "] -> [" + to + "]";}
	}
	
	public BackupStartDialog(Frame parent){
		super(parent, true);
		setMinimumSize(new Dimension(MIN_WIDTH, MIN_HEIGHT));
		setPreferredSize(new Dimension(MIN_WIDTH, MIN_HEIGHT));
		setLocationRelativeTo(parent);
		initGUI();
		submap = new HashMap<String, String>();
	}
	
	private void initGUI(){
		setTitle("Run Backup");
		
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{0, 0};
		gridBagLayout.rowHeights = new int[]{0, 0, 0};
		gridBagLayout.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{1.0, 0.0, Double.MIN_VALUE};
		getContentPane().setLayout(gridBagLayout);
		
		JPanel pnlSubpaths = new JPanel();
		pnlSubpaths.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		GridBagConstraints gbc_pnlSubpaths = new GridBagConstraints();
		gbc_pnlSubpaths.insets = new Insets(0, 0, 5, 0);
		gbc_pnlSubpaths.fill = GridBagConstraints.BOTH;
		gbc_pnlSubpaths.gridx = 0;
		gbc_pnlSubpaths.gridy = 0;
		getContentPane().add(pnlSubpaths, gbc_pnlSubpaths);
		GridBagLayout gbl_pnlSubpaths = new GridBagLayout();
		gbl_pnlSubpaths.columnWidths = new int[]{0, 0};
		gbl_pnlSubpaths.rowHeights = new int[]{0, 0, 0, 0};
		gbl_pnlSubpaths.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gbl_pnlSubpaths.rowWeights = new double[]{0.0, 1.0, 0.0, Double.MIN_VALUE};
		pnlSubpaths.setLayout(gbl_pnlSubpaths);
		
		JLabel lblPathSubstitutions = new JLabel("Path Substitutions:");
		lblPathSubstitutions.setFont(new Font("Tahoma", Font.BOLD, 11));
		GridBagConstraints gbc_lblPathSubstitutions = new GridBagConstraints();
		gbc_lblPathSubstitutions.anchor = GridBagConstraints.WEST;
		gbc_lblPathSubstitutions.insets = new Insets(5, 5, 5, 0);
		gbc_lblPathSubstitutions.gridx = 0;
		gbc_lblPathSubstitutions.gridy = 0;
		pnlSubpaths.add(lblPathSubstitutions, gbc_lblPathSubstitutions);
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setViewportBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		GridBagConstraints gbc_scrollPane = new GridBagConstraints();
		gbc_scrollPane.insets = new Insets(0, 0, 5, 0);
		gbc_scrollPane.fill = GridBagConstraints.BOTH;
		gbc_scrollPane.gridx = 0;
		gbc_scrollPane.gridy = 1;
		pnlSubpaths.add(scrollPane, gbc_scrollPane);
		
		lstPathSubs = new JList<Subpath>();
		scrollPane.setViewportView(lstPathSubs);
		
		JPanel panel = new JPanel();
		GridBagConstraints gbc_panel = new GridBagConstraints();
		gbc_panel.fill = GridBagConstraints.BOTH;
		gbc_panel.gridx = 0;
		gbc_panel.gridy = 2;
		pnlSubpaths.add(panel, gbc_panel);
		GridBagLayout gbl_panel = new GridBagLayout();
		gbl_panel.columnWidths = new int[]{0, 0, 0, 0, 0};
		gbl_panel.rowHeights = new int[]{0, 0};
		gbl_panel.columnWeights = new double[]{0.0, 0.0, 1.0, 0.0, Double.MIN_VALUE};
		gbl_panel.rowWeights = new double[]{0.0, Double.MIN_VALUE};
		panel.setLayout(gbl_panel);
		
		JButton btnAdd = new JButton("Add");
		GridBagConstraints gbc_btnAdd = new GridBagConstraints();
		gbc_btnAdd.insets = new Insets(0, 5, 0, 5);
		gbc_btnAdd.gridx = 0;
		gbc_btnAdd.gridy = 0;
		panel.add(btnAdd, gbc_btnAdd);
		btnAdd.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				action_onAdd();
			}
		});
		
		JButton btnDelete = new JButton("Delete");
		GridBagConstraints gbc_btnDelete = new GridBagConstraints();
		gbc_btnDelete.insets = new Insets(0, 0, 0, 5);
		gbc_btnDelete.gridx = 1;
		gbc_btnDelete.gridy = 0;
		panel.add(btnDelete, gbc_btnDelete);
		btnDelete.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				action_onDelete();
			}
		});
		
		JButton btnClear = new JButton("Clear");
		GridBagConstraints gbc_btnClear = new GridBagConstraints();
		gbc_btnClear.insets = new Insets(0, 0, 0, 5);
		gbc_btnClear.gridx = 3;
		gbc_btnClear.gridy = 0;
		panel.add(btnClear, gbc_btnClear);
		btnClear.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				action_onClear();
			}
		});
		
		JPanel pnlButtons = new JPanel();
		pnlButtons.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		GridBagConstraints gbc_pnlButtons = new GridBagConstraints();
		gbc_pnlButtons.fill = GridBagConstraints.BOTH;
		gbc_pnlButtons.gridx = 0;
		gbc_pnlButtons.gridy = 1;
		getContentPane().add(pnlButtons, gbc_pnlButtons);
		GridBagLayout gbl_pnlButtons = new GridBagLayout();
		gbl_pnlButtons.columnWidths = new int[]{0, 0, 0, 0, 0};
		gbl_pnlButtons.rowHeights = new int[]{0, 0};
		gbl_pnlButtons.columnWeights = new double[]{0.0, 1.0, 0.0, 0.0, Double.MIN_VALUE};
		gbl_pnlButtons.rowWeights = new double[]{0.0, Double.MIN_VALUE};
		pnlButtons.setLayout(gbl_pnlButtons);
		
		JButton btnStart = new JButton("Start");
		GridBagConstraints gbc_btnStart = new GridBagConstraints();
		gbc_btnStart.insets = new Insets(5, 0, 5, 5);
		gbc_btnStart.gridx = 2;
		gbc_btnStart.gridy = 0;
		pnlButtons.add(btnStart, gbc_btnStart);
		btnStart.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				action_onStart();
			}
		});
		
		JButton btnCancel = new JButton("Cancel");
		GridBagConstraints gbc_btnCancel = new GridBagConstraints();
		gbc_btnCancel.insets = new Insets(5, 0, 5, 5);
		gbc_btnCancel.gridx = 3;
		gbc_btnCancel.gridy = 0;
		pnlButtons.add(btnCancel, gbc_btnCancel);
		btnCancel.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				action_onCancel();
			}
		});
	}
	
	public boolean getCloseSelection(){return this.okay;}
	
	public Map<String, String> getSubstitutionPaths(){
		return submap;
	}
	
	private void updateSubList(){
		//To match map
		
		DefaultListModel<Subpath> mdl = new DefaultListModel<Subpath>();
		if(!submap.isEmpty()){
			List<String> keys = new ArrayList<String>(submap.size());
			keys.addAll(submap.keySet());
			Collections.sort(keys);
			for(String k : keys){
				Subpath sp = new Subpath(k, submap.get(k));
				mdl.addElement(sp);
			}
		}
	
		lstPathSubs.setModel(mdl);
		lstPathSubs.repaint();
	}
	
	private void action_onAdd(){
		//Just use 2 JFileChoosers?
		JFileChooser fc = new JFileChooser();
		fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
		fc.setDialogTitle("Select file/directory to be substituted");
		int res = fc.showOpenDialog(this);
		
		if(res != JFileChooser.APPROVE_OPTION) return;
		String p1 = fc.getSelectedFile().getAbsolutePath();
		
		fc = new JFileChooser();
		fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
		fc.setDialogTitle("Select file/directory to copy instead");
		res = fc.showOpenDialog(this);
		if(res != JFileChooser.APPROVE_OPTION) return;
		String p2 = fc.getSelectedFile().getAbsolutePath();
		
		submap.put(p1, p2);
		
		updateSubList();
	}
	
	private void action_onDelete(){
		List<Subpath> selected = lstPathSubs.getSelectedValuesList();
		for(Subpath p : selected){
			if(p.from != null) submap.remove(p.from);
		}
		updateSubList();
	}
	
	private void action_onClear(){
		submap.clear();
		updateSubList();
	}
	
	private void action_onStart(){
		okay = true;
		setVisible(false);
		dispose();
	}
	
	private void action_onCancel(){
		okay = false;
		submap.clear();
		setVisible(false);
		dispose();
	}
	
}
