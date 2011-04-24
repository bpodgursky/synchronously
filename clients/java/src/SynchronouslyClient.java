import java.io.*;
import java.net.*;
import java.util.Scanner;

public class SynchronouslyClient {
    public static void main(String[] args) throws IOException {
    	String host = args[0];
    	Integer port = Integer.parseInt(args[1]);
    	
    	System.out.println("opening connection to "+host+" port "+port);
    		Scanner s = new Scanner(SynchronouslyClient.connect(host, port));
    	
        while(true){
        	String line = s.nextLine();
        	
        	System.out.println("from server: "+line);
        }
    }
    
    public static final InputStream connect(String host, int port){

      Socket kkSocket = null;

      try {
          kkSocket = new Socket(host, port);
          return kkSocket.getInputStream();
      } catch (UnknownHostException e) {
          System.err.println("Don't know about host: "+host);
          System.exit(1);
      } catch (IOException e) {
          System.err.println("Couldn't get I/O for the connection to: "+host);
          System.exit(1);
      }

      return null;
    }
}
