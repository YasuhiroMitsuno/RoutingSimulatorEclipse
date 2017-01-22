package network.device;

import network.datagram.L2.Frame;
import network.datagram.L3.ARPPacket;
import network.datagram.L3.ICMPDatagram;
import network.datagram.L3.Packet;
import network.protocol.L2.STP.STP;

public class Terminal extends L2Switch {
	
    Terminal(long bytes) {
    	super(bytes);
    	this.portSize = 1;
    	this.type = "Termianl";    	    	
    	stp = new STP(this);
    }
    
    Terminal(long bytes, double x, double y) {
    	super(bytes, x, y);
    	this.portSize = 1;
    	this.type = "Terminal";    	
    	stp = new STP(this);
    }
    
	protected void doForFrame(Frame frame, int fromPortNo) {
		if (frame.getLength() == 0x0806) {
			ARPPacket arpPacket = new ARPPacket(frame);
			fetch(arpPacket, fromPortNo);
		} else {
			Packet packet = new Packet(frame);
			fetch(packet, fromPortNo);
		}
	}
	
	final protected void fetch(ARPPacket arpPacket, int fromPortNo) {
		arp.receive(arpPacket);
	}
	
    protected void fetch(Packet packet, int fromPortNo) {
    	ipv4.receivedPacket(packet, fromPortNo);
    }
}
