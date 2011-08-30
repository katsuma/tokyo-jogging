package tj.net;

import java.util.Vector;

import org.eclipse.jetty.continuation.Continuation;

public class JogContinuation {
	private static Vector<Continuation> continuations = new Vector<Continuation>();
	
	public Vector<Continuation> getContinuations() {
		return continuations;
	}
	
	public static void add(Continuation continuation) {
		continuations.add(continuation);
	}
	
	public static void flushMessage() {
		for(Continuation c : continuations){
			c.resume();
		}
		continuations.removeAllElements();		
	}
}
