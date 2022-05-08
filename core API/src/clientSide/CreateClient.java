package clientSide;

import java.io.*;
import java.net.*;
import java.net.Socket;

import java.util.Scanner;

public class CreateClient {

	Socket client;
	String payload = new String();
	Scanner scanner = new Scanner(System.in);
	DataInputStream dis = null;
	DataOutputStream dos = null;
	String hostname = "localhost";
	int portnumber;
	public int chancesLeft = 3;
	String clientToken = new String();

	public CreateClient(int portnumber) {
		try {
			hostname = "localhost"; // ????????
			portnumber = portnumber;
			client = new Socket(hostname, portnumber);
			client.setSoTimeout(20*1000);
			System.out.println("Connected to " + client.getInetAddress() + " on port " + client.getLocalPort());
			dis = new DataInputStream(client.getInputStream());
			dos = new DataOutputStream(client.getOutputStream());

		} catch (SocketException e) {
			System.out.println("Connection closed due to unresponsiveness.");
		} catch (IOException e) {
			System.out.println("IOException occured in CreateClient constructor.");
		}

	}

	public void Auth_Request(String request_type) throws IOException { 
		if (request_type.equals("username")) {
			System.out.println("Initialization phase, provide username: ");
			byte phase = 0;
			byte type = 0;
			payload = scanner.nextLine();
			byte[] byteArray = payload.getBytes();
			int size = byteArray.length;
			dos.writeByte(phase);
			dos.writeByte(type);
			dos.writeInt(size);
			dos.write(byteArray);
			dos.flush();
			System.out.println("Auth_Request for username is made, username sent to the server.");
		} else if (request_type.equals("pwd")) {
			byte phase = 0;
			byte type = 0;
			System.out.println("Please provide the password: ");
			payload = scanner.nextLine();
			byte[] byteArray = payload.getBytes();

			int size = byteArray.length;
			dos.writeByte(phase);
			dos.writeByte(type);
			dos.writeInt(size);
			dos.write(byteArray);
			dos.flush();
			System.out.println("Auth_Request for password is made, password sent to the server.");
		}

	}

	public boolean checkUsername() throws IOException { 
		byte phase = dis.readByte();
		byte type = dis.readByte();
		int size = dis.readInt();
		payload = new String(dis.readNBytes(size));
		System.out.println("Server reply : " + payload);
		if (type == 2) {
			return false;
		} else {
			return true;
		}

	}

	public boolean checkPassword() throws IOException {
		try {
			byte phase = dis.readByte();
			byte type = dis.readByte();
			int size = dis.readInt();
			payload = new String(dis.readNBytes(size));

			if (type == 2) {
				this.chancesLeft--;
				System.out.println("Server reply: " + payload);
				return false;

			} else {
				clientToken = payload;
				System.out.println("Recieved private token from the server. Token : " + this.clientToken);
				return true;

			}
		} catch (SocketTimeoutException e) {
			this.chancesLeft = -1;
			System.out.println(
					"Server closed has closed the socket due to unresponsiveness. No password recieved in time.");
		} catch (SocketException e) {
			System.out.println("Connection closed due to timeout.");
		}
		return false;
	}

	public void enterAPI() throws IOException {
		try {
			System.out.println("You have given access to search articles or journals.");
			while (true) {
				System.out.println(
						"Article search or Journal search? Please write article or journal case insensitive. If you want to quit, type no request.");
				String searchType = scanner.nextLine();
				byte phase = 1;
				byte search;
				while (!searchType.equalsIgnoreCase("article") && !searchType.equalsIgnoreCase("journal")
						&& !searchType.equalsIgnoreCase("no request")) {
					System.out.println("You have entered a wrong type, please enter again : ");
					searchType = scanner.nextLine();
				}
				if (searchType.equalsIgnoreCase("article")) {
					search = 1;
					System.out.println("Please enter the article name to search: ");
					String query = scanner.nextLine();
					byte[] payload = query.getBytes();
					int size = payload.length;
					byte[] tokenByte = this.clientToken.getBytes();
					int size2 = tokenByte.length;
					dos.writeByte(phase);
					dos.writeByte(search);
					dos.writeInt(size);
					dos.write(payload);
					dos.writeInt(size2);
					dos.write(tokenByte);
					dos.flush();
					phase = dis.readByte();
					search = dis.readByte();
					size = dis.readInt();
					query = new String(dis.readNBytes(size));
					System.out.println(query);
				} else if (searchType.equalsIgnoreCase("journal")) {
					search = 2;
					System.out.println("Please enter the issn for the journal search : ");
					String query = scanner.nextLine();
					byte[] payload = query.getBytes();
					int size = payload.length;
					byte[] tokenByte = this.clientToken.getBytes();
					int size2 = tokenByte.length;
					dos.writeByte(phase);
					dos.writeByte(search);
					dos.writeInt(size);
					dos.write(payload);
					dos.writeInt(size2);
					dos.write(tokenByte);
					dos.flush();
					phase = dis.readByte();
					search = dis.readByte();
					size = dis.readInt();
					query = new String(dis.readNBytes(size));
					System.out.println(query);
				} else { // the client does not have any more request.
					search = 3;
					int size = 0;
					System.out.println("Informing the server that there are no more requests.");
					byte[] tokenByte = this.clientToken.getBytes();
					int size2 = tokenByte.length;
					dos.writeByte(phase);
					dos.writeByte(search);
					dos.writeInt(size);
					dos.writeInt(size2);
					dos.write(tokenByte);
					dos.flush();
					break;
				}
			}
		} catch (SocketTimeoutException e) {
			this.chancesLeft = -1;
			System.out.println("Connection closed due to unresponsiveness.");
		} catch (SocketException e) {
			System.out.println("Connection closed due to timeout.");
		}
	}

	public void closeAll() {
		try {
			System.out.println("Closing all the connection objects.");
			if (client != null) {
				client.close();
			}

			if (dos != null) {
				dos.close();
			}
			if (dis != null) {
				dis.close();
			}
			if (scanner != null) {
				scanner.close();
			}
		} catch (IOException ie) {
			System.err.println("Socket Close Error");
		}
	}
}
