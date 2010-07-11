package tj.jog;

import java.util.logging.Logger;

import org.json.simple.JSONObject;

import tj.server.HttpServer;

public class IPhoneManager {
	private static final long serialVersionUID = 1L;
	private static final byte ACTION_JOG = 0x5;
	private static final int JOG_HIISTORY_SIZE = 2;
	private static final double ACCELERATION_THRESHOLD = 0.2;

	private final Logger logger;
	private final HttpServer httpServer;
	private int jogCounter = 0;
	private double prevXAcceleration = 0.0;
	private double prevYAcceleration = 0.0;
	private double prevZAcceleration = 0.0;
	private boolean isReceivedAccelerationFirst = true;

	public IPhoneManager(HttpServer httpServer, int iPhoneListeningServerPort) {
		this.logger = Logger.getLogger(this.getClass().getName());
		this.httpServer = httpServer;

		try {
			IPhoneListener.startServer(iPhoneListeningServerPort, this);
		} catch (Exception e) {
			this.logger.warning(e.getMessage());
		}
	}

	void accelerationInputReceived(double xAcceleration, double yAcceleration,
			double zAcceleration) {
		double deltaXAcceleration = Math.abs(xAcceleration
				- this.prevXAcceleration);
		double deltaYAcceleration = Math.abs(yAcceleration
				- this.prevYAcceleration);
		double deltaZAcceleration = Math.abs(zAcceleration
				- this.prevZAcceleration);

		this.prevXAcceleration = xAcceleration;
		this.prevYAcceleration = yAcceleration;
		this.prevZAcceleration = zAcceleration;

		if (this.isReceivedAccelerationFirst) {
			this.isReceivedAccelerationFirst = false;
			return;
		}

		if (deltaXAcceleration < ACCELERATION_THRESHOLD
				&& deltaYAcceleration < ACCELERATION_THRESHOLD
				&& deltaZAcceleration < ACCELERATION_THRESHOLD)
			return;

		this.jogCounter++;
		if (this.jogCounter == JOG_HIISTORY_SIZE) {
			JSONObject obj = new JSONObject();
			obj.put("xAcceleration", deltaXAcceleration);
			obj.put("yAcceleration", deltaYAcceleration);
			obj.put("zAcceleration", deltaZAcceleration);
			obj.put("action", ACTION_JOG);

			this.httpServer.setMessage(obj.toString());
			this.logger.info(obj.toString());
			this.jogCounter = 0;
		}
		return;
	}

	void buttonInputReceived(byte action) {
		JSONObject obj = new JSONObject();
		obj.put("action", action);

		if (obj.get("action") != null) {
			this.httpServer.setMessage(obj.toString());
			this.logger.info(obj.toString());
		}
	}
}
