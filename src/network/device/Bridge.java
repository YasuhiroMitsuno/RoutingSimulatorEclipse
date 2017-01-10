package network.device;

import java.io.IOException;
import java.awt.geom.Point2D;
import java.awt.geom.*;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Color;

import network.datagram.L2.Frame;
import network.protocol.L2.Ethernet;
import network.protocol.L2.STP.STP;

public class Bridge extends Device {
	
	Bridge() {
        super();
        size = 8;
        ports = new Port[size];
        for (int i=0;i<size;i++) {
            ports[i] = new Port(this, i);
        }
    }

    Bridge(double x, double y) {
        super(x, y);
        size = 8;
        ports = new Port[size];
        for (int i=0;i<size;i++) {
            ports[i] = new Port(this, i);
        }
    }

    Bridge(byte[] bytes) {
    	super(bytes);
    	stp = new STP(this);
    	stp.start();
    }
    
    Bridge(byte[] bytes, double x, double y) {
    	super(bytes, x, y);
    	stp = new STP(this);
    	stp.start();
    }
    
    Bridge(String addr) {
    	super(addr);
    }
    
    public void fetch(Frame frame, int number) {
    	stp.receivedSTPFrame(number, frame);

    	if (stp.willSendFrame(number)) {
	    	super.fetch(frame, number);
	        SendFrameThread sfThread = new SendFrameThread();
	        sfThread.setDelegate(this);
	        sfThread.setFrame(frame);
	        sfThread.setNumber(number);
	        sfThread.start();
    	}
     }

    public void sendFrame(Frame frame, int number) {
        for (Port port : ports) {
            if (port.isConnected() && port.getNumber() != number && stp.willSendFrame(port.getNumber())) {
                port.send(frame);
            }
        }
    }
    
    public void sendFrame(Frame frame) {
        for (Port port : ports) {
            if (port.isConnected()) {
                port.send(frame);
            }
        }
    }

    class SendFrameThread extends Thread {
        private Bridge delegate;
        private Frame frame;
        private int number;

        void setDelegate(Bridge delegate) {
            this.delegate = delegate;
        }

        void setFrame(Frame frame) {
            this.frame = frame;
        }

        void setNumber(int number) {
            this.number = number;
        }

        public void run() {
            if (frame.getLength() > MTU + 18) {
                //                System.out.println("MAKE FRAGMENT");
                for (Frame frame : Ethernet.makeFragment(frame, delegate.MTU)) {
                    delegate.sendFrame(frame, number);
                }
            } else {
                delegate.sendFrame(frame, number);
            }
        }
    }
}
