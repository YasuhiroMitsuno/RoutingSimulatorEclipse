package network.datagram.L3;

public class Util {
	/**
	 * IPアドレス文字列を4バイト配列に変換する。
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
    
    public static int addr2int(String addr) {
    	String[] addrs = addr.split("\\.", 0);
    	int val = 0;
    	val |= Integer.parseInt(addrs[0]) << 24;
    	val |= Integer.parseInt(addrs[1]) << 16;
    	val |= Integer.parseInt(addrs[2]) << 8;
    	val |= Integer.parseInt(addrs[3]);
    	return val;
    }
    
    public static long addr2long(String addr) {
    	String[] addrs = addr.split("\\:", 0);
    	long val = 0;
    	val |= Integer.parseInt(addrs[0], 16) << 40;
    	val |= Integer.parseInt(addrs[1], 16) << 32;
    	val |= Integer.parseInt(addrs[2], 16) << 24;
    	val |= Integer.parseInt(addrs[3], 16) << 16;
    	val |= Integer.parseInt(addrs[4], 16) << 8;
    	val |= Integer.parseInt(addrs[5], 16);
    	return val;
    }

    /**
     * 4バイト配列をIPアドレス文字列に変換する。
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
    
    public static String int2addr(int val) {
        String addr = String.format("%d", (val >> 24 & 0xFF)) + "." +
                String.format("%d", (val >> 16 & 0xFF)) + "." +
                String.format("%d", (val >> 8 & 0xFF)) + "." +
                String.format("%d", (val & 0xFF));
            return addr;
    }
    
    public static String long2addr(long val) {
        String addr = String.format("%02x", (val >> 40 & 0xFF)) + ":" +
            String.format("%02x", (val >> 32 & 0xFF)) + ":" +
            String.format("%02x", (val >> 24 & 0xFF)) + ":" +
            String.format("%02x", (val >> 16 & 0xFF)) + ":" +
            String.format("%02x", (val >> 8 & 0xFF)) + ":" +
            String.format("%02x", (val & 0xFF));
        return addr;
    }
    
    public static boolean equalsAddr(byte[] A, byte[] B) {
    	if (A.length != B.length) return false;
    	for (int i=0;i<A.length;i++) {
    		if (A[i] != B[i]) return false;
    	}
    	return true;
    }
    
	
	public static short byte2short(byte[] bytes, int offset) {
		short value = 0;
		for (int i=0;i<2;i++) {
			value |= (short)(bytes[offset+i] & 0xFF) << 8*(1 - i);
		}
		return value;
	}
	
	public static int byte2int(byte[] bytes, int offset) {
		int value = 0;
		for (int i=0;i<4;i++) {
			value |= (int)(bytes[offset+i] & 0xFF) << 8*(3- i);
		}
		return value;
	}
	
	public static long byte2long(byte[] bytes, int offset, int length) {
		long value = 0;
		for (int i=0;i<8;i++) {
			value |= (long)(bytes[offset+i] & 0xFF) << 8*(length - i - 1);
		}
		return value;
	}
	
	public static byte[] val2byte(short value) {
		byte[] bytes = new byte[2];
		bytes[0] = (byte)(value >> 8 & 0xFF); 
		bytes[1] = (byte)(value & 0xFF); 
		return bytes;
	}
	
	public static byte[] val2byte(int value) {
		byte[] bytes = new byte[4];
		bytes[0] = (byte)(value >> 24 & 0xFF); 
		bytes[1] = (byte)(value >> 16 & 0xFF); 
		bytes[2] = (byte)(value >> 8 & 0xFF); 
		bytes[3] = (byte)(value & 0xFF);
		return bytes;
	}
	
	public static byte[] val2byte(long value) {
		byte[] bytes = new byte[8];
		bytes[0] = (byte)(value >> 56 & 0xFF); 
		bytes[1] = (byte)(value >> 48 & 0xFF); 
		bytes[2] = (byte)(value >> 40 & 0xFF); 
		bytes[3] = (byte)(value >> 32 & 0xFF); 
		bytes[4] = (byte)(value >> 24 & 0xFF); 
		bytes[5] = (byte)(value >> 16 & 0xFF); 
		bytes[6] = (byte)(value >> 8 & 0xFF); 
		bytes[7] = (byte)(value & 0xFF);
		return bytes;
	}
}
