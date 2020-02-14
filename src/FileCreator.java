
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Calendar;
import java.util.Random;


public class FileCreator {
	
	
	static void usage() {
		System.out.println("Usage:");
		System.out.println("FileCreator ROOT_DIRECTORY NUMBER_OF_FILES");
	}
	
	public static File createFile(File rootDir) throws IOException {
		Random rand = new Random(Calendar.getInstance().getTimeInMillis());
		File file = null;
		do {
			String fileNameStr = "";
			for (int i = 0; i < GlobalConstants.FileNameSize; i++) {
				
				int randInt = -1;
				while((randInt = rand.nextInt(10)) == 0) {
					continue;
				}
				fileNameStr += randInt;
			}
			file = new File(rootDir.getCanonicalPath() + File.separator + fileNameStr);
		} while (file.exists());
		
		OutputStream os = new FileOutputStream(file);
		for (int i = 0; i < GlobalConstants.FileSize; i++) {
			os.write(rand.nextInt(256) + Byte.MIN_VALUE);
		}
		os.close();
		
		return file;
	}
	
	public static void createMetaData(File file, int nodeID) throws IOException {
		String metadataFileName = file.getPath();
		
		if (!file.getName().contains(".meta")) {
			metadataFileName += GlobalConstants.MetaDataFileSuffix;
		}
		System.out.println(metadataFileName);
		
		BufferedWriter out = new BufferedWriter(new FileWriter(metadataFileName));
		
		// Meta-file format: 1,1,3,1;04/27/2019 07:21:42
		// + 1 so that the current node also includes itself in the loop
		
		int i = 1;
		String stringToWrite = new String();
		while (i <= GlobalConstants.neighbours + 1) {
			
			if(i == nodeID) {
				if(i == GlobalConstants.neighbours + 1) {
					String temp = stringToWrite.substring(0, stringToWrite.length() - 1);
					stringToWrite = temp;
					stringToWrite += ";";
				}
			}
			else {
				if(i == GlobalConstants.neighbours + 1)
					stringToWrite += Integer.toString(i) + ",1;";
				else
					stringToWrite += Integer.toString(i) + ",1,";
			}
			
			i++;
		}
//		System.out.println("String to write: " + stringToWrite);
		out.write(stringToWrite);
		out.write(Utils.getCurrentTimestamp(GlobalConstants.zone));
		out.close();
		
	}
	
	public static void main(String[] args) {
		if (args.length != 3) {
			usage();
			System.exit(-1);
		}
		
		String rootDirStr = args[0];
		File rootDir = new File(rootDirStr);
		if (!rootDir.isDirectory()) {
			System.err.println("Root directory '" + rootDir + "' is no directorty.");
			System.exit(-1);
		}
		if (!rootDir.exists()) {
			System.err.println("Root directory '" + rootDir + "' does not exist.");
			System.exit(-1);
		}
		
		int numberOfFiles = -1;
		try {
			numberOfFiles = Integer.parseInt(args[1]);
		} catch (NumberFormatException e) {
			System.err.println("Invalid number of files");
			System.exit(-1);
		}
		if (numberOfFiles <= 0) {
			System.err.println("Invalid number of files");
			System.exit(-1);
		}
		
		int nodeID = -1;
		try {
			nodeID = Integer.parseInt(args[2]);
		} catch (NumberFormatException e) {
			System.err.println("Invalid node id");
			System.exit(-1);
		}
		
		
		for (int i = 0; i < numberOfFiles; i++) {
			File file;
			try {
				file = createFile(rootDir);
				createMetaData(file, nodeID);
			} 
			catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

}
