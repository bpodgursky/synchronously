import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import java.io.*;

public class DataServer {
	
	public final static String PUBLISH_FILE = "publish.list";
	
  public static void main(String[] args) throws IOException, InterruptedException {
		final List<PublishedStream> streams = new LinkedList<PublishedStream>();
	
  	Scanner scan = new Scanner(new File(PUBLISH_FILE));
  	
  	while(scan.hasNext()){
  		String filePath = scan.next();
  		Integer port = scan.nextInt();
  		File file = new File(filePath);
  		List<String> fields = Arrays.asList(scan.nextLine().trim().split("[\t ]"));
  		
  		if(!file.exists()) throw new FileNotFoundException(filePath);
  		
  		streams.add(PublishedStream.publish(file, port, fields));
  		
  		System.out.println("published "+file+" on port "+port+" with fields "+ fields);
  	}
  	
  	while(true) Thread.sleep(1000);
  }
}