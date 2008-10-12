package tj.jog;

import tj.server.HttpServer;

public class TokyoJogging {
	
	public static void main(String[] args){
		HttpServer httpServer = new HttpServer();
		new WiimoteManager(httpServer);
		httpServer.start();
	}
}
