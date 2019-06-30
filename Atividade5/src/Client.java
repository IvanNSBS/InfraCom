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

public class Client extends JFrame implements ActionListener, KeyListener, WindowListener {
	private Socket socket;
	
	private ObjectOutputStream msgToSend;
	private ObjectInputStream inr;
	
	private String clientID = UUID.randomUUID().toString();

	//Stack of unread messages that must be ACK'd
	private Stack<Message> unreadMessages = new Stack<Message>();
	//Messages that you sent. Used to know what to delete
	private ArrayList< String > sentMessages = new ArrayList<String>();
	//Sent messages occurrence index. Used to not remove wrong messages
	private ArrayList< Integer > sentMsgsIndex= new ArrayList<Integer>();
	
	private static final long serialVersionUID = 1L;
	
	private JLabel lblChat; // Chat label
	private JTextArea texto; //all chat text
	private JTextField txtMsg; // text user is writing to send
	private JButton btnSend; // button to send. Msg can be sent with keyboard
	private JButton btnSair; // same as above, but to close socket and exit server
	private JLabel lblMsg;
	private JPanel pnlContent;
	private JTextField txtIP;
	private JTextField txtPorta;
	private JTextField txtNome;

	// if the owner is focusing the window. Used to know if msg was visualized.
	boolean hasFocus = false; 
	
	int port = 3000;

	public Client() throws IOException {
		
		addWindowListener(this);
		
		JLabel lblMessage = new JLabel("Informe seus Dados");
		JLabel lblPorta = new JLabel("IP:");
		JLabel lblIP = new JLabel("Porta:");
		JLabel lblNome = new JLabel("Nome:");
		txtIP = new JTextField("localhost");
		txtPorta = new JTextField("3000");
		txtNome = new JTextField("Cliente");
		Object[] texts = { lblMessage, lblIP, txtIP, lblPorta, txtPorta, lblNome, txtNome };
		JOptionPane.showMessageDialog(null, texts);
		pnlContent = new JPanel();
		pnlContent.setPreferredSize(new Dimension(800,600));
		texto = new JTextArea(10, 20);
		texto.setEditable(false);
		texto.setBackground(new Color(240, 240, 240));
		txtMsg = new JTextField(20);
		lblChat = new JLabel("Chat");
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
		scroll.setAutoscrolls(true);
		pnlContent.add(lblChat);
		pnlContent.add(scroll);
		pnlContent.add(lblMsg);
		pnlContent.add(txtMsg);
		pnlContent.add(btnSair);
		pnlContent.add(btnSend);
		pnlContent.setBackground(Color.LIGHT_GRAY);
		texto.setBorder(BorderFactory.createEtchedBorder(Color.DARK_GRAY, Color.DARK_GRAY));
		txtMsg.setBorder(BorderFactory.createEtchedBorder(Color.DARK_GRAY, Color.DARK_GRAY));
		setTitle(txtNome.getText());
		setContentPane(pnlContent);
		setLocationRelativeTo(null);
		setResizable(true);
		setSize(240, 315);
		setVisible(true);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
	}

	public void updateOccurrenceIndexes(String deletedMsg, int msgIndex) {
		
		//A message was deleted. Decrease occurrence index for equal
		//strings so the deletion can delete the right message
		for(int i = msgIndex+1; i < sentMessages.size(); i++)
			if( deletedMsg.equals( sentMessages.get(i) )  ) 
				sentMsgsIndex.set(i, sentMsgsIndex.get(i) - 1);
		
		//If the index was valid, remove the msg from the lists
		if( msgIndex >= 0 && msgIndex < sentMessages.size() ) {
			sentMsgsIndex.remove(msgIndex);
			String msg = sentMessages.remove(msgIndex);
		}
	}
	
	public void conectar() throws IOException {
		//create socket
		socket = new Socket(txtIP.getText(), Integer.parseInt(txtPorta.getText()));
		
		//initialize output and input stream from socket
		msgToSend = new ObjectOutputStream(socket.getOutputStream());  
		inr = new ObjectInputStream( socket.getInputStream() );
		
		//prepare connection message to send
		String ms = txtNome.getText() + " conectou-se\n";
		Message cd = new Message(ms, clientID);

		//send message
		msgToSend.writeObject( cd );
		//update user chat with 'personal' text
		texto.append("Conectado\n");
		
		//flush output stream
		msgToSend.flush();
	}

