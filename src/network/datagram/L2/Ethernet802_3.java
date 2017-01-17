package network.datagram.L2;

public class Ethernet802_3 {
    private byte[] bytes;       /* Binary Data */
    private byte[] destination; /* Destination Address */
    private byte[] source;      /* Source Address */
    private int typeOrLenght;
    private byte[] FCS;
}
