package tj.walk;

import tj.server.HttpServer;

public class TokyoWalking {
	
	public static void main(String[] args){		
		HttpServer httpServer = new HttpServer();
		new BalanceBoardManager(httpServer);
		httpServer.start();
	}
}