	@SuppressWarnings("deprecation")
	public void enviarMensagem(String msg) throws IOException {
		
		//Verify if the socket is not closed before sending message
		//to avoid errors
		if( !socket.isClosed() ) {
			//if the message is a exit request
			if (msg.equals("___EXIT___")) {
				//prepare disconnect request/msg for server and other users
				String ms = txtNome.getText() + " Desconectou-se\n";
				Message cd = new Message(ms, true, this.clientID);
				
				//write message
				msgToSend.writeObject( cd );
				//reset writer
				msgToSend.reset();
				
				//update chat with local user text
				texto.append("Desconectado\n");
	
				//disable text input so the user can no longer try to write anything
				//update text background color for visual feedback that the user can 
				//no longer write messages
				txtMsg.setBackground(new Color( Color.LIGHT_GRAY.getRed()-40, 
												Color.LIGHT_GRAY.getGreen()-40, 
												Color.LIGHT_GRAY.getBlue()-40));
				txtMsg.disable();
			} 
			
			//if message is a delete command 
			else if( msg.contains("!del ") ) {
				
				int index = Integer.parseInt(msg.substring(5));
				
				//check if message index is valid
				if(index >= 0 && index < sentMessages.size()) {
					
					//get sent message
					String delMsg = sentMessages.get(index);
					//get sent message occurrence index. Used to know
					//whether there's the same message string in the chat
					//and remove the one that matches the sentMessage
					int ocrIndex = sentMsgsIndex.get(index);
					
					//Create message with delete request
					Message delReq = new Message( true, delMsg, ocrIndex );
					
					//Send delete request and reset writer
					msgToSend.writeObject( delReq );
					msgToSend.reset();
					
					//update local user chat. Local user chat strings are different
					//and must be manipulated to match the requested message 
					
					//oldMsg(not including name)
					String oldMsg = delMsg.substring( delMsg.indexOf("\n") + 1 );
					//newMsg to replace oldMsg
					String newMsg = "\nEsta mensagem foi apagada";
					
					//get All chat text
					String allTxt = texto.getText();
	
					
					//occurrence counter
					int occurence = 0;
					//curIndex of substring
					int curIndex = 0;
					
					//search for matching substring and count occurrence
					while ( (curIndex = allTxt.indexOf( oldMsg, curIndex )) != -1 ) {
						++occurence;
						
						//if occurrence is the same as the desired message, delete it
						if(occurence == ocrIndex) {
							allTxt = allTxt.substring(0, curIndex) + newMsg.substring(1) + allTxt.substring(curIndex + oldMsg.length() - 1);
							break;
						}
						curIndex += oldMsg.length();
					}
					
					updateOccurrenceIndexes( delMsg, index );
					texto.setText(allTxt);
				}
				else {
					System.out.println("Invalid msg index to remove\nMessages.size = " + sentMessages.size());
					System.out.println("sentMessages:");
					for(int i = 0; i < sentMessages.size(); i++)
						System.out.println( sentMessages.get(i) );
					
				}
			}
			
			else 
			{
				//message to be sent to server/other users
				String ms = txtNome.getText() + ":\n" + msg + "\n";
				//local message. Has a (v) (w) or (V) added to track
				//sent to server, sent to users, visualized, respectively
				//NOTE: Msg visualization has been implemented for 2 users only
				//for the sake of simplicity. It'll work for N users, but it'll
				//change it's state only if the first other user receive/visualize
				//This was chosen because the project will only be tested with 2
				//users.
				String mymsg = "Voce:  (v)\n" + msg + "\n";
				
				//message to sent to other server/users
				Message cd = new Message(ms, clientID);
				
				//send message and reset writer
				msgToSend.writeObject( cd );
				msgToSend.reset();
				
				//set local user chat with proper message
				texto.append( mymsg );
				
				//add the message sent to server/others to the buffer
				sentMessages.add( ms );
				
				//check inside the user text the occurrences index of
				//the sent message
				String realMsg = ms.substring( ms.indexOf("\n")+1 );
				String allTxt = texto.getText();
				int occurence = 0;
				int curIndex = 0;
				while ( (curIndex = allTxt.indexOf( realMsg, curIndex )) != -1 ) {
					++occurence;
					curIndex+= realMsg.length();
				}
				
				//add the index to the buffer
				sentMsgsIndex.add(occurence);
				
			}
			
			//flush writer and reset keyboard input message
			msgToSend.flush();
			txtMsg.setText("");
		}
	}

