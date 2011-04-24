import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.io.input.Tailer;
import org.apache.commons.io.input.TailerListenerAdapter;
import org.json.JSONException;
import org.json.JSONObject;


public class PublishedStream{

		protected final ServerSocket serverSocket;
		protected final Thread listenerThread;
		protected final Tailer inputFile;
		protected final List<Socket> openSockets;
		protected final List<String> dataFields;
		
		public ServerSocket getServerSocket() {
			return serverSocket;
		}

		public List<Socket> getOpenSockets() {
			return openSockets;
		}

		private PublishedStream(ServerSocket socket, Thread listener, Tailer input, List<Socket> connections, List<String> fields){
			this.serverSocket = socket;
			this.listenerThread = listener;
			this.inputFile = input;
			this.openSockets = connections;
			this.dataFields = fields;
		}
		
		public static final PublishedStream publish(File input, int port, List<String> fields) throws IOException{
			final ServerSocket serverSocket = new ServerSocket(port);
			final List<Socket> openSockets = Collections.synchronizedList(new LinkedList<Socket>());
			
    	Thread connectionListener = openConnectionListener(serverSocket, openSockets);
  	  Tailer tail = openFileTail(input, openSockets, fields);
    	 
    	 return new PublishedStream(serverSocket, connectionListener, tail, openSockets, fields);
		}
		
		private static final Thread openConnectionListener(final ServerSocket serverSocket, final List<Socket> openSockets){
			Thread t = new Thread(new Runnable(){
				@Override
				public void run() {
					while(true){
						try{
							Socket s = serverSocket.accept();
							System.out.println("connected to client at: "+s.getInetAddress());
							
							openSockets.add(s);
	          } catch (IOException e) {
	              System.err.println("Connection failed.");
	          		e.printStackTrace();
	          }
					}
				}});
    	t.start();
    	
    	return t;
		}
		
		private static final Tailer openFileTail(final File inputFile, final List<Socket> openSockets, final List<String> dataFields){
			return Tailer.create(inputFile, new TailerListenerAdapter(){

				@Override
				public void fileNotFound() {
					System.err.println("input file not found!");
				}

				private final Object handleLock = new Object();
				
				@Override
				public void handle(String arg0) {
	  			try {
						synchronized(handleLock){
							JSONObject json = new JSONObject();
							
				  		List<String> data = Arrays.asList(arg0.split("\t"));
				  		
				  		for(int i = 0; i < data.size(); i++){
								json.put(dataFields.get(i), data.get(i));
				  		}
				  		String publish = json.toString();
							
							System.out.println("pushing update: "+ publish);
							
							Iterator<Socket> iter = openSockets.iterator();
							while(iter.hasNext()){
								Socket s = iter.next();
								
								if(s.isConnected()){
									try {
										s.getOutputStream().write((publish+"\n").getBytes());
									} catch (SocketException e){
										System.out.println("disconnected from client at "+s.getInetAddress());
										iter.remove();
									} catch (IOException e) {
										e.printStackTrace();
									}
									
								}else{
									iter.remove();
								}
							}
						}
					} catch (JSONException e) {
						System.err.println("data string <"+arg0+" violates provided fields <"+dataFields+">");
						e.printStackTrace();
					}
				}

				@Override
				public void handle(Exception arg0) {
					arg0.printStackTrace();
				}
			}, 10, true);
		}
	}