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
import network.datagram.L3.Packet;
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
    
    public void fetch(Frame frame, int fromPortNo) {
    	if (stp.getState(fromPortNo) == State.DISABLED) {
    		return;
    	}

    	if (frame.getLength() < 1500) {
    		LLCU llcu = new LLCU(frame);
    		if (llcu.getDsap() == LLC.STP) {
    			STPFrame stpFrame = new STPFrame(llcu.getData());
    			//logger.log(frame.description());
    			//logger.log(llcu.description());
    			//logger.log(stpFrame.description());
    			stp.receivedBPDU(fromPortNo, stpFrame);
    		} else {
        		Packet packet = new Packet(frame);
        		fetch(packet, fromPortNo);
    		}
    	} else {
    		Packet packet = new Packet(frame);
    		fetch(packet, fromPortNo);
    	}
    	
    	
     }
    
    protected void fetch(Packet packet, int fromPortNo) {
    	Frame frame = new Frame();
    	frame.setDestination("FF:FF:FF:FF:FF:FF");
    	frame.setSource(this.MACAddress);
    	frame.setData(packet.getBytes());
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
