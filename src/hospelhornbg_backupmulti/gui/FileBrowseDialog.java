package hospelhornbg_backupmulti.gui;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JTextField;
import javax.swing.JButton;

public class FileBrowseDialog extends JDialog{

	private static final long serialVersionUID = 8584132803304229036L;
	
	public static final int WIDTH = 350;
	public static final int HEIGHT = 100;
	
	private boolean okay;
	private String value;
	
	private JTextField txtPath;

	public FileBrowseDialog(Frame parent) {
		super(parent, true);
		
		okay = false;
		value = null;
		
		setMinimumSize(new Dimension(WIDTH, HEIGHT));
		setPreferredSize(new Dimension(WIDTH, HEIGHT));
		setResizable(false);
		
		setTitle("Select File");
		setLocationRelativeTo(parent);
		getContentPane().setLayout(null);
		
		txtPath = new JTextField();
		txtPath.setBounds(10, 11, 324, 20);
		getContentPane().add(txtPath);
		txtPath.setColumns(10);
		
		JButton btnBrowse = new JButton("Browse...");
		btnBrowse.setBounds(10, 36, 89, 23);
		getContentPane().add(btnBrowse);
		btnBrowse.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				action_onBrowse();
			}
		});
		
		JButton btnCancel = new JButton("Cancel");
		btnCancel.setBounds(245, 36, 89, 23);
		getContentPane().add(btnCancel);
		btnCancel.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				action_onCancel();
			}
		});
		
		JButton btnOkay = new JButton("OK");
		btnOkay.setBounds(150, 36, 89, 23);
		getContentPane().add(btnOkay);
		btnCancel.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				action_onOkay();
			}
		});
	}

	public boolean getCloseSelection(){return okay;}
	public String getPathValue(){return value;}
	
	public void action_onOkay(){
		okay = true;
		value = txtPath.getText();
		setVisible(false);
		dispose();
	}
	
	public void action_onCancel(){
		okay = false;
		value = null;
		setVisible(false);
		dispose();
	}
	
	public void action_onBrowse(){
		JFileChooser fc = new JFileChooser(txtPath.getText());
		fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
		int res = fc.showOpenDialog(this);
		
		if(res == JFileChooser.APPROVE_OPTION){
			txtPath.setText(fc.getSelectedFile().getAbsolutePath());
			txtPath.repaint();
		}
	}
	
}
