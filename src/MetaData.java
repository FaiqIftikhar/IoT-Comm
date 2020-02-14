import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class MetaData {
	
	private String fileName;
	private String filePath;
	private String timeStamp;
	private HashMap<Integer, Integer> modifiedBits = new HashMap<>();
	
	
	public MetaData(File metaFile) throws IOException {
		this.filePath = metaFile.getCanonicalPath();
		
		if (metaFile.getName().contains(".meta")) {
			this.fileName = Utils.removeExt(metaFile.getName());
		}
		
		parseFile(metaFile);
	}
	
	public String getFileName() {
		return this.fileName;
	}
	
	public String getFilePath() {
		return this.filePath;
	}
	
	public String getTimeStamp() {
		return this.timeStamp;
	}

	public void setTimeStamp(String ts) {
		this.timeStamp = ts;
	}
	
	public Boolean hasClient(String clientID) {
		return modifiedBits.containsKey(Integer.parseInt(clientID));
	}
	
	public String getClientInfo(String clientID) {
		if (!hasClient(clientID)) {
			System.err.println(clientID + " does not exist in metadata of file " + filePath);
			System.exit(-1);
		}
		int client = Integer.parseInt(clientID);
		
		// Only send if modified bit is 1
		if (modifiedBits.get(client).equals(1)) {
			return fileName + ";" + timeStamp;
		}
		else {
			return "";
		}
	}

	public void setModificationBit(String clientID, int bit) throws IOException {
		if (bit < 0 || bit > 1) {
			System.err.println("Invalid modification bit");
			System.exit(-1);
		}
		
		int client = Integer.parseInt(clientID);
		this.modifiedBits.put(client, bit);
	}
	
	private void parseFile(File file) throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(this.filePath));
		String line = reader.readLine();	
		
		// Separate timestamp and modified bits of nodes
		String splitLine[] = line.split(";");
		
		if (splitLine.length != 2) {
			System.err.println("Metadata file is not in the proper format");
			System.exit(-1);
		}
		this.timeStamp = splitLine[1];
	
		
		String[] modBits = splitLine[0].split(",");
		
		if (modBits.length % 2 != 0) {
			System.err.println("Metadata file has invalid number of node id's/modified bits");
			System.exit(-1);
		}
		
		for (int i = 0; i < modBits.length; i += 2) {
			this.modifiedBits.put( Integer.parseInt(modBits[i]), 		// Node id
								   Integer.parseInt(modBits[i + 1]) );  // Modified bit
		}
		
		reader.close();
	}
	
	public void updateMetaFile() throws IOException {
		File metaDataFile = new File(this.filePath);
		metaDataFile.delete();
		
		FileWriter writer = new FileWriter(this.filePath); 
		
		String newMetaInfo = "";
		// Meta-file format: 1,1,3,1;04/27/2019 07:21:42
		for (Map.Entry<Integer, Integer> entry : this.modifiedBits.entrySet()) {
			newMetaInfo += (entry.getKey() + "," + entry.getValue()  + ",");
		}
		newMetaInfo = newMetaInfo.substring(0, newMetaInfo.length() - 1); // Remove extra ','
		newMetaInfo += (";" +this.timeStamp);
	
		writer.write(newMetaInfo);
		writer.close();
	}
	
}
