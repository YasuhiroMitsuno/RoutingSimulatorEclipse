package network.protocol.L3;

import network.datagram.L2.Frame;
import network.datagram.L3.ARPPacket;
import network.device.Device;

public class ARP {
	private Device delegate;
	
	public ARP(Device delegate) {
		this.delegate = delegate;
	}
	
	public void receive(Frame frame) {
		ARPPacket arpPacket = new ARPPacket(frame);
		
		if (arpPacket.getOperation() == 0x01) {
			int portNo = delegate.ipv4.getPortForAddress(arpPacket.getDestProtocolAddress());
			if (portNo != -1) {
				reply(arpPacket);
			}
		}
	}
	
	public void reply(ARPPacket arpPacket) {
		ARPPacket rArpPacket = new ARPPacket();
		arpPacket.setSourceHardwareAddress(delegate.getMACAddress());
		arpPacket.setSourceProtocolAddress(arpPacket.getDestProtocolAddress());
		arpPacket.setSourceHardwareAddress(delegate.getMACAddress());
		
		
	}
}
