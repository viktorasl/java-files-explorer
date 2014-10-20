/*
 * Informatika, 2 kursas, 2 grupe
 * Viktoras Laukevièius
 * Files explorer
 *
 * v2.0
 * - Added double click support
 * - Added up folder button support
 * - Added files execution support (for Mac or for Windows only)
 */

import java.io.File;
import java.awt.FlowLayout;
import javax.swing.JFrame;
import javax.swing.JTextField;
import javax.swing.JTable;
import javax.swing.JScrollPane;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseAdapter;
import java.text.DecimalFormat;
import javax.swing.table.DefaultTableModel;
import java.util.ArrayList;
import java.lang.Runtime;

/**
 * Files table model class intended to modify default
 * table
 */
class FilesTableModel extends DefaultTableModel{
	
	public FilesTableModel(String[] columnNames, int i){
		super(columnNames, i);
	}
	
	@Override
	public boolean isCellEditable(int row, int column) {                
		return false;
	};
}

/**
 * Program allows user to write file path in text box,
 * get files and folders list (and information related to 
 * them: name, type, normalized size and size in bytes) 
 * by clicking enter.
 */
public class FilesExplorer {
	
	JFrame guiFrame;
	JButton upBtn;
	JTextField pathTF;
	DefaultTableModel table;
	ArrayList<Object[]> filesList;
	String currentPath;
	
	/**
	*/
	public static void main (String[] args){
		FilesExplorer fx = new FilesExplorer();
	}
	
	public FilesExplorer(){
		this.guiFrame = new JFrame();
		this.guiFrame.getContentPane().setLayout(new FlowLayout());
		this.guiFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.guiFrame.setTitle("Files Explorer");
		this.guiFrame.setSize(500, 600);
		
		this.currentPath = ".";
		
		// initializing up folder button
		this.upBtn = new JButton("Up");
		this.upBtn.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				int index = currentPath.lastIndexOf('\\');
				if (index > 0){
					printFiles(currentPath.substring(0, index));
				}
			}
		});
		
		// initializing path text field
		this.pathTF = new JTextField("", 20);
		this.pathTF.addActionListener(new ActionListener(){
         public void actionPerformed(ActionEvent e){
				printFiles(pathTF.getText());
         }
      });
		
		// initializing files list table
		String[] columnNames = {"Name", "Size", "Type", "Size in bytes"};
		this.table = new FilesTableModel(columnNames, 0);
		JTable dataTable = new JTable(this.table);
		
		dataTable.addMouseListener(new MouseAdapter(){
			public void mouseClicked(MouseEvent e){
				if (e.getClickCount() == 2){
					JTable target = (JTable)e.getSource();
					int row = target.getSelectedRow();
					
					analyzeRow(row);
				}
			}
		});
		
		// initializing files list table scroller
		JScrollPane scrollPane = new JScrollPane(dataTable);
		
		this.guiFrame.getContentPane().add(this.upBtn);
		this.guiFrame.getContentPane().add(this.pathTF);
		this.guiFrame.getContentPane().add(scrollPane);
		this.guiFrame.setVisible(true);
	}
	
	/**
	 */
	public void printFiles(String path){
		
		this.table.setRowCount(0);
		this.filesList = new ArrayList<Object[]>();
		
		String fileName;
		long fileSize;
		
		File folder = new File(path);
		File[] files = folder.listFiles();
		
		if (files != null){
						
			try {
				pathTF.setText(folder.getCanonicalPath());
				this.currentPath = folder.getCanonicalPath();
			}
			catch(Exception e) {}
			
			
			for (int i = 0; i < files.length; i++){
				fileName = files[i].getName();
				fileSize = files[i].length();
				
				if (files[i].isFile()){
					int index = fileName.lastIndexOf('.');
					Object[] fileInfo = new Object[]{
						fileName,
						normalizeSize(fileSize),
						(index >= 0)? fileName.substring(index) : "",
						fileSize
					};
					
					this.table.addRow(fileInfo);
					this.filesList.add(fileInfo);
				}else if (files[i].isDirectory()){
					long size = folderSize(files[i].getAbsolutePath());
					Object[] fileInfo = new Object[]{
						fileName,
						normalizeSize(size),
						"File folder",
						size
					};
					
					this.table.addRow(fileInfo);
					this.filesList.add(fileInfo);
				}
			}
		}else {
			JOptionPane.showMessageDialog(this.guiFrame, "Wrong folder path");
		}
		
	}
	
	/**
	 */
	public static long folderSize(String path){
		long size = 0;
		
		File folder = new File(path);
		File[] files = folder.listFiles();
		
		if (files != null){
			for (int i = 0; i < files.length; i++){
				
				String fileName = files[i].getName();
				long fileSize = files[i].length();
				
				if (files[i].isFile()){
					size += fileSize;
				}else if (files[i].isDirectory()){
					size += folderSize(files[i].getAbsolutePath());
				}
				
			}
		}
		
		return size;
	}
	
	/**
	 */
	public static String normalizeSize(long bytes){
		String names[] = {"bytes", "KB", "MB", "GB", "TB", "PB"};
		int i = 0;
		long size = bytes;
		float _bytes = bytes;
		
		while ((size = size / 1024) > 0){
			_bytes = _bytes / 1024;
			i++;
		}
		
		DecimalFormat df = new DecimalFormat("0.00");
		return df.format(_bytes) + " " + names[i];
	}
	
	/**
	 */
	public void analyzeRow(int num){
		String type = (String)this.filesList.get(num)[2];
		String name = (String)this.filesList.get(num)[0];
		
		if (type == "File folder"){
			this.printFiles(this.currentPath + "/" + name);
		}else {
			try {
				Runtime.getRuntime().exec(new String[] {"cmd.exe", "/C", this.currentPath + "/" + name}); //Runs only on Windows, other OS'es throw exception
				//Runtime.getRuntime().exec(new String[] {"open", this.currentPath + "/" + name}); //Runs only on Mac, other OS'es throw exception
			}catch (Throwable e){
				JOptionPane.showMessageDialog(this.guiFrame, "Could not open the file " + this.currentPath + "/" + name);
			}
		}
	}
	
}
