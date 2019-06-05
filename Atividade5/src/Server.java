import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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
	private InputStreamReader inr;
	private BufferedReader bfr;

	public Server(Socket sock) {
		this.socket = sock;

		try {
			in = sock.getInputStream();
			inr = new InputStreamReader(in);
			bfr = new BufferedReader(inr);
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void run() {
		try {
			String msg;
			String line;
			OutputStream ou = this.socket.getOutputStream();
			Writer ouw = new OutputStreamWriter(ou);
			BufferedWriter bfw = new BufferedWriter(ouw);
			
			clientList.add(bfw);
			
			msg = bfr.readLine();
			System.out.println("Readed msg was: " + msg);
			
			while (!"Sair".equalsIgnoreCase(msg) && msg != null) {
				sendToAll(bfw, msg);
				msg = bfr.readLine();
				System.out.println(msg);
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
				bw.write(msg + "\n");
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
				System.out.println("Aguardando conexão...");
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
