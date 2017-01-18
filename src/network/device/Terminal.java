package network.device;

import network.datagram.L3.ICMPD;
import network.datagram.L3.Packet;
import network.protocol.L2.STP.STP;

public class Terminal extends L2Switch {
	
    Terminal(byte[] bytes) {
    	super(bytes);
    	this.portSize = 1;
    	this.type = "Termianl";    	    	
    	stp = new STP(this);
    }
    
    Terminal(byte[] bytes, double x, double y) {
    	super(bytes, x, y);
    	this.portSize = 1;
    	this.type = "Terminal";    	
    	stp = new STP(this);
    }
	
    protected void fetch(Packet packet, int fromPortNo) {
    	ipv4.receivedPacket(packet, fromPortNo);
    }
}
