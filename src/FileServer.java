
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class FileServer extends Thread {
	
	private class RequestHandler extends Thread {
		public final static int BufferSize = 1024;
		
		private Socket socket;
		
		public RequestHandler(Socket socket) {
			this.socket = socket;
		}
		
		private void resetModificationBit(String fileName, String clientID) throws IOException {
			System.err.println("Ressetting " + fileName + " for client " + clientID);
			GlobalConstants.mdManager.setModificationBit(fileName, clientID, 0);
		}
		
		@Override
		public void run() {
			byte[] buffer = new byte[BufferSize];
			
			try {
				BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				String clientID = reader.readLine();
				String fileName = reader.readLine();
				System.out.println("File server received file request: " + rootDir.getCanonicalFile() + "\\" + fileName);
				
				File file = new File(rootDir.getCanonicalFile() + "\\" + fileName);
				
				OutputStream os = new BufferedOutputStream(socket.getOutputStream());
				InputStream fis = new BufferedInputStream(new FileInputStream(file));
				
				while (fis.read(buffer) != -1) {
					os.write(buffer);
				}
				
				resetModificationBit(fileName, clientID);
				
				fis.close();
				os.close();
				reader.close();
			} 
			catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	private int port;
	private File rootDir;
	
	public FileServer(int port, File rootDir) {
		this.port = port;
		this.rootDir = rootDir;
	}

	@Override
	public void run() {
		try {
			System.out.println("FILESERVER INIITATED");
			ServerSocket serverSocket = new ServerSocket(port);
			while (true) {
				Socket socket = serverSocket.accept();
				RequestHandler requestHandler = new RequestHandler(socket);
				requestHandler.start();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}	
	}
}
