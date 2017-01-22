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
import network.datagram.L3.ICMPDatagram;
import network.datagram.L3.Packet;
import network.protocol.L2.Ethernet;
import network.protocol.L2.STP.STP;
import network.protocol.L2.STP.STP.State;
import network.protocol.L3.ARP;
import network.protocol.L3.ICMP;
import network.protocol.L3.IPv4;

public abstract class Device {
	protected Logger logger;
    protected Port[] ports;
    protected int portSize = 4;
    protected String name;
    protected String type;
    public Point2D position;
    public Boolean selected;
    protected Device device;
    public int MTU;
    protected long MACAddress;
	private Command command;
	public IPv4 ipv4;
	public ARP arp;
	STP stp;    

    public static void connect(Device d1, Device d2) {
        int p1 = d1.getDisconnectedPort();
        int p2 = d2.getDisconnectedPort();
        d1.getPort(p1).connect(d2.getPort(p2));
        d2.getPort(p2).connect(d1.getPort(p1));
        d1.stp.enablePort(p1 + 1);
        d2.stp.enablePort(p2 + 1);
        //d1.stp.enablePort(p1);
        //d2.stp.enablePort(p2);        
    }
    
    public Device(double x, double y) {
        this();
        position = new Point2D.Double(x, y);
    }
    public Device() {
        MTU = 1500;
        ports = new Port[portSize];
        for (int i=0;i<portSize;i++) {
            ports[i] = new Port(this, i);
        }
        selected = false;
        logger = new Logger();

    	command = new Command(this);
    	ipv4 = new IPv4(this);
    	arp = new ARP(this);
    }
    
    public Device(long addr) {
    	this();
    	MACAddress = addr;
    }
    
    public Device(long addr, double x, double y) {
    	this();
    	MACAddress = addr;
        position = new Point2D.Double(x, y);
    }
    
    public Device(String addr) {
    	this();
    	MACAddress = Util.addr2long(addr);
    }
    
    public int getPortSize() {
    	return this.portSize;
    }
    
    public String getLog() {
    	return logger.getLog();
    }
    
    public Port[] getPorts() {
    	return this.ports;
    }
    
    public long getMACAddress() {
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
	
	public void execute(String str) {
		command.execute(str);
	}
    
    /*
    public void connect(Port _port, int _index) {
        Port port = this.ports[_index];
        port.connect(_port);
    }
    */
    
    /* Add the received queue to the output queue */
    public void sendFrame(int toPortNo, Frame frame) {
    	if (stp.getState(toPortNo) != State.DISABLED && stp.getState(toPortNo) != State.BLOCKING) {
/*    		if (frame.getLength() > MTU + 18) {
    			MakeFragmentThread mfThread = new MakeFragmentThread();
    			mfThread.setDelegate(this);
    			mfThread.setFrame(frame);
    			mfThread.start();
    			return;
        	}
        	*/
        	ports[toPortNo].send(frame);        	        
    	}
        //if ((stp != null && stp.willSendFrame(portNo))) {

//        }
    }

    public void broadcastData(byte[] data, int lengthOrType, int toPortNo) {
    	Frame frame = new Frame();
    	frame.setData(data);
    	frame.setDestination("FF:FF:FF:FF:FF:FF");
    	frame.setSource(this.MACAddress);
    	frame.setLength(lengthOrType);
    	sendFrame(toPortNo, frame);
    }
    
    public void unicastData(byte[] data, int lengthOrType, long macAddr, int toPortNo) {
    	Frame frame = new Frame();
    	frame.setData(data);
    	frame.setDestination(macAddr);
    	frame.setSource(this.MACAddress);
    	frame.setLength(lengthOrType);
    	sendFrame(toPortNo, frame);
    }
        
    public Port getPort(int index) {
	return ports[index];
    }

    public int getDisconnectedPort() {
    	for (int portNo = 0; portNo < ports.length; portNo++) {
            if (!ports[portNo].isConnected()) {
                return portNo;
            }
        }
        return 0;
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
    
    public void showMAC() {
    	System.out.println(Util.long2Addr(MACAddress));
    }

    /* Take out frame from the input queue and interpret it */
    protected void fetch(Frame frame, int number) {

    }
    
    public void sendFrame(Frame frame, int fromPortNo, int toPortNo) {
    	if (stp.getState(fromPortNo) == State.FORWARDING && stp.getState(toPortNo) == State.FORWARDING && fromPortNo != toPortNo) {
    		ports[toPortNo].send(frame);
    	}
    }

    public void sendFrame(Frame frame, int number) {
        for (Port port : ports) {
            if (port.isConnected() && port.getNumber() != number && stp.willSendFrame(port.getNumber())) {
                port.send(frame);
            }
        }
    }
    
    class FraddingThread extends Thread {
        private Device delegate;
        private Frame frame;
        private int fromPortNo;

        void setDelegate(Device delegate) {
            this.delegate = delegate;
        }

        void setFrame(Frame frame) {
            this.frame = frame;
        }

        void setFromPortNo(int fromPortNo) {
            this.fromPortNo = fromPortNo;
        }

        public void run() {
            if (frame.getLength() > MTU + 18) {
                //                System.out.println("MAKE FRAGMENT");
                for (Frame frame : Ethernet.makeFragment(frame, delegate.MTU)) {
                    delegate.sendFrame(frame, fromPortNo);
                }
            } else {
                for (int portNo=0; portNo<ports.length; portNo++) {
                	delegate.sendFrame(frame, fromPortNo, portNo);
                }
            }
        }
    }
    public void showMacTable() {
    	
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
                delegate.sendFrame(0, f);
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
    	g.drawString(this.type, 0, -10);
    	

    	if (stp.rootBridge()) {
    		g.drawString("root", 0, -20);
    	}	        
        for (Port port : ports) {
            port.draw(g2);
            aL.translate(0, 15);
            g2.setTransform(aL);            
        }

        
        /* ワールド座標に戻す */
        g2.setTransform(aG);
        g2.setColor(Color.BLACK);
    }
    
    public void setIP(int portNo, int addr, int mask) {
		ipv4.setIP(portNo, addr, mask);
	}
	
	public void ping(int addr) {
		ipv4.ping(addr);
	}
	
	public void setRoute(int addr, int mask, int next) {
		ipv4.setRoute(addr, mask, next);
	}
	
	public void showIpAddr() {
		ipv4.showIpAddr();
	}
	
	public void showIpRoute() {
		ipv4.showIpRoute();
	}
	
	public void showInterface() {
		System.out.println(Util.long2Addr(MACAddress));
	}
}
