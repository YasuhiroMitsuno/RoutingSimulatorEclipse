package network.protocol.L3;

import java.util.Arrays;
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
		return table.getMacAddrForIpAddr(ipAddr);
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
		table.setAddr(arpPacket.getSourceProtocolAddress(), arpPacket.getSourceHardwareAddress());
	}
	
	public void showArpTable() {
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
	
	public void setAddr(int ipAddr, long macAddr) {
		int num = getNumberForAddr(ipAddr);
		if (num == -1) {
			cache[count].ipAddr = ipAddr;
			cache[count].macAddr = macAddr;
			count++;
			Arrays.sort(cache);
		} else {
			cache[num].macAddr = macAddr;
		}
	}
	
	public long getMacAddrForIpAddr(int ipAddr) {
		int num = getNumberForAddr(ipAddr);
		if (num == -1) {
			return -1;
		}
		return cache[num].macAddr;
	}
	
	private int getNumberForAddr(int ipAddr) {
		for (int i=0;i<count;i++) {
			if (cache[i].ipAddr == ipAddr) {
				return i;
			}
		}
		return -1;
	}
	
	public void show() {
		for (int i=0;i<count;i++) {
			System.out.println(Util.int2addr(cache[i].ipAddr) + " " + Util.long2addr(cache[i].macAddr));
		}
	}
}

class Cache implements Comparable<Cache> {
	int ipAddr;
	long macAddr;
	
	@Override
	public int compareTo(Cache other) {
        return this.ipAddr - other.ipAddr;
	}
}