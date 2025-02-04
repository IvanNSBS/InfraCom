import java.io.IOException;
import java.net.DatagramSocket;
import java.net.DatagramPacket;
import java.net.InetAddress;

public class UDPServer {
	
	public static byte[] answerMsg(String msg) {
		
		if( msg.contains("Oie"))
			return ("Um ol� do servidor!").getBytes();
		else if( msg.contains("Eu sou o cliente") )
			return ("Eu sei...").getBytes();
		else if( msg.contains("Hmm...") )
			return ("???").getBytes();
		else if( msg.contains("Ok") )
			return ("kO").getBytes(); 
		else
			return ("Nao entendi sua msg").getBytes();
	}
	
	public static void main(String[] args) throws IOException{
		DatagramSocket serverSocket = new DatagramSocket(5000);
		 
		byte[] recvData = new byte[1024]; 
		byte[] sendData = new byte[1024];
		
		InetAddress clientIP;
		
		int port = 6000;
		
		while(true) {
			recvData = new byte[1024]; // flush byte data
			DatagramPacket recvPacket = new DatagramPacket(recvData, recvData.length);
			serverSocket.receive(recvPacket);
			System.out.print("Servidor recebeu um pacote de " + recvPacket.getAddress());
			String recvMsg = new String(recvPacket.getData());
			System.out.println("A mensagem recebida foi: " + recvMsg);
			
			clientIP = recvPacket.getAddress();
			
			sendData = answerMsg(recvMsg);
			
			DatagramPacket sendPacket  = new DatagramPacket(sendData, sendData.length, clientIP, port);
			serverSocket.send(sendPacket);
			System.out.println("Servidor enviou a resposta para: " + recvPacket.getAddress());
			
		}
		
		
	}
}
