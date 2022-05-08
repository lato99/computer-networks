package serverSide;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.Socket;
import java.net.SocketException;
import java.net.URL;
import java.util.Random;
import java.util.Scanner;

public class ServerThread extends Thread {

	protected Socket client = null;
	protected DataOutputStream dos = null;
	protected DataInputStream dis = null;
	protected int numIncorrectPasword = 0;
	protected String latoToken = "Di0sKZV2HTlmo4zEepctBIJaruOG9LjA";
	protected String clientToken = "";
	protected String clientName = "Client-" + Long.toString(this.getId());

	public ServerThread(Socket client) throws SocketException {
		this.client = client;
		client.setSoTimeout(20 * 1000);
	}

	public void run() {
		try {

			dis = new DataInputStream(client.getInputStream());
			dos = new DataOutputStream(client.getOutputStream());

			boolean isUsername = checkUserName(dis.readByte(), dis.readByte(), dis.readInt());
			if (isUsername) {
				Auth_Challenge("password request");
				boolean isPassword = checkPassword(dis.readByte(), dis.readByte(), dis.readInt());
				if (isPassword) {
					Auth_Success();
					enterAPI();
				} else {
					while (this.numIncorrectPasword < 3) {
						Auth_fail("incorrect pwd");
						isPassword = checkPassword(dis.readByte(), dis.readByte(), dis.readInt());
						if (isPassword == true)
							break;
					}
					if (this.numIncorrectPasword < 3) {
						Auth_Success();
						enterAPI();
					}
				}
			} else {
				Auth_fail("username");
			}
		} catch (IOException e) {
			System.out.println(this.clientName + " : client socket is closed.");
		} finally {
			try {
				System.out.println(this.clientName + " : closing all the connection objects.");

				if (client != null) {
					client.close();
				}

				if (dos != null) {
					dos.close();
				}
				if (dis != null) {
					dis.close();
				}
			} catch (IOException ie) {
				System.err.println("Socket Close Error");
			}
		} // end finally
	}

	public void Auth_fail(String failType) throws IOException {
		if (failType.equals("username")) {
			byte phase = 0;
			byte type = 2;
			String response = "User does not exist.";
			byte[] responseB = response.getBytes();
			int size = responseB.length;
			dos.writeByte(phase);
			dos.writeByte(type);
			dos.writeInt(size);
			dos.write(responseB);
			dos.flush();
			System.out.println(this.clientName + " : Auth_fail due to wrong username");
			// disconnect from the socket, and you can actually close the thread.
		} else if (failType.equals("incorrect pwd")) {
			byte phase = 0;
			byte type = 2;
			this.numIncorrectPasword++;
			if (this.numIncorrectPasword == 3) {
				String response = "Wrong password. You don't have any chances left, closing the connection.";
				byte[] responseB = response.getBytes();
				int size = responseB.length;
				dos.writeByte(phase);
				dos.writeByte(type);
				dos.writeInt(size);
				dos.write(responseB);
				dos.flush();
				System.out.println(this.clientName + " : Auth_fail due to wrong password, no chance left.");
			} else {
				int chanceLeft = 3 - this.numIncorrectPasword;
				String response = "Wrong password. You have " + chanceLeft + " chances left.";
				byte[] responseB = response.getBytes();
				int size = responseB.length;
				dos.writeByte(phase);
				dos.writeByte(type);
				dos.writeInt(size);
				dos.write(responseB);
				dos.flush();
				System.out.println(
						this.clientName + " : Auth_fail due to wrong password " + chanceLeft + " chances left.");
			}
			// disconnect from the socket, and you can actually close the thread.
		}
	}

