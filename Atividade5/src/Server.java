import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.stream.Collectors;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class Server extends Thread {
	
	private static ArrayList<BufferedWriter> clientList;
	private static ServerSocket server;
	
	private String nome;
	private Socket socket;
	
	private InputStream in;
	private ObjectInputStream inr;
	private BufferedReader bfr;

	public Server(Socket sock) {
		this.socket = sock;

		try {
			in = sock.getInputStream();
			inr = new ObjectInputStream(in);
			
			bfr = new BufferedReader(new InputStreamReader(in));
			
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
			BufferedWriter bfw = new BufferedWriter(ouw);
			
			clientList.add(bfw);
			
			ClientData cd = (ClientData) inr.readObject();
			System.out.print(cd.getMsg());
			msg = cd.getMsg();
			
			//FIXME: The Client function disconnect causes errors if the client is the only
			// one in the chat
			//TODO: Add override to jframe onclose to avoid errors
			while ( msg != null ) {
				sendToAll(bfw, msg);
				
				cd = (ClientData)inr.readObject();
				msg = cd.getMsg();
				System.out.print(cd.getMsg());
				
				if(cd.disconnected) {
					sendToAll(bfw, msg);
					clientList.remove(bfw);
					break;
				}
			}
		} 
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void sendToAll(BufferedWriter bwSaida, String msg) throws IOException {
		BufferedWriter bwS;

		for (BufferedWriter bw : clientList) {
			bwS = (BufferedWriter) bw;
			if (!(bwSaida == bwS)) {
				bw.write(msg);
				bw.flush();
			}
//			else {
//				bw.write(msg + "\tvv\r\n");
//				bw.flush();				
//			}
		}
	}

	public static void main(String[] args) {
		try {
			
		    JLabel lblMessage = new JLabel("Porta do Servidor:");
		    JTextField txtPorta = new JTextField("3000");
		    Object[] texts = {lblMessage, txtPorta };  
		    JOptionPane.showMessageDialog(null, texts);
		    server = new ServerSocket(Integer.parseInt(txtPorta.getText()));
		    JOptionPane.showMessageDialog(null,"Servidor ativo na porta: " + txtPorta.getText() + "e ip: " + server.getInetAddress());
		    
			clientList = new ArrayList<BufferedWriter>();

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
