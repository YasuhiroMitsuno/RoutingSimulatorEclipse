package network.device;

import network.datagram.L3.Packet;
import network.protocol.L2.STP.STP;

public class Terminal extends L2Switch {
	
    Terminal(byte[] bytes) {
    	super(bytes);
    	this.size = 1;
    	this.type = "Termianl";    	    	
    	stp = new STP(this);
    }
    
    Terminal(byte[] bytes, double x, double y) {
    	super(bytes, x, y);
    	this.size = 1;
    	this.type = "Terminal";    	
    	stp = new STP(this);
    }
	
    protected void fetch(Packet packet, int fromPortNo) {
    	System.out.println(packet.description());
    }
}
