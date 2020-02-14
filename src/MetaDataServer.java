import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;



public class MetaDataServer extends Thread {
	private class RequestHandler extends Thread {
		public final static int BufferSize = 1024;

		private Socket socket;
		
		public RequestHandler(Socket socket) {
			this.socket = socket;
		}
				
		private void sendMetaData(PrintWriter printer, String clientID) throws IOException {	
			// TODO: Handle empty files
			System.out.println("MDManager: " + GlobalConstants.mdManager.getFilesToSync(clientID));
			printer.println(GlobalConstants.mdManager.getFilesToSync(clientID));
			
	
		}
		
		@Override
		public void run() {
			try {
				BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
						
				String clientID = reader.readLine();
				System.out.println("Client ID " + clientID  + " recieved at metadata server of " + GlobalConstants.NodeID);
				PrintWriter printer = new PrintWriter(socket.getOutputStream(), true);
				sendMetaData(printer, clientID);
				
				reader.close();
				printer.close();
			} 
			catch (IOException e) {
				e.printStackTrace();
			}	
		}
	}
	
	private int port;
	private File rootDir;
	
	public MetaDataServer(int port, File rootDir) {
		this.port = port;
		this.rootDir = rootDir;
		GlobalConstants.mdManager = new MetaDataManager(rootDir);
	}
	
	@Override
	public void run() {
		try {
			System.out.println("META-DATA SERVER STARTED");
			ServerSocket serverSocket = new ServerSocket(port);
			
			while (true) {
				Socket socket = serverSocket.accept();
				RequestHandler requestHandler = new RequestHandler(socket);
				System.out.println("Meta-data server of " + GlobalConstants.NodeID + " received new request:");
				requestHandler.start();
			}
		} 
		catch (IOException e) {
			e.printStackTrace();
		}
	}
}
