import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.text.DefaultCaret;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class GundySpider extends JFrame {

	private static final int BUFFER_SIZE = 4096;
	private final String USER_AGENT = "Googlebot/2.1 (+http://www.google.com/bot.html)";
	private final JPanel contentPane;
	private final JTextField txtAddress;
	private final JTextField txtFileType;
	private final DefaultListModel<String> model;
	private final JTextArea status;
	private final JProgressBar progressBar;
	private final JList<String> linkList;
	private final JComboBox<String> comboBox;
	private Thread ThrdSearch, ThrdDownload;
	int bingPage=1,yandexPage=0,proxyCounter;
	Document doc;
	private List<String> proxyList;

	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			@Override
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
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowOpened(WindowEvent arg0) {
				txtAddress.setEnabled(false);
				txtFileType.setEnabled(false);
				comboBox.setEnabled(false);
				new Thread(new Runnable() {
					@Override
					public void run() {
						proxyBulo();
					}
				}).start();
			}
		});
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
			JOptionPane.showMessageDialog(null, "Errör : \n " + e.toString(), "Errör", JOptionPane.ERROR_MESSAGE);
		}
		
		setLocationRelativeTo(null);
		JScrollPane scrollPane_1 = new JScrollPane();
		scrollPane_1.setBounds(75, 247, 551, 160);
		contentPane.add(scrollPane_1);
		//scrollPane_1.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		
		status = new JTextArea();
		status.setEditable(false);
		scrollPane_1.setViewportView(status);
		status.setBackground(new Color(0, 0, 0));
		status.setForeground(new Color(0, 128, 0));
		status.setFont(new Font("Tahoma", Font.BOLD, 12));
		DefaultCaret caret = (DefaultCaret)status.getCaret();  
		caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
		
		JLabel lblUrl = new JLabel("URL");
		lblUrl.setFont(new Font("Tahoma", Font.BOLD, 13));
		lblUrl.setBounds(10, 71, 46, 14);
		contentPane.add(lblUrl);

		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(74, 71, 551, 160);
		contentPane.add(scrollPane);

		linkList = new JList<String>();
		linkList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		scrollPane.setViewportView(linkList);
		linkList.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				if ((linkList.getSelectedIndex() != -1) && (JOptionPane.showConfirmDialog(null, "Are You Sure For Download ?") == 0)) {
					status.setText(status.getText() + "\nDownloading to " + linkList.getSelectedValue());

					ThrdDownload = new Thread(new Runnable() {
						@Override
						public void run() {
							while (true)
								try {
									downloadFile(linkList.getSelectedValue(), "");
								} catch (Exception e) {
									JOptionPane.showMessageDialog(null, "Errör : \n " + e.toString(), "Errör", JOptionPane.ERROR_MESSAGE);
								}
							// Thread.sleep(5);
						}
					});
					ThrdDownload.start();
				}
			}
		});

		JLabel lblAdres = new JLabel("Address");
		lblAdres.setFont(new Font("Tahoma", Font.BOLD, 13));
		lblAdres.setBounds(10, 37, 54, 14);
		contentPane.add(lblAdres);

		txtAddress = new JTextField();
		txtAddress.setFont(new Font("Tahoma", Font.BOLD, 12));
		txtAddress.setBounds(74, 33, 150, 23);
		contentPane.add(txtAddress);
		txtAddress.setColumns(10);

		JLabel lblDosyaTipi = new JLabel("File Type");
		lblDosyaTipi.setFont(new Font("Tahoma", Font.BOLD, 13));
		lblDosyaTipi.setBounds(240, 37, 55, 14);
		contentPane.add(lblDosyaTipi);

		model = new DefaultListModel<String>();
		linkList.setModel(model);
		JButton btnAra = new JButton("Search");

		btnAra.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				model.clear();
				proxyCounter=0; yandexPage=0; bingPage=0;
				ThrdSearch = new Thread(new Runnable() {
					@Override
					public void run() {
						search(linkList);
					}
				});
				ThrdSearch.start();
			}
		});
		btnAra.setBounds(545, 31, 80, 25);
		contentPane.add(btnAra);

		txtFileType = new JTextField();
		txtFileType.setFont(new Font("Tahoma", Font.BOLD, 12));
		txtFileType.setColumns(10);
		txtFileType.setBounds(307, 33, 73, 23);
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
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if ((linkList.getModel().getSize() != 0) && (JOptionPane.showConfirmDialog(null, "Are You Sure Download All Items ?") == 0)) {
					ThrdDownload = new Thread(new Runnable() {
						@Override
						public void run() {
							int i = 0;
							while (true && i < linkList.getModel().getSize())
								try {
									status.setText(status.getText() + "\nDownloading to " + linkList.getModel().getElementAt(i));
									downloadFile(linkList.getModel().getElementAt(i), "");
									i++;
								} catch (Exception e) {
									JOptionPane.showMessageDialog(null,"Errör : \n " + e.toString(),"Errör", JOptionPane.ERROR_MESSAGE);
								}
							// Thread.sleep(5);
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
				JOptionPane.showMessageDialog(null,
								"******************************** \n\n| <<<  YUNUS YILDIRIM  >>>|\n\n********************************",
								"Coder", JOptionPane.INFORMATION_MESSAGE);
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
		
		JLabel lblSite = new JLabel("Site");
		lblSite.setFont(new Font("Tahoma", Font.BOLD, 13));
		lblSite.setBounds(390, 35, 46, 14);
		contentPane.add(lblSite);
		
		comboBox = new JComboBox<String>();
		comboBox.setModel(new DefaultComboBoxModel<String>(new String[] {"Google", "Yandex", "Bing"}));
		comboBox.setBounds(435, 32, 94, 23);
		contentPane.add(comboBox);
	}

	public void search(JList<String> list) {
		if (!(txtAddress.getText().isEmpty() || txtFileType.getText().isEmpty())) {
			status.setText(null);
			status.setText("Connecting to " +comboBox.getSelectedItem()+ " \n");
			status.setText(status.getText() + "Starting Search ...\n\n");
			status.setText(status.getText() + "Sending 'GET' request to  :\n   ");
			long start = System.currentTimeMillis();
			if(comboBox.getSelectedIndex() == 0){	
				// Google
				yandexPage = 0;
				while (yandexPage < 500) {
					try {
						googleSearch(txtAddress.getText(), txtFileType.getText(), yandexPage);
					} catch (Exception e) {
						JOptionPane.showMessageDialog(null,"Errör : \n" + e.toString());
					}
					yandexPage += 100;
					progressBar.setValue((int) (yandexPage * 0.25));
				}//yandex
			} else if(comboBox.getSelectedIndex() == 1)
				//searchYandex();
				JOptionPane.showMessageDialog(null, "Yandex Arama Modülü Geçici Bir Süre Kullanýlamýyor.","Ops!", JOptionPane.INFORMATION_MESSAGE);
			else if(comboBox.getSelectedIndex() == 2)
				searchBing();
				//bing
			linkList.setModel(model);
			status.setText(status.getText() + "\n\nAnalysing URL...\n");
			long end = System.currentTimeMillis();
			status.setLineWrap(true);
			status.setWrapStyleWord(true);
			status.setText(status.getText() + "\nTotal number of founded URL : " + list.getModel().getSize() + "\n");
			status.setText(status.getText() + "Total time : " + (float) (end - start) / 1000 + " sn\n");
			status.setText(status.getText() + "\nFor Downloading Clicked URL on List\n");
		} else
			JOptionPane.showMessageDialog(null,"Please Enter the Address and File Type", "Missing Value",
					JOptionPane.WARNING_MESSAGE);
	}

	public void googleSearch(String site, String fileType, int page)	throws Exception {

		String url = "http://www.google.com/search?num=500&q=site:" + site + "+filetype:" + fileType + "&start=" + page;
		try {
			URL obj = new URL(url);
			HttpURLConnection con = (HttpURLConnection) obj.openConnection();

			con.setRequestMethod("GET");

			con.setRequestProperty("User-Agent", USER_AGENT);

			int responseCode = con.getResponseCode();
			status.setText(status.getText() + "\n   " + url);

			if (responseCode == HttpURLConnection.HTTP_OK) {
				BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
				String inputLine = "";
				String regex = "(><a href=\"/url\\?q=)" + "(\\S+)" + "(&amp;sa=U&amp;)";
				Pattern p = Pattern.compile(regex);
				Matcher m = p.matcher(inputLine);

				while ((inputLine = in.readLine()) != null) {
					m = p.matcher(inputLine);
					if (m.find())
						model.addElement(m.group(2));
				}
				in.close();
			} else {
				JOptionPane.showMessageDialog(null,"Bad Connection. Responce Code : " + responseCode,
						"Errör", JOptionPane.ERROR_MESSAGE);
				System.exit(0);
			}
		} catch (Exception e) {
			JOptionPane.showMessageDialog(null, "Errör : " + e.getMessage(),"Errör", JOptionPane.ERROR_MESSAGE);
		}
		txtAddress.setEnabled(true);
		txtFileType.setEnabled(true);
		linkList.setModel(model);
	}
	
	public void searchYandex(){
		try {
			model.clear();
			String url = "http://www.yandex.com.tr/search?text=site:"+txtAddress.getText()+"+mime:"+txtFileType.getText()+"&numdoc=100&p="+yandexPage;
			while(yandexPage < 5){
				status.setText(status.getText() + "\n   " + url);
				doc = Jsoup.connect(url).userAgent("Mozilla/5.0 (Windows NT 6.0; rv:29.0) Gecko/20100101 Firefox/29.0").get();
				if(doc.text().contains("IP") && doc.text().contains("Yandex")){
					status.setText(status.getText() + "\n" + "Arama Motoru Tarafýndan Banlandýk.!!");
					status.setText(status.getText() + "\n" + "Proxy Adresleri Kullanýlýyor...");
					String proxy[];
					proxy = proxyList.get(proxyCounter).split(":");
					System.setProperty("http.proxyHost",proxy[0]);
					System.setProperty("http.proxyPort",proxy[1]);
					proxyCounter++;
					searchYandex();
				}
				yandexPage++;
				Elements links = doc.select("div h2 a");
				System.out.println(doc.text());
				for(Element link: links){
					model.addElement(link.attr("href"));
					System.out.println(link.attr("href"));
				}
				progressBar.setValue((yandexPage * 25));
				
				url = "http://www.yandex.com.tr/search?text=site:"+txtAddress.getText()+"+mime:"+txtFileType.getText()+"&numdoc=100&p="+yandexPage;				
			}
		} catch (IOException e) {
			status.setText(status.getText() + "\n" + "Error : " + e.getMessage());
			//JOptionPane.showMessageDialog(null, "Error : " + e.getMessage() , "Error", JOptionPane.ERROR_MESSAGE);
			if(e.getMessage().contains("timed out"))
				searchYandex();
		  }
		linkList.setModel(model);
	}
	
	public void searchBing(){
		try {
			model.clear();
			String url = "http://www.bing.com/search?q=site:"+txtAddress.getText()+"+filetype:"+txtFileType.getText()+"&first="+bingPage;
			while(bingPage < 200){
			status.setText(status.getText() + "\n   " + url);
			doc = Jsoup.connect(url).userAgent("Mozilla/5.0 (Windows NT 6.0; rv:29.0) Gecko/20100101 Firefox/29.0").get();
			//System.out.println(doc.text());
			
			if(doc.text().contains("Microsoft") && doc.text().contains("Bing") && !doc.text().contains(txtAddress.getText())){
				status.setText(status.getText() + "\n" + "Arama Motoru Tarafýndan Banlandýk.!!");
				status.setText(status.getText() + "\n" + "Proxy Adresleri Kullanýlýyor...");
				String proxy[];
				proxy = proxyList.get(proxyCounter).split(":");
				System.setProperty("http.proxyHost",proxy[0]);
				System.setProperty("http.proxyPort",proxy[1]);
				proxyCounter++;
				searchBing();
			}
			bingPage += 10;
			Elements links = doc.select("li.b_algo h2 a[href]");
			for(Element link: links)
				model.addElement(link.attr("href"));
			linkList.setModel(model);
			progressBar.setValue((int) (bingPage * 0.2));
			
			url = "http://www.bing.com/search?q=site:"+txtAddress.getText()+"+filetype:"+txtFileType.getText()+"&first="+bingPage;
			}
		} catch (IOException e) {
			status.setText(status.getText() + "\n" + "Error : " + e.getMessage());
			//JOptionPane.showMessageDialog(null, "Error : " + e.getMessage() , "Error", JOptionPane.ERROR_MESSAGE);
			if(e.getMessage().contains("timed out"))
				searchBing();
		  }
		linkList.setModel(model);
	}	
	
	public void proxyBulo(){
		proxyList = new ArrayList<String>();
		int pagee=1;
		status.setText(null);
		status.setText("Proxy Adresleri Alýnýyor...");
		while( pagee<=2 ){
			try {
				doc = Jsoup.connect("http://proxy-list.org/english/index.php?p="+pagee).userAgent("Mozilla/5.0 (compatible; MSIE 10.0; Windows NT 6.1; WOW64; Trident/6.0)").get();
				Elements proxys = doc.select("li.proxy");
				for (Element list: proxys){
					proxyList.add(list.text());
					status.setText(status.getText() + "\n   " + list.text());
				}
			} catch (IOException e) {
				JOptionPane.showMessageDialog(null, "Error : " + e.getMessage() , "Error", JOptionPane.ERROR_MESSAGE);
				  proxyBulo();
			   }
			pagee++;
		}
		if(proxyList.size()>15){
			proxyList.remove(0);
			proxyList.remove(14);
			txtAddress.setEnabled(true);
			txtFileType.setEnabled(true);
			comboBox.setEnabled(true);
		} else
			proxyBulo();
		status.setText(status.getText() + "\n" + "\nProxy Adresleri Tamamlandý...");
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
				if (index > 0)
					fileName = disposition.substring(index + 10,disposition.length() - 1);
			} else
				// extracts file name from URL
				fileName = fileURL.substring(fileURL.lastIndexOf("/") + 1,fileURL.length());
			status.setText(status.getText() + "\nContent-Type = " + contentType);
			status.setText(status.getText() + "\nFile-Length = "  + contentLength / 1024 + " Kb");

			saveDir += "GundySpider";
			if (!(new File(saveDir).exists())) {
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
			float sum = 0;
			byte[] buffer = new byte[BUFFER_SIZE];

			while ((bytesRead = inputStream.read(buffer)) != -1) {
				outputStream.write(buffer, 0, bytesRead);
				sum += bytesRead;
				// System.out.printf( "%.2f\n", sum/(float)contentLength*100 );
				progressBar.setValue((int) (sum / contentLength * 100));
			}
			outputStream.close();
			inputStream.close();
			status.setText(status.getText() + "\nFile Succesfully Downloaded to " + saveDir + "\n");
		} else
			status.setText(status.getText() + "File could not be downloaded. Server replied HTTP code: "
					+ responseCode);
		httpConn.disconnect();
		ThrdDownload.stop();
	}
}