package tj.socket;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import org.eclipse.jetty.websocket.WebSocket;

public class JogSocket implements WebSocket {

	private Outbound outbound;
	private static Set<JogSocket> socketMembers = new CopyOnWriteArraySet<JogSocket>();
	
	public static void flushMessage(String message) {
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
		System.out.println("connect!!!");
		this.outbound = outbound;
		socketMembers.add(this);
	}

	@Override
	public void onDisconnect() {
		System.out.println("disconnect!!!");
		socketMembers.remove(this);
	}

	@Override
	public void onMessage(byte frame, String data) {
		for(JogSocket socket : socketMembers) {
			try {
				System.out.println("message:" + data);
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