	public void escutar() throws IOException, ClassNotFoundException {
		
		//listen to input while the client socket is open
		while ( !socket.isClosed() )
		{
			//get received message
			Message cd = (Message)inr.readObject();
			
			//if message was a disconnect request and this user sent it
			//close reader/writers and socket
			if( cd.disconnected && cd.clientID.equals(this.clientID) ) {
				msgToSend.close();
				inr.close();
				socket.close();
			}
			else {
				//if the message was a delete request, update user chat with
				//delete message. Same logic as above delete
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
				
				//if the message was not a MessageReceivedAck or MessageWasVisualizedAck
				else if( !cd.ACKReceive && !cd.ACKVis ) {
					//simply append message to chat text
					texto.append( cd.getMsg() );
					
					//prepare receiveAck to tell the message was received
					//and the sender update the message status
					Message rcvAck = new Message(true, false, cd.clientID, cd.getMsg());
					
					//send message and clear writer
					msgToSend.writeObject( rcvAck );
					msgToSend.reset();
					msgToSend.flush();
					
					//prepare visualize Ack
					Message visAck = new Message(false, true, cd.clientID, cd.getMsg());
					//before sending, check if the user is with the window focused
					//to know if he actually "saw" the message
					if( hasFocus ) {
						msgToSend.writeObject( visAck );
						msgToSend.reset();
						msgToSend.flush();
					}
					//if he does not have the window open, put the visACK on the stack
					//of unread messages
					else 
						unreadMessages.push(visAck);
				}
				else {
					//if this client sent a message and the receiving msg
					//is an ACK saying the other user received the message
					if( cd.ACKReceive && cd.clientID.equals(this.clientID) ) {
						
						//update sent message string with the appropriate status
						String txtMsg = cd.getMsg().replaceFirst("\n", "  (v)\n");
						txtMsg = txtMsg.replaceFirst( txtNome.getText(), "Voce");
						
						String newMsg = cd.getMsg().replaceFirst("\n" , "  (w)\n");
						newMsg = newMsg.replaceFirst(txtNome.getText(), "Voce");
						String allTxt = texto.getText();
						
						allTxt = allTxt.replace(txtMsg, newMsg);
						//update chat text with the appropriate status
						texto.setText(allTxt);
					}
					//if this client sent a message and the receiving msg
					//is an ACK saying the other user visualized the message
					else if( cd.ACKVis && cd.clientID.equals(this.clientID) ) {
						//update sent message string with the appropriate status
						String txtMsg = cd.getMsg().replaceFirst("\n", "  (w)\n");
						txtMsg = txtMsg.replaceFirst( txtNome.getText(), "Voce");
						
						String newMsg = cd.getMsg().replaceFirst("\n", "  (V)\n");
						newMsg = newMsg.replaceFirst(txtNome.getText(), "Voce");
						String allTxt = texto.getText();
						
						allTxt = allTxt.replace(txtMsg, newMsg);
						//update chat text with the appropriate status
						texto.setText(allTxt);
					}
				}
			}
			
		}
		
	}

	public void sair() throws IOException {
		enviarMensagem("___EXIT___");
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		try {
			//if the button was "Enviar", send the message with the
			//keyboard text string on txtMsg
			if (e.getActionCommand().equals(btnSend.getActionCommand()))
				enviarMensagem(txtMsg.getText());
			//if the button was "Sair", send ___EXIT___ message for user
			else if (e.getActionCommand().equals(btnSair.getActionCommand()))
				sair();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

	@Override
	public void keyPressed(KeyEvent e) {
		//if the user pressed ENTER, send message with the
		//keyboard text string on txtMsg
	    if(e.getKeyCode() == KeyEvent.VK_ENTER){
	       try {
	    	  //but only if it's not an empty string
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
		//if the user now activaded the window, that is
		//he's now probably reading the chat, update focus
		//and send all stacked visACK's
		hasFocus = true;
		// TODO Auto-generated method stub
		int size = unreadMessages.size();
		for(int i = 0; i < size; i++) {
			Message visAck = unreadMessages.pop();
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
		//call sair() if the user is closing the window
		try {
			sair();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
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