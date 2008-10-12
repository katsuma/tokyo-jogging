package tj.walk;

import java.io.IOException;
import java.util.logging.Logger;
import java.util.logging.Level;

import org.json.simple.JSONObject;

import tj.server.HttpServer;
import wiiremotej.BalanceBoard;
import wiiremotej.WiiRemoteJ;
import wiiremotej.MassConstants;
import wiiremotej.event.BBButtonEvent;
import wiiremotej.event.BBCombinedEvent;
import wiiremotej.event.BBMassEvent;
import wiiremotej.event.BBStatusEvent;
import wiiremotej.event.BalanceBoardListener;

public class BalanceBoardManager extends Thread implements BalanceBoardListener {
	
	private BalanceBoard balanceBoard = null;
	private HttpServer httpServer;	
	private Logger logger;
	
	public static final byte BALANCE_UP= 0x1;
	public static final byte BALANCE_RIGHT = 0x2;
	public static final byte BALANCE_DOWN = 0x3;
	public static final byte BALANCE_LEFT = 0x4;
	public static final byte BALANCE_CENTER = 0x5;
	public static final byte BALANCE_ZOOM_IN = 0x6;
	public static final byte BALANCE_ZOOM_OUT = 0x7;
	public static final byte BALANCE_NOT_DETECTED = -0x1;
	
	
	public BalanceBoardManager(HttpServer httpServer) {
		this.httpServer = httpServer;
		this.logger = Logger.getLogger(this.getClass().getName());
		this.start();
		
	}
	
	public void run(){
		logger.info("Start BalanceboardManager.");
		
		try{
			this.balanceBoard = WiiRemoteJ.findBalanceBoard();
			if(this.balanceBoard!=null){
				logger.info("Detected your balance board.");
			}
			this.balanceBoard.addBalanceBoardListener(this);
		} catch (IllegalStateException e){
			logger.log(Level.SEVERE, "IllegalStateException", e);
		} catch (InterruptedException e) {
			logger.log(Level.SEVERE, "InterruptedException", e);
		} catch (IOException e) {
			logger.log(Level.SEVERE, "IOException", e);
		} catch (NullPointerException e){
			logger.log(Level.SEVERE, "NullException. We couold not find Balance Wii Board.", e);
			System.exit(1);
			return;
		}
		
		if(this.balanceBoard==null){
			logger.log(Level.SEVERE, "Initialize Error and terminataed");
			return;
		}
	}

	public void buttonInputReceived(BBButtonEvent evt) { 
	}

	public void combinedInputReceived(BBCombinedEvent evt) {
	}

	public void disconnected() {
	}

