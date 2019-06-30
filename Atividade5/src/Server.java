import java.io.ObjectOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

public class Server extends Thread {
	
	private static ArrayList<ObjectOutputStream> clientList;
	private static ServerSocket server;
	
	private String nome;
	private Socket socket;
	
	private InputStream in;
	private ObjectInputStream inr;
	
	public Server(Socket sock) {
		this.socket = sock;

		try {
			in = sock.getInputStream();
			inr = new ObjectInputStream(in);
			
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void run() {
		try {
			String msg;
			OutputStream ou = this.socket.getOutputStream();
			
			Writer ouw = new OutputStreamWriter(ou);
			ObjectOutputStream bfw = new ObjectOutputStream(this.socket.getOutputStream());
			
			clientList.add(bfw);
		
			
			//FIXME: The Client function disconnect causes errors if the client is the only
			// one in the chat
			//TODO: Add override to jframe onclose to avoid errors
			do{
				Message cd = (Message)inr.readObject();
				msg = cd.getMsg();
				System.out.print(cd.getMsg());
				
				sendToAll(bfw, cd );
				
				if(cd.disconnected) {
					clientList.remove(bfw);
					break;
				}
				
			}while ( msg != null );
		} 
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void sendToAll(ObjectOutputStream bwSaida, Message msg) throws IOException {
		ObjectOutputStream bwS;

		for (ObjectOutputStream bw : clientList) {
			bwS = (ObjectOutputStream) bw;
			if (!(bwSaida == bwS)) {
				bw.writeObject( msg );
				bw.flush();
			}
		}
	}

	public static void main(String[] args) {
		try {
			
		    JLabel lblMessage = new JLabel("Porta do Servidor:");
		    JTextField txtPorta = new JTextField("3000");
		    JLabel lblIPMessage = new JLabel("IP do Servidor:");
		    JTextField txtIP= new JTextField( InetAddress.getLocalHost().getHostAddress() );
		    Object[] texts = {lblMessage, txtPorta, lblIPMessage, txtIP };  
		    JOptionPane.showMessageDialog(null, texts);
		    
		    server = new ServerSocket(Integer.parseInt(txtPorta.getText()), 10, InetAddress.getByName(txtIP.getText()));
		    JOptionPane.showMessageDialog(null,"Servidor ativo na porta: " + txtPorta.getText() + "\nIP: " + server.getInetAddress());
		    
		    System.out.println("Host IP: " + InetAddress.getByName(txtIP.getText()));
		    
			clientList = new ArrayList<ObjectOutputStream>();

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
