package tj.servlet;

import javax.servlet.http.HttpServletRequest;

import org.eclipse.jetty.websocket.WebSocket;
import org.eclipse.jetty.websocket.WebSocketServlet;

import tj.net.JogSocket;

public class WiiDeviceWebSocketServlet extends WebSocketServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	protected WebSocket doWebSocketConnect(HttpServletRequest request, String protocol) {
		return new JogSocket();
	}

}
