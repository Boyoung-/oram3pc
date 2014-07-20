package sprout.crypto.oprf;

import org.bouncycastle.math.ec.ECPoint;

// For now this message only handles Elliptic Curve Points
// Eventually this should be more generic

public class Message {

	// If we need to transmit two points then we use v and w, otherwise we only use v.
	ECPoint v,w;
	
	public Message(ECPoint point) {
		v = point;
	}
	
	public Message(ECPoint v, ECPoint w) {
		this.v = v;
		this.w = w;
	}
	
	public ECPoint getV() {
		return v;
	}

	public ECPoint getW() {
		return w;
	}
	
	public Message setW(ECPoint w) {
	  this.w = w;
	  
	  return this;
	}
	
	public Message setV(ECPoint v) {
	  this.v = v;
	  
	  return this;
	}
	
	// Currently getResult is only used when we set a single point
	public ECPoint getResult() {
		return v;
	}
}
