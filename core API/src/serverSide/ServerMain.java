package serverSide;
public class ServerMain {
	public static void main(String[] args) {
		int port = Integer.parseInt(args[0]); //get this from the arguments later.
		Server server = new Server(port); //server only gets the port as argument
	}
}
