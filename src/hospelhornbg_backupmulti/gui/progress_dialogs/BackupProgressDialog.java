package hospelhornbg_backupmulti.gui.progress_dialogs;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JDialog;

import hospelhornbg_backupmulti.BackupListener;
import hospelhornbg_backupmulti.BackupManager;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import javax.swing.JButton;

import java.awt.Dimension;
import java.awt.Font;
import javax.swing.JSeparator;

public class BackupProgressDialog extends JDialog implements BackupListener{

	private static final long serialVersionUID = 5905925317438344023L;

	public static final int WIDTH = 370;
	public static final int HEIGHT = 250;
	
	private BackupManager manager; //So can send cancel signal!
	
	private JLabel lblPrimary;
	private JLabel lblSecondary;
	private JLabel lblCurrent;
	private JLabel lblTotal;
	
	private JButton btnOk;
	private JButton btnCancel;
	
	private JProgressBar pbCurrent;
	private JProgressBar pbTotal;
	
	private long currentVal;
	private long currentMax;
	private long totalVal;
	private long totalMax;
	
	private boolean cancelbool = false;
	
	public BackupProgressDialog(Frame parent, BackupManager bumng){
		super(parent, true);
		manager = bumng;
		//GUI
		initGUI();
		setPrimaryLabel("Initializing");
		setSecondaryLabel("Please wait");
		setCurrentProgressLabel("Estimating...");
		setTotalProgressLabel("Estimating...");
	}
	
