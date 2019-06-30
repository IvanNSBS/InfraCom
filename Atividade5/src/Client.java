import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Stack;
import java.util.UUID;

import javax.swing.*;

//TODO: Fix !del command deleting all messages that are the same

public class Client extends JFrame implements ActionListener, KeyListener, WindowListener {
	private Socket socket;
	
	private ObjectOutputStream msgToSend;
	private ObjectInputStream inr;
	
	private String clientID = UUID.randomUUID().toString();

	private Stack<ClientData> unreadMessages = new Stack<ClientData>();
	private ArrayList< String > sentMessages = new ArrayList<String>();
	//Sent messages occurrence index. Used to not remove wrong messages
	private ArrayList< Integer > sentMsgsIndex= new ArrayList<Integer>();
	
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

	boolean hasFocus = false;
	
	int port = 3000;

	public Client() throws IOException {
		
		addWindowListener(this);
		
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

	public void updateOccurrenceIndexes(String deletedMsg, int msgIndex) {
		
		for(int i = msgIndex+1; i < sentMessages.size(); i++)
			if( deletedMsg.equals( sentMessages.get(i) )  ) 
				sentMsgsIndex.set(i, sentMsgsIndex.get(i) - 1);
			
		if( msgIndex >= 0 && msgIndex < sentMessages.size() ) {
			sentMsgsIndex.remove(msgIndex);
			sentMessages.remove(msgIndex);
		}
	}
	
	public void conectar() throws IOException {
		socket = new Socket(txtIP.getText(), Integer.parseInt(txtPorta.getText()));
		
		msgToSend = new ObjectOutputStream(socket.getOutputStream());  
		inr = new ObjectInputStream( this.socket.getInputStream() );
		
		String ms = txtNome.getText() + " conectou-se\n";
		ClientData cd = new ClientData(ms, clientID);

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
			
			txtMsg.disable();
			socket.close();
		} 
		
		else if( msg.contains("!del ") ) {
			int index = Integer.parseInt(msg.substring(5));
			
			if(index >= 0 && index < sentMessages.size()) {
				String delMsg = sentMessages.get(index);
				int ocrIndex = sentMsgsIndex.get(index);
				
				ClientData delReq = new ClientData( true, delMsg, ocrIndex );
				msgToSend.writeObject( delReq );
				msgToSend.reset();
				
				
				String name = delMsg.substring( delMsg.indexOf("\n") );
				String newMsg = "\nEsta mensagem foi apagada\n";
				
				
				String allTxt = texto.getText();
				

				String realMsg = name.substring( 1 );
				int occurence = 0;
				int curIndex = 0;
				
				while ( (curIndex = allTxt.indexOf( realMsg, curIndex )) != -1 ) {
					++occurence;
					if(occurence == ocrIndex) {
						allTxt = allTxt.substring(0, curIndex) + newMsg.substring(1) + allTxt.substring(curIndex + name.length() - 1);
						break;
					}
					curIndex+= realMsg.length();
				}
				
				updateOccurrenceIndexes( delMsg, ocrIndex);
				texto.setText(allTxt);
			}
			else
				System.out.println("Invalid msg index to remove");
		}
		
		else 
		{
			String ms = txtNome.getText() + ":\n" + msg + "\t\n";
			String mymsg = "Voce:  (v)\n" + msg + "\t\n";
			
			ClientData cd = new ClientData(ms, clientID);
			
			msgToSend.writeObject( cd );
			msgToSend.reset();
			texto.append( mymsg );
			
			sentMessages.add( ms );
			
			String realMsg = ms.substring( ms.indexOf("\n")+1 );
			String allTxt = texto.getText();
			int occurence = 0;
			int curIndex = 0;
			while ( (curIndex = allTxt.indexOf( realMsg, curIndex )) != -1 ) {
				++occurence;
				curIndex+= realMsg.length();
			}
			
			sentMsgsIndex.add(occurence);
			
		}
		
		msgToSend.flush();
		txtMsg.setText("");
	}

