package tj.server;

import java.util.logging.Logger;
import java.util.logging.Level;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import tj.servlet.WiiDeviceServlet;
import tj.socket.JogSocket;

public class HttpServer extends Thread {
	
	private int port = 8080;
	private Server server  = null;

	private Logger logger = Logger.getLogger(this.getClass().getName());

	public HttpServer() throws Exception {
		this.init();
	}
	public HttpServer(int port) throws Exception{
		this.port = port;
		this.init();
	}
	
	private void init() throws Exception{
		this.server = new Server(this.port);

	    WiiDeviceServlet wiiDeviceServlet = new WiiDeviceServlet();
	    ServletHolder sh = new ServletHolder(wiiDeviceServlet);
	    ServletContextHandler servletContextHandler = new ServletContextHandler();
	    servletContextHandler.addServlet(sh, "/ws/*");

	    HandlerList handlerList = new HandlerList();
	    handlerList.setHandlers(new Handler[] {servletContextHandler});
	    server.setHandler(handlerList);
	}
	
	public void run() {
		if(this.server != null){
			try {
				this.server.start();
			} catch (Exception e) {
				logger.log(Level.WARNING, "HTTP Server Exception", e);
			}
			logger.log(Level.INFO, "HTTP Server start...");
		}
	}

	public void setMessage(String message) {
		JogSocket.flushMessage(message);
	}
}
