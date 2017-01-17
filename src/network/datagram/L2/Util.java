package network.datagram.L2;

import java.util.Arrays;

public class Util {
	/**
	 * MACアドレス文字列を6バイト配列に変換する。
	 * @param addr
	 * @return
	 */
    public static byte[] addr2Bytes(String addr) {
        String[] addrs = addr.split("\\:", 0);
        byte[] bytes = new byte[6];
        bytes[0] = (byte)Integer.parseInt(addrs[0], 16);
        bytes[1] = (byte)Integer.parseInt(addrs[1], 16);
        bytes[2] = (byte)Integer.parseInt(addrs[2], 16);
        bytes[3] = (byte)Integer.parseInt(addrs[3], 16);
        bytes[4] = (byte)Integer.parseInt(addrs[4], 16);
        bytes[5] = (byte)Integer.parseInt(addrs[5], 16);
        return bytes;
    }

    /**
     * 6バイト配列をMACアドレス文字列に変換する。
     * @param bytes
     * @return
     */
    public static String bytes2Addr(byte[] bytes) {
        String addr = String.format("%02x", (bytes[0] & 0xFF)) + ":" +
            String.format("%02x", (bytes[1] & 0xFF)) + ":" +
            String.format("%02x", (bytes[2] & 0xFF)) + ":" +
            String.format("%02x", (bytes[3] & 0xFF)) + ":" +
            String.format("%02x", (bytes[4] & 0xFF)) + ":" +
            String.format("%02x", (bytes[5] & 0xFF));
        return addr;
    }
    
    public static boolean equalsAddr(byte[] A, byte[] B) {
    	if (A.length != B.length) return false;
    	for (int i=0;i<A.length;i++) {
    		if (A[i] != B[i]) return false;
    	}
    	return true;
    }
    
    public static boolean isLargeAddr(byte[] A, byte[] B, int length) {
    	for (int i=0;i<length;i++) {
    		if (A[i] > B[i]) return true;
    	}
    	return false;
    }
    
    public static boolean isSmallAddr(byte[] A, byte[] B, int length) {
    	for (int i=0;i<length;i++) {
    		if (A[i] < B[i]) return true;
    	}
    	return false;
    }
    
    public static boolean isSmallEqualAddr(byte[] A, byte[] B, int length) {
    	for (int i=0;i<length;i++) {
    		if (A[i] < B[i]) return true;
    	}
    	if (Arrays.equals(A, B)) return true;
    	return false;
    }
    
    public static byte[] longToBytes(long l, int length) {
    	byte[] bytes = new byte[length];
    	for (int i=0;i<length;i++) {
    		bytes[i] = (byte)((l >> i*8) & 0xFF); 
    	}
    	return bytes;
    }
    
    public static long bytesToLong(byte[] bytes, int length) {
    	long l = 0;
    	if (length > 8) length = 8;
    	for (int i=0;i<length;i++) {
    		l += ((long)bytes[i] << 8*i);
    	}
    	return l;
    }
}
