import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;

public class TCPServer {
	
	public static String answerMsg(String msg) {
		
		if( msg.contains("Oie"))
			return ("Um olá do servidor!");
		else if( msg.contains("Eu sou o cliente") )
			return ("Eu sei...");
		else if( msg.contains("Hmm...") )
			return ("???");
		else if( msg.contains("Ok") )
			return ("kO"); 
		else
			return ("Nao entendi sua msg");
	}
	
	 
	public static void main(String[] args) throws BindException{

		int port = 6000;
		try {
			ServerSocket serverSocket = new ServerSocket(port);
			
			
			System.out.println("Iniciando o servidor...");
			
			while(true) {
				Socket client = serverSocket.accept();
				BufferedReader clientMsg = new BufferedReader(new InputStreamReader(client.getInputStream()));
				DataOutputStream answer = new DataOutputStream(client.getOutputStream()); 
				
				String clientMsgStr = clientMsg.readLine();
				System.out.println("Servidor recebeu mensagem de: " + client.getRemoteSocketAddress().toString());
				System.out.println("A mensagem foi: " + clientMsgStr);
				
				answer.flush();
				answer.writeBytes(answerMsg(clientMsgStr));
				System.out.println("Resposta enviada ao cliente");
				 
				answer.close();
				client.close();
				
			}
		}
		catch(Exception e) {
			System.out.println("Erro: " + e);
		}
	}  
}
