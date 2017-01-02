package network.device;

import java.io.IOException;
import java.awt.geom.Point2D;
import java.awt.geom.*;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Color;

import network.datagram.L2.Frame;
import network.protocol.L2.Ethernet;

public class Hub extends Device {

    public Hub() {
        super();
        size = 8;
        ports = new Port[size];
        for (int i=0;i<size;i++) {
            ports[i] = new Port(this, i);
        }
    }

    public Hub(double x, double y) {
        super(x, y);
        size = 8;
        ports = new Port[size];
        for (int i=0;i<size;i++) {
            ports[i] = new Port(this, i);
        }
    }

    public void fetch(Frame frame, int number) {
        super.fetch(frame, number);
        SendFrameThread sfThread = new SendFrameThread();
        sfThread.setDelegate(this);
        sfThread.setFrame(frame);
        sfThread.setNumber(number);
        sfThread.start();
    }

    public void sendFrame(Frame frame, int number) {
        for (Port port : ports) {
            if (port.isConnected() && port.getNumber() != number) {
                port.send(frame);
            }
        }
    }

    class SendFrameThread extends Thread {
        private Hub delegate;
        private Frame frame;
        private int number;

        void setDelegate(Hub delegate) {
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
