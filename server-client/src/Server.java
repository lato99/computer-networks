import java.net.*;
import java.util.concurrent.TimeoutException;
import java.io.*;

public class Server {
	public static void main(String... args) throws IOException, TimeoutException {
		
	//get the port number from the arguments
	int port = Integer.parseInt(args[0]);
	
	//initialize the objects
	ServerSocket serverSocket = null;
	Socket clientSocket = null; //now this client socket is in the welcoming port
	PrintWriter printWriter = null;
    BufferedReader bufferedReaderStream = null;
	BufferedReader bufferedReaderInput = null;
    
	try {
		
	System.out.println("Waiting for client to join");
	serverSocket =   new ServerSocket(port);
	

	//For server socket, setting a timeout sets a limit time for accepting a client, 
	//if no client is connected in this time (provided as miliseconds) there is timout.
	serverSocket.setSoTimeout(10 *1000); 
	
    clientSocket = serverSocket.accept(); //after accepting the socket, now this socket is in a connection port, so it is no more in the welcoming port
   
    //setting a setSoTimeout for the client socket sets a limit for client to send a message
    //if client is idle for setSoTimeout's provided argument (in miliseconds) then there is timout.
    clientSocket.setSoTimeout(10*1000); 
	System.out.println("Client has joined");
	
    printWriter = new PrintWriter(clientSocket.getOutputStream());
    bufferedReaderStream = new BufferedReader( new InputStreamReader(clientSocket.getInputStream()));
    bufferedReaderInput =  new BufferedReader (new InputStreamReader ( System.in ) );
        
    String clientMessage = "";
    String serverMessage = "";
    	while(!clientMessage.equalsIgnoreCase("quit") || !serverMessage.equalsIgnoreCase("quit")) {
        clientMessage = bufferedReaderStream.readLine();
        if(clientMessage.equals("quit")){
        	System.out.println("The client has typed quit, program terminated.");
			break;
    	}
    	System.out.println ( "Client Says: " + clientMessage );
    	
    	System.out.print("Enter your message as Server: ");
        serverMessage = bufferedReaderInput.readLine();
        printWriter.println ( serverMessage );
        printWriter.flush();
        if(serverMessage.equals("quit")) {
        	System.out.println("You have typed quit, program terminated.");
			break;
        }

    	}
	}
	catch(SocketTimeoutException e) {
		System.out.printf("Either no messages recieved or no client has joined.");
	}
	catch (IOException e) {
		
		e.printStackTrace();
	}
	
	finally {
		if(clientSocket != null)
			clientSocket.close();
		if(serverSocket != null)
			serverSocket.close();
		if(printWriter != null)
			printWriter.close();
		if(bufferedReaderStream != null)
			bufferedReaderStream.close();
		if(bufferedReaderInput != null) 
			bufferedReaderInput.close();	
		}
	
	
    	}
  }
