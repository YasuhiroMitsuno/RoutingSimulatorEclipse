import network.datagram.L2.*;
import network.device.Device;
import network.protocol.L2.STP.STP;
import network.protocol.L2.STP.STPFrame;
import network.device.Bridge;
import network.device.BridgeFactory;

public class Main {
	public static void main(String args[]) {
		BridgeFactory hubFactory = new BridgeFactory();
		Bridge hub = (Bridge)hubFactory.create();
		Bridge hub2 = (Bridge)hubFactory.create();
		Bridge hub3 = (Bridge)hubFactory.create();		
		Device.connect(hub, hub2);
		Device.connect(hub2, hub3);
		Device.connect(hub3, hub);
	}
}
