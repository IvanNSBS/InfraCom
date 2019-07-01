import java.io.ObjectOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;


import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Server extends Thread {
	
	//list of clients the server has to send data
	private static ArrayList<ObjectOutputStream> clientList;
	private static ArrayList<Thread> threads = new ArrayList<Thread>();
	private static ServerSocket server;
	
	private Socket socket;
	
	private InputStream in;
	private ObjectInputStream inr;
	
	private static JFrame frame;
	private static JPanel panel;
	private static JLabel labIP;
	private static JLabel labPort;
	private static JButton close;
	
	private static boolean closed = false;
	
	public Server(Socket sock) {
		//set server socket
		this.socket = sock;
		
		try {
			//initialize input streams
			in = sock.getInputStream();
			inr = new ObjectInputStream(in);
			
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	//thread for a user 
	public void run() {
		try {
			//new user output stream
			ObjectOutputStream bfw = new ObjectOutputStream(this.socket.getOutputStream());
			//add user to list
			clientList.add(bfw);
		
			//loop to send received messages to other users
			do{
				try {
					//get received message
					Message cd = (Message)inr.readObject();
					
					//send received messages to all users
					sendToAll(bfw, cd );
					
					//remove client from list if message was
					//a disconnect request
					if(cd.disconnected) 
						clientList.remove(bfw);
				}
				catch(Exception e){ }
				
			}while ( clientList.size() > 0 && !server.isClosed());//loop while there's users
		} 
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	//broadcast function
	public void sendToAll(ObjectOutputStream bwSaida, Message msg) throws IOException {
		for (ObjectOutputStream bw : clientList) {
			//dont send message to the whoever sent the message
			//unless it was a dc request.
			//The user who sent the dc request will properly
			//disconnect from the server once he listens to the 
			//delete request
			if (!(bwSaida == bw) || msg.disconnected ) {
				if( !socket.isClosed() ) {
					bw.writeObject( msg );
					bw.flush();
				}
			}
		}
	}

	public static void main(String[] args) {
		try {
			
			//create a pane to input the server port and IP
			//necessary to start the server
		    JLabel lblMessage = new JLabel("Porta do Servidor:");
		    JTextField txtPorta = new JTextField("3000");
		    JLabel lblIPMessage = new JLabel("IP do Servidor:");
		    JTextField txtIP= new JTextField( InetAddress.getLocalHost().getHostAddress() );
		    
		    //temporary line to make debugging easier
		    txtIP.setText("localhost");
		    
		    Object[] texts = {lblMessage, txtPorta, lblIPMessage, txtIP };  
		    JOptionPane.showMessageDialog(null, texts);
		    
		    //create the serverSocket with the input data
		    server = new ServerSocket(Integer.parseInt(txtPorta.getText()), 10, InetAddress.getByName(txtIP.getText()));
		    JOptionPane.showMessageDialog(null,"Servidor ativo na porta: " + txtPorta.getText() + "\nIP: " + server.getInetAddress());
		    
		    System.out.println("Host IP: " + InetAddress.getByName(txtIP.getText()));
		    
		    //initialize clientList
			clientList = new ArrayList<ObjectOutputStream>();
			
			frame = new JFrame("Servidor");
			frame.setVisible(true);
			frame.setSize(250, 100);
			frame.setResizable(false);
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			frame.setLocationRelativeTo(null);
			
			panel = new JPanel();
			panel.setBackground(Color.LIGHT_GRAY);
		
			close = new JButton("Fechar Servidor");
			
			labIP = new JLabel( "IP:" + txtIP.getText() + "\n" );
			labPort = new JLabel( "Porta:" + txtPorta.getText() );
			panel.add(labIP);
			panel.add(labPort);
			panel.add(close);
			frame.add(panel);
			
			close.addActionListener( new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					// TODO Auto-generated method stub
					try {
						//create socket so the server can properly close
						closed = true;
						Socket socket = new Socket(txtIP.getText(), Integer.parseInt(txtPorta.getText()));
						
						close.setEnabled(false);
						close.setText("Servidor Fechado");
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}
			} );
			
			//loop indefinitely to listen to connection request
			while ( !server.isClosed() ) {
				Socket con = server.accept();
				if( closed == true ) {
					server.close();
					for(int i = 0; i < threads.size(); i++)
						threads.get(i).stop();
				}
				else {
					Thread t = new Server(con);
					threads.add(t);
					t.start();
				}
			}

		} 
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}
