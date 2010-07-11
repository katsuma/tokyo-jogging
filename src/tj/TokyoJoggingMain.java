package tj;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Logger;

import tj.jog.IPhoneManager;
import tj.jog.WiimoteManager;
import tj.server.HttpServer;
import tj.walk.BalanceBoardManager;

public class TokyoJoggingMain {
	public static void main(String[] args){
		Logger logger = Logger.getLogger("Tokyo-Jogging");

		try{
			Properties prop = new Properties();
			prop.load(new FileInputStream("config.properties"));
			
			boolean isWiimoteAvailable = prop.getProperty("IS_WIIMOTE_AVAILABLE").equalsIgnoreCase("true")? true : false;
			boolean isBalanceBoardAvailable = prop.getProperty("IS_BALANCE_BOARD_AVAILABLE").equalsIgnoreCase("true")? true : false;
			int httpServerPort = Integer.parseInt(prop.getProperty("HTTP_SERVER_PORT"));
			boolean isIPhoneAvailable = prop.getProperty("IS_IPHONE_AVAILABLE").equalsIgnoreCase("true");
			int iPhoneServerPort = (isIPhoneAvailable) ? Integer.parseInt(prop.getProperty("IPHONE_SERVER_PORT")) : -1;
			
			logger.info("isWiimoteAvailable : " + isWiimoteAvailable);
			logger.info("isBalanceBoardAvailable : " + isBalanceBoardAvailable);
			logger.info("isIPhoneAvailable : " + isIPhoneAvailable);
			logger.info("HttpSeverPort : " + httpServerPort);
			if (isIPhoneAvailable) { logger.info("iPhoneServerPort : " + iPhoneServerPort); }
			
			HttpServer httpServer = new HttpServer(httpServerPort);
			if(isWiimoteAvailable){
				new WiimoteManager(httpServer);
			}
			if(isBalanceBoardAvailable){
				new BalanceBoardManager(httpServer);
			}
			if(isIPhoneAvailable){
				new IPhoneManager(httpServer, iPhoneServerPort);
			}
			
			if(isWiimoteAvailable || isBalanceBoardAvailable || isIPhoneAvailable){
				httpServer.start();
			}

		}catch(IOException e){
			logger.warning("config.properties file was not found.");
		} catch(NumberFormatException e){
			logger.warning("JOG_HTTP_SERVER_PORT, WALK_HTTP_SERVER_PORT or IPHONE_SERVER_PORT may be not number.");		
			e.printStackTrace();
		}
	}
}
