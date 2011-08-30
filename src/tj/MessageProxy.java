package tj;

import tj.net.JogSocket;
import tj.server.HttpServer;

public class MessageProxy {
	private HttpServer httpServer;
	private String message;
	
	public MessageProxy(HttpServer httpServer){
		this.httpServer = httpServer;
	}
	public synchronized void setMessage(String message) {
		this.message = message;
		JogSocket.flushMessage(message);
	}
	
	public synchronized String getMessage() {
		return this.message;
	}
}
