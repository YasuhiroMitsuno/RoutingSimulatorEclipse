package network.protocol.L3;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import javax.management.relation.RoleInfo;

import network.datagram.L2.Frame;
import network.datagram.L3.ICMPDatagram;
import network.datagram.L3.Packet;
import network.datagram.L3.Util;
import network.datagram.L4.TCPSegment;
import network.device.Device;
import network.protocol.L2.STP.STP.State;
import network.protocol.L4.TCP;

public class IPv4 {
    public enum Protocol {
        ICMP(1), IGMP(2), IP(4), TCP(6), UDP(17);
        private final int num;

        private Protocol(final int num) {
            this.num = num;
        }
        
        public int getNum() {
        	return this.num;
        }

        public static String getName(int num) {
            for (Protocol protocol : Protocol.values()) {
                if (protocol.num == num) {
                    return protocol.name();
                }
            }
            return "None";
        }
        public static Protocol getProtocol(int num) {
            for (Protocol protocol : Protocol.values()) {
                if (protocol.num == num) {
                    return protocol;
                }
            }
            return null;
        }
    }
    
    public static void read(Packet packet) {
	System.out.println(packet.description());
        if (packet.verifyChecksum() != 0) {
            System.out.println("######## Check Sum is wrong ########");
            return;
        }

        if ((packet.getFlags() & 0x001) == 1 || (packet.getOffset() != 0)) {
            System.out.println("######## Fragment ########");
	    restruct(packet);
            return;
	}

        switch(Protocol.getProtocol(packet.getProtocol())) {
	case ICMP:
            /* Internet Control Management Protocol */
	    break;
	case IGMP:
            /* Internet Group Management Protocol */
	    break;
	case IP:
            /* IP in IP */
	    break;
	case TCP:
            /* Transmission Control Protocol */
	    TCPSegment segment = new TCPSegment(packet);
	    TCP.read(segment);
	    break;
	case UDP:
            /* User Diagram Protocol */
	    break;
	}
    }

    static int[] test;
    static byte[] buffer;
    static boolean[] bool;
    public static void restruct(Packet packet) {
	if (buffer == null) {
	    buffer = new byte[65536];
	    bool = new boolean[8192];
	}
	if ((packet.getFlags() & 0x01) == 0) {
	    int size = packet.getOffset() * 8 + packet.getData().length;
	    byte[] newBuffer = new byte[size];
	    boolean[] newBool = new boolean[(size+7)/8];
	    System.arraycopy(buffer, 0, newBuffer, 0, size);
	    System.arraycopy(bool, 0, newBool, 0, (size+7)/ 8);
	    buffer = newBuffer;
	    bool = newBool;
	}

	int length = packet.getData().length;
	int offset = packet.getOffset();
	System.arraycopy(packet.getData(), 0, buffer, packet.getOffset() * 8, length);
	for (int i=0;i<(length+7)/8;i++) {
	    bool[offset + i] = true;
	}
	
	boolean res = true;
	for(int i=0;i<bool.length;i++) {
	    if (bool[i] == false) {
		res = false;
	    }
	}
	if (res) {
            Packet p = Packet.getHeader(packet);
	    p.setFlags(0x00);
	    p.setOffset(0);
	    p.setData(buffer);
	    read(p);
	}
    }

    public static Packet[] makeFragment(Packet packet, int MTU) {
        /* 8の倍数で計算する */
        int PMTU = (MTU-20) - ((MTU-20) % 8);
        /* check enable IP Fragmentation */
        if ((packet.getFlags() & 0x02) > 0) {
            /* exception */
        }

        byte[] data = packet.getData();
        /* 分割後の個数 */
        int fsize = (data.length + PMTU - 1)/PMTU;

        Packet[] packets = new Packet[fsize];
        for(int i=0;i<fsize;i++) {
            Packet p = Packet.getHeader(packet);
            /*  */
            int length;
            if (i < fsize-1) {
                length = PMTU;
                p.setFlags(p.getFlags() | 0x01);
            } else {
                length = data.length - (fsize - 1) * PMTU;
                p.setFlags(p.getFlags() | 0x00);
            }

            byte[] tmp = new byte[length];
            System.arraycopy(data, PMTU * i, tmp, 0, length);
            p.setOffset(packet.getOffset() + (PMTU * i)/ 8);
            p.setData(tmp);
            packets[i] = p;
        }
        return packets;
    }
    
	protected int portSize;
	protected PortData[] portInfo;
	protected RouteInfo routeInfo;
	protected Device delegate;
	protected boolean enableForward;
	protected ICMP icmp;
	
	public IPv4(Device delegate) {
		this.delegate = delegate;
		this.portSize = delegate.getPortSize();
		portInfo = new PortData[portSize];
		for (int portNo=0;portNo<portSize;portNo++) {
			portInfo[portNo] = new PortData();
		}
		routeInfo = new RouteInfo();
		icmp = new ICMP(this);
		enableForward = false;
	}
	
	public void setEnableForward(boolean enableForward) {
		this.enableForward = enableForward;
	}
	