	public void massInputReceived(BBMassEvent evt) {
		double threshold = 1.0;
		double top = evt.getMass(MassConstants.TOP, (int)Math.floor((MassConstants.LEFT + MassConstants.RIGHT)/2));
		double right = evt.getMass((int)Math.floor(MassConstants.TOP + MassConstants.BOTTOM),  MassConstants.RIGHT);
		double BOTTOM = evt.getMass(MassConstants.BOTTOM, (int)Math.floor((MassConstants.LEFT + MassConstants.RIGHT)/2));
		double left = evt.getMass((int)Math.floor(MassConstants.TOP + MassConstants.BOTTOM),  MassConstants.LEFT);
		double totalMass = evt.getTotalMass();
		
		double topRatio = top / (top + right + BOTTOM + left);
		double rightRatio = right / (top + right + BOTTOM + left);
		double BOTTOMRatio = BOTTOM / (top + right + BOTTOM + left);
		double leftRatio = left / (top + right + BOTTOM + left);

		// check value by threshold
		if(top<threshold || left<threshold || right<threshold || BOTTOM<threshold) return;

		byte direction = getDirectionByMass(topRatio, rightRatio, BOTTOMRatio, leftRatio);
		logger.info("Direction(1):" + direction);
		
		// dispatch job to HttpServer
		JSONObject obj = new JSONObject();
		obj.put("Top",  topRatio);
		obj.put("Right", rightRatio);
		obj.put("BOTTOM", BOTTOMRatio);
		obj.put("Left", leftRatio);
		obj.put("Direction", direction);
		obj.put("TotalMass", totalMass);
		
		this.httpServer.setMessage(obj.toString());
//		logger.log(Level.INFO, "Direction(2) : " + direction);
	}
	
	
	/*
	 * detect direction by 4 direction ratios
	 */
	private byte getDirectionByMass(double topRatio, double rightRatio, double bottomRatio, double leftRatio){
		double lowestValueDirection  = Math.min(Math.min(Math.min(topRatio, rightRatio), bottomRatio), leftRatio);
		double weightThreshold = 4.5;
//		logger.info("lowestValue:" + lowestValueDirection);
		String l = "";
		
		if(lowestValueDirection == topRatio){
			l = "lowest:Top : (" + topRatio + "," + rightRatio + "," + bottomRatio + "," + leftRatio + ")\n";
			l +="T/L:" + topRatio/lowestValueDirection + "   R/L:" +  rightRatio/lowestValueDirection + "   B/L:" + bottomRatio/lowestValueDirection  + "   L/L:" + leftRatio/lowestValueDirection;
//			logger.info(l);
			if((rightRatio/lowestValueDirection)>weightThreshold && 
					(bottomRatio/lowestValueDirection)>weightThreshold &&
					(leftRatio/lowestValueDirection)>weightThreshold){
				return BALANCE_ZOOM_OUT;
			} else {
				if((rightRatio/lowestValueDirection)>weightThreshold) return BALANCE_RIGHT;
				else if((bottomRatio/lowestValueDirection)>weightThreshold) return BALANCE_ZOOM_OUT;
				else if((leftRatio/lowestValueDirection)>weightThreshold) return BALANCE_LEFT;
				return BALANCE_CENTER;
			}
					
		} else if(lowestValueDirection == rightRatio){
			l ="lowest:Right: (" + topRatio + "," + rightRatio + "," + bottomRatio + "," + leftRatio + ")";
			l +="T/L:" + topRatio/lowestValueDirection + "   R/L:" +  rightRatio/lowestValueDirection + "   B/L:" + bottomRatio/lowestValueDirection  + "   L/L:" + leftRatio/lowestValueDirection;
//			logger.info(l);
			if((topRatio/lowestValueDirection)>weightThreshold &&
					(bottomRatio/lowestValueDirection)>weightThreshold &&
					(leftRatio/lowestValueDirection)>weightThreshold){
				return BALANCE_LEFT;
			} else {
				if((topRatio/lowestValueDirection)>weightThreshold) return BALANCE_ZOOM_IN;
				else if((bottomRatio/lowestValueDirection)>weightThreshold) return BALANCE_ZOOM_OUT;
				else if((leftRatio/lowestValueDirection)>weightThreshold) return BALANCE_LEFT;
				return BALANCE_CENTER; 
			}
		
		} else if(lowestValueDirection == bottomRatio){
			l = "lowest:BOTTOM: (" + topRatio + "," + rightRatio + "," + bottomRatio + "," + leftRatio + ")";
			l +="T/L:" + topRatio/lowestValueDirection + "   R/L:" +  rightRatio/lowestValueDirection + "   B/L:" + bottomRatio/lowestValueDirection  + "   L/L:" + leftRatio/lowestValueDirection;
//			logger.info(l);
			
			if((topRatio/lowestValueDirection)>weightThreshold &&
				(rightRatio/lowestValueDirection)>weightThreshold &&
					(leftRatio/lowestValueDirection)>weightThreshold ){
				return BALANCE_ZOOM_IN;
			} else {
				if((topRatio/lowestValueDirection)>weightThreshold) return BALANCE_ZOOM_IN;
				else if((rightRatio/lowestValueDirection)>weightThreshold) return BALANCE_RIGHT;
				else if((leftRatio/lowestValueDirection)>weightThreshold) return BALANCE_LEFT;
				return BALANCE_CENTER;
			}
					
		} else if(lowestValueDirection == leftRatio){
			l = "lowest:Left: (" + topRatio + "," + rightRatio + "," + bottomRatio + "," + leftRatio + ")";
			l +="T/L:" + topRatio/lowestValueDirection + "   R/L:" +  rightRatio/lowestValueDirection + "   B/L:" + bottomRatio/lowestValueDirection  + "   L/L:" + leftRatio/lowestValueDirection;
//			logger.info(l);
			
			if((topRatio/lowestValueDirection)>weightThreshold &&
				(rightRatio/lowestValueDirection)>weightThreshold &&
					(bottomRatio/lowestValueDirection)>weightThreshold ){
						return BALANCE_RIGHT;
			} else {
				if((topRatio/lowestValueDirection)>weightThreshold) return BALANCE_ZOOM_IN;
				else if((rightRatio/lowestValueDirection)>weightThreshold) return BALANCE_RIGHT;
				else if((bottomRatio/lowestValueDirection)>weightThreshold) return BALANCE_ZOOM_OUT;
				return BALANCE_CENTER;
			}
		}
		return BALANCE_NOT_DETECTED;
	}
	
	public void statusReported(BBStatusEvent evt) {
	}
	
}