	public boolean checkUserName(byte phase, byte type, int size) throws IOException { // 1 - get
		try {
			byte[] applicationPayload = dis.readNBytes(size);
			String applicationPayloadS = new String(applicationPayload);
			Scanner scanner = new Scanner(getClass().getResourceAsStream("unamePass.txt"));
			String line = new String();
			String check = "username : " + applicationPayloadS;
			while (scanner.hasNextLine()) {
				line = scanner.nextLine();

				if (line.equals(check)) {

					System.out.println(this.clientName + " : recieved correct username.");
					return true;

				}
			}
		}

		catch (IOException e) {
			System.out.println("Error inside the checkUserName function in the ServerThread");
		}
		System.out.println(this.clientName + " : recieved incorrect username.");
		return false;
	}// end of checkUserName

	public void Auth_Challenge(String challengeType) throws IOException { // 2 - writes
		if (challengeType.equals("password request")) {
			System.out.println(this.clientName + " : Auth_Challenge for password sent.");
			byte phase = 0;
			byte type = 1;
			String askPassword = "Valid username, what is the password?";
			byte[] byteArray = askPassword.getBytes();
			int size = byteArray.length;
			dos.writeByte(phase);
			dos.writeByte(type);
			dos.writeInt(size);
			dos.write(byteArray);
			dos.flush();
		}

	}

	public boolean checkPassword(byte phase, byte type, int size) throws IOException {
		try {
			byte[] applicationPayload = dis.readNBytes(size);
			String applicationPayloadS = new String(applicationPayload);
			Scanner scanner = new Scanner(getClass().getResourceAsStream("unamePass.txt"));
			String line = new String();
			String check = "password : " + applicationPayloadS;
			while (scanner.hasNextLine()) {
				line = scanner.nextLine();
				if (line.equals(check)) {
					System.out.println(this.clientName + " : recieved correct password.");
					return true;
				}
			}
		} catch (IOException e) {
			System.out.println("Error inside the checkUserName function in the ServerThread");
		}
		System.out.println(this.clientName + " : recieved incorrect password.");
		return false;
	}// end of checkUserName

	public void Auth_Success() throws IOException { // writes
		System.out.println(this.clientName + " : Auth_Success , private token sent.");
		byte phase = 0;
		byte type = 3;
		String token = hashToken();
		this.clientToken = token;
		byte[] sending = token.getBytes();
		int size = sending.length;
		dos.write(phase);
		dos.write(type);
		dos.writeInt(size);
		dos.write(sending);
		dos.flush();
	}

	public void enterAPI() throws IOException {
		while (true) {
			byte phase = dis.readByte();
			byte search = dis.readByte();
			int size = dis.readInt();
			String query = new String(dis.readNBytes(size));
			int size2 = dis.readInt();
			String tokenRecieved = new String(dis.readNBytes(size2));
			if (this.clientToken.equals(tokenRecieved)) {
				String ret = new String();
				if (search == 1) {
					System.out.println(this.clientName + " : responding search article method with query : " + query);
					ret = "https://core.ac.uk:443/api-v2/articles/search/" + query + "?apiKey=" + latoToken;
					GETfromURL("article", ret);
				} else if (search == 2) {
					System.out.println(this.clientName + " : responding search journal method with query : " + query);
					ret = "https://core.ac.uk:443/api-v2/journals/get/" + query + "?apiKey=" + latoToken;
					GETfromURL("journal", ret);
				} else if (search == 3) {
					System.out.println(this.clientName + " has no more requests. Closing the connection.");
					break;
				}
			} else {
				System.out.println(this.clientName + " : recieved an invalid token. Not responding.");
				phase = 1;
				byte type = 4;
				String payload = "Your token is invalid. Try again.";
				byte[] byteArr = payload.getBytes();
				size = byteArr.length;
				dos.write(phase);
				dos.write(type);
				dos.writeInt(size);
				dos.write(byteArr);
			}

		}
	}

