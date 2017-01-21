package network.protocol.L3;

import java.util.Date;
import java.util.Formatter;

import network.datagram.L2.Frame;
import network.datagram.L3.ICMPDatagram;
import network.datagram.L3.Packet;
import network.datagram.L3.Util;
import network.device.Device;


public class ICMP {
	public static byte ICMP_TYPE_ECHO_REPLY = 0x00;
	public static byte ICMP_TYPE_DESTINATION_UNREACHABLE = 0x03;
	public static byte ICMP_TYPE_SOURCE_QUENCH = 0x04;
	public static byte ICMP_TYPE_REDIRECT = 0x05;
	public static byte ICMP_TYPE_ECHO_REQUEST = 0x08;
	public static byte ICMP_TYPE_ROUTER_ADVERTISEMENT = 0x09;
	public static byte ICMP_TYPE_ROUTER_SOLICITATION = 0x10;
	public static byte ICMP_TYPE_TIME_EXCEEDED = 0x11;
	public static byte ICMP_TYPE_PARAMETER_PROBLEM = 0x12;
	public static byte ICMP_TYPE_TIMISTAMP = 0x13;
	public static byte ICMP_TYPE_TIMISTAMP_REPLY = 0x14;
	public static byte ICMP_TYPE_INFORMATION_REQUEST = 0x15;
	public static byte ICMP_TYPE_INFORMATION_REPLY = 0x16;
	public static byte ICMP_TYPE_ADDRESS_MASK_REQUEST = 0x17;
	public static byte ICMP_TYPE_ADDRESS_MASK_REPLY = 0x18;
	private IPv4 delegate;
	private long t;
	
	public ICMP(IPv4 delegate) {
		this.delegate = delegate;
	}
	
	public void receive(Packet packet) {
		System.out.println("receive ICMP");
		ICMPDatagram icmpd = new ICMPDatagram(packet);
		Formatter fm = new Formatter();
		if (delegate.getPortForAddress(packet.getDestination()) != -1) {
			if (icmpd.getType() == ICMP_TYPE_ECHO_REPLY && icmpd.getCode() == 0x00) {
				
				long time = System.currentTimeMillis() - t;
				fm.format("Reply from %s: bytes=%d time=%dms TTL=%d", packet.getSourceString(), icmpd.getData().length, time, packet.getTTL());
		        System.out.println(fm);
			}
			if (icmpd.getType() == ICMP_TYPE_ECHO_REQUEST && icmpd.getCode() == 0x00) {
					ICMPDatagram replyICMPDatagram = new ICMPDatagram();
					replyICMPDatagram.setType((byte)0x00);
					replyICMPDatagram.setCode((byte)0x00);
					int destination = packet.getSource();
					int source = delegate.portInfo[delegate.nextPort(destination)].addr;			
					delegate.sendData(replyICMPDatagram.getBytes(), source, destination, 1, 2);
					return;
			}
			if (icmpd.getType() == ICMP_TYPE_TIME_EXCEEDED) {
				fm.format("Data from %s: TIMEEXCEED", packet.getSourceString());
				System.out.println(fm);
			}
		} else {
			int ttl = packet.getTTL() - 1;
			if (ttl == 0) {
				ICMPDatagram replyICMPDatagram = new ICMPDatagram();
				replyICMPDatagram.setType(ICMP_TYPE_TIME_EXCEEDED);
				replyICMPDatagram.setCode((byte)0x00);
				int destination = packet.getSource();
				int source = delegate.portInfo[delegate.nextPort(destination)].addr;			
				delegate.sendData(replyICMPDatagram.getBytes(), source, destination, 1, 100);
			} else {
				/* forward */
				Packet rPacket = new Packet(packet);
				delegate.forwardPacket(rPacket);
				return;
			}
		}
	}
	
	private void reply() {
		//delegate.sendFrame(frame);
	}
	
	public void ping(int destination) {
		ICMPDatagram icmpd = new ICMPDatagram();
		icmpd.setType((byte)0x08);
		icmpd.setCode((byte)0x00);
		
		t = System.currentTimeMillis();
		int source = delegate.portInfo[delegate.nextPort(destination)].addr;
		delegate.sendData(icmpd.getBytes(), source, destination, 1, 2);
	}
}
