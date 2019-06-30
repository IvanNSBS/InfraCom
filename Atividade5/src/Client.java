import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.UUID;

import javax.swing.*;

public class Client extends JFrame implements ActionListener, KeyListener {
	private Socket socket;
	
	ObjectOutputStream msgToSend;
	ObjectInputStream inr;
	
	private String name = "User";
	private String ip = "localhost";
	
	String clientID = UUID.randomUUID().toString();

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
		
		msgToSend = new ObjectOutputStream(socket.getOutputStream());  
		inr = new ObjectInputStream( this.socket.getInputStream() );
		
		String ms = txtNome.getText() + " conectou-se\n";
		ClientData cd = new ClientData(ms);

		msgToSend.writeObject( cd );
		texto.append("Conectado\n");
		
		System.out.println(txtNome.getText() + " id is: " + clientID);
		
		msgToSend.flush();
	}

	public void enviarMensagem(String msg) throws IOException {
		
		if (msg.equals("Sair")) {
			String ms = txtNome.getText() + " Desconectou-se\n";
			ClientData cd = new ClientData(ms, true);
			
			msgToSend.writeObject( cd );
			msgToSend.reset();
			texto.append("Desconectado\n");
			
			socket.close();
		} 
		
		else 
		{
			String ms = txtNome.getText() + ": \n" + msg + "\t\n";
			String mymsg = txtNome.getText() + ": \n" + msg + "\tv\n";
			ClientData cd = new ClientData(ms);
			
			msgToSend.writeObject( cd );
			msgToSend.reset();
			texto.append( mymsg );
		}
		
		msgToSend.flush();
		txtMsg.setText("");
	}

	public void escutar() throws IOException, ClassNotFoundException {
		
		while ( !socket.isClosed() )
		{
			ClientData cd = (ClientData)inr.readObject();
			texto.append(cd.getMsg());
		}
		
	}

	public void sair() throws IOException {
		enviarMensagem("Sair");
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
	
	public static void main(String[] args) throws IOException, ClassNotFoundException {
		Client app = new Client();
		app.conectar();
		app.escutar();
	}

}