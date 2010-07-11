package tj.jog;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.mortbay.jetty.Connector;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.nio.SelectChannelConnector;
import org.mortbay.jetty.servlet.ServletHandler;

public class IPhoneListener extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private final Logger logger = Logger.getLogger(this.getClass().getName());
	private static Map<Integer, IPhoneManager> port2DelegateMap = new HashMap<Integer, IPhoneManager>();

	static void startServer(final int port, IPhoneManager delegate)
			throws Exception {
		if (port2DelegateMap.containsKey(port)) {
			throw new java.net.BindException("Port already in use: " + port);
		}
		port2DelegateMap.put(port, delegate);
		new Thread(new Runnable() {
			public void run() {
				try {
					Server server = new Server();
					Connector connector = new SelectChannelConnector();
					connector.setPort(port);
					server.addConnector(connector);

					ServletHandler handler = new ServletHandler();
					handler.addServletWithMapping(IPhoneListener.class, "/");

					server.addHandler(handler);
					server.start();
					server.join();
				} catch (Exception e) {
					Logger logger = Logger.getLogger(this.getClass().getName());
					logger.warning("IPhoneListener server error: "
							+ e.getMessage());
				}
			}
		}).start();
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse res)
			throws ServletException, IOException {
		String path = req.getRequestURI();

		if (path.equals("/command")) {
			try {
				handleCommand(req);
				int length = response(req, res);
				log(req, length, HttpServletResponse.SC_OK);
			} catch (IllegalArgumentException e) {
				res.sendError(HttpServletResponse.SC_BAD_REQUEST);
				log(req, -1, HttpServletResponse.SC_BAD_REQUEST);
				this.logger.log(Level.INFO, e.getMessage());
			} catch (IllegalStateException e) {
				res.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				log(req, -1, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				this.logger.log(Level.WARNING, e.getMessage());
			} catch (Exception e) {
				res.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				log(req, -1, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				this.logger.log(Level.WARNING, e.getMessage());
			}
		} else {
			res.sendError(HttpServletResponse.SC_NOT_FOUND);
			log(req, -1, HttpServletResponse.SC_NOT_FOUND);
		}
	}

	private void handleCommand(HttpServletRequest req) {
		if (req.getParameter("action") != null) {
			byte action = 0;
			try {
				action = Byte.parseByte(req.getParameter("action"));
			} catch (NumberFormatException e) {
				throw new IllegalArgumentException(
						"Illegal number format arguments");
			}
			if (port2DelegateMap.containsKey(req.getLocalPort())) {
				IPhoneManager delegate = port2DelegateMap.get(req
						.getLocalPort());
				if (delegate != null) {
					delegate.buttonInputReceived(action);
					return;
				}
			}
			throw new IllegalStateException(
					"not having IPhoneManager instance for server port:"
							+ req.getLocalPort());
		} else if (req.getParameter("xa") != null
				&& req.getParameter("ya") != null
				&& req.getParameter("za") != null) {
			double xAcceleration = 0;
			double yAcceleration = 0;
			double zAcceleration = 0;
			try {
				xAcceleration = Double.parseDouble(req.getParameter("xa"));
				yAcceleration = Double.parseDouble(req.getParameter("ya"));
				zAcceleration = Double.parseDouble(req.getParameter("za"));
			} catch (NumberFormatException e) {
				throw new IllegalArgumentException(
						"Illegal number format arguments");
			}
			if (port2DelegateMap.containsKey(req.getLocalPort())) {
				IPhoneManager delegate = port2DelegateMap.get(req
						.getLocalPort());
				if (delegate != null) {
					delegate.accelerationInputReceived(xAcceleration,
							yAcceleration, zAcceleration);
					return;
				}
			}
			throw new IllegalStateException(
					"not having IPhoneManager instance for server port:"
							+ req.getLocalPort());
		} else {
			throw new IllegalArgumentException("Missing necessary arguments");
		}
	}

	private int response(HttpServletRequest req, HttpServletResponse res)
			throws ServletException, IOException {
		res.setDateHeader("Date", (new Date()).getTime());
		res.setContentType("text/plain; charset=utf-8");
		res.setHeader("Cache-control", "max-age=7200");
		res.setHeader("Connection", "close");

		PrintWriter out = res.getWriter();
		String s = "[{'success':true}]";
		out.print(s);
		return s.length();
	}

	private void log(HttpServletRequest req, int length, int serverStatus) {
		String s = req.getRemoteAddr()
				+ " \""
				+ req.getMethod()
				+ " "
				+ req.getRequestURI()
				+ " "
				+ req.getProtocol()
				+ "\" "
				+ serverStatus
				+ " "
				+ length
				+ " \""
				+ req.getHeader("user-agent")
				+ "\" \""
				+ req.getRequestURL()
				+ ((req.getQueryString() == null) ? "" : ("?" + req
						.getQueryString())) + "\"";
		this.logger.log(Level.INFO, s);
	}
}
