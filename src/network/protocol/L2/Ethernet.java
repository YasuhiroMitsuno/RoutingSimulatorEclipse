package network.protocol.L2;

import java.util.Arrays;

import network.datagram.L2.Frame;
import network.datagram.L2.Util;
import network.datagram.L3.Packet;
import network.device.Device;
import network.protocol.L3.IPv4;

public class Ethernet {
	private Device delegate;
	
    public static void read(Frame frame) {
    	System.out.println(frame.description());
    	Packet packet = new Packet(frame);
    	IPv4.read(packet);
    }
    public static Frame[] makeFragment(Frame frame, int MTU) {
    	Packet packet = new Packet(frame);
    	Packet[] packets = IPv4.makeFragment(packet, MTU);

    	Frame[] frames = new Frame[packets.length];

    	for (int i=0;i<packets.length;i++) {
    		//    System.out.println(packets[i].description());
    		Frame f = Frame.getHeader(frame);
    		f.setData(packets[i].getBytes());
    		frames[i] = f;
    	}
    	return frames;
    }
    
    public void setDelegate(Device delegate) {
    	this.delegate = delegate;
    }
    
    private void receiveDataDecapsulation(Frame frame) {
    	if (!Util.equalsAddr(frame.getDestination(), delegate.getMACAddress())) {
    		return;
    	}
    	
    	//indication(frame.getDestination(), frame.getSource(), 0, frame.getFCS());
    }
    
    private void request(byte[] destinationAddress, byte[] sourceAddress, int macServiceDataUnit,byte[] frameCheckSequence) {
    	
    }

    private void indication(byte[] destinationAddress, byte[] sourceAddress, int macServiceDataUnit,byte[] frameCheckSequence) {
    	
    }
}
