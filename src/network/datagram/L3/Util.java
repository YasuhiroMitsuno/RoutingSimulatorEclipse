package network.datagram.L3;

public class Util {
	/**
	 * MACアドレス文字列を6バイト配列に変換する。
	 * @param addr
	 * @return
	 */
    public static byte[] addr2Bytes(String addr) {
        String[] addrs = addr.split("\\.", 0);
        byte[] bytes = new byte[4];
        bytes[0] = (byte)Integer.parseInt(addrs[0]);
        bytes[1] = (byte)Integer.parseInt(addrs[1]);
        bytes[2] = (byte)Integer.parseInt(addrs[2]);
        bytes[3] = (byte)Integer.parseInt(addrs[3]);
        return bytes;
    }

    /**
     * 6バイト配列をMACアドレス文字列に変換する。
     * @param bytes
     * @return
     */
    public static String bytes2Addr(byte[] bytes) {
        String addr = String.format("%d", (bytes[0] & 0xFF)) + "." +
            String.format("%d", (bytes[1] & 0xFF)) + "." +
            String.format("%d", (bytes[2] & 0xFF)) + "." +
            String.format("%d", (bytes[3] & 0xFF));
        return addr;
    }
    
    public static boolean equalsAddr(byte[] A, byte[] B) {
    	if (A.length != B.length) return false;
    	for (int i=0;i<A.length;i++) {
    		if (A[i] != B[i]) return false;
    	}
    	return true;
    }
}
