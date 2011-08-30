package tj.net;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.logging.Logger;

import org.eclipse.jetty.websocket.WebSocket;

public class JogSocket implements WebSocket {

	private Outbound outbound;
	private static Set<JogSocket> socketMembers = new CopyOnWriteArraySet<JogSocket>();
	private Logger logger = Logger.getLogger(this.getClass().getName());
	
	public static void flushMessage(String message) {
		if(message == null) return;
		for(JogSocket socket : socketMembers) {
			try {
				socket.outbound.sendMessage(message);
			} catch (IOException e) {
				e.printStackTrace();
			}			
		}
	}
	
	@Override
	public void onConnect(Outbound outbound) {
		logger.info("connected");
		this.outbound = outbound;
		socketMembers.add(this);
	}

	@Override
	public void onDisconnect() {
		logger.info("disconnected");
		socketMembers.remove(this);
	}

	@Override
	public void onMessage(byte frame, String data) {
		for(JogSocket socket : socketMembers) {
			try {
				logger.info("message:" + data);
				socket.outbound.sendMessage(frame, data);
			} catch (IOException e) {
				e.printStackTrace();
			}			
		}
	}

	@Override
	public void onMessage(byte frame, byte[] data, int offset, int length) {
		for(JogSocket socket : socketMembers) {
			try {
				socket.outbound.sendMessage(frame, data, offset, length);
			} catch (IOException e) {
				e.printStackTrace();
			}			
		}
	}
	
}
