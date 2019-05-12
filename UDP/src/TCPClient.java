import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.net.Socket;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Random;
import java.util.Date;

public class TCPClient {
	
	public static int amountOfMessages = 10;
	
	public static String selectMsg() {
		Random rand = new Random();
		int i = rand.nextInt(6);
		
		switch(i) {
			case 0:
				return ("Oie");
			case 1:
				return ("Eu sou o cliente");
			case 2:
				return ("Hmm...");
			case 3:
				return ("Ok");
			default:
				return ("Other msg");
		} 
	}
	
	public static void main(String[] args) throws ConnectException {
		
		int port = 6000;
		String address = "localhost";
		
		for(int i = 0; i < amountOfMessages; i++) {
			try {
				
				DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss\"");
				
				Socket socket = new Socket(address, port);
				DataOutputStream msgToSend = new DataOutputStream(socket.getOutputStream());
				BufferedReader serverAnswer = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				
				msgToSend.writeBytes(selectMsg() + '\n');
				long startTime = System.nanoTime();
				Date sendTime = new Date();		
				
				System.out.println("Mensagem enviada ao servidor...");
				
				String rcvdMsg = serverAnswer.readLine();
				long endTime = System.nanoTime();
				Date rcvTime = new Date();		
				
				System.out.println("Resposta recebida do servidor: " + rcvdMsg);
		 
				
				System.out.println("Momento de envio da msg: " + sendTime);
				System.out.println("Momento de recebimento da resposta: " + rcvTime);
				System.out.println("RTT = " + (endTime - startTime)/1000000.0 + "ms");
				 
				socket.close(); 
			} 
			catch(Exception e) {
				e.printStackTrace();
			}
		}
		
	}
	
}
