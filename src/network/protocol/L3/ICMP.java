package network.protocol.L3;

import java.util.Date;
import java.util.Formatter;

import network.datagram.L2.Frame;
import network.datagram.L3.ICMPD;
import network.datagram.L3.Packet;
import network.datagram.L3.Util;
import network.device.Device;


public class ICMP {
	private IPv4 delegate;
	private long t;
	
	public ICMP(IPv4 delegate) {
		this.delegate = delegate;
	}
	
	public void receive(Packet packet) {
		System.out.println("AAAA");
		ICMPD icmpd = new ICMPD(packet);
		Formatter fm = new Formatter();
		if (icmpd.getType() == 0x00 && icmpd.getCode() == 0x00) {
			long time = System.currentTimeMillis() - t;
	        fm.format("Reply from %s: bytes=%d time=%dms TTL=%d", packet.getSourceString(), icmpd.getData().length, time, packet.getTTL());
	        System.out.println(fm);
		}
		if (icmpd.getType() == 0x08 && icmpd.getCode() == 0x00) {
			ICMPD rIcmpd = new ICMPD();
			rIcmpd.setType((byte)0x00);
			rIcmpd.setCode((byte)0x00);
			delegate.sendData(rIcmpd.getBytes(), null, packet.getSource(), 1, 2);
			return;
		}
	}
	
	private void reply() {
		//delegate.sendFrame(frame);
	}
	
	public void ping(byte[] addr) {
		ICMPD icmpd = new ICMPD();
		icmpd.setType((byte)0x08);
		icmpd.setCode((byte)0x00);
		
		t = System.currentTimeMillis();
		delegate.sendData(icmpd.getBytes(), null, addr, 1, 1);
	}
}