	public void escutar() throws IOException, ClassNotFoundException {
		
		while ( !socket.isClosed() )
		{
			ClientData cd = (ClientData)inr.readObject();
			
			if( cd.deleteRequest ) {
				String old = cd.getMsg().substring(cd.getMsg().indexOf("\n") );
				String newMsg = "Esta mensagem foi apagada\n";
				String allTxt = texto.getText();
				
				int index = cd.occurrence;
				
				String realMsg = cd.getMsg().substring( cd.getMsg().indexOf("\n")+1 );
				int occurence = 0;
				int curIndex = 0;
				
				while ( (curIndex = allTxt.indexOf( realMsg, curIndex )) != -1 ) {
					++occurence;
					if(occurence == index) {
						allTxt = allTxt.substring(0, curIndex) + newMsg + allTxt.substring(curIndex + old.length() - 1);
						break;
					}
					curIndex+= realMsg.length();
					updateOccurrenceIndexes( cd.getMsg(), index);
				}
				
				texto.setText(allTxt);
			}
			
			else if( !cd.ACKReceive && !cd.ACKVis ) {
				texto.append( cd.getMsg() );
				ClientData rcvAck = new ClientData(true, false, cd.clientID, cd.getMsg());
				
				msgToSend.writeObject( rcvAck );
				msgToSend.reset();
				msgToSend.flush();
				
				ClientData visAck = new ClientData(false, true, cd.clientID, cd.getMsg());
				if( hasFocus ) {
					
					msgToSend.writeObject( visAck );
					msgToSend.reset();
					msgToSend.flush();
				}
				else 
					unreadMessages.push(visAck);
			}
			else {
				//if target client == this client
				if( cd.ACKReceive && cd.clientID.equals(this.clientID) ) {
					
					String txtMsg = cd.getMsg().replaceFirst("\n", "  (v)\n");
					txtMsg = txtMsg.replaceFirst( txtNome.getText(), "Voce");
					
					String newMsg = cd.getMsg().replaceFirst("\n" , "  (w)\n");
					newMsg = newMsg.replaceFirst(txtNome.getText(), "Voce");
					String allTxt = texto.getText();
					
					allTxt = allTxt.replace(txtMsg, newMsg);
					texto.setText(allTxt);
				}
				else if( cd.ACKVis && cd.clientID.equals(this.clientID) ) {
					String txtMsg = cd.getMsg().replaceFirst("\n", "  (w)\n");
					txtMsg = txtMsg.replaceFirst( txtNome.getText(), "Voce");
					
					String newMsg = cd.getMsg().replaceFirst("\n", "  (V)\n");
					newMsg = newMsg.replaceFirst(txtNome.getText(), "Voce");
					String allTxt = texto.getText();
					
					allTxt = allTxt.replace(txtMsg, newMsg);
					texto.setText(allTxt);
				}
			}
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
	public void windowActivated(WindowEvent e) {
		hasFocus = true;
		// TODO Auto-generated method stub
		int size = unreadMessages.size();
		for(int i = 0; i < size; i++) {
			ClientData visAck = unreadMessages.pop();
			try {
				msgToSend.writeObject( visAck );
				msgToSend.reset();
				msgToSend.flush();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
		
	}

	@Override
	public void windowDeactivated(WindowEvent e) {
		// TODO Auto-generated method stub
		hasFocus = false;
	}

	@Override
	public void keyReleased(KeyEvent arg0) {
	  // TODO Auto-generated method stub               
	}
	    
	@Override
	public void keyTyped(KeyEvent arg0) {
	  // TODO Auto-generated method stub               
	}
	
	@Override
	public void windowOpened(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowClosing(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowClosed(WindowEvent e) {
		// TODO Auto-generated method stub
	}

	@Override
	public void windowIconified(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowDeiconified(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}
	
	public static void main(String[] args) throws IOException, ClassNotFoundException {
		Client app = new Client();
		app.conectar();
		app.escutar();
	}


}