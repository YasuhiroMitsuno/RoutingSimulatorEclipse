package network.device;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

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
    	if (!portEnable(fromPortNo)) {
    		discardFrame(frame);
    		return;
    	}
    	
    	if (debug) {
    		System.out.println(type + " " + Util.long2addr(MACAddress) + " "+ fromPortNo + "\n" + frame.description());    	
    	}
    	if (stp.getState(fromPortNo) != State.BLOCKING) {
    		table.setAddr(fromPortNo, frame.getSource());
    	}

    	
    	if (forme(frame)) {
    		if (debug) { System.out.println("DELETE"); }
    		return;
    	}

    	if (frame.getStandard() == Frame.STANDARD_ETHERNET_2) {// || (frame.getStandard() == Frame.STANDARD_IEEE_802_3 && frame.getLength() >= 0x0600)) {
    		doForFrame(frame, fromPortNo);
    	} else if (frame.getStandard() == Frame.STANDARD_IEEE_802_3) {
    		LLCU llcu = new LLCU(frame);
    		if (debug) { System.out.println(llcu.description()); }
    		if (llcu.getDsap() == LLC.STP) {
    			STPFrame stpFrame = new STPFrame(llcu.getData());
    			if (debug) { System.out.println(stpFrame.description()); }
    			stp.receivedBPDU(fromPortNo, stpFrame);
    		}
    	} else {
    		if (debug) {System.out.println("UNKNOWN"); }
    	}

     }
        
    protected void doForFrame(Frame frame, int fromPortNo) {
    	Frame rFrame = new Frame(frame.getDestination(), this.MACAddress, frame.getLength(), frame.getData());
    	if (frame.getDestination() == Util.addr2long("FF:FF:FF:FF:FF:FF")) {
    		/* broardcast */
    		if (stp.willSendFrame(fromPortNo)) {
    			fradding(frame, fromPortNo);
    		}
    	} else {
    		/* unicast */
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
    	int toPortNo = table.getCacheForMacAddr(frame.getDestination()).portNo;
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
	private ArrayList<Cache> cacheList;
	
	public Table() {
		cacheList = new ArrayList<Cache>();
	}
	
	public void setAddr(int portNo, long macAddr) {
		Cache cache = getCacheForMacAddr(macAddr);
		if (cache == null) {
			cache = new Cache(portNo, macAddr);
			cacheList.add(cache);
		} else {
			cache.portNo = portNo;
		}
		Collections.sort(cacheList);
	}
	
	public Cache getCacheForMacAddr(long macAddr) {
		for (Cache cache :cacheList) {
			if (cache.macAddr == macAddr) {
				return cache;
			}
		}
		return null;
	}
	
	public void show() {
		System.out.println("PORT NUMBER   MAC ADDRESS");
		for (Cache cache :cacheList) {
			System.out.println(cache.portNo + " " + Util.long2addr(cache.macAddr));
		}
	}
}

class Cache implements Comparable<Cache> {
	int portNo;
	long macAddr;
	
	public Cache(int portNo, long macAddr) {
		this.portNo = portNo;
		this.macAddr = macAddr;
	}
	
	@Override
	public int compareTo(Cache other) {
        if (this.portNo != other.portNo) {
        	return this.portNo - other.portNo;
        }
        if (this.macAddr > other.portNo) {
        	return 1;
        } else if (this.macAddr < other.portNo) {
        	return -1;
        } else {
        	return 0;
        }
	}
}