	private void initGUI(){
		setResizable(false);
		setTitle("Running Backup");
		getContentPane().setLayout(null);
		setMinimumSize(new Dimension(WIDTH, HEIGHT));
		setPreferredSize(new Dimension(WIDTH, HEIGHT));
		
		lblPrimary = new JLabel("Message 1");
		lblPrimary.setFont(new Font("Tahoma", Font.PLAIN, 13));
		lblPrimary.setBounds(10, 11, 344, 23);
		getContentPane().add(lblPrimary);
		
		lblSecondary = new JLabel("Message 2");
		lblSecondary.setFont(new Font("Tahoma", Font.PLAIN, 11));
		lblSecondary.setBounds(10, 39, 344, 14);
		getContentPane().add(lblSecondary);
		
		lblCurrent = new JLabel("Current Progress");
		lblCurrent.setFont(new Font("Tahoma", Font.PLAIN, 11));
		lblCurrent.setBounds(10, 74, 344, 14);
		getContentPane().add(lblCurrent);
		
		lblTotal = new JLabel("Total Progress");
		lblTotal.setFont(new Font("Tahoma", Font.PLAIN, 11));
		lblTotal.setBounds(10, 123, 344, 14);
		getContentPane().add(lblTotal);
		
		pbCurrent = new JProgressBar();
		pbCurrent.setBounds(10, 89, 344, 23);
		getContentPane().add(pbCurrent);
		
		pbTotal = new JProgressBar();
		pbTotal.setBounds(10, 139, 344, 23);
		getContentPane().add(pbTotal);
		
		btnOk = new JButton("OK");
		btnOk.setBounds(265, 187, 89, 23);
		getContentPane().add(btnOk);
		btnOk.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				onOkayButton();
			}
		});
		btnOk.setEnabled(false);
		
		btnCancel = new JButton("Cancel");
		btnCancel.setBounds(166, 187, 89, 23);
		getContentPane().add(btnCancel);
		btnCancel.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				onCancelButton();
			}
		});
		
		JSeparator separator = new JSeparator();
		separator.setBounds(10, 61, 344, 2);
		getContentPane().add(separator);
	}
	
	public void setPrimaryLabel(String s){
		lblPrimary.setText(s);
		lblPrimary.repaint();
	}
	
	public void setSecondaryLabel(String s){
		lblSecondary.setText(s);
		lblSecondary.repaint();
	}
	
	public void setCurrentProgressLabel(String s){
		lblCurrent.setText(s);
		lblCurrent.repaint();
	}
	
	public void setTotalProgressLabel(String s){
		lblTotal.setText(s);
		lblTotal.repaint();
	}
	
	public void setCurrentProgress(double prop){
		double pbraw = 100.0 * Math.max(0.0, Math.min(1.0, prop));
		pbCurrent.setValue((int)Math.round(pbraw));
	}
	
	public void setTotalProgress(double prop){
		double pbraw = 100.0 * Math.max(0.0, Math.min(1.0, prop));
		pbTotal.setValue((int)Math.round(pbraw));
	}

	/*----- Backup Callbacks -----*/
	
	private void updateCurrentLabel(boolean withHash){
		StringBuilder sb = new StringBuilder(1024);
		if(withHash) sb.append("Copying & Hashing: ");
		else sb.append("Copying: ");
		sb.append(currentVal);
		sb.append(" of ");
		sb.append(currentMax);
		sb.append(" bytes processed");
		setCurrentProgressLabel(sb.toString());
	}
	
	private void setCurrentMax(long size, boolean withHash){
		currentVal = 0L; currentMax = Math.max(0L, size);
		updateCurrentLabel(withHash);
		setCurrentProgress(0.0);
	}
	
	private void setCurrentVal(long val, boolean withHash){
		currentVal = val;
		updateCurrentLabel(withHash);
		setCurrentProgress((double)currentVal/(double)currentMax);
	}
	
	@Override
	public void setHashCopySize(long size) {
		setCurrentMax(size, true);
	}

	@Override
	public void updateHashCopyProgress(long bytesProcessed) {
		setCurrentVal(bytesProcessed, true);
	}

	@Override
	public void setCopySize(long size) {
		setCurrentMax(size, false);
	}

	@Override
	public void updateCopyProgress(long bytesProcessed) {
		setCurrentVal(bytesProcessed, false);
	}

	private void updateTotalLabel(){
		StringBuilder sb = new StringBuilder(1024);
		sb.append(totalVal); sb.append(" of ");
		sb.append(totalMax); sb.append(" files processed");
		setTotalProgressLabel(sb.toString());
	}
	
	@Override
	public void setTotalEstimatedFiles(int count) {
		totalVal = 0L; totalMax = count;
		updateTotalLabel();
		setTotalProgress(0.0);
		setPrimaryLabel("Backup Running");
	}

	@Override
	public void incrementProcessedFileCount() {
		totalVal++;
		updateTotalLabel();
		setTotalProgress((double)totalVal/(double)totalMax);
	}

	@Override
	public void onStartFileProcessing(String file_path) {
		setSecondaryLabel("Working on: " + file_path);
		setCurrentProgressLabel("Calculating...");
		setCurrentProgress(0.0);
	}

	@Override
	public void onStartDirectoryProcessing(String dir_path) {
		setSecondaryLabel("Directory found: " + dir_path);
		setCurrentProgressLabel("Calculating...");
		setCurrentProgress(0.0);
	}

	@Override
	public void onCancelRequest() {
		setPrimaryLabel("Cancelling...");
		setSecondaryLabel("Requesting cancellation");
		cancelbool = true;
		manager.requestBackupCancel();
		btnCancel.setEnabled(false);
	}

	@Override
	public void onCancelAcknowledge() {
		setSecondaryLabel("Cancellation request acknowledged");
	}

	@Override
	public void onFinishSuccess() {
		setPrimaryLabel("Complete!");
		setSecondaryLabel("Backup completed successfully");
		setCurrentProgressLabel("Done");
		setTotalProgressLabel("Done");
		setCurrentProgress(1.0); setTotalProgress(1.0);
		btnOk.setEnabled(true);
	}

	@Override
	public void onFinishFailure() {
		setPrimaryLabel("Backup failed!");
		setSecondaryLabel("Backup did not complete successfully");
		btnOk.setEnabled(true);
		JOptionPane.showMessageDialog(this, "Backup was unable to complete successfully. \n"
				+ "If this is unexpected, see stderr for details.", 
				"Backup Failed", JOptionPane.WARNING_MESSAGE);
	}
	
	/*----- Button Callbacks -----*/
	
	private void onCancelButton(){
		if(cancelbool) return;
		int res = JOptionPane.showConfirmDialog(this, "Are you sure you want to cancel the backup in progress?", 
				"Cancel Backup", JOptionPane.YES_NO_OPTION);
		if(res == JOptionPane.YES_OPTION){
			onCancelRequest();
		}
	}
	
	private void onOkayButton(){
		setVisible(false);
		dispose();
	}
	
}
