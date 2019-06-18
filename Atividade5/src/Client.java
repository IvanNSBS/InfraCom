import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.Socket;
import javax.swing.*;

public class Client extends JFrame implements ActionListener, KeyListener {
	private Socket socket;
	private DataOutputStream ou;
	private Writer ouw;
	ByteArrayOutputStream bos;
	ObjectOutput out;
	
	DataOutputStream msgToSend;
	BufferedReader serverAnswer;
	
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

	public Client() throws IOException {
		JLabel lblMessage = new JLabel("Verificar!");
		txtIP = new JTextField("localhost");
		txtPorta = new JTextField("3000");
		txtNome = new JTextField("Cliente");
		Object[] texts = { lblMessage, txtIP, txtPorta, txtNome };
		JOptionPane.showMessageDialog(null, texts);
		pnlContent = new JPanel();
		pnlContent.setPreferredSize(new Dimension(800,600));
		texto = new JTextArea(10, 20);
		texto.setEditable(false);
		texto.setBackground(new Color(240, 240, 240));
		txtMsg = new JTextField(20);
		lblHistorico = new JLabel("Histórico");
		lblMsg = new JLabel("Mensagem");
		btnSend = new JButton("Enviar");
		btnSend.setToolTipText("Enviar Mensagem");
		btnSair = new JButton("Sair");
		btnSair.setToolTipText("Sair do Chat");
		btnSend.addActionListener(this);
		btnSend.addKeyListener(this);
		txtMsg.addKeyListener(this);
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
		texto.setBorder(BorderFactory.createEtchedBorder(Color.BLUE, Color.BLUE));
		txtMsg.setBorder(BorderFactory.createEtchedBorder(Color.BLUE, Color.BLUE));
		setTitle(txtNome.getText());
		setContentPane(pnlContent);
		setLocationRelativeTo(null);
		setResizable(true);
		setSize(240, 315);
		setVisible(true);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
	}

	public void conectar() throws IOException {
		socket = new Socket(txtIP.getText(), Integer.parseInt(txtPorta.getText()));
		
		msgToSend = new DataOutputStream(socket.getOutputStream());
		serverAnswer = new BufferedReader(new InputStreamReader(socket.getInputStream()));

		
		ou =  new DataOutputStream(socket.getOutputStream());
		bos = new ByteArrayOutputStream();
		out = new ObjectOutputStream(bos);   
		
		String ms = txtNome.getText() + " conectou-se\n";
		ClientData cd = new ClientData(ms);
		
	
		out.writeObject(cd);
		out.flush();
		
		byte[] yourBytes = bos.toByteArray();
		bos.reset();
		
		msgToSend.write( yourBytes );
		texto.append("Conectado\n");
		
		msgToSend.flush();
	}

	public void enviarMensagem(String msg) throws IOException {
		
		if (msg.equals("Sair")) {
			String ms = txtNome.getText() + " Desconectou-se\n";
			ClientData cd = new ClientData(ms);
			
		
			out.writeObject(cd);
			out.flush();
			
			byte[] yourBytes = bos.toByteArray();
			bos.reset();
			
			msgToSend.write( yourBytes );
			texto.append("Desconectado \n");
		} 
		
		else 
		{
			String ms = txtNome.getText() + " diz -> \n" + msg + "\n";
			ClientData cd = new ClientData(ms);
			
			out.writeObject(cd);
			out.flush();
			
			byte[] yourBytes = bos.toByteArray();
			bos.reset();
			
			msgToSend.write( yourBytes );
			texto.append(ms);
		}
		
		msgToSend.flush();
		txtMsg.setText("");
	}

	public void escutar() throws IOException {
		InputStream in = socket.getInputStream();
		InputStreamReader inr = new InputStreamReader(in);
		BufferedReader bfr = new BufferedReader(inr);
		String msg = "";

		while (!"Sair13".equalsIgnoreCase(msg)) 
		{
			if (bfr.ready())
			{
				msg = bfr.readLine();
				if (msg.equals("Sair"))
					texto.append("Servidor caiu! \n");
				else
					texto.append(msg + "\n");
			}
		}
	}

	public void sair() throws IOException {
		enviarMensagem("Sair");
		ouw.close();
		ou.close();
		socket.close();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		try {
			if (e.getActionCommand().equals(btnSend.getActionCommand()))
				enviarMensagem(txtMsg.getText());
			else if (e.getActionCommand().equals(btnSair.getActionCommand()))
				sair();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

	@Override
	public void keyPressed(KeyEvent e) {
	    if(e.getKeyCode() == KeyEvent.VK_ENTER){
	       try {
	    	  if(!txtMsg.getText().equals(""))
	          enviarMensagem(txtMsg.getText());
	       } catch (IOException e1) {
	           // TODO Auto-generated catch block
	           e1.printStackTrace();
	       }                                                          
	   }                       
	}
	    
	@Override
	public void keyReleased(KeyEvent arg0) {
	  // TODO Auto-generated method stub               
	}
	    
	@Override
	public void keyTyped(KeyEvent arg0) {
	  // TODO Auto-generated method stub               
	}
	
	public static void main(String[] args) throws IOException {
		Client app = new Client();
		app.conectar();
		app.escutar();
	}

}