	public void setIP(int portNo, int addr, int mask) {
		portInfo[portNo].addr = addr;
		portInfo[portNo].mask = mask;
	}
	

	
	public int sameNetworkPort(int addr) {
		for (int portNo=0;portNo<portSize;portNo++) {
			if (IPv4.isSameNetwork(addr, portInfo[portNo].addr, portInfo[portNo].mask)) {
				return portNo;
			}
		}
		return -1;
	}
	
	public int getPortForAddress(int addr) {
		for (int portNo=0;portNo<portSize;portNo++) {
			if (IPv4.isSameAddress(addr, portInfo[portNo].addr)) {
				return portNo;
			}
		}
		return -1;
	}
	
	public void setRoute(int addr, int mask, int next) {
		routeInfo.setRoute(addr, mask, next);
	}
	
	public int nextPort(int addr) {
		int portNo;
		portNo = sameNetworkPort(addr);
		if (portNo == -1) {
			portNo = sameNetworkPort(routeInfo.getNextHop(addr));
		}
		return portNo;
	}
	
	public int nextHop(int addr) {
		if (sameNetworkPort(addr) == -1) {
			return routeInfo.getNextHop(addr);
		}
		return addr;
	}
	
	public void receivedPacket(Packet packet, int fromPortNo) {
    	System.out.println(packet.description());
    	
		if (packet.getProtocol() == 1) {
			icmp.receive(packet);			
		} else if (enableForward) {
			int toPortNo = nextPort(packet.getDestination());
			if (toPortNo != -1) {
				System.out.println(toPortNo + "AAAAA");
				packet.setTTL(packet.getTTL()-1);
				sendPacket(packet);
			}
		}
	}
	
	public void ping(int addr) {
		icmp.ping(addr);
	}
	
	public void showIpAddr() {
		for (int portNo=0;portNo<portSize;portNo++) {
			System.out.println(portNo + " " +
					Util.int2addr(portInfo[portNo].addr) + " " + 
					Util.int2addr(portInfo[portNo].mask));
		}
	}
	
	public void showIpRoute() {
		routeInfo.show();
	}
	
	public void sendData(byte[] data, int source, int destination, int protocol, int ttl) {
		Packet packet = new Packet();
		packet.setSource(source);
		packet.setDestination(destination);
		packet.setProtocol(protocol);
		packet.setTTL(ttl);
		packet.setData(data);
		sendPacket(packet);
	}
	
	public void forwardPacket(Packet packet) {
		packet.setTTL(packet.getTTL() - 1);
		sendPacket(packet);
	}
	
	private void sendPacket(Packet packet) {
		int portNo = nextPort(packet.getDestination());
		int nextHop = nextHop(packet.getDestination());
		long macAddr = delegate.arp.getMacAddrForIpAddr(nextHop);
		if (macAddr == -1) {
			System.out.println("NO MAC ADDRESS FOR" + Util.int2addr(packet.getDestination()));
			delegate.arp.arp(packet.getDestination());
			return;
		}
    	delegate.unicastData(packet.getBytes(), packet.getBytes().length, macAddr, portNo);
	}
	
	public static boolean isSameNetwork(int addr1, int addr2, int mask) {
		return ((addr1 & mask) == (addr2 & mask));
	}
	
	public static boolean isSameAddress(int addr1, int addr2) {
		return (addr1 == addr2);
	}
}

class RouteInfo {
	private ArrayList<RouteData> routeDataList;
	
	public RouteInfo() {
		routeDataList = new ArrayList<RouteData>();
	}
	
	public void show() {
		for (RouteData routeData: routeDataList) {
			System.out.println(Util.int2addr(routeData.addr) + " " + 
									Util.int2addr(routeData.mask) + " " + 
									Util.int2addr(routeData.next));
		}
	}
	
	public void setRoute(int addr, int mask, int next) {
		RouteData routeData = getRouteDataForAddr(addr, mask);
		if (routeData == null) {
			routeData = new RouteData(addr, mask, next);
			routeData.addr = addr;
			routeData.mask = mask;
			routeData.next = next;
			routeDataList.add(routeData);
			Collections.sort(routeDataList);
		} else {
			routeData.next = next;
		}
	}
	
	public RouteData getRouteDataForAddr(int addr, int mask) {
		for (RouteData routeData: routeDataList) {
			if (routeData.addr == addr && routeData.mask == mask) {
				return routeData;
			}
		}
		return null;
	}
	
	public int getNextHop(int addr) {
		for (RouteData routeData: routeDataList) {
			if (IPv4.isSameNetwork(addr, routeData.addr, routeData.mask)) {
				return  routeData.next;
			}
		}
		return 0;
	}
	
	class RouteData implements Comparable<RouteData> {
		int addr;
		int mask;
		int next;
		
		public RouteData(int addr, int mask, int next) {
			this.addr = addr;
			this.mask = mask;
			this.next = next;
		}
		
		@Override
		public int compareTo(RouteData o) {
			if (this.mask != o.mask) {
				return this.mask - o.mask;
			}
			return this.addr - o.addr;
		}
	}
}

class PortData {
	int 	portId;
	int 	addr;
	int 	mask = 255;
}