package network.device;

import java.io.IOException;
import java.awt.geom.Point2D;
import java.awt.geom.*;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Color;

import network.datagram.L2.Frame;
import network.datagram.L2.LLCU;
import network.datagram.L2.STPFrame;
import network.datagram.L3.ARPPacket;
import network.datagram.L3.Packet;
import network.datagram.L3.Util;
import network.protocol.L2.STP.LLC;
import network.protocol.L2.STP.STP;
import network.protocol.L2.STP.STP.State;

public class L2Switch extends Device {
	
	L2Switch() {
        super();
        portSize = 8;
        ports = new Port[portSize];
        for (int i=0;i<portSize;i++) {
            ports[i] = new Port(this, i);
        }
    }

    L2Switch(double x, double y) {
        super(x, y);
        portSize = 8;
        ports = new Port[portSize];
        for (int i=0;i<portSize;i++) {
            ports[i] = new Port(this, i);
        }
    }

    L2Switch(long addr) {
    	super(addr);
    	this.type = "L2";    	    	
    	stp = new STP(this);
    }
    
    L2Switch(long addr, double x, double y) {
    	super(addr, x, y);
    	this.type = "L2";    	
    	stp = new STP(this);
    }
    
    L2Switch(String addr) {
    	super(addr);
    }
    
    final public void fetch(Frame frame, int fromPortNo) {
    	if (stp.getState(fromPortNo) == State.DISABLED) {
    		return;
    	}
    	
    	System.out.println(frame.description());
    	
    	if (frame.getDestination() != MACAddress && 
    		frame.getDestination() != Util.addr2long("FF:FF:FF:FF:FF:FF")) {
    		System.out.println(String.format("%012x", frame.getDestination()) + " " + String.format("%012x", Util.addr2long("FF:FF:FF:FF:FF:FF")));
    		System.out.println("DELETE");
    		return;
    	}

    	if (frame.getStandard() == Frame.STANDARD_ETHERNET_2 || (frame.getStandard() == Frame.STANDARD_IEEE_802_3 && frame.getLength() >= 0x0600)) {
    		doForFrame(frame, fromPortNo);
    	} else if (frame.getStandard() == Frame.STANDARD_IEEE_802_3) {
    		LLCU llcu = new LLCU(frame);
    		System.out.println(llcu.description());
    		if (llcu.getDsap() == LLC.STP) {
    			STPFrame stpFrame = new STPFrame(llcu.getData());
    			System.out.println(stpFrame.description());
    			stp.receivedBPDU(fromPortNo, stpFrame);
    		}
    	} else {
    		System.out.println("UNKNOWN");
    	}

     }
        
    protected void doForFrame(Frame frame, int fromPortNo) {
    	Frame rFrame = new Frame();
//    	rFrame.setDestination("FF:FF:FF:FF:FF:FF");
    	rFrame.setSource(frame.getDestination());
    	rFrame.setSource(this.MACAddress);
    	rFrame.setData(frame.getData());
    	if (stp.willSendFrame(fromPortNo)) {
    		fradding(frame, fromPortNo);
    	}	
    }
    
    private void fradding(Frame frame, int fromPortNo) {
        FraddingThread fraddingThread = new FraddingThread();
        fraddingThread.setDelegate(this);
        fraddingThread.setFrame(frame);
        fraddingThread.setFromPortNo(fromPortNo);
        fraddingThread.start();
    }
       
    public void sendFrame(Frame frame) {
        for (Port port : ports) {
            if (port.isConnected()) {
                port.send(frame);
            }
        }
    }
}
