package clientSide;

import java.io.*;
import java.net.*;
import java.net.Socket;

import java.util.Scanner;

public class ClientMain {

	public static void main(String[] args) throws IOException, InterruptedException {
		try {
			int port = Integer.parseInt(args[0]);
			CreateClient client = new CreateClient(port);
			client.Auth_Request("username");
			boolean isUsername = client.checkUsername();
			if (isUsername) {
				client.Auth_Request("pwd");
				boolean isPassword = client.checkPassword();
				if (isPassword) {
					System.out.println("The password is correct, now you can do a GET request with the API key");
					client.enterAPI();

				} else {
					while (client.chancesLeft > 0) {
						client.Auth_Request("pwd");
						isPassword = client.checkPassword();
						if (isPassword == true) {
							client.enterAPI();
							break;
						}
					}
				}

			}

			client.closeAll();
			System.out.println("Disconnected from the sever. Have a nice day.");
		}
		catch(SocketTimeoutException e) {
			System.out.println("Timeout due to unresponsivenes. Terminated.");
		}
		catch(SocketException e) {
			System.out.println("Timeout due to unresponsivenes. Terminated.");
		}
		catch(EOFException e) {
			System.out.println("EOFException exception thrown.");
		}
		
	}// end of main

}// end of Class