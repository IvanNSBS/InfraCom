import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
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
	
	private static ArrayList<DataOutputStream> clientList;
	private static ServerSocket server;
	
	private String nome;
	private Socket socket;
	
	private InputStream in;
	private ObjectInputStream inr;
	private BufferedReader bfr;

	private ByteArrayOutputStream bos;
	private ObjectOutputStream out;
	
	public Server(Socket sock) {
		this.socket = sock;

		try {
			in = sock.getInputStream();
			inr = new ObjectInputStream(in);
			
			bfr = new BufferedReader(new InputStreamReader(in));
			
			bos = new ByteArrayOutputStream();
			out = new ObjectOutputStream(bos);
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
			DataOutputStream bfw = new DataOutputStream(this.socket.getOutputStream());
			
			clientList.add(bfw);
		
			
			//FIXME: The Client function disconnect causes errors if the client is the only
			// one in the chat
			//TODO: Add override to jframe onclose to avoid errors
			do{
				ClientData cd = (ClientData)inr.readObject();
				msg = cd.getMsg();
				System.out.print(cd.getMsg());
				
				sendToAll(bfw, msg);
				
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

	public void sendToAll(DataOutputStream bwSaida, String msg) throws IOException {
		DataOutputStream bwS;

		for (DataOutputStream bw : clientList) {
			bwS = (DataOutputStream) bw;
			if (!(bwSaida == bwS)) {
				ClientData answ = new ClientData(msg);
				
				out.writeObject(answ);
				out.flush();
				
				byte[] yourBytes = bos.toByteArray();
				bos.reset();
				
				bw.write( yourBytes );
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
		    
			clientList = new ArrayList<DataOutputStream>();

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
