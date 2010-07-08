package tj.server;

import java.io.IOException;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.Vector;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.mortbay.jetty.HttpConnection;
import org.mortbay.jetty.Request;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.handler.ResourceHandler; 
import org.mortbay.util.ajax.Continuation;
import org.mortbay.util.ajax.ContinuationSupport;


public class HttpServer extends Thread{
	
	private int port = 8080;
	private Server server  = null;
	private Object mutex = new Object();
	private String message = null;
	private Vector< Continuation> continuations = new Vector< Continuation>();

	private Logger logger = Logger.getLogger(this.getClass().getName());

	public HttpServer() {
		this.init();
	}
	public HttpServer(int port){
		this.port = port;
		this.init();
	}
	
	private void init(){
		this.server = new Server(this.port);
		WiimoteHandler wiimoteHandler = new WiimoteHandler();
		this.server.setHandler(wiimoteHandler);
	}
	
	public void run() {
		if(this.server!=null){
			try {
				this.server.start();
			} catch (Exception e) {
				logger.log(Level.WARNING, "HTTP Server Exception", e);
			}
			logger.log(Level.INFO, "HTTP Server start...");
		}
	}

	public void setMessage(String message) {
		this.message = message;
		if(message!=null && !message.equals("")){
			for(Continuation c : continuations){
				c.resume();
			}
			this.continuations.removeAllElements();
		}
	}
	
	public class WiimoteHandler extends ResourceHandler{
		

		private void returnStatus(HttpServletResponse response, String callback){
			try{
				response.setContentType("text/javascript; charset=utf-8");
				response.setStatus(HttpServletResponse.SC_OK);
				String responseMessage = getJSONMessage();
				if(callback!=null){
					responseMessage = callback + "(" + responseMessage + ")";
				}
				response.getWriter().print(responseMessage);
				logger.log(Level.INFO, "flush message:" + responseMessage);
			} catch (IOException e){
				e.printStackTrace();
			}
			message = null;			
		}
		
		public void handle(
				String target, HttpServletRequest request, 
				HttpServletResponse response, int dispatch
			) throws IOException, ServletException {
			logger.log(Level.INFO, "Servlet handle was called " + request.getRequestURI());
			Request baseRequest = (request instanceof Request) ? (Request)request:HttpConnection.getCurrentConnection().getRequest();
			baseRequest.setHandled(true);
			
			String callback = request.getParameter("callback");
			
			// flush message
			Continuation continuation =  ContinuationSupport.getContinuation(request, mutex);
			logger.log(Level.INFO, "Current message:" + message);
			if(message==null){
				logger.log(Level.INFO, "Start to suspend.............");		
				continuations.add(continuation);
				continuation.suspend(60*1000);
			}
			this.returnStatus(response, callback);
			
		}
	}
	
	
	private String getJSONMessage(){
		return (this.message==null || this.message.equals(""))? "{}" : this.message;
	}
}
