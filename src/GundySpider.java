import java.awt.EventQueue;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JLabel;
import java.awt.Font;

import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JButton;
import javax.swing.JTextArea;
import javax.swing.UIManager;
import java.awt.Color;
import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JProgressBar;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import javax.swing.ListSelectionModel;


public class GundySpider extends JFrame {

    private static final int BUFFER_SIZE = 4096;
	private final String USER_AGENT = "Mozilla/5.0";
	private JPanel contentPane;
	private JTextField txtAddress;
	private JTextField txtFileType;
	private DefaultListModel<String> model;
	private JTextArea status;
	private JProgressBar progressBar;
	private Thread ThrdSearch,ThrdDownload;
	int page;
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					GundySpider frame = new GundySpider();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	public GundySpider() {
		setResizable(false);
		setTitle("GundySpider");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 669, 479);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			JOptionPane.showMessageDialog(null, "Err�r : \n " + e.toString() ,"Err�r", JOptionPane.ERROR_MESSAGE);
		}
		
		JScrollPane scrollPane_1 = new JScrollPane();
		scrollPane_1.setBounds(75, 247, 551, 160);
		contentPane.add(scrollPane_1);
		
		status = new JTextArea();
		scrollPane_1.setViewportView(status);
		status.setEnabled(false);
		status.setBackground(new Color(0, 0, 0));
		status.setForeground(Color.WHITE);
		status.setFont(new Font("Tahoma", Font.BOLD, 12));
		
		JLabel lblUrl = new JLabel("URL");
		lblUrl.setFont(new Font("Tahoma", Font.BOLD, 13));
		lblUrl.setBounds(10, 71, 46, 14);
		contentPane.add(lblUrl);
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(74, 71, 551, 160);
		contentPane.add(scrollPane);
		
		final JList<String> list = new JList<String>();
		list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		scrollPane.setViewportView(list);
		list.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				if( (list.getSelectedIndex() != -1) && (JOptionPane.showConfirmDialog(null, "Are You Sure For Download ?") == 0 ) ){
					status.setText(status.getText()+ "\nDownloading to " + list.getSelectedValue());
					
						ThrdDownload = new Thread(new Runnable() {
				            @Override
				            public void run() {
				                while(true){
				                	try {
										downloadFile(list.getSelectedValue(), "");
									} catch (Exception e) {
										JOptionPane.showMessageDialog(null, "Err�r : \n " + e.toString() ,"Err�r", JOptionPane.ERROR_MESSAGE);
									}
				                        //Thread.sleep(5);
				                }
				            }
				        });
				        ThrdDownload.start();
				}
			}
		});
		
		JLabel lblAdres = new JLabel("Address");
		lblAdres.setFont(new Font("Tahoma", Font.BOLD, 13));
		lblAdres.setBounds(10, 35, 54, 14);
		contentPane.add(lblAdres);
		
		txtAddress = new JTextField();
		txtAddress.setFont(new Font("Tahoma", Font.BOLD, 12));
		txtAddress.setBounds(74, 33, 213, 23);
		contentPane.add(txtAddress);
		txtAddress.setColumns(10);
		
		JLabel lblDosyaTipi = new JLabel("File Type");
		lblDosyaTipi.setFont(new Font("Tahoma", Font.BOLD, 13));
		lblDosyaTipi.setBounds(297, 35, 65, 14);
		contentPane.add(lblDosyaTipi);
		
		model = new DefaultListModel<String>();
		list.setModel(model);		
		JButton btnAra = new JButton("Search");
		btnAra.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent arg0) {
				model.clear();
				progressBar.setValue(0);
				if(arg0.getKeyCode() == KeyEvent.VK_ENTER){
					
					if(!( txtAddress.getText().isEmpty() || txtFileType.getText().isEmpty() )){
						txtAddress.setEnabled(false);
						txtFileType.setEnabled(false);
						status.setText(null);
						status.setText("Connecting to Google \n");
						status.setText(status.getText() +  "Starting Search ...\n\n");
						status.setText(status.getText() + "Sending 'GET' request to  :\n   " );
						long start = System.currentTimeMillis();
						  page=0;
						  while(page < 500 ){
							try {
							    sendGet(txtAddress.getText(), txtFileType.getText(), page);
							    //progressBar.setValue( (page%10)*2 );
							} catch (Exception e) {
								JOptionPane.showMessageDialog(null, "Err�r : \n" + e.toString());
							}
							page += 100;
						  }
						  //progressBar.setValue((int) (page%10)*2);
						  progressBar.setValue(100);
						status.setText(status.getText() + "\n\nAnalysing URL...\n");
						long end = System.currentTimeMillis();
						status.setText(status.getText() + "\nTotal number of founded URL : " + list.getModel().getSize() +"\n");
						status.setText(status.getText() + "Total time : " + (float)(end-start)/1000 + " sn\n");
						status.setText(status.getText() + "\nFor Downloading Clicked URL on List\n");
						} else {
							JOptionPane.showMessageDialog(null, "Please Enter the Address and File Type", "Missing Value", JOptionPane.WARNING_MESSAGE);
						  }
				}
			}
		});
		
		btnAra.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				model.clear();
				progressBar.setValue(0);
				 if(!( txtAddress.getText().isEmpty() || txtFileType.getText().isEmpty() )){
					txtAddress.setEnabled(false);
					txtFileType.setEnabled(false);
					status.setText(null);
					status.setText("Connecting to Google \n");
					status.setText(status.getText() +  "Starting Search ...\n\n");
					status.setText(status.getText() + "Sending 'GET' request to  :\n   " );
					long start = System.currentTimeMillis();
					  page=0;
					  while(page < 500 ){
						try {
						    sendGet(txtAddress.getText(), txtFileType.getText(), page);
						} catch (Exception e) {
							JOptionPane.showMessageDialog(null, "Err�r : \n" + e.toString());
						}
						page += 100;
					  }
					status.setText(status.getText() + "\n\nAnalysing URL...\n");
					long end = System.currentTimeMillis();
					status.setText(status.getText() + "\nTotal number of founded URL : " + list.getModel().getSize() +"\n");
					status.setText(status.getText() + "Total time : " + (float)(end-start)/1000 + " sn\n");
					status.setText(status.getText() + "\nFor Downloading Clicked URL on List\n");
				 } else {
					 JOptionPane.showMessageDialog(null, "Please Enter the Address and File Type", "Missing Value", JOptionPane.WARNING_MESSAGE);
				 }
			}
		});
		btnAra.setBounds(545, 32, 80, 23);
		contentPane.add(btnAra);
		
		txtFileType = new JTextField();
		txtFileType.setFont(new Font("Tahoma", Font.BOLD, 12));
		txtFileType.setColumns(10);
		txtFileType.setBounds(361, 32, 162, 23);
		contentPane.add(txtFileType);
		
		JLabel lblDurum = new JLabel("Status");
		lblDurum.setFont(new Font("Tahoma", Font.BOLD, 13));
		lblDurum.setBounds(10, 247, 46, 14);
		contentPane.add(lblDurum);
		
		JMenuBar menuBar = new JMenuBar();
		menuBar.setBounds(0, 0, 679, 21);
		contentPane.add(menuBar);
		
		JMenu mnFile = new JMenu("File");
		menuBar.add(mnFile);
		
		JMenuItem mnitmSaveAll = new JMenuItem("Save All");
		mnitmSaveAll.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
			   if( (list.getModel().getSize() != 0) && (JOptionPane.showConfirmDialog(null, "Are You Sure Download All Items ?") == 0 ) ){
					ThrdDownload = new Thread(new Runnable() {
			            @Override
			            public void run() {
			            	int i=0;
			                while(true && i<list.getModel().getSize()){
			                	try {
			                		status.setText(status.getText()+ "\nDownloading to " + list.getModel().getElementAt(i));
									downloadFile(list.getModel().getElementAt(i), "");
									i++;
								} catch (Exception e) {
									JOptionPane.showMessageDialog(null, "Err�r : \n " + e.toString() ,"Err�r", JOptionPane.ERROR_MESSAGE);
								}
			                        //Thread.sleep(5);
			                }
			            }
			        });
			        ThrdDownload.start();
			   }
			}
		});
		mnFile.add(mnitmSaveAll);
		
		JMenu mnAbout = new JMenu("About");
		mnAbout.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				JOptionPane.showMessageDialog(null, "******************************** \n\n| <<<  YUNUS YILDIRIM  >>>|\n\n********************************","Coder",JOptionPane.INFORMATION_MESSAGE);
			}
		});
		menuBar.add(mnAbout);
		
		JMenu mnClose = new JMenu("Close");
		mnClose.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				System.exit(0);
			}
		});
		menuBar.add(mnClose);
		
		progressBar = new JProgressBar();
		progressBar.setStringPainted(true);
		progressBar.setBounds(75, 415, 550, 27);
		contentPane.add(progressBar);
	}	
	public void sendGet(String site,String fileType,int page) throws Exception {
		
		String url = "http://www.google.com/search?num=500&q=site:"+site+"+filetype:"+fileType+"&start="+page;
     try {
		URL obj = new URL(url);
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();
 
		// optional default is GET
		con.setRequestMethod("GET");
 
		//add request header
		con.setRequestProperty("User-Agent", USER_AGENT);
 
		int responseCode = con.getResponseCode();
		status.setText(status.getText() + "\n   " + url);	
		
	if(responseCode == HttpURLConnection.HTTP_OK){
		
		BufferedReader in = new BufferedReader( new InputStreamReader(con.getInputStream()) );
		String inputLine = "";
		String regex = "(><a href=\"/url\\?q=)"+"(\\S+)"+"(&amp;sa=U&amp;)";
		Pattern p = Pattern.compile(regex);
		Matcher m = p.matcher(inputLine);
		
		while ((inputLine = in.readLine()) != null) {
			m = p.matcher(inputLine);
			if (m.find()) {
				model.addElement(m.group(2));
			}
		}
		in.close();
	} else {
		JOptionPane.showMessageDialog(null, "Bad Connection. Responce Code : " + responseCode , "Err�r", JOptionPane.ERROR_MESSAGE);
		System.exit(0);
	}
     } catch (Exception e) {
		JOptionPane.showMessageDialog(null, "Err�r : " + e.getMessage() , "Err�r", JOptionPane.ERROR_MESSAGE);
	  }
     txtAddress.setEnabled(true);
	 txtFileType.setEnabled(true);
     //ThrdSearch.stop();
   }
	
	public void downloadFile(String fileURL, String saveDir) throws IOException {

        URL url = new URL(fileURL.trim());
        HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
        int responseCode = httpConn.getResponseCode();
 
        // check HTTP response code
        if (responseCode == HttpURLConnection.HTTP_OK) {
            String fileName = "";
            String disposition = httpConn.getHeaderField("Content-Disposition");
            String contentType = httpConn.getContentType();
            int contentLength = httpConn.getContentLength();

            if (disposition != null) {
                // extracts file name from header field
                int index = disposition.indexOf("filename=");
                if (index > 0) {
                    fileName = disposition.substring(index + 10,
                            disposition.length() - 1);
                }
            } else {
                // extracts file name from URL
                fileName = fileURL.substring(fileURL.lastIndexOf("/") + 1,
                        fileURL.length());
            }
           status.setText(status.getText() + "\nContent-Type = " + contentType);
            status.setText(status.getText() + "\nFile-Length = " + contentLength/1024 + " Kb");
            
            saveDir += "GundySpider";
            if(!( new File(saveDir).exists() )){
            	new File(saveDir).mkdir();
            	status.setText(status.getText() + "\nCreating \"GundySpider\" folder in project folder.");
            }
            // opens input stream from the HTTP connection
            InputStream inputStream = httpConn.getInputStream();
            String saveFilePath = saveDir + File.separator + fileName;
             
            // opens an output stream to save into file
            FileOutputStream outputStream = new FileOutputStream(saveFilePath);
            status.setText(status.getText() + "\nDownloading ...");
            int bytesRead = -1;
            float sum=0;
            byte[] buffer = new byte[BUFFER_SIZE];
            
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
                sum += bytesRead;
               // System.out.printf( "%.2f\n", sum/(float)contentLength*100 );
                progressBar.setValue((int) (sum/contentLength*100));
            }
            outputStream.close();
            inputStream.close();
            status.setText(status.getText() + "\nFile Succesfully Downloaded to " + saveDir +"\n");
        } else {
            status.setText(status.getText() + "File could not be downloaded. Server replied HTTP code: " + responseCode);
        }
        httpConn.disconnect();
        ThrdDownload.stop();
	}
	
	
}
