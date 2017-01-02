import network.datagram.L2.*;
import network.protocol.L2.STP;

public class Main {
	public static void main(String args[]) {
		STPFrame frame = new STPFrame();
		frame.setPortId(0x8006);
		frame.setBridgePriority(32779);
		frame.setBridgeAddress("00:29:1c:00:12:23");
		System.out.println(frame.description());
		STP stp = new STP();
		stp.start();
	}
}