	public void GETfromURL(String form, String urlString) throws IOException {
		String line = new String();
		if (form.equals("article")) {
			try {
				URL url = new URL(urlString);
				HttpURLConnection connection = (HttpURLConnection) url.openConnection();
				DataInputStream dis = new DataInputStream(connection.getInputStream());
				Scanner s = new Scanner(dis).useDelimiter("\"subjects\"");
				String result = new String();
				while (s.hasNext()) {
					result = s.next();
					String title = new String(" ");
					String topics = new String(" ");
					String year = new String(" ");

					int startTitle = result.indexOf("\"title\"");
					int endTitle = result.indexOf(",\"topics\"");
					if (startTitle > -1 && endTitle > -1) {
						title = result.substring(startTitle, endTitle);
					}
					int startTopics = result.indexOf("\"topics\"");
					int endTopics = result.indexOf(",\"types\"");
					if (startTopics > -1 && endTopics > -1) {
						topics = result.substring(startTopics, endTopics);
					}
					int startYear = result.indexOf("\"year\"");
					int endYear = result.indexOf(",\"", startYear);
					if (startYear > -1 && endYear > -1) {
						year = result.substring(startYear, endYear);
					}
					line += title + "\n" + topics + "\n" + year + "\n";

				}
				byte phase = 1;
				byte type = 1;
				byte[] byteArr = line.getBytes();
				int size = byteArr.length;
				dos.write(phase);
				dos.write(type);
				dos.writeInt(size);
				dos.write(byteArr);

			} catch (EOFException e) {
				byte phase = 1;
				byte type = 5;
				byte[] byteArr = "The query is not valid, nothing found, try another query.".getBytes();
				int size = byteArr.length;
				dos.write(phase);
				dos.write(type);
				dos.writeInt(size);
				dos.write(byteArr);
			} catch (IOException e) {
				byte phase = 1;
				byte type = 5;
				byte[] byteArr = "The query is not valid, nothing found, try another query.".getBytes();
				int size = byteArr.length;
				dos.write(phase);
				dos.write(type);
				dos.writeInt(size);
				dos.write(byteArr);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else if (form.equals("journal")) {
			try {
				URL url = new URL(urlString);
				HttpURLConnection connection = (HttpURLConnection) url.openConnection();
				DataInputStream dis = new DataInputStream(connection.getInputStream());
				Scanner s = new Scanner(dis).useDelimiter("\"subjects\"");
				String result = new String();
				while (s.hasNext()) {
					result = s.next();
					String title = new String(" ");

					int startTitle = result.indexOf("\"title\"");
					int endTitle = result.indexOf(",\"", startTitle);
					if (startTitle > -1 && endTitle > -1) {
						title = result.substring(startTitle, endTitle);
						line += title + "\n";
					}
					int startLang = result.indexOf("\"language\"");
					int endLang = result.indexOf(",\"", startLang);
					if (startLang > -1 && endLang > -1) {
						title = result.substring(startLang, endLang);
						line += title + "\n";
					}

				}
				byte phase = 1;
				byte type = 2;
				byte[] byteArr = line.getBytes();
				int size = byteArr.length;
				dos.write(phase);
				dos.write(type);
				dos.writeInt(size);
				dos.write(byteArr);
			} catch (EOFException e) {
				byte phase = 1;
				byte type = 5;
				byte[] byteArr = "The query is not valid, nothing found, try another query.".getBytes();
				int size = byteArr.length;
				dos.write(phase);
				dos.write(type);
				dos.writeInt(size);
				dos.write(byteArr);
			} catch (IOException e) {
				byte phase = 1;
				byte type = 5;
				byte[] byteArr = "The query is not valid, nothing found, try another query.".getBytes();
				int size = byteArr.length;
				dos.write(phase);
				dos.write(type);
				dos.writeInt(size);
				dos.write(byteArr);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				System.out.println(this.clientName + " closing the connection due to an " + e.getMessage());
			}

		}
	}

	public String hashToken() {
		String pure = "lato79";
		String token = new String();
		Random rand = new Random();
		int bound = pure.length();
		for (int i = 0; i < 6; i++) {
			token += pure.charAt(rand.nextInt(bound));
		}
		return token;
	}

}
