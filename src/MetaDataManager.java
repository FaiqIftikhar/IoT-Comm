import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class MetaDataManager {

	private ArrayList<MetaData> metaData = new ArrayList<>();
	private File rootDir;
	
	public MetaDataManager(File rootDir) {
		this.rootDir = rootDir;
		createMetaInformation();
	}
	
	public void refreshMetaInformation() {
		if (!metaData.isEmpty())
			metaData.clear();
		
		createMetaInformation();
	}
	
	private void createMetaInformation() {
		File[] files = rootDir.listFiles();
		for (File file : files) {
			
			if (!file.getPath().endsWith(GlobalConstants.MetaDataFileSuffix)) 
				continue;
	
			try {
				metaData.add(new MetaData(file));
			} 
			catch (IOException e) {
				System.err.println("Failed to create MetaData for " + file.getName());
				e.printStackTrace();
			}
		}
	}
	
	public String getFilesToSync(String clientID) {
		String filesToSync = "";
		
		for (MetaData mData: this.metaData) {
			String info = mData.getClientInfo(clientID);
			
			if (!info.isEmpty()) 
				filesToSync += (info + ";");
		}
		
		if (filesToSync.isEmpty())
			return "";
		else
			return filesToSync + GlobalConstants.NodeID;
	}
	
	public Boolean hasFile(String fileName) {
		for (MetaData mData: this.metaData) {
			if (fileName.equals(mData.getFileName())) 
				return true;
		}
		return false;
	}

	public String getTimeStamp(String fileName) {
		if (!hasFile(fileName)) {
			System.err.println("MDManager does not have file " + fileName);
			System.exit(1);
		}
		
		for (MetaData mData: this.metaData) {
			if (fileName.equals(mData.getFileName())) 
				return mData.getTimeStamp();
		}
		System.err.println("Should not print this. (MDManager Class");
		return "";
	}

	public void setModificationBit(String fileName, String clientID, int bit) throws IOException {
		for (MetaData mData: this.metaData) {
			if (fileName.equals(mData.getFileName())) {
				mData.setModificationBit(clientID, bit);
				mData.updateMetaFile();
			}
		}
	}

	
	private void updataMetaDataFile(File file, int nodeID, int partnerNode, String timeStamp) throws IOException {
		String metadataFileName = file.getPath();
		
		if (!file.getName().contains(".meta")) {
			metadataFileName += GlobalConstants.MetaDataFileSuffix;
		}
		System.out.println(metadataFileName);
		System.out.println(nodeID + " received at " + SyncClient.clientId);
		
		BufferedWriter out = new BufferedWriter(new FileWriter(metadataFileName));
		
		// Meta-file format: 1,1,3,1;04/27/2019 07:21:42
		// + 1 so that the current node also includes itself in the loop
		
		int i = 1;
		String stringToWrite = new String();
		while (i <= GlobalConstants.neighbours + 1) {
			
			if (i == nodeID) {
				if(i == GlobalConstants.neighbours + 1) {
					String temp = stringToWrite.substring(0, stringToWrite.length() - 1);
					stringToWrite = temp;
					stringToWrite += ";";
				}
			}
			else {
				if (i == partnerNode) {
					if (i == GlobalConstants.neighbours + 1)
						stringToWrite += Integer.toString(i) + ",0;";
					else
						stringToWrite += Integer.toString(i) + ",0,";
				}
				else {
					if (i == GlobalConstants.neighbours + 1)
						stringToWrite += Integer.toString(i) + ",1;";
					else
						stringToWrite += Integer.toString(i) + ",1,";
				}
			}
			i++;
		}
		System.out.println("String to write: " + stringToWrite);
		out.write(stringToWrite);
		out.write(timeStamp);
		out.close();
		
	}
	
	public void updateMetaData(String fileName, String timeStamp, String partnerNode) throws IOException {
		if (hasFile(fileName)) {
			for (MetaData mData: this.metaData) {
				if (fileName.equals(mData.getFileName())) {
					mData.setTimeStamp(timeStamp);
					mData.setModificationBit(partnerNode, 0);
					mData.updateMetaFile();
				}
			}
		}
		else {
			String filePath = rootDir.getCanonicalPath() + "\\" + fileName + GlobalConstants.MetaDataFileSuffix;
			File metaFile = new File(filePath);
			
			// TODO: Change ID to GlobalConstant ID
			this.updataMetaDataFile(metaFile, Integer.parseInt(SyncClient.clientId), 
									Integer.parseInt(partnerNode), timeStamp);
			
			

		}
	}
	
}
