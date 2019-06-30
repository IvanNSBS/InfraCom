import java.io.ObjectOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

public class Server extends Thread {
	
	//list of clients the server has to send data
	private static ArrayList<ObjectOutputStream> clientList;
	private static ServerSocket server;
	
	private Socket socket;
	
	private InputStream in;
	private ObjectInputStream inr;
	
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
					System.out.print(cd.getMsg());
					
					//send received messages to all users
					sendToAll(bfw, cd );
					
					//remove client from list if message was
					//a disconnect request
					if(cd.disconnected) 
						clientList.remove(bfw);
				}
				catch(Exception e){ }
				
			}while ( clientList.size() > 0 );//loop while there's users
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
				bw.writeObject( msg );
				bw.flush();
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

			//loop indefinitely to listen to connection request
			while (true) {
				Socket con = server.accept();
				Thread t = new Server(con);
				t.start();
			}

		} 
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}
