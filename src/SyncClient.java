import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;

public class SyncClient {

	private File rootDir;
	private String serverHostName;
	private int fileServerPort;
	private int metaDataServerPort;
	public static String clientId;
	public MetaDataManager mdManager;
	
	public SyncClient(String client, File rootDir, String serverHostName, int fileServerPort, int metaDataServerPort) {
		clientId = client;
		this.rootDir = rootDir;
		this.serverHostName = serverHostName;
		this.fileServerPort = fileServerPort;
		this.metaDataServerPort = metaDataServerPort;
		this.mdManager = new MetaDataManager(rootDir);
	}
	
	private String getMetaData() throws UnknownHostException, IOException {
		// Retrieve meta data from server.
		Socket socket = new Socket(serverHostName, metaDataServerPort);
		PrintWriter printer = new PrintWriter(socket.getOutputStream(), true);
		BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		
		System.out.println(clientId + " connecting to metadata server");
		printer.println(clientId);
		System.out.println(clientId + " successfully sent id to metadata-server");
		
		String metaData = reader.readLine();
		socket.close();
		return metaData;
	}
	
	private void syncFile(String metaInfo, String partnerNode) throws IOException {
		Socket socket = new Socket(serverHostName, fileServerPort);
		
		PrintWriter writer = null;
		BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		PrintWriter printer = new PrintWriter(socket.getOutputStream(), true);

		printer.println(clientId);
		
		String[] splitStr = metaInfo.split(",");
		
		String fileName = splitStr[0];
		String timeStamp = splitStr[1];
		
		printer.println(fileName);
		
		File file = new File(rootDir.getCanonicalPath() + "\\" + fileName);
		writer = new PrintWriter(file);
		
		String line = reader.readLine();
		while (line != null) {
			writer.write(line);
			writer.write("\n");
			line = reader.readLine();
		}

		writer.close();
		reader.close();
		socket.close();
		
		this.mdManager.updateMetaData(fileName, timeStamp, partnerNode);
	}
	
	private void syncFiles(ArrayList<String> reqFiles, String partnerNode) throws UnknownHostException, IOException {
		for (String file: reqFiles) {
			syncFile(file, partnerNode);
		}
	}
	
	public void sync() throws UnknownHostException, IOException {
		String filesToSync = getMetaData();
		System.out.println(clientId + " received files to sync: "  + filesToSync);
		
		String splitStr[] = filesToSync.split(";");
		String partnerNode = splitStr[splitStr.length - 1];
		
		ArrayList<String> filesToRequest = new ArrayList<>();
		
		for (int i = 0; i < splitStr.length - 2; i += 2) {
			
			String fileName = splitStr[i];
			String timeStamp = splitStr[i + 1];
			
			if (this.mdManager.hasFile(fileName)) {
				// Passing file name, time stamp, partner node id
				filesToRequest.addAll(compareMetaFiles(fileName, timeStamp, partnerNode));
			}
			else {
				filesToRequest.add(fileName + "," + timeStamp);
			}
		}
		
		for(String reqFile: filesToRequest) 
			System.out.println("Client requests: " + reqFile);
		
		
		syncFiles(filesToRequest, partnerNode);
	}
	
	private ArrayList<String> compareMetaFiles(String filename, String timestamp, String partnerNode) throws FileNotFoundException, IOException {
		ArrayList<String> filesToRequest = new ArrayList<>();
		String clientTimeStamp = this.mdManager.getTimeStamp(filename);
		
		switch (Utils.compareTimestamps(clientTimeStamp, timestamp)) {
			case 0:	// Timestamps are same
				if (Integer.parseInt(partnerNode) < Integer.parseInt(clientId))
					filesToRequest.add(filename + "," + timestamp);
		
				break;
				
			case 1: // Client has newer file
				break;
			
			case -1: // Client has out-dated file
				filesToRequest.add(filename + "," + timestamp);
				break;
		}
		return filesToRequest;
	}
	
	private static void usage() {
		System.out.println("Usage:");
		System.out.println("FileSync CLIENT_ID ROOT_DIRECTORY SERVER_HOST_NAME FILE_SERVER_PORT META_DATA_SERVER_PORT");
	}
	
	public static void main(String[] args) {
		if (args.length != 5) {
			usage();
			System.exit(-1);
		}
		String clientId = args[0];
		
		String rootDirStr = args[1];
		File rootDir = new File(rootDirStr);
		if (!rootDir.isDirectory()) {
			System.err.println("Root directory '" + rootDir + "' is no directorty.");
			System.exit(-1);
		}
		if (!rootDir.exists()) {
			System.err.println("Root directory '" + rootDir + "' does not exist.");
			System.exit(-1);
		}
	
		String serverHostName = args[2];
		
		int fileServerPort = -1;
		try {
			fileServerPort = Integer.parseInt(args[3]);
		} catch (NumberFormatException e) {
			System.err.println("Invalid file server port.");
			System.exit(-1);
		}
		
		int metaDataServerPort = -1;
		try {
		 metaDataServerPort = Integer.parseInt(args[4]);
		} catch (NumberFormatException e) {
			System.err.println("Invalid meta data server port.");
			System.exit(-1);
		}
		
		SyncClient client = new SyncClient(clientId, rootDir, serverHostName, fileServerPort, metaDataServerPort);
		try {
//			System.out.println("Client is syncing now.");
//			System.out.println("ID: " + clientId + "\nRoot Directory: " + rootDirStr + "\n" + serverHostName
//								+ "\n" + fileServerPort + "\n" +  metaDataServerPort);
			client.sync();
		} 
		catch (UnknownHostException e) {
			e.printStackTrace();
		} 
		catch (IOException e) {
			e.printStackTrace();
		}
	}

}
