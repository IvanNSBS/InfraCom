import java.io.IOException;
import java.net.DatagramSocket;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.util.Random;
import java.util.concurrent.TimeUnit;
		

public class UDPClient {
	
	public static int amountOfMessages = 400;
	
	public static byte[] selectMsg() {
		Random rand = new Random();
		int i = rand.nextInt(6);
		
		switch(i) {
			case 0:
				return ("Oie").getBytes();
			case 1:
				return ("Eu sou o cliente").getBytes();
			case 2:
				return ("Hmm...").getBytes();
			case 3:
				return ("Ok").getBytes();
			default:
				return ("Other msg").getBytes();
		}
	}
	
	public static void main(String[] args) throws IOException {
		DatagramSocket clientSocket = new DatagramSocket(6000);
		InetAddress IPServer = InetAddress.getByName("localhost");
		
		for(int i = 0; i < amountOfMessages; i++) {
			byte[] sendData = new byte[1024];
			sendData = selectMsg();
			DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPServer, 5000);
			clientSocket.send(sendPacket);
			long startTime = System.nanoTime();
			
			byte[] recvData = new byte[1024];
			DatagramPacket recvPacket = new DatagramPacket(recvData, recvData.length);
			System.out.print("Pacote recebido pelo cliente! ");
			
			clientSocket.receive(recvPacket);
			long endTime = System.nanoTime();
			String recvMsg = new String(recvPacket.getData());
			System.out.println("A Mensagem Recebida pelo cliente foi: " + recvMsg);
			
			System.out.println("RTT = " + (endTime - startTime)/1000000.0 + "ms");
		}
	}
	
}
