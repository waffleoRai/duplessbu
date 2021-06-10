package hospelhornbg_backupmulti.gui;

import javax.swing.JFrame;
import java.awt.GridBagLayout;
import javax.swing.JPanel;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import javax.swing.JScrollPane;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.List;

import javax.swing.border.BevelBorder;

import hospelhornbg_backupmulti.BackupManager;
import hospelhornbg_backupmulti.DeviceRecord;

import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JButton;

public class DeviceSelectWindow extends JFrame{

	private static final long serialVersionUID = -2329326807530385037L;

	public static final int WIDTH = 400;
	public static final int HEIGHT = 260;
	
	private boolean selection = false;
	private DeviceRecord selected = null;
	private JList<DeviceRecord> list;
	private BackupManager manager;
	
	public DeviceSelectWindow(BackupManager manager) {
		this.manager = manager;
		initGUI();
		populateList(manager);
	}
	
	private void populateList(BackupManager manager){
		if(manager == null) return;
		List<DeviceRecord> list = manager.getAllDevices();
		for(DeviceRecord r : list)list.add(r);
	}
	
	private void initGUI(){
		
		setResizable(false);
		setMinimumSize(new Dimension(WIDTH, HEIGHT));
		setPreferredSize(new Dimension(WIDTH, HEIGHT));
		
		setTitle("Select Device");
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{0, 0};
		gridBagLayout.rowHeights = new int[]{0, 0, 0};
		gridBagLayout.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{1.0, 0.0, Double.MIN_VALUE};
		getContentPane().setLayout(gridBagLayout);
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setViewportBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		GridBagConstraints gbc_scrollPane = new GridBagConstraints();
		gbc_scrollPane.insets = new Insets(5, 5, 5, 5);
		gbc_scrollPane.fill = GridBagConstraints.BOTH;
		gbc_scrollPane.gridx = 0;
		gbc_scrollPane.gridy = 0;
		getContentPane().add(scrollPane, gbc_scrollPane);
		
		JList<DeviceRecord> list = new JList<DeviceRecord>();
		scrollPane.setViewportView(list);
		
		JPanel panel = new JPanel();
		GridBagConstraints gbc_panel = new GridBagConstraints();
		gbc_panel.insets = new Insets(5, 5, 5, 5);
		gbc_panel.fill = GridBagConstraints.BOTH;
		gbc_panel.gridx = 0;
		gbc_panel.gridy = 1;
		getContentPane().add(panel, gbc_panel);
		GridBagLayout gbl_panel = new GridBagLayout();
		gbl_panel.columnWidths = new int[]{0, 0, 0, 0, 0};
		gbl_panel.rowHeights = new int[]{0, 0};
		gbl_panel.columnWeights = new double[]{0.0, 1.0, 0.0, 0.0, Double.MIN_VALUE};
		gbl_panel.rowWeights = new double[]{0.0, Double.MIN_VALUE};
		panel.setLayout(gbl_panel);
		
		JButton btnNew = new JButton("New");
		GridBagConstraints gbc_btnNew = new GridBagConstraints();
		gbc_btnNew.insets = new Insets(0, 0, 0, 5);
		gbc_btnNew.gridx = 0;
		gbc_btnNew.gridy = 0;
		panel.add(btnNew, gbc_btnNew);
		btnNew.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				onNew();
			}
		});
		if(manager == null) btnNew.setEnabled(false);
		
		JButton btnOkay = new JButton("Okay");
		GridBagConstraints gbc_btnOkay = new GridBagConstraints();
		gbc_btnOkay.insets = new Insets(0, 0, 0, 5);
		gbc_btnOkay.gridx = 2;
		gbc_btnOkay.gridy = 0;
		panel.add(btnOkay, gbc_btnOkay);
		btnOkay.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				onOkay();
			}
		});
		
		JButton btnCancel = new JButton("Cancel");
		GridBagConstraints gbc_btnCancel = new GridBagConstraints();
		gbc_btnCancel.gridx = 3;
		gbc_btnCancel.gridy = 0;
		panel.add(btnCancel, gbc_btnCancel);
		btnCancel.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				onCancel();
			}
		});
	}

	public void onNew(){
		if(manager == null) return;
		DeviceRecord r = null;
		try{r = manager.genRecordForLocalHost();}
		catch(IOException ex){
			ex.printStackTrace();
			JOptionPane.showMessageDialog(this, "Detection of device info failed! Exiting...", 
					"Error", JOptionPane.ERROR_MESSAGE);
			onCancel();
		}
		
		if(r != null){
			selection = true;
			selected = r;
			JOptionPane.showMessageDialog(this, "Device added: " + r.toString(), 
					"Device Added", JOptionPane.INFORMATION_MESSAGE);
			setVisible(false);
			dispose();
			return;
		}
		else{
			JOptionPane.showMessageDialog(this, "Detection of device info failed! Exiting...", 
					"Error", JOptionPane.ERROR_MESSAGE);
			onCancel();
		}
	}
	
	public void onOkay(){
		selection = true;
		selected = list.getSelectedValue();
		setVisible(false);
		dispose();
	}
	
	public void onCancel(){
		selection = false;
		selected = null;
		setVisible(false);
		dispose();
	}
	
	public boolean getCloseSelection(){return selection;}
	
	public DeviceRecord getSelectedDevice(){
		return selected;
	}
	
}
