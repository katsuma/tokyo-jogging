package tj.jog;

import tj.server.HttpServer;

public class TokyoJogging {
	
	public static void main(String[] args){
		HttpServer httpServer = new HttpServer();
		new WiimoteManager(httpServer);
		//new IPhoneManager(httpServer, 8081);
		httpServer.start();
	}
}
