import network.datagram.L2.*;
import network.device.Device;
import network.protocol.L2.STP.STP;
import network.device.Bridge;
import network.device.BridgeFactory;

public class Main {
	public static void main(String args[]) {
		STPFrame frame = new STPFrame();
		frame.setPortId(0x8006);
		frame.setBridgePriority(32779);
		frame.setBridgeAddress("00:29:1c:00:12:23");
		System.out.println(frame.description());
		BridgeFactory hubFactory = new BridgeFactory();
		Bridge hub = (Bridge)hubFactory.create();
		Bridge hub2 = (Bridge)hubFactory.create();
		Device.connect(hub, hub2);
	}
}
