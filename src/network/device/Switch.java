package network.device;

import java.io.IOException;

import network.datagram.L2.Frame;
import network.datagram.L2.Util;

public class Switch extends Bridge {
    private byte[][] table;

    public Switch() {
        table = new byte[8][];
        for(int i=0;i<8;i++) {
            table[i] = new byte[6];
        }
    }
    
    public void showTable() {
        System.out.println("TABLE");
        for(int i=0;i<8;i++) {
            System.out.println(Util.bytes2Addr(table[i]));
        }
    }
    
    protected void test(Frame frame, int index) {
        table[index] = frame.getSource();
        showTable();
        byte[] data = frame.getBytes();
        /* search destination */
        boolean notExist = true;
        for(int i=0;i<8;i++) {
            Port port = this.ports[i];
            if (port.isConnected() &&  match(table[i],frame.getDestination())) {
                notExist = false;
                try {
                    try {
                        Thread.sleep(data.length);
                    } catch (Exception e) {
                    }
                    //port.write(data, 0, data.length);
                } catch (IOException e) {
                    System.out.println(e.getMessage());
                }                
            }
        }
        if (notExist) {
            //super.test(frame, index);
        }
    }

    private boolean match(byte[] a, byte[] b) {
        for(int i=0;i<6;i++) {
            if (a[i] != b[i]) return false;
        }
        return true;
    }
}
