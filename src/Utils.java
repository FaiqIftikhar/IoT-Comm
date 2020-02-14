import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.Date;

public class Utils {

	public static String getCurrentTimestamp(String zoneIDStr) 
	{
	    DateTimeFormatter formatter = new DateTimeFormatterBuilder()
	        .appendPattern("MM").appendLiteral("/")
	        .appendPattern("dd").appendLiteral("/")
	        .appendPattern("yyyy").appendLiteral(" ")
	        .appendPattern("hh").appendLiteral(":")
	        .appendPattern("mm").appendLiteral(":")
	        .appendPattern("ss")
	        .toFormatter();
	
	    return formatter.withZone(ZoneId.of(zoneIDStr)).format(Instant.now());
	  }
	
	// Concatenates the given array of Strings into a single string
	// using the provided 'delim' as the delimiter
	public static String concatListToString(String[] strArr, String delim) 
	{
		StringBuilder strBuilder = new StringBuilder();
		for (int i = 0; i < strArr.length; i++) 
		{
		   strBuilder.append(strArr[i]);
		   
		   if(i != strArr.length - 1)
			   strBuilder.append(delim);
		}
		return strBuilder.toString();
	}
	
	public static int compareTimestamps(String ts1, String ts2) 
	{
	    SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss");

	    Timestamp ds1 = null;
	    Timestamp ds2 = null;

	    try 
	    {
	      Date date1 = dateFormat.parse(ts1);
	      ds1 = new Timestamp(date1.getTime());

	      Date date2 = dateFormat.parse(ts2);
	      ds2 = new Timestamp(date2.getTime());
	    }
	    catch(ParseException e) {
	      System.err.println("Failed to convert timestamps!");
	    }
	    return ds1.compareTo(ds2);
	}
	
	public static String removeExt(String filename) 
	{
		return filename.substring(0, filename.indexOf("."));
	}
	
	
	
}
