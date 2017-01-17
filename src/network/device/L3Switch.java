package network.device;

import network.datagram.L2.Frame;
import network.datagram.L3.Util;
import network.datagram.L3.Packet;
import network.protocol.L2.STP.STP;
import network.protocol.L3.IPv4;

public class L3Switch extends L2Switch {
	
    L3Switch(byte[] bytes) {
    	super(bytes);
    	this.type = "L3";
    	stp.setEnabled(false);
    }
	
	L3Switch(byte[] bytes, double x, double y) {
		super(bytes, x, y);
		this.type = "L3";
		stp.setEnabled(false);
	}
	
	final protected void fetch(Packet packet, int fromPortNo) {
		packet.setTTL(packet.getTTL() - 1);
		if (packet.getTTL() == 0) {
			Packet rPacket = new Packet();
			rPacket.setDestination(packet.getSource());
			rPacket.setSource(portInfo[fromPortNo].addr);
			rPacket.setProtocol(1);
			rPacket.setTTL(255);
			rPacket.setData(new byte[2]);
			Frame frame = new Frame();
			frame.setData(rPacket.getBytes());
			this.sendFrame(fromPortNo, frame);
			return;
		}
		//System.out.println(packet.description());
		int toPortNo = nextPort(packet.getDestination());
		if (toPortNo != -1) {
			Frame frame = new Frame();
			frame.setData(packet.getBytes());
			this.sendFrame(toPortNo, frame);
		}
	}
	

}
