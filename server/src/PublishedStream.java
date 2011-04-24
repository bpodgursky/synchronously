import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.io.input.Tailer;
import org.apache.commons.io.input.TailerListenerAdapter;


public class PublishedStream{

		protected final ServerSocket serverSocket;
		protected final Thread listenerThread;
		protected final Tailer inputFile;
		protected final List<Socket> openSockets;
		
		public ServerSocket getServerSocket() {
			return serverSocket;
		}

		public List<Socket> getOpenSockets() {
			return openSockets;
		}

		private PublishedStream(ServerSocket socket, Thread listener, Tailer input, List<Socket> connections){
			this.serverSocket = socket;
			this.listenerThread = listener;
			this.inputFile = input;
			this.openSockets = connections;
		}
		
		public static final PublishedStream publish(File input, int port) throws IOException{
			final ServerSocket serverSocket = new ServerSocket(port);
			final List<Socket> openSockets = Collections.synchronizedList(new LinkedList<Socket>());
			
    	Thread connectionListener = openConnectionListener(serverSocket, openSockets);
  	  Tailer tail = openFileTail(input, openSockets);
    	 
    	 return new PublishedStream(serverSocket, connectionListener, tail, openSockets);
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
		
		private static final Tailer openFileTail(final File inputFile, final List<Socket> openSockets){
			return Tailer.create(inputFile, new TailerListenerAdapter(){

				@Override
				public void fileNotFound() {
					System.err.println("input file not found!");
				}

				private final Object handleLock = new Object();
				
				@Override
				public void handle(String arg0) {
					synchronized(handleLock){
						System.out.println("pushing update: "+ arg0);
						
						Iterator<Socket> iter = openSockets.iterator();
						while(iter.hasNext()){
							Socket s = iter.next();
							
							if(s.isConnected()){
								try {
									s.getOutputStream().write((arg0+"\n").getBytes());
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
				}

				@Override
				public void handle(Exception arg0) {
					arg0.printStackTrace();
				}
			}, 10, true);
		}
	}