package network.protocol.L3;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

import network.datagram.L2.Frame;
import network.datagram.L3.ARPPacket;
import network.datagram.L3.Packet;
import network.datagram.L3.Util;
import network.device.Device;
import network.protocol.L3.RouteInfo.RouteData;

public class ARP {
	public final static short ARP_OPERATION_REQUEST = (short)0x0001;
	public final static short ARP_OPERATION_REPLY = (short)0x0002;
	private Device delegate;
	private Table table;
	
	public ARP(Device delegate) {
		this.delegate = delegate;
		table = new Table();
	}
	
	public void receive(ARPPacket arpPacket) {
		System.out.println(arpPacket.description());
		
		switch (arpPacket.getOperation()) {
		case ARP_OPERATION_REQUEST:
			/* ARP request */
			getRequest(arpPacket);
			break;
		case ARP_OPERATION_REPLY:
			/* ARP reply */
			getReply(arpPacket);
			break;
		}

	}
	
	public void arp(int destination) {
		int portNo = delegate.ipv4.sameNetworkPort(destination);
		if (portNo == -1) {
			System.out.println("Network Error.");
			return;
		}
		
		ARPPacket arpPacket = new ARPPacket();
		arpPacket.setSourceHardwareAddress(delegate.getMACAddress());
		arpPacket.setSourceProtocolAddress(delegate.ipv4.portInfo[portNo].addr);
		arpPacket.setDestHardwareAddress(0);
		arpPacket.setDestProtocolAddress(destination);
		arpPacket.setOperation(ARP_OPERATION_REQUEST);
		sendRequest(arpPacket, portNo);
	}
	
	public long getMacAddrForIpAddr(int ipAddr) {
		Cache cache = table.getCacheForIpAddr(ipAddr);
		if (cache == null) {
			return -1;
		}
		return cache.macAddr;
	}
	
	private void sendRequest(ARPPacket arpPacket, int portNo) {
		delegate.broadcastData(arpPacket.getBytes(), 0x0806, portNo);
	}
	
	private void sendReply(ARPPacket arpPacket, long macAddr, int toPortNo) {
		delegate.unicastData(arpPacket.getBytes(), 0x0806, macAddr, toPortNo);
	}
	
	private void getRequest(ARPPacket arpPacket) {
		int portNo = delegate.ipv4.getPortForAddress(arpPacket.getDestProtocolAddress());
		System.out.println(portNo + " AAAAAAAAA");
		if (portNo == -1) {
			return;
		}
		table.setAddr(arpPacket.getSourceProtocolAddress(), arpPacket.getSourceHardwareAddress());
		
		ARPPacket rArpPacket = new ARPPacket();
		rArpPacket.setSourceHardwareAddress(delegate.getMACAddress());
		rArpPacket.setSourceProtocolAddress(arpPacket.getDestProtocolAddress());
		rArpPacket.setDestHardwareAddress(arpPacket.getSourceHardwareAddress());
		rArpPacket.setDestProtocolAddress(arpPacket.getSourceProtocolAddress());
		rArpPacket.setOperation(ARP_OPERATION_REPLY);
		sendReply(rArpPacket, arpPacket.getSourceHardwareAddress(), portNo);
	}
	
	private void getReply(ARPPacket arpPacket) {
		System.out.println("SET ARP " + Util.int2addr(arpPacket.getSourceProtocolAddress()) + " " + Util.long2addr(arpPacket.getSourceHardwareAddress()));
		table.setAddr(arpPacket.getSourceProtocolAddress(), arpPacket.getSourceHardwareAddress());
	}
	
	public void showArpTable() {
		table.show();
	}
}

class Table {
	private ArrayList<Cache> cacheList;
	
	public Table() {
		cacheList = new ArrayList<Cache>();
	}
	
	public void setAddr(int ipAddr, long macAddr) {
		Cache cache = getCacheForIpAddr(ipAddr);
		if (cache == null) {
			cache = new Cache(ipAddr, macAddr);
			cacheList.add(cache);
			Collections.sort(cacheList);
		} else {
			cache.macAddr = macAddr;
		}
	}
	
	public Cache getCacheForIpAddr(int ipAddr) {
		for (Cache cache: cacheList) { 
			if (cache.ipAddr == ipAddr) {
				return cache;
			}
		}
		return null;
	}

	public void show() {
		for (Cache cache: cacheList) { 
			System.out.println(Util.int2addr(cache.ipAddr) + " " + Util.long2addr(cache.macAddr));
		}
	}
}

class Cache implements Comparable<Cache> {
	int ipAddr;
	long macAddr;
	
	public Cache(int ipAddr, long macAddr) {
		this.ipAddr = ipAddr;
		this.macAddr = macAddr;
	}
	
	@Override
	public int compareTo(Cache other) {
        return this.ipAddr - other.ipAddr;
	}
}