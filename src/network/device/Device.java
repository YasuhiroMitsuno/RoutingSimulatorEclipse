package network.device;

import java.io.IOException;

//import java.awt.Point;
//import java.awt.Graphics2D;
import java.awt.geom.*;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Color;

import network.datagram.L2.Frame;
import network.datagram.L2.Util;
import network.protocol.L2.Ethernet;

public abstract class Device {
    protected Port[] ports;
    protected int size = 2;
    protected String name;
    public Point2D position;
    public Boolean selected;
    protected Device device;
    protected int MTU;
    protected byte[] MACAddress;

    public static void connect(Device d1, Device d2) {
        Port p1 = d1.getDisconnectedPort();
        Port p2 = d2.getDisconnectedPort();
        p1.connect(p2);
        p2.connect(p1);
    }
    
    public Device(double x, double y) {
        this();
        position = new Point2D.Double(x, y);
    }
    public Device() {
        MTU = 1500;
        ports = new Port[size];
        for (int i=0;i<size;i++) {
            ports[i] = new Port(this);
        }
        selected = false;
    }
    
    public Device(byte[] bytes) {
    	this();
    	MACAddress = bytes;
    }
    
    public Device(String addr) {
    	this();
    	MACAddress = Util.addr2Bytes(addr);
    }
    
    public Port[] getPorts() {
    	return this.ports;
    }
    
    public byte[] getMACAddress() {
    	return MACAddress;
    }
    
    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }
    
    public void broadCastFrame(Frame frame) {
    	for (Port port: ports) {
    		port.send(frame);
    	}
    }

    /*
    public void connect(Port _port, int _index) {
        Port port = this.ports[_index];
        port.connect(_port);
    }
    */
    
    /* Add the received queue to the output queue */
    public void sendFrame(int portNo, Frame frame) {
        if (frame.getLength() > MTU + 18) {
            MakeFragmentThread mfThread = new MakeFragmentThread();
            mfThread.setDelegate(this);
            mfThread.setFrame(frame);
            mfThread.start();
            return;
        }
        ports[portNo-1].send(frame);
    }

    public Port getPort(int index) {
	return ports[index];
    }

    public Port getDisconnectedPort() {
        for (Port port : ports) {
            if (!port.isConnected()) {
                return port;
            }
        }
        return null;
    }

    public int getNumber() {
        int c = 0;
        for (Port port : ports) {
            if (!port.isConnected()) {
                break;
            }
            c++;
        }
        return c;
    }

    public int getSendingCount() {
        return ports[0].getSendingCount();
    }

    /* Take out frame from the input queue and interpret it */
    protected void fetch(Frame frame, int number) {

    }

    class MakeFragmentThread extends Thread {
        private Device delegate;
        private Frame frame;
        
        void setDelegate(Device delegate) {
            this.delegate = delegate;
        }
        
        void setFrame(Frame frame) {
            this.frame = frame;
        }
        
        public void run() {
            //            System.out.println("MAKE FRAGMENT");
            for (Frame f : Ethernet.makeFragment(frame, delegate.MTU)) {
                delegate.sendFrame(f);
            }
        }
    }

    public void draw(Graphics g, double scale, double zoom, boolean drawOval) {
        Graphics2D g2 = (Graphics2D)g;
        /* ワールド座標 */
        AffineTransform aG = g2.getTransform();
        /* ローカル座標 */
        AffineTransform aL = new AffineTransform(aG);

        /* 上下反転ローカル変換 */
        aL.scale(1.0, -1.0);

        aL.translate((int)(position.getX() *scale), -(int)(position.getY()*scale));
        g2.setTransform(aL);

     
        for (Port port : ports) {
            port.draw(g2);
            aL.translate(0, 15);
            g2.setTransform(aL);            
        }
        
        /* ワールド座標に戻す */
        g2.setTransform(aG);
        g2.setColor(Color.BLACK);
    }
}
