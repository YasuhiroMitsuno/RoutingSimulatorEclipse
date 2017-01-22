package network.device;

import network.datagram.L2.Frame;
import network.datagram.L3.Util;
import network.datagram.L3.ARPPacket;
import network.datagram.L3.ICMPDatagram;
import network.datagram.L3.Packet;
import network.protocol.L2.STP.STP;
import network.protocol.L3.ICMP;
import network.protocol.L3.IPv4;

public class L3Switch extends L2Switch {
	
    L3Switch(long bytes) {
    	super(bytes);
    	this.type = "L3";
    	stp.setEnabled(false);
    	ipv4.setEnableForward(true);
    }
	
	L3Switch(long bytes, double x, double y) {
		super(bytes, x, y);
		this.type = "L3";
		stp.setEnabled(false);
		ipv4.setEnableForward(true);
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
	
	final protected void fetch(Packet packet, int fromPortNo) {
		ipv4.receivedPacket(packet, fromPortNo);
		/*
		packet.setTTL(packet.getTTL() - 1);

		if (Util.equalsAddr(portInfo[fromPortNo].addr ,packet.getDestination())) {

			return;
		}
		
		if (packet.getTTL() == 0) {
			Packet rPacket = new Packet();
			rPacket.setDestination(packet.getSource());
			rPacket.setSource(portInfo[fromPortNo].addr);
			rPacket.setProtocol(1);
			rPacket.setTTL(255);
			ICMPD ricmpd = new ICMPD();
			ricmpd.setType((byte)0x0B);
			ricmpd.setCode((byte)0x00);
			rPacket.setData(ricmpd.getBytes());
			Frame frame = new Frame();
			frame.setData(rPacket.getBytes());
			this.sendFrame(fromPortNo, frame);
			return;
		}
		System.out.println(packet.description());
		int toPortNo = nextPort(packet.getDestination());
		if (toPortNo != -1) {
			System.out.println(toPortNo + "AAAAA");
			Frame frame = new Frame();
			frame.setData(packet.getBytes());
			this.sendFrame(toPortNo, frame);
		} else {
			System.out.println("Destination ERROR");
		}
		*/
	}
	

}
