package tj.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.continuation.Continuation;
import org.eclipse.jetty.continuation.ContinuationSupport;

import tj.server.HttpServer;

public class WiiDeviceHttpServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private HttpServer httpServer;
	public WiiDeviceHttpServlet(HttpServer httpServer) {
		this.httpServer = httpServer;
	}
	
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String callback = request.getParameter("callback");
		String responseMessage = getMessage();
		if (responseMessage == null) {
			Continuation continuation = ContinuationSupport.getContinuation(request);
			continuation.suspend();
		} else {
			if(callback != null) responseMessage = callback + "(" + responseMessage + ")";
			
			response.setContentType("text/html");
	        response.setStatus(HttpServletResponse.SC_OK);
	        response.getWriter().println("session=" + request.getSession(true).getId());			
		}
    }
	
	private String getMessage() {
		return this.httpServer.getMessageProxy().getMessage();
	}
}
