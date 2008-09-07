package tv.katsuma.jog;

public class TokyoJogging {
	public static void main(String[] args){
		HttpServer httpServer = new HttpServer();
		new WiimoteManager(httpServer);
		try{
			httpServer.start();
		} catch(Exception e){
			e.printStackTrace();
		}
	}
}
