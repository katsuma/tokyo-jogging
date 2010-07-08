package tj.jog;

import java.io.IOException;
import java.util.logging.Logger;
import java.util.logging.Level;

import org.json.simple.JSONObject;

import tj.server.HttpServer;
import wiiremotej.WiiRemote;
import wiiremotej.WiiRemoteExtension;
import wiiremotej.WiiRemoteJ;
import wiiremotej.event.WRAccelerationEvent;
import wiiremotej.event.WRButtonEvent;
import wiiremotej.event.WRCombinedEvent;
import wiiremotej.event.WRExtensionEvent;
import wiiremotej.event.WRIREvent;
import wiiremotej.event.WRStatusEvent;
import wiiremotej.event.WiiRemoteListener;
import wiiremotej.event.WRNunchukExtensionEvent;
import wiiremotej.AnalogStickData;


public class WiimoteManager extends Thread implements WiiRemoteListener{
	private HttpServer httpServer;
	private Logger logger;
	
	private WiiRemote wiiRemote = null;
	
	public static final byte ACTION_UP = 0x1;
	public static final byte ACTION_RIGHT = 0x2;
	public static final byte ACTION_DOWN = 0x3;
	public static final byte ACTION_LEFT = 0x4;
	public static final byte ACTION_JOG = 0x5;
	public static final byte ACTION_ZOOM_IN = 0x6;
	public static final byte ACTION_ZOOM_OUT = 0x7;
	public static final byte ACTION_NOT_DETECTED = -0x1;
	
	private final int JOG_HIISTORY_SIZE = 12;
	private int jogCounter = 0;
	
	public WiimoteManager(HttpServer httpServer){
		this.httpServer = httpServer;
		this.httpServer = httpServer;
		this.logger = Logger.getLogger(this.getClass().getName());
		this.start();
		
	}
	
	public void run(){
		try {
			this.wiiRemote = WiiRemoteJ.findRemote();
			if(this.wiiRemote!=null){
				logger.info("Detected your wiimote.");
			}
			this.wiiRemote.addWiiRemoteListener(this);
			this.wiiRemote.setAccelerometerEnabled(true);
		} catch (IllegalStateException e){
			logger.log(Level.SEVERE, "IllegalStateException", e);
		} catch (InterruptedException e) {
			logger.log(Level.SEVERE, "InterruptedException", e);
		} catch (IOException e) {
			logger.log(Level.SEVERE, "IOException", e);
		} catch (NullPointerException e){
			logger.log(Level.SEVERE, "NullException. We could not find your Wiimote.", e);
			System.exit(1);
			return;
		}
	}

	public void IRInputReceived(WRIREvent evt) {
		logger.info("WRIREvent");
	}

	public void accelerationInputReceived(WRAccelerationEvent evt) {
		double jogThreashold = 0.8;

		double xAcceleration = evt.getXAcceleration();
		double yAcceleration = evt.getYAcceleration();
		double zAcceleration = evt.getZAcceleration();
		
		double absX = Math.abs(xAcceleration);
		double absY = Math.abs(yAcceleration);
		double absZ = Math.abs(zAcceleration);
		
		if(absX<jogThreashold || absY<jogThreashold || absZ<jogThreashold) return;


		this.jogCounter++;
		if (this.jogCounter == JOG_HIISTORY_SIZE) {
			JSONObject obj = new JSONObject();
			obj.put("xAcceleration",  xAcceleration);
			obj.put("yAcceleration", yAcceleration);
			obj.put("zAcceleration", zAcceleration);
			obj.put("action", ACTION_JOG);
			
			this.httpServer.setMessage(obj.toString());
			logger.info(obj.toString());
			this.jogCounter = 0;
		}
		return;			
	}

	public void buttonInputReceived(WRButtonEvent evt) {
		JSONObject obj = new JSONObject();
		if(evt.wasPressed(WRButtonEvent.LEFT)){
			obj.put("action", ACTION_LEFT);
		} else if(evt.wasPressed(WRButtonEvent.RIGHT)){
			obj.put("action", ACTION_RIGHT);			
		} else if(evt.wasPressed(WRButtonEvent.UP)){
			obj.put("action", ACTION_UP);
		} else if(evt.wasPressed(WRButtonEvent.DOWN)){
			obj.put("action", ACTION_DOWN);		
		} else if(evt.wasPressed(WRButtonEvent.PLUS)){
			obj.put("action", ACTION_ZOOM_IN);
		} else if(evt.wasPressed(WRButtonEvent.MINUS)){
			obj.put("action", ACTION_ZOOM_OUT);					
		}
		
		if(obj.get("action")!=null){
			this.httpServer.setMessage(obj.toString());
			logger.info(obj.toString());	
		}
	}

	public void combinedInputReceived(WRCombinedEvent evt) {
		
	}

	public void disconnected() {
		logger.info("disconnect");	
		System.exit(MAX_PRIORITY);
	}

	public void extensionConnected(WiiRemoteExtension evt) {
		try {
			this.wiiRemote.setExtensionEnabled(true);
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void extensionDisconnected(WiiRemoteExtension evt) {
	}

	public void extensionInputReceived(WRExtensionEvent evt) {
		if (evt instanceof WRNunchukExtensionEvent) {
			nunchukEventReceived((WRNunchukExtensionEvent)evt);
			return;
		}
	}
	
	private void nunchukEventReceived(WRNunchukExtensionEvent evt) {
		AnalogStickData analogStickData = evt.getAnalogStickData();
		double threashold = 0.1;
		double analogStickX = analogStickData.getX();
		double analogStickY = analogStickData.getY();
		double analogStickAngle = analogStickData.getAngle();
		
		if (Math.abs(analogStickX)<threashold || Math.abs(analogStickY)<threashold) return;
		logger.info("analogStick:[" + analogStickX + "," + analogStickY + ", " +  analogStickAngle + "]");
		JSONObject obj = new JSONObject();
		byte direction;
		if(Math.abs(analogStickX) < Math.abs(analogStickY)){
			direction = analogStickY > 0 ? ACTION_DOWN : ACTION_UP;		
		} else {
			direction = analogStickX > 0 ? ACTION_RIGHT : ACTION_LEFT;		
		}
		obj.put("action", direction);
		
		if(obj.get("action")!=null){
			this.httpServer.setMessage(obj.toString());
			logger.info(obj.toString());	
		}
	}

	
	
	public void extensionPartiallyInserted() {
		logger.info("extensionPartiallyInserted");		
	}

	public void extensionUnknown() {		
	}

	public void statusReported(WRStatusEvent evt) {
		logger.info("WRStatusEvent");			
	}
}
