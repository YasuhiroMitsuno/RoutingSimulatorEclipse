package network.device;

import java.io.IOException;
import java.util.Arrays;

import org.omg.CORBA.portable.Delegate;

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
	private Table table;
	
	L2Switch() {
        super();
        portSize = 8;
        ports = new Port[portSize];
        for (int i=0;i<portSize;i++) {
            ports[i] = new Port(this, i);
        }
        table = new Table();
    }

    L2Switch(double x, double y) {
        super(x, y);
        portSize = 8;
        ports = new Port[portSize];
        for (int i=0;i<portSize;i++) {
            ports[i] = new Port(this, i);
        }
        table = new Table();
    }

    L2Switch(long addr) {
    	super(addr);
    	this.type = "L2";    	    	
    	stp = new STP(this);
    	table = new Table();
    }
    
    L2Switch(long addr, double x, double y) {
    	super(addr, x, y);
    	this.type = "L2";    	
    	stp = new STP(this);
    	table = new Table();
    }
    
    L2Switch(String addr) {
    	super(addr);
    	table = new Table();
    }
    
    protected boolean portEnable(int portNo) {
    	if (stp.isEnabled() && stp.getState(portNo) == State.DISABLED) {
    		return false;
    	}
    	return true;
    }
    
    protected void discardFrame(Frame frame) {
    	System.out.println("DISCARD FRAME");
    	System.out.println(frame.description());
    }
    
    protected boolean forme(Frame frame) {
    	return false;
    }
    
    final public void fetch(Frame frame, int fromPortNo) {
    	System.out.println(type + " " + Util.long2addr(MACAddress) + " "+ fromPortNo + "\n" + frame.description());    	
    	table.setAddr(fromPortNo, frame.getSource());
    	if (!portEnable(fromPortNo)) {
    		discardFrame(frame);
    		return;
    	}
    	
    	if (forme(frame)) {
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
    	Frame rFrame = new Frame(frame.getDestination(), this.MACAddress, frame.getLength(), frame.getData());
    	if (frame.getDestination() == Util.addr2long("FF:FF:FF:FF:FF:FF")) {
    		/* broardcast */
    		System.out.println("Broad");
    		if (stp.willSendFrame(fromPortNo)) {
    			fradding(frame, fromPortNo);
    		}
    	} else {
    		/* unicast */
    		System.out.println("uni");
    		transmit(frame, fromPortNo);
    	}
    }
    
    private void fradding(Frame frame, int fromPortNo) {
        FraddingThread fraddingThread = new FraddingThread();
        fraddingThread.setDelegate(this);
        fraddingThread.setFrame(frame);
        fraddingThread.setFromPortNo(fromPortNo);
        fraddingThread.start();
    }
    
    private void transmit(Frame frame, int fromPortNo) {
    	int toPortNo = table.getPortForMacAddr(frame.getDestination());
    	if (toPortNo == -1) {
    		System.out.println("FFAFFAFAF");
    		return;
    	}
    	sendFrame(frame, fromPortNo, toPortNo);
    }
       
    public void sendFrame(Frame frame) {
        for (Port port : ports) {
            if (port.isConnected()) {
                port.send(frame);
            }
        }
    }
    
    public void showMacTable() {
    	table.show();
    }
}

class Table {
	final static int TABLE_SIZE = 100;
	private int count = 0;
	private Cache[] cache = new Cache[TABLE_SIZE];
	
	public Table() {
		for (int i=0;i<TABLE_SIZE;i++) {
			cache[i] = new Cache();
		}
	}
	
	public void setAddr(int portNo, long macAddr) {
		int num = getNumberForAddr(macAddr);
		if (num == -1) {
			cache[count].portNo = portNo;
			cache[count].macAddr = macAddr;
			count++;
			//Arrays.sort(cache);
		} else {
			cache[num].portNo = portNo;
		}
	}
	
	public int getPortForMacAddr(long macAddr) {
		int num = getNumberForAddr(macAddr);
		if (num == -1) {
			return -1;
		}
		return cache[num].portNo;
	}
	
	private int getNumberForAddr(long macAddr) {
		for (int i=0;i<count;i++) {
			if (cache[i].macAddr == macAddr) {
				return i;
			}
		}
		return -1;
	}
	
	public void show() {
		for (int i=0;i<count;i++) {
			System.out.println(cache[i].portNo + " " + Util.long2addr(cache[i].macAddr));
		}
	}
}

class Cache implements Comparable<Cache> {
	int portNo;
	long macAddr;
	
	@Override
	public int compareTo(Cache other) {
        return this.portNo - other.portNo;
	}
}