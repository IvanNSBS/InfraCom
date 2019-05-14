import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.Socket;
import java.util.Scanner;

public class Client extends Thread{
	private Socket socket;
	private DataOutputStream ou;
	private Writer ouw;
	private BufferedWriter bfw;
	
	private String name = "User";
	private String ip = "localhost";
	
	public Runnable listener;
	public Runnable writer;
	
	int port = 3000;
	
	public Client() {
		
		Scanner input = new Scanner(System.in);
		System.out.println("Digite a porta que voce deseja se conectar.\nA porta padrão é 3000");
		
		String line = input.nextLine();
		if (!line.equals(""))
			port = Integer.parseInt(input.nextLine());
		
		System.out.println("Digite o ip do servidor.\nO ip padrao eh localhost");
		
		line = input.nextLine();
		if (!line.equals(""))
			ip = line;
		
		System.out.println("Digite o seu nome de usuario.\nO nome padrao eh User");
		
		line = input.nextLine();
		if (!line.equals(""))
			name = line;
		
		System.out.println(name + " Conectado!\nVocê já pode digitar as mensagens para conversar!");
		
	}

	public void conectar() throws IOException {

		socket = new Socket(ip, port);
		ou = new DataOutputStream(socket.getOutputStream());
		ouw = new OutputStreamWriter(ou);
		bfw = new BufferedWriter(ouw);
		bfw.write(name + ": \r\n");
		bfw.flush();
		
	}

	public void enviarMensagem(String msg) throws IOException {
		if (msg.equals("Sair")) {
			msg = "Desconectado \r\n";
			bfw.write(msg);
		} 
		else {
			bfw.write(msg + "\r\n"); 
		}
		bfw.flush();
	}

	public void escutar() throws IOException {
		listener = new Runnable() {
			public void run() {
				InputStream in;
				try {
					in = socket.getInputStream();
					InputStreamReader inr = new InputStreamReader(in);
					BufferedReader bfr = new BufferedReader(inr);
					String msg = "";
					while (!"Sair".equalsIgnoreCase(msg)) {
						if (bfr.ready()) {
							msg = bfr.readLine();
							if (msg.equals("Sair"))
								System.out.println("Servidor caiu! \r\n");
							else
								System.out.println(msg + "\r\n");
						}
					}
				} 
				catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		};
	}
	
	public void escrever() {
		writer = new Runnable() {
			public void run() {
				String line = "";
				while(!line.equalsIgnoreCase("Sair")) {
					try {
					    InputStreamReader streamReader = new InputStreamReader(System.in);
					    BufferedReader bufferedReader = new BufferedReader(streamReader);
					    line = bufferedReader.readLine();
						enviarMensagem(line);
						System.out.println(name + ": " + line);
					} catch (IOException e) {
					    e.printStackTrace();
					}
				}
			}
		};
	}

	public void sair() throws IOException {
		enviarMensagem("Sair");
		bfw.close();
		ouw.close();
		ou.close();
		socket.close();
	}

	public static void main(String[] args) throws IOException {

		Client app = new Client();
		app.conectar();
		app.escutar();
		app.escrever();
        new Thread(app.listener).start(); // start listener
        new Thread(app.writer).start(); // start writer
		
	}
}