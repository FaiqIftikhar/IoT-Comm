import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Random;


public class FileShuffler {
	// Change every 10th file
	public final static float ChangeProbability = 0.5f;
	
	private static void usage() {
		System.out.println("Usage:");
		System.out.println("FileShuffler ROOT_DIRECTORY");
	}
	
	private static void changeFile(File file) throws IOException {
		Random rand = new Random();
		
		file.delete();
		OutputStream os = new FileOutputStream(file);
		for (int i = 0; i < GlobalConstants.FileSize; i++) {
			os.write(rand.nextInt(256) + Byte.MIN_VALUE);
		}
		os.close();
	}
	
	private static void changeMetaData(File file) throws IOException {
		String metadataFileName = file.getCanonicalPath() + GlobalConstants.MetaDataFileSuffix;

		File metaDataFile = new File(metadataFileName);
		metaDataFile.delete();
		
		BufferedWriter out = new BufferedWriter(new FileWriter(metadataFileName));
		
		int i = 1;
		String stringToWrite = new String();
		while (i <= GlobalConstants.neighbours + 1) {
			
			if(i == GlobalConstants.NodeID) {
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
		out.write(stringToWrite);
		out.write(Utils.getCurrentTimestamp(GlobalConstants.zone));
		out.close();
	}
	
	public static void main(String[] args) {
		if (args.length != 1) {
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
		
		Random rand = new Random();
		File[] files = rootDir.listFiles();
		for (int i = 0; i < files.length; i++) {
			File file = files[i];
			if (file.getPath().endsWith(GlobalConstants.MetaDataFileSuffix)) {
				continue;
			}
			
			double r = rand.nextDouble();
			if (r <= ChangeProbability) {
				try {
					changeFile(file);
					changeMetaData(file);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

}
