package serverSide;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.Inet4Address;

public class Server {
	private ServerSocket serverSocket;
	public int serverPort;
	
	public Server(int port) {
		try {
			serverSocket = new ServerSocket(port);
			System.out.println("Opened up a server socket on " + Inet4Address.getLocalHost());	
		}
		catch(IOException e) {
			e.printStackTrace();
			System.out.println("IOException occured");
		}
		while(true) {
			listenAndAccept();
		}
	}
	
	private void listenAndAccept() {
		Socket client;
		try {
			client = serverSocket.accept();
			System.out.println("Connection with a client with the adress : " + client.getRemoteSocketAddress() + " has been established.");
			ServerThread st = new ServerThread(client);
			st.start();
		}
		catch(Exception e) {
			e.printStackTrace();
			System.out.println("Connection error occured.");
		}
	}
	
}
