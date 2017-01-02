package network.protocol.L4;

import network.datagram.L4.TCPSegment;

public class TCP {
    public static void read(TCPSegment segment) {
	System.out.println(segment.description());
    }
}
