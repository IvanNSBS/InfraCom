import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.Socket;
import javax.swing.*;

public class ClientExample extends JFrame implements ActionListener{
	private Socket socket;
	private DataOutputStream ou;
	private Writer ouw;
	private BufferedWriter bfw;
	
	private String name = "User";
	private String ip = "localhost";
	
	public Runnable listener;
	public Runnable writer;
	
	private static final long serialVersionUID = 1L;
	private JTextArea texto;
	private JTextField txtMsg;
	private JButton btnSend;
	private JButton btnSair;
	private JLabel lblHistorico;
	private JLabel lblMsg;
	private JPanel pnlContent;
	private JTextField txtIP;
	private JTextField txtPorta;
	private JTextField txtNome;
	
	int port = 3000;
	
	  public ClientExample() throws IOException{                  
		    JLabel lblMessage = new JLabel("Verificar!");
		    txtIP = new JTextField("127.0.0.1");
		    txtPorta = new JTextField("12345");
		    txtNome = new JTextField("Cliente");                
		    Object[] texts = {lblMessage, txtIP, txtPorta, txtNome };  
		    JOptionPane.showMessageDialog(null, texts);              
		    pnlContent = new JPanel();
		    texto              = new JTextArea(10,20);
		    texto.setEditable(false);
		    texto.setBackground(new Color(240,240,240));
		    txtMsg                       = new JTextField(20);
		    lblHistorico     = new JLabel("Histórico");
		    lblMsg        = new JLabel("Mensagem");
		    btnSend                     = new JButton("Enviar");
		    btnSend.setToolTipText("Enviar Mensagem");
		    btnSair           = new JButton("Sair");
		    btnSair.setToolTipText("Sair do Chat");
		    btnSend.addActionListener(this);
		    btnSair.addActionListener(this);
		    JScrollPane scroll = new JScrollPane(texto);
		    texto.setLineWrap(true);  
		    pnlContent.add(lblHistorico);
		    pnlContent.add(scroll);
		    pnlContent.add(lblMsg);
		    pnlContent.add(txtMsg);
		    pnlContent.add(btnSair);
		    pnlContent.add(btnSend);
		    pnlContent.setBackground(Color.LIGHT_GRAY);                                 
		    texto.setBorder(BorderFactory.createEtchedBorder(Color.BLUE,Color.BLUE));
		    txtMsg.setBorder(BorderFactory.createEtchedBorder(Color.BLUE, Color.BLUE));                    
		    setTitle(txtNome.getText());
		    setContentPane(pnlContent);
		    setLocationRelativeTo(null);
		    setResizable(false);
		    setSize(250,300);
		    setVisible(true);
		    setDefaultCloseOperation(EXIT_ON_CLOSE);
		}

	public void conectar() throws IOException {

		socket = new Socket(ip, port);
		ou = new DataOutputStream(socket.getOutputStream());
		ouw = new OutputStreamWriter(ou);
		bfw = new BufferedWriter(ouw);
		bfw.write(name + ": \r\n");
		bfw.flush();
		
	}

	public void enviarMensagem(String msg) throws IOException {
		if (msg.equals("Sair")) {
			msg = "Desconectado \r\n";
			bfw.write(msg);
		} 
		else {
			bfw.write(msg + "\r\n"); 
		}
		bfw.flush();
	}

	public void escutar() throws IOException {
		listener = new Runnable() {
			public void run() {
				InputStream in;
				try {
					in = socket.getInputStream();
					InputStreamReader inr = new InputStreamReader(in);
					BufferedReader bfr = new BufferedReader(inr);
					String msg = "";
					while (!"Sair".equalsIgnoreCase(msg)) {
						if (bfr.ready()) {
							msg = bfr.readLine();
							if (msg.equals("Sair"))
								System.out.println("Servidor caiu! \r\n");
							else
								System.out.println(msg + "\r\n");
						}
					}
				} 
				catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		};
	}
	
	public void escrever() {
//		writer = new Runnable() {
//			public void run() {
//				String line = "";
//				while(!line.equalsIgnoreCase("Sair")) {
//					try {
//					    InputStreamReader streamReader = new InputStreamReader(System.in);
//					    BufferedReader bufferedReader = new BufferedReader(streamReader);
//					    line = bufferedReader.readLine();
//						enviarMensagem(line);
//						System.out.println(name + ": " + line);
//					} catch (IOException e) {
//					    e.printStackTrace();
//					}
//				}
//			}
//		};
	}

	public void sair() throws IOException {
		enviarMensagem("Sair");
		bfw.close();
		ouw.close();
		ou.close();
		socket.close();
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
	          
				try {
					if(e.getActionCommand().equals(btnSend.getActionCommand()))
					   enviarMensagem(txtMsg.getText());
					else
					   if(e.getActionCommand().equals(btnSair.getActionCommand()))
					   sair();
				} 
				catch (IOException e1) 
				{
				     // TODO Auto-generated catch block
				     e1.printStackTrace();
				} 
	}

	public static void main(String[] args) throws IOException {

		ClientExample app = new ClientExample();
		app.conectar();
		app.escutar();
		app.escrever();
        new Thread(app.listener).start(); // start listener
        new Thread(app.writer).start(); // start writer
		
	}

}