package network.protocol.L3;

import network.datagram.L3.Packet;
import network.datagram.L4.TCPSegment;
import network.protocol.L4.TCP;

public class IPv4 {
    public enum Protocol {
        ICMP(1), IGMP(2), IP(4), TCP(6), UDP(17);
        private final int num;

        private Protocol(final int num) {
            this.num = num;
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
}

