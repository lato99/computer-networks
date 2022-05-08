import java.io.*;
import java.net.*;


public class Client {
	public static void main(String... args) throws IOException {

		//get the port number from the arguments
		String hostname = "localhost";
		int port = Integer.parseInt(args[0]);
		
		//initialize the objects
		Socket client = null;
		PrintWriter printWriter = null;
		BufferedReader bufferedReaderStream = null;
		BufferedReader bufferedReaderInput = null;
		try {
				client = new Socket(hostname, port);
				printWriter = new PrintWriter(client.getOutputStream());
				bufferedReaderStream = new BufferedReader(new InputStreamReader(client.getInputStream()));
				bufferedReaderInput = new BufferedReader(new InputStreamReader(System.in));

				String clientMessage = "";
				String serverMessage = "";
			while (!clientMessage.equalsIgnoreCase("quit") || !serverMessage.equalsIgnoreCase("quit")) {
				System.out.print("Enter your message as Client: ");
				clientMessage = bufferedReaderInput.readLine();
				printWriter.println(clientMessage);
				printWriter.flush();
				if(clientMessage.equals("quit")) {
					System.out.println("You have typed quit, program terminated.");
					break;
				}
				
				serverMessage = bufferedReaderStream.readLine();
				if(serverMessage.equals("quit")) {
					System.out.println("Server has typed quit, program terminated.");
					break;
				}	
				System.out.println("Server says: " +  serverMessage);
			}
		}
		catch (SocketException e) {
			System.out.println("No server found.");
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		finally {
			if(client != null)
				client.close();
			if(printWriter != null)
				printWriter.close();
			if(bufferedReaderStream != null)
				bufferedReaderStream.close();
			if(bufferedReaderInput != null) 
				bufferedReaderInput.close();
		}
}}