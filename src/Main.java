import network.datagram.L2.*;
import network.device.Device;
import network.protocol.L2.STP.STP;
import network.device.L2Switch;
import network.device.L2SwitchFactory;

public class Main {
	public static void main(String args[]) {
		L2SwitchFactory hubFactory = new L2SwitchFactory();
		L2Switch hub = (L2Switch)hubFactory.create();
		L2Switch hub2 = (L2Switch)hubFactory.create();
		L2Switch hub3 = (L2Switch)hubFactory.create();		
		Device.connect(hub, hub2);
		Device.connect(hub2, hub3);
		Device.connect(hub3, hub);
	}
}
