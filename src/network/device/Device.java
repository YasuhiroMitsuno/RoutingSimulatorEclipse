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
import network.datagram.L3.Packet;
import network.protocol.L2.Ethernet;
import network.protocol.L2.STP.STP;
import network.protocol.L2.STP.STP.State;
import network.protocol.L3.IPv4;

public abstract class Device {
	protected PortData[] portInfo;
	protected RouteInfo routeInfo;
	protected Logger logger;
    protected Port[] ports;
    protected int size = 4;
    protected String name;
    protected String type;
    public Point2D position;
    public Boolean selected;
    protected Device device;
    public int MTU;
    protected byte[] MACAddress;
	private Command command;
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
        ports = new Port[size];
        for (int i=0;i<size;i++) {
            ports[i] = new Port(this, i);
        }
        selected = false;
        logger = new Logger();
		portInfo = new PortData[this.size];
		for (int i=0;i<this.size;i++) {
			portInfo[i] = new PortData();
		}
		routeInfo = new RouteInfo();
    	command = new Command(this);
    }
    
    public Device(byte[] bytes) {
    	this();
    	MACAddress = bytes;
    }
    
    public Device(byte[] bytes, double x, double y) {
    	this();
    	MACAddress = bytes;
        position = new Point2D.Double(x, y);
    }
    
    public Device(String addr) {
    	this();
    	MACAddress = Util.addr2Bytes(addr);
    }
    
    public String getLog() {
    	return logger.getLog();
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
			frame.setDestination("FF:FF:FF:FF:FF:FF");
			frame.setSource(this.MACAddress);
    		if (frame.getLength() > MTU + 18) {
    			MakeFragmentThread mfThread = new MakeFragmentThread();
    			mfThread.setDelegate(this);
    			mfThread.setFrame(frame);
    			mfThread.start();
    			return;
        	}
        	ports[toPortNo].send(frame);        	        
    	}
        //if ((stp != null && stp.willSendFrame(portNo))) {

//        }
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
    
	public void setIP(int portNo, byte[] addr, byte[] mask) {
		System.arraycopy(addr, 0, portInfo[portNo].addr, 0, 4);
		System.arraycopy(mask, 0, portInfo[portNo].mask, 0, 4);		
	}
	
	protected int nextPort(byte[] addr) {
		int portNo;
		portNo = subnetPort(addr);
		if (portNo == -1) {
			portNo = subnetPort(routeInfo.getNextHop(addr));
		}
		return portNo;
	}
	
	protected int subnetPort(byte[] addr) {
		for (int portNo=0;portNo<size;portNo++) {
			if (IPv4.isSameNetwork(addr, portInfo[portNo].addr, portInfo[portNo].mask)) {
				return portNo;
			}
		}
		return -1;
	}
	
	public void ping(byte[] addr) {
		int portNo = nextPort(addr);
		System.out.println(portNo);
		Packet rPacket = new Packet();
		rPacket.setSource(portInfo[portNo].addr);
		rPacket.setDestination(addr);
		rPacket.setProtocol(1);
		rPacket.setTTL(255);
		rPacket.setData(new byte[2]);
		Frame frame = new Frame();
		frame.setData(rPacket.getBytes());
		this.sendFrame(portNo, frame);
	}
	
	public void setRoute(byte[] addr, byte[] mask, byte[] next) {
		routeInfo.setRoute(addr, mask, next);
	}
}

class PortData {
	int 	portId;
//	State 	state;
	byte[] 	addr = new byte[4];
	byte[] 	mask = new byte[4];
}

class RouteInfo {
	final static int ROUTE_SIZE = 100;
	private int count = 0;
	private RouteData[] routeData = new RouteData[ROUTE_SIZE];
	
	public RouteInfo() {
		for (int i=0;i<ROUTE_SIZE;i++) {
			routeData[i] = new RouteData();
		}
	}
	
	public void setRoute(byte[] addr, byte[] mask, byte[] next) {
		routeData[count].addr = addr;
		routeData[count].mask = mask;
		routeData[count].next = next;
		count++;
		sort();
	}
	
	public byte[] getNextHop(byte[] addr) {
		byte[] next = new byte[4];
		for (int i=0;i<count;i++) {
			if (IPv4.isSameNetwork(addr, routeData[i].addr, routeData[i].mask)) {
				System.arraycopy(routeData[i].next, 0, next, 0, 4);		
			}
		}
		return next;
	}
	
	private boolean isSmall(RouteData routeData1, RouteData routeData2) {
		boolean small = true;
		for (int i=0;i<4;i++) {
			if (routeData1.mask[i] > routeData2.mask[i]) small = false;
		}
		return small;
	}
	
	private void sort() {
		RouteData tmp;
		for (int i=0;i<count;i++) {
			for (int j=0;j<count-i;j++) {
				if (!isSmall(routeData[i], routeData[j])) {
					tmp = routeData[i];
					routeData[i] = routeData[j];
					routeData[j] = tmp;
				}
			}
		}
	}
}

class RouteData {
	byte[] addr = new byte[4];
	byte[] mask = new byte[4];
	byte[] next = new byte[4];
}
