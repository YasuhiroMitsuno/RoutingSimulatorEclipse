package network.protocol.L3;

import javax.management.relation.RoleInfo;

import network.datagram.L2.Frame;
import network.datagram.L3.ICMPD;
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
	
	public void setIP(int portNo, byte[] addr, byte[] mask) {
		System.arraycopy(addr, 0, portInfo[portNo].addr, 0, 4);
		System.arraycopy(mask, 0, portInfo[portNo].mask, 0, 4);		
	}
	
	public int sameNetworkPort(byte[] addr) {
		for (int portNo=0;portNo<portSize;portNo++) {
			if (IPv4.isSameNetwork(addr, portInfo[portNo].addr, portInfo[portNo].mask)) {
				return portNo;
			}
		}
		return -1;
	}
	
	public void setRoute(byte[] addr, byte[] mask, byte[] next) {
		routeInfo.setRoute(addr, mask, next);
	}
	
	public int nextPort(byte[] addr) {
		int portNo;
		portNo = sameNetworkPort(addr);
		if (portNo == -1) {
			portNo = sameNetworkPort(routeInfo.getNextHop(addr));
		}
		return portNo;
	}
	
	public void receivedPacket(Packet packet, int fromPortNo) {
		if (Util.equalsAddr(portInfo[fromPortNo].addr ,packet.getDestination())) {
			System.out.println("GET PACKET");
			if (packet.getProtocol() == 1) {
				icmp.receive(packet);			
			}
		} else if (enableForward) {
			int toPortNo = nextPort(packet.getDestination());
			if (toPortNo != -1) {
				System.out.println(toPortNo + "AAAAA");
				packet.setTTL(packet.getTTL()-1);
				sendPacket(packet);
			}
		}
	}
	
	public void ping(byte[] addr) {
		icmp.ping(addr);
	}
	
	public void sendData(byte[] data, byte[] source, byte[] destination, int protocol, int ttl) {
		if (source == null) {
			int portNo = nextPort(destination);
			System.out.println(portNo + " " + Util.bytes2Addr(destination));			
			source = portInfo[portNo].addr;
		}
		Packet packet = new Packet();
		packet.setSource(source);
		packet.setDestination(destination);
		packet.setProtocol(protocol);
		packet.setTTL(ttl);
		packet.setData(data);
		sendPacket(packet);
	}
	
	private void sendPacket(Packet packet) {
		int portNo = nextPort(packet.getDestination());
		System.out.println("BBB" + portNo);
    	delegate.sendData(packet.getBytes(), portNo);
	}
	
	public static boolean isSameNetwork(byte[] addr1, byte[] addr2, byte[] mask) {
		boolean same = true;
		for(int i=0;i<4;i++) {
			if ((addr1[i] & mask[i]) != (addr2[i] & mask[i])) {
				same = false;
			}
		}
		return same;
	}
}

class RouteInfo {
	final static int TABLE_SIZE = 100;
	private int count = 0;
	private RouteData[] routeData = new RouteData[TABLE_SIZE];
	
	public RouteInfo() {
		for (int i=0;i<TABLE_SIZE;i++) {
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
				if (isSmall(routeData[i], routeData[j])) {
					tmp = routeData[i];
					routeData[i] = routeData[j];
					routeData[j] = tmp;
				}
			}
		}
	}
	
	class RouteData {
		byte[] addr = new byte[4];
		byte[] mask = new byte[4];
		byte[] next = new byte[4];
	}
}

class PortData {
	int 	portId;
	byte[] 	addr = new byte[4];
	byte[] 	mask = new byte[4];
}